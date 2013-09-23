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
 *
 */

/*
 * @(#)MessageDAO.java	1.24 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.SysMessageID;

import java.sql.Connection;
import java.io.IOException;
import java.util.List;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is an interface for the Message table which will be implemented
 * by database specific code.
 */
public interface MessageDAO extends BaseDAO {

    /**
     * Message table:
     * Holds persisted messages.
     *
     * CREATE TABLE IMQMSG<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      ID                  VARCHAR(100),
     *      MESSAGE             LONGVARBINARY,
     *      MESSAGE_SIZE        BIGINT,
     *      STORE_SESSION_ID    BIGINT NOT NULL,
     *      DESTINATION_ID      VARCHAR(100),
     *      TRANSACTION_ID      BIGINT,
     *      CREATED_TS          BIGINT,
     *      PRIMARY KEY(ID)
     * );
     *
     * ID - String format of SysMessageID for the message
     * MESSAGE - Wire format of message packet
     * MESSAGE_SIZE - Byte count of this message
     * STORE_SESSION_ID - Store session ID associated with the Broker
     *  responsible for routing the message
     * DESTINATION_ID - Unique name of the Destination of the message
     * TRANSACTION_ID - Transaction ID associated with an acknowledgement
     * 	(Sent when a message has sent in a transaction but not committed)
     * CREATED_TS - Timestamp when the message is created
     */
    public static final String TABLE = "MQMSG";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String SYSMESSAGE_ID_COLUMN = "SYSMESSAGE_ID";
    public static final String MESSAGE_COLUMN = "MESSAGE";
    public static final String MESSAGE_SIZE_COLUMN = "MESSAGE_SIZE";
    public static final String STORE_SESSION_ID_COLUMN = "STORE_SESSION_ID";
    public static final String DESTINATION_ID_COLUMN = "DESTINATION_ID";
    public static final String TRANSACTION_ID_COLUMN = "TRANSACTION_ID";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";

    void insert( Connection conn, DestinationUID dstUID, Packet message,
        ConsumerUID[] consumerUIDs, int[] states, long storeSessionID,
        long createdTime, boolean checkMsgExist, boolean replaycheck )
        throws BrokerException;

    void insert( Connection conn, String dstID, Packet message, 
        ConsumerUID[] consumerUIDs, int[] states, long storeSessionID,
        long createdTime, boolean checkMsgExist, boolean replaycheck )
        throws BrokerException;

    /**
     * This method is for special case where ID column is found
     * corrupted for a message after loaded from the database table  
     * however the packet in MESSAGE column is found intact
     */
    void repairCorruptedSysMessageID( Connection conn, 
        SysMessageID realSysId, String badSysIdStr, String duidStr)
        throws BrokerException;

    void moveMessage( Connection conn, Packet message,
        DestinationUID from, DestinationUID to, ConsumerUID[] consumerUIDs,
        int[] states ) throws IOException, BrokerException;

    void delete( Connection conn, DestinationUID dstUID, String id, boolean replaycheck )
        throws BrokerException;

    int deleteByDestinationBySession( Connection conn, 
        DestinationUID dstUID, Long storeSession)
        throws BrokerException;

    String getBroker( Connection conn, DestinationUID dstUID, String id )
        throws BrokerException;

    Packet getMessage( Connection conn, DestinationUID dstUID, SysMessageID sysMsgID )
        throws BrokerException;

    Packet getMessage( Connection conn, DestinationUID dstUID, String id )
        throws BrokerException;

    List getMessagesByBroker( Connection conn, String brokerID )
        throws BrokerException;

    Map<String, String> getMsgIDsAndDstIDsByBroker( Connection conn, String brokerID )
        throws BrokerException;

    List getIDsByDst( Connection conn, Destination dst, String brokerID, Long storeSession )
        throws BrokerException;

    Enumeration messageEnumeration( Destination dst, String brokerID, Long storeSession  ) 
        throws BrokerException;

    Enumeration messageEnumerationCursor( Destination dst, String brokerID, Long storeSession ) 
        throws BrokerException;

    boolean hasMessageBeenAcked( Connection conn, DestinationUID dstUID,
        SysMessageID sysMsgID ) throws BrokerException;
    
    boolean hasMessage( Connection conn, String id ) throws BrokerException;

    void checkMessage( Connection conn, String dstID, String id )
        throws BrokerException;

    int getMessageCount( Connection conn, String brokerID )
        throws BrokerException;

    HashMap getMessageStorageInfo( Connection conn, Destination dst, Long storeSession )
        throws BrokerException;
}
