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
 * @(#)PortMapper.java	1.35 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import javax.net.ServerSocketFactory;

import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.io.PortMapperTable;
import com.sun.messaging.jmq.io.PortMapperEntry;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.service.PortMapperClientHandler;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsserver.BrokerStateHandler;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.util.LockFile;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.BrokerMQAddress;
import com.sun.messaging.jmq.util.net.MQServerSocketFactory;
import com.sun.messaging.portunif.PUService;
import com.sun.messaging.jmq.jmsserver.service.portunif.PortMapperMessageFilter;


/**
 * The PortMapper is a simple service that hands out service/port pairs.
 * Basically a thread listens on a ServerSocket. When a client connects
 * the PortMapper dumps the port map to the socket, closes the connection
 * and goes back to listening on the socket.
 */
public class PortMapper implements Runnable, ConfigListener, PortMapperClientHandler {

    public static final int PORTMAPPER_DEFAULT_PORT = 7676;

    public static final String SERVICE_NAME = "portmapper";

    // Hostname can not be dynamically updated
    public static final String HOSTNAME_PROPERTY = Globals.IMQ +
                                                   ".portmapper.hostname";

    private static final String IMQHOSTNAME_PROPERTY = Globals.IMQ +
                                                    ".hostname";

    private static final String PORT_PROPERTY = Globals.IMQ +
                                                    ".portmapper.port";
    public static final String BIND_PROPERTY = Globals.IMQ +
    												".portmapper.bind";
    private static final String BACKLOG_PROPERTY = Globals.IMQ +
                                                    ".portmapper.backlog";
    private static final String SOTIMEOUT_PROPERTY = Globals.IMQ +
                                                    ".portmapper.sotimeout";
    private static final String SOLINGER_PROPERTY = Globals.IMQ +
                                                    ".portmapper.solinger";


    private static boolean DEBUG = false;

    private BrokerResources rb = null;
    private BrokerConfig bc = null;

    protected Logger logger = null;
    protected PortMapperTable portMapTable = null;

    private ServerSocket   serverSocket = null;
    private int port = 0;
    
    // specifies whether the portmapper should bind to the portmapper port,
    // or whether some other component will do that on our behalf 
    private boolean doBind = true;

	private int backlog = 100;
    private int sotimeout = 100;
    private int solinger = -1;
    private InetAddress bindAddr = null;
    private String hostname = null;
    private HashMap portmapperMap = null;

    private MQAddress mqaddress = null;

    private boolean running = false;

    private static MQServerSocketFactory ssf = (MQServerSocketFactory)MQServerSocketFactory.getDefault();


    public boolean getDEBUG() {
        return DEBUG;
	}

    /**
     * updates the portmapper service
     */
    public void updateProperties() {
        // if we have a brokerid set, publish in in the portmapper
        // entry
        portmapperMap = new HashMap();
        if (Globals.getBrokerID() != null) {
            portmapperMap.put("brokerid", Globals.getBrokerID());
        }
        if (Globals.getBrokerSessionID() != null) {
            portmapperMap.put("sessionid", Globals.getBrokerSessionID().toString());

	    String tmp;
	    tmp = Globals.getConfig().getProperty(Globals.JMQ_HOME_PROPERTY);
	    if ((tmp != null) && (!tmp.equals("")))  {
                portmapperMap.put("imqhome", tmp);
	    }
	    tmp = Globals.getConfig().getProperty(Globals.JMQ_VAR_HOME_PROPERTY);
	    if ((tmp != null) && (!tmp.equals("")))  {
                portmapperMap.put("imqvarhome", tmp);
	    }
        }
        updateServiceProperties(SERVICE_NAME, portmapperMap );
    }
    /**
     * Create a portmapper for this instance of a broker.
     *
     * @param instance  Instance name of this broker
     */
    public PortMapper(String instance) {
		running = true;
		bc = Globals.getConfig();
		if (!bc.getBooleanProperty("imq.portmapper.reuseAddress", true)) {
			ssf.setReuseAddress(false);
		}
		portMapTable = new PortMapperTable();
		portMapTable.setBrokerInstanceName(instance);
		portMapTable.setBrokerVersion(Globals.getVersion().getProductVersion());
		logger = Globals.getLogger();
		rb = Globals.getBrokerResources();

		addService(SERVICE_NAME, "tcp", "PORTMAPPER", port, portmapperMap);
		addService("cluster_discovery", "tcp", "CLUSTER_DISCOVERY", 0, null);

		bc.addListener(PORT_PROPERTY, this);
		bc.addListener(BACKLOG_PROPERTY, this);
	}

    public void destroy() {
        running = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException ex) {
            logger.logStack(Logger.INFO,"Error closing portmapper", ex);
        }
        serverSocket = null;
        PUService pu = Globals.getPUService();
        if (pu != null) {
            try {
                pu.destroy();
            } catch (IOException ex) {
                logger.logStack(Logger.INFO,
                "Error closing Grizzly PU service transport", ex);
            }
        }
    }

    /**
     * Configure the portmapper with its properties
     */
    public void setParameters(BrokerConfig params)
        throws PropertyUpdateException {
    	
    	// doBind must be checked first
        doBind = params.getBooleanProperty(PortMapper.BIND_PROPERTY,true);

        // Hostname must be configured next
        String value = (String)params.getProperty(HOSTNAME_PROPERTY);
        if (value == null || value.trim().length() == 0) {
            // If portmapper specific hostname is not set, check imq.hostname
            value = (String)params.getProperty(IMQHOSTNAME_PROPERTY);
        }
        validate(HOSTNAME_PROPERTY, value);
        update(HOSTNAME_PROPERTY, value);

        value = (String)params.getProperty(PORT_PROPERTY);
        validate(PORT_PROPERTY, value);
        update(PORT_PROPERTY, value);

        value = (String)params.getProperty(BACKLOG_PROPERTY);
        validate(BACKLOG_PROPERTY, value);
        update(BACKLOG_PROPERTY, value);

        value = (String)params.getProperty(SOTIMEOUT_PROPERTY);
        if (value != null) {
            validate(SOTIMEOUT_PROPERTY, value);
            update(SOTIMEOUT_PROPERTY, value);
        }

        value = (String)params.getProperty(SOLINGER_PROPERTY);
        if (value != null) {
            validate(SOLINGER_PROPERTY, value);
            update(SOLINGER_PROPERTY, value);
        }
    }

    /**
     * Change the portmapper service's port
     */
    private synchronized void setPort(int port) {

        if (port == this.port) {
            return;
        }

        this.port = port;

        addService(SERVICE_NAME, "tcp", "PORTMAPPER", port, portmapperMap);

        LockFile lf = LockFile.getCurrentLockFile();

	    try {
	        if (lf != null) {
	            lf.updatePort(port);
	        }
        } catch (IOException e) {
	        logger.log(Logger.WARNING, rb.E_LOCKFILE_BADUPDATE, e);
        }

        if (serverSocket != null) {
            // If there is a server socket close it so we create a new
            // one with the new port
            try {
                serverSocket.close();
            } catch (IOException e) {}
        }
        PUService pu = Globals.getPUService();
        if (pu != null) { 
            try {
                pu.rebind(new InetSocketAddress(bindAddr, port));
            } catch (IOException e) {
                logger.logStack(logger.ERROR,
                "Failed to reconfigure PU service port to "+port, e);
            }
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Change the portmapper service's host interface
     */
    public synchronized void setHostname(String hostname)
        throws PropertyUpdateException {

        MQAddress mqaddr = null;
        try {
            String h = hostname;
            if (hostname != null && 
                hostname.equals(Globals.HOSTNAME_ALL)) {
                h = null;
            }
            mqaddr = MQAddress.getMQAddress(h, getPort());
        } catch (Exception e) {
            throw new PropertyUpdateException(
                PropertyUpdateException.InvalidSetting,
                hostname+": "+e.toString(), e);
        }

        if (hostname == null || hostname.equals(Globals.HOSTNAME_ALL) ||
            hostname.trim().length() == 0) {
            // Bind to all
            this.hostname = null;
            this.bindAddr = null;
            mqaddress = mqaddr;
            PUService pu = Globals.getPUService();
            if (pu != null) {
                try {
                    pu.rebind(new InetSocketAddress(bindAddr, port));
                } catch (IOException e) {
                    logger.logStack(logger.ERROR, 
                        "Failed to reconfigure PU service to "+bindAddr, e);
                }
            }
            return;
        }

        if (hostname.equals(this.hostname)) {
	        return;
        }

        try {
            if (Globals.isConfigForCluster()) {
                String hn = hostname;
                if (hn != null && hn.equals("localhost")) hn = null; 
                this.bindAddr = BrokerMQAddress.resolveBindAddress(hn, true);
                mqaddr = MQAddress.getMQAddress(
                    this.bindAddr.getHostAddress(), getPort());
            } else {
                this.bindAddr = InetAddress.getByName(hostname);
            }
        } catch (Exception e) {
            throw new PropertyUpdateException(
                    PropertyUpdateException.InvalidSetting,
                    rb.getString(rb.E_BAD_HOSTNAME, hostname),
                    e);
        }

        this.hostname = hostname;
        this.mqaddress =  mqaddr;

        LockFile lf = LockFile.getCurrentLockFile();

        try {
	        if (lf != null) {
	           lf.updateHostname(mqaddress.getHostName());
	        }
        } catch (IOException e) {
	        logger.log(Logger.WARNING, rb.E_LOCKFILE_BADUPDATE, e);
        }

        if (serverSocket != null) {
            // If there is a server socket close it so we create a new
            // one with the new port
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }

        PUService pu = Globals.getPUService();
        if (pu != null) {
            try {
                pu.rebind(new InetSocketAddress(bindAddr, port));
            } catch (IOException e) {
                logger.logStack(logger.ERROR,
                "Failed to reconfigure PU service to "+bindAddr, e);
            }
        }
    }

    /**
     * This should never return HOSTNAME_ALL when
     * called by Globals.getPortMapper().getHostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This should never return null when called 
     * by Globals.getPortMapper().getMQAddress
     */
    public MQAddress getMQAddress() {
        return mqaddress;
    }

    public InetAddress getBindAddress() {
        return bindAddr;
    }

    /**
     * caller must not modify the returned table
     */
    public PortMapperTable getPortMapTable() {
        return portMapTable;
    }

    /**
     * Set the backlog parameter on the socket the portmapper is running on
     */
    public synchronized void setBacklog(int backlog) {
        this.backlog = backlog;

        if (serverSocket != null) {
            // If there is a server socket close it so we create a new
            // one with the new backlog
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }

        PUService pu = Globals.getPUService();
        if (pu != null) {
            try {
                pu.setBacklog(backlog);
            } catch (IOException e) {
                logger.logStack(logger.WARNING, 
                "Failed to set PU service backlog to "+backlog, e);
            }
        }
    }

    /**
     * Add a service to the port mapper
     *
     * @param   name    Name of service
     * @param   protocl Transport protocol of service ("tcp", "ssl", etc)
     * @param   type    Service type (NORMAL, ADMIN, etc)
     * @param   port    Port service is runningon
     */
    public synchronized void addService(String name,
        String protocol, String type, int port, HashMap props) {
        PortMapperEntry pme = new PortMapperEntry();

        pme.setName(name);
        pme.setProtocol(protocol);
        pme.setType(type);
        pme.setPort(port);

        if (props != null) {
            pme.addProperties(props);
        }
        portMapTable.add(pme);

    }

    /**
     * update the port for a service
     * @param   name    Name of service
     * @param   port    Port service is runningon
     */

    public synchronized void updateServicePort(String name, int port)
    {
        PortMapperEntry pme = portMapTable.get(name);
        if (pme != null) {
           pme.setPort(port);
        }
    }

    /**
     * update the service properties
     * @param   name    Name of service
     * @param   props   Properties for a service
     */

    public synchronized void updateServiceProperties(String name, HashMap props)
    {
        PortMapperEntry pme = portMapTable.get(name);
        if (pme != null) {
            pme.addProperties(props);
        }
    }


    /**
     * Add a service to the port mapper
     *
     * @param   pme    A PortMapperEntry containing all information for
     *                  the service.
     */
    public synchronized void addService(String name, PortMapperEntry pme) {
        portMapTable.add(pme);
    }

    /**
     * Remove a service from the port mapper
     */
    public synchronized void removeService(String name) {
        portMapTable.remove(name);
    }

    /**
     * Get a hashtable containing information for each service.
     * This hashtable is indexed by the service name, and the values
     * are PortMapperEntry's
     */
    public synchronized Map getServices() {
        return portMapTable.getServices();
    }

    public synchronized String toString() {
        return portMapTable.toString();
    }

    /**
     * Bind the portmapper to the port
     * unless we have configured the portmapper to expect some other component to do this on our behalf
     */
    public synchronized void bind() throws Exception {
        PUService pu = Globals.getPUService();
    	if (doBind) {
            if (pu == null) {
    		    serverSocket = createPortMapperServerSocket(this.port, this.bindAddr);
            } else {
                try {
                    pu.register(PortMapperMessageFilter.
                                configurePortMapperProtocol(pu));
                    pu.bind(new InetSocketAddress(this.bindAddr, this.port));
                    pu.start();
                    logger.log(logger.INFO, "Grizzly PU service on ["+
                        (this.bindAddr == null ? "":this.bindAddr)+":"+this.port+"] is ready");
                } catch (Exception e) {
                    String emsg = "PU service failed to init";
                    logger.logStack(logger.ERROR, emsg, e);
                    pu.stop();
                    throw new BrokerException(emsg);
                }
            }
        } else if (pu != null) {
            throw new BrokerException(
                Globals.PUSERVICE_ENABLED_PROP+"=true setting not allowed if nobind");
        }
    }

    /**
     * Return the ServerSocket the portmapper is using.
     * Returns null if portmapper is not currently bound to
     * a server socket.
     */
    public synchronized ServerSocket getServerSocket() {
    	return serverSocket;
    }

    /**
     * Create a ServerSocket for the portmapper
     *
     * @param   port        Port number to create socket on
     * @param   bindAddr    Interface to bind to. Null to bind to all.
     */
    private ServerSocket createPortMapperServerSocket(
                                int port, InetAddress bindAddr)  {

        ServerSocket serverSocket = null;
        try {
            serverSocket = ssf.createServerSocket(port, backlog, bindAddr);
        } catch (BindException e) {
            logger.log(Logger.ERROR, rb.E_BROKER_PORT_BIND, 
                SERVICE_NAME, String.valueOf(port));
            return null;
        } catch (IOException e) {
            logger.log(Logger.ERROR, rb.E_BAD_SERVICE_START, 
                SERVICE_NAME, new Integer(port), e);
            return null;
        }

        Object[] args = {SERVICE_NAME,
                         "tcp [ " + port + ", " + backlog + ", " +
                         (bindAddr != null ? bindAddr.getHostAddress() :
                         Globals.HOSTNAME_ALL) +
                         " ]",
                          new Integer(1), new Integer(1)};
        logger.log(Logger.INFO, rb.I_SERVICE_START, args);

        return serverSocket;
    }

    public void run() {
		int restartCode = BrokerStateHandler.getRestartCode();
		String restartOOMsg = rb.getKString(rb.M_LOW_MEMORY_PORTMAPPER_RESTART);

		String acceptOOMsg = rb.getKString(rb.M_LOW_MEMORY_PORTMAPPER_ACCEPT);

		Socket connection = null;
		boolean oom = false;
		try {

			if (serverSocket == null) {
				serverSocket = createPortMapperServerSocket(this.port, this.bindAddr);
			}

			if (DEBUG && serverSocket != null) {
				logger.log(Logger.DEBUG, "PortMapper: " + serverSocket + " "
						+ MQServerSocketFactory.serverSocketToString(serverSocket) + ", backlog=" + backlog + "");
			}

			boolean firstpass = true;
			while (running) {
				if (serverSocket == null) {
					logger.log(Logger.ERROR, rb.E_PORTMAPPER_EXITING);
					return;
				}

				try {
					connection = serverSocket.accept();
					firstpass = true;
				} catch (SocketException e) {
					if (e instanceof BindException || e instanceof ConnectException
							|| e instanceof NoRouteToHostException) {

						logger.log(Logger.ERROR, rb.E_PORTMAPPER_ACCEPT, e);
						// We sleep in case the exception is not repairable.
						// This
						// prevents us from a tight loop.
						sleep(1);
					} else {
						if (!running)
							break;
						// Serversocket was closed. Should be because something
						// like the port number has changed. Try to recreate
						// the server socket.
						try {
							// Make sure it is closed
							serverSocket.close();
						} catch (IOException ioe) {
						} catch (NullPointerException ioe) {
							if (!running)
								break;
						}
						serverSocket = createPortMapperServerSocket(this.port, this.bindAddr);
					}
					continue;
				} catch (IOException e) {
					logger.logStack(Logger.ERROR, rb.E_PORTMAPPER_ACCEPT, e);
					sleep(1);
					continue;
				} catch (OutOfMemoryError e) {
					if (!running)
						break;
					if (firstpass) {
						firstpass = false;
						Globals.handleGlobalError(e, acceptOOMsg);
						sleep(1);
						continue;
					}
					Broker.getBroker().exit(restartCode, restartOOMsg, BrokerEvent.Type.RESTART, null, false, false,
							true);
					return;
				}

				// process the new portmapper client and close the socket
				handleSocket(connection);

			}  

		} catch (OutOfMemoryError e) {
			oom = true;
			throw e;
		} finally {
			try {
				try {
					if (connection != null)
						connection.close();
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
				}

				if (oom && running) {
					logger.log(Logger.ERROR, restartOOMsg);
					Broker.getBroker().exit(restartCode, restartOOMsg, BrokerEvent.Type.RESTART, null, false, false,
							true);
				}
				if (running)
					logger.log(Logger.INFO, rb.M_PORTMAPPER_EXITING);
			} catch (OutOfMemoryError e) {
				if (running) {
					Broker.getBroker().exit(restartCode, restartOOMsg, BrokerEvent.Type.RESTART, null, false, false,
							true);
				}
			}
		}
	}
    
    /**
     * Process a newly-connected PortMapper client and then close the socket
     * 
     * This method takes a Socket and is intended to be called by this class's run() loop after
     * the incoming connection has been accepted and the new socket created
     * 
     * @param socket the newly-connected PortMapper client
     */
    public void handleSocket(Socket socket) {
		String connOOMsg = rb.getKString(rb.M_LOW_MEMORY_PORTMAPPER_CONNECTION);

		// Make sure socket is still connected. Client may
		// have disconnected if we were slow to accept connection
		if (!socket.isConnected()) {
			logger.log(Logger.DEBUG, "PortMapper: accepted client connection (" + socket.toString() + ") that is"
					+ " no longer connected. Ignoring.");
			try {
				socket.close();
			} catch (IOException e) {
			}
			return;
		}

		// Got connection. Write port map and close connection
		try {
			synchronized (this) {
				/*
				 * Get version from client. 2.0 client does not send a version
				 * so we must set SoTimeout to timeout if there is nothing to
				 * read. 3.0 client does send version (101).
				 */
				socket.setSoTimeout(sotimeout);
				if (solinger > 0) {
					socket.setSoLinger(true, solinger);
				}
				InputStream is = socket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String version = "";// "101";
				try {
					version = br.readLine();
				} catch (SocketTimeoutException e) {
					// 2.0 client did not send version
				}
				// System.out.println("Version = " + version);

				portMapTable.write(socket.getOutputStream());

				// Force reads until EOF. This avoids leaving
				// sockets in TIME_WAIT. See 4750307. Typically
				// this will block until the client closes the
				// connection or SoTimeout expires.
				try {
					int n = 0;
					while (br.readLine() != null) {
						if (++n >= 5)
							break; // Don't read forever
					}
				} catch (SocketTimeoutException e) {
					// Client did not close socket before sotimeout
					// expired. That's OK. Just means we leave connection
					// in TIME_WAIT state.
				}
			}
		} catch (IOException e) {
			InetAddress ia = socket.getInetAddress();
			logger.logStack(Logger.WARNING, rb.E_PORTMAPPER_EXCEPTION, ia.getHostAddress(), e);
		} catch (OutOfMemoryError e) {
			if (!running)
				return;
			logger.log(Logger.WARNING, connOOMsg);
			try {
				socket.close();
			} catch (Throwable t) {
			}
			;
			Globals.handleGlobalError(e, connOOMsg);
			sleep(1);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
    
    
    /**
     * Process a newly-connected PortMapper client and then close the socket
     * 
     * This method takes a SocketChannel and is intended to be called by an external proxy 
     * which has accepted the connection for us and created the new socket
     * 
     * @param clientSocketChannel the newly-connected PortMapper client
     */
	public void handleRequest(SocketChannel clientSocketChannel) {
		if (doBind) throw new IllegalStateException("Should not call PortMapper.handleRequest() unless Broker has been started with the -noBind argument");
		
		handleSocket(clientSocketChannel.socket());
	}   

    public void validate(String name, String value) 
        throws PropertyUpdateException {

        if (!name.equals(PORT_PROPERTY) &&
            !name.equals(BACKLOG_PROPERTY) &&
            !name.equals(SOLINGER_PROPERTY) &&
            !name.equals(HOSTNAME_PROPERTY) &&
            !name.equals(SOTIMEOUT_PROPERTY)) {
            throw new PropertyUpdateException(
                rb.getString(rb.X_BAD_PROPERTY, name));
        }

        if (name.equals(HOSTNAME_PROPERTY)) {
            if (value == null || value.trim().length() == 0 ||
                value.equals(Globals.HOSTNAME_ALL)) {
                // null is OK. Means bind to all interfaces
                return;
            }
            try {
                if (Globals.isConfigForCluster()) {
                    BrokerMQAddress.resolveBindAddress(value, true);
                } else {
                    InetAddress.getByName(value);
                }
            } catch (Exception e) {
                throw new PropertyUpdateException(
                    PropertyUpdateException.InvalidSetting,
                    rb.getKString(rb.E_BAD_HOSTNAME_PROP, value, name)+": "+e.toString(),
                    e);
            }
            return;
        }

        // Will throw an exception if integer value is bad
        int n = getIntProperty(name, value);

        if (name.equals(PORT_PROPERTY)) {
	    if (n == this.port)  {
		return;
            }
	    	if (isDoBind()){
	            // Check if we will be able to bind to this port
	            try {
	                canBind(n, this.bindAddr);
	            } catch (BindException e) {
	                throw new PropertyUpdateException(
	                    rb.getKString(rb.E_BROKER_PORT_BIND, SERVICE_NAME, String.valueOf(value))+
                        "\n"+e.toString());
	            } catch (IOException e) {
	                throw new PropertyUpdateException(
	                    rb.getKString(rb.E_BAD_SERVICE_START, SERVICE_NAME, String.valueOf(value)) +
	                    "\n"+e.toString());
	            }
	    	}
        }
    }

    public boolean update(String name, String value) {
        try {
            if (name.equals(PORT_PROPERTY)) {
                setPort(getIntProperty(name, value));
                if (mqaddress != null) {
                    mqaddress = MQAddress.getMQAddress(
                        mqaddress.getHostName()+":"+getPort());
                }
            } else if (name.equals(BACKLOG_PROPERTY)) {
                setBacklog(getIntProperty(name, value));
            } else if (name.equals(SOTIMEOUT_PROPERTY)) {
                sotimeout = getIntProperty(name, value);
            } else if (name.equals(SOLINGER_PROPERTY)) {
                solinger = getIntProperty(name, value);
            } else {
                setHostname(value);
            }
        } catch (Exception e) {
            logger.log(
                Logger.ERROR,
                rb.getString(rb.X_BAD_PROPERTY_VALUE, name + "=" + value),
                e);
            return false;
        }
        return true;
    }

    public int getIntProperty(String name, String value)
                        throws PropertyUpdateException  {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new PropertyUpdateException(
                rb.getString(rb.X_BAD_PROPERTY_VALUE, name + "=" + value));
        }
    }

    public static void sleep(int nseconds) {
	try {
	    Thread.sleep(nseconds * 1000L);
        } catch (Exception e) {}

    }

    /**
     * Check if you can open a server socket on the specified port
     * and address.
     *
     * @param port      Port number to bind to
     * @param bindAddr  Address to bind to. Null to bind to all connections
     *
     * Throws an IOException if you can't
     */
    public static void canBind(int port, InetAddress bindAddr)
        throws IOException {

        ServerSocket ss = null;
        ss = ssf.createServerSocket(port, 0, bindAddr);
        ss.close();
        return;
    }
    
    /**
     * Return whether the portmapper should bind to the portmapper port,
     * or whether some other component will do that on our behalf 
     * 
     * @return
     */
    public boolean isDoBind() {
		return doBind;
	}

}

