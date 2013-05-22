/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
import java.util.Map;
import java.util.Properties;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.MemoryManager; 
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.CompletionHandler;
import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;

/**
 *
 * @author amyk
 */
public class StompMessageDispatchFilter extends BaseFilter implements StompOutputHandler {
     
     protected static final String STOMP_PROTOCOL_HANDLER_ATTR = "stomp-protocol-handler";
     private Logger _logger = null;

     private BridgeContext _bc = null;
     private Properties _jmsprop = null;
     private StompBridgeResources _sbr = null;

     public StompMessageDispatchFilter() {
         _bc = StompServer._bc;
         _jmsprop = StompServer.jmsprop;
         _logger = StompServer.logger();
         _sbr = StompServer.getStompBridgeResources();
     }

     public StompMessageDispatchFilter(BridgeContext bc, Properties jmsprop) {
         _logger = StompServer.logger();
         _bc = bc;
         _jmsprop = jmsprop;
         _sbr = StompServer.getStompBridgeResources();
     }
    
     @Override
     public NextAction handleRead(final FilterChainContext ctx) throws IOException {
         synchronized(this) {
             if (_bc == null) {
                 _bc = StompServer._bc;
                 _jmsprop = StompServer.jmsprop;
                 _logger = StompServer.logger();
                 _sbr = StompServer.getStompBridgeResources();
             }
             if (_bc == null || _jmsprop == null ||
                 _logger == null || _sbr == null) {
                 if (_logger != null) {
                     _logger.log(Level.WARNING, "Stomp service not ready yet");
                 }
                 throw new IOException("Stomp service not ready yet");
             }
         }
         final Connection conn = ctx.getConnection();

        StompProtocolHandler sph = null;
        try {

        final StompFrameMessage msg = ctx.getMessage();
        sph = (StompProtocolHandler)ctx.getAttributes().
              getAttribute(StompMessageFilter.STOMP_PROTOCOL_HANDLER);

        switch (msg.getCommand()) { 
            case CONNECT:
                sph.onCONNECT(msg, this, ctx);
                break; 
            case SEND:
                sph.onSEND(msg, this, ctx);
                break; 
            case SUBSCRIBE:
                StompOutputHandler soh = new AsyncStompOutputHandler(ctx, sph, _bc);

                sph.onSUBSCRIBE(msg, this, ctx, soh);

                return ctx.getSuspendingStopAction(); 

            case UNSUBSCRIBE:
                sph.onUNSUBSCRIBE(msg, this, ctx);
                break; 
            case BEGIN:
                sph.onBEGIN(msg, this, ctx);
                break; 
            case COMMIT:
                sph.onCOMMIT(msg, this, ctx);
                break; 
            case ABORT:
                sph.onABORT(msg, this, ctx);
                break; 
            case ACK:
                sph.onACK(msg, this, ctx);
                break; 
            case DISCONNECT:
                sph.onDISCONNECT(msg, this, ctx);
                break; 
            case ERROR:
                sendToClient(msg, ctx, sph);
                break; 
            default: 
                throw new IOException(
                "Internal Error: unexpected STOMP frame "+msg.getCommand());
        }
 
        } catch (Throwable t) {
            _logger.log(Level.SEVERE,  t.getMessage(), t);
            try {

            StompFrameMessage err = StompProtocolHandler.toStompErrorMessage("StompProtocolFilter", t);
            sendToClient(err, ctx, sph);

            } catch (Exception e) {
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_UNABLE_SEND_ERROR_MSG, t.toString(), e.toString()), e);
            }
        }
        return ctx.getInvokeAction();
    }

    public void sendToClient(StompFrameMessage msg) throws Exception {
        throw new UnsupportedOperationException("sendToclient(msg)");
    }
    
    public void sendToClient(final StompFrameMessage msg, 
                             final FilterChainContext ctx,
                             StompProtocolHandler sph) throws Exception {
        boolean closechannel = false;
        try {
            if (msg.getCommand() == StompFrameMessage.Command.ERROR) {
                if (msg.isFatalERROR()) {
                    closechannel = true;
                }
            }
            ctx.write(msg, true);

        } catch (Exception e) {
            if (e instanceof java.nio.channels.ClosedChannelException ||
                e.getCause() instanceof java.nio.channels.ClosedChannelException) { 
                _logger.log(Level.WARNING, _sbr.getKString(
                  _sbr.W_EXCEPTION_ON_SEND_MSG, msg.toString(), e.toString()));
                if (sph != null) {
                    sph.close(false);
                }
            }
            throw e;
        } finally {
            if (closechannel) {
                GrizzlyFuture f = ctx.getConnection().close();              
                try {
                    f.get();
                } catch (Exception ee) {
                    _logger.log(Level.WARNING, ee.getMessage(), ee);
                }
            }
        }
    }
    
}
