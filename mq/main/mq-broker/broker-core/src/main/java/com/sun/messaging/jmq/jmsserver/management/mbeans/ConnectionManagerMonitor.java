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
 * @(#)ConnectionManagerMonitor.java	1.16 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.List;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;

import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.jmsserver.management.util.ConnectionUtil;
import com.sun.messaging.jmq.jmsserver.Globals;

public class ConnectionManagerMonitor extends MQMBeanReadOnly {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ConnectionAttributes.NUM_CONNECTIONS,
					Integer.class.getName(),
					mbr.getString(mbr.I_CXN_MGR_ATTR_NUM_CONNECTIONS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ConnectionAttributes.NUM_CONNECTIONS_OPENED,
					Long.class.getName(),
					mbr.getString(mbr.I_CXN_MGR_ATTR_NUM_CONNECTIONS_OPENED),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ConnectionAttributes.NUM_CONNECTIONS_REJECTED,
					Long.class.getName(),
					mbr.getString(mbr.I_CXN_MGR_ATTR_NUM_CONNECTIONS_REJECTED),
					true,
					false,
					false)
			};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ConnectionOperations.GET_CONNECTIONS,
		mbr.getString(mbr.I_CXN_MGR_MON_OP_GET_CONNECTIONS_DESC),
		null,
		ObjectName[].class.getName(),
		MBeanOperationInfo.INFO)
		};
	

    private static String[] cxnNotificationTypes = {
		    ConnectionNotification.CONNECTION_OPEN,
		    ConnectionNotification.CONNECTION_CLOSE,
		    ConnectionNotification.CONNECTION_REJECT
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    cxnNotificationTypes,
		    ConnectionNotification.class.getName(),
		    mbr.getString(mbr.I_CXN_NOTIFICATIONS)
		    )
		};

    private long numConnectionsOpened = 0;
    private long numConnectionsRejected = 0;

    public ConnectionManagerMonitor()  {
        super();
    }

    public Integer getNumConnections()  {
	List connections = ConnectionUtil.getConnectionInfoList(null);

	return (Integer.valueOf(connections.size()));
    }

    public long getNumConnectionsOpened()  {
	return (numConnectionsOpened);
    }

    public long getNumConnectionsRejected()  {
	return (numConnectionsRejected);
    }

    public void resetMetrics()  {
        numConnectionsOpened = 0;
        numConnectionsRejected = 0;
    }

    public ObjectName[] getConnections() throws MBeanException  {
	List connections = ConnectionUtil.getConnectionInfoList(null);

	if (connections.size() == 0)  {
	    return (null);
	}

	ObjectName oNames[] = new ObjectName [ connections.size() ];

	Iterator itr = connections.iterator();
	int i = 0;
	while (itr.hasNext()) {
	    ConnectionInfo cxnInfo = (ConnectionInfo)itr.next();
	    try  {
	        ObjectName o = 
		    MQObjectName.createConnectionMonitor(Long.toString(cxnInfo.uuid));

	        oNames[i++] = o;
	    } catch (Exception e)  {
		handleOperationException(ConnectionOperations.GET_CONNECTIONS, e);
	    }
        }

	return (oNames);
    }

    public String getMBeanName()  {
	return ("ConnectionManagerMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_CXN_MGR_MON_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (notifs);
    }

    public void notifyConnectionOpen(long id)  {
	ConnectionNotification cn;
	cn = new ConnectionNotification(ConnectionNotification.CONNECTION_OPEN, 
			this, sequenceNumber++);
	cn.setConnectionID(Long.toString(id));

	sendNotification(cn);
        numConnectionsOpened++;
    }

    public void notifyConnectionClose(long id)  {
	ConnectionNotification cn;
	cn = new ConnectionNotification(ConnectionNotification.CONNECTION_CLOSE, 
			this, sequenceNumber++);
	cn.setConnectionID(Long.toString(id));

	sendNotification(cn);
    }

    public void notifyConnectionReject(String serviceName, String userName,
				String remoteHostString)  {
	ConnectionNotification cn;
	cn = new ConnectionNotification(ConnectionNotification.CONNECTION_REJECT, 
			this, sequenceNumber++);
	cn.setServiceName(serviceName);
	cn.setUserName(userName);
	cn.setRemoteHost(remoteHostString);

	sendNotification(cn);
        numConnectionsRejected++;
    }

}
