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
 * @(#)ClusterBrokerInfoReply.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker;

import java.io.*;
import java.util.*;
import java.nio.*;

import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;
import com.sun.messaging.jmq.util.io.FilteringObjectInputStream;

/**
 * BROKER_INFO_REPLY
 */

public class ClusterBrokerInfoReply 
{
    private static boolean DEBUG = false;

    private BrokerInfo brokerInfo = null;
    private int status = ProtocolGlobals.G_BROKER_INFO_OK;

    private GPacket pkt = null;

    private ClusterBrokerInfoReply(BrokerInfo bi, int status) {
        this.brokerInfo = bi;
        this.status = status;
    }

    private ClusterBrokerInfoReply(GPacket pkt) throws Exception {

        assert ( pkt.getType() == ProtocolGlobals.G_BROKER_INFO_REPLY );

        this.pkt = pkt;
        status = ((Integer)pkt.getProp("S")).intValue(); 

        ByteArrayInputStream bis = new ByteArrayInputStream(pkt.getPayload().array());
        ObjectInputStream ois = new FilteringObjectInputStream(bis);
        brokerInfo = (BrokerInfo) ois.readObject();
 
    }

    /**
     */
    public static ClusterBrokerInfoReply newInstance(BrokerInfo bi, int status) {
        return new ClusterBrokerInfoReply(bi, status);
    }

    /**
     */
    public static ClusterBrokerInfoReply newInstance(GPacket pkt) throws Exception {
        return new ClusterBrokerInfoReply(pkt);
    }

    public GPacket getGPacket() throws Exception { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_BROKER_INFO_REPLY);
        gp.setBit(pkt.A_BIT, false);
        gp.putProp("S", Integer.valueOf(status));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(brokerInfo);
        oos.flush();
        oos.close();

        byte[] buf = bos.toByteArray();
        gp.setPayload(ByteBuffer.wrap(buf));
        return gp;
    }

    public int getStatus() {
        return status;
    }

    public BrokerInfo getBrokerInfo() {
        return brokerInfo;
    }

    public boolean isTakingover() {
        return (getStatus() == ProtocolGlobals.G_BROKER_INFO_TAKINGOVER);
    }

    public boolean sendAndClose() {
        return isTakingover();
    }
}
