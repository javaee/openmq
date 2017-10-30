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
 * @(#)HttpTunnelPush.java	1.10 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.client;

import com.sun.messaging.jmq.httptunnel.tunnel.HttpTunnelPacket;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelDefaults;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Vector;


/**
 * This class provides a continuous push thread for sending packets
 * from client to server. Pushing a packet can be a time consuming
 * task. A dedicated push thread ensures that the threads generating
 * outbound packets don't waste their cycles in HTTP I/O.
 */
public class HttpTunnelPush extends Thread implements HttpTunnelDefaults {
    private URL pushUrl = null;
    private Vector q = null;
    private boolean stopThread = false;
    private boolean shutdownComplete = false;

    public HttpTunnelPush() {
    }

    /**
     * Set the default push URL and start the push thread.
     */
    public void startPushThread(URL pushUrl) {
        this.pushUrl = pushUrl;
        q = new Vector();

        setName("HttpTunnelPush");
        setDaemon(true);
        start();
    }

    /**
     * Terminate the push thread.
     */
    public void shutdown() {
        synchronized (q) {
            stopThread = true;
            q.notifyAll();
        }

        while (!shutdownComplete) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Adds a packet to the push queue and wakes up the push thread.
     * Note that the flow control mechanism automatically limits the
     * maximum queue size.
     */
    public void sendPacket(HttpTunnelPacket p) {
        synchronized (q) {
            if (stopThread) { // Don't accept any more packets.

                return;
            }

            q.addElement(p);
            q.notifyAll();
        }
    }

    /**
     * Sends a packet to a given URL and optionally reads a single
     * HttpTunnelPacket from the HTTP response stream.
     */
    public HttpTunnelPacket sendPacketDirect(URL u, HttpTunnelPacket p,
        boolean getResponse) throws Exception {
        URLConnection uc = u.openConnection();
        uc.setDoInput(true);
        uc.setDoOutput(true);
        uc.setUseCaches(false);

        uc.setRequestProperty("content-type", "application/octet-stream");

        OutputStream os = uc.getOutputStream();
        p.writePacket(os);
        os.close();

        uc.connect();

        int response = HttpURLConnection.HTTP_OK;

        if (uc instanceof HttpURLConnection) {
            response = ((HttpURLConnection) uc).getResponseCode();
        } else {
            uc.getContentType();

            // We don't really need the content type. This just forces
            // the uc to do its job.
        }

        InputStream is = uc.getInputStream();

        if (getResponse == false) {
            is.close();

            return null;
        }

        if (response != HttpURLConnection.HTTP_OK) {
            is.close();
            throw new IOException("Failed to receive response");
        }

        HttpTunnelPacket ret = new HttpTunnelPacket();
        ret.readPacket(is);
        is.close();

        return ret;
    }

    public void run() {
        while (true) {
            HttpTunnelPacket p = null;

            synchronized (q) {
                while (q.isEmpty() && (stopThread == false)) {
                    try {
                        q.wait();
                    } catch (Exception e) {
                    }
                }

                if (stopThread && q.isEmpty()) {
                    break;
                }

                p = (HttpTunnelPacket) q.elementAt(0);
                q.removeElementAt(0);
            }

            try {
                sendPacketDirect(pushUrl, p, false);
            } catch (Exception e) {
            }
        }

        shutdownComplete = true;
    }
}

/*
 * EOF
 */
