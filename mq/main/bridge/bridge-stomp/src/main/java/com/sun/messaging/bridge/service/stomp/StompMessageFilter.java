/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.messaging.bridge.service.stomp;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.Charset;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.attributes.NullaryFunction;
import org.glassfish.grizzly.utils.BufferInputStream;
import org.glassfish.grizzly.utils.BufferOutputStream;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.attributes.AttributeHolder;
import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompProtocolHandler;
import com.sun.messaging.bridge.api.StompFrameParseException;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;
import com.sun.messaging.jmq.util.LoggerWrapper;

/**
 * Parse bytes into a STOMP protocol frame message.
 *
 * @author amyk
 */

public class StompMessageFilter extends BaseFilter {

    protected static final String STOMP_PROTOCOL_HANDLER = "STOMP_PROTOCOL_HANDLER"; 
    private final String _OOMMSG = "Running low on memory while parsing stomp incoming data"; 

    private LoggerWrapper logger = null;
    private BridgeContext _bc = null;
    private Properties _jmsprop = null;
    private StompServer server = null;
	 
    private final Attribute<PacketParseState> parsestateAttr =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(
            StompMessageFilter.class + ".parsestateAttr",
            new NullaryFunction<PacketParseState>() {

                @Override
                public PacketParseState evaluate() {
                    return new PacketParseState();
                }
            });

     private final Attribute<StompProtocolHandler> sphAttr =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(
            StompMessageFilter.class + ".sphAttr",
            new NullaryFunction<StompProtocolHandler>() {

                @Override
                public StompProtocolHandler evaluate() {
                    return new StompProtocolHandlerImpl(server);
                }
            });

    protected StompMessageFilter(StompServer server) {
        this.server = server;
        _bc = server.getBridgeContext();
        _jmsprop = server.getJMSConfig();
        logger = new LoggerWrapperImpl(server.getLogger());
    }

    @Override
    public NextAction handleClose(FilterChainContext ctx)
    throws IOException {
        Connection c = ctx.getConnection();
        StompProtocolHandler sph = sphAttr.get(c);
        if (sph != null) {
            sph.close(false);
        }
        if (logger.isFineLoggable()) {
            logger.logFine(this+", conn=@"+c.hashCode()+
                ", sph=@"+(sph == null ? "null":sph.hashCode()), null);
        }
        return super.handleClose(ctx);
    }

