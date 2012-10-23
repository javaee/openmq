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
 * @(#)DestinationMonitor.java	1.25 06/28/07
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

import com.sun.messaging.jmq.util.DestMetricsCounters;
import com.sun.messaging.jmq.jmsserver.management.util.DestinationUtil;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.plugin.spi.ProducerSpi;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.admin.DestinationInfo;

import com.sun.messaging.jms.management.server.*;

public class DestinationMonitor extends MQMBeanReadOnly  {
    private Destination d = null;

    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(DestinationAttributes.AVG_NUM_ACTIVE_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_AVG_NUM_ACTIVE_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.AVG_NUM_BACKUP_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_AVG_NUM_BACKUP_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.AVG_NUM_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_AVG_NUM_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.AVG_NUM_MSGS,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_AVG_NUM_MSGS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.AVG_TOTAL_MSG_BYTES,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_AVG_TOTAL_MSG_BYTES),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.CONNECTION_ID,
					String.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_CONNECTION_ID),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.CREATED_BY_ADMIN,
					Boolean.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_CREATED_BY_ADMIN),
					true,
					false,
					true),

	    new MBeanAttributeInfo(DestinationAttributes.DISK_RESERVED,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_DISK_RESERVED),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.DISK_USED,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_DISK_USED),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.DISK_UTILIZATION_RATIO,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_DISK_UTILIZATION_RATIO),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.MSG_BYTES_IN,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_MSG_BYTES_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.MSG_BYTES_OUT,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_MSG_BYTES_OUT),
					true,
					false,
					false),
        
	    new MBeanAttributeInfo(DestinationAttributes.NAME,
					String.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NAME),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_ACTIVE_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_ACTIVE_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_BACKUP_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_BACKUP_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_WILDCARDS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_WILDCARDS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_WILDCARD_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_WILDCARD_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_WILDCARD_PRODUCERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_WILDCARD_PRODUCERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_REMOTE,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_REMOTE),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_HELD_IN_TRANSACTION,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_HELD_IN_TRANSACTION),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_IN,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_IN),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_OUT,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_OUT),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_PENDING_ACKS,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_PENDING_ACKS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_MSGS_IN_DELAY_DELIVERY,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_MSGS_IN_DELAY_DELIVERY),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NUM_PRODUCERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NUM_PRODUCERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_MSG_BYTES,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_MSG_BYTES),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_NUM_ACTIVE_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_NUM_ACTIVE_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_NUM_BACKUP_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_NUM_BACKUP_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_NUM_CONSUMERS,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_NUM_CONSUMERS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_NUM_MSGS,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_NUM_MSGS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.PEAK_TOTAL_MSG_BYTES,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_PEAK_TOTAL_MSG_BYTES),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.NEXT_MESSAGE_ID,
					String.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_NEXT_MESSAGE_ID),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.STATE,
					Integer.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_STATE),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.STATE_LABEL,
					String.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_STATE_LABEL),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.TEMPORARY,
					Boolean.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_TEMPORARY),
					true,
					false,
					true),

	    new MBeanAttributeInfo(DestinationAttributes.TOTAL_MSG_BYTES,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_TOTAL_MSG_BYTES),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.TOTAL_MSG_BYTES_REMOTE,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_TOTAL_MSG_BYTES_REMOTE),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.TOTAL_MSG_BYTES_HELD_IN_TRANSACTION,
					Long.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_TOTAL_MSG_BYTES_HELD_IN_TRANSACTION),
					true,
					false,
					false),

	    new MBeanAttributeInfo(DestinationAttributes.TYPE,
					String.class.getName(),
					mbr.getString(mbr.I_DST_ATTR_TYPE),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] numWildcardConsumersSignature = {
	    new MBeanParameterInfo("wildcard", String.class.getName(), 
		        mbr.getString(mbr.I_BKR_OP_WILDCARD_CONSUMERS_DESC))
    		};

    private static MBeanParameterInfo[] numWildcardProducersSignature = {
	    new MBeanParameterInfo("wildcard", String.class.getName(), 
		        mbr.getString(mbr.I_BKR_OP_WILDCARD_PRODUCERS_DESC))
    		};

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(DestinationOperations.GET_ACTIVE_CONSUMER_IDS,
		mbr.getString(mbr.I_DST_OP_GET_ACTIVE_CONSUMER_IDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_BACKUP_CONSUMER_IDS,
		mbr.getString(mbr.I_DST_OP_GET_BACKUP_CONSUMER_IDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_CONNECTION,
		mbr.getString(mbr.I_DST_OP_GET_CONNECTION),
		null , 
		ObjectName.class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_CONSUMER_IDS,
		mbr.getString(mbr.I_DST_OP_GET_CONSUMER_IDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_PRODUCER_IDS,
		mbr.getString(mbr.I_DST_OP_GET_PRODUCER_IDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_WILDCARDS,
		mbr.getString(mbr.I_DST_OP_GET_WILDCARDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_CONSUMER_WILDCARDS,
		mbr.getString(mbr.I_DST_OP_GET_CONSUMER_WILDCARDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_NUM_WILDCARD_CONSUMERS,
		mbr.getString(mbr.I_DST_OP_GET_NUM_WILDCARD_CONSUMERS),
		numWildcardConsumersSignature , 
		Integer.class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_PRODUCER_WILDCARDS,
		mbr.getString(mbr.I_DST_OP_GET_PRODUCER_WILDCARDS),
		null , 
		String[].class.getName(),
		MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(DestinationOperations.GET_NUM_WILDCARD_PRODUCERS,
		mbr.getString(mbr.I_DST_OP_GET_NUM_WILDCARD_PRODUCERS),
		numWildcardProducersSignature , 
		Integer.class.getName(),
		MBeanOperationInfo.INFO)
		    };
	
    private static String[] dstNotificationTypes = {
		    DestinationNotification.DESTINATION_COMPACT,
		    DestinationNotification.DESTINATION_PAUSE,
		    DestinationNotification.DESTINATION_PURGE,
		    DestinationNotification.DESTINATION_RESUME
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    dstNotificationTypes,
		    DestinationNotification.class.getName(),
		    mbr.getString(mbr.I_DST_NOTIFICATIONS)
		    )
		};

    public DestinationMonitor(Destination dest) {
	d = dest;
    }

    public Integer getAvgNumActiveConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getAvgActiveConsumers()));
    }

    public Integer getAvgNumBackupConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getAvgFailoverConsumers()));
    }

    public Integer getAvgNumConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getAvgActiveConsumers()));
    }

    public Long getAvgNumMsgs()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getAverageMessages()));
    }

    public Long getAvgTotalMsgBytes()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getAverageMessageBytes()));
    }

    public String getConnectionID()  {
	ConnectionUID cxnId;

	if (!isTemporary().booleanValue())  {
	    return (null);
	}

	cxnId = d.getConnectionUID();

	if (cxnId == null)  {
	    return (null);
	}

	return (Long.toString(cxnId.longValue()));
    }

    public Boolean isCreatedByAdmin()  {
	boolean b = !(d.isAutoCreated() || d.isInternal() || d.isDMQ() || d.isAdmin());

	return (Boolean.valueOf(b));
    }

    public Boolean getCreatedByAdmin()  {
	return (isCreatedByAdmin());
    }

    public Long getDiskReserved()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getDiskReserved()));
    }

    public Long getDiskUsed()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getDiskUsed()));
    }

    public Integer getDiskUtilizationRatio()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getDiskUtilizationRatio()));
    }

    public Long getMsgBytesIn()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getMessageBytesIn()));
    }

    public Long getMsgBytesOut()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getMessageBytesOut()));
    }

    public String getName()  {
	return (d.getDestinationName());
    }

    public Integer getNumActiveConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getActiveConsumers()));
    }

    public Integer getNumBackupConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getFailoverConsumers()));
    }

    public Integer getNumConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getNumConsumers()));
    }

    public Integer getNumWildcards() throws MBeanException  {
	int numConsumers = getNumConsumers().intValue();
	int numProducers = getNumProducers().intValue();
	int count = 0;

	if (numConsumers > 0)  {
	    Iterator consumers = d.getConsumers();
	    while (consumers.hasNext()) {
	        Consumer oneCon = (Consumer)consumers.next();

	        if (oneCon.isWildcard())  {
		    ++count;
	        }
	    }
	}

	if (numProducers > 0)  {
	    Iterator producers = d.getProducers();

	    while (producers.hasNext()) {
	        ProducerSpi oneProd = (ProducerSpi)producers.next();
    
	        if (oneProd.isWildcard())  {
		    ++count;
	        }
	    }
	}

	return (new Integer(count));
    }

    public Integer getNumWildcardConsumers() throws MBeanException  {
	/*
	int numConsumers = getNumConsumers().intValue();
	Iterator consumers;

	if (numConsumers <= 0)  {
	    return (new Integer(0));
	}

	consumers = d.getConsumers();

	int count = 0;
	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();

	    if (oneCon.isWildcard())  {
		++count;
	    }
	}

	return (new Integer(count));
	*/

        return(getNumWildcardConsumers(null));
    }

    public Integer getNumWildcardConsumers(String wildcard) throws MBeanException  {
	int numConsumers = getNumConsumers().intValue();

	if (numConsumers <= 0)  {
	    return (new Integer(0));
	}

	Iterator consumers = d.getConsumers();

	if (consumers == null)  {
	    return (new Integer(0));
	}

	int count = 0;
	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();

	    if (oneCon.isWildcard())  {
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
	}

        return (new Integer(count));
    }


    public Integer getNumWildcardProducers() throws MBeanException  {
	/*
	int numProducers = getNumProducers().intValue();
	Iterator producers;

	if (numProducers <= 0)  {
	    return (new Integer(0));
	}

	producers = d.getProducers();

	int count = 0;
	while (producers.hasNext()) {
	    Producer oneProd = (Producer)producers.next();

	    if (oneProd.isWildcard())  {
		++count;
	    }
	}

	return (new Integer(count));
	*/

        return(getNumWildcardProducers(null));
    }

    public Integer getNumWildcardProducers(String wildcard) throws MBeanException  {
	int numProducers = getNumProducers().intValue();

	if (numProducers <= 0)  {
	    return (new Integer(0));
	}

	Iterator producers = d.getProducers();

	if (producers == null)  {
	    return (new Integer(0));
	}

	int count = 0;
	while (producers.hasNext()) {
	    ProducerSpi oneProd = (ProducerSpi)producers.next();

	    if (oneProd.isWildcard())  {
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
	}

        return (new Integer(count));
    }


    public String[] getWildcards() throws MBeanException  {
	ArrayList<String> al = new ArrayList<String>();
	String[] list = null;
	int numConsumers = getNumConsumers().intValue(),
	    numProducers = getNumProducers().intValue();

	if (numConsumers > 0)  {
	    Iterator consumers = d.getConsumers();

	    while (consumers.hasNext()) {
	        Consumer oneCon = (Consumer)consumers.next();
    
	        if (oneCon.isWildcard())  {
		    DestinationUID id = oneCon.getDestinationUID();
	            al.add(id.getName());
	        }
	    }
	}

	if (numProducers > 0)  {
	    Iterator producers = d.getProducers();

	    while (producers.hasNext()) {
	        ProducerSpi oneProd = (ProducerSpi)producers.next();
    
	        if (oneProd.isWildcard())  {
		    DestinationUID id = oneProd.getDestinationUID();
	            al.add(id.getName());
	        }
	    }
	}

	if (al.size() > 0)  {
	    list = new String [ al.size() ];
	    list = (String[])al.toArray(list);
	}

        return (list);
    }

    public String[] getConsumerWildcards() throws MBeanException  {
	ArrayList<String> al = new ArrayList<String>();
	String[] list = null;
	int numConsumers = getNumConsumers().intValue();
	Iterator consumers;

	if (numConsumers <= 0)  {
	    return (null);
	}

	consumers = d.getConsumers();

	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();

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


    public String[] getProducerWildcards() throws MBeanException  {
	ArrayList<String> al = new ArrayList<String>();
	String[] list = null;
	int numProducers = getNumProducers().intValue();
	Iterator producers;

	if (numProducers <= 0)  {
	    return (null);
	}

	producers = d.getProducers();

	while (producers.hasNext()) {
	    ProducerSpi oneProd = (ProducerSpi)producers.next();

	    if (oneProd.isWildcard())  {
		DestinationUID id = oneProd.getDestinationUID();
	        al.add(id.getName());
	    }
	}

	if (al.size() > 0)  {
	    list = new String [ al.size() ];
	    list = (String[])al.toArray(list);
	}

        return (list);
    }



    public Long getNumMsgs()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nMessages - di.nTxnMessages));
    }

    public Long getNumMsgsRemote()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nRemoteMessages));
    }

    public Long getNumMsgsHeldInTransaction()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nTxnMessages));
    }

    public Long getNumMsgsIn()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getMessagesIn()));
    }

    public Long getNumMsgsOut()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getMessagesOut()));
    }

    public Long getNumMsgsPendingAcks()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nUnackMessages));
    }

    public Long getNumMsgsInDelayDelivery()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nInDelayMessages));
    }

    public Integer getNumProducers()  {
	return (new Integer (d.getProducerCount()));
    }

    public Long getPeakMsgBytes()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getHighWaterLargestMsgBytes()));
    }

    public Integer getPeakNumActiveConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getHWActiveConsumers()));
    }

    public Integer getPeakNumBackupConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getHWFailoverConsumers()));
    }

    public Integer getPeakNumConsumers()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Integer(dmc.getHWActiveConsumers()));
    }

    public Long getPeakNumMsgs()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getHighWaterMessages()));
    }

    public Long getPeakTotalMsgBytes()  {
	DestMetricsCounters dmc = d.getMetrics();
	return (new Long(dmc.getHighWaterMessageBytes()));
    }

    public String getNextMessageID() {
        PacketReference ref = d.peekNext();
        if (ref != null) {
            return ref.getSysMessageID().toString();
        } else {
            return "";
       }
    }

    public Integer getState()  {
	return (new Integer(
	    DestinationUtil.toExternalDestState(d.getState())));
    }

    public String getStateLabel()  {
	return (DestinationState.toString(
	    DestinationUtil.toExternalDestState(d.getState())));
    }

    public Boolean isTemporary()  {
	return (Boolean.valueOf(d.isTemporary()));
    }
    public Boolean getTemporary()  {
	return (isTemporary());
    }

    public Long getTotalMsgBytes()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nMessageBytes - di.nTxnMessageBytes));
    }

    public Long getTotalMsgBytesRemote()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nRemoteMessageBytes));
    }

    public Long getTotalMsgBytesHeldInTransaction()  {
	DestinationInfo di = DestinationUtil.getDestinationInfo(d);

	if (di == null)  {
	    return (null);
	}

	return (new Long(di.nTxnMessageBytes));
    }

    public String getType()  {
	return (d.isQueue() ? 
		DestinationType.QUEUE : DestinationType.TOPIC);
    }

    public String[] getActiveConsumerIDs() throws MBeanException  {
	int numConsumers = getNumActiveConsumers().intValue();
	String ids[];
	Iterator consumers;

	if (numConsumers <= 0)  {
	    return (null);
	}

	consumers = d.getActiveConsumers().iterator();

	ids = new String [ numConsumers ];

	int i = 0;
	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();
	    long conID = oneCon.getConsumerUID().longValue();
	    String id;

	    try  {
	        id = Long.toString(conID);

	        ids[i] = id;
	    } catch (Exception ex)  {
	        handleOperationException(DestinationOperations.GET_ACTIVE_CONSUMER_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public String[] getBackupConsumerIDs() throws MBeanException  {
	int numConsumers = getNumBackupConsumers().intValue();
	String ids[];
	Iterator consumers;

	if (numConsumers <= 0)  {
	    return (null);
	}

	consumers = d.getFailoverConsumers().iterator();

	ids = new String [ numConsumers ];

	int i = 0;
	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();
	    long conID = oneCon.getConsumerUID().longValue();
	    String id;

	    try  {
	        id = Long.toString(conID);

	        ids[i] = id;
	    } catch (Exception ex)  {
	        handleOperationException(DestinationOperations.GET_BACKUP_CONSUMER_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public ObjectName getConnection() throws MBeanException  {
	ConnectionUID cxnId;
	ObjectName oName = null;

	if (!isTemporary().booleanValue())  {
	    return (null);
	}

	cxnId = d.getConnectionUID();

	if (cxnId == null)  {
	    return (null);
	}

	try  {
	    oName = MQObjectName.createConnectionMonitor(Long.toString(cxnId.longValue()));
	} catch (Exception e)  {
	    handleOperationException(DestinationOperations.GET_CONNECTION, e);
	}

	return (oName);
    }

    public String[] getConsumerIDs() throws MBeanException  {
	int numConsumers = getNumConsumers().intValue();
	String ids[];
	Iterator consumers;

	if (numConsumers <= 0)  {
	    return (null);
	}

	consumers = d.getConsumers();

	ids = new String [ numConsumers ];

	int i = 0;
	while (consumers.hasNext()) {
	    Consumer oneCon = (Consumer)consumers.next();
	    long conID = oneCon.getConsumerUID().longValue();
	    String id;

	    try  {
	        id = Long.toString(conID);

	        ids[i] = id;
	    } catch (Exception ex)  {
	        handleOperationException(DestinationOperations.GET_CONSUMER_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public String[] getProducerIDs() throws MBeanException  {
	int numProducers = getNumProducers().intValue();
	String ids[];
	Iterator producers;

	if (numProducers <= 0)  {
	    return (null);
	}

	producers = d.getProducers();

	ids = new String [ numProducers ];

	int i = 0;
	while (producers.hasNext()) {
	    ProducerSpi oneProd = (ProducerSpi)producers.next();
	    long prodID = oneProd.getProducerUID().longValue();

	    try  {
	        ids[i] = Long.toString(prodID);
	    } catch (Exception ex)  {
	        handleOperationException(DestinationOperations.GET_PRODUCER_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public String getMBeanName()  {
	return ("DestinationMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_DST_MON_DESC));
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

    public void notifyDestinationCompact()  {
	DestinationNotification n;
	n = new DestinationNotification(DestinationNotification.DESTINATION_COMPACT,
			this, sequenceNumber++);
	n.setDestinationName(getName());
	n.setDestinationType(getType());

	sendNotification(n);
    }

    public void notifyDestinationPause(String pauseType)  {
	DestinationNotification n;
	n = new DestinationNotification(
			DestinationNotification.DESTINATION_PAUSE, 
			this, sequenceNumber++);
	n.setDestinationName(getName());
	n.setDestinationType(getType());
	n.setPauseType(pauseType);

	sendNotification(n);
    }

    public void notifyDestinationPurge()  {
	DestinationNotification n;
	n = new DestinationNotification(DestinationNotification.DESTINATION_PURGE,
			this, sequenceNumber++);
	n.setDestinationName(getName());
	n.setDestinationType(getType());

	sendNotification(n);
    }

    public void notifyDestinationResume()  {
	DestinationNotification n;
	n = new DestinationNotification(DestinationNotification.DESTINATION_RESUME,
			this, sequenceNumber++);
	n.setDestinationName(getName());
	n.setDestinationType(getType());

	sendNotification(n);
    }
}
