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
 * @(#)ClusterMessageInfo.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * An instance of this class is intended to be used one direction only
 */

public class ClusterMessageInfo 
{
    protected Logger logger = Globals.getLogger();

    private static final String PROP_PREFIX_CUID_DCT = "CUID-DCT:";
    private static final String PROP_REDELIVERED = "redelivered";

    private PacketReference ref = null;
    private ArrayList<Consumer> consumers =  null; 
    private ArrayList<Integer> deliveryCnts =  null; 
    private boolean redelivered =  false; 
    private boolean sendMessageDeliveredAck = false;
    private Cluster c = null;

    private GPacket pkt = null;
    private DataInputStream dis = null;

    private ClusterMessageInfo(PacketReference ref, 
                               ArrayList<Consumer> consumers,
                               ArrayList<Integer> deliveryCnts,
                               boolean redelivered,
                               boolean sendMessageDeliveredAck, Cluster c) {
        this.ref = ref;
        this.consumers = consumers;
        this.deliveryCnts = deliveryCnts;
        this.redelivered = redelivered;
        this.sendMessageDeliveredAck = sendMessageDeliveredAck;
        this.c = c;
    }

    private ClusterMessageInfo(GPacket pkt, Cluster c) {
        this.pkt = pkt;
        this.c = c;
    }

    /**
     * Destination to GPacket
     *
     * @param d The Destination to be marshaled to GPacket
     */
    public static ClusterMessageInfo newInstance(
        PacketReference ref,
        ArrayList<Consumer> consumers, 
        ArrayList<Integer> deliveryCnts,
        boolean redelivered,
        boolean sendMessageDeliveredAck, Cluster c) {

        return new ClusterMessageInfo(ref, consumers, deliveryCnts,
                          redelivered, sendMessageDeliveredAck, c);
    }

    /**
     * GPacket to Destination 
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterMessageInfo newInstance(GPacket pkt, Cluster c) {
        return new ClusterMessageInfo(pkt, c);
    }

    public GPacket getGPacket() throws Exception {
        assert ( ref !=  null );
        assert ( consumers !=  null );

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_MESSAGE_DATA);
        gp.putProp("D", Boolean.valueOf(sendMessageDeliveredAck));
        gp.putProp("C", Integer.valueOf(consumers.size()));
        if (Globals.getDestinationList().isPartitionMode()) {
            gp.putProp("partitionID", Long.valueOf(
                ref.getPartitionedStore().getPartitionID().longValue()));
        }
        c.marshalBrokerAddress(c.getSelfAddress(), gp);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);


        Packet roPkt = null;
        try {
            for (int i = 0; i < consumers.size(); i++) {
                ConsumerUID intid = consumers.get(i).getConsumerUID();
                ClusterConsumerInfo.writeConsumerUID(intid, dos);
                gp.putProp(PROP_PREFIX_CUID_DCT+intid.longValue(),
                           deliveryCnts.get(i));
            }
            if (redelivered) {
                gp.putProp(PROP_REDELIVERED, Boolean.valueOf(redelivered));
            }
            roPkt = ref.getPacket();
            if (roPkt == null) {
                throw new BrokerException(Globals.getBrokerResources().getKString(
                          BrokerResources.X_NULL_PACKET_FROM_REF, ref.toString()));
            }
            roPkt.generateTimestamp(false);
            roPkt.generateSequenceNumber(false);

            roPkt.writePacket(dos);
            dos.flush();
            bos.flush();
           
        } catch (Exception e) {
            String emsg =  Globals.getBrokerResources().getKString(
                           BrokerResources.X_EXCEPTION_WRITE_PKT_ON_SEND_MSG_REMOTE, ref.toString(), e.getMessage());
            if (e instanceof BrokerException) {
                logger.log(Logger.WARNING, emsg);
                throw e;
            } 
            logger.logStack(Logger.WARNING, emsg, e);
            throw e;
        }

        byte[] buf = bos.toByteArray();
        gp.setPayload(ByteBuffer.wrap(buf));

        return gp;
    }

    public String toString() {
        if (consumers == null || ref == null) {
            return super.toString();
        }
        StringBuffer buf = new StringBuffer("\n");
        for (int i = 0; i < consumers.size(); i++) {
            ConsumerUID intid = ((Consumer) consumers.get(i)).getConsumerUID();
            buf.append("\t").append(intid).append("\n");
        }
        return buf.toString();
    }

    public Long getPartitionID() {
        assert (pkt != null);
        return (Long)pkt.getProp("partitionID");
    }

    public BrokerAddress getHomeBrokerAddress() throws Exception {
        assert (pkt != null);
        return c.unmarshalBrokerAddress(pkt);
    }

    public boolean getSendMessageDeliveredAck() {
        assert (pkt != null);
        return ((Boolean) pkt.getProp("D")).booleanValue();
    }

    public int getConsumerCount() {
        assert (pkt != null);
        return ((Integer) pkt.getProp("C")).intValue();
    }

    private Boolean getRedelivered() {
        assert (pkt != null);
        return (Boolean)pkt.getProp(PROP_REDELIVERED);
    }

    /**
     * @return null if not found
     */
    public Integer getDeliveryCount(ConsumerUID cuid) {
        assert (pkt != null);
        return (Integer)pkt.getProp(PROP_PREFIX_CUID_DCT+cuid.longValue());
    }

    /**
     * must called in the following order: 
     *
     * initPayloadRead()
     * readPayloadConsumerUIDs()
     * readPayloadMessage()
     */
    public void initPayloadRead() {
        assert ( pkt != null );
        ByteArrayInputStream bis = new ByteArrayInputStream(pkt.getPayload().array());
        dis = new DataInputStream(bis);
    }
    public Iterator readPayloadConsumerUIDs() {
        assert ( pkt !=  null );
        assert ( dis !=  null );

        return new ProtocolConsumerUIDIterator(dis, getConsumerCount());
    }
    public Packet readPayloadMessage() throws IOException {
        assert ( pkt !=  null );
        assert ( dis !=  null );

        Packet roPkt = new Packet(false);
        roPkt.generateTimestamp(false);
        roPkt.generateSequenceNumber(false);
        roPkt.readPacket(dis);
        Boolean b = getRedelivered();
        if (b != null) {
            roPkt.setRedelivered(b.booleanValue());
        }
        return roPkt;
    }

    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public GPacket getReplyGPacket(int status) {
        assert ( pkt != null );

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_MESSAGE_DATA_REPLY);
        gp.putProp("S", Integer.valueOf(status));
        // TBD: ADD SysMessageID as property?

        return gp;
    }

}

