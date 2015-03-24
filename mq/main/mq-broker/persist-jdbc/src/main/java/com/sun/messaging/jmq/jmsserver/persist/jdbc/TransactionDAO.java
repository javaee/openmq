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

/*
 * @(#)TransactionDAO.java	1.14 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;
import com.sun.messaging.jmq.jmsserver.data.TransactionBroker;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.persist.api.TransactionInfo;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

/**
 * This class is an interface for the Transaction table which will be implemented
 * by database specific code.
 */
public interface TransactionDAO extends BaseDAO {

    /**
     * Destination table:
     * Holds all the destination in the system.
     *
     * CREATE TABLE MQTXN<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      ID                  NUMBER NOT NULL,
     *      TYPE                INTERGER NOT NULL,
     *      STATE               INTEGER NOT NULL,
     *      AUTO_ROLLBACK       INTEGER NOT NULL,
     *      XID                 VARCHAR(100),
     *      TXN_STATE           LONGVARBINARY NOT NULL,
     *      TXN_HOME_BROKER     LONGVARBINARY,
     *      TXN_BROKERS         LONGVARBINARY,
     *      STORE_SESSION_ID    BIGINT NOT NULL,
     *      EXPIRED_TS_COLUMN   BIGINT NOT NULL,
     *      ACCESSED_TS_COLUMN  BIGINT NOT NULL
     *      PRIMARY KEY(ID)
     * );
     *
     * ID - Long value of local broker transaction
     * TYPE - Type of transaction, i.e. LOCAL, CLUSTER, or REMOTE
     * STATE - State of the transaction
     * AUTO_ROLLBACK - Transaction rollback mode
     * XID - XID (if any) associated with this transaction
     * TXN_STATE - Serialized Transaction state information
     * TXN_HOME_BROKER - Transaction's home broker for a REMOTE txn
     *  (serialized BrokerAddress information)
     * TXN_BROKERS - Transaction's participant brokers for a REMOTE/CLUSTER txn
     *  (serialized array of TransactionBroker information)
     * STORE_SESSION_ID - Store session ID associated with the Broker
     *  responsible for reaping the transaction
     * EXPIRED_TS_COLUMN - Timestamp when the transaction expired
     * ACCESSED_TS_COLUMN - Timestamp when the transaction was last accessed
     */
    public static final String TABLE = "MQTXN";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String TYPE_COLUMN = "TYPE";
    public static final String STATE_COLUMN = "STATE";
    public static final String AUTO_ROLLBACK_COLUMN = "AUTO_ROLLBACK";
    public static final String XID_COLUMN = "XID";
    public static final String TXN_STATE_COLUMN = "TXN_STATE";
    public static final String TXN_HOME_BROKER_COLUMN = "TXN_HOME_BROKER";
    public static final String TXN_BROKERS_COLUMN = "TXN_BROKERS";
    public static final String STORE_SESSION_ID_COLUMN = "STORE_SESSION_ID";
    public static final String EXPIRED_TS_COLUMN = "EXPIRED_TS";
    public static final String ACCESSED_TS_COLUMN = "ACCESSED_TS";

    void insert( Connection conn, TransactionUID txnUID,
        TransactionState txnState, long storeSessionID )
        throws BrokerException;

    void insert( Connection conn, TransactionUID txnUID,
        TransactionState txnState, BrokerAddress txnHomeBroker,
        TransactionBroker[] txnBrokers, int type, long storeSessionID )
        throws BrokerException;

    void updateTransactionState( Connection conn, TransactionUID txnUID,
        TransactionState state, boolean replaycheck ) throws BrokerException;

    public void updateTransactionHomeBroker( Connection conn, TransactionUID txnUID,
        BrokerAddress txnHomeBroker) throws BrokerException;

    public void updateTransactionBrokers( Connection conn, TransactionUID txnUID,
        TransactionBroker[] brokers) throws BrokerException;

    public void updateTransactionBrokerState( Connection conn, TransactionUID txnUID,
        int expectedTxnState,TransactionBroker txnBroker) throws BrokerException;

    void updateAccessedTime( Connection conn, TransactionUID txnUID,
        long timeStamp ) throws BrokerException;

    void delete( Connection conn, TransactionUID txnUID ) throws BrokerException;

    TransactionState getTransactionState( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    BrokerAddress getTransactionHomeBroker( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    TransactionBroker[] getTransactionBrokers( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    TransactionInfo getTransactionInfo( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    long getAccessedTime( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    List getTransactionsByBroker( Connection conn, String brokerID )
        throws BrokerException;

    List getRemoteTransactionsByBroker( Connection conn, String brokerID )
        throws BrokerException;

    HashMap getTransactionStatesByBroker( Connection conn, String brokerID, Long storeSession )
        throws BrokerException;

    HashMap getRemoteTransactionStatesByBroker( Connection conn, String brokerID, Long storeSession )
        throws BrokerException;

    int[] getTransactionUsageInfo( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    void checkTransaction( Connection conn, long txnUID ) throws BrokerException;

    boolean hasTransaction( Connection conn, long txnUID ) throws BrokerException;
}
