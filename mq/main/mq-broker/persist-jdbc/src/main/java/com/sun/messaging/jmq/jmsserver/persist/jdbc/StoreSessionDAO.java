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
 * @(#)StoreSessionDAO.java	1.7 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * This class is an interface for the Store Session table which will be
 * implemented by database specific code.
 */
public interface StoreSessionDAO extends BaseDAO {

    /**
     * Store Session table:
     * Keep track of store sessions that a broker currently owns.
     *
     * CREATE TABLE MQSES<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      ID              BIGINT NOT NULL,
     *      BROKER_ID	    VARCHAR(100) NOT NULL,
     *      IS_CURRENT      INTEGER NOT NULL,
     *      CREATED_BY      VARCHAR(100) NOT NULL,
     *      CREATED_TS      BIGINT NOT NULL,
     *      PRIMARY KEY(ID)
     * );
     *
     * ID - Unique store session ID associated with this run of the broker
     * BROKER_ID - Broker ID that owns or responsible for routing the messages
     *  associated with this session
     * IS_CURRENT - Specify whether the session is current
     * CREATED_BY_COLUMN - Broker ID that creates this session
     * CREATED_TS_COLUMN - Timestamp when the session created
     */
    public static final String TABLE = "MQSES";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String BROKER_ID_COLUMN = "BROKER_ID";
    public static final String IS_CURRENT_COLUMN = "IS_CURRENT";
    public static final String CREATED_BY_COLUMN = "CREATED_BY";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";

    long insert( Connection conn, String brokerID, long sessionID, boolean failExist )
        throws BrokerException;

    void insert( Connection conn, String brokerID, long sessionID,
                 int isCurrent, String createdBy, long createdTS )
        throws BrokerException;

    void delete( Connection conn, long id ) throws BrokerException;

    void deleteByBrokerID( Connection conn, String brokerID )
        throws BrokerException;

    List<Long> deleteInactiveStoreSession( Connection conn ) throws BrokerException;

    List<Long> takeover( Connection conn, String brokerID, String targetBrokerID )
        throws BrokerException;

    long getStoreSession( Connection conn, String brokerID )
        throws BrokerException;

    String getStoreSessionOwner( Connection conn, long sessionID )
        throws BrokerException;

    boolean ifOwnStoreSession( Connection conn, long id, String brokerID )
        throws BrokerException;

    String getStoreSessionCreator( Connection conn, long sessionID )
        throws BrokerException;

    Map getAllStoreSessions( Connection conn ) throws BrokerException;

    List<Long> getStoreSessionsByBroker( Connection conn, String brokerID )
        throws BrokerException;

    boolean isCurrent( Connection conn, long sessionID ) throws BrokerException;

    void moveStoreSession( Connection conn, long sessionID, 
                   String targetBrokerID ) throws BrokerException;

}
