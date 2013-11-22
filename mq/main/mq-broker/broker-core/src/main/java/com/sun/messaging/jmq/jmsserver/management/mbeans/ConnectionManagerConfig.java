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
 * @(#)ConnectionManagerConfig.java	1.17 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;

import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.management.util.ConnectionUtil;

public class ConnectionManagerConfig extends MQMBeanReadWrite  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ConnectionAttributes.NUM_CONNECTIONS,
					Integer.class.getName(),
					mbr.getString(mbr.I_CXN_MGR_ATTR_NUM_CONNECTIONS),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] destroySignature = {
	    new MBeanParameterInfo("connectionID", String.class.getName(), 
					mbr.getString(mbr.I_CXN_MGR_OP_DESTROY_PARAM_CXN_ID_DESC))
		        };

    /*
    private static MBeanParameterInfo[] destroyServiceSignature = {
	    new MBeanParameterInfo("serviceName", String.class.getName(), 
					"Service Name")
		        };
    */

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ConnectionOperations.DESTROY,
		    mbr.getString(mbr.I_CXN_MGR_OP_DESTROY_DESC),
		    destroySignature, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),

	    /*
	    new MBeanOperationInfo("destroyConnectionsInService",
		    "Destroy all connections in the specified service",
		    destroyServiceSignature, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),
	    */

	    new MBeanOperationInfo(ConnectionOperations.GET_CONNECTIONS,
		    mbr.getString(mbr.I_CXN_MGR_CFG_OP_GET_CONNECTIONS_DESC),
		    null , 
		    ObjectName[].class.getName(),
		    MBeanOperationInfo.INFO)
		};

    public ConnectionManagerConfig()  {
	super();
    }

    public Integer getNumConnections()  {
	List connections = ConnectionUtil.getConnectionInfoList(null);

	return (Integer.valueOf(connections.size()));
    }

    public void destroy(String connectionID)  {
        if (connectionID == null)  {
            throw new
                IllegalArgumentException("Null connection ID specified");
        }

	long longCxnID = 0;

	try  {
            longCxnID = Long.parseLong(connectionID);
        } catch (NumberFormatException e)  {
            throw new
                IllegalArgumentException("Invalid connection ID specified: " + connectionID);
        }

	ConnectionUtil.destroyConnection(longCxnID, "Destroy operation invoked from " + getMBeanDescription());
    }

    public void destroyConnectionsInService(String serviceName)  {
        if (serviceName == null)  {
            throw new
                IllegalArgumentException("Null service name specified");
        }

	ConnectionUtil.destroyConnection(serviceName, 
		"Destroy operation invoked from " + getMBeanDescription());
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
		    MQObjectName.createConnectionConfig(Long.toString(cxnInfo.uuid));

	        oNames[i++] = o;
	    } catch (Exception e)  {
		handleOperationException(ConnectionOperations.GET_CONNECTIONS, e);
	    }
        }

	return (oNames);
    }

    public String getMBeanName()  {
	return ("ConnectionManagerConfig");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_CXN_MGR_CFG_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (null);
    }
}
