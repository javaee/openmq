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
 * @(#)HTTPProtocol.java	1.32 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net.http;

import java.util.Map;
import java.net.*;
import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import com.sun.messaging.jmq.httptunnel.api.server.*;
import com.sun.messaging.jmq.httptunnel.api.share.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsserver.license.LicenseBase;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.util.*;

public class HTTPProtocol implements Protocol
{
    private static boolean HTTP_ALLOWED = false;

    protected boolean nodelay = true;
    protected static final int defaultPullPeriod = -1;
    protected static final int defaultConnectionTimeout = 300;

    protected String servletHost = null;
    protected int servletPort = -1;
    protected int pullPeriod = defaultPullPeriod;
    protected int connectionTimeout = defaultConnectionTimeout;

    protected int rxBufSize = Globals.getConfig().getIntProperty(
         Globals.IMQ + ".httptunnel.rxBufSize", 0);
        
    protected ProtocolCallback cb = null;
    protected Object callback_data = null;

    protected int inputBufferSize = 2048;
    protected int outputBufferSize = 2048;

    protected HttpTunnelServerDriver driver = null;
    protected HttpTunnelServerSocket serversocket = null;
    protected String driverClass = null;
    protected String serverSocketClass = null;

    static {
        try {
            LicenseBase license = Globals.getCurrentLicense(null);
            HTTP_ALLOWED = license.getBooleanProperty(
                license.PROP_ENABLE_HTTP, false);
        } catch (BrokerException ex) {
            HTTP_ALLOWED = false;
        }
    }

    public HTTPProtocol() {
        if (!HTTP_ALLOWED) {
            Globals.getLogger().log(Logger.ERROR,
                BrokerResources.E_FATAL_FEATURE_UNAVAILABLE,
                Globals.getBrokerResources().getString(
                    BrokerResources.M_HTTP_JMS));
            Broker.getBroker().exit(1,
                Globals.getBrokerResources().getKString(
                    BrokerResources.E_FATAL_FEATURE_UNAVAILABLE,
                    Globals.getBrokerResources().getString(
                        BrokerResources.M_HTTP_JMS)),
                BrokerEvent.Type.FATAL_ERROR);
        }
        driverClass = "com.sun.messaging.jmq.httptunnel.tunnel.server.HttpTunnelServerDriverImpl";
        serverSocketClass = "com.sun.messaging.jmq.httptunnel.tunnel.server.HttpTunnelServerSocketImpl";
    }

    public void registerProtocolCallback(ProtocolCallback cb, 
             Object callback_data)
    {
        this.cb = cb;
        this.callback_data = callback_data;
    }

    protected void notifyProtocolCallback() {
        if (cb != null)
            cb.socketUpdated(callback_data, getLocalPort(), null);
    }


    public String getHostName() {
        return null;
    }

    public boolean canPause() {
        return true;
    }

    public AbstractSelectableChannel getChannel()
        throws IOException
    {
         return null;
    }

    public void configureBlocking(boolean blocking)
        throws UnsupportedOperationException,IOException
    {
         throw new UnsupportedOperationException("HttpProtocol is not a channel, can not change blocking state");
    }

    protected void createDriver() throws IOException {
        String name = InetAddress.getLocalHost().getHostName() + ":" +
            Globals.getConfigName();

        if (servletHost != null || servletPort != -1) {
            String host = servletHost;
            if (host == null)
                host = InetAddress.getLocalHost().getHostAddress();

            int port = servletPort;
            if (port == -1)
                port = HttpTunnelDefaults.DEFAULT_HTTP_TUNNEL_PORT;

            InetAddress paddr = InetAddress.getLocalHost();
            InetAddress saddr = InetAddress.getByName(host);
            InetAddress laddr = InetAddress.getByName("localhost");

            if (port == Globals.getPortMapper().getPort() &&
                (saddr.equals(paddr) || saddr.equals(laddr))) {
                throw new IOException(Globals.getBrokerResources().getString(
                    BrokerResources.X_HTTP_PORT_CONFLICT));
            }

            try {
                driver = (HttpTunnelServerDriver)
                             Class.forName(driverClass).newInstance();
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
            driver.init(name, host, port);
            driver.start();
        }
        else {
            try {
                driver = (HttpTunnelServerDriver)
                             Class.forName(driverClass).newInstance();
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
            driver.init(name);
            driver.start();
        }

        driver.setInactiveConnAbortInterval(connectionTimeout);
        driver.setRxBufSize(rxBufSize);
    }

    protected HttpTunnelServerSocket createSocket() throws IOException {
        if (driver == null) {
            createDriver();
        } 

	HttpTunnelServerSocket sock = null;
	try {
            sock = (HttpTunnelServerSocket)
		       Class.forName(serverSocketClass).newInstance();
	} catch (Exception e) {
            throw new IOException(e.getMessage(), e);
	}
        sock.init(driver);
	return sock;
    }

    private HTTPStreams createConnection(HttpTunnelSocket socket) {
        return new HTTPStreams(socket, inputBufferSize, outputBufferSize);
    }

    public ProtocolStreams accept()  throws IOException
    {
         if (serversocket == null)
             throw new IOException( Globals.getBrokerResources().getString(
                 BrokerResources.X_INTERNAL_EXCEPTION,"Unable to accept on un-opened protocol"));

         HttpTunnelSocket s = serversocket.accept();
         s.setPullPeriod(pullPeriod);
         s.setConnectionTimeout(connectionTimeout);

         HTTPStreams streams = createConnection(s);
         return streams;
    }

    public void open() throws IOException, IllegalStateException
    {
        if (serversocket != null)
             throw new IOException( Globals.getBrokerResources().getString(
                BrokerResources.X_INTERNAL_EXCEPTION,"can not open already opened protocol"));

        if (serversocket == null) {
            synchronized(this) {
                if (serversocket == null) 
                    serversocket = createSocket();
            }
        }
        
        notifyProtocolCallback(); // ok-> socket is creates, callback
    }

    public boolean isOpen() {
        return serversocket != null;
    }

    public void close() throws IOException, IllegalStateException
    {
        synchronized(this) {
            if (serversocket != null) {
                serversocket.close();
                serversocket = null;
            } else {
               throw new IOException( Globals.getBrokerResources().getString(
                   BrokerResources.X_INTERNAL_EXCEPTION,"can not close un-opened protocol"));
            }
        }
    }

    public int getLocalPort() {
        return 0;
    }

    public void checkParameters(Map params)
        throws IllegalArgumentException
    {
    }

    public Map setParameters(Map params)
    {
        boolean active = serversocket != null;

        String newServletHost = getStringValue("servletHost",
            params, null);
        int newServletPort = getIntValue("servletPort",
            params, -1);

        pullPeriod = getIntValue("pullPeriod", params, pullPeriod);
        connectionTimeout = getIntValue("connectionTimeout", params,
            connectionTimeout);

        if ((servletHost != null && !servletHost.equalsIgnoreCase(newServletHost)) 
            || servletPort != newServletPort) {
            /*
                Because of a bug in HttpTunnelServerSocket in JMQ 2.0 we cannot
                close and reopen the listening socket.

                Uncomment this code when the HttpTunnelServerSocket bug is
                fixed.

            if (active) {
                try {
                    close();
                } catch (Exception ex) {
                }
            }
            */

            servletHost = newServletHost;
            servletPort = newServletPort;

            /*
            if (active) {
                try {
                    open();
                } catch (Exception ex) {
                }
            }
            */
        }
        return null;
    }

    private int getIntValue(String propname, Map params, int defval)
    {
        String propvalstr = (String)params.get(propname);
        if (propvalstr == null) return defval;
        try {
            int val = Integer.parseInt(propvalstr);
            return val;
        } catch (Exception ex) {
            return defval;
        }
    }

    private String getStringValue(String propname, Map params,
        String defval) {
        String propvalstr = (String)params.get(propname);
        if (propvalstr == null)
            return defval;
        return propvalstr;
    }

    public String toString() {
        return "http [ " + serversocket + "]";
    }

    public void setNoDelay(boolean set) {
        nodelay = set;

       // LKS - XXX - 10/24/00
       // currently the no delay flag has no affect
       // we may want it to affect the tcp connection between the
       //  broker and servlet in the future
    }

    public void setTimeout(int val) {
       // LKS - XXX - 10/24/00
       // currently the no delay flag has no affect
       // we may want it to affect the tcp connection between the
       //  broker and servlet in the future
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
        return true;
    }
}

/*
 * EOF
 */
