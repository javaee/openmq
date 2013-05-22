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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;


/**
 * @author amyk 
 */
public class StompSubscriberSession implements MessageListener {

    private Logger _logger = null;

    private String _subid = null;

    private Session _session = null;
    private MessageConsumer _subscriber = null;

    private StompOutputHandler _out = null;
    private ArrayList<Message> _unacked = new ArrayList<Message>();
    private String _duraName = null;
    private StompConnection _stompc;
    private StompBridgeResources _sbr = null;
    private boolean _clientack = false;
    private int _ackfailureCount = 0;
    private int MAX_CONSECUTIVE_ACK_FAILURES = 3;

    public StompSubscriberSession(String id, int ackMode,
                                  StompConnection sc) throws Exception {
        _stompc = sc;
        _subid = id;
        _sbr = StompServer.getStompBridgeResources();
        _session = sc.getConnection().createSession(false, ackMode);
        _clientack = (_session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);
     
    }

    public void createSubscriber(Destination dest, String selector, 
                                 String duraname, boolean nolocal,
                                 StompOutputHandler out) 
                                 throws Exception {
        _logger = StompServer.logger();

        if (_subscriber != null) {
            throw new javax.jms.IllegalStateException("createSubscriber(): Unexpected call");
        }
        _out = out;

        if (dest instanceof Queue) {
            _subscriber = _session.createConsumer(dest, selector);
        } else if (duraname != null) { 
            _subscriber = _session.createDurableSubscriber(
                                   (Topic)dest, duraname, selector, nolocal);
            _duraName = duraname;
        } else {
           _subscriber = _session.createConsumer(dest, selector, nolocal);
        }
        _subscriber.setMessageListener(this);
            
    }

    public void closeSubscriber() throws Exception {
        if (_subscriber != null) _subscriber.close();
    }

    public Session getJMSSession() {
        return _session;
    }

    public String getDuraName() {
        return _duraName;
    }

    public void onMessage(Message msg) {
        String msgid = "";
        try { 
            if (_clientack) {
                synchronized(_unacked) {
                    _unacked.add(msg);
                }
            }
            msgid = msg.getJMSMessageID();
            _out.sendToClient(_stompc.toStompFrameMessage(msg, _subid, _session));
        } catch (Throwable t) {

            try {

            String[] eparam = {msgid, _subid, t.getMessage()};
            if (t instanceof java.nio.channels.ClosedChannelException) {
                _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_UNABLE_DELIVER_MSG_TO_SUB, eparam));
                RuntimeException re = new RuntimeException(t.getMessage());
                re.initCause(t);
                throw re;
            } 

            _logger.log(Level.WARNING, _sbr.getKString(_sbr.W_UNABLE_DELIVER_MSG_TO_SUB, eparam), t);

            StompFrameMessage err = null;
            try {
                err = StompProtocolHandler.toStompErrorMessage(
                          "Subscriber["+_subid+"].onMessage", t, true);

            } catch (Throwable tt) {
                _logger.log(Level.WARNING, _sbr.getKString(_sbr.E_UNABLE_CREATE_ERROR_MSG, t.getMessage()), tt);
                RuntimeException re = new RuntimeException(t.getMessage());
                re.initCause(t);
                throw re;
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
            RuntimeException re = new RuntimeException(t.getMessage());
            re.initCause(t);
            throw re;

            } finally {

            try {
            closeSubscriber();
            } catch (Exception e) {
            _logger.log(Level.FINE, "Close subscriber "+this+" failed:"+e.getMessage(), e);
            }

            }
        }
    }

    public void ack(String msgid) throws Exception {
        if (_session.getAcknowledgeMode() != Session.CLIENT_ACKNOWLEDGE) {
            throw new JMSException(_sbr.getKString(_sbr.X_NOT_CLIENT_ACK_MODE, msgid, _subid));
        }

        synchronized(_unacked) {

            Message msg = null;
            int end = _unacked.size() -1;
            boolean found = false;
            int i = 0;
            for (i = end; i >= 0; i--) { 
                msg = _unacked.get(i);
                if (msgid.equals(msg.getJMSMessageID())) {
                    try {
                        ((com.sun.messaging.jmq.jmsclient.MessageImpl)msg).acknowledgeUpThroughThisMessage(); 
                        _ackfailureCount = 0;
                    } catch (Exception e) { 
                        _ackfailureCount++;
                        JMSException ex = null;
                        if ((e instanceof JMSException) &&
                            (_session instanceof com.sun.messaging.jmq.jmsclient.SessionImpl) &&
                            ((com.sun.messaging.jmq.jmsclient.SessionImpl)_session)._appCheckRemoteException((JMSException)e)) {
                            ex = new UnrecoverableAckFailureException(
                            "An unrecoverable ACK failure has occurred in subscriber "+this);
                            ex.setLinkedException(e);
                            throw ex;
                        }
                        if (_ackfailureCount > MAX_CONSECUTIVE_ACK_FAILURES) { 
                            ex = new UnrecoverableAckFailureException(
                            "Maximum consecutive ACK failures "+MAX_CONSECUTIVE_ACK_FAILURES+" has occurred in subscriber "+this);
                            ex.setLinkedException(e);
                            throw ex;
                        }
                        throw e;
                    }
                    found = true;
                    break;
                } 
            }
            if (found) {
                for (int j = 0; j <= i; j++) { 
                    _unacked.remove(0); 
                }
                return;
            }
        }

        throw new JMSException(_sbr.getKString(_sbr.X_ACK_MSG_NOT_FOUND_IN_SUB, msgid, _subid));
    }

    public void close() throws Exception {
        try {
            _subscriber.close();
        } catch (Exception e) {
        } finally {
            try {
            _session.close();
            } finally {
            synchronized(_unacked) {
             _unacked.clear();
            }
            }
        }
    }
}
