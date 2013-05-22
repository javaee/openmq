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
 * @(#)ClusterDestInfo.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.io.PacketProperties;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;

/**
 * An instance of this class is intended to be used one direction only
 * either Destination -> GPacket or GPacket -> Destination (see assertions)
 */

public class ClusterDestInfo 
{
    private Destination d = null;

    private GPacket pkt = null;
    private String destName = null;
    private int destType = -1;
    private ChangeRecordInfo shareccInfo = null;

    private ClusterDestInfo(Destination d) {
        this.d = d;
    }

    private ClusterDestInfo(GPacket pkt) {
        assert (pkt.getType() == ProtocolGlobals.G_REM_DESTINATION || 
                pkt.getType() == ProtocolGlobals.G_UPDATE_DESTINATION);

        this.pkt = pkt;
        destName = (String) pkt.getProp("N");
        destType = ((Integer) pkt.getProp("DT")).intValue();
        if (pkt.getProp("shareccSeq") != null) {
            shareccInfo =  new ChangeRecordInfo();
            shareccInfo.setSeq((Long)pkt.getProp("shareccSeq"));
            shareccInfo.setUUID((String)pkt.getProp("shareccUUID"));
            shareccInfo.setResetUUID((String)pkt.getProp("shareccResetUUID"));
            shareccInfo.setType(pkt.getType());
        }
    }

    /**
     * Destination to GPacket
     *
     * @param d The Destination to be marshaled to GPacket
     */
    public static ClusterDestInfo newInstance(Destination d) {
        return new ClusterDestInfo(d);
    }

    /**
     * GPacket to Destination 
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterDestInfo newInstance(GPacket pkt) {
        return new ClusterDestInfo(pkt);
    }

    public GPacket getGPacket(short protocol, boolean changeRecord) { 
        assert (d !=  null);
        assert (protocol == ProtocolGlobals.G_REM_DESTINATION || 
                protocol == ProtocolGlobals.G_UPDATE_DESTINATION);
        GPacket gp = GPacket.getInstance();
        gp.setType(protocol);
        gp.putProp("N", d.getDestinationName());
        gp.putProp("DT", new Integer(d.getType()));

        switch (protocol) {
           case ProtocolGlobals.G_REM_DESTINATION:
           ChangeRecordInfo cri = d.getCurrentChangeRecordInfo(
                                  ProtocolGlobals.G_REM_DESTINATION);
           if (cri != null) {
               gp.putProp("shareccSeq", cri.getSeq());
               gp.putProp("shareccUUID", cri.getUUID());
               gp.putProp("shareccResetUUID", cri.getResetUUID());
           }
           
           break;

           case ProtocolGlobals.G_UPDATE_DESTINATION:

           cri = d.getCurrentChangeRecordInfo(
                      ProtocolGlobals.G_UPDATE_DESTINATION);
           if (cri != null) {
               gp.putProp("shareccSeq", cri.getSeq());
               gp.putProp("shareccUUID", cri.getUUID());
               gp.putProp("shareccResetUUID", cri.getResetUUID());
           }

           if (DestType.isTemporary(d.getType())) {
               ConnectionUID cuid = d.getConnectionUID();
               if (cuid != null) {
                   gp.putProp("connectionUID", new Long(cuid.longValue()));
               }
           }

           HashMap props = d.getDestinationProperties();
           if (props == null) props = new HashMap();
           ByteArrayOutputStream bos = new ByteArrayOutputStream();
           try {
               PacketProperties.write(props, bos);
               bos.flush();
           }
           catch (IOException e) { /* Ignore */ }

           byte[] buf = bos.toByteArray();
           gp.setPayload(ByteBuffer.wrap(buf));
           break;

        }
        if (changeRecord) gp.putProp("M", Boolean.valueOf(true));

        return gp;
    }

    public DestinationUID getDestUID() throws BrokerException {
        assert (destName != null);
        return DestinationUID.getUID(destName, DestType.isQueue(destType));
    }

    public int getDestType() {
        assert (pkt != null);
        return destType;
    }

    public String getDestName() {
        assert (pkt != null);
        return destName;
    }

    public ChangeRecordInfo getShareccInfo() {
        return shareccInfo;
    }

    public Hashtable getDestProps() throws IOException, ClassNotFoundException {
        assert (pkt != null); 
        ByteArrayInputStream bis = new ByteArrayInputStream(pkt.getPayload().array());
        return PacketProperties.parseProperties(bis);
    }

}
