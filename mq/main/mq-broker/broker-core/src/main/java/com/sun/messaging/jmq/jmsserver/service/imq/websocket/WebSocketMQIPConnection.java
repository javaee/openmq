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

/*
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.websocket;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.spi.AbstractSelectableChannel;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.BigPacketException;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.MQThread;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQIPConnection;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.service.imq.OperationRunnable;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;


/**
 * @author amyk
 */
public final class WebSocketMQIPConnection extends IMQIPConnection implements Runnable
{

    private static boolean DEBUG = (false || Globals.getLogger().getLevel() <= Logger.DEBUG);

    private MQWebSocket websocket = null;
    private Thread writerThread = null;
    private Object assignWriteLock = new Object();
    private boolean writeThreadAssigned = false;

    public WebSocketMQIPConnection(WebSocketIPService svc, PacketRouter router, MQWebSocket ws)
    throws IOException, BrokerException {

        super(svc, null, router);
        this.websocket = ws;
        setRemoteIP(getRemoteAddress().getAddress());
        if (svc.useDedicatedWriter()) {
            writerThread = new MQThread(this, "GrizzlyMQIPConnection");
            writerThread.start(); 
        }
    }
    
    @Override
    protected InetAddress getRemoteAddress() { 
        if (websocket == null) {
            return null;
        }
        return websocket.getRemoteAddress();
    }

    @Override
    protected int getRemotePort() { 
        return websocket.getRemotePort();
    }

    @Override
    public int getLocalPort() {
        return ((MQWebSocket)websocket).getLocalPort();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public synchronized AbstractSelectableChannel getChannel() {
    throw new RuntimeException("Unexpected call: "+getClass().getName()+".getChannel()");
    }

    @Override
    protected void closeProtocolStream() throws IOException {
        websocket.close();
    }

    @Override
    public void sendControlMessage(Packet msg) {
        if (DEBUG) {
            logger.log(Logger.INFO, 
            "GrizzlyMQIPConnection:sendControlMessage: "+msg+", "+isValid());
        }
        if (!isValid() && msg.getPacketType() != PacketType.GOODBYE ) {
            logger.log(Logger.INFO,"Internal Warning: message " + msg
                  + "queued on destroyed connection " + this);
        }
        if (!websocket.isConnected() && msg.getPacketType() == PacketType.GOODBYE) {
            return;
        }
        try {
            if (getDEBUG() || getDumpPacket() || getDumpOutPacket()) {
                dumpControlPacket(msg);
            }
            websocket.writePacket(msg);
        } catch (Exception e) {
            logger.logStack(logger.WARNING, 
            "Failed to send control packet "+msg+" to "+websocket, e);
        }
    }

    public void receivedPacket(Packet pkt) {
        readpkt = pkt;
    }

    @Override
    protected boolean readInPacket(Packet p)
    throws IllegalArgumentException, StreamCorruptedException,
           BigPacketException, IOException {

        if (DEBUG) {
            logger.log(Logger.INFO, "GrizzlyMQIPConnection:readInPacket: "+readpkt);
        }

        if (readpkt == null) {
            throw new IOException("No packet to read");
        }
        return true;
    }

    @Override
    protected boolean writeOutPacket(Packet p) throws IOException {
        if (DEBUG) {
            logger.log(Logger.INFO, "GrizzlyMQIPConnection:writeOutPacket("+p+") to "+websocket);
        }
        websocket.writePacket(p);
        return true; //XXX
    }

  
    @Override
    protected void handleWriteException(Throwable e)
    throws IOException, OutOfMemoryError {
       super.handleWriteException(e);
    }

    @Override
    protected void handleBigPacketException(Packet pkt, BigPacketException e) {
        super.handleBigPacketException(pkt, e);
    }

    @Override
    protected void handleIllegalArgumentExceptionPacket(
        Packet pkt, IllegalArgumentException e) {
        super.handleIllegalArgumentExceptionPacket(pkt, e);
    }

    @Override
    public synchronized void threadAssigned(
        OperationRunnable runner, int events)
        throws IllegalAccessException {
        throw new UnsupportedOperationException(
        "Unexpected call: GrizzlyMQIPConnection.threadAssigned()");
    }

    @Override
    protected void localFlushCtrl() {
        throw new UnsupportedOperationException(
        "Unexpected call: GrizzlyMQIPConnection.localFlushCtrl()");
    }

    @Override
    protected void localFlush() {
        throw new UnsupportedOperationException(
        "Unexpected call: GrizzlyMQIPConnection.localFlush()");
    }

    protected boolean assignWriteThread(boolean b) {
        synchronized(assignWriteLock) {
            if (b && writeThreadAssigned) {
                return false;
            }
            writeThreadAssigned = b;
            return true;
        }
    }

    public void run() {
        while (isValid()) {
            try {
                writeData(true);
            } catch (IOException e) {
                if (isValid()) {
                    logger.logStack(logger.ERROR,
                    "Exception in writing data on conection "+this, e);
                }
            }
        }
    }
}



