/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsclient.protocol.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.websocket.Encoder;
import javax.websocket.Decoder;
import javax.websocket.Session;
import javax.websocket.Endpoint;
import javax.websocket.EncodeException;
import javax.websocket.DecodeException;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.websocket.MessageHandler;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.ClientEndpointConfig;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.io.ReadWritePacket;
import com.sun.messaging.jmq.io.BigPacketException;
import com.sun.messaging.jmq.io.ByteBufferOutput;
import com.sun.messaging.jmq.jmsclient.MQAddress;
import com.sun.messaging.jmq.jmsclient.ConnectionImpl;
import com.sun.messaging.jmq.jmsclient.ConnectionHandler;
import com.sun.messaging.jmq.jmsclient.Debug;

/**
 * @author amyk
 */
public class WebSocketConnectionHandler extends Endpoint implements 
ConnectionHandler, MessageHandler.Whole<ByteBuffer> 
{
	
    private static final boolean debug = Debug.debug;
    private static final Logger logger =  ConnectionImpl.getConnectionLogger();
    private static final String WEBSOCKET_JMS_PATH = "/mqjms";
    private static final String DEFAULT_WS_SERVICE_NAME = MQAddress.DEFAULT_WS_SERVICE;
    private static final String DEFAULT_WSS_SERVICE_NAME = MQAddress.DEFAULT_WSS_SERVICE;
    private static final int DEFAULT_ASYNC_SEND_TIMEOUT = 0;
    private static final int DEFAULT_MAX_BINARY_BUFFER_SIZE = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_TEXT_BUFFER_SIZE = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_SESSION_IDLE_TIMEOUT = 0;

    private Object sessionLock = new Object(); 
    private Session session = null;
    private boolean closed = false;
    private ReadWritePacket packetRead = null;
    private ReadWritePacket packetPending = null;
    private final CountDownLatch onOpenLatch = new CountDownLatch(1);

    private ConnectionImpl conn = null;
    private MQAddress mqaddr = null;
    private int connectTimeout = 0;
	
    public WebSocketConnectionHandler(MQAddress addr, ConnectionImpl conn) {
        this.mqaddr = addr;
        this.connectTimeout = conn.getSocketConnectTimeout();
        this.conn = (ConnectionImpl)conn;
    }
    
    /*************************************************************
     * Implements ConnectionHandler interface
     *************************************************************/

    @Override
    public int getLocalPort() throws IOException {
        synchronized(sessionLock) {
            if (closed) {
                throw new IOException(
                    AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_WEBSOCKET_SESSION_CLOSED));
            }
            if (session == null) {
                throw new IOException(
                "WebSocket Session not open on JMS connection "+
                 conn.getConnectionID());
            }
        }
        return session.getId().hashCode();
    }

    @Override
    public boolean isDirectMode(){
        return false;
    }
	
    @Override
    public ReadWritePacket readPacket() throws IOException {
        ReadWritePacket pkt = null;
        synchronized(sessionLock) { 
            if (session == null) {
                throw new IOException(
                "WebSocket Session not open on JMS connection "+
                 conn.getConnectionID());
            }
            String id = session.getId();
            while (!closed && packetRead == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, 
                        "WebSocketConnectionHandler@"+hashCode()+
                        ": readPacket() waiting for incoming packet, ws-session="+
                         id+" on JMS connection "+conn.getConnectionID());
                }
                try {
                    sessionLock.wait();
                } catch (InterruptedException e) {}
            }
            if (closed) {
                throw new IOException(
                    AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_WEBSOCKET_SESSION_CLOSED));
            }
            pkt = packetRead;
            packetRead = null;
            sessionLock.notifyAll();
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, 
                "WebSocketConnectionHandler@"+hashCode()+": READ PACKET="+pkt+
                ", ws-session="+session.getId()+
                ", on JMS connection "+conn.getConnectionID());
        }
        return pkt;
    }
	
    @Override
    public void writePacket(ReadWritePacket pkt) throws IOException {
        synchronized(sessionLock) {
            if (session == null) {
                throw new IOException(
                "WebSocket Session not open on JMS connection "+
                 conn.getConnectionID());
            }
            if (closed) {
                throw new IOException(
                AdministeredObject.cr.getKString(
                AdministeredObject.cr.X_WEBSOCKET_SESSION_CLOSED));
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": WRITE PACKET="+pkt+", ws-session="+session.getId()+
                " on JMS connection "+conn.getConnectionID());
        }

        pkt.writePacket(new ByteBufferOutput() {
            public void writeByteBuffer(ByteBuffer data) throws IOException {
                session.getBasicRemote().sendBinary(data);
            }
            public void writeBytes(byte[] data) throws IOException {
                throw new IOException("Unexpected call", 
                    new UnsupportedOperationException("writeBytes(byte[])"));
            }
            }, true);
    }

    private URI getURI() throws Exception {
        String mqscheme = mqaddr.getSchemeName();
        String service = mqaddr.getServiceName();
        String scheme = "ws";
        if (mqscheme.equalsIgnoreCase(MQAddress.SCHEME_NAME_MQWS)) { 
            if (service == null || service.trim().equals("")) {
                service = DEFAULT_WS_SERVICE_NAME;
            }
        } else {
            scheme = "wss";
            if (service == null || service.trim().equals("")) {
                service = DEFAULT_WSS_SERVICE_NAME;
            }
        }
        return new URI(scheme, null, 
            mqaddr.getHostName(), mqaddr.getPort(), 
            "/"+service+WEBSOCKET_JMS_PATH,  null, null);
    }
	
    @Override
    public void configure(Properties configuration) throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": configure("+configuration+") on jms-connection@"+conn.hashCode());
        }
        try {
            WebSocketContainer client = ContainerProvider.getWebSocketContainer();
            client.setAsyncSendTimeout(DEFAULT_ASYNC_SEND_TIMEOUT);
            client.setDefaultMaxBinaryMessageBufferSize(DEFAULT_MAX_BINARY_BUFFER_SIZE);
            client.setDefaultMaxTextMessageBufferSize(DEFAULT_MAX_TEXT_BUFFER_SIZE);
            client.setDefaultMaxSessionIdleTimeout(DEFAULT_MAX_SESSION_IDLE_TIMEOUT);
            URI uri = getURI();

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                    "WebSocketConnectionHandler@"+hashCode()+
                    ": configure(): WebSocketContainer.connectToServer("+uri+
                    ") on jms-connection@"+conn.hashCode());
            }

            client.connectToServer(this,
                ClientEndpointConfig.Builder.create().build(), uri);
 
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                    "WebSocketConnectionHandler@"+hashCode()+
                    ": configure(): waiting ("+connectTimeout+
                    ") for websocket session open to "+uri+" on jms-connection@"+conn.hashCode());
            }

            if (connectTimeout > 0) {
                if (!onOpenLatch.await(connectTimeout, TimeUnit.MILLISECONDS)) {
                    throw new IOException(AdministeredObject.cr.getKString(
                        AdministeredObject.cr.X_WEBSOCKET_OPEN_TIMEOUT, 
                        mqaddr.getURL(), "jms-connection@"+conn.hashCode()));
                }
            } else {
                onOpenLatch.await();
            }
        } catch (Exception e) {
            String[] params = { mqaddr.getURL(), 
                                "jms-connection@"+conn.hashCode(), e.getMessage() };
            String emsg = AdministeredObject.cr.getKString(
                AdministeredObject.cr.X_WEBSOCKET_OPEN_FAILED, params);
            logger.log(Level.SEVERE, emsg, e);
            throw new IOException(emsg, e);
        } 
    }

    @Override
    public void close() throws IOException {
        String id = null;
        Session ss = null;
        synchronized(sessionLock) {
            if (closed) {
                return;
            }
            closed = true;
            ss = session;
            if (ss != null) {
                id = ss.getId();
            }
        }     
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": close(): ws-session="+id+
                " on JMS connection "+conn.getConnectionID());
        }
        if (ss != null) {
            ss.close();
        }
        synchronized(sessionLock) {
            sessionLock.notifyAll();
            onOpenLatch.countDown();
        }
    }

    @Override
    public String getBrokerAddress() {
        return mqaddr.getURL();
    }
    
    @Override
    public String getBrokerHostName() {
        return mqaddr.getHostName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException(
            "WebSocketConnectionHandler.getInputStream()");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException(
            "WebSocketConnectionHandler.getOutputStream()");
    }

    /********************************************************
     * Implement Endpoint interface
     ***********************************************************/

    @Override
    public void onOpen(Session ss, EndpointConfig config) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": onOpen(WS-Session="+ss+", EndpointConfig="+config+") on JMS connection "+conn.getConnectionID());
        } else if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": onOpen(WS-Session="+ss.getId()+") on JMS connection "+conn.getConnectionID());
        }
        boolean doclose = false;
        synchronized(sessionLock) {
            if (session != null || closed) {
                doclose = true;     
            } else {
	        session = ss;
                onOpenLatch.countDown();
            }
        }
        if (doclose) {
            try {
                try {
                    ss.close();
                } finally {
                    close();
                }
            } catch (Exception e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, 
                        AdministeredObject.cr.getKString(
                        AdministeredObject.cr.W_WEBSOCKET_CLOSE_FAILED,
                        this.toString(), e.toString()), e);
                }
            } finally { 
                if (!closed) {
                    throw new IllegalStateException(
                    "There is an existing WebSocket session "+this);
                }
            }
        } else {
            session.addMessageHandler(this);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": onClose(Session="+session+", CloseReason="+
                 closeReason+"), ws-session="+this.session+
                " on JMS connection "+ conn.getConnectionID());
        } else if (logger.isLoggable(Level.FINE)) {
            Session ss = this.session;
            logger.log(Level.FINE, 
                "WebSocketConnectionHandler@"+hashCode()+
                ": onClose(Session="+session.getId()+", CloseReason="+
                 closeReason+"), ws-session="+(ss == null ? "null":ss.getId())+
               " on JMS connection "+conn.getConnectionID());
        }
        try {
            close();
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                    AdministeredObject.cr.getKString(
                    AdministeredObject.cr.W_WEBSOCKET_CLOSE_FAILED,
                    this.toString(), e.toString()), e);
            }
        }
    }

    @Override
    public void onError(Session session, Throwable thr) {
        logger.log(Level.SEVERE, "onError("+session+", "+thr+")"+this, thr);
        logger.log(Level.INFO, AdministeredObject.cr.getKString(
            AdministeredObject.cr.X_WEBSOCKET_CLOSE_ONERROR, 
            this.toString(), thr.toString()));
        try {
            close();
        } catch (Exception e) {
            logger.log(Level.WARNING, 
                AdministeredObject.cr.getKString(
                AdministeredObject.cr.W_WEBSOCKET_CLOSE_FAILED,
                this.toString(), e.toString()), e);
        }
    }

    /*******************************************************
     * Implement MessageHandler.Whole interface
     ***********************************************************/
    
    /**
     * Called when the message has been fully received.
     *
     * @param message the message data.
     */
    @Override
    public void onMessage(ByteBuffer data) {
        if (logger.isLoggable(Level.FINEST)) {
            Session ss = session;
            logger.log(Level.FINEST, 
            Thread.currentThread()+"WebSocketConnectionHandler@"+hashCode()+
            ": onMessage(ByteBuffer@"+data.hashCode()+
            "[len="+data.remaining()+", pos="+data.position()+"]), ws-session="+
             (ss == null ? "null":ss.getId())+" on JMS connection "+conn.getConnectionID());
        }
        String id = null;
        while (data.hasRemaining()) {
            synchronized(sessionLock) {
                if (session == null) {
                    throw new IllegalStateException(
                    "WebSocket Session not open on JMS connection "+
                    conn.getConnectionID());
                }
                id = session.getId();
                while (!closed && packetRead != null) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, 
                        Thread.currentThread()+"WebSocketConnectionHandler@"+hashCode()+
                         ": onMessage() waiting for packet read to be dispatched, ws-session="+
                          id+" on JMS connection "+conn.getConnectionID());
                    }
                    try {
                        sessionLock.wait();
                    } catch (InterruptedException e) {}
                 }
                 if (closed) {
                     throw new IllegalStateException(
                         AdministeredObject.cr.getKString(
                         AdministeredObject.cr.X_WEBSOCKET_SESSION_CLOSED));
                 }
                 if (packetPending == null) {
                     packetPending = new ReadWritePacket();
                 }
            }
            try {
                if (packetPending.readPacket(data)) {
                    if (!packetPending.hasBigPacketException()) {
                        synchronized(sessionLock) {
                            packetRead = packetPending;
                            packetPending = null; 
                            sessionLock.notifyAll();
                        }
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST, 
                                Thread.currentThread()+"WebSocketConnectionHandler@"+hashCode()+
                                ": onMessage(): RECEIVED PACKET="+packetPending+
                                ", remaining="+data.remaining()+",ws-session="+id+
                                " on JMS connection "+conn.getConnectionID());
                        }
                    } else {
                        IOException ioe = packetPending.getBigPacketException(); 
                        packetPending = null;
                        throw new IOException("BigPacketException", ioe);
                    }
                }
            } catch (BigPacketException e) {
                String[] params = { (packetPending == null ? "(r):"+packetRead:"(p):"+packetPending), 
                                    this.toString(), e.getMessage() };
                String emsg =  AdministeredObject.cr.getKString(
                     AdministeredObject.cr.X_WEBSOCKET_PROCESS_PKT, params);
                logger.log(Level.SEVERE, emsg);
            } catch (IOException e) {
                onError(session, e);
                String[] params = {  (packetPending == null ? "(r):"+packetRead:"(p):"+packetPending), 
                                     this.toString(), e.getMessage() };
                throw new RuntimeException(
                    AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_WEBSOCKET_PROCESS_PKT, params), e);
            }
        }
    }

    @Override
    public String toString() {
        Session ss = session;
        return "[WebSocketConnectionHandler@"+hashCode()+", ws-session="+
            (ss == null ? "null":ss.getId())+", jms-connection@"+conn.hashCode()+
            "["+conn.getConnectionID()+"]]";
    }
}

