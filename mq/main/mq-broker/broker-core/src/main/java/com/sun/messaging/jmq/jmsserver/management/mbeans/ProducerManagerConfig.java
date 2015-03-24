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
 * @(#)ProducerManagerConfig.java	1.15 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
import java.util.HashSet;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.Producer;

public class ProducerManagerConfig extends MQMBeanReadWrite  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(ProducerAttributes.NUM_PRODUCERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_PRD_MGR_ATTR_NUM_PRODUCERS),
					true,
					false,
					false)
			};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(ProducerOperations.GET_PRODUCER_IDS,
		mbr.getString(mbr.I_PRD_MGR_OP_GET_PRODUCER_IDS),
		    null, 
		    String[].class.getName(),
		    MBeanOperationInfo.INFO)
		};

    public ProducerManagerConfig()  {
	super();
    }

    public Integer getNumProducers()  {
        return (Integer.valueOf(Producer.getNumProducers()));
    }

    public String[] getProducerIDs() throws MBeanException  {
	int numProducers = getNumProducers().intValue();
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
	    String id;

	    try  {
	        id = Long.toString(prodID);

	        ids[i] = id;
	    } catch (Exception ex)  {
		handleOperationException(ProducerOperations.GET_PRODUCER_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public String getMBeanName()  {
	return ("ProducerManagerConfig");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_PRD_MGR_CFG_DESC));
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
