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
 * @(#)HeartbeatInfo.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.heartbeat;

import java.io.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.core.BrokerMQAddress;

/**
 */
public class HeartbeatInfo { 

    public static final short HEARTBEAT_ALIVE = 1;
    public static final int HEARTBEAT_PROTOCOL_VERSION = 400;

    private String brokerID = null;
    private long brokerSession = 0;
    private BrokerMQAddress brokerAddress = null;
    private String toBrokerID = null;
    private long toBrokerSession = 0;
    private long sequence = 0;

    private GPacket pkt = null;

    private HeartbeatInfo(GPacket pkt) {
        this.pkt = pkt;
    }

    private HeartbeatInfo() {
    }

    public static HeartbeatInfo newInstance() {
        return new HeartbeatInfo();
    }

    public static HeartbeatInfo newInstance(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        GPacket pkt = GPacket.getInstance();
        pkt.read(bis);
        int ver = ((Integer)pkt.getProp("protocolVersion")).intValue();
        if (ver < HEARTBEAT_PROTOCOL_VERSION) {
            throw new IOException("Protocol version not supported:"+ver);
        }
        return new HeartbeatInfo(pkt);
    }

    public GPacket getGPacket() {
        GPacket gp = GPacket.getInstance();
        gp.generateSequenceNumber(false);
        gp.setType(HEARTBEAT_ALIVE);
        gp.setSequence(sequence);
        gp.putProp("protocolVersion", Integer.valueOf(HEARTBEAT_PROTOCOL_VERSION));
        gp.putProp("brokerID", brokerID);
        gp.putProp("brokerSession", Long.valueOf(brokerSession));
        gp.putProp("brokerAddress", brokerAddress.toString());
        gp.putProp("toBrokerID", toBrokerID);
        gp.putProp("toBrokerSession", Long.valueOf(toBrokerSession));
        return gp;
    }

    public static byte[] toByteArray(GPacket pkt) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            pkt.write(bos);
            bos.flush();
        }catch (Exception e) {}

        return bos.toByteArray();
    }

    public void setBrokerID(String id) {
        this.brokerID = id;
    }

    public void setBrokerSession(long uid) {
        this.brokerSession = uid;
    }

    public void setBrokerAddress(BrokerMQAddress ma) {
        this.brokerAddress = ma;
    }

    public void setToBrokerID(String id) {
        this.toBrokerID = id;
    }

    public void setToBrokerSession(long uid) {
        this.toBrokerSession = uid;
    }

    public void setSequence(long s) {
        this.sequence = s;
    }

    public String getBrokerID() {
        assert ( pkt != null ); 
        return (String)pkt.getProp("brokerID");
    }

    public long getBrokerSession() {
        assert ( pkt != null ); 
        return ((Long)pkt.getProp("brokerSession")).longValue();
    }

    public String getToBrokerID() {
        assert ( pkt != null ); 
        return (String)pkt.getProp("toBrokerID");
    }

    public long getToBrokerSession() {
        assert ( pkt != null ); 
        return ((Long)pkt.getProp("toBrokerSession")).longValue();
    }

    public long getSequence() {
        assert ( pkt != null ); 
        return pkt.getSequence();
    }

    public String toString() {
        if (pkt != null) {
            return "#"+getSequence()+" ["+getBrokerID()+","+getBrokerSession()+"] to " +
                   "["+getToBrokerID()+","+getToBrokerSession()+"]";
        } else {
            return "["+brokerID+","+brokerSession+"] to " +
                   "["+toBrokerID+","+toBrokerSession+"]";
        }
    }

}
