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
 * @(#)ConsumerDAO.java	1.11 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;

import java.sql.Connection;
import java.util.List;

/**
 * This class is an interface for the Consumer table which will be implemented
 * by database specific code.
 */
public interface ConsumerDAO extends BaseDAO {

    /**
     * Consumer table:
     * Holds durable subscriptions.
     *
     * CREATE TABLE MQCON<schemaVersion>[C<clusterID>|S<brokerID>] (
     *      ID		    BIGINT NOT NULL,
     *      CLIENT_ID       VARCHAR(1024),
     *      DURABLE_NAME    VARCHAR(1024),
     *      CONSUMER	    LONGVARBINARY NOT NULL,
     *      CREATED_TS      BIGINT NOT NULL,
     *      PRIMARY KEY(ID)
     * );
     *
     * ID - Long value of the ConsumerUID of the consumer object
     * CONSUMER - Serialized Consumer object
     * DURABLE_NAME - JMS durable name
     * CLIENT_ID - JMS client ID
     * CREATED_TS - Timestamp when the entry was created
     */
    public static final String TABLE = "MQCON";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String ID_COLUMN = "ID";
    public static final String CLIENT_ID_COLUMN = "CLIENT_ID";
    public static final String DURABLE_NAME_COLUMN = "DURABLE_NAME";
    public static final String CONSUMER_COLUMN = "CONSUMER";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";

    void insert( Connection conn, Consumer consumer, long createdTS ) throws BrokerException;

    void delete( Connection conn, Consumer consumer ) throws BrokerException;

    List getAllConsumers( Connection conn ) throws BrokerException;

    Consumer getConsumer( Connection conn, ConsumerUID consumerUID ) throws BrokerException;
}
