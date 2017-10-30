/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)BrokerDAO.java	1.17 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import java.util.List;
import java.util.HashMap;
import java.sql.Connection;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.persist.api.HABrokerInfo;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;
import com.sun.messaging.jmq.jmsserver.cluster.api.BrokerState;

/**
 * This class is an interface for the Broker table which will be implemented
 * by database specific code.
 */
public interface BrokerDAO extends BaseDAO {

    /**
     * Broker table:
     * Holds all the broker info in a HA cluster.
     *
     * CREATE TABLE MQBKR<schemaVersion>[C<clusterID>|S<brokerID>] (
     * 		ID                  VARCHAR(100) NOT NULL,
     * 		URL                 VARCHAR(100) NOT NULL,
     * 		VERSION             INTEGER NOT NULL,
     * 		STATE               INTEGER NOT NULL,
     * 		TAKEOVER_BROKER     VARCHAR(100),
     * 		HEARTBEAT_TS        BIGINT,
     * 		PRIMARY KEY(ID)
     * );
     *
     * ID - Unique ID of the broker
     * URL - The URL of the broker (i.e. includes hostname and port)
     * VERSION - Current version of the borker
     * STATE - State of the broker
     * TAKEOVER_BROKER - Name of broker that has taken over the store
     * HEARTBEAT_TS - Timestamp periodically updated by a running borker
     */
    public static final String TABLE = "MQBKR";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String URL_COLUMN = "URL";
    public static final String VERSION_COLUMN = "VERSION";
    public static final String STATE_COLUMN = "STATE";
    public static final String TAKEOVER_BROKER_COLUMN = "TAKEOVER_BROKER";
    public static final String HEARTBEAT_TS_COLUMN = "HEARTBEAT_TS";

    void insert( Connection conn, String id, String takeoverID, String url,
        int version, int state, Long sessionID, long heartbeat, List<UID> additionalSessions )
        throws BrokerException;

    UID update( Connection conn, String id, int updateType, Object oldValue, Object newValue )
        throws BrokerException;

    Long updateHeartbeat( Connection conn, String id )
        throws BrokerException;

    Long updateHeartbeat( Connection conn, String id,
        long lastHeartbeat ) throws BrokerException;

    boolean updateState( Connection conn, String id, BrokerState newState,
        BrokerState expectedState, boolean local) throws BrokerException;

    void delete( Connection conn, String id ) throws BrokerException;

    HABrokerInfo takeover( Connection conn, String brokerID,
        String targetBrokerID, long lastHeartbeat, BrokerState expectedState,
        long newHeartbeat, BrokerState newState) throws BrokerException;

    long getHeartbeat( Connection conn, String id ) throws BrokerException;

    HashMap getAllHeartbeats( Connection conn ) throws BrokerException;

    BrokerState getState( Connection conn, String id ) throws BrokerException;

    Object[] getAllStates( Connection conn ) throws BrokerException;

    HABrokerInfo getBrokerInfo( Connection conn, String id ) throws BrokerException;

    HashMap getAllBrokerInfos( Connection conn, boolean loadSession )
        throws BrokerException;

    HashMap getAllBrokerInfosByState( Connection conn, BrokerState state )
        throws BrokerException;

    boolean isBeingTakenOver( Connection conn, String id ) throws BrokerException;
}
