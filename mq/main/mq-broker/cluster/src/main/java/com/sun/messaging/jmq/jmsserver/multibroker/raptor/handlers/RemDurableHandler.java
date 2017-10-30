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
 * @(#)RemDurableHandler.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor.handlers;

import java.io.*;
import java.util.Iterator;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.*;
import com.sun.messaging.jmq.jmsserver.multibroker.MessageBusCallback;

public class RemDurableHandler extends GPacketHandler {
    private static boolean DEBUG = false;

    public RemDurableHandler(RaptorProtocol p) {
        super(p);
    }

    public void handle(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        if (DEBUG)
            logger.log(logger.DEBUG, "RemDurableHandler");

        if (pkt.getType() == ProtocolGlobals.G_REM_DURABLE_INTEREST) {
            handleRemDurableInterest(cb, sender, pkt);
        }
        else if (pkt.getType() ==
            ProtocolGlobals.G_REM_DURABLE_INTEREST_REPLY) {
            handleRemDurableInterestAck(sender, pkt);
        }
        else {
            logger.log(logger.WARNING, "RemDurableHandler " +
                "Internal error : Cannot handle this packet :" +
                pkt.toLongString());
        }
    }

    public void handleRemDurableInterest(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {

        ClusterSubscriptionInfo csi = ClusterSubscriptionInfo.newInstance(pkt);

        if (p.getConfigSyncComplete() == false && !csi.isConfigSyncResponse()) {
            // Do not accept the normal interest updates before
            // config sync is complete. Here is 
            if (DEBUG) {
                logger.log(logger.DEBUG,
                    "MessageBus: Dropping the G_REM_DURABLE_INTEREST " +
                    "packet from {0}. Not ready yet.", sender);
            }
            return;
        }

        Iterator itr = csi.getSubscriptions();
        try {
            int i = 0;
            ChangeRecordInfo lastcri = null;
            while(itr.hasNext()) {
                i++;
                Subscription intr = (Subscription)itr.next();
                if (intr != null) {
                    cb.unsubscribe(intr);
                }
                ChangeRecordInfo cri = csi.getShareccInfo(i);
                if (cri != null) {
                    if (lastcri == null) {
                        lastcri = cri;
                    } else if (cri.getSeq().longValue()
                               > lastcri.getSeq().longValue()) {
                        lastcri = cri;
                    }
                }
            }
            if (lastcri != null) {
                cb.setLastReceivedChangeRecord(sender, lastcri);
            }
        } catch (Exception e) { 
           logger.logStack(logger.DEBUG,"Exception processing packet ", e);
        } 

        if (csi.needReply()) {
            GPacket gp = ClusterSubscriptionInfo.getReplyGPacket(
                                                ProtocolGlobals.G_REM_DURABLE_INTEREST_REPLY,
                                                ProtocolGlobals.G_SUCCESS);
            try {
                c.unicast(sender, gp);
            }
            catch (IOException e) {}
        }
    }

    private void handleRemDurableInterestAck(BrokerAddress sender,
        GPacket pkt) {
        logger.log(logger.DEBUG,
            "MessageBus: Received G_REM_DURABLE_INTEREST_REPLY " +
            "from {0} : STATUS = {1}",
            sender, ((Integer) pkt.getProp("S")));
    }
}


/*
 * EOF
 */
