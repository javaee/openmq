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
 * @(#)TCPConnectionHandler.java	1.23 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.protocol.tcp;

import javax.jms.*;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;
import com.sun.messaging.jmq.jmsclient.protocol.SocketConnectionHandler;

import java.net.*;
import java.util.logging.Level;
import java.io.*;


/**
 * This class is the default protocol handler for the iMQ JMS client
 * implementation.  It uses TCP protocol to communicate with the Broker.
 */
public class TCPConnectionHandler extends SocketConnectionHandler {

    private static int connectionCount = 0;
    private int counter = 0;

    private Socket socket = null;
    
    private int socketConnectTimeout = 0;
    
    //private static int soLingerTime = 5;

    private String host = null;

    private int baseport = 0;
    private int directport = 0;
    private int port = 0;
    
    //check if host is reachable
    public static boolean imqCheckHostIsReachable = false;
    
    //is reachable time out value in milli seconds.
    public static int imqIsReachableTimeout = 30000;
          
    static {
    	
    	try {
    		//check is reachable
    		imqCheckHostIsReachable = Boolean.getBoolean("imqCheckHostIsReachable");
    		
    		//in milli secs
    		String tmp = System.getProperty("imqIsReachableTimeout", "30000");
    		
    		//is reachable timeout
    		imqIsReachableTimeout = Integer.parseInt(tmp);
    		
    	} catch (Exception ex) {
    		ConnectionImpl.getConnectionLogger().log(Level.WARNING, ex.toString(), ex);
    	}
    	 
    }
    
    /**
     * Constructor.  Called by TCPStreamHandler.
     * This creates a socket connection to the broker.
     */
    TCPConnectionHandler (Object conn) throws JMSException {
        ConnectionImpl connection = (ConnectionImpl) conn;
        directport = 0;

        // First, gather the configuration attributes.
        host = connection.getProperty(
            ConnectionConfiguration.imqBrokerHostName);
        baseport = Integer.parseInt(connection.getProperty(
            ConnectionConfiguration.imqBrokerHostPort));
        directport = Integer.parseInt(connection.getProperty(
            ConnectionConfiguration.imqBrokerServicePort));
        String namedservice = connection.getProperty(
            ConnectionConfiguration.imqBrokerServiceName);
        socketConnectTimeout=connection.getSocketConnectTimeout();
        

        // Resolve the service port if necessary.
        if (directport == 0) {
            PortMapperClient pmc = new PortMapperClient(connection);
            if (namedservice != null && !("".equals(namedservice))) {
                port = pmc.getPortForService("tcp", namedservice);
            } else {
                port = pmc.getPortForProtocol("tcp");
            }
        } else {
            port = directport;
        }

        ConnectionImpl.checkHostPort (host, port);

        // Create the connection
        try {
            connection.setLastContactedBrokerAddress( getBrokerAddress() );
            this.socket = makeSocket(host, port);
            counter = ++connectionCount;
        } catch ( Exception e ) {
            connection.getExceptionHandler().handleConnectException (
                e, host, port);
        }
    }

    /**
     * Constructor.  Called by TCPStreamHandler.
     * This creates a socket connection to the broker.
     */
    TCPConnectionHandler (MQAddress addr, ConnectionImpl conn)
        throws JMSException {
        ConnectionImpl connection = (ConnectionImpl) conn;
        port = 0;

        // First, gather the configuration attributes.
        host = addr.getHostName();
        directport = 0;
        if (addr.isServicePortFinal())
            directport = addr.getPort();
        String namedservice = addr.getServiceName();
        socketConnectTimeout=connection.getSocketConnectTimeout();

        // Resolve the service port if necessary.
        if (directport == 0) {
            PortMapperClient pmc = new PortMapperClient(addr, connection);
            baseport = pmc.getHostPort();
            if (namedservice != null && !("".equals(namedservice))) {
                port = pmc.getPortForService("tcp", namedservice);
            } else {
                port = pmc.getPortForProtocol("tcp");
            }

        } else {
            port = directport;
        }

        conn.setLastContactedBrokerAddress( getBrokerAddress() );

        ConnectionImpl.checkHostPort (host, port);

        // Create the connection
        try {
            this.socket = makeSocket(host, port);
            counter = ++connectionCount;
        } catch ( Exception e ) {
            connection.getExceptionHandler().handleConnectException (
                e, host, port);
        }
    }

    private Socket makeSocket(String host, int port) throws Exception {
        if (Debug.debug) {
            Debug.println("in TCPConnectionHandler.makeSocket()");
        }

        //tcp no delay flag
        boolean tcpNoDelay = true;
        String prop = System.getProperty("imqTcpNoDelay", "true");
        if ( prop.equals("false") ) {
            tcpNoDelay = false;
        }
        
        checkIsReachable(host, port);

        //Socket socket = new Socket(host, port);
        
        //bug 6696742 - be able to set connect timeout 
        Socket socket = makeSocketWithTimeout(host, port, socketConnectTimeout);
        
        socket.setTcpNoDelay( tcpNoDelay );

        return socket;
    }
    
    /**
     * Check if a host is reachable.
     * 
     * @param host 
     * @param port
     * @throws IOException
     */
    private void checkIsReachable (String host, int port) throws IOException {
    	
    	if (imqCheckHostIsReachable) {
    		
    		ConnectionImpl.getConnectionLogger().fine ("checking network is reachable");
    		
    		//get instance
    		InetAddress iaddr = InetAddress.getByName(host);
    		
    		//check if reachable
    		boolean isReachable = iaddr.isReachable(imqIsReachableTimeout);
    		
    		if ( isReachable == false ) {
    			
    			ConnectionImpl.getConnectionLogger().fine ("network is not reachable, host=" + host);
    			
    			throw new IOException ("Network is unreachable. Host= " + host);
    		} else {
    			ConnectionImpl.getConnectionLogger().fine ("network is reachable, host=" + host);
    		}
    	}
    	
    }
    
    private Socket makeSocketWithTimeout (String host, int port, int timeout) throws IOException {
    	
    	Socket socket = null;
    	
    	if (timeout > 0) {
    		
    		ConnectionImpl.getConnectionLogger().fine ("connecting with timeout=" + timeout);
    		
    		socket = new Socket();
    	
    		InetSocketAddress socketAddr = new InetSocketAddress (host, port);
    	
    		socket.connect(socketAddr, timeout);
    	
    		//disable the timeout
    		socket.setSoTimeout(0);
    		
    	} else {
    		
    		ConnectionImpl.getConnectionLogger().fine ("connecting with no timeout ...");
    		
    		socket = new Socket(host, port);
    	}
    	
    	ConnectionImpl.getConnectionLogger().fine ("socket connected., host=" + host + ", port="+ port);
    	
    	return socket;
    }
    
    

    /*
     * Get socket input stream.
     */
    public InputStream
    getInputStream() throws IOException {
        return socket.getInputStream();
    }

     /*
     * Get socket output stream.
     */
    public OutputStream
    getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

     /*
     * Get socket local port for the current connection.
     */
    public int
    getLocalPort() throws IOException {
        return socket.getLocalPort();
    }
    
    protected void closeSocket() throws IOException{
    	socket.close();
    }

    public String getBrokerHostName() {
        return this.host;
    }

    public String getBrokerAddress() {

        if (directport == 0) {
            return host + ":" + baseport + "(" + port + ")";
        } else {
            return host + ":" + directport;
        }
        //return host + ":" port;
    }
    
    public int getSocketConnectTimeout() {
		return socketConnectTimeout;
    }

    public String toString() {
        String info = null;
        try {
        info =  "TCPConnectionHandler: " + counter + "-" + getLocalPort();
        } catch (Exception e) {
            Debug.printStackTrace(e);
        }

        return info;
    }

}
