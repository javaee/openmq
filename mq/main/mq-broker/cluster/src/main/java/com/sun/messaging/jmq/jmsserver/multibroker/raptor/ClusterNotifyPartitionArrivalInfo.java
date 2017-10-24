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
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;

/**
 */

public class ClusterNotifyPartitionArrivalInfo 
{
    protected Logger logger = Globals.getLogger();

    private UID partitionID = null;
    private String targetBrokerID = null;
    private Long xid = null;
    private Cluster c = null;
    private GPacket pkt = null;

    private ClusterNotifyPartitionArrivalInfo(UID partitionID, String targetBrokerID,
                                              Long xid, Cluster c) {
        this.partitionID = partitionID;
        this.targetBrokerID = targetBrokerID;
        this.xid = xid;
        this.c = c;
    }

    private ClusterNotifyPartitionArrivalInfo(GPacket pkt, Cluster c) {
        this.pkt = pkt;
        this.c = c;
    }

    public static ClusterNotifyPartitionArrivalInfo newInstance(
                      UID partitionID, String targetBrokerID,
                      Long xid, Cluster c) {
        return new ClusterNotifyPartitionArrivalInfo(partitionID, targetBrokerID, xid, c);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterNotifyPartitionArrivalInfo newInstance(GPacket pkt, Cluster c) {
        return new ClusterNotifyPartitionArrivalInfo(pkt, c);
    }

    public GPacket getGPacket() throws IOException { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_NOTIFY_PARTITION_ARRIVAL);
        gp.putProp("targetBrokerID", targetBrokerID);
        gp.putProp("partitionID", Long.valueOf(partitionID.longValue()));
        gp.putProp("X", xid);
        gp.putProp("TS", Long.valueOf(System.currentTimeMillis()));
        c.marshalBrokerAddress(c.getSelfAddress(), gp); 
        gp.setBit(gp.A_BIT, true);

        return gp;
    }

    public UID getPartitionID() {
        assert ( pkt != null );
        return new UID(((Long)pkt.getProp("partitionID")).longValue());
    }

    public String getTargetBrokerID() {
        assert ( pkt != null );
        return (String)pkt.getProp("targetBrokerID");
    }

    public Long getXid() {
        assert ( pkt != null );
        return (Long)pkt.getProp("X");
    }

    public BrokerAddress getOwnerAddress() throws Exception {
        assert ( pkt != null );
        return c.unmarshalBrokerAddress(pkt);
    }

    public Long getTimestamp() {
        assert( pkt != null);
        return (Long)pkt.getProp("TS");
    }

    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public GPacket getReplyGPacket(int status, String reason) {
        assert( pkt != null);
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_NOTIFY_PARTITION_ARRIVAL_REPLY);
        gp.putProp("X", (Long)pkt.getProp("X"));
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) {
            gp.putProp("reason", reason);
        }
        return gp;
    }

    /**
     */
    public String toString() {

        if (pkt == null) {
            return "["+partitionID+", "+targetBrokerID+"]";
        }
        return "["+getPartitionID()+", "+getTargetBrokerID()+"]";
    }

    protected String getReplyToString(GPacket reply) {
        return toString()+":[status="+reply.getProp("S")+", "+reply.getProp("reason")+"]";
    }

    public static Long getReplyPacketXid(GPacket gp) {
        return (Long)gp.getProp("X");
    }
}
