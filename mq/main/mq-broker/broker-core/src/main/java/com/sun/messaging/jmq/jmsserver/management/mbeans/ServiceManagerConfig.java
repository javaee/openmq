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
 * @(#)ServiceManagerConfig.java	1.13 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.MetricCounters;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.MetricManager;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.management.util.ServiceUtil;

public class ServiceManagerConfig extends MQMBeanReadWrite  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ServiceAttributes.MAX_THREADS,
					Integer.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_MAX_THREADS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ServiceAttributes.MIN_THREADS,
					Integer.class.getName(),
	                                mbr.getString(mbr.I_SVC_MGR_ATTR_MIN_THREADS),
					true,
					false,
					false),
			};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ServiceOperations.GET_SERVICES,
	        mbr.getString(mbr.I_SVC_MGR_CFG_OP_GET_SERVICES),
		    null, 
		    ObjectName[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ServiceOperations.PAUSE,
	        mbr.getString(mbr.I_SVC_MGR_OP_PAUSE),
		    null, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),

	    new MBeanOperationInfo(ServiceOperations.RESUME,
	        mbr.getString(mbr.I_SVC_MGR_OP_RESUME),
		    null, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION)
		};

    public ServiceManagerConfig()  {
	super();
    }

    public Integer getMaxThreads()  {
	MetricManager mm = Globals.getMetricManager();
	MetricCounters mc = mm.getMetricCounters(null);

	return (new Integer (mc.threadsHighWater));
    }

    public Integer getMinThreads()  {
	MetricManager mm = Globals.getMetricManager();
	MetricCounters mc = mm.getMetricCounters(null);

	return (new Integer (mc.threadsLowWater));
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
	        ObjectName o = MQObjectName.createServiceConfig(service);

	        oNames[i++] = o;
	    } catch (Exception e)  {
		handleOperationException(ServiceOperations.GET_SERVICES, e);
	    }
        }

	return (oNames);
    }

    public void pause() throws MBeanException  {
	try  {
	    logger.log(Logger.INFO, "Pausing all services");
	    ServiceUtil.pauseService(null);
	} catch(BrokerException e)  {
	    handleOperationException(ServiceOperations.PAUSE, e);
	}
    }

    public void resume() throws MBeanException  {
	try  {
	    logger.log(Logger.INFO, "Resuming all services");
	    ServiceUtil.resumeService(null);
	} catch(BrokerException e)  {
	    handleOperationException(ServiceOperations.RESUME, e);
	}
    }

    public String getMBeanName()  {
	return ("ServiceManagerConfig");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_SVC_MGR_CFG_DESC));
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
