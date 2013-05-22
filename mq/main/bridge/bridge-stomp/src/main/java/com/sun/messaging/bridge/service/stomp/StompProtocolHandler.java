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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.BytesMessage;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;
import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import com.sun.messaging.bridge.api.Bridge;
import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.bridge.api.MessageTransformer;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompProtocolHandler {

    private Logger _logger = null;

    private static final String DEFAULT_SUBID_PREFIX = "/subscription-to/";

    private static final String MQ_TEMP_QUEUE_PREFIX = "temporary_destination://queue/";
    private static final String MQ_TEMP_TOPIC_PREFIX = "temporary_destination://topic/";

    private StompConnection _stompConnection = null;

    private Map<String, TemporaryQueue> _tempQueues = Collections.synchronizedMap(
                                            new HashMap<String, TemporaryQueue>());
    private Map<String, TemporaryTopic> _tempTopics = Collections.synchronizedMap(
                                            new HashMap<String, TemporaryTopic>());

    private Map<String, TemporaryQueue> _mqtempQueues = Collections.synchronizedMap(
                                              new HashMap<String, TemporaryQueue>());
    private Map<String, TemporaryTopic> _mqtempTopics = Collections.synchronizedMap(
                                              new HashMap<String, TemporaryTopic>());

    private List<String> _subids = Collections.synchronizedList(
                                         new ArrayList<String>());

    private StompBridgeResources _sbr = null;


    public StompProtocolHandler(BridgeContext bc,  Properties jmsprop) {
        _logger = StompServer.logger();
        _sbr = StompServer.getStompBridgeResources();
        _stompConnection = new StompConnection(bc, jmsprop, this);
    }

    public void close(boolean spawnthread) {

        _logger.log(Level.INFO, _sbr.getKString(_sbr.I_CLOSE_STOMP_CONN,
                                  _stompConnection)+"("+spawnthread+")");
        if (!spawnthread) {
            try {
            _stompConnection.disconnect(false);
            return;

            } catch (Throwable t) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.W_CLOSE_STOMP_CONN_FAILED, _stompConnection.toString(), t.getMessage()));
            }
        }
        Thread thr = new Thread (new Runnable() {
                          public void run() {
                              try {
                               _logger.log(Level.INFO, _sbr.getKString(
                                    _sbr.I_CLOSE_STOMP_CONN, _stompConnection));
                               _stompConnection.disconnect(false);

                              } catch (Throwable t) {
                              _logger.log(Level.WARNING, _sbr.getKString(
                              _sbr.W_CLOSE_STOMP_CONN_FAILED, _stompConnection.toString(), t.getMessage()));
                              }
                          }
                     });
        thr.setName("SpawnedClosingThread");
        thr.setDaemon(true);
        thr.start();
    }

    /**
     *
     */
    public void onCONNECT(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) { 
        StompFrameMessage reply = null;

        try {

        String login = message.getHeader(
                       (StompFrameMessage.ConnectHeader.LOGIN).toString());
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", login="+login);
        }

        String passcode = message.getHeader(
                          (StompFrameMessage.ConnectHeader.PASSCODE).toString());
        String clientid = message.getHeader(
                         (StompFrameMessage.ConnectHeader.CLIENTID).toString());

        String id = _stompConnection.connect(login, passcode, clientid);

        reply = new StompFrameMessage(StompFrameMessage.Command.CONNECTED);

        reply.addHeader((StompFrameMessage.ConnectedHeader.SESSION).toString(), id);
        String requestid = message.getHeader(
                           (StompFrameMessage.CommonHeader.RECEIPT).toString());

        if (requestid != null) {
            reply.addHeader(
                 (StompFrameMessage.ResponseCommonHeader.RECEIPTID).toString(), requestid);
        }

        out.sendToClient(reply, ctx, this);

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.CONNECT).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            return;
            }
        } 

    }


    /**
     *
     */
    public void onDISCONNECT(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) {

        try {
            if (_logger.isLoggable(Level.FINE)) {
             _logger.log(Level.FINE, "on"+ message.getCommand());
            }
            _stompConnection.disconnect(true);
            StompFrameMessage reply = getStompReceiptMessage(message);

            if (reply != null) {
                out.sendToClient(reply, ctx, this);
            }
        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            if (e instanceof NotConnectedException) {
                _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam));
                return;
            } else {
                _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            }
            try {
            StompFrameMessage err = toStompErrorMessage(
                                    (StompFrameMessage.Command.DISCONNECT).toString(), e);
            out.sendToClient(err, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            return;
            }
        }
    }

    /**
     *
     */
    public void onSEND(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) {


        StompFrameMessage reply = null;

        Message jmsmsg = null; 
        StompSenderSession ss = null;

        try {

        LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
        headers.putAll(message.getHeaders());

        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String tid = headers.remove(
                         (StompFrameMessage.CommonHeader.TRANSACTION).toString());

        if (tid != null) {
            ss = _stompConnection.getTransactedSession(tid);
            _logger.log(Level.FINE, 
            "Sending message on transacted session: "+ss+" for transaction "+tid); 

        } else {
            ss = _stompConnection.getSenderSession();
        }

        if (message.getContentLength() != -1) {
            headers.remove(
                    (StompFrameMessage.CommonHeader.CONTENTLENGTH).toString());
            jmsmsg = ss.getJMSSession().createBytesMessage();
            ((BytesMessage)jmsmsg).writeBytes(message.getBody());
        } else {
            jmsmsg = ss.getJMSSession().createTextMessage();
            ((TextMessage)jmsmsg).setText(message.getBodyText());
        }


        String stompdest = headers.remove(
                          (StompFrameMessage.SendHeader.DESTINATION).toString()); 
        Destination jmsdest = toJMSDestination(stompdest, ss.getJMSSession(), false);

        MessageProducer jmspub = ss.getJMSProducer(); 

        int priority = jmspub.getPriority();
        String v = headers.remove((StompFrameMessage.SendHeader.PRIORITY).toString());
        if (v != null) priority = Integer.parseInt(v);

        int deliveryMode = jmspub.getDeliveryMode();
        v = headers.remove((StompFrameMessage.SendHeader.PERSISTENT).toString());
        if (v != null) {
            deliveryMode = (Boolean.valueOf(v) ? DeliveryMode.PERSISTENT :
                                                 DeliveryMode.NON_PERSISTENT);
        }

        long timeToLive = jmspub.getTimeToLive();
        v = headers.remove((StompFrameMessage.SendHeader.EXPIRES).toString());
        if (v != null) { 
            timeToLive = Long.parseLong(v);
        }


        jmsmsg.setJMSCorrelationID(headers.remove(
               (StompFrameMessage.SendHeader.CORRELATIONID).toString()));

        v = headers.remove((StompFrameMessage.SendHeader.TYPE).toString());
        if (v != null) jmsmsg.setJMSType(v);

        v = headers.remove((StompFrameMessage.SendHeader.REPLYTO).toString());
        if (v != null) {
            jmsmsg.setJMSReplyTo(toJMSDestination(v, ss.getJMSSession(), false));
        }

        v = headers.remove(StompFrameMessage.CommonHeader.RECEIPT);

        MessageTransformer<Message, Message> mt = StompServer.getMessageTransformer();

        Properties props = null;
        if (mt != null) {
            props = new Properties();
        }
        String key = null;
        String val = null;
        String h = null;
        Iterator<String> itr = headers.keySet().iterator();
        while (itr.hasNext()) {
            key = itr.next();
            val = headers.get(key);
            h = key+StompFrameMessage.HEADER_SEPERATOR+val;
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Setting header "+h+" as JMS message property");
            }
            try {
                jmsmsg.setStringProperty(key, val);
                itr.remove(); 
            } catch (JMSException e) {
                if (mt == null) {
                    throw e;
                }
                props.setProperty(key, val);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_SET_JMS_PROPERTY_FAILED, h, e.getMessage()), e);
                } else {
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_SET_JMS_PROPERTY_FAILED, h, e.getMessage()));
                }
                _logger.log(Level.INFO, _sbr.getString(_sbr.I_PASS_HEADER_TO_TRANSFORMER, h, mt.getClass().getName()));
            }
        }

        if (mt != null) {
            mt.init(ss.getJMSSession(), Bridge.STOMP_TYPE);
            jmsmsg = mt.transform(jmsmsg, false, "UTF-8", 
                        MessageTransformer.STOMP, MessageTransformer.SUN_MQ, props);
            if (jmsmsg == null) {
                throw new JMSException("null returned from "+ mt.getClass().getName()+ " transform() method");
            }
        }
	    jmspub.send(jmsdest, jmsmsg, deliveryMode, priority, timeToLive);
        _logger.log(Level.FINE, "Sent message "+jmsmsg.getJMSMessageID()); 

        reply = getStompReceiptMessage(message);

        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Throwable e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.SEND).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
	 *   
     */
    public void onSUBSCRIBE(StompFrameMessage message, 
                            StompOutputHandler out, FilterChainContext ctx, 
                            StompOutputHandler aout) 
                            throws Exception {
        StompFrameMessage reply = null;

        String subid = null;
        String duraname = null;
        boolean created = false;
        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String tid = headers.get(
                         (StompFrameMessage.CommonHeader.TRANSACTION).toString());
        subid = headers.get(
                         (StompFrameMessage.SubscribeHeader.ID).toString());
        String ack = headers.get(
                         (StompFrameMessage.SubscribeHeader.ACK).toString());
        int ackMode = Session.AUTO_ACKNOWLEDGE;
        if (ack != null) {
            if (ack.equals((StompFrameMessage.AckMode.CLIENT).toString())) {
                ackMode = Session.CLIENT_ACKNOWLEDGE;
            }
        }
        String selector = headers.get(
                           (StompFrameMessage.SubscribeHeader.SELECTOR).toString());

        String stompdest = headers.get(
                           (StompFrameMessage.SubscribeHeader.DESTINATION).toString());
        if (stompdest == null) {
            throw new StompProtocolException(
            "SUBSCRIBE without "+StompFrameMessage.SubscribeHeader.DESTINATION+" header!"); 
        }
        if (subid == null) {
            subid = makeDefaultSubscriberId(stompdest); 
        }
        if (_subids.contains(subid)) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_SUBID_ALREADY_EXISTS, subid)); 
        }

        boolean nolocal = false;
        String val = headers.get(
                       (StompFrameMessage.SubscribeHeader.NOLOCAL).toString());
        if (val != null && val.equalsIgnoreCase("true")) {
            nolocal = true;
        }

        duraname = headers.get((StompFrameMessage.SubscribeHeader.DURASUBNAME).toString());

        if (tid == null) {
            StompSubscriberSession ss = _stompConnection.createSubscriberSession(
                                                                  subid, ackMode);
            created = true;
            ss.createSubscriber(toJMSDestination(stompdest, ss.getJMSSession(), true), 
                                               selector, duraname, nolocal, aout);
        } else {
            StompTransactedSession ts = _stompConnection.getTransactedSession(tid);
            created = true;
            ts.createSubscriber(subid, 
                                toJMSDestination(stompdest, ts.getJMSSession(), true), 
                                selector, duraname, nolocal, aout);
        }
        _subids.add(subid);

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            if (created) {
            _stompConnection.closeSubscriber(subid, null);
            _subids.remove(subid);
            } 
            } catch (Exception e1) {
            _logger.log(Level.FINEST, message.getCommand()+
                ": Unable to close subscriber (subid="+subid+", duraname="+duraname+"): "+
                 e1.getMessage()+" after creation failure: "+e.getMessage(), e1);
            } finally {

            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.SUBSCRIBE).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            }
            return;
        }

    }

    /**
     *  	
     */
    public void onUNSUBSCRIBE(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx)
                                                              throws Exception {
        StompFrameMessage reply = null;

        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String subid = headers.get(
                           (StompFrameMessage.SubscribeHeader.ID).toString());
        String stompdest = headers.get(
                           (StompFrameMessage.SubscribeHeader.DESTINATION).toString());
		String duraname = headers.get((StompFrameMessage.SubscribeHeader.DURASUBNAME).toString());

        if (subid == null && duraname == null) {
            if (stompdest == null) {
                throw new StompProtocolException(_sbr.getKString(_sbr.X_UNSUBSCRIBE_WITHOUT_HEADER,
                    StompFrameMessage.SubscribeHeader.DESTINATION, StompFrameMessage.SubscribeHeader.ID));
            }
            subid = makeDefaultSubscriberId(stompdest);
        }

        String id = _stompConnection.closeSubscriber(subid, duraname);

        if (duraname == null) {
            _subids.remove(subid);
        } else {
            if (id != null) _subids.remove(id);
        }

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.UNSUBSCRIBE).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
     *
     */
    public void onBEGIN(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) throws Exception {
        StompFrameMessage reply = null;

        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String tid = headers.get(
                         (StompFrameMessage.CommonHeader.TRANSACTION).toString());
        if (tid == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_HEADER_NOT_SPECIFIED_FOR, 
                  (StompFrameMessage.CommonHeader.TRANSACTION).toString(),
                  (StompFrameMessage.Command.BEGIN).toString())); 
        }

        _stompConnection.beginTransactedSession(tid);

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.BEGIN).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
     *
     */
    public void onCOMMIT(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) throws Exception {
        StompFrameMessage reply = null;

        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String tid = headers.get(
                             (StompFrameMessage.CommonHeader.TRANSACTION).toString());
        if (tid == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_HEADER_NOT_SPECIFIED_FOR, 
                  (StompFrameMessage.CommonHeader.TRANSACTION).toString(),
                  (StompFrameMessage.Command.COMMIT).toString())); 
        }

        _stompConnection.commitTransactedSession(tid);

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.COMMIT).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
     *
     */
    public void onABORT(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) throws Exception {
        StompFrameMessage reply = null;

        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String tid = headers.get(
                         (StompFrameMessage.CommonHeader.TRANSACTION).toString());
        if (tid == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_HEADER_NOT_SPECIFIED_FOR, 
                  (StompFrameMessage.CommonHeader.TRANSACTION).toString(),
                  (StompFrameMessage.Command.ABORT).toString())); 
        }

        _stompConnection.abortTransactedSession(tid);

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.ABORT).toString(), e);
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
     *
     */
    public void onACK(StompFrameMessage message, StompOutputHandler out, FilterChainContext ctx) throws Exception {
        StompFrameMessage reply = null;

        try {

        HashMap<String, String> headers = message.getHeaders();
        if (_logger.isLoggable(Level.FINE)) {
        _logger.log(Level.FINE, "on"+ message.getCommand()+", headers="+headers);
        }

        String msgid = headers.get(StompFrameMessage.AckHeader.MESSAGEID);
        if (msgid == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_HEADER_NOT_SPECIFIED_FOR, 
                  (StompFrameMessage.AckHeader.MESSAGEID).toString(),
                  (StompFrameMessage.Command.ACK).toString())); 
        }
        String tid = headers.get(
                         (StompFrameMessage.CommonHeader.TRANSACTION).toString());

        String subid = headers.get(
                           (StompFrameMessage.MessageHeader.SUBSCRIPTION).toString());
        if (subid != null) {
            if (!_subids.contains(subid)) {
                throw new StompProtocolException(_sbr.getKString(_sbr.X_SUBSCRIBER_ID_NOT_FOUND, subid));
            }
        }

        if (tid != null) {
            StompTransactedSession ts = _stompConnection.getTransactedSession(tid);
            if (subid != null) {
                ts.ack(subid, msgid);
            } else {
                String[] eparam = {(StompFrameMessage.MessageHeader.SUBSCRIPTION).toString(),
                                   tid, DEFAULT_SUBID_PREFIX, msgid};
                _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_NO_SUBID_TXNACK, eparam)); 

                ts.ack(DEFAULT_SUBID_PREFIX, msgid, true);
            }
        } else if (subid != null) {

            StompSubscriberSession ss = _stompConnection.getSubscriberSession(subid);
            if (ss != null ) {
                ss.ack(msgid);
            } else {
                StompTransactedSession ts = _stompConnection.getTransactedSession();
                if (ts == null) {
                    throw new StompProtocolException(_sbr.getKString(_sbr.X_SUBSCRIBE_NO_SESSION, subid));
                }
                ts.ack(subid, msgid);
            }
        } else {
            String[] eparam = {(StompFrameMessage.MessageHeader.SUBSCRIPTION).toString(),
                                DEFAULT_SUBID_PREFIX, msgid};
            _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_NO_SUBID_NONTXNACK, eparam));

            _stompConnection.ackNonTransacted(DEFAULT_SUBID_PREFIX, msgid);
        }

        reply = getStompReceiptMessage(message);
        if (reply != null) {
            out.sendToClient(reply, ctx, this);
        }

        } catch (Exception e) {
            String[] eparam = {message.getCommand().toString(), e.getMessage(), _stompConnection.toString()};
            _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_COMMAND_FAILED, eparam), e);
            try {
            reply = toStompErrorMessage((StompFrameMessage.Command.ACK).toString(), e, 
                                        ((e instanceof UnrecoverableAckFailureException) ? true:false));
            out.sendToClient(reply, ctx, this);
            } catch (Exception ee) {
            _logger.log(Level.WARNING, _sbr.getKString(
                    _sbr.E_UNABLE_SEND_ERROR_MSG, e.getMessage(), ee.getMessage()), ee);
            }
            return;
        }

    }

    /**
     *
     */	   
    public static StompFrameMessage getStompReceiptMessage(StompFrameMessage message)
                                                                   throws Exception {
        StompFrameMessage reply = null;

        String requestid = message.getHeader(
                           (StompFrameMessage.CommonHeader.RECEIPT).toString());
        if (requestid != null) {
            reply = new StompFrameMessage(StompFrameMessage.Command.RECEIPT);
            reply.addHeader(
                 (StompFrameMessage.ResponseCommonHeader.RECEIPTID).toString(), requestid);
        }
        return reply;
    }

    /**
     *
     */
    public static StompFrameMessage toStompErrorMessage(String where,
                                                        Throwable e) 
                                                        throws Exception {
        return toStompErrorMessage(where, e, false);
    }

    public static StompFrameMessage toStompErrorMessage(String where,
                                                        Throwable e,
                                                        boolean fatal)
                                                        throws Exception {

        StompFrameMessage err = new StompFrameMessage(StompFrameMessage.Command.ERROR);
        err.addHeader((StompFrameMessage.ErrorHeader.MESSAGE).toString(),
                       where+": "+e.getMessage()+(fatal ? ", STOMP connection will be closed":""));
        err.writeExceptionToBody(e);
        if (fatal) {
            err.setFatalERROR();
        }
        return err;
    }

    public Destination toJMSDestination(String stompdest, 
                                        Session ss, boolean sub)
                                              throws Exception {

        if (stompdest.startsWith("/queue/")) {
            String dest = stompdest.substring("/queue/".length(), stompdest.length()).trim();
            return ss.createQueue(dest);

        } else if (stompdest.startsWith("/topic/")) {
            String dest = stompdest.substring("/topic/".length(), stompdest.length()).trim();
            return ss.createTopic(dest);

        } else if (stompdest.startsWith("/temp-queue/")) {
            String dest = stompdest.substring("/temp-queue/".length(), stompdest.length()).trim();
            if (dest.startsWith(MQ_TEMP_QUEUE_PREFIX)) {
                if (sub) throw new JMSException("Can't subscribe "+stompdest);
                synchronized(_mqtempQueues) {
                    Destination d = _mqtempQueues.get(dest); 
                    if (d == null) {
                        throw new JMSException("MQ TemporaryQueue not found: "+stompdest);
                    }
                    return d;
                }
            }
            synchronized(_tempQueues) {
                TemporaryQueue d = _tempQueues.get(dest);
                if (d != null) return d;
                d = ss.createTemporaryQueue();
                _tempQueues.put(dest, d);
                return d;
            }

        } else if (stompdest.startsWith("/temp-topic/")) {
            String dest = stompdest.substring("/temp-topic/".length(), stompdest.length()).trim();
            if (dest.startsWith(MQ_TEMP_TOPIC_PREFIX)) {
                if (sub) throw new JMSException("Can't subscribe "+stompdest);
                synchronized(_mqtempTopics) {
                    Destination d = _mqtempTopics.get(dest); 
                    if (d == null) {
                        throw new JMSException("MQ TemporaryTopic not found: "+stompdest);
                    }
                    return d;
                }
            }
            synchronized(_tempTopics) {
                TemporaryTopic d = _tempTopics.get(dest);
                if (d != null) return d;
                d = ss.createTemporaryTopic();
                _tempTopics.put(dest, d);
                return d;
            }

        } else {
            throw new StompProtocolException(
            "Invalid header "+StompFrameMessage.SendHeader.DESTINATION+" value:"+stompdest); 
        }
    }

    public String toStompDestination(Destination jmsdest, boolean cache)
                                                      throws Exception {

        if (jmsdest == null) throw new JMSException("JMS destination null !"); 

        StringBuffer buf = new StringBuffer();

        if (jmsdest instanceof javax.jms.TemporaryQueue) {
            String d = ((javax.jms.Queue)jmsdest).getQueueName();
            buf.append("/temp-queue/").append(d);
            if (cache) {
            synchronized(_mqtempQueues) {
                if (_mqtempQueues.get(d) == null) {
                    _mqtempQueues.put(d, (javax.jms.TemporaryQueue)jmsdest); 
                }
            }
            }
            return buf.toString();
        }
        if (jmsdest instanceof javax.jms.TemporaryTopic) {
            String d = ((javax.jms.Topic)jmsdest).getTopicName();
            buf.append("/temp-topic/").append(d);
            if (cache) {
            synchronized(_mqtempTopics) {
                if (_mqtempTopics.get(d) == null) {
                    _mqtempTopics.put(d, (javax.jms.TemporaryTopic)jmsdest); 
                }
            }
            }
            return buf.toString();
        }
        if (jmsdest instanceof javax.jms.Queue) {
            buf.append("/queue/").append(((javax.jms.Queue)jmsdest).getQueueName());
            return buf.toString();
        }
        if (jmsdest instanceof javax.jms.Topic) {
            buf.append("/topic/").append(((javax.jms.Topic)jmsdest).getTopicName());
            return buf.toString();
        }
        throw new JMSException("Unknow destination type: "+jmsdest);
    }

    private static String makeDefaultSubscriberId(String stompdest) {
        return DEFAULT_SUBID_PREFIX+stompdest;
    }

    protected StompFrameMessage toStompFrameMessage(Message jmsmsg, String subid, Session ss) throws Exception {
        MessageTransformer<Message, Message> mt = StompServer.getMessageTransformer();
        if (mt != null) {
            mt.init(ss, Bridge.STOMP_TYPE);
            jmsmsg = mt.transform(jmsmsg, true, null, 
                     MessageTransformer.SUN_MQ, MessageTransformer.STOMP, null);
            if (jmsmsg == null) { 
                throw new JMSException("null returned from "+mt.getClass().getName()+
                " transform() method for JMS message "+jmsmsg.toString()+" in subscription "+subid);
            }
        }

        StompFrameMessage message = new StompFrameMessage(StompFrameMessage.Command.MESSAGE);
        HashMap<String, String> headers = message.getHeaders();

        headers.put(StompFrameMessage.MessageHeader.SUBSCRIPTION, subid);

        Destination jmsdest = jmsmsg.getJMSDestination();
        headers.put(
                 (StompFrameMessage.MessageHeader.DESTINATION).toString(),
                 toStompDestination(jmsdest, false));

        jmsdest = jmsmsg.getJMSReplyTo();
        if (jmsdest != null) {
            headers.put(
                     (StompFrameMessage.MessageHeader.REPLYTO).toString(),
                     toStompDestination(jmsdest, true));
        }

        headers.put(
                 (StompFrameMessage.MessageHeader.MESSAGEID).toString(),
                 jmsmsg.getJMSMessageID());

        String val = jmsmsg.getJMSCorrelationID(); 
        if (val != null) {
            headers.put((StompFrameMessage.MessageHeader.CORRELATIONID).toString(), val);
        }

        headers.put((StompFrameMessage.MessageHeader.EXPIRES).toString(), 
                    String.valueOf(jmsmsg.getJMSExpiration()));

        headers.put(StompFrameMessage.MessageHeader.REDELIVERED, 
                        String.valueOf(jmsmsg.getJMSRedelivered()));

        headers.put((StompFrameMessage.SendHeader.PRIORITY).toString(), 
                    String.valueOf(jmsmsg.getJMSPriority()));

        headers.put((StompFrameMessage.MessageHeader.TIMESTAMP).toString(), 
                    String.valueOf(jmsmsg.getJMSTimestamp()));

        val = jmsmsg.getJMSType(); 
        if (val != null) {
            headers.put((StompFrameMessage.MessageHeader.TYPE).toString(), val);
        }

        String name, value;
        Enumeration en = jmsmsg.getPropertyNames();
        while (en.hasMoreElements()) {
            name = (String)en.nextElement();
            value = (jmsmsg.getObjectProperty(name)).toString();
            headers.put(name, value);
        }

        if (jmsmsg instanceof TextMessage) {
            String text = ((TextMessage)jmsmsg).getText();
            if (text != null) {
                byte[] data = text.getBytes("UTF-8");
                message.setBody(data);
                headers.put(StompFrameMessage.CommonHeader.CONTENTLENGTH, 
                            String.valueOf(data.length));
            } else {
                headers.put(StompFrameMessage.CommonHeader.CONTENTLENGTH, 
                            String.valueOf(0));
            }
        } else if (jmsmsg instanceof BytesMessage) {
            BytesMessage m = (BytesMessage)jmsmsg;
            byte[] data = new byte[(int)m.getBodyLength()];
            m.readBytes(data);
            message.setBody(data);
            headers.put(StompFrameMessage.CommonHeader.CONTENTLENGTH, 
                        String.valueOf(data.length));

        } else {
            throw new JMSException("Message type is not supported: "+jmsmsg);
        }

        return message;
    }
}
