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
 * @(#)InterestUpdateHandler.java	1.9 07/23/07
 */ 
 
package com.sun.messaging.jmq.jmsserver.multibroker.raptor.handlers;

import java.io.*;
import java.util.Iterator;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.*;
import com.sun.messaging.jmq.jmsserver.multibroker.MessageBusCallback;

public class InterestUpdateHandler extends GPacketHandler {
    private static boolean DEBUG = false;

    public InterestUpdateHandler(RaptorProtocol p) {
        super(p);
    }

    public void handle(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        if (DEBUG)
            logger.log(logger.DEBUG, "InterestUpdateHandler");

        if (pkt.getType() == ProtocolGlobals.G_INTEREST_UPDATE) {
            handleInterestUpdate(cb, sender, pkt);
        }
        else if (pkt.getType() == ProtocolGlobals.G_INTEREST_UPDATE_REPLY) {
            handleInterestUpdateReply(sender, pkt);
        }
        else {
            logger.log(logger.WARNING, "InterestUpdateHandler " +
            "Internal error : Cannot handle this packet :" +
            pkt.toLongString());
        }
    }

    private void handleInterestUpdate(MessageBusCallback cb, BrokerAddress sender, GPacket pkt) {
        ClusterConsumerInfo cci = ClusterConsumerInfo.newInstance(pkt, c);
        int c = cci.getConsumerCount();
        int t = cci.getSubtype();
        if (DEBUG) {
        logger.log(logger.INFO, "handleInterestUpdate: subtype= "+t+", count="+c);
        }

        ConsumerUID intid;

        try {
            Iterator itr = cci.getConsumerUIDs();
            switch (t) {
            case ProtocolGlobals.G_DURABLE_DETACH:
            case ProtocolGlobals.G_REM_INTEREST:
                while (itr.hasNext()) {
                    intid  = (ConsumerUID)itr.next();

                    Consumer cons = Consumer.getConsumer(intid);
                    if (cons == null && cci.isCleanup()) cons =Consumer.newInstance(intid);
                    if (cons != null) {
                        if (DEBUG) {
                        logger.log(logger.INFO, "Remove remote interest: "+cons+ 
                        ", pending="+cci.getPendingMessages()+", cleanup="+cci.isCleanup());
                        }
                        cb.interestRemoved(cons, cci.getPendingMessages(), cci.isCleanup());
                    }
                }
                break;

            case ProtocolGlobals.G_NEW_PRIMARY_INTEREST:
                while (itr.hasNext()) {
                    intid  = (ConsumerUID)itr.next();
                    Consumer cons = Consumer.getConsumer(intid);
                    if (cons != null) {
                        cb.activeStateChanged(cons);
                    }
                }
                break;
            }
        }
        catch (Exception e) {
            if (DEBUG) {
            logger.logStack(logger.INFO, "Exception processing packet ", e);
            }
        }
    }

    private void handleInterestUpdateReply(BrokerAddress sender, GPacket pkt) {
        logger.log(logger.DEBUG,
            "MessageBus: Received G_INTEREST_UPDATE_REPLY " +
            "from {0} : STATUS = {1}",
            sender, ((Integer) pkt.getProp("S")));
    }
}


/*
 * EOF
 */
