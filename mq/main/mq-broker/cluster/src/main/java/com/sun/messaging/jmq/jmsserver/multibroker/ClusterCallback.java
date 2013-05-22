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
 * @(#)ClusterCallback.java	1.17 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker;

import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * This interface defines a mechanism for receiving packets
 * from the broker cluster. Only the <code> MessageBus </code> class
 * implements this interface.
 */
public interface ClusterCallback {

    public int getHighestSupportedVersion();

    /**
     * Receive a unicast packet.
     * @param sender Address of the broker who sent this message.
     * @param pkt Packet.
     */
    public void receiveUnicast(BrokerAddress sender, GPacket pkt);

    /**
     * Receive a broadcast packet.
     * @param sender Address of the broker who sent this message.
     * @param pkt Packet.
     */
    public void receiveBroadcast(BrokerAddress sender, GPacket pkt);

    /**
     * Receive a unicast packet.
     * @param sender Address of the broker who sent this message.
     * @param destId Tells the this broker how this message
     * shoule be handled
     * @param pkt Packet data.
     */
    public void receiveUnicast(BrokerAddress sender, int destId, byte []pkt);

    /**
     * Receive a broadcast packet.
     * @param sender Address of the broker who sent this message.
     * @param destId Tells the this broker how this message
     * shoule be handled
     * @param pkt Packet data.
     */
    public void receiveBroadcast(BrokerAddress sender, int destId, byte []pkt);

    /**
     * Construct a BrokerInfo object that describes this broker.
     * This object is exchanged during initial handshake between
     * brokers.
     * @return BrokerInfo object describing the current state of the broker.
     */
    public BrokerInfo getBrokerInfo();

    /**
     */
    public ClusterBrokerInfoReply getBrokerInfoReply(BrokerInfo remote) throws Exception;

    public static final int ADD_BROKER_INFO_OK = 0;
    public static final int ADD_BROKER_INFO_RETRY = 1;
    public static final int ADD_BROKER_INFO_BAN = 2;

    /**
     * Add a new broker to the list of known brokers in this cluster.
     * This serves as a notification that a new broker has joined
     * the cluster so all the ongoing (unresolved) elections for
     * locking various resources must be repeated.
     *
     * @return false if the new broker is rejected due to some
     * state mismatch, otherwise true. If the return value is false,
     * the topology driver should forget all about the new broker
     * and let it retry the connection..
     */
    public int addBrokerInfo(BrokerInfo brokerInfo);

    /**
     * Remove a broker since it is no longer attached to this cluster.
     * This serves as a notification that a broker has left the cluster,
     * so all the interests local to that broker are no longer valid.
     *
     * @param broken link broken with IOException after handshake 
     *
     */
    public void removeBrokerInfo(BrokerAddress broker, boolean broken);

    /**
     * Synchronize cluster change record on remote broker join 
     */
    public void syncChangeRecordOnJoin(BrokerAddress broker,  ChangeRecordInfo cri)
    throws BrokerException;

    /**
     */
    public ChangeRecordInfo getLastStoredChangeRecord();
}

/*
 * EOF
 */
