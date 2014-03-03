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

/*
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;
import com.sun.messaging.jmq.jmsserver.data.TransactionBroker;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.multibroker.ClusterGlobals;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusterProtocolHelper;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;

/**
 */

public class ClusterTakeoverMEPrepareInfo implements ClusterProtocolHelper
{
    protected Logger logger = Globals.getLogger();

    private String groupName = null;
    private String nodeName = null;
    private String masterHostPort = null;
    private byte[] commitToken = null;
    private String targetNodeName = null;
    private Long xid = null;
    private Cluster c = null;
    private String uuid = null;
    private Long syncTimeout = null;

    private GPacket pkt = null;

    private ClusterTakeoverMEPrepareInfo(String groupName, String nodeName,
                                         String masterHostPort,
                                         byte[] commitToken, Long syncTimeout,
                                         String targetNodeName, String uuid,
                                         Long xid, Cluster c) {
        this.groupName = groupName;
        this.nodeName = nodeName;
        this.masterHostPort = masterHostPort;
        this.targetNodeName = targetNodeName;
        this.xid = xid;
        this.c = c;
        this.commitToken = commitToken;
        this.syncTimeout = syncTimeout;
        this.uuid = uuid;
    }

    private ClusterTakeoverMEPrepareInfo(GPacket pkt, Cluster c) {
        this.pkt = pkt;
        this.c = c;
    }

    public static ClusterTakeoverMEPrepareInfo newInstance(
                      String groupName, String nodeName, 
                      String masterHostPort, byte[] commitToken,
                      Long syncTimeout, String targetNodeName, String uuid,
                      Long xid, Cluster c) {
        return new ClusterTakeoverMEPrepareInfo(groupName, nodeName, masterHostPort,
                   commitToken, syncTimeout, targetNodeName, uuid, xid,  c);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterTakeoverMEPrepareInfo newInstance(GPacket pkt, Cluster c) {
        return new ClusterTakeoverMEPrepareInfo(pkt, c);
    }

    public GPacket getGPacket() throws IOException { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_TAKEOVER_ME_PREPARE);
        gp.putProp("groupName", groupName);
        gp.putProp("nodeName", nodeName);
        gp.putProp("masterHostPort", masterHostPort);
        gp.putProp("clusterid", Globals.getClusterID());
        gp.putProp("targetNodeName", targetNodeName);
        gp.putProp("X", xid);
        gp.putProp("UUID", uuid);
        gp.putProp("TS", Long.valueOf(System.currentTimeMillis()));
        if (syncTimeout != null) {
            gp.putProp("syncTimeout", syncTimeout); 
        }
        c.marshalBrokerAddress(c.getSelfAddress(), gp); 
		gp.setPayload(ByteBuffer.wrap(commitToken));
        gp.setBit(gp.A_BIT, true);

        return gp;
    }

    public String getGroupName() {
        assert ( pkt != null );
        return (String)pkt.getProp("groupName");
    }

    public String getNodeName() {
        assert ( pkt != null );
        return (String)pkt.getProp("nodeName");
    }

    public String getMasterHostPort() {
        assert ( pkt != null );
        return (String)pkt.getProp("masterHostPort");
    }

    public String getClusterID() {
        assert ( pkt != null );
        return (String)pkt.getProp("clusterid");
    }

    public byte[] getCommitToken() {
        assert ( pkt != null );
        byte[] buf = null;
        if (pkt.getPayload() != null) {
            buf = pkt.getPayload().array();
        }
        return buf;
    }

    public Long getSyncTimeout() {
        assert ( pkt != null );
        return (Long)pkt.getProp("syncTimeout");
    }

    public String getTargetNodeName() {
        assert ( pkt != null );
        return (String)pkt.getProp("targetNodeName");
    }

    public BrokerAddress getOwnerAddress() throws Exception {
        assert ( pkt != null );
        return c.unmarshalBrokerAddress(pkt);
    }

    public String getUUID() {
        if (pkt != null) {
            return (String)pkt.getProp("UUID");
        }
        return uuid;
    }

    public Long getXid() {
        assert( pkt != null);
        return (Long)pkt.getProp("X");
    }

    public Long getTimestamp() {
        assert( pkt != null);
        return (Long)pkt.getProp("TS");
    }

    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public GPacket getReplyGPacket(int status, String reason, String replicaHostPort) {
        assert( pkt != null);
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_TAKEOVER_ME_PREPARE_REPLY);
        gp.putProp("X", (Long)pkt.getProp("X"));
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) {
            gp.putProp("reason", reason);
        }
        if (replicaHostPort != null) {
            gp.putProp("replicaHostPort", replicaHostPort);
        }
        return gp;
    }

    public void sendReply(BrokerAddress recipient, int status,
                          String reason, Object extraInfo) {
        if (!needReply()) {
            return;
        }
        String replicaHostPort = (String)extraInfo;
        GPacket reply = getReplyGPacket(status, reason, replicaHostPort);
        try {
            c.unicast(recipient, reply);
        } catch (Exception e) {
            String[] args = new String[] {
                ProtocolGlobals.getPacketTypeDisplayString(
                    ProtocolGlobals.G_TAKEOVER_ME_PREPARE_REPLY),
                    recipient.toString(), this.toString() };
            logger.logStack(logger.ERROR, Globals.getBrokerResources().getKString(
                Globals.getBrokerResources().E_CLUSTER_SEND_PACKET_FAILED, args), e);
        }
    }

    /**
     */
    public String toString() {

        if (pkt == null) {
            return "["+groupName+"["+nodeName+", "+masterHostPort+"]target="+
                   targetNodeName+", timeout="+syncTimeout+", xid="+xid+", uuid="+uuid+"]";
        } 
        return "["+getGroupName()+"["+getNodeName()+", "+getMasterHostPort()+"]target="+
               getTargetNodeName()+", timeout="+getSyncTimeout()+", xid="+getXid()+
               ", uuid="+getUUID()+", time="+getTimestamp()+"]";
    }

    protected static Long getReplyPacketXid(GPacket gp) {
        return (Long)gp.getProp("X");
    }

    protected String getReplyReplicaHostPort(GPacket gp) { 
        return (String)gp.getProp("replicaHostPort");
    }
}
