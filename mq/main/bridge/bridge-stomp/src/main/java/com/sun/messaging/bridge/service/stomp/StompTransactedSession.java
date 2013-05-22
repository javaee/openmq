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
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompTransactedSession implements StompSenderSession, Runnable {

    protected  Logger _logger = null;

    private final static int MAX_QUEUE_SIZE = 100;

    private String _lastRolledbackTID = null;

    private Connection _connection = null;
    private Session _session = null;
    private MessageProducer _producer = null;

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
    private boolean _closed = false;
    private boolean _locked = false;  //to lock _subthread
    private boolean _stopped = false; //when _subthread is locked
    private Thread _subthread = null;
    private StompConnection _stompc = null;
    private StompBridgeResources _sbr = null;

    public StompTransactedSession(StompConnection sc)
                                throws Exception {
        _logger = StompServer.logger();

        _sbr = StompServer.getStompBridgeResources();
        _stompc = sc;
        _connection = sc.getConnection();
        _session = _connection.createSession(true, 0); 

        _logger.log(Level.INFO, _sbr.getString(_sbr.I_CREATED_TXN_SESSION, this.toString()));

        _producer = _session.createProducer(null);
    }

    public String toString() {
        return "["+_connection+", "+_session+", "+_tid+"]";
    }

    /**
     * 
     */
    public MessageProducer getJMSProducer() throws Exception {
        checkSession();
        return _producer;
    }

    /**
     *
     */
    public synchronized void createSubscriber(String subid, 
                             Destination dest, String selector,
                             String duraname, boolean nolocal,
                             StompOutputHandler out)
                             throws Exception {
        _out = out;
        checkSession();

        MessageConsumer sub = null;

        if (_subscribers.get(subid) != null) {
            throw new JMSException(_sbr.getKString(
                _sbr.X_SUBID_ALREADY_EXIST_IN_TXN_SESSION, subid, this.toString()));
        }

        String destname = null;

        if (dest instanceof Queue) {
            sub = _session.createConsumer(dest, selector);
            destname = ((Queue)dest).getQueueName();  
        } else if (duraname != null) {
            sub = _session.createDurableSubscriber(
                           (Topic)dest, duraname, selector, nolocal);
            destname = ((Topic)dest).getTopicName();  
        } else {
           sub = _session.createConsumer(dest, selector, nolocal);
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
        _logger.log(Level.INFO, _sbr.getString(_sbr.I_CREATED_TXN_SUB, param));
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
                _session.unsubscribe(duraname);
                return id;
            }
        }
        }
        _session.unsubscribe(duraname);
        return null;
    }

    private void preCloseSubscriber(String subid) throws Exception {

        _connection.stop();
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
                        _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_UNABLE_ACK_MSG_ON_CLOSE_SUB, eparam), e);
                    }
                    itr.remove();
                }
            }
        }
        
        } finally {
        _connection.start();
        }
    }

    /**
     *
     */
    public String getTransactionID() {
        synchronized(_lock) {
            return _tid;
        }
    }

    /**
     *
     */
    public synchronized void setTransactionID(String tid) {

         if (_ackedqueue.size() != 0) { 
             _logger.log(Level.WARNING, 
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

    /**
     *
     */
    public synchronized void commit() throws Exception {
        _logger.log(Level.FINE, "Committing transaction "+_tid+ " on JMS session "+_session);

        boolean stopped = false;
        try {
            if (_ackedqueue.size() > 0) {
                stopped = true;
                _connection.stop();
               synchronized(_ackedqueue) {
                   TransactedAck ta = null;
                   Iterator<TransactedAck> itr = _ackedqueue.iterator();
                   while (itr.hasNext()) {
                       ta = itr.next();
                       if (!ta.tid.equals(_tid)) {
                       throw new JMSException(
                       "Transaction ack ["+ta+"] tid not match current transaction id "+_tid);
                       }
                       _logger.log(Level.FINE, "Ack message "+ta.msgid+ " for committing transaction "+_tid);
                       acknowledge(ta.msg);
                       itr.remove();
                   }
               }
            }

            _session.commit();
        } catch (Exception e)  {
            String emsg = _sbr.getKString(_sbr.E_COMMIT_FAIL_WILL_ROLLBACK, _tid, e.getMessage());
            _logger.log(Level.SEVERE, emsg); 

            try {
            rollback(); 

            } finally {
            JMSException je = new JMSException(emsg);
            je.initCause(e);
            throw je;
            }

        } finally {
            setTransactionID(null);
            _ackedqueue.clear();
            if (stopped) _connection.start();
        } 
    }
  
    /**
     *
     */
    public synchronized String getLastRolledbackTID() {
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
        _connection.stop();
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
                    String[] eparam = {ta.msg.getJMSMessageID(), getTransactionID(), e.getMessage()};
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_TXNACK_MSG_ON_ROLLBACK_FAIL, eparam), e);
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
                    String[] eparam = { sm.msg.getJMSMessageID(), getTransactionID(), e.getMessage() };
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_TXNACK_DELIVERED_MSG_ON_ROLLBACK_FAIL, eparam), e);
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
                    String[] eparam = { sm.msg.getJMSMessageID(), getTransactionID(), e.getMessage() };
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_TXNACK_UNDELIVERED_MSG_ON_ROLLBACK_FAIL, eparam), e);
                    }
                }
                itr.remove();
            }

        }

        _session.rollback();

        } finally {
            _lastRolledbackTID = _tid;
            setTransactionID(null);
            _ackedqueue.clear();
            stop(false);
            _connection.start();
        }

    }

    /**
     *
     */
    public void ack(String subid, String msgid) throws Exception {
        ack(subid, msgid, false);
    }

    public synchronized void ack(String subid, 
                                 String msgid, 
                                 boolean prefix)
                                 throws Exception {
        checkSession();

        if (getTransactionID() == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXNACK_NO_CURRENT_TRANSACTION, msgid, subid));
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
                    throw new JMSException(_sbr.getKString(_sbr.X_SUBID_NOT_FOUND_IN_TXN, eparam));
                } else {
                    String[] eparam = {msgid, _tid, (StompFrameMessage.MessageHeader.SUBSCRIPTION).toString()};
                    throw new JMSException(_sbr.getKString(_sbr.X_ACK_CANNOT_DETERMINE_SUBSCRIBER_IN_TXN, eparam));
                }
            }
        }

        synchronized(_unackqueue) {

            SubscribedMessage sm = new SubscribedMessage(subid, msgid);
            int index =  _unackqueue.indexOf(sm);
            if (index == -1) { 
                if (!_ackedqueue.contains(new TransactedAck(_tid, subid, sm.msgid))) {
                    String[] eparam = {msgid, subid, _tid};
                    throw new StompProtocolException(_sbr.getKString(_sbr.X_MSG_NOT_FOUND_IN_TXN, eparam));
                }
                if (_logger.isLoggable(Level.INFO)) {
                   
                    _logger.log(Level.INFO, "Message "+msgid+ " for subcriber "+subid+" has already acked in transaction "+_tid);
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
                 (com.sun.messaging.jmq.jmsclient.SessionImpl)_session ;
         ss._appTransactedAck(m);
    }


    /**
     *
     */
    public Session getJMSSession() throws Exception {
        checkSession();
        return _session;
    }

    private void checkSession() throws Exception { 
        if (_closed) {
            throw new JMSException("Session closed !");
        }
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

    class SubscribedMessage {

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

    class TransactedAck {

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
                while (_subthread != null && !_closed && !_stopped) {
                    _logger.log(Level.INFO, _sbr.getString(_sbr.I_WAITING_TXNSESSION_THREAD_STOP,
                    "["+Thread.currentThread()+"]", _subthread.toString()));

                    _lock.wait (60000);
                }
           } catch (InterruptedException e) {}

           if (_closed) {
               throw new JMSException(_sbr.getKString(_sbr.X_TXN_SESSION_CLOSED, this.toString()));
           }
       }

       }
    }

    /**
     *
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
            _logger.log(Level.WARNING, _sbr.getKString(
                _sbr.W_TXNSESSION_ROLLBACK_FAIL, this.toString(), e.getMessage()), e);
        }

        synchronized(_lock) {
            _closed = true;
            _lock.notifyAll();
        }

        _session.close();
        _msgqueue.clear();
    }

    public void run() {
        while (true) {

        synchronized (_lock) {

            while (_locked || (_msgqueue.isEmpty() || getTransactionID() == null)) {
                if (_closed) {
                     _logger.log(Level.INFO, _sbr.getString(_sbr.I_TXNSESSION_THREAD_EXIT, this.toString()));
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
                     _logger.log(Level.FINE, 
                     "Skip delivering message "+sm.msg.getJMSMessageID()+
                      " for transaction "+_tid+" for its subscriber "+
                      sm.subid+" has been closed");

                      continue;
                 }
                 _logger.log(Level.FINE, "Delivering message "+ 
                             sm.msg.getJMSMessageID()+
                             " to STOMP client for subscriber "+sm.subid);
                 _unackqueue.add(sm);
                _out.sendToClient(_stompc.toStompFrameMessage(
                                          sm.msg, sm.subid, _session));

            } catch (Throwable t) {

                String[] eparam = { sm.msgid, sm.subid, t.getMessage() };
                if (t instanceof java.nio.channels.ClosedChannelException) {
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_UNABLE_DELIVER_MSG_TO_TXNSUB, eparam));
                    break;
                }

                _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_UNABLE_DELIVER_MSG_TO_TXNSUB, eparam), t);

                StompFrameMessage err = null;
                try {
                    err = StompProtocolHandler.toStompErrorMessage(
                                           "getTransactionID().run", t, true);
                } catch (Throwable tt) {
                    _logger.log(Level.WARNING, _sbr.getKString(_sbr.E_UNABLE_CREATE_ERROR_MSG, t.getMessage()), tt);
                    break;
                }
                try {
                    _out.sendToClient(err);
                } catch (Throwable ee) {
                    if (ee instanceof java.nio.channels.ClosedChannelException) {
                        _logger.log(Level.WARNING, _sbr.getKString(_sbr.E_UNABLE_SEND_ERROR_MSG, t.getMessage(), ee.getMessage()));
                    } else {
                        _logger.log(Level.WARNING, _sbr.getKString(_sbr.E_UNABLE_SEND_ERROR_MSG, t.getMessage(), ee.getMessage()), ee);
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
            _logger.log(Level.FINE,  "Close transacted session "+this+" failed: "+e.getMessage(), e);
        }
        _logger.log(Level.INFO, _sbr.getString(_sbr.I_TXNSESSION_THREAD_EXIT, this.toString()));
    }

}

class TransactedSubscriber implements MessageListener {

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
             _parent._logger.log(Level.FINE, "onMessage message "+ 
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
