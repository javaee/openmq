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
 * @(#)ProducerUtil.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.ProducerUID;
import com.sun.messaging.jmq.jmsserver.core.Producer;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.util.net.IPAddress;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class ProducerUtil {
    /*
     * Producer Info composite type for Monitor MBeans
     */
    private static CompositeType monitorCompType = null;

    public static ConnectionUID getConnectionUID(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);
	ConnectionUID cxnId;

	if (p == null)  {
	    return (null);
	}

	cxnId = p.getConnectionUID();

	return (cxnId);
    }

    public static String[] getProducerIDs()  {
	int numProducers = Producer.getNumProducers();
	String ids[];
	Iterator producers;

	if (numProducers <= 0)  {
	    return (null);
	}

	ids = new String [ numProducers ];

	producers = Producer.getAllProducers();
	int i = 0;
	while (producers.hasNext()) {
	    Producer oneProd = (Producer)producers.next();
	    long prodID = oneProd.getProducerUID().longValue();

	    ids[i] = Long.toString(prodID);
	    i++;
	}

	return (ids);
    }

    public static CompositeData[] getProducerInfo()
				throws BrokerException, OpenDataException  {
	String[] ids = getProducerIDs();

	if (ids == null)  {
	    return (null);
	}

	CompositeData cds[] = new CompositeData [ ids.length ];

	for (int i = 0; i < ids.length; ++i)  {
	    cds[i] = getProducerInfo(ids[i]);
	}
	
	return (cds);
    }

    public static CompositeData getProducerInfo(String producerID) 
				throws BrokerException, OpenDataException  {
	CompositeData cd = null;
	ProducerUID pid = null;
        BrokerResources	rb = Globals.getBrokerResources();

	if (producerID == null)  {
	    throw new 
		IllegalArgumentException(rb.getString(rb.X_JMX_NULL_PRODUCER_ID_SPEC));
	}

	long longPid = 0;

	try  {
	    longPid = Long.parseLong(producerID);
	} catch (NumberFormatException e)  {
	    throw new 
		IllegalArgumentException(rb.getString(rb.X_JMX_INVALID_PRODUCER_ID_SPEC, producerID));
	}

	pid = new ProducerUID(longPid);

	if (pid == null)  {
	    throw new BrokerException(rb.getString(rb.X_JMX_PRODUCER_NOT_FOUND, producerID));
	}

	cd = getProducerInfo(pid);

	return (cd);
    }


    public static String getConnectionID(ProducerUID pid)  {
        ConnectionUID cxnId = getConnectionUID(pid);

	if (cxnId == null)  {
	    return (null);
	}

	return(Long.toString(cxnId.longValue()));
    }

    public static String getDestinationName(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);

	if (p != null)  {
	    DestinationUID did = p.getDestinationUID();

	    if (did == null)  {
		return (null);
	    }

	    return(did.getName());
	}

        return (null);
    }

    private static String[] getDestinationNames(ProducerUID pid)  {
        Producer p = (Producer)Producer.getProducer(pid);
	String[] ret = null;

	if (p == null)  {
	    return (null);
	}

	ArrayList<String> al = new ArrayList<String>();
	Set dests = p.getDestinations();
	Iterator itr = dests.iterator();
	while (itr.hasNext()) {
	    DestinationUID duid = (DestinationUID)itr.next();
	    al.add(duid.getName());
	}

	if (al.size() > 0)  {
	    ret = new String [ al.size() ];
	    ret = (String[])al.toArray(ret);
	}

	return (ret);
    }


    public static String getDestinationType(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);

	if (p != null)  {
	    DestinationUID did = p.getDestinationUID();

	    return(did.isQueue() ? DestinationType.QUEUE : DestinationType.TOPIC);
	}

	return (null);
    }

    public static Boolean getFlowPaused(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);

	if (p == null)  {
	    return (null);
	}

        return (new Boolean(p.isPaused()));
    }

    public static String getHost(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);
	ConnectionUID cxnId = null;

	if (p == null)  {
	    return (null);
	}

	cxnId = p.getConnectionUID();

	if (cxnId == null)  {
	    return (null);
	}

	ConnectionInfo cxnInfo = ConnectionUtil.getConnectionInfo(cxnId.longValue());

	if (cxnInfo == null)  {
	    return (null);
	}

	String host = null;

	if (cxnInfo.remoteIP != null) {
            host = String.valueOf(
		IPAddress.rawIPToString(cxnInfo.remoteIP, true, true));
        }

	return (host);
    }

    public static Long getCreationTime(ProducerUID pid)  {
	long currentTime = System.currentTimeMillis();

	return (new Long(currentTime - pid.age(currentTime)));
    }


    public static Long getNumMsgs(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);

	if (p == null)  {
	    return (null);
	}

        return (new Long(p.getMsgCnt()));
    }

    public static String getServiceName(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);
	ConnectionUID cxnId = null;

	if (p == null)  {
	    return (null);
	}

	cxnId = p.getConnectionUID();

	if (cxnId == null)  {
	    return (null);
	}

	return(ConnectionUtil.getServiceOfConnection(cxnId.longValue()));
    }

    public static String getUser(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);
	ConnectionUID cxnId = null;

	if (p == null)  {
	    return (null);
	}

	cxnId = p.getConnectionUID();

	if (cxnId == null)  {
	    return (null);
	}

	ConnectionInfo cxnInfo = ConnectionUtil.getConnectionInfo(cxnId.longValue());

	return (cxnInfo.user);
    }

    public static boolean isWildcard(ProducerUID pid)  {
	Producer p = (Producer)Producer.getProducer(pid);

	if (p != null)  {
	    return (p.isWildcard());
	}

	return (false);
    }

    private static Boolean getWildcard(ProducerUID pid)  {
        return (new Boolean(isWildcard(pid)));
    }


    private static CompositeData getProducerInfo(ProducerUID pid) 
						throws OpenDataException  {
        /*
         * Producer Info item names for Monitor MBeans
         */
        final String[] producerInfoMonitorItemNames = {
                            ProducerInfo.CONNECTION_ID,
                            ProducerInfo.CREATION_TIME,
                            ProducerInfo.DESTINATION_NAME,
                            ProducerInfo.DESTINATION_NAMES,
                            ProducerInfo.DESTINATION_TYPE,
                            ProducerInfo.FLOW_PAUSED,
                            ProducerInfo.HOST,
                            ProducerInfo.NUM_MSGS,
                            ProducerInfo.PRODUCER_ID,
                            ProducerInfo.SERVICE_NAME,
                            ProducerInfo.USER,
                            ProducerInfo.WILDCARD
                    };

        /*
         * Producer Info item description for Monitor MBeans
         * TBD: use real descriptions
         */
        final String[] producerInfoMonitorItemDesc = producerInfoMonitorItemNames;

        /*
         * Producer Info item types for Monitor MBeans
         */
        final OpenType[] producerInfoMonitorItemTypes = {
			    SimpleType.STRING,		// connection ID
			    SimpleType.LONG,		// creation time
			    SimpleType.STRING,		// dest name
			    new ArrayType(1, 
				SimpleType.STRING),	// dest names
			    SimpleType.STRING,		// dest type
			    SimpleType.BOOLEAN,		// flow paused
			    SimpleType.STRING,		// host
			    SimpleType.LONG,		// num msgs
			    SimpleType.STRING,		// producer ID
			    SimpleType.STRING,		// service name
			    SimpleType.STRING,		// user
			    SimpleType.BOOLEAN		// wildcard
                    };

	Object[] producerInfoMonitorItemValues = {
                            getConnectionID(pid),
                            getCreationTime(pid),
                            getDestinationName(pid),
                            getDestinationNames(pid),
                            getDestinationType(pid),
                            getFlowPaused(pid),
                            getHost(pid),
                            getNumMsgs(pid),
			    Long.toString(pid.longValue()),
                            getServiceName(pid),
                            getUser(pid),
                            getWildcard(pid)
			};
	CompositeData cd = null;

        if (monitorCompType == null)  {
            monitorCompType = new CompositeType("ProducerMonitorInfo", "ProducerMonitorInfo", 
                        producerInfoMonitorItemNames, producerInfoMonitorItemDesc, 
				producerInfoMonitorItemTypes);
        }

	cd = new CompositeDataSupport(monitorCompType, 
			producerInfoMonitorItemNames, producerInfoMonitorItemValues);
	
	return (cd);
    }

}
