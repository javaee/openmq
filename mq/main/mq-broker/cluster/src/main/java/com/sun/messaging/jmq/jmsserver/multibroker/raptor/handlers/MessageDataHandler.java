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
 * @(#)MessageDataHandler.java	1.17 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor.handlers;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.util.lists.*;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.*;
import com.sun.messaging.jmq.jmsserver.multibroker.MessageBusCallback;

public class MessageDataHandler extends GPacketHandler {
    private static boolean DEBUG = false;

    public MessageDataHandler(RaptorProtocol p) {
        super(p);
    }

    public void handle(MessageBusCallback cb, BrokerAddress sender, GPacket gp) {
        if (DEBUG)
            logger.log(logger.DEBUG, "MessageDataHandler");

        if (gp.getType() == ProtocolGlobals.G_MESSAGE_DATA) {
            handleMessageData(cb, sender, gp);
        }
        else if (gp.getType() == ProtocolGlobals.G_MESSAGE_DATA_REPLY) {
            handleMessageDataReply(sender, gp);
        }
        else {
            logger.log(logger.WARNING, "MessageDataHandler " +
                "Internal error : Cannot handle this packet :" +
                gp.toLongString());
        }
    }

    public void handleMessageData(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {

        ClusterMessageInfo cmi =  ClusterMessageInfo.newInstance(pkt, c);
        boolean sendMsgDeliveredAck = cmi.getSendMessageDeliveredAck();

        LinkedHashMap<ConsumerUID, Integer> cuids = 
            new LinkedHashMap<ConsumerUID, Integer>();

        Packet roPkt;

        if (DEBUG) {
            logger.log(logger.DEBUGMED, "MessageBus: receiving message.");
        }


        try {
            cmi.initPayloadRead();
            Iterator itr = cmi.readPayloadConsumerUIDs();
            while (itr.hasNext()) {
                ConsumerUID intid = (ConsumerUID)itr.next();
                cuids.put(intid, cmi.getDeliveryCount(intid));
            }
            roPkt = cmi.readPayloadMessage();
            BrokerAddress home = cmi.getHomeBrokerAddress();
            if (home ==  null) {
                home = sender;
            }
            Long partitionid = cmi.getPartitionID();
            if (home != null && partitionid != null) {
                home.setStoreSessionUID(new UID(partitionid.longValue()));
            }
            cb.processRemoteMessage(roPkt, cuids, home, sendMsgDeliveredAck);
        } catch (Exception e) {
            logger.logStack(logger.ERROR,"Internal Exception, unable to process message " +
                       pkt, e);
        }

        if (cmi.needReply()) {
            try {
                c.unicast(sender, cmi.getReplyGPacket(ProtocolGlobals.G_SUCCESS));
            }
            catch (IOException e) {}
        }
    }


    public void handleMessageDataReply(BrokerAddress sender, GPacket gp) {
        logger.log(logger.DEBUG,
"MessageBus: Received reset G_MESSAGE_DATA_REPLY from {0} : STATUS = {1}",
            sender, ((Integer) gp.getProp("S")));
    }
}


/*
 * EOF
 */
