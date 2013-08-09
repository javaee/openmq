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

package com.sun.messaging.bridge.service.stomp;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import com.sun.messaging.bridge.api.Bridge;
import com.sun.messaging.bridge.api.StompMessage;
import com.sun.messaging.bridge.api.MessageTransformer;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompSession;
import com.sun.messaging.bridge.api.StompDestination;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompSenderSession implements StompSession  {

    protected Logger logger = StompServer.logger();
    protected StompBridgeResources sbr = null;

    protected Session session = null;
    protected boolean closed = false;

    protected MessageProducer producer = null;
    protected StompConnectionImpl stompconn = null;
    protected Connection connection = null;

    public StompSenderSession(StompConnectionImpl stompc) throws Exception {
        sbr = StompServer.getStompBridgeResources();
        stompconn = stompc;
        connection = stompconn.getConnection();
        session = createSession();
        producer = session.createProducer(null);
    }

    protected Session createSession() throws JMSException {
        return connection.createSession(false, 0);
    }

    protected Session getJMSSession() throws Exception {
        checkSession();
        return session;
    }

    public void sendStompMessage(StompFrameMessage message) throws Exception { 
        checkSession();

	MessageTransformer<Message, Message> mt = StompServer.getMessageTransformer();

        StompMessageImpl msg = new StompMessageImpl(mt);
        stompconn.getProtocolHandler().fromStompFrameMessage(message, msg);
        Message jmsmsg = msg.jmsmsg;
        Destination jmsdest = msg.jmsdest;

        if (mt != null) {
            mt.init(session, Bridge.STOMP_TYPE);
            jmsmsg = mt.transform(jmsmsg, false, "UTF-8",
                     MessageTransformer.STOMP, MessageTransformer.SUN_MQ, msg.propsForTransformer);
            if (jmsmsg == null) {
                throw new JMSException("null returned from "+ mt.getClass().getName()+ " transform() method");
            }
        }
        producer.send(jmsdest, jmsmsg, jmsmsg.getJMSDeliveryMode(),
            jmsmsg.getJMSPriority(), jmsmsg.getJMSExpiration());
        logger.log(Level.FINE, "Sent message "+jmsmsg.getJMSMessageID());
    }


    /**
     */
    public void close() throws Exception {
        session.close();
        closed = true;
    }

    protected void checkSession() throws Exception { 
        if (closed) {
            throw new JMSException(StompServer.getStompBridgeResources().getKString(
                                             StompBridgeResources.X_SESSION_CLOSED));
        }
    }

    @Override
    public StompDestination createStompDestination(String name, boolean isQueue)
    throws Exception {
        if (isQueue) {
            return new StompDestinationImpl(
                session.createQueue(name));
        }
        return new StompDestinationImpl(
            session.createTopic(name));
    }

    @Override
    public StompDestination createTempStompDestination(boolean isQueue)
    throws Exception {
        if (isQueue) {
            return new StompDestinationImpl(
                session.createTemporaryQueue());
        }
        return new StompDestinationImpl(
            session.createTemporaryTopic());
    }

    class StompMessageImpl implements StompMessage {
        Message jmsmsg = null;
        Destination jmsdest = null;
        MessageTransformer mt = null;
        Properties propsForTransformer = null;

        public StompMessageImpl(MessageTransformer mt) {
            this.mt = mt;
        }

        public void setText(StompFrameMessage message) throws Exception {
            jmsmsg = session.createTextMessage();
            ((TextMessage)jmsmsg).setText(message.getBodyText());
        }
        public void setBytes(StompFrameMessage message) throws Exception {
            jmsmsg = session.createBytesMessage();
            ((BytesMessage)jmsmsg).writeBytes(message.getBody());
        }
        public void setDestination(String stompdest) throws Exception {
            StompDestination d = stompconn.getProtocolHandler().
                toStompDestination(stompdest, StompSenderSession.this, false);
            this.jmsdest = ((StompDestinationImpl)d).getJMSDestination();
        }
        public void setReplyTo(String replyto) throws Exception {
            if (replyto == null) {
                return;
            }
            StompDestination dr = stompconn.getProtocolHandler().
                toStompDestination(replyto, StompSenderSession.this, false);
            Destination jmsdestr = ((StompDestinationImpl)dr).getJMSDestination();
            jmsmsg.setJMSReplyTo(jmsdestr);
        }
        public void setPersistent(String v) throws Exception {
            if (v == null) {
                return;
            }
            int deliveryMode = (Boolean.valueOf(v) ? 
                            DeliveryMode.PERSISTENT :
                            DeliveryMode.NON_PERSISTENT);
            jmsmsg.setJMSDeliveryMode(deliveryMode);
        }
        public void setJMSExpiration(String v) throws Exception {
            if (v == null) {
                return;
            }
            long timeToLive = Long.parseLong(v);
            if (timeToLive != 0L) {
                jmsmsg.setJMSExpiration(timeToLive);
            }
        }
        public void setJMSPriority(String v) throws Exception {
            if (v == null) {
                return;
            }
            int pri = Integer.parseInt(v);
            jmsmsg.setJMSPriority(pri);
        }
        public void setJMSCorrelationID(String v) throws Exception {
            if (v != null) {
                jmsmsg.setJMSCorrelationID(v);
            }
        }
        public void setJMSType(String v) throws Exception {
            if (v != null) {
                jmsmsg.setJMSType(v);
            }
        }
        public void setProperty(String name, String value) throws Exception {
            try {
                jmsmsg.setStringProperty(name, value);
            } catch (JMSException e) {
                if (mt == null) {
                    throw e;
                }
                propsForTransformer = new Properties();
                propsForTransformer.setProperty(name, value);
                String h = name+"="+value;
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.WARNING, 
                        sbr.getKString(sbr.W_SET_JMS_PROPERTY_FAILED, h, e.getMessage()), e);
                } else {
                    logger.log(Level.WARNING, 
                        sbr.getKString(sbr.W_SET_JMS_PROPERTY_FAILED, h, e.getMessage()));
                }
                logger.log(Level.INFO, 
                    sbr.getString(sbr.I_PASS_HEADER_TO_TRANSFORMER, h, mt.getClass().getName()));
            }
        }

        public String getSubscriptionID() throws Exception {
            throw new RuntimeException("Unexpected call: getSubscriptionID()");
        }
        public String getDestination() throws Exception {
            throw new RuntimeException("Unexpected call: getDestination()");
        }
        public String getReplyTo() throws Exception {
            throw new RuntimeException("Unexpected call: getReplyTo()");
        }
        public String getJMSMessageID() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSMessageID()");
        }
        public String getJMSCorrelationID() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSCorrelationID()");
        }
        public String getJMSExpiration() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSExpiration()");
        }
        public String getJMSRedelivered() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSRedelivered()");
        }
        public String getJMSPriority() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSPriority()");
        }
        public String getJMSTimestamp() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSTimestamp()");
        }
        public String getJMSType() throws Exception {
            throw new RuntimeException("Unexpected call: getJMSType()");
        }
        public Enumeration getPropertyNames() throws Exception {
            throw new RuntimeException("Unexpected call: getPropertyNames()");
        }
        public String getProperty(String name) throws Exception {
            throw new RuntimeException("Unexpected call: getProperty()");
        }
        public boolean isTextMessage() throws Exception {
            throw new RuntimeException("Unexpected call: isTextMessage()");
        }
        public boolean isBytesMessage() throws Exception {
            throw new RuntimeException("Unexpected call: isBytesMessage()");
        }
        public String getText() throws Exception {
            throw new RuntimeException("Unexpected call: getText()");
        }
        public byte[] getBytes() throws Exception {
            throw new RuntimeException("Unexpected call: getBytes()");
        }
    }

}
