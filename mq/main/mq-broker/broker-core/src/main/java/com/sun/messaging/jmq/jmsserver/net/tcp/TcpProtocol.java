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
 * @(#)TcpProtocol.java	1.47 09/11/07
 */ 

package com.sun.messaging.jmq.jmsserver.net.tcp;

import java.net.*;
import java.util.Map;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import javax.net.ServerSocketFactory;

import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.util.net.MQServerSocketFactory;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;

public class TcpProtocol implements Protocol
{

    private static boolean DEBUG = false;

    private static ServerSocketFactory ssf = MQServerSocketFactory.getDefault();

    public static final int defaultReadTimeout = 0;
    public static final int defaultLingerTimeout = 0;
    public static final int defaultPort = 8888;
    public static final int defaultBacklog = 100;
    protected static final Integer zero = Integer.valueOf(0);
    protected static final Integer one = Integer.valueOf(1);

    protected ProtocolCallback cb = null;
    protected Object callback_data = null;

    protected boolean canChangeBlocking = true;

    protected int readTimeout = defaultReadTimeout;
    protected int lingerTimeout = defaultLingerTimeout;
    protected int port = defaultPort;
    protected String hostname = null; // all hosts
    protected int backlog = defaultBacklog; 
    protected boolean nodelay = true;
    protected boolean blocking = true;
    protected int inputBufferSize = 0;
    protected int outputBufferSize = 0;

    protected ServerSocket serversocket = null;
    ServerSocketChannel chl = null;


    Selector selector = null;
    SelectionKey key = null;

    private boolean useChannels = false;

    Object protocolLock = new Object();

    private String modelName = null;


    public TcpProtocol() {
        // does nothing
    }

    public void registerProtocolCallback(ProtocolCallback cb, 
             Object callback_data)
    {
        this.cb = cb;
        this.callback_data = callback_data;
    }

    protected void notifyProtocolCallback() {
        if (cb == null) {
            return;
        }
        cb.socketUpdated(callback_data, getLocalPort(), hostname);
    }

    public String getHostName() {
        return hostname;
    }

    public boolean canPause() {
        return true;
    }

    boolean resetSocket = false;
    boolean startSocket = false;
        

    protected ServerSocket createSocket(String hostname, 
                                        int port, 
                                        int backlog, 
                                        boolean blocking, 
                                        boolean useChannel)
        throws IOException
    {
        ServerSocket svc = null;
        synchronized (protocolLock) {
            this.useChannels = useChannel;
            this.blocking = blocking;
            if (hostname != null && hostname.trim().length() == 0)
                hostname = null;
            if (useChannels) {
                if (!blocking && selector == null)
                    selector = Selector.open();
                chl = ServerSocketChannel.open();
                svc = chl.socket();
                // Bug 6294767: Force SO_REUSEADDRR to true 
                svc.setReuseAddress(true);
                InetSocketAddress endpoint = null;
                if (hostname == null || hostname.equals(Globals.HOSTNAME_ALL)) 
                    endpoint = new InetSocketAddress(port);
                else 
                    endpoint = new InetSocketAddress(hostname, port);
                svc.bind(endpoint, backlog);
                serversocket = svc;
                if (!blocking) {
                    configureBlocking(blocking);
                    key = chl.register(selector, SelectionKey.OP_ACCEPT);
                   selector.wakeup();
                }
            } else {
                if (hostname == null || hostname.equals(Globals.HOSTNAME_ALL))
                    svc = ssf.createServerSocket(port, backlog);
                else  {
                    InetAddress endpoint = InetAddress.getByName(hostname);
                    svc = ssf.createServerSocket(port, backlog, endpoint);
                }
            }

        }

	if (DEBUG && svc != null) {
	    Globals.getLogger().log(Logger.DEBUG,
		"TcpProtocol.creatSocket: " + svc + " " +
		MQServerSocketFactory.serverSocketToString(svc) +
		", backlog=" + backlog + "");
	}

        return svc;
    }

    public void configureBlocking(boolean blocking) 
        throws UnsupportedOperationException, IOException
    {
        if (!useChannels)
             return;
        if (!canChangeBlocking && !blocking)// we cant be non-blocking
            throw new UnsupportedOperationException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                   "This protocol can not be non-blocking"));

        // OK .. only configure blocking IF we have a channel
        AbstractSelectableChannel asc = getChannel();
        if (asc == null) {
            // didnt create w/ blocking.configureBlocking(blocking);
            throw new UnsupportedOperationException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                  "Can not change blocking because "
                  + "there isnt a socket channel"));
        }
        this.blocking = blocking;
        asc.configureBlocking(blocking);
    }

    public AbstractSelectableChannel getChannel()
        throws IOException
    {
        synchronized (protocolLock) {
            if (serversocket == null) {
                return null;
            }
            return serversocket.getChannel();
        }
    }



    protected TcpStreams createConnection(Socket socket)
        throws IOException
    {
        return new TcpStreams(socket, blocking,
                    inputBufferSize, outputBufferSize);
    }

    public ProtocolStreams accept()  
        throws IOException
    {
         ServerSocket currentsocket = null;
         synchronized (protocolLock) {
             currentsocket = serversocket;
         }

        try {
            Socket s = null;
            if (useChannels && !blocking) {
                int cnt = selector.select();
                if (resetSocket) {
                    try {
                        close();
                        serversocket = createSocket(hostname, 
                                 port, backlog, blocking, useChannels);
                        // if we are a non-blocking port -> its changed here
                        notifyProtocolCallback();
                        
                    } catch (IOException ex) {
                    } finally {
                        resetSocket = false;
                    }
                }
                if (startSocket) {
                    try {
                        serversocket = createSocket(hostname, 
                                port, backlog, blocking, useChannels);
                        // if we are a non-blocking port -> its changed here
                        notifyProtocolCallback();
                    } catch (IOException ex) {
                    } finally {
                        startSocket = false;
                    }
                }

                if (cnt > 0) {
                    Set keys = selector.selectedKeys();
                    Iterator itr = keys.iterator();
                    if (itr.hasNext()) {
                        SelectionKey listenkey = (SelectionKey)itr.next();
                        itr.remove();
                        ServerSocketChannel channel = (ServerSocketChannel)
                            listenkey.channel();
                        // workaround for 5046333
                        // get channel, then accept vs
                        // accepting on the low level socket
                        SocketChannel sch = channel.accept();
                        s = sch.socket();
                    }
                } else {
                    return accept();
                }
            } else {
                if (serversocket != null)
                    s = serversocket.accept();
            }
            if (s == null) throw new IOException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    "no socket"));
            try {
            s.setTcpNoDelay(nodelay);
            } catch (SocketException e) {
            Globals.getLogger().log(Logger.WARNING, getClass().getSimpleName()+
            ".accept(): ["+s.toString()+"]setTcpNoDelay("+nodelay+"): "+ e.toString(), e);
            }
            if (readTimeout > 0) {
                try {
                s.setSoTimeout(readTimeout*1000);
                } catch (SocketException e) {
                Globals.getLogger().log(Logger.WARNING, getClass().getSimpleName()+
                ".accept(): ["+s.toString()+"]setSoTimeout("+readTimeout+"): "+ e.toString(), e);
                }
            }
            if (lingerTimeout > 0) {
                try {
                s.setSoLinger(true, lingerTimeout*1000);
                } catch (SocketException e) {
                Globals.getLogger().log(Logger.WARNING, getClass().getSimpleName()+
                ".accept(): ["+s.toString()+"]setSoLinger("+lingerTimeout+"): "+ e.toString(), e);
                }
            }

            TcpStreams streams = createConnection(s);
            return streams;
        } catch (IOException ex) {
            if (currentsocket != serversocket) {
                return accept();
            }
            throw ex;
        }
    }

    public void open() 
        throws IOException, IllegalStateException
    {
         synchronized (protocolLock) {
             if (selector != null && !startSocket) { // we are a channel
                 startSocket = true;
                 selector.wakeup();
                 return;
             }
             if (serversocket != null || startSocket)
                 throw new IOException( 
                      Globals.getBrokerResources().getString(
                          BrokerResources.X_INTERNAL_EXCEPTION,
                          "can not open already opened protocol"));
             serversocket = createSocket(hostname, port, 
                        backlog, blocking, useChannels);
             notifyProtocolCallback();
         }
    }

    public boolean isOpen()
    {
        synchronized (protocolLock) {
            return serversocket != null;
        }
    }

    public void close() 
        throws IOException, IllegalStateException
    {
        synchronized (protocolLock) {
            try {
                if (serversocket == null) {
                     throw new IOException( 
                         Globals.getBrokerResources().getString(
                           BrokerResources.X_INTERNAL_EXCEPTION,
                           "can not close un-opened protocol"));
                }
                // handle closing channel, etc
                ServerSocketChannel channel = (ServerSocketChannel)getChannel();
                if (channel != null) {
                    SelectionKey mykey = channel.keyFor(selector);
                    if (mykey != null) {
                       mykey.cancel();
                    }   
                    channel.close();
                }
                serversocket.close();

                // never close the selector

            } finally {
                serversocket = null;
                key = null;
            }
        }
                
    }

    public void checkParameters(Map params)
    throws IllegalArgumentException {
        checkTcpParameters(params);
    }

    public static void checkTcpParameters(Map params)
    throws IllegalArgumentException {
        checkIntValue("port", params, zero, null);
        checkIntValue("backlog", params, one, null);
    }

    public Map setParameters(Map params) throws IOException {

        if (params.get("serviceFactoryHandlerName") != null) {
            this.modelName = (String)params.get("serviceFactoryHandlerName");
        }

        boolean active = serversocket != null;

        int newport = getIntValue("port", params, port);
        readTimeout = getIntValue("readtimeout", params, readTimeout);
        lingerTimeout = getIntValue("solinger", params, lingerTimeout);
        int newbacklog = getIntValue("backlog", params, backlog);
        blocking = getBooleanValue("blocking", params, blocking);
        useChannels = getBooleanValue("useChannels", params, useChannels);
        String newhostname = (String) params.get("hostname");

        if (newhostname == null) {
            newhostname =  Globals.getHostname();
        }

        if (newhostname == null ||  newhostname.trim().length() == 0) {
            newhostname = Globals.HOSTNAME_ALL;
        }

        int oldport = port;
        int oldbacklog = backlog;

        boolean newhost = (newhostname == null && hostname != null) ||
                   (newhostname != null && hostname == null) ||
                   (newhostname != null && hostname != null &&
                         ! newhostname.equals(hostname));

        if (newport != port || newbacklog != backlog || newhost) {

            if (newport != -1)
                port = newport;
            if (newbacklog != -1)
                backlog = newbacklog;
            if (newhost)
                hostname = newhostname;

             if (!active) {
                 return null;
             }

             // canceling the key and registering the new one will hang
             // so .. just call wakeup
             if (getChannel() != null) {
                 resetSocket = true;
                 selector.wakeup();
                 return null;
             }

             ServerSocket oldserversocket = serversocket;

             try {
                 serversocket = createSocket(hostname, port, 
                               backlog, blocking, useChannels);
                 notifyProtocolCallback();
                 oldserversocket.close();
             } catch (IOException ex) {
                 serversocket = oldserversocket;
                 port = oldport;
                 backlog = oldbacklog;
                 throw ex;
             }
        }
        return null;
    }

    private static int checkIntValue(String propname, Map params)
        throws IllegalArgumentException
    {
        if (params == null) {
            return -1;
        }

        String propvalstr = (String)params.get(propname);

        if (propvalstr == null) {
            return -1;
        }

        try {
            return Integer.parseInt(propvalstr);
        } catch (Exception ex) {}

        throw new IllegalArgumentException( 
           Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,
               "Can not convert " + propname));
    }

    private static int checkIntValue(String propname, Map params,
             Integer min, Integer max)
        throws IllegalArgumentException
    {
        int value = checkIntValue(propname, params);
        if (value == -1) {
            return value;
        }

        if (min != null && value < min.intValue())
            throw new IllegalArgumentException( 
                Globals.getBrokerResources().getString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    propname + "(" + value + ")" 
                + " value below minimum of " + min));

        if (max != null && value > max.intValue())
            throw new IllegalArgumentException( 
                 Globals.getBrokerResources().getString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    propname + "(" + value + ")" 
                +  " value above maximum of " + max));

        return value;
    }
        
    public static int getIntValue(String propname, 
                                  Map params, int defval) {
        String propvalstr = (String)params.get(propname);
        if (propvalstr == null) {
            return defval;
        }
        try {
            int val = Integer.parseInt(propvalstr);
            return val;
        } catch (Exception ex) {
            Globals.getLogger().log(Logger.INFO,
                BrokerResources.E_BAD_PROPERTY_VALUE, propname, ex);
            return defval;
        }
    }

    public static boolean getBooleanValue(String propname, 
                          Map params, boolean defval) {
        String propvalstr = (String)params.get(propname);
        if (propvalstr == null) {
            return defval;
        }
        try {
            boolean val = Boolean.valueOf(propvalstr).booleanValue();
            return val;
        } catch (Exception ex) {
            Globals.getLogger().log(Logger.INFO,
                BrokerResources.E_BAD_PROPERTY_VALUE, propname, ex);
            return defval;
        }
    }

    public int getBacklog() {
        return backlog;
    }

    public int getLocalPort() {
        if (serversocket == null)
            return 0;
        return serversocket.getLocalPort();
    }


    public String toString() {
        boolean nio = (chl != null);
        boolean blockednio = (nio && blocking);
        return "tcp(host = " 
                 + (hostname == null ? Globals.HOSTNAME_ALL  : hostname) 
                 + ", port=" + port + ", mode="+ modelName
                 + (blockednio ? " [blocked i/o]" : "") + ")";
    }

    public void setNoDelay(boolean val) {
        nodelay = val;
    }

    public void setTimeout(int val) {
        throw new UnsupportedOperationException(
            "Setting timeouts no longer supported");
    }

    public void setInputBufferSize(int val) {
        inputBufferSize = val;
    }

    public void setOutputBufferSize(int val) {
        outputBufferSize = val;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }


    public boolean getBlocking() {
        return blocking;
    }
}
