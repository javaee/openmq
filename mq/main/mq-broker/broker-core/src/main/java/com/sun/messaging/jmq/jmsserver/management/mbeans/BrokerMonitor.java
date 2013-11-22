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
 * @(#)BrokerMonitor.java	1.24 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Iterator;
import java.util.Properties;
import java.lang.management.MemoryUsage;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.Version;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.config.ConfigListener;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;
import com.sun.messaging.jmq.jmsserver.cluster.api.*;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.jmsserver.core.BrokerMQAddress;
import com.sun.messaging.jmq.jmsserver.management.util.ClusterUtil;
import com.sun.messaging.jmq.jmsserver.management.util.MQAddressUtil;

public class BrokerMonitor extends MQMBeanReadOnly implements ConfigListener {
    private Properties brokerProps = null;

    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(BrokerAttributes.BROKER_ID,
					String.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_BKR_ID),
					true,
					false,
					false),

	    new MBeanAttributeInfo(BrokerAttributes.EMBEDDED,
					Boolean.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_EMBEDDED),
					true,
					false,
					true),

	    new MBeanAttributeInfo(BrokerAttributes.INSTANCE_NAME,
					String.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_INSTANCE_NAME),
					true,
					false,
					false),

	    new MBeanAttributeInfo(BrokerAttributes.RESOURCE_STATE,
					String.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_RESOURCE_STATE),
					true,
					false,
					false),

	    new MBeanAttributeInfo(BrokerAttributes.PORT,
					Integer.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_PORT),
					true,
					false,
					false),

	    new MBeanAttributeInfo(BrokerAttributes.HOST,
					String.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_HOST),
					true,
					false,
					false),

	    new MBeanAttributeInfo(BrokerAttributes.VERSION,
					String.class.getName(),
					mbr.getString(mbr.I_BKR_ATTR_VERSION),
					true,
					false,
					false)
			};

    private static String[] brokerNotificationTypes = {
		    BrokerNotification.BROKER_RESOURCE_STATE_CHANGE,
		    BrokerNotification.BROKER_QUIESCE_COMPLETE,
		    BrokerNotification.BROKER_QUIESCE_START,
		    BrokerNotification.BROKER_SHUTDOWN_START,
		    BrokerNotification.BROKER_TAKEOVER_COMPLETE,
		    BrokerNotification.BROKER_TAKEOVER_FAIL,
		    BrokerNotification.BROKER_TAKEOVER_START
		};

    private static String[] clusterNotificationTypes = {
		    ClusterNotification.CLUSTER_BROKER_JOIN
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    brokerNotificationTypes,
		    BrokerNotification.class.getName(),
		    mbr.getString(mbr.I_BKR_NOTIFICATIONS)
		    ),

	    new MBeanNotificationInfo(
		    clusterNotificationTypes,
		    ClusterNotification.class.getName(),
		    mbr.getString(mbr.I_CLS_NOTIFICATIONS)
		    )
		};


    public BrokerMonitor()  {
	super();
	initProps();

	BrokerConfig cfg = Globals.getConfig();
	cfg.addListener("imq.instancename", this);
	cfg.addListener("imq.portmapper.port", this);
	cfg.addListener("imq.product.version", this);
	cfg.addListener("imq.system.max_count", this);
    }

    public String getBrokerID()  {
        return (Globals.getBrokerID());
    }

    public Boolean getEmbedded() {
	return (Boolean.valueOf(Broker.isInProcess()));
    }

    public Boolean isEmbedded() {
	return (getEmbedded());
    }

    public String getInstanceName()  {
	return (brokerProps.getProperty("imq.instancename"));
    }

    public Integer getPort() throws MBeanException  {
	String s = brokerProps.getProperty("imq.portmapper.port");
	Integer i = null;

	try  {
	    i = new Integer(s);
	} catch (Exception e)  {
	    handleGetterException(BrokerAttributes.PORT, e);
	}

	return (i);
    }

    public String getHost()  {
	return (Globals.getBrokerHostName());
    }

    public String getResourceState()  {
	return (Globals.getMemManager().getCurrentLevelName());
    }

    public MQAddress getMQAddress()  {
	MQAddress addr = null;

	try  {
	    addr = MQAddressUtil.getPortMapperMQAddress(getPort());
	} catch (Exception e)  {
	}

	return (addr);
    }


    public String getVersion()  {
	return (brokerProps.getProperty("imq.product.version"));
    }

    public String getMBeanName()  {
	return("BrokerMonitor");
    }

    public String getMBeanDescription()  {
	return(mbr.getString(mbr.I_BKR_MON_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (null);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (notifs);
    }

    public void validate(String name, String value)
            throws PropertyUpdateException {
    }
            
    public boolean update(String name, String value) {
	/*
        System.err.println("### cl.update called: "
            + name
            + "="
            + value);
	*/
        initProps();
        return true;
    }

    public void notifyResourceStateChange(String oldResourceState, String newResourceState, MemoryUsage heapMemoryUsage)  {
	BrokerNotification n = new BrokerNotification(BrokerNotification.BROKER_RESOURCE_STATE_CHANGE, 
						this, sequenceNumber++);
	n.setOldResourceState(oldResourceState);
	n.setNewResourceState(newResourceState);
	n.setHeapMemoryUsage(heapMemoryUsage);
	sendNotification(n);
    }

    public void notifyQuiesceStart()  {
	sendNotification(
	    new BrokerNotification(BrokerNotification.BROKER_QUIESCE_START, this, sequenceNumber++));
    }

    public void notifyQuiesceComplete()  {
	sendNotification(
	    new BrokerNotification(BrokerNotification.BROKER_QUIESCE_COMPLETE, this, sequenceNumber++));
    }

    public void notifyShutdownStart()  {
	sendNotification(
	    new BrokerNotification(BrokerNotification.BROKER_SHUTDOWN_START, this, sequenceNumber++));
    }

    public void notifyTakeoverStart(String brokerID)  {
	BrokerNotification n;
	CompositeData cd;

	n = new BrokerNotification(BrokerNotification.BROKER_TAKEOVER_START, 
						this, sequenceNumber++);
	n.setFailedBrokerID(brokerID);

	cd = getLocalBrokerInfo();

	if (cd != null)  {
	    /*
	     * This notification will be sent only by brokers in a HA Cluster
	     */
	    n.setBrokerAddress((String)cd.get(BrokerClusterInfo.ADDRESS));
	    n.setBrokerID((String)cd.get(BrokerClusterInfo.ID));
	}

	sendNotification(n);
    }

    public void notifyTakeoverComplete(String brokerID)  {
	BrokerNotification n;
	CompositeData cd;

	n = new BrokerNotification(BrokerNotification.BROKER_TAKEOVER_COMPLETE, 
						this, sequenceNumber++);

	n.setFailedBrokerID(brokerID);

	cd = getLocalBrokerInfo();

	if (cd != null)  {
	    /*
	     * This notification will be sent only by brokers in a HA Cluster
	     */
	    n.setBrokerAddress((String)cd.get(BrokerClusterInfo.ADDRESS));
	    n.setBrokerID((String)cd.get(BrokerClusterInfo.ID));
	}

	sendNotification(n);
    }

    public void notifyTakeoverFail(String brokerID)  {
	BrokerNotification n;
	CompositeData cd;

	n = new BrokerNotification(BrokerNotification.BROKER_TAKEOVER_FAIL, 
						this, sequenceNumber++);

	n.setFailedBrokerID(brokerID);

	cd = getLocalBrokerInfo();

	if (cd != null)  {
	    /*
	     * This notification will be sent only by brokers in a HA Cluster
	     */
	    n.setBrokerAddress((String)cd.get(BrokerClusterInfo.ADDRESS));
	    n.setBrokerID((String)cd.get(BrokerClusterInfo.ID));
	}

	sendNotification(n);
    }

    public void notifyClusterBrokerJoin(String brokerID)  {
	ClusterNotification n;
	n = new ClusterNotification(ClusterNotification.CLUSTER_BROKER_JOIN, 
			this, sequenceNumber++);

	n.setBrokerID(brokerID);
	n.setBrokerAddress(ClusterUtil.getBrokerAddress(brokerID));

	n.setClusterID(Globals.getClusterID());
	n.setHighlyAvailable(Globals.getHAEnabled());

	boolean isMaster = false;
	if (n.isHighlyAvailable())  {
	    isMaster = false;
	} else  {
	    /*
	     * FIXME: Need to determine if broker is master broker or not
	     */
	    isMaster = false;
	}
	n.setMasterBroker(isMaster);

	sendNotification(n);
    }

    private CompositeData getLocalBrokerInfo()  {
        ClusterManager cm = Globals.getClusterManager();
        CompositeData cd = null;

        if (cm == null)  {
            return (null);
        }

	MQAddress address = cm.getMQAddress();

        String id = null;

        try  {
            id = cm.lookupBrokerID(BrokerMQAddress.createAddress(address.toString()));
        } catch (Exception e)  {
            return (null);
        }

        if ((id == null) || (id.equals("")))  {
            return (null);
        }

        try  {
            ClusteredBroker cb = cm.getBroker(id);

	    if (cb == null)  {
		return (null);
	    }

            cd = ClusterUtil.getConfigCompositeData(cb);
        } catch (Exception e)  {
            return (null);
        }

        return(cd);
    }

    private void initProps() {
	brokerProps = Globals.getConfig().toProperties();
	Version version = Globals.getVersion();
	brokerProps.putAll(version.getProps());
    }
}
