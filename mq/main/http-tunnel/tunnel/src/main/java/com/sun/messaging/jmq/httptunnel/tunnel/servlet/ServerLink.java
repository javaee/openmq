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

/*
 * @(#)ServerLink.java	1.10 09/11/07
 */ 
 
package com.sun.messaging.jmq.httptunnel.tunnel.servlet;

import com.sun.messaging.jmq.httptunnel.tunnel.*;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelDefaults;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This class implements the servlet end of the link between the
 * servlet and the server application (JMQ broker).
 */
public class ServerLink extends Link implements HttpTunnelDefaults {
    private static boolean DEBUG = Boolean.getBoolean("httptunnel.debug");
    private Socket serverConn = null;
    private ServerLinkTable parent = null;
    private String serverName = null;
    private boolean listenState = false;
    private boolean serverReady = false;

    public ServerLink(Socket serverConn, ServerLinkTable parent)
        throws IOException {
        try {
        serverConn.setTcpNoDelay(true);
        } catch (SocketException e) {
        parent.servletContext.log("WARNING: HttpTunnelTcpLink()["+serverConn.toString()+
                                  "]setTcpNoDelay: " + e.toString(), e);
        }

        this.serverConn = serverConn;
        this.parent = parent;

        is = serverConn.getInputStream();
        os = serverConn.getOutputStream();

        setName("HttpTunnelTcpLink[" + serverConn + "]");

        start();
    }

    protected void createLink() {
    }

    protected void handleLinkDown() {
        try {
            serverConn.close();
        } catch (Exception e) {
        }

        parent.serverDown(this);
    }

    protected void linkDown() {
        super.linkDown();
    }

    protected boolean isDone() {
        return done;
    }

    protected String getServerName() {
        return serverName;
    }

    protected boolean getListenState() {
        return listenState;
    }

    /**
     * Enqueue the packet to the appropriate connection queue.
     * This should wakeup the appropriate pull thread.
     */
    protected void receivePacket(HttpTunnelPacket p) {
        if (serverReady) {
            if (p.getPacketType() == LISTEN_STATE_PACKET) {
                receiveListenStatePacket(p);

                return;
            }

            if (DEBUG) {
                log("Received Packet : " + p);
            }

            parent.receivePacket(p, this);

            return;
        }

        if (p.getPacketType() != LINK_INIT_PACKET) {
            parent.servletContext.log("HttpTunnelServlet: ServerLink[" +
                serverName + "] received " + "unexpected packet type " +
                p.getPacketType());
            shutdown();
            linkDown();

            return;
        }

        // Recreate the connection table...
        byte[] buf = p.getPacketBody();
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        DataInputStream dis = new DataInputStream(bis);

        try {
            serverName = dis.readUTF();

            int n = dis.readInt();

            for (int i = 0; i < n; i++) {
                int connId = dis.readInt();
                int pullPeriod = dis.readInt();
                parent.updateConnection(connId, pullPeriod, this);
            }

            parent.servletContext.log("HttpTunnelServlet: ServerLink[" +
                serverName + "]" + " link initialized");

            parent.updateServerName(this);

            serverReady = true;
        } catch (Exception e) {
            parent.servletContext.log("HttpTunnelServlet: ServerLink[" +
                serverName + "]" + " init link failed: " + e.getMessage(), e);
            shutdown();
            linkDown();
        }
    }

    private void receiveListenStatePacket(HttpTunnelPacket p) {
        byte[] buf = p.getPacketBody();

        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        DataInputStream dis = new DataInputStream(bis);

        String sname = null;

        try {
            sname = dis.readUTF();
            listenState = dis.readBoolean();
        } catch (Exception e) {
            parent.servletContext.log("HttpTunnelServlet: ServerLink[" + sname +
                "]" + " receiveListenStatePacket failed: " + e.getMessage(), e);
            shutdown();
            linkDown();
        }
    }
}

/*
 * EOF
 */
