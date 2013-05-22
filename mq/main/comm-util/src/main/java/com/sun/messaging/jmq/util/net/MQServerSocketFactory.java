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
 * @(#)MQServerSocketFactory.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.util.net;

import javax.net.ServerSocketFactory;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * Our versino of a ServerSocketFactory. We do this to centralize
 * creation of server sockets.
 */
public class MQServerSocketFactory extends javax.net.ServerSocketFactory {

    boolean reuseAddr = true;

    ServerSocketFactory  ssf = null;

    protected MQServerSocketFactory() {
        this.ssf = null;
    }

    protected MQServerSocketFactory(ServerSocketFactory ssf) {
        this.ssf = ssf;
    }

    public void setReuseAddress(boolean on) {
        this.reuseAddr = on;
    }

    /**
     * Create an unbound ServerSocket.
     */
    public ServerSocket createServerSocket() throws IOException {
        ServerSocket ss;
        if (this.ssf != null) {
            /* Use wrapped ServerSocketFactory to create ServerSocket */
            ss = ssf.createServerSocket();
        } else {
            /* No wrapped factory, use ServerSocket constructor */
            ss = new ServerSocket();
        }

        // Bug 6294767: Force SO_REUSEADDRR to true
        ss.setReuseAddress(reuseAddr);
        return ss;
    }

    /**
     * Create a ServerSocket, bound to the specified port (on all interfaces)
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket ss = createServerSocket();
        ss.bind(new InetSocketAddress(port));
        return ss;
    }

    /**
     * Create a ServerSocket, bound to the specified port (on all interfaces)
     * with the specified backlog
     */
    public ServerSocket createServerSocket(int port,
                                           int backlog)
                                           throws IOException {
        ServerSocket ss = createServerSocket();
        ss.bind(new InetSocketAddress(port), backlog);
        return ss;
    }

    /**
     * Create a ServerSocket, bound to the specified port with 
     * the specified backlog, on the specified interface.
     */
    public ServerSocket createServerSocket(int port,
                                           int backlog,
                                           InetAddress ifAddress)
                                           throws IOException {

        ServerSocket ss = createServerSocket();
        ss.bind(new InetSocketAddress(ifAddress, port), backlog);
        return ss;
    }

    /**
     * Get the default factory;
     */
    public static ServerSocketFactory getDefault() {
        return new MQServerSocketFactory();
    }

    /**
     * Create a factory that wraps the specified factory.
     */
    public static ServerSocketFactory wrapFactory(ServerSocketFactory ssf) {
        return new MQServerSocketFactory(ssf);
    }


    /**
     * Return a string description of a ServerSocket
     */
    public static String serverSocketToString(ServerSocket s) {

        try {
            return "SO_RCVBUF=" + s.getReceiveBufferSize() +
                ", SO_REUSEADDR=" + s.getReuseAddress() +
                ", SO_TIMEOUT=" + s.getSoTimeout();
        } catch (IOException e) {
            return "Bad serverSocket: " + e;
        }
    }
}
