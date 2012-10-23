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
 * @(#)ClusterImpl.java	1.18 07/02/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.standalone;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.multibroker.ClusterCallback;
import com.sun.messaging.jmq.jmsserver.multibroker.Cluster;
import com.sun.messaging.jmq.jmsserver.cluster.api.FileTransferCallback;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.config.ConfigListener;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;


/**
 * This class implements the 'standalone' topology.
 */
public class ClusterImpl implements Cluster, ConfigListener {
    ClusterCallback cb = null;
    private BrokerAddressImpl self;

    /**
     * Creates and initializes a topology manager for the two broker
     * topology using the broker configuration.
     */
    public ClusterImpl() {
        self = new BrokerAddressImpl();
    }

    public void setCallback(ClusterCallback cb) {
        this.cb = cb;
    }

    public void useGPackets(boolean useGPackets) {
    }

    public void setMatchProps(Properties matchProps) {
    }

    public void start() {
    }

    public void shutdown(boolean force, BrokerAddress excludedBroker) {
    }

    public void closeLink(BrokerAddress remote, boolean force) {
    }

    public boolean isReachable(BrokerAddress remote, int timeout) throws IOException {
        return true;
    }

    public BrokerAddress getSelfAddress() {
        return (BrokerAddress) self;
    }

    public BrokerAddress getConfigServer() throws BrokerException {
        return null;
    }

    public void marshalBrokerAddress(BrokerAddress ddr, GPacket gp) {
    }

    public BrokerAddress unmarshalBrokerAddress(GPacket gp) throws Exception {
        return null;
    }
 

    public void stopMessageFlow() throws IOException {
    }

    public void resumeMessageFlow() throws IOException {
    }

    public void unicastAndClose(BrokerAddress addr, GPacket gp) throws IOException {
        if (cb != null) cb.receiveUnicast(self, gp);
    }

    public void unicast(BrokerAddress addr, GPacket gp, boolean flowControl)
        throws IOException {
        if (cb != null) cb.receiveUnicast(self, gp);
    }

    public void unicastUrgent(BrokerAddress addr, GPacket gp)
        throws IOException {
        if (cb != null) cb.receiveUnicast(self, gp);
    }

    public void unicast(BrokerAddress addr, GPacket gp) throws IOException {
        unicast(addr, gp, false);
    }

    public void broadcast(GPacket gp) throws IOException {
    }

    public void unicast(BrokerAddress addr, int destId, byte[] pkt,
        boolean flowControl) throws IOException {
        if (cb != null)
            cb.receiveUnicast(self, destId, pkt);
    }

    public void unicast(BrokerAddress addr, int destId, byte[] pkt)
        throws IOException {
        unicast(addr, destId, pkt, false);
    }

    public void broadcast(int destId, byte[] pkt) {
    }

    public boolean election(int electionId, byte[] params) {
        return true;
    }

    public void reloadCluster() {
    }

    public Hashtable getDebugState() {
        return new Hashtable();
	}

    /**
     * Dynamic configuration property validation..
     */
    public void validate(String name, String value)
        throws PropertyUpdateException {
    }

    /**
     * Dynamic configuration property updation..
     */
    public boolean update(String name, String value) {
        return true;
    }
    public void changeMasterBroker(BrokerAddress newmater, BrokerAddress oldmaster)
    throws BrokerException { 
    }

    public void receivedFileTransferRequest(BrokerAddress from, String uuid) {
    }

    public void transferFiles(String[] fileNames, BrokerAddress targetBroker,
                              Long syncTimeout, String uuid, String myBrokerID,
                              String module, FileTransferCallback callback)
                              throws BrokerException { 
    }
}

/*
 * EOF
 */
