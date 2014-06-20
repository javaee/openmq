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
 */

/*
 * @(#)DestinationDAO.java	1.12 07/24/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;

import java.sql.Connection;
import java.util.List;

/**
 * This class is an interface for the Destination table which will be implemented
 * by database specific code.
 */
public interface DestinationDAO extends BaseDAO {

    /**
     * Destination table:
     * Holds all the destination in the system.
     *
     * CREATE TABLE MQDST<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      ID                  VARCHAR(100) NOT NULL,
     *      DESTINATION         LONGVARBINARY NOT NULL,
     *      IS_LOCAL            INTEGER NOT NULL,
     *      CONNECTION_ID       BIGINT,
     *      CONNECTED_TS        BIGINT,
     *      STORE_SESSION_ID    BIGINT,
     *      CREATED_TS          BIGINT NOT NULL,
     *      PRIMARY KEY(ID)
     * );
     *
     * ID - Unique name of the Destination object
     * DESTINATION - Serialized Destination object
     * IS_LOCAL - Specify whether the destination is local
     * CONNECTION_ID - Connection ID for temporary destination
     * CONNECTED_TS - Timestamp when a temporary destination was created or
     *      when a consumer connected to the destination
     * STORE_SESSION_ID - Store session ID associated with the temporary destination
     * CREATED_TS - Timestamp when the entry was created
     */
    public static final String TABLE = "MQDST";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String DESTINATION_COLUMN = "DESTINATION";
    public static final String IS_LOCAL_COLUMN = "IS_LOCAL";
    public static final String CONNECTION_ID_COLUMN = "CONNECTION_ID";
    public static final String CONNECTED_TS_COLUMN = "CONNECTED_TS";
    public static final String STORE_SESSION_ID_COLUMN = "STORE_SESSION_ID";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";

    void insert( Connection conn, Destination destination, long storeSessionID,
        long connectedTime, long createdTime ) throws BrokerException;

    void update( Connection conn, Destination destination )
        throws BrokerException;
    
    void updateConnectedTime( Connection conn, Destination destination,
        long connectedTime ) throws BrokerException;

    boolean delete( Connection conn, DestinationUID dstUID, int type )
        throws BrokerException;

    boolean delete( Connection conn, Destination destination, Long storeSessionID )
        throws BrokerException;

    List getAllDestinations( Connection conn, String brokerID )
        throws BrokerException;

    List getLocalDestinationsByBroker( Connection conn, String brokerID )
        throws BrokerException;

    Destination getDestination( Connection conn, String destName )
        throws BrokerException;

    long getDestinationConnectedTime( Connection conn, String destName )
        throws BrokerException;

    void checkDestination( Connection conn, String destName )
        throws BrokerException;

    boolean hasDestination( Connection conn, String destName )
        throws BrokerException;
}
