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
 * @(#)DestinationUpdateHandler.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor.handlers;

import java.io.*;
import java.util.Hashtable;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.*;
import com.sun.messaging.jmq.jmsserver.multibroker.MessageBusCallback;

public class DestinationUpdateHandler extends GPacketHandler {

    private DestinationList DL = Globals.getDestinationList();

    public DestinationUpdateHandler(RaptorProtocol p) {
        super(p);
    }

    public void handle(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        if (pkt.getType() == ProtocolGlobals.G_UPDATE_DESTINATION) {
            handleUpdateDestination(cb, sender, pkt);
        }
        else if (pkt.getType() == ProtocolGlobals.G_REM_DESTINATION) {
            handleRemDestination(cb, sender, pkt);
        }
        else if (pkt.getType() == ProtocolGlobals.G_UPDATE_DESTINATION_REPLY ||
            pkt.getType() == ProtocolGlobals.G_REM_DESTINATION_REPLY) {
            handleReply(sender, pkt);
        }
        else {
            logger.log(logger.WARNING, "DestinationUpdateHandler " +
                "Internal error : Cannot handle this packet :" +
                pkt.toLongString());
        }
    }

    public void handleUpdateDestination(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        ClusterDestInfo cdi = ClusterDestInfo.newInstance(pkt);

        try {
            DestinationUID duid = cdi.getDestUID(); 
            Hashtable props = cdi.getDestProps();

            Destination[] ds = DL.getDestination(null, duid);
            Destination d = ds[0];
            if (d == null) {
                ds = DL.createDestination(null, cdi.getDestName(), cdi.getDestType(),
                    ! DestType.isTemporary(cdi.getDestType()), false, selfAddress);
                d = ds[0];
                d.setDestinationProperties(props);
                cb.notifyCreateDestination(d);
            }
            else {
                cb.notifyUpdateDestination(duid, props);
            }
            if (cdi.getShareccInfo() != null) {
                cb.setLastReceivedChangeRecord(sender, cdi.getShareccInfo());
            }
        }
        catch (Exception e) {
            logger.logStack(logger.INFO,
                "Internal Exception, unable to process message " +
                pkt, e);
            return;
        }
    }

    public void handleRemDestination(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        try {
            ClusterDestInfo cdi = ClusterDestInfo.newInstance(pkt);
            DestinationUID duid = cdi.getDestUID(); 

            cb.notifyDestroyDestination(duid);
            if (cdi.getShareccInfo() != null) {
                cb.setLastReceivedChangeRecord(sender, cdi.getShareccInfo());
            }
        }
        catch (Exception e) {
            logger.logStack(logger.INFO,
                "Internal Exception, unable to process message " +
                pkt, e);
            return;
        }
    }

    public void handleReply(BrokerAddress sender, GPacket pkt) {
    }
}


/*
 * EOF
 */
