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
 * @(#)SSLConnectionHandler.java	1.32 06/29/07
 */ 

package com.sun.messaging.jmq.jmsclient.protocol.ssl;

import java.net.*;
import java.io.*;

import javax.jms.*;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;
import com.sun.messaging.jmq.jmsclient.protocol.SocketConnectionHandler;

import java.security.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

 /**
  * This class is the SSL protocol handler for the iMQ JMS client
  * implementation.  It uses SSL protocol to communicate with the Broker.
  */
public class SSLConnectionHandler extends SocketConnectionHandler {

    private static boolean isRegistered = false;
    private static boolean debug = Debug.debug;

    private SSLSocket sslSocket = null;

    private String host = null;

    private int baseport = 0;
    private int directport = 0;
    private int port = 0;

    /**
     * Constructor.  Called by SSLStreamHandler.
     * This creates a SSL socket connection to the broker.
     * bug 4959114.
     */
    SSLConnectionHandler (Object conn) throws JMSException {

        ConnectionImpl connection = (ConnectionImpl) conn;
        //int port = 0;
        //String host = null;
        directport = 0;
        try {
            doRegister(connection);

            // First, gather the configuration attributes.
            host = connection.getProperty(
                ConnectionConfiguration.imqBrokerHostName);
            baseport = Integer.parseInt(connection.getProperty(
                ConnectionConfiguration.imqBrokerHostPort));
            directport = Integer.parseInt(connection.getProperty(
                ConnectionConfiguration.imqBrokerServicePort));
            String namedservice = connection.getProperty(
                ConnectionConfiguration.imqBrokerServiceName);
            boolean isHostTrusted = Boolean.valueOf(connection.getProperty(
                ConnectionConfiguration.imqSSLIsHostTrusted)).booleanValue();

            // Resolve the service port if necessary.
            if (directport == 0) {
                PortMapperClient pmc = new PortMapperClient(connection);
                if (namedservice != null && !("".equals(namedservice))) {
                    port = pmc.getPortForService("tls", namedservice);
                } else {
                    port = pmc.getPortForProtocol("tls");
                }

            } else {
                port = directport;
            }

            ConnectionImpl.checkHostPort (host, port);

            // Create the connection
            this.sslSocket = SSLUtil.makeSSLSocket(host, port, isHostTrusted,
                                 connection.getProperty(
                                     ConnectionConfiguration.imqKeyStore, null),
                                 connection.getProperty(
                                     ConnectionConfiguration.imqKeyStorePassword, null),
                                 connection.getConnectionLogger(), AdministeredObject.cr);
        } catch (JMSException jmse) {
            throw jmse;
        } catch (Exception e) {
            connection.getExceptionHandler().handleConnectException (
                e, host, port);
        } finally {
            connection.setLastContactedBrokerAddress(getBrokerAddress());
        }
    }

    /**
     * Constructor.  Called by SSLStreamHandler.
     * This creates a SSL socket connection to the broker.
     * bug 4959114.
     */
    SSLConnectionHandler (MQAddress addr, ConnectionImpl conn)
        throws JMSException {

        ConnectionImpl connection = (ConnectionImpl) conn;
        //int port = 0;
        //String host = null;

        try {
            doRegister(connection);

            // First, gather the configuration attributes.
            host = addr.getHostName();
            directport = 0;
            if (addr.isServicePortFinal())
                directport = addr.getPort();
            String namedservice = addr.getServiceName();
            /**
             * If 'isHostTrusted' is set in address list, it is used.
             * Otherwise, 'imqSSLIsHostTrusted' prop is used.
             */
            boolean isHostTrusted = true;
            if (addr.getIsSSLHostTrustedSet()) {
                isHostTrusted = Boolean.valueOf(
                    addr.getProperty(MQAddress.isHostTrusted)).booleanValue();
            } else {
                isHostTrusted = Boolean.valueOf(connection.getProperty(
                ConnectionConfiguration.imqSSLIsHostTrusted)).booleanValue();

            }

            // Resolve the service port if necessary.
            if (directport == 0) {
                PortMapperClient pmc = new PortMapperClient(addr, connection);
                baseport = pmc.getHostPort();
                if (namedservice != null && !("".equals(namedservice))) {
                    port = pmc.getPortForService("tls", namedservice);
                } else {
                    port = pmc.getPortForProtocol("tls");
                }

            } else {
                port = directport;

            }

            ConnectionImpl.checkHostPort (host, port);

            // Create the connection
            this.sslSocket = SSLUtil.makeSSLSocket(host, port, isHostTrusted,
                                 connection.getProperty(
                                     ConnectionConfiguration.imqKeyStore, null),
                                 connection.getProperty(
                                     ConnectionConfiguration.imqKeyStorePassword, null),
                                 connection.getConnectionLogger(), AdministeredObject.cr);
        } catch (JMSException jmse) {
            throw jmse;
        } catch (Exception e) {
            connection.getExceptionHandler().handleConnectException (
                e, host, port);
        } finally {
            connection.setLastContactedBrokerAddress(getBrokerAddress());
        }
    }

    private void doRegister(ConnectionImpl connection) throws Exception {

        // Not needed for JDK 1.4 or later; for backward compatibility execute
        // the registration code if imq.registerSSLProvider prop is set to true
        if ( Boolean.getBoolean("imq.registerSSLProvider") &&
             isRegistered == false ) {
            synchronized ( this.getClass() ) {
                String name =
                connection.getProperty(ConnectionConfiguration.imqSSLProviderClassname);
                Provider provider = (Provider) Class.forName(name).newInstance();
                Security.addProvider( provider );
                isRegistered = true;
            }
        }
    }

    /*
     * Get SSL socket input stream.
     */
    public InputStream
    getInputStream() throws IOException {
        return sslSocket.getInputStream();
    }

     /*
     * Get SSL socket output stream.
     */
    public OutputStream
    getOutputStream() throws IOException {
        return sslSocket.getOutputStream();
    }

     /*
     * Get SSL socket local port for the current connection.
     */
    public int
    getLocalPort() throws IOException {
        return sslSocket.getLocalPort();
    }

	protected void closeSocket() throws IOException {
		sslSocket.close();
	}

    public String getBrokerHostName() {
        return this.host;
    }

    public int getBrokerPort() {
        return this.port;
    }

    public String getBrokerAddress() {
        if (directport == 0) {
            return host + ":" + baseport + "(" + port + ")";
        } else {
            return host + ":" + directport;
        }
        //return host + ":" + port;
    }

}
