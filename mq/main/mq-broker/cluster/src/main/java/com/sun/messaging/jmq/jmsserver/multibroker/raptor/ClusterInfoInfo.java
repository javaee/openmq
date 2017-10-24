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

import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;

/**
 * A general cluster G_INFO 
 */

public class ClusterInfoInfo 
{

    private ChangeRecordInfo lastStoredChangeRecord = null;

    private GPacket gp = null;  //out going
    private GPacket pkt = null; //in coming

    private ClusterInfoInfo() {
    }

    private ClusterInfoInfo(GPacket pkt) {
        this.pkt = pkt;
    }

    public static ClusterInfoInfo newInstance() {
        return new ClusterInfoInfo(); 
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterInfoInfo newInstance(GPacket pkt) {
        return new ClusterInfoInfo(pkt);
    }

    public void partitionAdded(UID partitionID) {
        if (gp == null) {
            gp = GPacket.getInstance();
            gp.setType(ProtocolGlobals.G_INFO);
        }
        Integer v = (Integer)gp.getProp("T");
        if (v == null) {
	    gp.putProp("T", Integer.valueOf(ClusterInfoRequestInfo.PARTITION_ADDED_TYPE));
        } else { 
            int t = (v.intValue() | ClusterInfoRequestInfo.PARTITION_ADDED_TYPE);
	    gp.putProp("T", Integer.valueOf(t));
        }
        gp.putProp(ClusterInfoRequestInfo.PARTITION_PROP,
                   Long.valueOf(partitionID.longValue()));
    } 

    public void storeSessionOwnerRequestReply(ClusterInfoRequestInfo cir, 
        int status, String reason, String owner) {

        if (gp == null) {
            gp = GPacket.getInstance();
            gp.setType(ProtocolGlobals.G_INFO);
	}
        Integer v = (Integer)gp.getProp("T");
        if (v == null) {
	    gp.putProp("T", Integer.valueOf(ClusterInfoRequestInfo.STORE_SESSION_OWNER_TYPE));
        } else { 
            int t = (v.intValue() | ClusterInfoRequestInfo.STORE_SESSION_OWNER_TYPE);
	    gp.putProp("T", Integer.valueOf(t));
        }
	long ss = cir.getStoreSession();
	gp.putProp(ClusterInfoRequestInfo.STORE_SESSION_PROP, Long.valueOf(ss));
	gp.putProp("X", cir.getXid());
	gp.putProp("S", Integer.valueOf(status));
	if (reason != null) {
            gp.putProp("reason", reason);
        }
        if (owner != null) {
            gp.putProp(ClusterInfoRequestInfo.STORE_SESSION_OWNER_PROP, owner);
        }
    }

    public Long getXid() {
        assert ( gp != null );
        return (Long)gp.getProp("X");
    }

    public void setBroadcast(boolean b) {
        assert ( gp != null );
        if (b) { 
            gp.setBit(gp.B_BIT, true); 
        }
    }

    public GPacket getGPacket() { 
        assert ( gp != null );
        gp.setBit(gp.A_BIT, false);
        return gp;
    }

    public static boolean isStoreSessionOwnerInfo(GPacket pkt) {
        Integer t = (Integer)pkt.getProp("T");
        if (t == null) {
            return false;
        }
        return (t.intValue() & ClusterInfoRequestInfo.STORE_SESSION_OWNER_TYPE)
                == ClusterInfoRequestInfo.STORE_SESSION_OWNER_TYPE;
    }

    public static boolean isPartitionAddedInfo(GPacket pkt) {
        Integer t = (Integer)pkt.getProp("T");
        if (t == null) {
            return false;
        }
        return (t.intValue() & ClusterInfoRequestInfo.PARTITION_ADDED_TYPE)
                == ClusterInfoRequestInfo.PARTITION_ADDED_TYPE;
    }

   public String getStoreSessionOwner() {
        if (pkt != null) {
            return (String)pkt.getProp(
                ClusterInfoRequestInfo.STORE_SESSION_OWNER_PROP);
        }
        return null;
    }

    public UID getPartition() {
        assert ( pkt != null );
        Long v = (Long)pkt.getProp(
                 ClusterInfoRequestInfo.PARTITION_PROP);
        if (v == null) {
            return null;
        }
        return new UID(v.longValue());
    }

    public String toString() {
        GPacket p = (pkt == null ? gp:pkt);
        if (p == null) {
            return "[]";
        }
        return "[infoType="+p.getProp("T")+", "+p.propsEntrySet()+"]";
    }
}
