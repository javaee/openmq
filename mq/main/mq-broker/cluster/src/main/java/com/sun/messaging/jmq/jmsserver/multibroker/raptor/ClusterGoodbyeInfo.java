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
 * @(#)ClusterGoodbyeInfo.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusteredBroker;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAClusteredBroker;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;

/**
 * An instance of this class is intended to be used one direction only
 */

public class ClusterGoodbyeInfo 
{
    private boolean requestTakeover = false;
    private Cluster c = null;

    private GPacket pkt = null;
    private BrokerAddress sender = null;

    private ClusterGoodbyeInfo(boolean requestTakeover, Cluster c) {
        this.requestTakeover = requestTakeover;
        this.c = c;
    }

    private ClusterGoodbyeInfo(GPacket pkt, Cluster c) throws Exception {
        assert (pkt.getType() == ProtocolGlobals.G_GOODBYE );

        this.pkt = pkt;
        this.c = c;

        sender = c.unmarshalBrokerAddress(pkt);
        if (sender.getHAEnabled()) {
            requestTakeover = ((Boolean)pkt.getProp("requestTakeover")).booleanValue();
        }
    }

    /**
     */
    public static ClusterGoodbyeInfo newInstance(boolean requestTakeover, Cluster c) {
        return new ClusterGoodbyeInfo(requestTakeover, c);
    }

    /**
     */
    public static ClusterGoodbyeInfo newInstance(Cluster c) {
        return new ClusterGoodbyeInfo(false, c);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterGoodbyeInfo newInstance(GPacket pkt, Cluster c) throws Exception {
        return new ClusterGoodbyeInfo(pkt, c);
    }

    public GPacket getGPacket() { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_GOODBYE);
        gp.setBit(gp.A_BIT, true);
        c.marshalBrokerAddress(c.getSelfAddress(), gp);
        if (c.getSelfAddress().getHAEnabled()) {
            gp.putProp("requestTakeover", Boolean.valueOf(requestTakeover));
        }

        return gp;
    }

    public boolean getRequestTakeover() {
        assert ( pkt != null );
        Boolean b = (Boolean)pkt.getProp("requestTakeover");
        if (b == null) return false;
        return b.booleanValue();
    }


    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public String toString() {
        if (pkt == null) {
            if (Globals.getHAEnabled()) {
                return "requestTakeover="+requestTakeover+" "+c.getSelfAddress().toString();
            }
            return  c.getSelfAddress().toString();
        }

        if (sender.getHAEnabled()) {
            return "requestTakeover="+requestTakeover+" "+sender.toString();
        }
        return sender.toString();
    }

    public static GPacket getReplyGPacket(int status) {
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_GOODBYE_REPLY);
        gp.putProp("S", Integer.valueOf(status));
        return gp;
    }
}
