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
 */

public class ClusterTransferFileEndInfo 
{
    private static boolean DEBUG = false;

    private GPacket pkt = null;

    private String uuid = null;
    private String module = null;  
    private String brokerID = null;
    private String filename = null;
    private byte[] digest = null;
    private boolean moreFiles = false;

    private ClusterTransferFileEndInfo(String uuid, String module,
                                         String brokerID, String filename,
                                         byte[] digest, boolean morefiles) {
        this.uuid = uuid;
        this.brokerID = brokerID;
        this.module = module;
        this.filename = filename;
        this.digest = digest;
        this.moreFiles = morefiles;
    }

    private ClusterTransferFileEndInfo(GPacket pkt) {
        assert ( pkt.getType() == ProtocolGlobals.G_TRANSFER_FILE_END );
        this.pkt = pkt;
    }

    /**
     */
    public static ClusterTransferFileEndInfo newInstance(String uuid, String module,
                                                    String brokerID, String filename,
                                                    byte[] digest, boolean morefiles) {
        return new ClusterTransferFileEndInfo(uuid, module, brokerID,
                                                filename, digest, morefiles);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterTransferFileEndInfo newInstance(GPacket pkt) {
        return new ClusterTransferFileEndInfo(pkt);
    }

    public GPacket getGPacket() throws BrokerException { 
        if (pkt != null) {
           return pkt;
        }

        GPacket gp = GPacket.getInstance();
        gp.putProp("uuid", uuid);
        gp.putProp("module", module);
        gp.putProp("brokerID", brokerID);
        gp.putProp("filename", filename);
        gp.putProp("morefiles", moreFiles);
        gp.setType(ProtocolGlobals.G_TRANSFER_FILE_END);
		gp.setPayload(ByteBuffer.wrap(digest));
        gp.setBit(gp.A_BIT, true);
        return gp;
    }

    public String getUUID() {
        assert ( pkt != null );
        return (String)pkt.getProp("uuid");
    }

    public String getModule() {
        assert ( pkt != null );
        return (String)pkt.getProp("module");
    }

    public String getBrokerID() {
        assert ( pkt != null );
        return (String)pkt.getProp("brokerID");
    }

    public String getFileName() {
        assert ( pkt != null );
        return (String)pkt.getProp("filename");
    }

    public boolean hasMoreFiles() {
        assert ( pkt != null );
        return ((Boolean)pkt.getProp("morefiles")).booleanValue();
    }

    public byte[] getDigest() {
        assert ( pkt != null );
        byte[] buf = null;
        if (pkt.getPayload() != null) {
            buf = pkt.getPayload().array();
        }
        return buf;
    }

    public static GPacket getReplyGPacket(int status, String reason) {
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_TRANSFER_FILE_END_ACK);
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) {
            gp.putProp("reason", reason);
        }
        return gp;
    }

    public static GPacket getReplyAckGPacket(int status, String reason) {
        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_TRANSFER_FILE_END_ACK_ACK);
        gp.putProp("S", Integer.valueOf(status));
        if (reason != null) {
            gp.putProp("reason", reason);
        }
        return gp;
    }

    public static int getReplyStatus(GPacket gp) {
        return ((Integer)gp.getProp("S")).intValue();
    }

    public static String getReplyStatusReason(GPacket gp) {
        return (String)gp.getProp("reason");
    }

    public String toString() {
        if (pkt != null) {
            return "[brokerID="+getBrokerID()+", file="+getFileName()+"]"+getUUID()+"("+hasMoreFiles()+")";
        }
        return "[brokerID="+brokerID+", file="+filename+"]"+uuid+"("+moreFiles+")";
    }
}
