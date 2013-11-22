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
 * @(#)BrokerNotification.java	1.8 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

import javax.management.Notification;
import java.lang.management.MemoryUsage;

/**
 * Class containing information on broker related notifications.
 * This notification is broadcasted from the relevant MBeans in a broker 
 * that is either:
 * <UL>
 * <LI>in the process of quiescing
 * <LI>in the process of shutting down
 * <LI>in the process of taking over another broker's persistence store
 * </UL>
 * 
 * With regards to the takeover related notifications, this notification is 
 * broadcasted by the broker that is performing the takeover operation, not the 
 * broker that is being taken over.
 */
public class BrokerNotification extends MQNotification  {
    /** 
     * A broker's memory level/state has changed
     */
    public static final String		BROKER_RESOURCE_STATE_CHANGE = MQNotification.PREFIX
						+ "broker.resource.state.change";

    /** 
     * A broker has finished quiescing.
     */
    public static final String		BROKER_QUIESCE_COMPLETE = MQNotification.PREFIX
						+ "broker.quiesce.complete";

    /** 
     * A broker has started to quiesce.
     */
    public static final String		BROKER_QUIESCE_START = MQNotification.PREFIX
						+ "broker.quiesce.start";

    /** 
     * A broker has started the process of shutting down.
     */
    public static final String		BROKER_SHUTDOWN_START = MQNotification.PREFIX
						+ "broker.shutdown.start";

    /** 
     * A broker has completed the takeover of another broker.
     */
    public static final String		BROKER_TAKEOVER_COMPLETE = MQNotification.PREFIX
						+ "broker.takeover.complete";

    /** 
     * A broker has failed in the attempt to takeover another broker.
     */
    public static final String		BROKER_TAKEOVER_FAIL = MQNotification.PREFIX
						+ "broker.takeover.fail";

    /** 
     * A broker has started to takeover another broker.
     */
    public static final String		BROKER_TAKEOVER_START = MQNotification.PREFIX
						+ "broker.takeover.start";

    private String brokerID, brokerAddress, failedBrokerID, oldResourceState, newResourceState;
    private MemoryUsage heapMemoryUsage;

    /**
     * Creates a BrokerNotification object.
     *
     * @param type		The notification type.
     * @param source		The notification source.
     * @param sequenceNumber	The notification sequence number within the source object.
     */
    public BrokerNotification(String type, Object source, long sequenceNumber) {
	super(type, source, sequenceNumber);
    }

    /**
     * Sets the broker ID. Depending on the type of notification, this can be
     * the ID of the broker that is quiescing, shutting down, or the ID of the
     * broker that is taking over another broker's persistence store.
     *
     * @param brokerID	The broker ID.
     */
    public void setBrokerID(String brokerID)  {
	this.brokerID = brokerID;
    }

    /**
     * Returns the broker ID. Depending on the type of notification, this can be
     * the ID of the broker that is quiescing, shutting down, or the ID of the
     * broker that is taking over another broker's persistence store.
     *
     * @return The broker ID.
     */
    public String getBrokerID()  {
	return(brokerID);
    }

    /**
     * Sets the broker address. Depending on the type of notification, this can be
     * the address of the broker that is quiescing, shutting down, or the address 
     * of the broker that is taking over another broker's persistence store.
     *
     * @param brokerAddress	The broker address.
     */
    public void setBrokerAddress(String brokerAddress)  {
	this.brokerAddress = brokerAddress;
    }

    /**
     * Returns the broker address. Depending on the type of notification, this 
     * can be the address of the broker that is quiescing, shutting down, or the 
     * address of the broker that is taking over another broker's persistence store.
     *
     * @return The broker address.
     */
    public String getBrokerAddress()  {
	return(brokerAddress);
    }

    /**
     * Sets the ID of the broker in the cluster that failed and is in the
     * process of being taken over.
     *
     * @param failedBrokerID	Sets the ID of the broker in the cluster 
     *				that failed and is in the process of being 
     *				taken over.
     */
    public void setFailedBrokerID(String failedBrokerID)  {
	this.failedBrokerID = failedBrokerID;
    }

    /**
     * Returns the ID of the broker in the cluster that failed and is in the
     * process of being taken over.
     *
     * @return	Sets the ID of the broker in the cluster 
     *		that failed and is in the process of being 
     *		taken over.
     */
    public String getFailedBrokerID()  {
	return(failedBrokerID);
    }

    public void setOldResourceState(String oldResourceState)  {
	this.oldResourceState = oldResourceState;
    }

    public String getOldResourceState()  {
	return(oldResourceState);
    }

    public void setNewResourceState(String newResourceState)  {
	this.newResourceState = newResourceState;
    }

    public String getNewResourceState()  {
	return(newResourceState);
    }

    public void setHeapMemoryUsage(MemoryUsage heapMemoryUsage)  {
	this.heapMemoryUsage = heapMemoryUsage;
    }

    public MemoryUsage getHeapMemoryUsage()  {
	return(heapMemoryUsage);
    }
}
