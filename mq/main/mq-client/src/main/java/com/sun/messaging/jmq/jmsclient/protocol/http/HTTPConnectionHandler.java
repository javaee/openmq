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
 * @(#)HTTPConnectionHandler.java	1.15 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.protocol.http;

import java.io.*;
import java.net.*;
import javax.jms.*;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;
import com.sun.messaging.jmq.jmsclient.protocol.SocketConnectionHandler;

import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;

/**
 * This class implements the HTTP protocol connection handler
 * for iMQ clients.
 */
public class HTTPConnectionHandler extends SocketConnectionHandler {

    private static final String socketClass =
        "com.sun.messaging.jmq.httptunnel.tunnel.HttpTunnelSocketImpl"; 

    private HttpTunnelSocket socket = null;

    private String URLString = null;
    /**
     * Create a connection with broker.
     */
    public HTTPConnectionHandler (Object conn) throws JMSException {
        ConnectionImpl connection = (ConnectionImpl) conn;
        URLString = connection.getProperty(ConnectionConfiguration.imqConnectionURL);

        if (URLString == null) {
            throw new JMSException(ConnectionConfiguration.imqConnectionURL + " property not found.");
        }

        try {
            socket = (HttpTunnelSocket)Class.forName(socketClass).newInstance();
            socket.init(URLString);
        } catch ( Exception e ) {
            connection.getExceptionHandler().handleConnectException (
                e, URLString);
        } finally {
            connection.setLastContactedBrokerAddress(URLString);
        }
    }

    public HTTPConnectionHandler (MQAddress addr, ConnectionImpl conn)
        throws JMSException {
        ConnectionImpl connection = (ConnectionImpl) conn;
        URLString = addr.getURL();

        if (URLString == null) {
            throw new JMSException("URL not found.");
        }

        try {
            socket = (HttpTunnelSocket)Class.forName(socketClass).newInstance();
            socket.init(URLString);
        } catch ( Exception e ) {
            connection.getExceptionHandler().handleConnectException (
            e, URLString );
        } finally {
            conn.setLastContactedBrokerAddress(URLString);
        }
    }

    /**
     * Get socket input stream.
     */
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * Get socket output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * Get socket local port for the current connection.
     */
    public int
    getLocalPort() throws IOException {
        return socket.getConnId();
    }
    
	protected void closeSocket() throws IOException {
        socket.close();
	}

    public String getBrokerHostName() {
        return this.URLString;
    }

    public String getBrokerAddress() {
        return this.URLString;
    }
}

/*
 * EOF
 */
