/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)HttpTunnelServlet.java	1.18 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.servlet;

import com.sun.messaging.jmq.httptunnel.tunnel.HttpTunnelPacket;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelDefaults;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;


public class HttpTunnelServlet extends HttpServlet implements HttpTunnelDefaults {
    private int serviceCounter = 0;
    private Object serviceLock = new Object();
    private boolean servletShuttingDown = false;
    protected String servletName = null;
    protected boolean inService = false;
    protected ServletContext servletContext;
    protected java.util.Date startTime = null;
    protected ServerLinkTable linkTable = null;
    protected Throwable initException = null;

    public void init() throws ServletException {
        serviceCounter = 0;
        servletShuttingDown = false;
        servletContext = this.getServletContext();
        startTime = new java.util.Date();
        servletName = "HttpTunnelServlet";

        try {
            linkTable = new ServerLinkTable(this.getServletConfig());
            inService = true;
        } catch (Exception e) {
            // save the exception
            initException = e;
            servletContext.log(servletName + ": initialization failed, " + e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        response.setContentType("application/octet-stream");

        if (servletShuttingDown) {
            return;
        }

        String qs = request.getQueryString();

        if (qs == null) {
            handleTest(request, response);

            return;
        }

        // servlet not started successfully, return
        if (!inService) {
            return;
        }

        Hashtable ht = HttpUtils.parseQueryString(qs);

        String[] tmp1 = (String[]) ht.get("Type");
        String requestType = tmp1[0];

        if (requestType == null) {
            handleTest(request, response);

            return;
        }

        String serverName = null;
        String[] tmp2 = (String[]) ht.get("ServerName");

        if (tmp2 != null) {
            serverName = tmp2[0];
        }

        if (requestType.equals("push")) {
            handlePush(request, response, serverName);
        } else if (requestType.equals("pull")) {
            tmp2 = (String[]) ht.get("ConnId");

            String connIdStr = tmp2[0];
            handlePull(request, response, connIdStr, serverName);
        } else if (requestType.equals("connect")) {
            handleConnect(request, response, serverName);
        } else {
            handleTest(request, response);
        }
    }

    public void handleTest(HttpServletRequest request,
        HttpServletResponse response) {
        try {
            response.setContentType("text/html; charset=UTF-8 ");

            PrintWriter pw = response.getWriter();

            pw.println("<HTML>");

            pw.println("<HEAD>");
            pw.println("<TITLE> JMQ HTTP Tunneling Servlet </TITLE>");
            pw.println("</HEAD>");

            pw.println("<BODY>");

            if (inService) {
                pw.println("HTTP tunneling servlet ready.<BR>");
                pw.println("Servlet Start Time : " + startTime + " <BR>");
                pw.println("Accepting TCP connections from brokers on port : " +
                    linkTable.getServletPort() + " <P>");

                Vector slist = linkTable.getServerList();
                pw.println("Total available brokers = " + slist.size() +
                    "<BR>");
                pw.println("Broker List : <BR>");

                pw.println("<BLOCKQUOTE><PRE>");

                for (int i = 0; i < slist.size(); i++) {
                    pw.println((String) slist.elementAt(i));
                }

                pw.println("</PRE></BLOCKQUOTE>");
            } else {
                pw.println(new java.util.Date() + "<br>");
                pw.println("HTTP Tunneling servlet cannot be started.<br>");

                if (initException != null) {
                    pw.println("    " + initException);
                }
            }

            pw.println("</BODY>");
            pw.println("</HTML>");
        } catch (Exception e) {
        }
    }

    private void sendNoOp(HttpServletResponse response) {
        try {
            ServletOutputStream sos = response.getOutputStream();

            HttpTunnelPacket p = new HttpTunnelPacket();
            p.setPacketType(NO_OP_PACKET);
            p.setConnId(0);
            p.setSequence(0);
            p.setWinsize(0);
            p.setChecksum(0);
            p.setPacketBody(null);

            p.writePacket(sos);
        } catch (Exception e) { /* Ignore */
        }
    }

    /**
     * Send data from server to client. This method does its best to
     * deliver the packet. There may be many reasons why the packet may
     * not get delivered - e.g. web server/proxy timeouts, and there
     * is not much this method can do about it, but that's exactly why
     * we have packet acknowledgements and retransmissions...
     */
    public void handlePull(HttpServletRequest request,
        HttpServletResponse response, String connIdStr, String serverName) {
        if (ONE_PACKET_PER_REQUEST) {
            HttpTunnelPacket p = linkTable.waitForPacket(connIdStr, serverName);

            if (p == null) {
                return;
            }

            try {
                ServletOutputStream sos = response.getOutputStream();
                p.writePacket(sos);
            } catch (Exception e) {
                // Obvious failure - resend the packet.
                linkTable.retrySendPacket(p, connIdStr, serverName);
            }
        } else {
            Vector v = linkTable.waitForPackets(connIdStr, serverName);

            if ((v == null) || (v.size() == 0)) {
                sendNoOp(response);

                return;
            }

            try {
                ServletOutputStream sos = response.getOutputStream();

                for (int i = 0; i < v.size(); i++) {
                    HttpTunnelPacket p = (HttpTunnelPacket) v.elementAt(i);
                    p.writePacket(sos);
                }
            } catch (Exception e) {
                // Obvious failure - resend the packet.
                linkTable.retrySendPackets(v, connIdStr, serverName);
            }
        }
    }

    /**
     * Send data from client to server.
     */
    public void handlePush(HttpServletRequest request,
        HttpServletResponse response, String serverName) {
        int length = request.getContentLength();

        if (length > 0) {
            try {
                ServletInputStream sis = request.getInputStream();
                HttpTunnelPacket p = new HttpTunnelPacket();
                p.readPacket(sis);

                linkTable.sendPacket(p, serverName);
            } catch (Exception e) {
            }
        }

        sendNoOp(response);
    }

    /**
     * Handle a connection establishment request from the client.
     */
    public void handleConnect(HttpServletRequest request,
        HttpServletResponse response, String serverName) {
        int length = request.getContentLength();

        if (length > 0) {
            HttpTunnelPacket p = null;

            try {
                ServletInputStream sis = request.getInputStream();
                p = new HttpTunnelPacket();
                p.readPacket(sis);
            } catch (Exception e) {
                return;
            }

            if (p == null) {
                return;
            }

            if (serverName == null) {
                serverName = linkTable.getDefaultServer();
            }

            if (serverName == null) {
                return;
            }

            if (linkTable.getListenState(serverName) == false) {
                return;
            }

            // Allocate a new connection ID and setup pullQ...
            int connId = linkTable.createNewConn(serverName);

            if (connId == -1) {
                return;
            }

            p.setConnId(connId);

            try {
                p.setPacketBody(("ServerName=" + serverName).getBytes("UTF8"));

                // Echo the connection request back to the client side
                // driver with the correct connId, so that it can
                // start sending the pull requests...
                ServletOutputStream sos = response.getOutputStream();
                p.writePacket(sos);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeUTF("ServerName=" + serverName);
                dos.writeUTF(request.getRemoteAddr());
                dos.flush();
                bos.flush();

                p.setPacketBody(bos.toByteArray());
                // Forward the connection request to server side driver
                // with the correct connId.
                linkTable.sendPacket(p, serverName);
            } catch (Exception e) {
                servletContext.log(servletName + ": client connect: " +
                    e.getMessage(), e);
                linkTable.destroyConn(connId, serverName);
            }
        }
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        enteringServiceMethod();

        try {
            super.service(req, resp);
        } finally {
            leavingServiceMethod();
        }
    }

    protected void enteringServiceMethod() {
        synchronized (serviceLock) {
            serviceCounter++;
        }
    }

    protected void leavingServiceMethod() {
        synchronized (serviceLock) {
            serviceCounter--;

            if ((serviceCounter == 0) && servletShuttingDown) {
                serviceLock.notifyAll();
            }
        }
    }

    protected int numServices() {
        synchronized (serviceLock) {
            return serviceCounter;
        }
    }

    public void destroy() {
        try {
            Thread.sleep(1); //nextConnId benefit
        } catch (Exception e) {
        }

        synchronized (serviceLock) {
            servletShuttingDown = true;
            if (linkTable != null) {
            	linkTable.close();
            }
        }

        servletContext.log(servletName + ": destroy() ...");

        try {
            // servlet not started successfully, no clean up to do; return
            if (!inService) {
                return;
            }

            // cleanup here
            linkTable.shuttingDown();

            synchronized (serviceLock) {
                while (numServices() > 0) {
                    try {
                        serviceLock.wait();
                    } catch (InterruptedException e) {
                    }
                }

                linkTable.destroy();
            }

            servletContext.log(servletName + ": destroy() done");
        } finally {
            super.destroy();
        }
    }
}

/*
 * EOF
 */
