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
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * A general cluster info request protocol 
 */

public class ClusterInfoRequestInfo 
{
    public static final int STORE_SESSION_OWNER_TYPE = 0x00000001; 
    public static final int PARTITION_ADDED_TYPE     = 0x00000002;

    //properties for STORE_SESSION_OWNER request
    public static final String STORE_SESSION_PROP  = "storeSession"; 
    public static final String STORE_SESSION_OWNER_PROP  = "storeSessionOwner"; 

    //properties for PARTITION_ADDED
    public static final String PARTITION_PROP  = "partition";

    private GPacket gp = null;  //out going
    private GPacket pkt = null; //in coming

    private Long xid = null;

    private ClusterInfoRequestInfo(Long xid) {
        this.xid = xid;
    }


    private ClusterInfoRequestInfo(GPacket pkt) {
        this.pkt = pkt;
    }

    public static ClusterInfoRequestInfo newInstance(Long xid) {
        return new ClusterInfoRequestInfo(xid); 
    }

    public void storeSessionOwnerRequest(long storeSession) 
    throws BrokerException { 
        if (gp == null) {
            gp = GPacket.getInstance();
            gp.setType(ProtocolGlobals.G_INFO_REQUEST);
            gp.putProp("X", xid);
        }
        Integer v = (Integer)gp.getProp("T");
	if (v == null) {
            gp.putProp("T", Integer.valueOf(STORE_SESSION_OWNER_TYPE));
	} else {
            if ((v.intValue() & STORE_SESSION_OWNER_TYPE) == 
                STORE_SESSION_OWNER_TYPE) {
                throw new BrokerException(
                "Internal Error: only 1 "+STORE_SESSION_OWNER_TYPE+
                " type info request is allowed");
                
            }
            int t = (v.intValue() | STORE_SESSION_OWNER_TYPE);
            gp.putProp("T", Integer.valueOf(t));
	}
        gp.putProp(STORE_SESSION_PROP, Long.valueOf(storeSession));
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterInfoRequestInfo newInstance(GPacket pkt) {
        return new ClusterInfoRequestInfo(pkt);
    }

    public GPacket getGPacket() { 
        return gp;
    }

    public ClusterInfoInfo getReply(int status, String reason, Object info) {
        assert ( pkt != null );
        ClusterInfoInfo cii = ClusterInfoInfo.newInstance();
        if (ClusterInfoInfo.isStoreSessionOwnerInfo(pkt)) {
            cii.storeSessionOwnerRequestReply(this, status, reason, (String)info);
        }
        return cii;
    }

    public long getStoreSession() {
        assert ( pkt != null );
        return ((Long)pkt.getProp(STORE_SESSION_PROP)).longValue();
    }

    public Long getXid() {
        assert ( pkt != null );
        return  (Long)pkt.getProp("X");
    }

    public String toString() {
        GPacket p = (pkt == null ? gp:pkt);
        if (p == null) {
            return "[]";
        }
        return "[requestType="+p.getProp("T")+", "+p.propsEntrySet()+"]";
    }
}
