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
 * @(#)HAClusteredBroker.java	1.13 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api.ha;

import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.persist.api.TakeoverStoreInfo;
import com.sun.messaging.jmq.jmsserver.cluster.api.*;



/**
 * Subclass of ClusteredBroker which contains HA specific information.
 */
public interface HAClusteredBroker extends ClusteredBroker
{
    /**
     * The brokerid assigned to the broker. <P>
     *
     * The name is unique to the cluster (and overrides
     * the superclass implementation).
     *
     * @return the name of the broker
     */
    public String getBrokerName(); 

    /**
     * Gets the UID associated with the store session.
     *
     * @return the store session uid (if known)
     */
    public UID getStoreSessionUID();

    /**
     * Retrieves the id of the broker who has taken over this broker's store.
     *
     * @return the broker id of the takeover broker (or null if there is not
     *      a takeover broker).
     */
    public String getTakeoverBroker()
            throws BrokerException;

    /**
     * Returns the heartbeat timestamp associated with this broker.
     *
     * @return the heartbeat in milliseconds
     * @throws BrokerException if the heartbeat can not be retrieve.
     */
    public long getHeartbeat()
            throws BrokerException;
 

    /**
     * Update the timestamp associated with this broker.
     * @return the updated heartbeat in milliseconds
     * @throws BrokerException if the heartbeat can not be set or retrieve.
     */
    public long updateHeartbeat() throws BrokerException;

    /**
     * Update the timestamp associated with this broker.
     *
     * @param reset update heartbeat without check state
     * @return the updated heartbeat in milliseconds
     * @throws BrokerException if the heartbeat can not be set or retrieve.
     */
    public long updateHeartbeat(boolean reset) throws BrokerException;

    /**
     * Attempt to take over the persistent state of the broker.
     * 
     * @param force force the takeover
     * @param tracker for tracking takingover stages
     * @throws IllegalStateException if this broker can not takeover.
     * @return data associated with previous broker
     */
    public TakeoverStoreInfo takeover(boolean force, Object extraInfo,
                                     TakingoverTracker tracker)
                                     throws BrokerException;

    /**
     * Remove takeover broker ID and set state to OPERATING
     *
     * @throws Exception if operation fails
     */
    public void resetTakeoverBrokerReadyOperating() throws Exception;

    /**
     * Set another broker's state to FAILOVER_PROCESSED if same store session
     *
     * @param storeSession the store session that the failover processed
     * @throws Exception if operation fails
     */
    public void setStateFailoverProcessed(UID storeSession) throws Exception;

    /**
     * Set another broker's state to FAILOVER_FAILED if same broker session
     *
     * @param brokerSession the broker session that the failover failed
     * @throws Exception if operation fails
     */
    public void setStateFailoverFailed(UID brokerSession) throws Exception;
  
}
