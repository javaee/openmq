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

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompDestination;
import com.sun.messaging.bridge.api.StompSubscriber;
import com.sun.messaging.bridge.api.StompOutputHandler;
import com.sun.messaging.bridge.api.StompProtocolException;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompTransactedSession extends StompSenderSession implements Runnable {

    private final static int MAX_QUEUE_SIZE = 100;

    private String _lastRolledbackTID = null;

    private StompOutputHandler _out = null;

    private Map<String, TransactedSubscriber> _subscribers = 
            Collections.synchronizedMap(new HashMap<String, TransactedSubscriber>());

    private List<SubscribedMessage> _msgqueue = Collections.synchronizedList(
                                           new ArrayList<SubscribedMessage>());

    private List<SubscribedMessage> _unackqueue = Collections.synchronizedList(
                                             new ArrayList<SubscribedMessage>());

    private List<TransactedAck> _ackedqueue = Collections.synchronizedList(
                                              new ArrayList<TransactedAck>());

    private Object _lock =  new Object();
    private String _tid = null;
    private boolean _locked = false;  //to lock _subthread
    private boolean _stopped = false; //when _subthread is locked
    private Thread _subthread = null;

    public StompTransactedSession(StompConnectionImpl sc)
                                 throws Exception {
        super(sc);
        logger.log(Level.INFO, sbr.getString(sbr.I_CREATED_TXN_SESSION, this.toString()));
    }

    protected Session createSession() throws JMSException {
       return connection.createSession(true, 0); 
    }

    public String toString() {
        return "["+connection+", "+session+", "+_tid+"]";
    }

    /**
     */
    public synchronized StompSubscriber createSubscriber(String subid, 
                             StompDestination d, String selector,
                             String duraname, boolean nolocal,
                             StompOutputHandler out)
                             throws Exception {
        _out = out;
        Destination dest = ((StompDestinationImpl)d).getJMSDestination();
        
        checkSession();

        MessageConsumer sub = null;

        if (_subscribers.get(subid) != null) {
            throw new JMSException(sbr.getKString(
                sbr.X_SUBID_ALREADY_EXIST_IN_TXN_SESSION, subid, this.toString()));
        }

        String destname = null;

        if (dest instanceof Queue) {
            sub = session.createConsumer(dest, selector);
            destname = ((Queue)dest).getQueueName();  
        } else if (duraname != null) {
            sub = session.createDurableSubscriber(
                           (Topic)dest, duraname, selector, nolocal);
            destname = ((Topic)dest).getTopicName();  
        } else {
           sub = session.createConsumer(dest, selector, nolocal);
            destname = ((Topic)dest).getTopicName();  
        }

        synchronized(_lock) {
            if (_subthread == null) {
                _subthread = new Thread(this);
                _subthread.setName("TransactedSession["+this+"]");
                _subthread.setDaemon(true);
                _subthread.start();
           }
        }
        TransactedSubscriber txsub = new TransactedSubscriber(subid, sub, 
                             ((dest instanceof Queue) ? null:duraname), this);
        _subscribers.put(subid, txsub);

        String[] param = {subid, destname, this.toString()};
        logger.log(Level.INFO, sbr.getString(sbr.I_CREATED_TXN_SUB, param));
        return txsub;
    }

    /**
     * @param duraname if not null, subid will be ignored
     * @return subid if found else return null
     */
    public synchronized String closeSubscriber(String subid, String duraname) throws Exception {
        if (duraname == null) {
            TransactedSubscriber sub = _subscribers.get(subid);   
            if (sub == null) return null; 
            preCloseSubscriber(subid);
            sub.close();
            _subscribers.remove(subid);
            return subid;
        }

        TransactedSubscriber sub = null;
        String dn = null;

        synchronized(_subscribers) {

        for (String id: _subscribers.keySet()) {
            sub = _subscribers.get(id); 
            dn = sub.getDuraName();
            if (dn == null) continue;
            if (dn.equals(duraname)) {
                preCloseSubscriber(id);
                sub.close();
                _subscribers.remove(id);
                session.unsubscribe(duraname);
                return id;
            }
        }
        }
        session.unsubscribe(duraname);
        return null;
    }

    private void preCloseSubscriber(String subid) throws Exception {

        connection.stop();
        try {

        TransactedAck ta = null;
        synchronized(_ackedqueue) {
            Iterator<TransactedAck> itr = _ackedqueue.iterator();
            while (itr.hasNext()) {
                ta = itr.next();
                if (ta.subid.equals(subid)) {
                    try {
                        acknowledge(ta.msg);
                    } catch (Exception e) {
                        String[] eparam = { ta.msg.getJMSMessageID(), _tid, subid, e.getMessage() };
                        logger.log(Level.WARNING, sbr.getKString(sbr.W_UNABLE_ACK_MSG_ON_CLOSE_SUB, eparam), e);
                    }
                    itr.remove();
                }
            }
        }
        
        } finally {
        connection.start();
        }
    }

    /**
     *
     */
    public String getStompTransactionId() {
        synchronized(_lock) {
            return _tid;
        }
    }

    /**
     *
     */
    public void setStompTransactionId(String tid)
    throws Exception {

         synchronized(this) {

         if (_ackedqueue.size() != 0) { 
             logger.log(Level.WARNING, 
             "acked-queue is not empty on setting transaction ID "+tid+
              (_lastRolledbackTID == null ? "":", last rolledback transaction ID was "+_lastRolledbackTID));
         }
         _ackedqueue.clear();
         if (tid != null) _lastRolledbackTID = null;

         synchronized(_lock) {
             _tid = tid;
             _lock.notifyAll();
         }
         }
    }

    /**
     *
     */
    public synchronized void commit() throws Exception {
        logger.log(Level.FINE, "Committing transaction "+_tid+ " on JMS session "+session);

        boolean stopped = false;
        try {
            if (_ackedqueue.size() > 0) {
                stopped = true;
                connection.stop();
               synchronized(_ackedqueue) {
                   TransactedAck ta = null;
                   Iterator<TransactedAck> itr = _ackedqueue.iterator();
                   while (itr.hasNext()) {
                       ta = itr.next();
                       if (!ta.tid.equals(_tid)) {
                       throw new JMSException(
                       "Transaction ack ["+ta+"] tid not match current transaction id "+_tid);
                       }
                       logger.log(Level.FINE, "Ack message "+ta.msgid+ " for committing transaction "+_tid);
                       acknowledge(ta.msg);
                       itr.remove();
                   }
               }
            }

            session.commit();
        } catch (Exception e)  {
            String emsg = sbr.getKString(sbr.E_COMMIT_FAIL_WILL_ROLLBACK, _tid, e.getMessage());
            logger.log(Level.SEVERE, emsg); 

            try {
            rollback(); 

            } finally {
            JMSException je = new JMSException(emsg);
            je.initCause(e);
            throw je;
            }

        } finally {
            setStompTransactionId(null);
            _ackedqueue.clear();
            if (stopped) connection.start();
        } 
    }
  
    /**
     *
     */
    public synchronized String getLastRolledbackStompTransactionId() {
        return _lastRolledbackTID;
    }

    /**
     *
     */
    public synchronized void rollback() throws Exception {

        try {

        /**
         * 1. stop MQ client session thread
         * 2. stop this session thread
         */
        connection.stop();
        stop(true);

        synchronized(_ackedqueue) {

            TransactedAck ta = null;
            Iterator<TransactedAck> itr = _ackedqueue.iterator();
            while (itr.hasNext()) {
                ta = itr.next();
                if (!ta.tid.equals(_tid)) {
                    throw new JMSException(
                    "Transaction ack ["+ta+"] tid not match current transaction id "+_tid);
                }
                if (_subscribers.get(ta.subid) != null) {
                    try {
                    acknowledge(ta.msg);
                    } catch (Exception e) {
                    String[] eparam = {ta.msg.getJMSMessageID(), getStompTransactionId(), e.getMessage()};
                    logger.log(Level.WARNING, sbr.getKString(sbr.W_TXNACK_MSG_ON_ROLLBACK_FAIL, eparam), e);
                    }
                }
                itr.remove();
            }
        }

        synchronized(_unackqueue) {

            SubscribedMessage sm = null;
            Iterator<SubscribedMessage> itr = _unackqueue.iterator();
            while (itr.hasNext()) {
                sm = itr.next();
                if (_subscribers.get(sm.subid) != null) {
                    try {
                    acknowledge(sm.msg);
                    } catch (Exception e) {
                    String[] eparam = { sm.msg.getJMSMessageID(), getStompTransactionId(), e.getMessage() };
                    logger.log(Level.WARNING, sbr.getKString(sbr.W_TXNACK_DELIVERED_MSG_ON_ROLLBACK_FAIL, eparam), e);
                    }
                }
                itr.remove();
            }
        }

        synchronized(_msgqueue) {
            SubscribedMessage sm = null;
            Iterator<SubscribedMessage> itr = _msgqueue.iterator();
            while (itr.hasNext()) {
                sm = itr.next();
                if (_subscribers.get(sm.subid) != null) {
                    try {
                    acknowledge(sm.msg);
                    } catch (Exception e) {
                    String[] eparam = { sm.msg.getJMSMessageID(), getStompTransactionId(), e.getMessage() };
                    logger.log(Level.WARNING, sbr.getKString(sbr.W_TXNACK_UNDELIVERED_MSG_ON_ROLLBACK_FAIL, eparam), e);
                    }
                }
                itr.remove();
            }

        }

        session.rollback();

        } finally {
            _lastRolledbackTID = _tid;
            setStompTransactionId(null);
            _ackedqueue.clear();
            stop(false);
            connection.start();
        }

    }

    /**
     *
     */
    public void ack(String subid, String msgid) throws Exception {
        ack(subid, msgid, false);
    }

    public synchronized void ack10(String subid, 
                                 String msgid)
                                 throws Exception {
        ack(subid, msgid, true);
    }

    private synchronized void ack(String subid, String msgid, boolean prefix)
    throws Exception {

        checkSession();

        if (getStompTransactionId() == null) {
            throw new StompProtocolException(sbr.getKString(sbr.X_TXNACK_NO_CURRENT_TRANSACTION, msgid, subid));
        }

        TransactedSubscriber sub = _subscribers.get(subid);
        if (sub == null) {
            if (prefix) {
                synchronized(_subscribers) {

                for (String id: _subscribers.keySet()) {
                    if (id.startsWith(subid)) {
                        subid = id;
                        sub = _subscribers.get(id);
                        break;
                    }
                }

                }
            }
            if (sub == null) {
                if (!prefix) {
                    String[] eparam = {subid, msgid, _tid};
                    throw new JMSException(sbr.getKString(sbr.X_SUBID_NOT_FOUND_IN_TXN, eparam));
                } else {
                    String[] eparam = {msgid, _tid, StompFrameMessage.MessageHeader.SUBSCRIPTION};
                    throw new JMSException(sbr.getKString(sbr.X_ACK_CANNOT_DETERMINE_SUBSCRIBER_IN_TXN, eparam));
                }
            }
        }

        synchronized(_unackqueue) {

            SubscribedMessage sm = new SubscribedMessage(subid, msgid);
            int index =  _unackqueue.indexOf(sm);
            if (index == -1) { 
                if (!_ackedqueue.contains(new TransactedAck(_tid, subid, sm.msgid))) {
                    String[] eparam = {msgid, subid, _tid};
                    throw new StompProtocolException(sbr.getKString(sbr.X_MSG_NOT_FOUND_IN_TXN, eparam));
                }
                if (logger.isLoggable(Level.INFO)) {
                   
                    logger.log(Level.INFO, "Message "+msgid+ " for subcriber "+subid+" has already acked in transaction "+_tid);
                }
                return;
            }
            ArrayList<SubscribedMessage> acks = new ArrayList<SubscribedMessage>();
            for (int i = 0; i <= index; i++) {
                sm = _unackqueue.get(i);
                if (sm.subid.equals(subid)) {
                    _ackedqueue.add(new TransactedAck(_tid, subid, sm.msg));
                   acks.add(sm);
                }
            }
            Iterator<SubscribedMessage> itr = acks.iterator();
            while (itr.hasNext()) {
                _unackqueue.remove(itr.next());
            }
        }

        return;
    }
    
    /**
     *
     */
    private void acknowledge(Message msg) throws Exception {
         com.sun.messaging.jmq.jmsclient.MessageImpl m = 
                 (com.sun.messaging.jmq.jmsclient.MessageImpl)msg;
         com.sun.messaging.jmq.jmsclient.SessionImpl ss = 
                 (com.sun.messaging.jmq.jmsclient.SessionImpl)session ;
         ss._appTransactedAck(m);
    }

    protected SubscribedMessage dequeue() {
        SubscribedMessage sm = _msgqueue.remove(0);
        return sm;
    }

    protected void enqueue(String subid, Message msg) throws Exception {
        _msgqueue.add(new SubscribedMessage(subid, msg));
        synchronized(_lock) {
            _lock.notifyAll();
        }
    }

    static class SubscribedMessage {

        String subid = null; 
        String msgid = null;
        Message msg = null; 

        public SubscribedMessage(String subid, String msgid) {
            this.subid = subid;
            this.msgid =  msgid;
        }

        public SubscribedMessage(String subid, Message msg) throws Exception {
            this.subid = subid;
            this.msg =  msg;
            this.msgid = msg.getJMSMessageID();
        }

        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof SubscribedMessage)) return false;

            SubscribedMessage that = (SubscribedMessage)obj;
            if (that.subid.equals(this.subid) &&
                that.msgid.equals(this.msgid)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return subid.hashCode()+msgid.hashCode();
        }
    }

    static class TransactedAck {

        String tid = null; 
        String subid = null;
        String msgid = null;
        Message msg = null;

        public TransactedAck(String tid, String subid, String msgid) {
            this.tid = tid;
            this.subid = subid;
            this.msgid = msgid;
        }

        public TransactedAck(String tid, String subid, Message msg) throws JMSException {
            this.tid = tid;
            this.subid = subid;
            this.msgid = msg.getJMSMessageID();
            this.msg = msg;
        }

        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof TransactedAck)) return false;

            TransactedAck that = (TransactedAck)obj;
            if (that.subid.equals(this.subid) &&
                that.msgid.equals(this.msgid) &&
                that.tid.equals(this.tid)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return tid.hashCode()+subid.hashCode()+msgid.hashCode();
        }

        public String toString() {
            return "tid="+tid+", subid="+subid+", msgid="+msgid;
        }
    }

    private void stop(boolean b) throws Exception {
       synchronized(_lock) {

       _locked = b;
       _lock.notifyAll();
       if (b) {
           try {
                while (_subthread != null && !closed && !_stopped) {
                    logger.log(Level.INFO, sbr.getString(sbr.I_WAITING_TXNSESSION_THREAD_STOP,
                    "["+Thread.currentThread()+"]", _subthread.toString()));

                    _lock.wait (60000);
                }
           } catch (InterruptedException e) {}

           if (closed) {
               throw new JMSException(sbr.getKString(sbr.X_TXN_SESSION_CLOSED, this.toString()));
           }
       }

       }
    }

    /**
     */
    public synchronized void close() throws Exception {
        String id = null;
        TransactedSubscriber sub = null;
        Iterator<String> itr = _subscribers.keySet().iterator();
        while(itr.hasNext()) {
            id = itr.next();
            sub = _subscribers.get(id);
            preCloseSubscriber(id);
            sub.close();
            itr.remove();
        }
        try {
            rollback();
        } catch (Exception e) {
            logger.log(Level.WARNING, sbr.getKString(
                sbr.W_TXNSESSION_ROLLBACK_FAIL, this.toString(), e.getMessage()), e);
        }

        synchronized(_lock) {
            closed = true;
            _lock.notifyAll();
        }

        session.close();
        _msgqueue.clear();
    }

    public void run() {
        while (true) {

        synchronized (_lock) {

            while (_locked || (_msgqueue.isEmpty() || getStompTransactionId() == null)) {
                if (closed) {
                     logger.log(Level.INFO, sbr.getString(sbr.I_TXNSESSION_THREAD_EXIT, this.toString()));
                     return;
                }

                _stopped = true;
                _lock.notifyAll();
                try {
                    _lock.wait();
                } catch (Exception e) {}
            }

            _stopped = false;

            SubscribedMessage sm = null;
            try {

                sm = dequeue();
                if (sm  == null) continue;

                if (_subscribers.get(sm.subid) == null) {
                    logger.log(Level.FINE, 
                    "Skip delivering message "+sm.msg.getJMSMessageID()+
                    " for transaction "+_tid+" for its subscriber "+
                    sm.subid+" has been closed");

                    continue;
                }
                logger.log(Level.FINE, "Delivering message "+ 
                           sm.msg.getJMSMessageID()+
                           " to STOMP client for subscriber "+sm.subid);
                _unackqueue.add(sm);
                _out.sendToClient(
                    StompSubscriberSession.toStompFrameMessage(
                    sm.msg, sm.subid, session, stompconn.getProtocolHandler()));

            } catch (Throwable t) {

                String[] eparam = { sm.msgid, sm.subid, t.getMessage() };
                if (t instanceof java.nio.channels.ClosedChannelException) {
                    logger.log(Level.WARNING, sbr.getKString(sbr.W_UNABLE_DELIVER_MSG_TO_TXNSUB, eparam));
                    break;
                }

                logger.log(Level.WARNING, sbr.getKString(sbr.W_UNABLE_DELIVER_MSG_TO_TXNSUB, eparam), t);

                StompFrameMessage err = null;
                try {
                    err = stompconn.getProtocolHandler().toStompErrorMessage(
                                           "getTransactionID().run", t, true);
                } catch (Throwable tt) {
                    logger.log(Level.WARNING, sbr.getKString(sbr.E_UNABLE_CREATE_ERROR_MSG, t.getMessage()), tt);
                    break;
                }
                try {
                    _out.sendToClient(err);
                } catch (Throwable ee) {
                    if (ee instanceof java.nio.channels.ClosedChannelException) {
                        logger.log(Level.WARNING, sbr.getKString(sbr.E_UNABLE_SEND_ERROR_MSG, t.getMessage(), ee.getMessage()));
                    } else {
                        logger.log(Level.WARNING, sbr.getKString(sbr.E_UNABLE_SEND_ERROR_MSG, t.getMessage(), ee.getMessage()), ee);
                    }
                }

                break;
            }
        }
        }

        synchronized(_lock) {
             _stopped = true;
             _lock.notifyAll();
        }

        try {
            close();
        } catch (Exception e) {
            logger.log(Level.FINE,  "Close transacted session "+this+" failed: "+e.getMessage(), e);
        }
        logger.log(Level.INFO, sbr.getString(sbr.I_TXNSESSION_THREAD_EXIT, this.toString()));
    }

}

class TransactedSubscriber implements StompSubscriber, MessageListener {

    private String _subid = null; 
    private StompTransactedSession _parent = null;
    private MessageConsumer _subscriber = null;
    private String _duraName = null;

    public TransactedSubscriber(String subid, MessageConsumer sub, String duraname,
                                StompTransactedSession parent) 
                                throws Exception {
        _subid = subid;
        _subscriber = sub;
        _parent = parent;
        _duraName = duraname;

    }

    public void startDelivery() throws Exception {
        _subscriber.setMessageListener(this);
    }

    public String getDuraName() {
        return _duraName;
    }

    public void startMessageDelivery() throws Exception {
        _subscriber.setMessageListener(this);
    }

    /**
     *
     */
    public void onMessage(Message msg) {

        try {
             _parent.logger.log(Level.FINE, "onMessage message "+ 
                         msg.getJMSMessageID()+" for STOMP subscriber "+_subid);
            _parent.enqueue(_subid, msg);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     *
     */
    public void close() throws Exception {
        _subscriber.close(); 
    }
}