    /** 
     * @param ctx Context of {@link FilterChainContext} processing
     * @return the next action
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleRead(final FilterChainContext ctx)
    throws IOException {
        BridgeContext bc = null;
        synchronized(this) {
            if (_bc == null || _jmsprop == null || logger == null) {
                if (logger != null) {
                    logger.logWarn("Stomp Service not ready yet", null);
                }
                throw new IOException("Stomp service not ready yet");
            }
            bc = _bc;
        }

        final Connection c = ctx.getConnection();
        StompProtocolHandler sph = sphAttr.get(c);

        if (logger.isFinestLoggable()) {
            logger.logFinest(this+", conn=@"+c.hashCode()+
                ", sph=@"+(sph == null ? "null":sph.hashCode()), null);
        }
        AttributeHolder ah = ctx.getAttributes();
        ah.setAttribute(STOMP_PROTOCOL_HANDLER, sph);

        final Buffer input = ctx.getMessage();

        final PacketParseState parsestate = parsestateAttr.get(c);
        int pos = input.position();

        StompFrameMessageImpl _message = null;
        try {

        if (logger.isFinestLoggable()) {
            logger.logFinest(this+", position="+pos+", input="+input, null);
        }

        if (parsestate.message == null) {

            if (input.remaining() >= StompFrameMessage.MIN_COMMAND_LEN) {
                parsestate.message = StompFrameMessageImpl.parseCommand(input, logger);

                if (logger.isFinestLoggable()) {
                    logger.logFinest("returned from parseCommand with "+parsestate.message, null);
                }
            }

            if (parsestate.message == null) {
                input.position(pos);
                return ctx.getStopAction(input);
            }
        }

        _message = parsestate.message;

        if (_message.getNextParseStage() == StompFrameMessage.ParseStage.HEADER) {
            _message.parseHeader(input);

            if (logger.isFinestLoggable()) {
                logger.logFinest("returned from parseHeader", null);
            }

        }
        if (_message.getNextParseStage() == StompFrameMessage.ParseStage.BODY) { 
            _message.readBody(input);
        }
        if (_message.getNextParseStage() == StompFrameMessage.ParseStage.NULL) { 
            _message.readNULL(input);
        }
        if (logger.isFinestLoggable()) {
            logger.logFinest(
                "position="+input.position()+", input="+input+
                ", nextParseState="+_message.getNextParseStage(), null);
        }

        if (_message.getNextParseStage() != StompFrameMessage.ParseStage.DONE) { 
            if (logger.isFinestLoggable()) {
                logger.logFinest("StopAction with position="+input.position()+
                                 ", hasRemaining="+input.hasRemaining(), null);
            }
            return ctx.getStopAction((input.hasRemaining() ? input:null));
        }

        ctx.setMessage(_message);

        Exception pex = _message.getParseException();
        if (pex != null) {
            if (pex instanceof StompFrameParseException) {
                _message = (StompFrameMessageImpl)((StompFrameParseException)pex).
                    getStompMessageERROR(StompFrameMessageImpl.getFactory(), logger);
            } else {
                _message = (StompFrameMessageImpl)(new StompFrameParseException(
                    pex.getMessage(), pex)).getStompMessageERROR(
                    StompFrameMessageImpl.getFactory(), logger);
            }
            ctx.setMessage(_message);
            parsestate.reset();
            return ctx.getInvokeAction();
        }
        final Buffer remainder = input.split(input.position());
        parsestate.reset();
        return ctx.getInvokeAction(remainder.hasRemaining() ? remainder : null);

        } catch (Throwable t) {
            if (t instanceof OutOfMemoryError) { 
                logger.logSevere(_OOMMSG, null);
                bc.handleGlobalError(t, _OOMMSG);
            } else {
                logger.logSevere(StompServer.getStompBridgeResources().getKString(
                    StompBridgeResources.E_PARSE_INCOMING_DATA_FAILED, t.getMessage()), t); 
            }
            try {

            if (t instanceof StompFrameParseException) {
                _message = (StompFrameMessageImpl)((StompFrameParseException)t).
                    getStompMessageERROR(StompFrameMessageImpl.getFactory(), logger);
                _message.setFatalERROR();
            } else {
                _message = (StompFrameMessageImpl)(new StompFrameParseException(t.getMessage(), t, true)).
                           getStompMessageERROR(StompFrameMessageImpl.getFactory(), logger);
            }

            } catch (Throwable tt) {

            if (t instanceof OutOfMemoryError) {
                _message = (StompFrameMessageImpl)StompFrameParseException.OOMMSG;
            } else {
                logger.logSevere(StompServer.getStompBridgeResources().getKString(
                    StompBridgeResources.E_UNABLE_CREATE_ERROR_MSG, t.getMessage()), tt);
                RuntimeException re = new RuntimeException(tt.getMessage());
                re.initCause(tt);
                throw re;
            }
            }
            ctx.setMessage(_message);
            parsestate.reset();
            return ctx.getInvokeAction();
        }
    }

    @Override
    public NextAction handleWrite(final FilterChainContext ctx)
    throws IOException {
        final StompFrameMessageImpl message = ctx.getMessage();

        final MemoryManager mm = ctx.getConnection().
                            getTransport().getMemoryManager();
        ctx.setMessage(((Buffer)message.marshall(mm).getWrapped()));

        return ctx.getInvokeAction();
    }

    static final class PacketParseState {
        StompFrameMessageImpl message = null;

        void reset() {
            message = null;
        }
    }

}
