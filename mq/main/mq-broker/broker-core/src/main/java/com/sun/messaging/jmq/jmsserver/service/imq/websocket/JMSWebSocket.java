/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver.service.imq.websocket;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketException;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.memory.MemoryManager;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.ByteBufferOutput;
import com.sun.messaging.jmq.io.BigPacketException;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.imq.grizzly.GrizzlyMQPacketList;


/**
 * @author amyk
 */
public class JMSWebSocket extends MQWebSocket {

    private Object packetLock = new Object();
    private Packet packetPending = null;

    public JMSWebSocket(MQWebSocketServiceApp app, 
                       ProtocolHandler protocolHandler,
                       HttpRequestPacket request,
                       WebSocketListener... listeners) {
        super(app, protocolHandler, request, listeners);
    }

    @Override
    protected void writePacket(Packet pkt) throws IOException {
        if (!isConnected()) {
            throw new IOException("JMSWebSocket@"+hashCode()+" is not connected"); 
        }
        if (DEBUG) {
            logger.log(logger.INFO, Thread.currentThread()+"JMSWebSocket@"+hashCode()+": WRITE PACKET="+pkt);
        }

        pkt.writePacket(new ByteBufferOutput() {
            public void writeByteBuffer(ByteBuffer data) throws IOException {
                throw new IOException("Unexpected call", 
                    new UnsupportedOperationException("writeByteBuffer(ByteBuffer)"));
            }
            public void writeBytes(byte[] data) throws IOException {
                if (DEBUG) {
                    logger.log(logger.INFO, Thread.currentThread()+"JMSWebSocket@"+hashCode()+
                    ": writeBytes(data.len="+data.length+")");
                }
                send(data);
            }
            }, false);
        if (DEBUG) {
            logger.log(logger.INFO, Thread.currentThread()+"JMSWebSocket@"+hashCode()+": SENT PACKET="+pkt);
        }
    }

    @Override
    protected void processData(String text) throws Exception {
        throw new IOException("JMSWebSocket.processData(String): unexpected call");
    }

    @Override
    protected void processData(byte[] data) throws Exception {
        ByteBuffer buf = ByteBuffer.wrap(data);
        processData(buf);
    }

    private void processData(ByteBuffer buf) throws IOException {

        if (DEBUG) {
            logger.log(Logger.INFO, 
                Thread.currentThread()+" processData:buf.remaining="+buf.remaining());
        }

        List<Packet> packetList = null;

	while (buf.hasRemaining()) {
            synchronized(packetLock) {
                if (packetPending != null) {
                    try {
                        if (packetPending.readPacket(buf)) {
                            if (!packetPending.hasBigPacketException()) {
                                if (packetList == null) {
                                    packetList = new ArrayList<Packet>();
                                }
                                packetList.add(packetPending); 
                                if (DEBUG) {
                                logger.log(logger.INFO, 
                                    Thread.currentThread()+"JMSWebSocket@"+hashCode()+" processData(): READ pending PACKET="+
                                     packetPending+", buf.remaining="+buf.remaining());
                                }
                            }
                            packetPending = null;
                        }  
                    } catch (BigPacketException e) {
                        logger.log(logger.ERROR, Thread.currentThread()+"readPacket: "+e.getMessage(), e);
                        WebSocketMQIPConnection conn = websocketApp.getMQIPConnection(this);
                        conn.handleBigPacketException(packetPending, e); //XXopt close conn if too big

                    } catch (IllegalArgumentException e) {
                        WebSocketMQIPConnection conn = websocketApp.getMQIPConnection(this);
                        conn.handleIllegalArgumentExceptionPacket(packetPending, e);

                    } catch (OutOfMemoryError err) { //XXopt close conn
		        Globals.handleGlobalError(err,
                            Globals.getBrokerResources().getKString(
                            BrokerResources.M_LOW_MEMORY_READALLOC) + ": "
                            +packetPending.headerToString());
                    }
                    continue;
                }
            }

            if (packetList == null) {
                packetList = new ArrayList<Packet>();
            }

            Packet packet = new Packet(false);
            packet.generateSequenceNumber(false);
            packet.generateTimestamp(false);

            try {
                if (packet.readPacket(buf)) {
                    if (!packet.hasBigPacketException()) {
                        packetList.add(packet); 
                        if (DEBUG) { 
                            logger.log(logger.INFO, Thread.currentThread()+
                            "JMSWebSocket@"+hashCode()+" processData(): READ a PACKET="+packet);
                        }
                    }
                } else {
                    synchronized(packetLock) {
                        packetPending = packet;
                    }
                }
            } catch (BigPacketException e) {
                logger.log(logger.ERROR, "readPacket: "+e.getMessage(), e);
                WebSocketMQIPConnection conn = websocketApp.getMQIPConnection(this);
                conn.handleBigPacketException(packet, e); //XXopt close conn if too big

            } catch (IllegalArgumentException e) {
                logger.log(logger.ERROR, "readPacket: "+e.getMessage(), e);
                 WebSocketMQIPConnection conn = websocketApp.getMQIPConnection(this);
                 conn.handleIllegalArgumentExceptionPacket(packet, e);

            } catch (OutOfMemoryError err) { //XXopt close conn
		Globals.handleGlobalError(err,
                        Globals.getBrokerResources().getKString(
                        BrokerResources.M_LOW_MEMORY_READALLOC) + ": "
                        + packet.headerToString());
            }
            continue;
        }

        if (packetList == null || packetList.isEmpty()) {
            packetList = null;
            return;
        }

	if (DEBUG) {
            logger.log(logger.INFO,
            "[JMSWebSocket@"+this.hashCode()+"]processData() after processed buf: remaining="+buf.remaining());
	}

	WebSocketMQIPConnection conn = websocketApp.getMQIPConnection(this);

        for (int i = 0; i < packetList.size(); i++) {
	    try {
        	final Packet packet = packetList.get(i);
        	conn.receivedPacket(packet);
        	conn.readData();
            } catch (BrokerException e) { //XXclean
                Globals.getLogger().logStack(Logger.ERROR,
        	"Failed to process packet from connection "+this, e);
            } 
        }
        packetList.clear();
        packetList = null;
    }
}
