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

package com.sun.messaging.jmq.jmsserver.cluster.api;

import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 */
public class NoClusteredBroker implements ClusteredBroker 
{

    private Logger logger = Globals.getLogger();   
    //private BrokerResources br = Globals.getBrokerResources();

        /**
         * Name associated with this broker. For non-ha clusters
         * it is of the form broker# and is not the same across
         * all brokers in the cluster (although it is unique on
         * this broker).
         */
        String brokerName = null;

        /**
         * The portmapper for this broker.
         */
        MQAddress address = null;

        /**
         * The instance name of this broker
         */
        transient String instanceName = null;

        /**
         * Current status of the broker.
         */
        Integer status = Integer.valueOf(BrokerStatus.BROKER_UNKNOWN);

        /**
         * Current state of the broker.
         */
        BrokerState state = BrokerState.INITIALIZING;

        /**
         * Broker SessionUID for this broker.
         * This uid changes on each restart of the broker.
         */
         UID brokerSessionUID = null;

         /**
          * has brokerID been generated
          */
         boolean isgen = false;

        /** 
         * Create a instace of ClusteredBroker.
         *
         * @param url the portampper address of this broker
         * @param local is this broker local
         */
        public NoClusteredBroker(MQAddress url, UID id) {
            this.address = url;
            brokerSessionUID = id;
            brokerName = Globals.getBrokerID();
            instanceName = Globals.getConfigName();
            if (brokerName == null) {
                isgen = true;
                brokerName = "broker0";
            }
        }

        public boolean equals(Object o) {
            if (! (o instanceof ClusteredBroker)) { 
                return false;
            }
            return this.getBrokerName().equals(((ClusteredBroker)o).getBrokerName());
        }

        public int hashCode() {
             return this.getBrokerName().hashCode();
        }

        /** 
         * String representation of this broker.
         */
        public String toString() {
             return brokerName + "* (" + address + ")";
        }

        /**
         * a unique identifier assigned to the broker
         * (randomly assigned).<P>
         *
         * This name is only unique to this broker. The
         * broker at this URL may be assigned a different name
         * on another broker in the cluster.
         *
         * @return the name of the broker
         */
        public String getBrokerName()
        {
             return brokerName;
        }
    
        /**
         * the URL to the portmapper of this broker.
         * @return the URL of this broker
         */
        public MQAddress getBrokerURL()
        {
             return address;
        }

        /**
         * @return the instance name of this broker, null if not available
         */
        public String getInstanceName() {
            return instanceName;
        }

        /**
         * @param Set the instance name of this broker, can be null
         */
        public void setInstanceName(String instName) {
             instanceName = instName;
        }
 
        /**
         * sets the URL to the portmapper of this broker.
         * @param address the URL of this broker
         * @throws UnsupportedOperationException if this change
         *         can not be made on this broker
         */
        public void setBrokerURL(MQAddress address) throws Exception {
             this.address = address;
        }

    
        /**
         */
        public boolean isLocalBroker() {
            return true;
        }
    
        /**
         * gets the status of the broker.
         *
         * @see BrokerStatus
         * @return the status of the broker
         */
        public synchronized int getStatus() {
            return status.intValue();
        } 
    
        /**
         * gets the protocol version of the broker .
         * @return the current cluster protocol version (if known)
         *        or 0 if not known
         */
        public synchronized int getVersion() {
            return 0;
        }  
    
        /**
         */
        public void setVersion(int version) throws Exception {
        }

        /**
         * sets the status of the broker (and notifies listeners).
         *
         * @param status the status to set
         * @param userData optional user data associated with the status change
         * @see ConfigListener
         */
        public void setStatus(int newstatus, Object userData) {

            // ok - for standalone case, adjust so that LINK_DOWN=DOWN
            if (BrokerStatus.getBrokerIsDown(newstatus))
                newstatus = BrokerStatus.setBrokerLinkIsDown(newstatus);
            else if (BrokerStatus.getBrokerLinkIsDown(newstatus))
                newstatus = BrokerStatus.setBrokerIsDown(newstatus);
            else if (BrokerStatus.getBrokerLinkIsUp(newstatus))
                newstatus = BrokerStatus.setBrokerIsUp(newstatus);
            else if (BrokerStatus.getBrokerIsUp(newstatus))
                newstatus = BrokerStatus.setBrokerLinkIsUp(newstatus);

            synchronized(this) {
                this.status = Integer.valueOf(newstatus);
            }
            try {
                if (BrokerStatus.getBrokerIsUp(newstatus))
                    setState(BrokerState.OPERATING);
                if (BrokerStatus.getBrokerIsDown(newstatus))
                    setState(BrokerState.SHUTDOWN_COMPLETE);
            } catch (Exception ex) {
                logger.logStack(Logger.DEBUG, "Error setting state ", ex);
            }

        }

