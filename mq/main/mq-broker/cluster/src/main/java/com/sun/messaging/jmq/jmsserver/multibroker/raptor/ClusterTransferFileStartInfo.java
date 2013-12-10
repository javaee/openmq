/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2011 Oracle and/or its affiliates. All rights reserved.
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

public class ClusterTransferFileStartInfo 
{
    private static boolean DEBUG = false;

    private GPacket pkt = null;

    private String uuid = null;
    private String module = null;  
    private String brokerID = null;
    private String filename = null;
    private long filesize = 0L;
    private long lastmodtime = 0L;

    private ClusterTransferFileStartInfo(String uuid, String module,
                                         String brokerID, String filename,
                                         long filesize, long lastmodtime) {
        this.uuid = uuid;
        this.brokerID = brokerID;
        this.module = module;
        this.filename = filename;
        this.filesize = filesize;
        this.lastmodtime = lastmodtime;
    }

    private ClusterTransferFileStartInfo(GPacket pkt) {
        assert ( pkt.getType() == ProtocolGlobals.G_TRANSFER_FILE_START );
        this.pkt = pkt;
    }

    /**
     */
    public static ClusterTransferFileStartInfo newInstance(String uuid, String module,
                                                    String brokerID, String filename,
                                                    long filesize, long lastmodtime) {
        return new ClusterTransferFileStartInfo(uuid, module, brokerID, filename,
                                                filesize, lastmodtime);
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterTransferFileStartInfo newInstance(GPacket pkt) {
        return new ClusterTransferFileStartInfo(pkt);
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
        gp.putProp("filesize", Long.valueOf(filesize));
        gp.putProp("lastModifiedTime", Long.valueOf(lastmodtime));
        gp.setType(ProtocolGlobals.G_TRANSFER_FILE_START);
        gp.setBit(gp.A_BIT, false);
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

    public long getFileSize() {
        assert ( pkt != null );
        return ((Long)pkt.getProp("filesize")).longValue();
    }

    public long getLastModifiedTime() {
        assert ( pkt != null );
        return ((Long)pkt.getProp("lastModifiedTime")).longValue();
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean verbose) {
        if (pkt != null) {
            return "[brokerID="+getBrokerID()+", file="+getFileName()+"]"+
                    getUUID()+(verbose ? "("+getModule()+")":"");
        }
        return "[brokerID="+brokerID+", file="+filename+"]"+uuid+(verbose ? "("+module+")":"");
    }

}
