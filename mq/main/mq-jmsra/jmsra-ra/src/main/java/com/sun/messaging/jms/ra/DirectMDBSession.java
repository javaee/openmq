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

package com.sun.messaging.jms.ra;

import javax.jms.*;

import com.sun.messaging.jmq.jmsservice.JMSAck;
import com.sun.messaging.jmq.io.JMSPacket;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.JMSService.SessionAckMode;
import com.sun.messaging.jmq.jmsservice.ConsumerClosedNoDeliveryException;

/**
 *
 */
public class DirectMDBSession extends DirectSession {
    
    /**
     *  Logging
     */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.DirectMDBSession";

    /** Creates a new instance of DirectMDBSession */
    public DirectMDBSession(DirectConnection dc,
            JMSService jmsservice, long sessionId, SessionAckMode ackMode)
    throws JMSException {
        super (dc, jmsservice, sessionId, ackMode);
    }

    protected void _initSession() {
        _loggerOC.entering(_className, "constructor():_init()");
    }
    /**
     *  Deliver a message from this DirectSession - only one thread can do this
     *  at a time.
     */
    protected synchronized JMSAck _deliverMessage(
            javax.jms.MessageListener msgListener, JMSPacket jmsPacket,
            long consumerId) throws ConsumerClosedNoDeliveryException {
        JMSAck jmsAck = null;
        if (this.enableThreadCheck) {
            //Relies on the *same* thread being used to deliver all messages
            //while this sesion is alive
            long tId = Thread.currentThread().getId();
            if (this.deliverThreadId == 0L) {
                //first time
                this.deliverThreadId = tId;
            } else {
                if (this.deliverThreadId != tId) {
                    throw new RuntimeException("Invalid to call deliver from two different threads!");
                }
            }
        }
        javax.jms.Message jmsMsg = null;
        if (msgListener == null) {
            throw new RuntimeException("DirectConsumer:MessageListener not set!");
        }
        if (jmsPacket == null){
            throw new RuntimeException(
                    "DirectConsumer:JMSPacket is null!");
        }
        try {
            jmsMsg = DirectPacket.constructMessage(jmsPacket, consumerId,
                    this, this.jmsservice, false);
        } catch (Exception e) {
            
        }
                
        if (jmsMsg == null) {
            throw new RuntimeException(
                    "DirectConsumer:JMS Message in Packet is null!");
        }
        try {
            this.inDeliver = true;
            msgListener.onMessage(jmsMsg);
            //this.ds._deliverMessage(this.msgListener, jmsMsg);
            this.inDeliver = false;
            if (this.ackMode != SessionAckMode.CLIENT_ACKNOWLEDGE) {
                jmsAck = new DirectAck(this.connectionId, this.sessionId,
                        consumerId,
                        ((DirectPacket)jmsMsg).getReceivedSysMessageID(),
                        this._getTransactionId(),
                        JMSService.MessageAckType.ACKNOWLEDGE);
            }
        } catch (Exception e){
            System.out.println(
                    "DirectConsumer:Caught Exception delivering message"
                    + e.getMessage());;
        }
        return jmsAck;
    }

    protected synchronized void _acknowledgeMDBMessage()
    throws Exception {
        
    }
}

