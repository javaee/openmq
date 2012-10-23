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
 * @(#)ConsumerManagerMonitor.java	1.14 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;

import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.management.util.ConsumerUtil;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;

public class ConsumerManagerMonitor extends MQMBeanReadOnly {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ConsumerAttributes.NUM_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_CON_MGR_ATTR_NUM_CONSUMERS),
					true,
					false,
					false),
	    new MBeanAttributeInfo(ConsumerAttributes.NUM_WILDCARD_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_CON_MGR_ATTR_NUM_WILDCARD_CONSUMERS),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] getConsumerInfoByIDSignature = {
		    new MBeanParameterInfo("consumerID", String.class.getName(),
			mbr.getString(mbr.I_CON_MGR_OP_PARAM_CON_ID_DESC))
			    };

    private static MBeanParameterInfo[] numWildcardConsumersSignature = {
	    new MBeanParameterInfo("wildcard", String.class.getName(), 
		        mbr.getString(mbr.I_BKR_OP_WILDCARD_CONSUMERS_DESC)) // XXX
    		};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ConsumerOperations.GET_CONSUMER_IDS,
		mbr.getString(mbr.I_CON_MGR_OP_GET_CONSUMER_IDS_DESC),
		    null , 
		    String[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ConsumerOperations.GET_CONSUMER_INFO,
		mbr.getString(mbr.I_CON_MGR_OP_GET_CONSUMER_INFO_DESC),
		    null , 
		    CompositeData[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ConsumerOperations.GET_CONSUMER_INFO_BY_ID,
		mbr.getString(mbr.I_CON_MGR_OP_GET_CONSUMER_INFO_BY_ID_DESC),
		    getConsumerInfoByIDSignature, 
		    CompositeData.class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ConsumerOperations.GET_CONSUMER_WILDCARDS,
		mbr.getString(mbr.I_CON_MGR_OP_GET_CONSUMER_WILDCARDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ConsumerOperations.GET_NUM_WILDCARD_CONSUMERS,
		mbr.getString(mbr.I_CON_MGR_OP_GET_NUM_WILDCARD_CONSUMERS),
		numWildcardConsumersSignature , 
		Integer.class.getName(),
		MBeanOperationInfo.INFO)

		};

    public ConsumerManagerMonitor()  {
	super();
    }

    public Integer getNumConsumers()  {
        return (new Integer(ConsumerUtil.getNumConsumersNoChildren()));
    }

    public Integer getNumWildcardConsumers() throws MBeanException  {
        int n = Consumer.getNumWildcardConsumers();
        return(new Integer(n));
    }

    public Integer getNumWildcardConsumers(String wildcard) throws MBeanException  {
	int numWildcardConsumers = Consumer.getNumWildcardConsumers();

	if (numWildcardConsumers <= 0)  {
	    return (new Integer(0));
	}

	Iterator consumers = Consumer.getWildcardConsumers();

	if (consumers == null)  {
	    return (new Integer(0));
	}

	int count = 0;
	while (consumers.hasNext()) {
	    ConsumerUID cid = (ConsumerUID)consumers.next();
	    Consumer oneCon = Consumer.getConsumer(cid);

	    /*
	     * If wildcard param is not null, check for matches
	     * If it is null, return total count of wildcards
	     */
	    if (wildcard != null)  {
	        DestinationUID id = oneCon.getDestinationUID();
	        if (id.getName().equals(wildcard))  {
	            count++;
	        }
	    } else  {
	        count++;
	    }
	}

        return (new Integer(count));
    }

    public String[] getConsumerWildcards() throws MBeanException  {
	ArrayList<String> al = new ArrayList<String>();
	String[] list = null;
	int numWildcardConsumers = Consumer.getNumWildcardConsumers();
	Iterator consumers;

	if (numWildcardConsumers <= 0)  {
	    return (null);
	}

	consumers = Consumer.getWildcardConsumers();

	if (consumers == null)  {
	    return (null);
	}

	while (consumers.hasNext()) {
	    ConsumerUID cid = (ConsumerUID)consumers.next();
	    Consumer oneCon = Consumer.getConsumer(cid);

	    if (oneCon.isWildcard())  {
		DestinationUID id = oneCon.getDestinationUID();
	        al.add(id.getName());
	    }
	}

	if (al.size() > 0)  {
	    list = new String [ al.size() ];
	    list = (String[])al.toArray(list);
	}

        return (list);
    }


    public String[] getConsumerIDs()  {
	return (ConsumerUtil.getConsumerIDs());
    }

    public CompositeData[] getConsumerInfo() throws MBeanException {
	CompositeData cds[] = null;

	try  {
	    cds = ConsumerUtil.getConsumerInfo();
	} catch(Exception e)  {
	    handleOperationException(ConsumerOperations.GET_CONSUMER_INFO, e);
	}

	return (cds);
    }

    public CompositeData getConsumerInfoByID(String consumerID) throws MBeanException  {
	CompositeData cd = null;

	try  {
	    cd = ConsumerUtil.getConsumerInfo(consumerID);
	} catch(Exception e)  {
	    handleOperationException(ConsumerOperations.GET_CONSUMER_INFO_BY_ID, e);
	}

	return (cd);
    }

    public String getMBeanName()  {
	return ("ConsumerManagerMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_CON_MGR_MON_DESC));
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
