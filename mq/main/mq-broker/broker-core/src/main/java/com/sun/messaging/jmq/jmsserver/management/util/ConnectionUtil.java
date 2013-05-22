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
 * @(#)ConnectionUtil.java	1.12 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;
import com.sun.messaging.jmq.util.admin.MessageType;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.GoodbyeReason;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;

import com.sun.messaging.jmq.jmsserver.service.Service;

public class ConnectionUtil {
    /**
     * Returns a List of IMQConnection
     */
    public static List getConnections()  {
	List connections = getConnections(null);

	return (connections);
    }

    /**
     * Returns a List of IMQConnection for a given service
     */
    public static List getConnections(String service)  {
	ConnectionManager cm = Globals.getConnectionManager();
	List connections = null;

	try  {
	    Service s = null;

	    if (service != null)  {
	        s = Globals.getServiceManager().getService(service);

		/*
		 * If service object is null, service may not exist or is inactive
		 */
		if (s == null)  {
		    return (connections);
		}
	    }

	    connections = cm.getConnectionList(s);
	} catch(Exception e)  {
            BrokerResources	rb = Globals.getBrokerResources();
	    Logger logger = Globals.getLogger();

            logger.log(Logger.WARNING, 
		rb.getString(rb.W_JMX_FAILED_TO_OBTAIN_CONNECTION_LIST),
		e);
	}

	return (connections);
    }

    /**
     * Returns a List of ConnectionInfo for the given service
     * or all services if the passed service is null.
     */
    public static List getConnectionInfoList(String service)  {
	ConnectionManager cm = Globals.getConnectionManager();
	List connections, connectionInfoList = new ArrayList();
	IMQConnection  cxn;
	ConnectionInfo cxnInfo;

	try  {
	    Service s = null;

	    if (service != null)  {
	        s = Globals.getServiceManager().getService(service);

		/*
		 * If service object is null, service may not exist or is inactive
		 */
		if (s == null)  {
		    return (connectionInfoList);
		}
	    }

	    connections = cm.getConnectionList(s);
	} catch(Exception e)  {
            BrokerResources	rb = Globals.getBrokerResources();
	    Logger logger = Globals.getLogger();

            logger.log(Logger.WARNING, 
		rb.getString(rb.W_JMX_FAILED_TO_OBTAIN_CONNECTION_LIST),
		e);

	    return (connectionInfoList);
	}

	if (connections.size() == 0)  {
	    return (connectionInfoList);
	}

        Iterator iter = connections.iterator();

        while (iter.hasNext()) {
	    cxn     = (IMQConnection)iter.next();
	    cxnInfo = cxn.getConnectionInfo();

	    connectionInfoList.add(cxnInfo);
        }

	return (connectionInfoList);
    }

    /**
     * Returns the ConnectionInfo for the passed connection ID.
     */
    public static ConnectionInfo getConnectionInfo(long id)  {
	ConnectionManager cm = Globals.getConnectionManager();
	ConnectionInfo cxnInfo = null;
	IMQConnection  cxn = null;

	cxn = (IMQConnection)cm.getConnection(new ConnectionUID(id));

	if (cxn == null)  {
	    return (null);
	}

	cxnInfo = cxn.getConnectionInfo();

	return (cxnInfo);
    }

    public static String getServiceOfConnection(long id)  {
	ConnectionInfo cxnInfo = getConnectionInfo(id);

	if (cxnInfo == null)  {
	    return (null);
	}

	return(cxnInfo.service);
    }

    public static Long getCreationTime(long cxnId)  {
	long currentTime = System.currentTimeMillis();
	ConnectionUID cxnUID = new ConnectionUID(cxnId);

	return (new Long(currentTime - cxnUID.age(currentTime)));
    }


    public static List getConsumerIDs(long cxnId)  {
	ConnectionManager	cm = Globals.getConnectionManager();
	ConnectionInfo		cxnInfo = null;
	IMQConnection		cxn = null;
	List			consumerIDs;

	cxn = (IMQConnection)cm.getConnection(new ConnectionUID(cxnId));
	consumerIDs = cxn.getConsumersIDs();

	return (consumerIDs);
    }

    public static List getProducerIDs(long cxnId)  {
	ConnectionManager	cm = Globals.getConnectionManager();
	ConnectionInfo		cxnInfo = null;
	IMQConnection		cxn = null;
	List			producerIDs;

	cxn = (IMQConnection)cm.getConnection(new ConnectionUID(cxnId));
	producerIDs = cxn.getProducerIDs();

	return (producerIDs);
    }

    public static void destroyConnection(long cxnId, String reasonString)  {
	ConnectionManager	cm = Globals.getConnectionManager();
	IMQConnection		cxn = null;

	cxn = (IMQConnection)cm.getConnection(new ConnectionUID(cxnId));

	if (cxn != null)  {
	    cxn.destroyConnection(true, GoodbyeReason.ADMIN_KILLED_CON,
				    reasonString);
	}
    }

    public static void destroyConnection(String serviceName, String reasonString)  {
	List			cxnList = getConnections(serviceName);
	IMQConnection		cxn = null;

	/*
	 * Return if no connections to destroy
	 */
	if ((cxnList == null) || (cxnList.size() == 0))  {
	    return;
	}

        Iterator iter = cxnList.iterator();

        while (iter.hasNext()) {
	    cxn     = (IMQConnection)iter.next();

	    if (cxn != null)  {
	        cxn.destroyConnection(true, GoodbyeReason.ADMIN_KILLED_CON,
				    reasonString);
	    }
        }
    }
}
