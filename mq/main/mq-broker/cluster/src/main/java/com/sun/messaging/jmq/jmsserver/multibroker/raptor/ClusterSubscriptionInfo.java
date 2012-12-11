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
 * @(#)ClusterSubscriptionInfo.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.util.*;
import java.io.*;
import java.nio.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusterManager;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;

/**
 * An instance of this class is intended to be used one direction only
 * either Subscription/Consumer -> GPacket or GPacket -> Subscription/Consumer
 * (see assertions)
 */

public class ClusterSubscriptionInfo
{
    private Subscription subscription =  null;
    private Consumer consumer = null;
    private Cluster c = null;

    private GPacket pkt = null;

    private ClusterSubscriptionInfo(Subscription sub) {
        this.subscription = sub;
    }

    private ClusterSubscriptionInfo(Subscription sub, Consumer cs, Cluster c) {
        this.subscription = sub;
        this.consumer = cs;
        this.c = c;
    }
    
    private  ClusterSubscriptionInfo(GPacket pkt) {
        this(pkt, null);
    }

    private  ClusterSubscriptionInfo(GPacket pkt, Cluster c) {
        this.pkt = pkt;
        this.c = c;
    }

    public static ClusterSubscriptionInfo newInstance(Subscription sub) {
        return new ClusterSubscriptionInfo(sub);
    }

    public static ClusterSubscriptionInfo newInstance(Subscription sub, Consumer cs, Cluster c) {
        return new ClusterSubscriptionInfo(sub, cs, c);
    }

    public static ClusterSubscriptionInfo newInstance(GPacket pkt) { 
        return new ClusterSubscriptionInfo(pkt);
    }

    public static ClusterSubscriptionInfo newInstance(GPacket pkt, Cluster c) { 
        return new ClusterSubscriptionInfo(pkt, c);
    }

    public GPacket getGPacket(short protocol) {
        return getGPacket(protocol, false);
    }

    public GPacket getGPacket(short protocol, boolean changeRecord) {
        assert ( subscription != null );

        assert ( protocol == ProtocolGlobals.G_NEW_INTEREST ||  
                 protocol == ProtocolGlobals.G_DURABLE_ATTACH ||
                 protocol == ProtocolGlobals.G_REM_DURABLE_INTEREST );

        if (changeRecord ) {
        assert ( protocol == ProtocolGlobals.G_NEW_INTEREST ||  
                 protocol == ProtocolGlobals.G_REM_DURABLE_INTEREST );
        }

        GPacket gp = null;

        switch (protocol) {

            case ProtocolGlobals.G_NEW_INTEREST:

            ClusterConsumerInfo cci = ClusterConsumerInfo.newInstance(subscription, null);
            gp = cci.getGPacket(protocol);

            if (changeRecord) {
                gp.putProp("N", subscription.getDurableName());
                String clientID = subscription.getClientID();
                gp.putProp("I", (clientID == null ? "":clientID));
                gp.putProp("M", Boolean.valueOf(true));
            }  else {
                ChangeRecordInfo cri = subscription.getCurrentChangeRecordInfo(
                                                 ProtocolGlobals.G_NEW_INTEREST);
                if (cri != null) {
                    gp.putProp("shareccSeq"+1, cri.getSeq());
                    gp.putProp("shareccUUID"+1, cri.getUUID());
                    gp.putProp("shareccResetUUID"+1, cri.getResetUUID());
                }
            }
                
            break;


            case ProtocolGlobals.G_DURABLE_ATTACH:
            {
            assert ( consumer != null );

            gp = GPacket.getInstance();
            gp.setType(protocol);

            String dname = subscription.getDurableName();
            if (dname != null) {
                gp.putProp("N", dname);
            } 
            String clientID = subscription.getClientID();
            gp.putProp("I", (clientID == null ? "":clientID));
            String ndsubname = subscription.getNDSubscriptionName();
            if (ndsubname != null) {
                gp.putProp("NDN", ndsubname);
            }
            ClusterManager cm = Globals.getClusterManager();
            int csize = 1;
            if (cm != null) {
                csize = cm.getConfigBrokerCount();
                if (csize <= 0) {
                    csize = 1;
                }
            }
            int prefetch = consumer.getPrefetchForRemote()/csize;
            if (prefetch <= 0) {
                prefetch = 1; 
            }
            gp.putProp(String.valueOf(consumer.getConsumerUID().longValue())+":"+
                       Consumer.PREFETCH, new Integer(prefetch));
            gp.putProp("allowsNonDurable", new Boolean(true));
            c.marshalBrokerAddress(c.getSelfAddress(), gp);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                ClusterConsumerInfo.writeConsumer(consumer, dos);
                dos.flush();
                bos.flush();
            }
            catch (IOException e) { /* Ignore */ }

            gp.setPayload(ByteBuffer.wrap(bos.toByteArray()));

            }
            break;


            case ProtocolGlobals.G_REM_DURABLE_INTEREST:
            {

            gp = GPacket.getInstance();
            gp.setType(protocol);
            gp.putProp("C", new Integer(1));

            String dname = subscription.getDurableName();
            String clientID = subscription.getClientID();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                dos.writeUTF(dname);
                dos.writeUTF((clientID == null ? "":clientID));
                dos.flush();
                bos.flush();
            }
            catch (IOException e) { /* Ignore */ }

            gp.setPayload(ByteBuffer.wrap(bos.toByteArray()));

            if (changeRecord) {
                gp.putProp("N", dname);
                gp.putProp("I", (clientID == null ? "":clientID));
                gp.putProp("M", Boolean.valueOf(true));
            } else {
                ChangeRecordInfo cri = subscription.getCurrentChangeRecordInfo(
                                           ProtocolGlobals.G_REM_DURABLE_INTEREST);
                if (cri != null) {
                    gp.putProp("shareccSeq"+1, cri.getSeq());
                    gp.putProp("shareccUUID"+1, cri.getUUID());
                    gp.putProp("shareccResetUUID"+1, cri.getResetUUID());
                }
            }

            }
            break;
        }

        return gp;
    }

    public int getConsumerCount() {
        assert ( pkt != null);

        short type = pkt.getType(); 
        assert ( type == ProtocolGlobals.G_NEW_INTEREST ||
                 type == ProtocolGlobals.G_INTEREST_UPDATE ||
                 type == ProtocolGlobals.G_REM_DURABLE_INTEREST );

        int count = ((Integer)pkt.getProp("C")).intValue();
        assert ( count == 1);
        return count;
    }

    public ChangeRecordInfo getShareccInfo(int i) {
        if (pkt.getProp("shareccSeq"+i) == null) {
            return null;
        }
        ChangeRecordInfo cri =  new ChangeRecordInfo();
        cri.setSeq((Long)pkt.getProp("shareccSeq"+i));
        cri.setUUID((String)pkt.getProp("shareccUUID"+i));
        cri.setResetUUID((String)pkt.getProp("shareccResetUUID"+i));
        cri.setType(pkt.getType());
        return cri;
    }

    public boolean isConfigSyncResponse() { 
        assert ( pkt != null );

        boolean b = false;
        if (pkt.getProp("M") != null) {
            b = ((Boolean) pkt.getProp("M")).booleanValue();
        }
        return b;
    }

    public String getDurableName() {
        assert ( pkt != null );
        return (String)pkt.getProp("N");
    }

    public String getClientID() {
        assert ( pkt != null );
        String clientID = (String)pkt.getProp("I");
        if (clientID == null || clientID.length() == 0) {
            return null;
        }
        return clientID;
    }

    public String getNDSubscriptionName() {
        assert ( pkt != null );
        return (String)pkt.getProp("NDN");
    }

    public Boolean allowsNonDurable() { 
        assert ( pkt != null );
        return (Boolean)pkt.getProp("allowsNonDurable");
    }

    public Consumer getConsumer() throws Exception {
        assert ( pkt != null );
        
        short type = pkt.getType();
        assert ( type ==  ProtocolGlobals.G_DURABLE_ATTACH );
        
        ByteArrayInputStream bis = new ByteArrayInputStream(pkt.getPayload().array());
        DataInputStream dis = new DataInputStream(bis);
        Consumer cs = ClusterConsumerInfo.readConsumer(dis);
        Integer prefetch = (Integer)pkt.getProp(String.valueOf(
                                                cs.getConsumerUID().longValue())+
                                                ":"+Consumer.PREFETCH);
        if (prefetch != null) cs.setRemotePrefetch(prefetch.intValue());
        BrokerAddress from = c.unmarshalBrokerAddress(pkt);
        if (from != null) cs.getConsumerUID().setBrokerAddress(from);
		return cs;
    }

    public Iterator getSubscriptions() {
        assert ( pkt != null );

        short type = pkt.getType();
        assert ( type ==  ProtocolGlobals.G_REM_DURABLE_INTEREST );
        return new SubscriptionIterator(pkt.getPayload().array(), getConsumerCount()); 
    }

    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public static GPacket getReplyGPacket(short protocol, int status) {
        GPacket gp = GPacket.getInstance();
        gp.setType(protocol);
        gp.putProp("S", new Integer(status));
        return gp;
    }

}


class SubscriptionIterator implements Iterator
{
    private int count = 0;
    private int count_read = 0;
    private DataInputStream dis = null;

    public SubscriptionIterator(byte[] payload, int count) {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload);
        dis = new DataInputStream(bis);
        this.count = count;
        this.count_read = 0;
    }

    public boolean hasNext() {
        if (count_read < 0) throw new IllegalStateException("SubscriptionIterator");
        return count_read < count;
    }

    /**
     * Caller must catch RuntimeException and getCause
     */
    public Object next() throws RuntimeException {
        try {
            String dname = dis.readUTF();
            String clientID = dis.readUTF();
            if (clientID != null && clientID.length() == 0) {
                clientID = null;
            }
            Subscription sub = Subscription.findDurableSubscription(clientID, dname);
            count_read++;
            return sub;
        } catch (IOException e) {
            count_read = -1;
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }
}


