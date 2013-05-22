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
 * @(#)NoCluster.java	1.38 07/23/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * Simple message bus implementation which can be used
 * in non-clustered environments.
 */
public class NoCluster implements ClusterBroadcast {

    private static final Object noOwner = new Object();

    private static BrokerAddress noAddress = 
         new BrokerAddress() {
            String address="localhost";

            public Object clone() {
               return this;
            }
            public boolean equals(Object o) {
                return o instanceof BrokerAddress;
            }
            public int hashCode() {
                return address.hashCode();
            }

            public String toProtocolString() {
                return null;
            }

            public BrokerAddress fromProtocolString(String s) throws Exception {
                throw new UnsupportedOperationException(
                          this.getClass().getName()+".fromProtocolString");
            }

            public void writeBrokerAddress(DataOutputStream os) 
                throws IOException
            {
            }
             
            public void readBrokerAddress(DataInputStream dis) 
                throws IOException
            {
            }

            public boolean getHAEnabled() { 
                return false; 
            }
            public String getBrokerID() {
                return null; 
            }
            public UID getBrokerSessionUID() { 
                return null; 
            }
            public UID getStoreSessionUID() { 
                return null; 
            }
            public void setStoreSessionUID(UID uid) { 
            }
            public String getInstanceName() { 
                return null; 
            }
         };

    public int getClusterVersion() {
        return VERSION_350;
    }

    public void messageDelivered(SysMessageID id, ConsumerUID uid,
                BrokerAddress ba)
    {
    }

    public void init(int connLimit,  int version)
    throws BrokerException {
    }

    public Object getProtocol() {
        return null;
    }

    /**
     * Set the matchProps for the cluster.
     */
    public void setMatchProps(Properties matchProps) {
    }

    /**
     *
     */
    public boolean waitForConfigSync() {
        return false;
    }



    public void startClusterIO() {
    }

    public void pauseMessageFlow() throws IOException {
    }
    public void resumeMessageFlow() throws IOException {
    }

    public void forwardMessage(PacketReference ref, Collection consumers)
    {
    }

    public void stopClusterIO(boolean requestTakeover, boolean force,
                              BrokerAddress excludedBroker) {
    }

    /**
     * Returns the address of this broker.
     * @return <code> BrokerAddress </code> object representing this
     * broker.
     */
    public BrokerAddress getMyAddress() {
        return noAddress;
    }

    private static Map map = Collections.synchronizedMap(
             new HashMap());

    public boolean lockSharedResource(String resource, Object owner) {
        return true;
    }

    public boolean lockExclusiveResource(String resource, Object owner) {
        return true;
    }

    public void unlockExclusiveResource(String resource, Object owner) {
    }

    public boolean lockDestination(DestinationUID uid, Object owner)
    {
        // unnecessary in single broker implementation
        return true;
    }

    public void unlockDestination(DestinationUID uid, Object owner) {
        // unnecessary in single broker 
    }

    public synchronized boolean lockClientID(String clientid, Object owner, boolean shared)
    {
        if (shared) {
            throw new RuntimeException("shared clientID's not supported w/o cluster");
        }
        String lockid = "clientid:" + clientid;
        if (shared) {
           // no reason to lock in multibroker
           return true;
        }
        return lockResource(lockid, System.currentTimeMillis(), owner);
    }
    public synchronized void unlockClientID(String clientid, Object owner) {
        String lockid = "clientid:" + clientid;
        unlockResource(lockid);
    }

    public boolean getConsumerLock(ConsumerUID uid,
                    DestinationUID duid, int position,
                    int maxActive, Object owner)
            throws BrokerException
    {

        return true;
    }


    public void unlockConsumer(ConsumerUID uid, DestinationUID duid, int position) {
        // for now, do nothing
    }
    

    public boolean lockResource(String id, long timestamp, 
                Object owner) {
        synchronized (map) {
            Object val = map.get(id);
            if (val != null) {
               return false;
            }
            if (owner == null) {
                owner = noOwner;
            }
            map.put(id, owner);
            return true;
        }    
    }

    public void unlockResource(String id) {
        map.remove(id);
    }

    public void freeAllLocks(Object owner) {
        synchronized (map) {
            Iterator itr = map.values().iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                if (o.equals(owner)) {
                    itr.remove();
                }
            }
        }
    }

    public void acknowledgeMessage(BrokerAddress address, SysMessageID sysid, 
                                   ConsumerUID cuid, int ackType, Map optionalProps,
                                   boolean ackack) throws BrokerException
    {
    }

    public void acknowledgeMessage2P(BrokerAddress address, SysMessageID[] sysids, 
                                   ConsumerUID[] cuids, int type,
                                   Map optProp, Long txnID, UID txnStoreSession, 
                                   boolean ackack, boolean async) 
                                   throws BrokerException
    {
       throw new BrokerException("Broker Internal Error: unexpected call acknowledgeMessage");
    }


    public void recordUpdateDestination(Destination d)
        throws BrokerException {
    }

    public void recordRemoveDestination(Destination d)
        throws BrokerException {
    }

    public void createDestination(Destination dest) 
            throws BrokerException
    {
    }

    public void recordCreateSubscription(Subscription sub)
        throws BrokerException {
    }

    public void recordUnsubscribe(Subscription sub)
        throws BrokerException {
    }

    public void createSubscription(Subscription sub, Consumer cons)
            throws BrokerException
    {
    }

    public void createConsumer(Consumer con)
            throws BrokerException {
    }

    public void updateDestination(Destination dest)
            throws BrokerException {
    }

    public void updateSubscription(Subscription sub)
            throws BrokerException {
    }

    public void updateConsumer(Consumer con)
            throws BrokerException {
    }


    public void destroyDestination(Destination dest)
            throws BrokerException {
    }

    public void destroyConsumer(Consumer con, Map pendingMsgs, boolean cleanup)
            throws BrokerException {
    }

    public void connectionClosed(ConnectionUID uid, boolean admin) {
        freeAllLocks(uid);
    }

    public void reloadCluster() {
    }

    public Hashtable getAllDebugState() {
        return new Hashtable();
    }

    public boolean lockUIDPrefix(short p){
        return true;
    }

    public void preTakeover(String brokerID, UID storeSession,
                String brokerHost, UID brokerSession) throws BrokerException { 
        throw new BrokerException("Not Supported");
    }

    public void postTakeover(String brokerID, UID storeSession, boolean aborted, boolean notify) {};

    public void sendClusterTransactionInfo(long tid, BrokerAddress address) {};

    public BrokerAddress lookupBrokerAddress(String brokerid) {
        return null;
    };

    public BrokerAddress lookupBrokerAddress(BrokerMQAddress mqaddr) {
        return null;
    };

    public String lookupStoreSessionOwner(UID storeSession) {
        return null;
    }

    /**
     * Change master broker
     */
    public void changeMasterBroker(BrokerMQAddress newmaster, BrokerMQAddress oldmaster)
    throws BrokerException {
        throw new BrokerException("Not Supported");
    }

    public String sendTakeoverMEPrepare(String brokerID, byte[] token,
                                        Long syncTimeout, String uuid)
                                        throws BrokerException {
        throw new BrokerException("Not Supported");
    }

    public String sendTakeoverME(String brokerID, String uuid)
    throws BrokerException {
        throw new BrokerException("Not Supported");
    }

    public void sendMigrateStoreRequest(String targetBrokerID, Long syncTimeout,
                                        String uuid, String myBrokerID)
                                        throws BrokerException {
        throw new BrokerException("Not Supported");
    }

    public void transferFiles(String[] fileNames, String targetBrokerID,
                              Long syncTimeout, String uuid, String myBrokerID,
                              String module, FileTransferCallback callback)
                              throws BrokerException { 
        throw new BrokerException("Not Supported");
    }

    public void syncChangeRecordOnStartup() throws BrokerException {
    }

    public void notifyPartitionArrival(UID partitionId, String brokerID)
    throws BrokerException {
    }
}

