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

package com.sun.messaging.jmq.jmsserver.service.imq.websocket.stomp;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.LinkedHashMap;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.JMSPacket;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.Destination;
import com.sun.messaging.jmq.jmsservice.JMSServiceReply;
import com.sun.messaging.jmq.jmsservice.JMSService.SessionAckMode;
import com.sun.messaging.jmq.jmsservice.JMSService.MessageDeliveryMode;
import com.sun.messaging.jmq.jmsservice.JMSService.MessagePriority;
import com.sun.messaging.jmq.jmsservice.JMSServiceException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompDestination;
import com.sun.messaging.bridge.api.StompProtocolException;
import com.sun.messaging.bridge.api.StompProtocolHandler;
import com.sun.messaging.bridge.api.StompProtocolHandler.StompAckMode;


/**
 * @author amyk 
 */
public class StompSenderSession extends StompSessionImpl  {

    //protected by closeLock
    protected Map<String, Long> producers = new HashMap<String, Long>();

    public StompSenderSession(StompConnectionImpl stompc) throws Exception {
        super(stompc, StompAckMode.AUTO_ACK, false);
    }

    protected StompSenderSession(StompConnectionImpl stompc, boolean transacted)
    throws Exception {
        super(stompc, StompAckMode.AUTO_ACK, transacted);
    }

    @Override
    public String toString() {
        return "[StompSenderSession@"+hashCode()+
               ", producers="+producers.size()+"]";
    }

    @Override
    protected synchronized void closeProducers() {
        Iterator<Long> itr = producers.values().iterator(); 
        Long prodid = null;
        while (itr.hasNext()) {
            prodid = itr.next();
            try {
                jmsservice.deleteProducer(connectionId, sessionId, prodid.longValue());
            } catch (Exception e) {
                if (!isClosing() || getDEBUG()) {
                    logger.logStack(logger.WARNING, e.getMessage(), e);
                }
            }
        }
        producers.clear();
    }

    public void sendStompMessage(StompFrameMessage message) throws Exception {
        checkSession();

        Packet pkt = new Packet();
        pkt.setPersistent(jmsservice.DEFAULT_MessageDeliveryMode ==
                          MessageDeliveryMode.PERSISTENT);
        pkt.setPriority(jmsservice.DEFAULT_MessagePriority.priority());
        pkt.setExpiration(jmsservice.DEFAULT_TIME_TO_LIVE);
        pkt.setDeliveryTime(jmsservice.DEFAULT_DELIVERY_DELAY);
        stompconn.fillRemoteIPAndPort(pkt);

        StompDestinationImpl d = fromStompFrameMessage(message, pkt);
        String stompdest = d.getStompDestinationString();

        try {
            jmsservice.createDestination(connectionId, d.getDestination());
        } catch (JMSServiceException jmsse) {
            JMSServiceReply.Status status = jmsse.getJMSServiceReply().getStatus();
            if (status == JMSServiceReply.Status.CONFLICT) {
                if (logger.isFineLoggable() || stompconn.getProtocolHandler().getDEBUG()) {
                    logger.log(logger.INFO, "Destination "+stompdest+" already exist");
                }
            } else {
                throw jmsse;
            }
        }
        synchronized(this) {
            Long prodid = producers.get(stompdest);
            if (prodid == null) {
                JMSServiceReply reply = jmsservice.addProducer(
                    connectionId, sessionId, d.getDestination());
                prodid = Long.valueOf(reply.getJMQProducerID());
                producers.put(stompdest, prodid);
            }
            pkt.setProducerID(prodid.longValue());
        }

        pkt.prepareToSend();
        synchronized(this) {
            if (isTransacted()) {
                pkt.setTransactionID(getTransactionId());
            } else {
                pkt.setTransactionID(0L);
            }
            final Packet p = pkt;
            jmsservice.sendMessage(connectionId, new JMSPacket() {
                       public Packet getPacket() {
                       return p;
                   }
                   });
        }
        if (logger.isFineLoggable() || stompconn.getProtocolHandler().getDEBUG()) {
            logger.log(logger.INFO, "Sent message "+pkt.getSysMessageID());
        }
    }
}
