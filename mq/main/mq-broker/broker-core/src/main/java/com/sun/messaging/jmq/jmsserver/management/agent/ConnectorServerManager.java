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
 * @(#)ConnectorServerManager.java	1.16 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.agent;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.io.IOException;
import java.util.Collection;

import javax.management.*;
import javax.management.remote.*;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

/**
 * Manager class for all JMX Connector Servers used by broker.
 *
 */
public class ConnectorServerManager implements NotificationListener {
    private Logger		logger;
    private BrokerConfig	config;
    private Hashtable		connectors;
    private Agent		agent;
    private BrokerResources	rb = Globals.getBrokerResources();

    public ConnectorServerManager(Agent agent)  {
        logger = Globals.getLogger();
        config = Globals.getConfig();
        connectors = new Hashtable();

	/*
	 * passed in because Globals.getAgent() has not been initialized yet.
	 */
        this.agent = agent;
    }

    public JMXConnectorServer getConnectorServer(String name) {
	ConnectorServerInfo	csInfo;

	csInfo = (ConnectorServerInfo)connectors.get(name);

	if (csInfo != null)  {
	    return (csInfo.getConnectorServer());
	} else  {
	    return (null);
	}
    }

    /*
     * Add/create a connector server entry.
     */
    public void add(String name, boolean configuredActive) throws BrokerException  {
	ConnectorServerInfo	csInfo;

        csInfo =  new ConnectorServerInfo(agent, name, configuredActive, this);
        connectors.put(name, csInfo);
    }

    public void remove(String name) throws IOException, BrokerException {
	ConnectorServerInfo	csInfo;

	csInfo = (ConnectorServerInfo)connectors.get(name);

	if (csInfo == null)  {
	    throw new BrokerException(rb.getString(rb.W_JMX_DELETE_CONNECTOR_NON_EXISTANT, 
			name));
	}

	if (csInfo.isActive())  {
	    csInfo.stop();
	}

	try  {
	    JMXConnectorServer cs = csInfo.getConnectorServer();
	    cs.removeNotificationListener(this);
	} catch (ListenerNotFoundException le)  {
	    throw new BrokerException(rb.getString(rb.W_JMX_DELETE_LISTENER_EXCEPTION, 
					le.toString()));
	}

        connectors.remove(name);
    }

    /*
     * Initialize connectors table with list specified by properties:
     *	imq.jmx.connector.list		all connectors
     *	imq.jmx.connector.activelist	connectors to be made active
     */
    public void initConfiguredConnectorServers() throws BrokerException {
        List connectorServers = getAllJMXConnectorNames();
	Set s = connectors.keySet();

	/*
	 * Empty out existing connectors in hashtable
	 */
	Iterator itr = s.iterator();
	while (itr.hasNext()) {
	    String name = (String)itr.next();

	    try  {
	        /*
	         * stop/remove connector
	         */
	        remove(name);
	    } catch (Exception e)  {
		logger.log(Logger.WARNING,
			rb.getString(rb.W_JMX_REMOVE_CONNECTOR_EXCEPTION, name), e);
	    }
        }

	for (int i =0; i < connectorServers.size(); i ++) {
	    String name = (String)connectorServers.get(i);

	    add(name, connectorIsConfiguredActive(name));
	}
    }

    private boolean connectorIsConfiguredActive(String connectorName)  {
        List activeConnectorServers = getAllActiveJMXConnectorNames();

	for (int i =0; i < activeConnectorServers.size(); i ++) {
	    String name = (String)activeConnectorServers.get(i);
	    
	    if (name.equals(connectorName))  {
		return (true);
	    }
	}

	return (false);
    }

    public List getAllJMXConnectorNames() {
        return config.getList(Globals.IMQ + ".jmx.connector.list");
    }

    public List getAllActiveJMXConnectorNames() {
        return config.getList(Globals.IMQ + ".jmx.connector.activelist");
    }

    /**
     * Start one connector server
     */
    public void start(String name)  throws IOException, BrokerException {
        JMXConnectorServer	cs;
	ConnectorServerInfo	csInfo;

	csInfo = (ConnectorServerInfo)connectors.get(name);

	if (csInfo == null)  {
	    throw new BrokerException(rb.getString(rb.W_JMX_START_CONNECTOR_NON_EXISTANT, 
					name));
	}

	try  {
	    csInfo.start();
	    HashMap map = new HashMap();
	    map.put("url", csInfo.getJMXServiceURL().toString());
            Globals.getPortMapper().addService(name, csInfo.getProtocol(), "JMX",
					    csInfo.getPort(), map);
            logger.log(Logger.INFO, 
		rb.getString(rb.I_JMX_CONNECTOR_STARTED, name, csInfo.getJMXServiceURL()));
	} catch (IOException e)  {
            logger.log(Logger.WARNING, 
		rb.getString(rb.W_JMX_CONNECTOR_START_EXCEPTION, name), e);
	    throw (e);
	}
    }

    public void start()  throws IOException, BrokerException {
	Set s = connectors.keySet();

	Iterator itr = s.iterator();
	while (itr.hasNext()) {
	    String name = (String)itr.next();

	    if (connectorIsConfiguredActive(name))  {
	        start(name);
	    }
        }
    }

    /**
     * Stop one connector server
     */
    public void stop(String name)  throws IOException, BrokerException {
        JMXConnectorServer	cs;
	ConnectorServerInfo	csInfo;

	csInfo = (ConnectorServerInfo)connectors.get(name);

	if (csInfo == null)  {
	    throw new BrokerException(rb.getString(rb.W_JMX_STOP_CONNECTOR_NON_EXISTANT, name));
	}

	if (!csInfo.isActive())  {
	    throw new BrokerException(rb.getString(rb.W_JMX_STOP_CONNECTOR_NOT_ACTIVE, name));
	}

	try  {
	    csInfo.stop();
            Globals.getPortMapper().removeService(name);
            logger.log(Logger.INFO, 
		rb.getString(rb.I_JMX_CONNECTOR_STOPPED, name));
	} catch (IOException e)  {
            logger.log(Logger.WARNING, 
		rb.getString(rb.W_JMX_CONNECTOR_STOP_EXCEPTION, name), e);
	    throw (e);
	}
    }

    public void stop()  throws IOException, BrokerException {
	Set s = connectors.keySet();

	Iterator itr = s.iterator();
	while (itr.hasNext()) {
	    String name = (String)itr.next();
	    ConnectorServerInfo	csInfo;

	    csInfo = (ConnectorServerInfo)connectors.get(name);
	    if (csInfo.isActive())  {
	        stop(name);
	    }
        }
    }

    public void handleNotification(Notification notification, Object handback)  {
	if (notification instanceof JMXConnectionNotification)  {
	    JMXConnectionNotification jmxc = (JMXConnectionNotification)notification;
	    ConnectorServerInfo csInfo = (ConnectorServerInfo)handback;
	    JMXConnectorServer cs = csInfo.getConnectorServer();
	    String msg = jmxc.getMessage(), type = jmxc.getType(),
			conId = jmxc.getConnectionId(),
			name = csInfo.getName();
	    String connectionIds[] = cs.getConnectionIds();
	    long sequenceNumber = jmxc.getSequenceNumber(),
	            timeStamp = jmxc.getTimeStamp();

	    Object args[] = new Object [ 3 ];
	    args[0] = name;
	    args[1] = conId;
	    args[2] = new Integer(connectionIds.length);

	    if (type.equals(JMXConnectionNotification.OPENED))  {
                logger.log(Logger.INFO, 
			rb.getString(rb.I_JMX_CONNECTION_OPEN, args));
	    } else if (type.equals(JMXConnectionNotification.CLOSED))  {
                logger.log(Logger.INFO, 
			rb.getString(rb.I_JMX_CONNECTION_CLOSE, args));
	    } else  {
                logger.log(Logger.INFO, 
			rb.getString(rb.I_JMX_CONNECTION_UNKNOWN, args));
	    }
	}
    }

    public Vector getConnectorInfo()  {
	Vector v = new Vector();
	Collection c = connectors.values();

	Iterator itr = c.iterator();
	while (itr.hasNext()) {
	    ConnectorServerInfo info = (ConnectorServerInfo)itr.next();
	    Hashtable h = new Hashtable();
	    String url;

	    h.put("name", info.getName());
	    h.put("active", Boolean.valueOf(info.isActive()));

	    if (info.isActive())  {
		url = info.getJMXServiceURL().toString();
	    } else  {
		url = "";
	    }

	    h.put("url", url);

	    v.add(h);

        }

	return (v);
    }
}