        /**
         * Updates the BROKER_UP bit flag on status.
         * 
         * @param userData optional user data associated with the status change
         * @param up setting for the bit flag (true/false)
         */
        public void setBrokerIsUp(boolean up, UID brokerSession, Object userData) {
            synchronized (this) {
                if (up) {
                    int newStatus = BrokerStatus.setBrokerIsUp
                                        (this.status.intValue());
                    status = Integer.valueOf(newStatus);
                } else {
                    int newStatus = BrokerStatus.setBrokerIsDown
                                        (this.status.intValue());
                    status = Integer.valueOf(newStatus);
                }
            }
            try {
                if (up) {
                    setState(BrokerState.OPERATING);
                } else {
                    setState(BrokerState.SHUTDOWN_COMPLETE);
                }
            } catch (Exception ex) {
                logger.logStack(Logger.DEBUG, "Error setting state ", ex);
            }
        }

        /**
         * Updates the BROKER_LINK_UP bit flag on status.
         * 
         * @param userData optional user data associated with the status change
         * @param up setting for the bit flag (true/false)
         */
        public void setBrokerLinkUp(boolean up, Object userData) {
            synchronized (this) {
                int newStatus = 0;
                if (up) {
                   newStatus = BrokerStatus.setBrokerLinkIsUp
                        (BrokerStatus.setBrokerIsUp(this.status.intValue()));
                } else {
                   newStatus = BrokerStatus.setBrokerLinkIsDown
                        (BrokerStatus.setBrokerIsDown(this.status.intValue()));
                }
                this.status = Integer.valueOf(newStatus);
            }
            try {
                if (up)
                    setState(BrokerState.OPERATING);
                else
                    setState(BrokerState.SHUTDOWN_COMPLETE);
            } catch (Exception ex) {
                logger.logStack(Logger.DEBUG, "Error setting state ", ex);
            }

        }


        /**
         * Updates the BROKER_INDOUBT bit flag on status.
         * 
         * @param userData optional user data associated with the status change
         * @param up setting for the bit flag (true/false)
         */
        public void setBrokerInDoubt(boolean up, Object userData) {
            throw new UnsupportedOperationException(
	    "Unexpected call: "+getClass().getName()+".setBrokerInDoubt()");
        }

        /**
         * marks this broker as destroyed. This is equivalent to setting
         * the status of the broker to DOWN.
         *
         * @see BrokerStatus#DOWN
         */
        public void destroy() {
            synchronized (this) {
                status = Integer.valueOf(BrokerStatus.setBrokerIsDown(
                              status.intValue()));
            }
        }

        /**
         * gets the state of the broker .
         *
         * @throws BrokerException if the state can not be retrieve
         * @return the current state
         */
        public BrokerState getState() {
            return state;
        }

        /**
         * sets the state of the broker  (and notifies any listeners).
         * @throws IllegalAccessException if the broker does not have
         *               permission to change the broker (e.g. one broker
         *               is updating anothers state).
         * @throws IllegalStateException if the broker state changed
         *               unexpectedly.
         * @throws IllegalArgumentException if the state is not supported
         *               for this cluster type.
         * @param state the state to set for this broker
         * @see ConfigListener
         */
        public void setState(BrokerState state)
             throws IllegalAccessException, IllegalStateException,
                IllegalArgumentException {
            this.state = state;
        }

        /**
         * Is the broker static or dynmically configured
         */
        public boolean isConfigBroker() {
            return true;
        }

        public synchronized UID getBrokerSessionUID() {
            return brokerSessionUID;
        }

        public synchronized void setBrokerSessionUID(UID session) {
            brokerSessionUID = session;
        }

        public boolean isBrokerIDGenerated() {
            return isgen;
        }

        public String getNodeName() throws BrokerException {
            throw new UnsupportedOperationException(
            "Unexpected call: "+getClass().getName()+".getNodeName()");
        }
}
