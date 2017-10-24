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
 * @(#)ClusterTakeoverInfo.java	1.12 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusteredBroker;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAClusteredBroker;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * An instance of this class is intended to be used one direction only
 */

public class ClusterTakeoverInfo 
{
    private static boolean DEBUG = false;

    private GPacket pkt = null;

    private String brokerID = null;
    private UID storeSession = null;
    private UID brokerSession = null;
    private String brokerHost = null;
    private Long xid =   null;
    //if issued or pkt originated from sender
    private boolean fromTaker = true; 
    private String taker = null;
    private Long timestamp = null;
    private boolean timedout = false;

    private ClusterTakeoverInfo(String brokerID, UID storeSession, 
                                String brokerHost, UID brokerSession,
                                Long xid, boolean fromTaker, boolean timedout) {
        this.brokerID = brokerID;
        this.storeSession = storeSession;
        this.brokerHost = brokerHost;
        this.brokerSession = brokerSession;
        this.xid = xid;
        this.fromTaker = fromTaker;
        this.timedout = timedout;
    }

    private ClusterTakeoverInfo(GPacket pkt) {
        assert ( pkt.getType() == ProtocolGlobals.G_TAKEOVER_COMPLETE ||
                 pkt.getType() == ProtocolGlobals.G_TAKEOVER_PENDING ||
                 pkt.getType() == ProtocolGlobals.G_TAKEOVER_ABORT );

        this.pkt = pkt;
        brokerID = (String)pkt.getProp("brokerID");
        storeSession = new UID(((Long)pkt.getProp("storeSession")).longValue());
        Long v = (Long)pkt.getProp("brokerSession");
        if (v != null) brokerSession = new UID(v.longValue());
        brokerHost =(String)pkt.getProp("brokerHost");
        taker = (String)pkt.getProp("taker");
        fromTaker = (taker != null);
        timestamp = (Long)pkt.getProp("timestamp");
        if (timestamp != null) {
            timedout = (timestamp.equals(Long.valueOf(0)));
        }
        xid = (Long)pkt.getProp("X");
    }

    /**
     */
    public static ClusterTakeoverInfo newInstance(String brokerID, UID storeSession) {
        return new ClusterTakeoverInfo(brokerID, storeSession, null, null, null, false, false);
    }

    public static ClusterTakeoverInfo newInstance(String brokerID, UID storeSession, 
                                                  String brokerHost, UID brokerSession,
                                                  Long xid, boolean fromTaker) {
        return new ClusterTakeoverInfo(brokerID, storeSession, brokerHost,
                                       brokerSession, xid, fromTaker, false);
    }

    public static ClusterTakeoverInfo newInstance(String brokerID, UID storeSession, 
                                        String brokerHost, UID brokerSession, Long xid,
                                        boolean fromTaker, boolean timedout) {
        return new ClusterTakeoverInfo(brokerID, storeSession, brokerHost,
                                       brokerSession, xid, fromTaker, timedout);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterTakeoverInfo newInstance(GPacket pkt) {
        return new ClusterTakeoverInfo(pkt);
    }

    public GPacket getGPacket(short protocol) throws BrokerException { 
//        assert ( protocol == ProtocolGlobals.G_TAKEOVER_COMPLETE ||
//                 protocol == ProtocolGlobals.G_TAKEOVER_PENDING ||
//                 protocol == ProtocolGlobals.G_TAKEOVER_ABORT );
        if (!Globals.getHAEnabled() && !Globals.isBDBStore()) {
            throw new BrokerException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.E_INTERNAL_BROKER_ERROR,
                    "Broker is not running in HA mode"));
        }

        if (pkt != null) {
            assert ( pkt.getType() == protocol );
           return pkt;
        }

        GPacket gp = GPacket.getInstance();
        gp.putProp("brokerID", brokerID);
        gp.putProp("storeSession", Long.valueOf(storeSession.longValue()));

        if (protocol == ProtocolGlobals.G_TAKEOVER_COMPLETE) {
            gp.setType(protocol);
            gp.setBit(gp.A_BIT, false);
            return gp;
        }

        HAClusteredBroker cb = (HAClusteredBroker)Globals.getClusterManager().getLocalBroker();
        if (protocol == ProtocolGlobals.G_TAKEOVER_PENDING) {
            gp.setType(ProtocolGlobals.G_TAKEOVER_PENDING);
            gp.setBit(gp.A_BIT, false);
            gp.putProp("brokerSession", Long.valueOf(brokerSession.longValue()));
            gp.putProp("brokerHost",  brokerHost);
            if (fromTaker) {
                taker = cb.getBrokerName();
                gp.putProp("taker", taker);
                gp.putProp("timestamp", Long.valueOf(cb.getHeartbeat()));
                gp.setBit(gp.A_BIT, true);
            } else if (timedout) { 
                gp.putProp("timestamp", Long.valueOf(0));
            }
            gp.putProp("X", xid);
            return gp;
        }

        if (protocol == ProtocolGlobals.G_TAKEOVER_ABORT) {
            gp.setType(ProtocolGlobals.G_TAKEOVER_ABORT);
            if (fromTaker) {
                gp.putProp("taker", cb.getBrokerName());
            }
            gp.setBit(gp.A_BIT, false);
            gp.putProp("X", xid);
            return gp;
        }
        throw new BrokerException("Unknown protocol: "+protocol);
    }

    public String getBrokerID() {
        return brokerID;
    }

    public UID getStoreSession() {
        return storeSession;
    }

    public Long getXid() {
        return xid;
    }

    public UID getBrokerSession() {
        return brokerSession;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public String getTaker() {
        return taker;
    }

    public Long getTimestamp() {
        assert (pkt != null);
        return timestamp;
    }

    public boolean isTimedout() {
        return timedout;
    }

    public boolean isFromTaker() {
        return fromTaker;
    }

    public String toString() {
        if (pkt == null) {
            return getTaker()+
                ":[brokerID="+getBrokerID()+", storeSession="+getStoreSession()+"]"+
                (getBrokerSession() == null ? "":"brokerSession="+getBrokerSession())+", xid="+xid;
        }          

        if (pkt.getType() == ProtocolGlobals.G_TAKEOVER_COMPLETE) {
            return "[brokerID="+getBrokerID()+", storeSession="+getStoreSession()+"]";
        }

        if (pkt.getType() == ProtocolGlobals.G_TAKEOVER_PENDING) {
        String prefix = "";

        if (DEBUG) {
        if (getTaker() != null) prefix = getTaker()+"("+ ((timestamp == null) ? "":""+timestamp)+":";
        return prefix+ "[brokerID="+getBrokerID()+
                       ", storeSession="+getStoreSession()+"]"+
                        (getBrokerSession() == null ? "":"brokerSession="+getBrokerSession())+
                        ", "+getBrokerHost()+", xid="+xid;
        }
        return prefix+ "[brokerID="+getBrokerID()+", storeSession="+getStoreSession()+"]"+
            (getBrokerSession() == null ? "":"brokerSession="+getBrokerSession())+", xid="+xid;
        }

        return getTaker()+":[brokerID="+getBrokerID()+", storeSession="+getStoreSession()+"]"+
            (getBrokerSession() == null ? "":"brokerSession="+getBrokerSession())+", xid="+xid;
    }

    public boolean needReply() {
        assert ( pkt != null );
        return pkt.getBit(pkt.A_BIT);
    }

    public GPacket getReplyGPacket(short protocol, int status, String reason) {
        assert ( pkt != null );
        assert ( protocol == ProtocolGlobals.G_TAKEOVER_PENDING_REPLY );

        GPacket gp = GPacket.getInstance();
        gp.setType(protocol);
        gp.putProp("X", (Long)pkt.getProp("X"));
        gp.putProp("brokerID", pkt.getProp("brokerID"));
        gp.putProp("storeSession", pkt.getProp("storeSession"));
        gp.putProp("taker", pkt.getProp("taker"));
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) gp.putProp("reason", reason);

        return gp;
    }

    public static int getReplyStatus(GPacket reply) {
        return ((Integer)reply.getProp("S")).intValue();
    }
    public static String getReplyStatusReason(GPacket reply) {
        return (String)reply.getProp("reason");
    }
    public static Long getReplyXid(GPacket reply) {
        return (Long)reply.getProp("X");
    }
    public static String toString(GPacket reply) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n\tstatus=").append(Status.getString(getReplyStatus(reply)));
        if (reply.getProp("reason") != null) {
        buf.append("\n\treason=").append(getReplyStatusReason(reply));
        }
        buf.append("[brokerID=").append((String)reply.getProp("brokerID"));
        buf.append(", storeSession=").append((Long)reply.getProp("storeSession"));
        buf.append("]xid=").append((Long)reply.getProp("X"));
        buf.append(", taker=").append((String)reply.getProp("taker"));
        return buf.toString();
    }
}
