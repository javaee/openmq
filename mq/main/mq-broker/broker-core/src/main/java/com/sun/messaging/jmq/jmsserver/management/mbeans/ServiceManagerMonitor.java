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
 * @(#)ServiceManagerMonitor.java	1.13 06/28/07
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

import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.MetricCounters;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.service.MetricManager;
import com.sun.messaging.jmq.jmsserver.management.util.ServiceUtil;

public class ServiceManagerMonitor extends MQMBeanReadOnly  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ServiceAttributes.MSG_BYTES_IN,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_MSG_BYTES_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.MSG_BYTES_OUT,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_MSG_BYTES_OUT),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_ACTIVE_THREADS,
					Integer.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_ACTIVE_THREADS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_MSGS_IN,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_MSGS_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_MSGS_OUT,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_MSGS_OUT),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_PKTS_IN,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_PKTS_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_PKTS_OUT,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_PKTS_OUT),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.NUM_SERVICES,
					Integer.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_NUM_SERVICES),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.PKT_BYTES_IN,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_PKT_BYTES_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.PKT_BYTES_OUT,
					Long.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_PKT_BYTES_OUT),
					true,
					false,
					false)
			};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ServiceOperations.GET_SERVICES,
	        mbr.getString(mbr.I_SVC_MGR_MON_OP_GET_SERVICES),
		null , 
		ObjectName[].class.getName(),
		MBeanOperationInfo.INFO)
		    };
	
    private static String[] svcNotificationTypes = {
		    ServiceNotification.SERVICE_PAUSE,
		    ServiceNotification.SERVICE_RESUME
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    svcNotificationTypes,
		    ServiceNotification.class.getName(),
		    mbr.getString(mbr.I_SVC_NOTIFICATIONS)
		    )
		};


    public ServiceManagerMonitor()  {
        super();
    }

    public Long getMsgBytesIn()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.messageBytesIn));
    }

    public Long getMsgBytesOut()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.messageBytesOut));
    }

    public Integer getNumActiveThreads()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Integer(mc.threadsActive));
    }

    public Long getNumMsgsIn()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.messagesIn));
    }

    public Long getNumMsgsOut()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.messagesOut));
    }

    public Long getNumPktsIn()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.packetsIn));
    }

    public Long getNumPktsOut()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.packetsOut));
    }

    public Integer getNumServices()  {
	List l = ServiceUtil.getVisibleServiceNames();

        return (new Integer(l.size()));
    }

    public Long getPktBytesIn()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.packetBytesIn));
    }

    public Long getPktBytesOut()  {
	MetricCounters mc = getMetricsForAllServices();
	return (new Long(mc.packetBytesOut));
    }


    public ObjectName[] getServices() throws MBeanException  {
	List l = ServiceUtil.getVisibleServiceNames();

	if (l.size() == 0)  {
	    return (null);
	}

	ObjectName oNames[] = new ObjectName [ l.size() ];

        Iterator iter = l.iterator();

	int i = 0;
        while (iter.hasNext()) {
            String service = (String)iter.next();

	    try  {
	        ObjectName o = MQObjectName.createServiceMonitor(service);

	        oNames[i++] = o;
	    } catch (Exception e)  {
		handleOperationException(ServiceOperations.GET_SERVICES, e);
	    }
        }

	return (oNames);
    }

    public String getMBeanName()  {
	return ("ServiceManagerMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_SVC_MGR_MON_DESC));
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

    public void notifyServicePause(String name)  {
	ServiceNotification sn;
	sn = new ServiceNotification(ServiceNotification.SERVICE_PAUSE, 
			this, sequenceNumber++);
	sn.setServiceName(name);

	sendNotification(sn);
    }

    public void notifyServiceResume(String name)  {
	ServiceNotification sn;
	sn = new ServiceNotification(ServiceNotification.SERVICE_RESUME, 
			this, sequenceNumber++);
	sn.setServiceName(name);

	sendNotification(sn);
    }

    private MetricCounters getMetricsForAllServices()  {
	MetricManager mm = Globals.getMetricManager();
	MetricCounters mc = null;
	mc = mm.getMetricCounters(null);

	return (mc);
    }
}
