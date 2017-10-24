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
import java.io.OutputStream;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.utils.BufferOutputStream;
import com.sun.messaging.jmq.util.LoggerWrapper;
import com.sun.messaging.bridge.api.ByteBufferWrapper;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompFrameMessageFactory;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;

/**
 * @author amyk 
 */
public class StompFrameMessageImpl extends StompFrameMessage {

    private static final StompBridgeResources sbr = StompServer.getStompBridgeResources();
    private static final StompFrameMessageFactory factory = new StompFrameMessageFactoryImpl();

    static class StompFrameMessageFactoryImpl implements StompFrameMessageFactory { 
        public StompFrameMessage newStompFrameMessage(Command cmd, LoggerWrapper logger) {
            return new StompFrameMessageImpl(cmd, logger);
        }
    }

    protected static StompFrameMessageFactory getFactory() {
        return factory;
    }

    protected StompFrameMessageImpl(Command cmd, LoggerWrapper logger) {
        super(cmd, logger);
    } 

    public static StompFrameMessageImpl parseCommand(
        Buffer buf, LoggerWrapper logger) throws Exception {
        return (StompFrameMessageImpl)StompFrameMessage.parseCommand(
                new ByteBufferWrapperImpl(buf), logger, factory);
    }
         
    public void parseHeader(Buffer buf) throws Exception {
        super.parseHeader(new ByteBufferWrapperImpl(buf));
    }

    public void readBody(Buffer buf) throws Exception {
        super.readBody(new ByteBufferWrapperImpl(buf));
    } 

    public void readNULL(Buffer buf) throws Exception {
        super.readNULL(new ByteBufferWrapperImpl(buf));
    }

    @Override
    protected OutputStream newBufferOutputStream(Object obj) throws IOException {
        MemoryManager mm = (MemoryManager)obj;
        return new BufferOutputStream(mm);
    }

    @Override
    protected ByteBufferWrapper getBuffer(OutputStream os) throws IOException {
        BufferOutputStream bos = (BufferOutputStream)os;
        return new ByteBufferWrapperImpl(bos.getBuffer());
    }

    @Override
    protected String getKStringX_CANNOT_PARSE_BODY_TO_TEXT(String cmd, String emsg) {
        return sbr.getKString(sbr.X_CANNOT_PARSE_BODY_TO_TEXT, cmd, emsg);
    }
    @Override
    protected String getKStringX_HEADER_NOT_SPECIFIED_FOR(String headerName, String cmd) {
        return sbr.getKString(sbr.X_HEADER_NOT_SPECIFIED_FOR, headerName, cmd);
    }
    @Override
    protected String getKStringX_INVALID_HEADER_VALUE(String headerValue, String cmd) {
        return sbr.getKString(sbr.X_INVALID_HEADER_VALUE, headerValue, cmd);
    }
    @Override
    protected String getKStringX_INVALID_HEADER(String headerName) {
        return sbr.getKString(sbr.X_INVALID_HEADER, headerName);
    }
    @Override
    protected String getKStringX_MAX_HEADERS_EXCEEDED(int maxHeaders) {
        return sbr.getKString(sbr.X_MAX_HEADERS_EXCEEDED, maxHeaders);
    }
    @Override
    protected String getKStringX_EXCEPTION_PARSE_HEADER(String headerName, String emsg) {
        return sbr.getKString(sbr.X_EXCEPTION_PARSE_HEADER, headerName, emsg);
    }
    @Override
    protected String getKStringX_NO_NULL_TERMINATOR(String contentlen) {
        return sbr.getKString(sbr.X_NO_NULL_TERMINATOR, contentlen);
    }
    @Override
    protected String getKStringX_UNKNOWN_STOMP_CMD(String cmd) {
        return sbr.getKString(sbr.X_UNKNOWN_STOMP_CMD, cmd);
    }
    @Override
    protected String getKStringX_MAX_LINELEN_EXCEEDED(int maxbytes) {
        return sbr.getKString(sbr.X_MAX_LINELEN_EXCEEDED, Integer.valueOf(maxbytes));
    }

    private static class ByteBufferWrapperImpl implements ByteBufferWrapper<Buffer> {
        private Buffer buf =  null;

        public ByteBufferWrapperImpl(Buffer buf) {
            this.buf = buf;
        }

        public Buffer getWrapped() {
            return buf;
        }

        public int position() {
            return buf.position();
        }

        public ByteBufferWrapper position(int newPosition) {
            buf.position(newPosition);
            return this;
        }

        public boolean hasRemaining() {
            return buf.hasRemaining();
        }

        public int remaining() {
            return buf.remaining();
        }

        public ByteBufferWrapper flip() {
            buf.flip();
            return this;
        }

        public byte get() {
            return buf.get();
        }
    }
}


