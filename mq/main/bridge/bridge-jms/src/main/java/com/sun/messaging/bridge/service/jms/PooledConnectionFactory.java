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

package com.sun.messaging.bridge.service.jms;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit; 
import java.util.concurrent.Executors; 
import java.util.concurrent.ScheduledFuture; 
import java.util.concurrent.ConcurrentLinkedQueue; 
import java.util.concurrent.ScheduledExecutorService; 
import javax.jms.XAConnection;
import javax.jms.Connection;
import javax.jms.JMSException;
import com.sun.messaging.bridge.service.jms.xml.JMSBridgeXMLConstant;
import com.sun.messaging.bridge.service.jms.resources.JMSBridgeResources;

/**
 *
 * @author amyk
 *
 */
public class PooledConnectionFactory implements Runnable {
    
    public static final String POOL_IDLE_TIMEOUT = "pool-idle-timeout";
    
    private Logger _logger = null;
    private Object _cf = null;
    private int _maxRetries = 0;
    private int _retryInterval = 0;

    private ScheduledExecutorService _scheduler = null;

    private ScheduledFuture _future = null;
 
    private int _idleTimeout = 0; //secs

    private ConcurrentLinkedQueue<PooledConnection> _idleConns = null;
    private ConcurrentLinkedQueue<PooledConnection> _outConns = null;
    private final EventNotifier _notifier = new EventNotifier();
    private boolean _closed = false;

    private String _username = null;
    private String _password = null;

    private static JMSBridgeResources _jbr = JMSBridge.getJMSBridgeResources();

    public PooledConnectionFactory(Object cf, Properties attrs, Logger logger) throws Exception {
        _logger = logger;
        _cf = cf;

        String val = attrs.getProperty(JMSBridgeXMLConstant.CF.USERNAME);
        if (val != null) {
            _username = val.trim();
            _password = attrs.getProperty(JMSBridgeXMLConstant.CF.PASSWORD);
        }
        
        val = attrs.getProperty(JMSBridgeXMLConstant.CF.IDLETIMEOUT,
                                JMSBridgeXMLConstant.CF.IDLETIMEOUT_DEFAULT);
        if (val != null) {
            _idleTimeout = Integer.valueOf(val).intValue();
        }
        if (_idleTimeout < 0) _idleTimeout = 0;
        val = attrs.getProperty(JMSBridgeXMLConstant.CF.CONNECTATTEMPTS,
                                JMSBridgeXMLConstant.CF.CONNECTATTEMPTS_DEFAULT);
        if (val != null) {
            _maxRetries = Integer.valueOf(val).intValue();
        }
        val = attrs.getProperty(JMSBridgeXMLConstant.CF.CONNECTATTEMPTINTERVAL,
                                JMSBridgeXMLConstant.CF.CONNECTATTEMPTINTERVAL_DEFAULT);
        if (val != null) {
            _retryInterval = Integer.valueOf(val).intValue();
        }
        if (_retryInterval < 0) _retryInterval = 0;

        _idleConns = new ConcurrentLinkedQueue<PooledConnection>();
        _outConns = new ConcurrentLinkedQueue<PooledConnection>();
 
        _scheduler = Executors.newSingleThreadScheduledExecutor(); 
        if (_idleTimeout > 0) {
            _logger.log(Level.INFO, _jbr.getString(_jbr.I_SCHEDULE_TIMEOUT_FOR_POOLCF,
                                                   _idleTimeout, this.toString()));
            _future = _scheduler.scheduleAtFixedRate(this, _idleTimeout, 
                                          _idleTimeout, TimeUnit.SECONDS);
        }
    }

    /** 
     */ 
    public Connection obtainConnection(Connection c, 
                                       String logstr,
                                       Object caller, boolean doReconnect) 
                                       throws Exception {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Obtaining pooled connection from pooled connection factory "+this);
        }

        if (_closed)  {
            if (c == null) {
            throw new JMSException(_jbr.getKString(_jbr.X_POOLED_CF_CLOSED, this.toString()));
            }
            try {
            c.close();
            } catch (Exception e) {
            _logger.log(Level.WARNING, "Unable to close connection in pooled connection factory "+this);
            }
            throw new JMSException(_jbr.getKString(_jbr.X_POOLED_CF_CLOSED, this.toString()));
        }

        PooledConnection pconn = null;
        if (c != null) {
            if (c instanceof XAConnection) {
                pconn = new PooledXAConnectionImpl((XAConnection)c);
            } else {
                pconn = new PooledConnectionImpl(c);
            }
            _idleConns.offer(pconn);
        }

