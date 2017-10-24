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
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.server;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.httptunnel.tunnel.*;
import com.sun.messaging.jmq.httptunnel.api.server.*;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;

/**
 * This class implements server sockets for HTTP tunnel protocol.
 * A server socket waits for connection requests from the clients.
 */
public class HttpTunnelServerSocketImpl implements HttpTunnelServerSocket {
    private Vector listenQ = null;
    private boolean closed;
    private HttpTunnelServerDriver wire = null;

    public HttpTunnelServerSocketImpl() {
    }

    /**
     * Creates a server socket.
     */
    public void init(HttpTunnelServerDriver wire) throws IOException {
        listenQ = wire.getListenQ();
        closed = false;
        this.wire = wire;

        wire.listen(true);
    }

    /**
     * Listens for a connection to be made to this socket and accepts
     * it. The method blocks until a connection is made. 
     */
    public HttpTunnelSocket accept() throws IOException {
        synchronized (listenQ) {
            while (listenQ.isEmpty()) {

                if (closed)
                    break;

                try {
                    listenQ.wait(5000);
                }
                catch (Exception e) {}
            }

            if (closed) {
                if (! listenQ.isEmpty())
                    listenQ.notifyAll(); // Wakeup the next thread
                throw new IOException("Socket closed");
            }

            HttpTunnelConnection conn =
                (HttpTunnelConnection) listenQ.elementAt(0);
            listenQ.removeElementAt(0);
            return new HttpTunnelSocketImpl(conn);
        }
    }

    /**
     * Closes this server socket.
     */
    public void close() throws IOException {
        wire.listen(false);

        synchronized (listenQ) {
            closed = true;
            listenQ.notifyAll();
        }
    }
}

/*
 * EOF
 */
