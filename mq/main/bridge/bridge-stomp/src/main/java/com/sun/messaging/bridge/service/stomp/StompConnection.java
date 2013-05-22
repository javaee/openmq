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

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompConnection implements ExceptionListener {

    private Logger _logger = null;

    private BridgeContext _bc = null;
    private Properties _jmsprop = null;
    private Connection _connection = null;
    private String  _connectionUID = "";

    private boolean _connectionException = false;

    private StompSenderSession _pubSession = null;
    private String _clientid = null;
    private StompBridgeResources _sbr = null;

    private Map<String, StompSubscriberSession> _subSessions = 
                                 Collections.synchronizedMap(
                                 new HashMap<String, StompSubscriberSession>());  

    private StompTransactedSession _txSession = null;
    private StompProtocolHandler _sph = null;

    public StompConnection(BridgeContext bc, Properties jmsprop, StompProtocolHandler sph) {
        _logger = StompServer.logger();
        _bc = bc;
        _jmsprop = jmsprop;
        _sph =  sph;
        _sbr = StompServer.getStompBridgeResources();
    }

    /**
     *
     */
    public synchronized String connect(String login, 
                                       String passcode,
                                       String clientid) throws Exception { 

        if (_connection != null) {
            throw new javax.jms.IllegalStateException(
            "Unexpected "+StompFrameMessage.Command.CONNECT+", already connected"); 
        }

        if (clientid == null) {
        _logger.log(Level.INFO, _sbr.getString(_sbr.I_CREATE_JMS_CONN, login));
        } else {
        _logger.log(Level.INFO, _sbr.getString(_sbr.I_CREATE_JMS_CONN_WITH_CLIENTID, login, clientid));
        }

        if (login != null) {
            _connection = _bc.getConnectionFactory(_jmsprop).createConnection(login, passcode);
        } else {
            _connection = _bc.getConnectionFactory(_jmsprop).createConnection();
        }
        if (clientid != null) {
            _clientid = clientid;
            _connection.setClientID(clientid);
        }
        ((com.sun.messaging.jmq.jmsclient.ConnectionImpl)
                                _connection)._setAppTransactedAck();
        
        _connectionUID = ((com.sun.messaging.jmq.jmsclient.ConnectionImpl)
                                             _connection)._getConnectionID()+
                                             "["+_connection.getClientID()+"]";
        
        _connection.start();

        _logger.log(Level.INFO, _sbr.getString(_sbr.I_STARTED_JMS_CONN, _connectionUID, login)); 

        return _connectionUID;
    }

    public Connection getConnection() {
        return _connection;
    }

    /**
     *
     */
    public String toString() {
        String s = _connectionUID;
        return ((s == null) ? "":s);
    }

    /**
     *
     */
    public synchronized void disconnect(boolean check) throws Exception { 
        if (check) checkConnection();

        if (_connection != null) {
            try {
                if (_pubSession != null) {
                    _pubSession.close();
                    _pubSession = null;
                 }
                if (_txSession != null) {
                    _txSession.close();
                    _txSession = null;
                }
                synchronized(_subSessions) {
                    for (String subid: _subSessions.keySet()) {
                        StompSubscriberSession ss = _subSessions.get(subid);
                        ss.close();
                    }
                    _subSessions.clear();
                }
                _connection.close();
            } catch (Exception e) {
                throw e;
            } finally {
                _connection = null;
                _connectionException = false;
            }
            _logger.log(Level.INFO, _sbr.getString(_sbr.I_STOMP_CONN_CLOSED, _connectionUID));
        } else {
            _logger.log(Level.FINE, _sbr.getString(_sbr.I_STOMP_CONN_NOT_CONNECTED, _connectionUID));
        }
    }

    /**
     *
     */
    public synchronized void beginTransactedSession(String tid)
                                             throws Exception { 
        Connection conn = _connection;
        checkConnection(conn);


        if (tid == null) {
            throw new IllegalArgumentException("Unexpected call: null transaction id");
        }

        if (_txSession == null) {
            _txSession =  new StompTransactedSession(this);
        }
        String currtid =  _txSession.getTransactionID();
        if (currtid != null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_NESTED_TXN_NOT_ALLOWED, currtid, tid));
        }
        _txSession.setTransactionID(tid);
    }

    /**
     *
     */
    public synchronized void commitTransactedSession(String tid)
                                              throws Exception { 
        Connection conn = _connection;
        checkConnection(conn);


        if (tid == null) {
            throw new IllegalArgumentException("Unexpected call: null transaction id");
        }

        if (_txSession == null) { 
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NO_SESSION, tid));
        }
        String currtid =  _txSession.getTransactionID();
        if (currtid == null || !currtid.equals(tid)) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NOT_FOUND, tid)+
                        (currtid == null ?"":" "+_sbr.getString(_sbr.I_CURRENT_TXN, currtid)));
        }
        _txSession.commit();
    }

    /**
     *
     */
    public synchronized void abortTransactedSession(String tid)
                                             throws Exception { 
        Connection conn = _connection;
        checkConnection(conn);


        if (tid == null) {
            throw new IllegalArgumentException("Unexpected call: null transaction id");
        }

        if (_txSession == null) { 
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NO_SESSION, tid));
        }
        String currtid =  _txSession.getTransactionID();
        String lastrb = _txSession.getLastRolledbackTID();
        if (currtid == null && lastrb != null && lastrb.equals(tid)) {
            _logger.log(Level.INFO, _sbr.getString(_sbr.I_TXN_ALREADY_ROLLEDBACK, tid));
            return;
        }
        if (currtid == null || !currtid.equals(tid)) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NOT_FOUND, tid)+
                        (currtid == null ?"":" "+_sbr.getString(_sbr.I_CURRENT_TXN, currtid)));
        }
        _txSession.rollback();
    }


    /**
     *
     */
    public synchronized StompTransactedSession getTransactedSession(
                                               String tid)
                                               throws Exception { 
        checkConnection();


        if (tid == null) {
            throw new IllegalArgumentException("Unexpected call: null transaction id");
        }

        if (_txSession == null) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NO_SESSION, tid));
        }
        String currtid = _txSession.getTransactionID();
        if (currtid == null || !currtid.equals(tid)) {
            throw new StompProtocolException(_sbr.getKString(_sbr.X_TXN_NOT_FOUND, tid)+
                        (currtid == null ?"":" "+_sbr.getString(_sbr.I_CURRENT_TXN, currtid)));
        }
        return _txSession;
    }

    /**
     *
     */
    public synchronized StompTransactedSession getTransactedSession()
                                               throws Exception { 
        checkConnection();

        if (_txSession == null) return null;

        if (_txSession.getTransactionID() == null) return null;

        return _txSession;
    }

    /**
     *
     */
    public synchronized StompSenderSession getSenderSession() throws Exception { 

        Connection conn = _connection;
        checkConnection(conn);

        if (_pubSession == null) {
            _pubSession = new StompSenderSessionImpl(conn);
        }
        return _pubSession;
   }

   /**
    *
    */
   public synchronized StompSubscriberSession createSubscriberSession(
                                             String subid, int ackMode) 
                                             throws Exception {
       Connection conn = _connection;
       checkConnection(conn);
       
       if (subid == null) {
           throw new IllegalArgumentException("No subscription id");
       }

       StompSubscriberSession ss = _subSessions.get(subid);
       if (ss != null) {
           throw new StompProtocolException(_sbr.getKString(_sbr.X_SUBSCRIBER_ID_EXIST, subid));
       }
       ss = new StompSubscriberSession(subid, ackMode, this);
       _subSessions.put(subid, ss);

       return ss;
    }

   /**
    *
    * @return null if not found
    */
   public synchronized StompSubscriberSession getSubscriberSession(
                                              String subid)
                                              throws Exception {
       checkConnection();
       
       if (subid == null) {
           throw new IllegalArgumentException("No subscription id");
       }

       StompSubscriberSession ss = _subSessions.get(subid);
       return ss;
    }

    /**
     * @return subid if duraname not null
     */
    public synchronized String closeSubscriber(String subid, String duraname) throws Exception {
        checkConnection();

        StompSubscriberSession ss = null;
        if (duraname == null) {
            ss = _subSessions.get(subid);
            if (ss != null) {
                ss.close();
                _subSessions.remove(subid);
                return null;
            }
        } else {
            if (_clientid == null) {
                throw new StompProtocolException(_sbr.getKString(_sbr.X_UNSUBSCRIBE_NO_CLIENTID, duraname));
            }
            String dn = null;
            for (String sid: _subSessions.keySet()) { 
                ss = _subSessions.get(sid);
                dn = ss.getDuraName();
                if (dn == null) continue;
                if (dn.equals(duraname)) {
                    ss.closeSubscriber();
                    ss.getJMSSession().unsubscribe(duraname);
                    ss.close();
                    _subSessions.remove(sid);
                    return sid;
                }
            }
        }
        
        if (_txSession != null) {
            String sid = _txSession.closeSubscriber(subid, duraname);
            if (duraname != null) return sid;
            if (sid != null) return sid;
        } else if (duraname != null) {
            getSenderSession().getJMSSession().unsubscribe(duraname);
            return null;
        }
        throw new StompProtocolException(_sbr.getKString(_sbr.X_SUBSCRIBER_ID_NOT_FOUND, subid)); 
    }

    /**
     *
     */
    public synchronized void ackNonTransacted(String subidPrefix, String msgid) throws Exception {
        checkConnection();

        StompSubscriberSession ss = null;
        for (String subid: _subSessions.keySet()) { 
            if (subid.startsWith(subidPrefix)) {
                ss = _subSessions.get(subid);
                ss.ack(msgid);
                return;
            }
        }

        throw new StompProtocolException(_sbr.getKString(
              _sbr.X_ACK_CANNOT_DETERMINE_SUBSCRIBER, msgid,
		      (StompFrameMessage.MessageHeader.SUBSCRIPTION).toString()));
    }

    /**
     *
     */
    private synchronized void checkConnection() throws Exception {
        checkConnection(_connection);
        if (_connectionException) {
            disconnect(false);
            _connectionException = false;
        }
    }

    /**
     *
     */
    private synchronized void checkConnection(Connection conn) throws Exception {
        if (conn == null) {
            throw new NotConnectedException(_sbr.getKString(_sbr.X_NOT_CONNECTED));
        }
    }

    public void onException(JMSException e) {
        _logger.log(Level.SEVERE, _sbr.getKString(_sbr.E_ONEXCEPTION_JMS_CONN, _connectionUID, e.getMessage()), e);
        _connectionException = true;
    }

    protected StompFrameMessage toStompFrameMessage(Message jmsmsg, 
                                                    String subid, 
                                                    Session ss)
                                                    throws Exception {
        return _sph.toStompFrameMessage(jmsmsg, subid, ss);
    }

}
