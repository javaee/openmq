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
 * @(#)HttpsTunnelServlet.java	1.8 06/28/07
 */ 
 
package com.sun.messaging.jmq.httptunnel.tunnel.servlet;

import java.io.PrintWriter;

import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HttpsTunnelServlet extends HttpTunnelServlet {
    public void init() throws ServletException {
        servletContext = this.getServletContext();
        startTime = new java.util.Date();
        servletName = "HttpsTunnelServlet";

        try {
            linkTable = new ServerLinkTable(this.getServletConfig(), true);
            inService = true;
        } catch (Exception e) {
            // save the exception 
            initException = e;
            servletContext.log(servletName + ": initialization failed, " + e);
        }
    }

    public void handleTest(HttpServletRequest request,
        HttpServletResponse response) {
        try {
            response.setContentType("text/html; charset=UTF-8 ");

            PrintWriter pw = response.getWriter();

            pw.println("<HTML>");

            pw.println("<HEAD>");
            pw.println("<TITLE> JMQ HTTPS Tunneling Servlet </TITLE>");
            pw.println("</HEAD>");

            pw.println("<BODY>");

            if (inService) {
                pw.println("HTTPS tunneling servlet ready.<BR>");
                pw.println("Servlet Start Time : " + startTime + " <BR>");
                pw.println("Accepting secured connections from brokers on " +
                    "port : " + linkTable.getServletPort() + " <P>");

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
                pw.println("HTTPS Tunneling servlet cannot be started.<br>");

                if (initException != null) {
                    pw.println("    " + initException);
                }
            }

            pw.println("</BODY>");
            pw.println("</HTML>");
        } catch (Exception e) {
        }
    }
}
