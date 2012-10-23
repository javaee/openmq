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
 * @(#)ConsumerStateDAO.java	1.18 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;
import com.sun.messaging.jmq.io.SysMessageID;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

/**
 * This class is an interface for the Consumer State table which will be
 * implemented by database specific code.
 */
public interface ConsumerStateDAO extends BaseDAO {

    /**
     * Consumer State table:
     * This table is used to handle processing of message acknowledgements.
     * Associates a message to a consumer it was sent to and tracks the
     * acknowledgement state. For durable subscription and queue receivers
     * only. Unique Key is MESSAGE_ID + CONSUMER_ID.
     *
     * CREATE TABLE MQCONSTATE<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      MESSAGE_ID          VARCHAR(100) NOT NULL,
     *      CONSUMER_ID         BIGINT NOT NULL,
     *      STATE               INTEGER,
     *      TRANSACTION_ID      BIGINT,
     *      CREATED_TS          BIGINT NOT NULL
     * );
     *
     * MESSAGE_ID - SysMessageID for the message
     * CONSUMER_ID - Long value of the ConsumerUID of the consumer object
     * STATE - State of the consumer with respect to the message
     * TRANSACTION_ID - Long value of the TransactionUID associated with an
     * 	acknowledgement (sent when a message has been acknowledged but not committed)
     * CREATED_TS - Timestamp when the entry was created
     */
    public static final String TABLE = "MQCONSTATE";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String MESSAGE_ID_COLUMN = "MESSAGE_ID";
    public static final String CONSUMER_ID_COLUMN = "CONSUMER_ID";
    public static final String STATE_COLUMN = "STATE";
    public static final String TRANSACTION_ID_COLUMN = "TRANSACTION_ID";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";

    void insert( Connection conn, String dstID, SysMessageID sysMsgID,
        ConsumerUID[] consumerUIDs, int[] states, boolean checkMsgExist, boolean replaycheck )
        throws BrokerException;

    void updateState( Connection conn, DestinationUID dstUID,
        SysMessageID sysMsgID, ConsumerUID consumerUID, int state, boolean replaycheck )
        throws BrokerException;

    void updateState( Connection conn, DestinationUID dstUID,
        SysMessageID sysMsgID, ConsumerUID consumerUID, int newState,
        int expectedState ) throws BrokerException;

    void updateTransaction( Connection conn, SysMessageID sysMsgID,
        ConsumerUID consumerUID, TransactionUID txnUID ) throws BrokerException;

    void clearTransaction( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    void deleteByMessageID( Connection conn, SysMessageID sysMsgID )
        throws BrokerException;

    public void deleteByTransaction( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    void deleteByDestinationBySession( Connection conn, 
        DestinationUID dstUID, Long storeSession )
        throws BrokerException;

    int getState( Connection conn, SysMessageID sysMsgID,
        ConsumerUID consumerUID ) throws BrokerException;

    HashMap getStates( Connection conn, SysMessageID sysMsgID )
        throws BrokerException;

    long getTransaction( Connection conn, SysMessageID sysMsgID,
        ConsumerUID consumerUID ) throws BrokerException;

    List getConsumerUIDs( Connection conn, SysMessageID sysMsgID )
        throws BrokerException;

    List getTransactionAcks( Connection conn, TransactionUID txnUID )
        throws BrokerException;

    HashMap getAllTransactionAcks( Connection conn ) throws BrokerException;
}
