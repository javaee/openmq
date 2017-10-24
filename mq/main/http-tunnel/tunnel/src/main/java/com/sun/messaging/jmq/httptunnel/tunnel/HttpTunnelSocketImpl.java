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

package com.sun.messaging.jmq.httptunnel.tunnel;

import java.io.*;
import java.util.Hashtable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;
import com.sun.messaging.jmq.httptunnel.tunnel.client.HttpTunnelClientDriver;

/**
 * This class implements socket-like interface for the HTTP tunnel
 * connections.
 */
public class HttpTunnelSocketImpl implements HttpTunnelSocket {
    private HttpTunnelConnection conn = null;

    private InputStream is = null;
    private OutputStream os = null;
    private boolean sockClosed = false;

    public HttpTunnelSocketImpl() { }

    /**
     * Creates a socket and establishes a connection with the specified
     * server address.
     */
    public void init(String serverAddr) throws IOException {
        HttpTunnelClientDriver wire = new HttpTunnelClientDriver(serverAddr);
        conn = wire.doConnect();
        initSocket();
    }

    /**
     * Creates a socket with a given HTTP tunnel connection. Used
     * internally by the server socket (accept) implementation.
     */
    public HttpTunnelSocketImpl(HttpTunnelConnection conn) {
        this.conn = conn;
        initSocket();
    }

    protected void initSocket() {
        is = null; // Will be created on demand
        os = null; // Will be created on demand
        sockClosed = false;
    }

    /**
     * Returns an input stream for this socket.
     */
    public synchronized InputStream getInputStream() throws IOException {
        if (sockClosed) {
            throw new IOException("Socket closed");
        }
        if (is == null)
            is = new HttpTunnelInputStream(conn);
        return is;
    }

    /**
     * Returns an output stream for this socket.
     */
    public synchronized OutputStream getOutputStream() throws IOException {
        if (sockClosed) {
            throw new IOException("Socket closed");
        }
        if (os == null)
            os = new HttpTunnelOutputStream(conn);
        return os;
    }

    /**
     * Close this socket.
     */
    public synchronized void close() throws IOException {
        if (is != null)
            is.close();
        if (os != null)
            os.close();
        sockClosed = true;
        conn.closeConn();
    }

    /**
     * Get the unique connection ID.
     */
    public int getConnId() {
        return conn.getConnId();
    }

    public InetAddress getRemoteAddress() 
        throws UnknownHostException, SecurityException { 

        HttpTunnelConnection c = conn;
        if (c == null || c.getRemoteAddr() == null) return null;
        return InetAddress.getByName(c.getRemoteAddr());
    }

    public int getPullPeriod() {
        return conn.getPullPeriod();
    }

    public void setPullPeriod(int pullPeriod) throws IOException {
        conn.setPullPeriod(pullPeriod);
    }

    public int getConnectionTimeout() {
        return conn.getConnectionTimeout();
    }

    public void setConnectionTimeout(int connectionTimeout)
        throws IOException {
        conn.setConnectionTimeout(connectionTimeout);
    }

    public Hashtable getDebugState() {
        return conn.getDebugState();
    }
}

/*
 * EOF
 */
