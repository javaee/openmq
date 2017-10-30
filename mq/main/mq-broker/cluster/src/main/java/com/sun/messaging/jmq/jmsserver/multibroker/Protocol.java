/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)Protocol.java	1.19 07/23/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.cluster.api.FileTransferCallback;

public interface Protocol extends ClusterCallback
{
    /**
     * Get the cluster protocol version used by this
     * protocol implementation.
     */
    public int getClusterVersion() throws BrokerException;

    /**
     * sets the list of properties that must match for brokers to connect
     */
    public void setMatchProps(Properties matchProps);

    public void startClusterIO();

    public void stopClusterIO(boolean requestTakeover, boolean force,
        com.sun.messaging.jmq.jmsserver.core.BrokerAddress excludedBroker);

    public void reloadCluster();

    public void stopMessageFlow() throws IOException;

    public void resumeMessageFlow() throws IOException;

    public boolean waitForConfigSync();

    public void sendMessage(PacketReference pkt, Collection<Consumer> targets,
                            boolean sendMsgDeliveredAck);

    public void sendMessageAck(com.sun.messaging.jmq.jmsserver.core.BrokerAddress msgHome, 
                               SysMessageID mid,
                               com.sun.messaging.jmq.jmsserver.core.ConsumerUID cid,
                               int ackType, Map optionalProps, boolean ackack) 
                               throws BrokerException; 
   
    public void sendMessageAck2P(com.sun.messaging.jmq.jmsserver.core.BrokerAddress msgHome, 
                                 SysMessageID[] mids,
                                 com.sun.messaging.jmq.jmsserver.core.ConsumerUID[] cids,
                                 int ackType, Map optionalProps, Long txnID, UID txnStoreSession,
                                 boolean ackack, boolean async) 
                                 throws BrokerException;

    public void clientClosed(ConnectionUID conid, boolean notify);

    /**
     * Obtain a cluster-wide "shared" lock on a resource.
     * Unlike the normal "exclusive" locks, the shared locks allow
     * more than one clients to access the same resource. This method
     * ensures that the resource cannot be locked as shared and
     * exclusive at the same time!
     *
     * @param resID Resource name. The caller must ensure that
     * there are no name space conflicts between different
     * types of resources. This can be achieved by simply using
     * resource names like -"durable:foo", "queue:foo",
     * "clientid:foo"...
     * @param owner The object representing the owner of the resource
     * @return  ProtocolGlobals.G_LOCK_SUCCESS if the resource was
     *          locked successfully.
     *          ProtocolGlobals.G_LOCK_FAILURE if the resource could
     *          not be locked.
     */
    public int lockSharedResource(String resId, Object owner);

    /**
     * Obtain a cluster-wide lock on a resource. This method is
     * used to ensure mutual exclusion for durable subscriptions,
     * queue receivers, client IDs etc.
     *
     * @param resID Resource name. The caller must ensure that
     * there are no name space conflicts between different
     * types of resources. This can be achieved by simply using
     * resource names like -"durable:foo", "queue:foo",
     * "clientid:foo"...
     *
     * @param timestamp The creation time for the resource.
     * In case of a lock contention the older resource automatically
     * wins.
     *
     * @param owner the owner object of the resource 
     *
     * @return MB_LOCK_SUCCESS if the resource was locked successfully.
     *         MB_LOCK_FAILURE if the resource could not be locked.
     */
    public int lockResource(String resId, long timestamp, Object owner);

    /**
     * Unlocks a resource.
     */
    public void unlockResource(String resId);

    /**
     * Record the destination create / update event with the master
     * broker. This method must be called before the destination is
     * added. If it throws an exception, the error must be propagated
     * back to the client.
     */
    public void recordUpdateDestination(Destination d)
        throws BrokerException;

    /**
     * Record the destroy destination event with the master broker.
     * This method must be called before the destination is deleted.
     * If it throws an exception, the error must be propagated back to
     * the client.
     */
    public void recordRemoveDestination(Destination d)
        throws BrokerException;

    public void sendNewDestination(Destination d)
                  throws BrokerException;

    public void sendRemovedDestination(Destination d)
                  throws BrokerException;

    public void sendUpdateDestination(Destination d)
                  throws BrokerException;

    public void recordCreateSubscription(Subscription sub)
        throws BrokerException;

    public void recordUnsubscribe(Subscription sub)
        throws BrokerException;

    public void sendNewSubscription(Subscription sub, Consumer cons,
        boolean active) throws BrokerException;

    public void sendNewConsumer(Consumer intr, boolean active)
                  throws BrokerException;

    public void sendRemovedConsumer(Consumer intr, Map pendingMsgs, boolean cleanup)
                  throws BrokerException;

    public void handleGPacket(MessageBusCallback mbcb, 
        com.sun.messaging.jmq.jmsserver.core.BrokerAddress sender, GPacket pkt);

    public void preTakeover(String brokerID, UID storeSession,
                String brokerHost, UID brokerSession) throws BrokerException ;
    public void postTakeover(String brokerID, UID storeSession, boolean aborted, boolean notify);

    public void sendClusterTransactionInfo(long tid,
                com.sun.messaging.jmq.jmsserver.core.BrokerAddress to);

    public void sendTransactionInquiry(TransactionUID tid,
                com.sun.messaging.jmq.jmsserver.core.BrokerAddress to);

    public void sendPreparedTransactionInquiries(List<TransactionUID> tids,
                com.sun.messaging.jmq.jmsserver.core.BrokerAddress to);

    //in seconds
    public int getClusterAckWaitTimeout();

    public com.sun.messaging.jmq.jmsserver.core.BrokerAddress lookupBrokerAddress(String brokerid);

    public com.sun.messaging.jmq.jmsserver.core.BrokerAddress lookupBrokerAddress(BrokerMQAddress mqaddr);

    public String lookupStoreSessionOwner(UID session);

    public void changeMasterBroker(BrokerMQAddress newmaster, BrokerMQAddress oldmaster)
    throws BrokerException;

    public String sendTakeoverMEPrepare(String brokerID, byte[] commitToken,
                                        Long syncTimeout, String uuid)
                                        throws BrokerException;

    public String sendTakeoverME(String brokerID, String uuid)
    throws BrokerException;
   
    public void sendMigrateStoreRequest(String targetBrokerID, Long syncTimeout,
                                        String uuid, String myBrokerID)
                                        throws BrokerException;

    public void transferFiles(String[] fileNames, String targetBrokerID,
                              Long syncTimeout, String uuid, String myBrokerID,
                              String module, FileTransferCallback callback)
                              throws BrokerException;

    public void notifyPartitionArrival(UID partitionID, String brokerID)
    throws BrokerException;

    public Hashtable getDebugState();
}