        while (true) {

        pconn =  _idleConns.poll();
        if (pconn == null) {
            if (_closed) {
               throw new JMSException(_jbr.getKString(_jbr.X_POOLED_CF_CLOSED, this.toString()));
            }
            Connection cn = null;
            EventListener l = new EventListener(this);
            try {
            _notifier.addEventListener(EventListener.EventType.CONN_CLOSE, l);
            cn = JMSBridge.openConnection(_cf, _maxRetries, _retryInterval, _username, _password,
                                          logstr, caller, l, _logger, doReconnect);
            } finally { 
            _notifier.removeEventListener(l);
            }
            if (cn instanceof XAConnection) { 
                pconn = new PooledXAConnectionImpl((XAConnection)cn);
            } else {
                pconn = new PooledConnectionImpl(cn);
            }
        } 
        if (!_closed && pconn.isValid()) {
            pconn.idleEnd();
            _outConns.offer(pconn);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, 
                "Obtained pooled connection "+pconn+" from pooled connection factory "+this);
            }
            return (Connection)pconn;
        }
        try {
             if (_closed) {
                _logger.log(Level.INFO, 
                "Closing connection "+pconn+" for pooled connection factory "+this+" is closed");
             } else if (!pconn.isValid()) {
                _logger.log(Level.INFO, _jbr.getString(_jbr.I_CLOSE_INVALID_CONN_IN_POOLCF,
                                                       pconn.toString(), this.toString()));
             }

             ((Connection)pconn).close();
        } catch (Exception e) {
            _logger.log(Level.WARNING, 
            "Unable to close connection "+pconn+" in pooled connection factory "+this+": "+ e.getMessage());
        }

        } //while
    }

    /** 
     */ 
    public void returnConnection(Connection conn) 
                               throws Exception {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Returning pooled connection "+conn+" to pooled connection factory "+this);
        }
        if (!(conn instanceof PooledConnection)) {
           throw new IllegalArgumentException(
           "Connection "+conn+" is not a pooled connection, can't return to pooled connection factory "+this);
        }
        if (!_outConns.contains(conn)) {
           throw new IllegalStateException(
           "Connection "+conn+" is not a in-use in pooled connection factory "+this);
        }

        _outConns.remove((PooledConnection)conn);
        ((PooledConnection)conn).idleStart();
        _idleConns.offer((PooledConnection)conn);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Returned pooled connection "+conn+" to pooled connection factory "+this);
        }
    }

    public void close() {
        _closed = true;
        _logger.log(Level.INFO, _jbr.getString(_jbr.I_CLOSE_POOLCF, this.toString()));

        _notifier.notifyEvent(EventListener.EventType.CONN_CLOSE, this);
        if (_outConns.size() > 0) {
            _logger.log(Level.WARNING, _jbr.getString(_jbr.W_FORCE_CLOSE_POOLCF, this.toString()));
        }
        if (_future != null) {
            _scheduler.shutdownNow();
            try {
            _scheduler.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
        }
        run();

    }

    public void run() {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Check idle timeout in pooled connection factory "+this);
        }
        ArrayList list = new ArrayList();
        PooledConnection c = _idleConns.peek();
        list.add(c);
        while (c != null) {

            long idlestime = c.getIdleStartTime();
            if (idlestime <= 0 && c.isValid() && !_closed) continue;

            c = _idleConns.poll();
            if (c == null) return;

            if (!c.isValid() || 
                (System.currentTimeMillis() - idlestime > _idleTimeout) || _closed) {

                _logger.log(Level.INFO, (c.isValid() ? 
                        _jbr.getString(_jbr.I_CLOSE_TIMEOUT_CONN_IN_POOLCF, c.toString(), this.toString()):
                        _jbr.getString(_jbr.I_CLOSE_INVALID_CONN_IN_POOLCF, c.toString(), this.toString())));

                try {
                    ((Connection)c).close();
                } catch (Exception e) {
                    _logger.log(Level.WARNING, 
                   "Failed to close "+(_closed ? "":(c.isValid() ? "idle timed out connection ":"invalid connection "))+c+
                  " in pooled connection factory "+this);
                   c.invalid();
                   _idleConns.offer(c);
                }
            } else {
                _idleConns.offer(c);
            }
            c =_idleConns.peek();
            if (list.contains(c)) break;
            list.add(c);
        }
    }

    public Object getCF() {
        return _cf;
    }

    public int getIdleTimeout() {
        return _idleTimeout;
    }

    public int getMaxRetries() {
        return _maxRetries;
    }

    public int getRetryInterval() {
        return _retryInterval;
    }

    public int getNumIdleConns() {
        return _idleConns.size();
    }

    public int getNumInUseConns() {
        return _outConns.size();
    }

    public String toString() {
        return _cf+"["+_outConns.size()+", "+_idleConns.size()+"]";
    }
}

