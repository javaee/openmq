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
 * @(#)Link.java	1.11 09/11/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides a common mechanism for establishing / maintaining
 * and consuming packets from a TCP connection.
 */
public abstract class Link extends Thread {
    private static boolean DEBUG = Boolean.getBoolean("httptunnel.debug");
    private boolean connected = false;
    protected boolean done = false;
    protected InputStream is = null;
    protected OutputStream os = null;
    private Logger logger = Logger.getLogger("Http Tunneling");

    /**
     * Establish the connection. This method blocks and keeps trying
     * until a connection is established.
     */
    protected abstract void createLink();

    /**
     * Consume a packet received over this connection.
     */
    protected abstract void receivePacket(HttpTunnelPacket p);

    /**
     * Handle connection error.
     */
    protected abstract void handleLinkDown();

    /**
     * Send a packet over this connection.
     */
    public synchronized void sendPacket(HttpTunnelPacket p) {
        if (DEBUG) {
            log("Sending packet : " + p);
        }

        try {
            p.writePacket(os);
        } catch (Exception e) {
            if (DEBUG) {
                log(e);
            }

            linkDown();
        }
    }

    protected void linkDown() {
        try {
            is.close();
            os.close();
            handleLinkDown();
        } catch (Exception e) {
        }

        connected = false;
    }

    public void shutdown() {
        done = true;
    }

    public void run() {
        while (!done) {
            try {
                if (connected == false) {
                    createLink();
                    connected = true;
                }

                HttpTunnelPacket p = new HttpTunnelPacket();
                p.readPacket(is);
                receivePacket(p);
            } catch (IllegalStateException e) {
                if (DEBUG) {
                    log(e);
                }

                done = true;
            } catch (Exception e) {
                if (DEBUG) {
                    log(e);
                }

                linkDown();
            }
        }
    }

    protected void log(String msg) {
        logger.log(Level.INFO, msg);
    }

    protected void log(Exception ex) {
    	logger.log(Level.INFO, "Http Tunneling", ex);
    }

    protected void log(Level v, String msg, Exception ex) {
    	logger.log(v, msg, ex);
    }
}

/*
 * EOF
 */
