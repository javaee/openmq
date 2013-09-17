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

package com.sun.messaging.jmq.jmsserver.multibroker;

import java.io.IOException;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ClusterSubscriptionInfo;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;

public class InterestUpdateChangeRecord extends ChangeRecord {
    private String dname;
    private String cid;
    //only for G_NEW_INTEREST
    private Boolean shared = null;
    private Boolean jmsshared = null;
    private BrokerAddress broker = null;

    public InterestUpdateChangeRecord(GPacket gp) {
        operation = gp.getType();

        ClusterSubscriptionInfo csi = ClusterSubscriptionInfo.newInstance(gp);
        dname = csi.getDurableName();
        cid = csi.getClientID();
        shared = csi.getShared();
        jmsshared = csi.getJMSShared();
    }

    public String getSubscriptionKey() {  
        return Subscription.getDSubKey(cid, dname);
    }

    @Override
    public String getUniqueKey() {
        return "dur:"+Subscription.getDSubKey(cid, dname);
    }

    public Boolean getShared() {
        return shared;
    }

    public Boolean getJMSShared() {
        return jmsshared;
    }

    @Override
    public boolean isAddOp() {
        return (operation == ProtocolGlobals.G_NEW_INTEREST);
    }

    @Override
    public void transferFlag(ChangeRecordInfo cri) {
        if (shared != null && shared.booleanValue()) {
            cri.setFlagBit(ChangeRecordInfo.SHARED);
        }
        if (jmsshared != null && jmsshared.booleanValue()) {
            cri.setFlagBit(ChangeRecordInfo.JMSSHARED);
        }
    }

    public String getFlagString() {
        ChangeRecordInfo cri = new ChangeRecordInfo();
        transferFlag(cri);
        return cri.getFlagString(cri.getFlag());
    }

    public void setBroker(BrokerAddress b) {
        broker = b;
    }
    public BrokerAddress getBroker() {
        return broker;
    }
}
