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

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;

/**
 */
public class ClusterNewMasterBrokerInfo 
{

    private Long xid = null;
    private String uuid = null;

    private BrokerAddress newmaster = null;
    private BrokerAddress oldmaster = null;
    Cluster c = null;

    private GPacket pkt = null; 

    private ClusterNewMasterBrokerInfo(BrokerAddress newmaster,
                                       BrokerAddress oldmaster, 
                                       String uuid, Long xid, Cluster c) {
        this.xid = xid;
        this.uuid = uuid;
        this.c = c;
        this.newmaster = newmaster;
        this.oldmaster = oldmaster;
    }

    private ClusterNewMasterBrokerInfo(GPacket pkt, Cluster c) {
        this.pkt = pkt;
        this.c = c;
    }

    public static ClusterNewMasterBrokerInfo newInstance(BrokerAddress newmaster, 
                                              BrokerAddress oldmaster, 
                                              String uuid, Long xid, Cluster c) {
        return new ClusterNewMasterBrokerInfo(newmaster, oldmaster, uuid, xid, c); 
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterNewMasterBrokerInfo newInstance(GPacket pkt, Cluster c) {
        return new ClusterNewMasterBrokerInfo(pkt, c);
    }

    public GPacket getGPacket() throws Exception { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_NEW_MASTER_BROKER);
        gp.putProp("TS", Long.valueOf(System.currentTimeMillis()));
        gp.putProp("X", xid);
        gp.putProp("UUID", uuid);
        gp.putProp("oldMasterBroker", oldmaster.toProtocolString());
        c.marshalBrokerAddress(newmaster, gp);
        gp.setBit(gp.A_BIT, true);

        return gp;
    }

    public BrokerAddress getNewMasterBroker() throws Exception {
        assert( pkt != null);
        newmaster = c.unmarshalBrokerAddress(pkt);
        return newmaster;
    }

    public BrokerAddress getOldMasterBroker() throws Exception {
        assert( pkt != null);
        String oldm = (String)pkt.getProp("oldMasterBroker");
        oldmaster = Globals.getMyAddress().fromProtocolString(oldm);
        return oldmaster;
    }

    public String getUUID() {
        assert( pkt != null);
        return (String)pkt.getProp("UUID");
    }
    public Long getXid() {
        assert( pkt != null);
        return (Long)pkt.getProp("X");
    }
    public Long getTimestamp() {
        assert( pkt != null);
        return (Long)pkt.getProp("TS");
    }

    public GPacket getReplyGPacket(int status, String reason) {
        assert( pkt != null);
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_NEW_MASTER_BROKER_REPLY);
        gp.putProp("X", (Long)pkt.getProp("X"));
        gp.putProp("UUID", (String)pkt.getProp("UUID"));
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) {
            gp.putProp("reason", reason);
        }
        return gp;
    }

    public String toString() {
        if (pkt == null) {
            return "[newMasterBroker="+newmaster+
            ", oldMasterBroker="+oldmaster+", xid="+xid+", uuid="+uuid+"]";
        } 
        return (newmaster == null ? "":"[newMasterBroker="+newmaster)+
               (oldmaster == null ? "":"[oldMasterBroker="+oldmaster)+
               ", xid="+getXid()+", ts="+getTimestamp()+", uuid="+getUUID()+"]";
    }

    public static Long getReplyPacketXid(GPacket gp) {
        return (Long)gp.getProp("X");
    }
}
