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
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;

/**
 * A general cluster first info packet after link handshake 
 */

public class ClusterFirstInfoInfo 
{

    private ChangeRecordInfo lastStoredChangeRecord = null;

    private GPacket gp = null;  //out going
    private GPacket pkt = null; //in coming

    private ClusterFirstInfoInfo() {
    }

    private ClusterFirstInfoInfo(GPacket pkt) {
        this.pkt = pkt;
    }

    public static ClusterFirstInfoInfo newInstance() {
        return new ClusterFirstInfoInfo(); 
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterFirstInfoInfo newInstance(GPacket pkt) {
        return new ClusterFirstInfoInfo(pkt);
    }

    public void setLastStoredChangeRecord(ChangeRecordInfo cri) {
        if (gp == null) {
            gp = GPacket.getInstance();
            gp.setType(ProtocolGlobals.G_FIRST_INFO);
        }
        gp.putProp("shareccLastStoredSeq", cri.getSeq());
        gp.putProp("shareccLastStoredUUID", cri.getUUID());
        gp.putProp("shareccLastStoredResetUUID", cri.getResetUUID());
        gp.putProp("shareccLastStoredType", new Integer(cri.getType()));
    }

    public void setBroadcast(boolean b) {
        assert ( gp != null );
        if (b) { 
            gp.setBit(gp.B_BIT, true); 
        }
    }

    public GPacket getGPacket() { 
        gp.setBit(gp.A_BIT, false);
        return gp;
    }

    public ChangeRecordInfo getLastStoredChangeRecord() {
        assert ( pkt != null );

        if (pkt.getProp("shareccLastStoredSeq") == null) {
            return null;
        }

        ChangeRecordInfo cri = new ChangeRecordInfo();
        cri.setSeq((Long)pkt.getProp("shareccLastStoredSeq"));
        cri.setUUID((String)pkt.getProp("shareccLastStoredUUID"));
        cri.setResetUUID((String)pkt.getProp("shareccLastStoredResetUUID"));
        cri.setType(((Integer)pkt.getProp("shareccLastStoredType")).intValue());
        return cri;
    }

}
