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
 * @(#)MessageBusCallback.java	1.29 07/23/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker;

import java.util.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;

/**
 * Interface for processing messages and acknowledgements coming
 * from the MessageBus.
 */
public interface MessageBusCallback {
    /**
     * Initial sync with the config server is complete.
     * We are now ready to accept connections from clients.
     */
    public void configSyncComplete();

    /**
     * @param consumers contains mapping for each consumer UID
     *        to its delivery count or null if unknown
     */
    public void processRemoteMessage(Packet msg, 
        Map<ConsumerUID, Integer> consumers, 
        BrokerAddress home, boolean sendMsgRedeliver) 
        throws BrokerException;

    /**
     * Process an acknowledgement.
     */
    public void processRemoteAck(SysMessageID sysid, ConsumerUID cuid, 
                                 int ackType, Map optionalProps)
                                 throws BrokerException;

    public void processRemoteAck2P(SysMessageID[] sysids, ConsumerUID[] cuids, 
                                   int ackType, Map optionalProps, Long txnID,
                                   BrokerAddress txnHomeBroker) 
                                   throws BrokerException;

    /**
     * Interest creation notification. This method is called when
     * any remote interest is created.
     */
    public void interestCreated(Consumer intr);

    /**
     * Interest removal notification. This method is called when
     * any remote interest is removed.
     */
    public void interestRemoved(Consumer cuid, 
        Map<TransactionUID, LinkedHashMap<SysMessageID, Integer>> pendingMsgs,
        boolean cleanup);

    /**
     * Durable subscription unsubscribe notification. This method is
     * called when a remote broker unsubscribes a durable interest.
     */
    public void unsubscribe(Subscription sub);


    /**
     * Primary interest change notification. This method is called when
     * a new interest is chosen as primary interest for a failover queue.
     */
    public void activeStateChanged(Consumer intr);

    /**
     * Client down notification. This method is called when a local
     * or remote client connection is closed.
     */
    public void clientDown(ConnectionUID conid);

    /**
     * Broker down notification. This method is called when any broker
     * in this cluster goes down.
     */
    public void brokerDown(BrokerAddress broker);

    /**
     * A new destination was created by the administrator on a remote
     * broker.  This broker should also add the destination if it is
     * not already present.
     */
    public void notifyCreateDestination(Destination d);

    /**
     * A destination was removed by the administrator on a remote
     * broker. This broker should also remove the destination, if it
     * is present.
     */
    public void notifyDestroyDestination(DestinationUID uid);

    /**
     * A destination was updated
     */
    public void notifyUpdateDestination(DestinationUID uid, Map changes);

    /**
     * Switch to HA_ACTIVE state.
     *
     * Falcon HA: Complete the initialization process, start all the
     * ServiceType.NORMAL services and start processing client work.
     */
    public void goHAActive();

    /**
     * Set last change record received from remote broker that this broker has processed
     */
    public void setLastReceivedChangeRecord(BrokerAddress remote,
                                          ChangeRecordInfo rec);
    /**
     * Synchronize cluster change record on remote broker join
     */
    public void syncChangeRecordOnJoin(BrokerAddress broker,  ChangeRecordInfo cri)
    throws BrokerException;
 
    /**
     * Get last change record generated (persisted) by this broker
     */
    public ChangeRecordInfo getLastStoredChangeRecord();

}

/*
 * EOF
 */
