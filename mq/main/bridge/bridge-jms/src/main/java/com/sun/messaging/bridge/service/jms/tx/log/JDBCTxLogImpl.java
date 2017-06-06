/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2016 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.bridge.service.jms.tx.log;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import com.sun.messaging.bridge.service.jms.tx.GlobalXid;
import com.sun.messaging.jmq.util.io.FilteringObjectInputStream;
import com.sun.messaging.bridge.service.jms.tx.BranchXid;
import com.sun.messaging.bridge.api.JMSBridgeStore;
import com.sun.messaging.bridge.api.UpdateOpaqueDataCallback;
import com.sun.messaging.bridge.service.jms.JMSBridge;
import com.sun.messaging.bridge.service.jms.resources.JMSBridgeResources;

/**
 *
 * @author amyk
 */

public class JDBCTxLogImpl extends TxLog {

    private static final String _type = TxLog.JDBCTYPE;
 
    private JMSBridgeStore _store = null;

    private boolean _inited = false;
    private boolean _closed = false;

    private static JMSBridgeResources _jbr = JMSBridge.getJMSBridgeResources();

    public void setJDBCStore(JMSBridgeStore store) 
                    throws IllegalArgumentException {
        if (store == null) {
            throw new IllegalArgumentException("null JDBC store");
        }
        _store = store;
    }

    public String getType() {
        return _type;
    }

    /**
     * @param props The properties is guaranteed to contain 
     *              "txlogDir", "txlogSuffix", 
     *              "txlogMaxBranches", "jmsbridge"
     * @param reset true to reset the txlog
     */
    public void init(Properties props, boolean reset) throws Exception {
        if (_store == null) {
            throw new IllegalStateException("JDBC store is null");
        }
        if (_logger == null) {
            throw new IllegalArgumentException("logger not set");
        }

        _jmsbridge = props.getProperty("jmsbridge");
        if (_jmsbridge == null) {
            throw new IllegalArgumentException("Property 'jmsbridge' not set");
        }
        _tmname = props.getProperty("tmname");
        if (_tmname == null) {
            throw new IllegalStateException("Property 'tmname' not set");
        }

        super.init(props, reset);

        _inited = true;
    }

    /**
     * @param lr the LogRecord to log
     */
    public void logGlobalDecision(LogRecord lr) throws Exception {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "jdbcTxLog: log global decision  "+lr);
        }

		super.checkClosedAndSetInProgress();
        try {

        _store.storeTMLogRecord(lr.getGlobalXid().toString(),
                                lr.toBytes(), _jmsbridge, false, _logger);
        } finally {
        super.setInProgress(false);
        }
    }

    /**
     * @param bxid the branch xid to update 
     * @param lr the LogRecord to identify the record to update 
     */
    public void logHeuristicBranch(BranchXid bxid, LogRecord lr) throws Exception {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "jdbcTxLog: log branch heuristic decision  "+lr);
        }

        final GlobalXid gxid = lr.getGlobalXid();
        final BranchXid branchXid = bxid;
        final LogRecord newlr = lr;

		super.checkClosedAndSetInProgress();
        try {

        UpdateOpaqueDataCallback callback = new UpdateOpaqueDataCallback() {
                         
            public Object update(Object currlr) throws Exception { 
                ObjectInputStream ois =  new FilteringObjectInputStream(
                                         new ByteArrayInputStream((byte[])currlr)); 
                LogRecord oldlr = (LogRecord)ois.readObject();
                if (oldlr == null) {
                    throw new IllegalArgumentException(
                    "Unexpected null current log record for "+gxid); 
                }
                if (!oldlr.getGlobalXid().equals(gxid)) {
                    throw new IllegalArgumentException(
                    "Unexpected global xid "+oldlr.getGlobalXid()+" from store, expected:"+gxid);
                }
                ois.close(); 
                
                if (oldlr.getBranchDecision(branchXid) == 
                    newlr.getBranchDecision(branchXid)) {
                    return currlr;
                }
                oldlr.setBranchDecision(branchXid, newlr.getBranchDecision(branchXid));
                return oldlr;
            }
        };
        _store.updateTMLogRecord(gxid.toString(), lr.toBytes(), _jmsbridge, 
                                 callback, true, true, _logger);
        } finally {
        super.setInProgress(false);
        }
    }
    
    /**
     * @param gxid the global xid record to remove 
     */
    public void reap(String gxid) throws Exception {
        String key = gxid;

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "jdbcTxLog: remove global transaction xid "+key);
        }

		super.checkClosedAndSetInProgress();
        try {

        _store.removeTMLogRecord(key, _jmsbridge, true, _logger);

        } finally {
        super.setInProgress(false);
        }
    } 


    /**
     *
     * @param gxid the GlobalXid
     * @return a copy of LogRecord corresponding gxid or null if not exist
     */
    public LogRecord getLogRecord(GlobalXid gxid) throws Exception {
        String key = gxid.toString();

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "jdbcTxLog: get log record for  xid "+key);
        }

		super.checkClosedAndSetInProgress();
        try {

        byte[] data = _store.getTMLogRecord(key, _jmsbridge, _logger);

        if (data == null) return null;

        ObjectInputStream ois =  new FilteringObjectInputStream(
                                 new ByteArrayInputStream(data)); 
        LogRecord lr = (LogRecord)ois.readObject();
        ois.close(); 
        return lr;

        } finally {
        super.setInProgress(false);
        }

    }

    public List getAllLogRecords() throws Exception {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "jdbcTxLog: get all log records");
        }

        ArrayList<LogRecord> list = new ArrayList<LogRecord>();

        super.checkClosedAndSetInProgress();
        try {

        List abytes = _store.getTMLogRecordsByName(_jmsbridge, _logger);
        if (abytes == null) return list;

        byte[] data = null;
        Iterator<byte[]> itr = abytes.iterator();
        while (itr.hasNext()) {
            data = itr.next();
            ObjectInputStream ois =  new FilteringObjectInputStream(
                                     new ByteArrayInputStream(data));
            LogRecord lr = (LogRecord)ois.readObject();
            ois.close();
            list.add(lr);
        }
        return list;

        } finally {
        super.setInProgress(false);
        }

    }

    /**
     */
    public void close() throws Exception {
        _logger.log(Level.INFO, _jbr.getString(_jbr.I_JDBCTXNLOG_CLOSE));

        super.setClosedAndWait();
        super.close();
    }
}
