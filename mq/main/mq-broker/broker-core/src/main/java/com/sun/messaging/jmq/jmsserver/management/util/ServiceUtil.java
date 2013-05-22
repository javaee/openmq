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
 * @(#)ServiceUtil.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.messaging.jmq.jmsserver.Globals;

import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.GetServicesHandler;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.PauseHandler;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

import com.sun.messaging.jms.management.server.MQObjectName;

public class ServiceUtil {
    public static ServiceInfo getServiceInfo(String service)  {
	ServiceInfo si = GetServicesHandler.getServiceInfo(service);

	return (si);
    }

    public static void pauseService(String service) 
		throws BrokerException {
	PauseHandler.pauseService(true, service);
    }

    public static void resumeService(String service)
		throws BrokerException {
	PauseHandler.pauseService(false, service);
    }

    /*
     * Returns a List of service names that are visible to
     * the outside
     */
    public static List getVisibleServiceNames()  {

        ServiceManager sm = Globals.getServiceManager();
        List serviceNames = sm.getAllServiceNames();
	return (serviceNames);
    }
   
    /*
     * Returns an ArrayList of services (ServiceInfo) that are visible to
     * the outside
     */
    public static List getVisibleServices()  {

        List serviceNames = getVisibleServiceNames();
        Iterator iter = serviceNames.iterator();

	ArrayList al = new ArrayList();

        while (iter.hasNext()) {
            String service = (String)iter.next();
	    /*
            System.out.println("\t" + service);
	    */
	    ServiceInfo sInfo = GetServicesHandler.getServiceInfo(service);
	    al.add(sInfo);
        }

	return (al);
    }

    public static int toExternalServiceState(int internalServiceState)  {
	switch (internalServiceState)  {
	case com.sun.messaging.jmq.util.ServiceState.RUNNING:
	    return (com.sun.messaging.jms.management.server.ServiceState.RUNNING);

	case com.sun.messaging.jmq.util.ServiceState.PAUSED:
	    return (com.sun.messaging.jms.management.server.ServiceState.PAUSED);

	case com.sun.messaging.jmq.util.ServiceState.QUIESCED:
	    return (com.sun.messaging.jms.management.server.ServiceState.QUIESCED);

	default:
	    return (com.sun.messaging.jms.management.server.ServiceState.UNKNOWN);
	}
    }

    public static int toInternalServiceState(int externalServiceState)  {
	switch (externalServiceState)  {
	case com.sun.messaging.jms.management.server.ServiceState.RUNNING:
	    return (com.sun.messaging.jmq.util.ServiceState.RUNNING);

	case com.sun.messaging.jms.management.server.ServiceState.PAUSED:
	    return (com.sun.messaging.jmq.util.ServiceState.PAUSED);

	case com.sun.messaging.jms.management.server.ServiceState.QUIESCED:
	    return (com.sun.messaging.jmq.util.ServiceState.QUIESCED);

	default:
	    return (com.sun.messaging.jmq.util.ServiceState.UNKNOWN);
	}
    }

    public static List getConsumerIDs(String service)  {
	List	consumerIDs = new ArrayList(),
		connections = ConnectionUtil.getConnectionInfoList(service);

	if ((connections == null) || (connections.size() == 0))  {
	    return (consumerIDs);
	}

	Iterator itr = connections.iterator();
	int i = 0;
	while (itr.hasNext()) {
	    ConnectionInfo cxnInfo = (ConnectionInfo)itr.next();
	    long cxnID = cxnInfo.uuid;
	    List oneCxnConsumerIDs = ConnectionUtil.getConsumerIDs(cxnID);

	    consumerIDs.addAll(oneCxnConsumerIDs);
	}

	return (consumerIDs);
    }

    public static List getProducerIDs(String service)  {
	List	producerIDs = new ArrayList(),
		connections = ConnectionUtil.getConnectionInfoList(service);

	if ((connections == null) || (connections.size() == 0))  {
	    return (producerIDs);
	}

	Iterator itr = connections.iterator();
	int i = 0;
	while (itr.hasNext()) {
	    ConnectionInfo cxnInfo = (ConnectionInfo)itr.next();
	    long cxnID = cxnInfo.uuid;
	    List oneCxnProducerIDs = ConnectionUtil.getProducerIDs(cxnID);

	    producerIDs.addAll(oneCxnProducerIDs);
	}

	return (producerIDs);
    }

}
