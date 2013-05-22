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
 * @(#)ProducerManagerMonitor.java	1.13 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
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
import com.sun.messaging.jmq.jmsserver.core.Producer;
import com.sun.messaging.jmq.jmsserver.core.ProducerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.management.util.ProducerUtil;

public class ProducerManagerMonitor extends MQMBeanReadOnly  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ProducerAttributes.NUM_PRODUCERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_PRD_MGR_ATTR_NUM_PRODUCERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(ProducerAttributes.NUM_WILDCARD_PRODUCERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_PRD_MGR_ATTR_NUM_WILDCARD_PRODUCERS),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] getProducerInfoByIDSignature = {
		    new MBeanParameterInfo("producerID", String.class.getName(), 
			mbr.getString(mbr.I_PRD_MGR_OP_PARAM_PRD_ID))
			    };

    private static MBeanParameterInfo[] numWildcardProducersSignature = {
	    new MBeanParameterInfo("wildcard", String.class.getName(), 
		        mbr.getString(mbr.I_BKR_OP_WILDCARD_PRODUCERS_DESC))
    		};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ProducerOperations.GET_PRODUCER_IDS,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_PRODUCER_IDS),
		    null , 
		    String[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ProducerOperations.GET_PRODUCER_INFO,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_PRODUCER_INFO),
		    null , 
		    CompositeData[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ProducerOperations.GET_PRODUCER_INFO_BY_ID,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_PRODUCER_INFO_BY_ID),
		    getProducerInfoByIDSignature, 
		    CompositeData.class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ProducerOperations.GET_PRODUCER_WILDCARDS,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_PRODUCER_WILDCARDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(ProducerOperations.GET_NUM_WILDCARD_PRODUCERS,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_NUM_WILDCARD_PRODUCERS),
		numWildcardProducersSignature , 
		Integer.class.getName(),
		MBeanOperationInfo.INFO)
		};
	
    public ProducerManagerMonitor()  {
	super();
    }

    public Integer getNumProducers()  {
        return (new Integer(Producer.getNumProducers()));
    }

    public Integer getNumWildcardProducers() throws MBeanException  {
        return(new Integer(Producer.getNumWildcardProducers()));
    }

    public Integer getNumWildcardProducers(String wildcard) throws MBeanException  {
	int numWildcardProducers = Producer.getNumWildcardProducers();

	if (numWildcardProducers <= 0)  {
	    return (new Integer(0));
	}

	Iterator producers = Producer.getWildcardProducers();

	if (producers == null)  {
	    return (new Integer(0));
	}

	int count = 0;
	while (producers.hasNext()) {
	    ProducerUID pid = (ProducerUID)producers.next();
	    Producer oneProd = (Producer)Producer.getProducer(pid);

	    /*
	     * If wildcard param is not null, check for matches
	     * If it is null, return total count of wildcards
	     */
	    if (wildcard != null)  {
	        DestinationUID id = oneProd.getDestinationUID();
	        if (id.getName().equals(wildcard))  {
	            count++;
	        }
	    } else  {
	        count++;
	    }
	}

        return (new Integer(count));
    }

    public String[] getProducerWildcards() throws MBeanException  {
	ArrayList<String> al = new ArrayList<String>();
	String[] list = null;
	int numWildcardProducers = Producer.getNumWildcardProducers();
	Iterator producers;

	if (numWildcardProducers <= 0)  {
	    return (null);
	}

	producers = Producer.getWildcardProducers();

	if (producers == null)  {
	    return (null);
	}

	while (producers.hasNext()) {
	    ProducerUID pid = (ProducerUID)producers.next();
	    Producer oneProd = (Producer)Producer.getProducer(pid);

	    DestinationUID id = oneProd.getDestinationUID();
	    al.add(id.getName());
	}

	if (al.size() > 0)  {
	    list = new String [ al.size() ];
	    list = (String[])al.toArray(list);
	}

        return (list);
    }


    public String[] getProducerIDs() throws MBeanException  {
	return (ProducerUtil.getProducerIDs());
    }

    public CompositeData[] getProducerInfo() throws MBeanException {
	CompositeData cds[] = null;

	try  {
	    cds = ProducerUtil.getProducerInfo();
	} catch(Exception e)  {
	    handleOperationException(ProducerOperations.GET_PRODUCER_INFO, e);
	}

	return (cds);
    }

    public CompositeData getProducerInfoByID(String producerID) throws MBeanException  {
	CompositeData cd = null;

	try  {
	    cd = ProducerUtil.getProducerInfo(producerID);
	} catch(Exception e)  {
	    handleOperationException(ProducerOperations.GET_PRODUCER_INFO_BY_ID, e);
	}

	return (cd);
    }

    public String getMBeanName()  {
	return ("ProducerManagerMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_PRD_MGR_MON_DESC));
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
