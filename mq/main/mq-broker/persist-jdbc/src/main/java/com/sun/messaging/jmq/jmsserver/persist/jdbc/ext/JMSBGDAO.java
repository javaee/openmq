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

package com.sun.messaging.jmq.jmsserver.persist.jdbc.ext;

import java.util.List;
import java.sql.Connection;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.DBConstants;
import com.sun.messaging.bridge.api.DupKeyException;
import com.sun.messaging.bridge.api.KeyNotFoundException;

/**
 * This class is an interface for JMS Bridges table
 *
 * @author amyk
 */
public interface JMSBGDAO extends BaseDAO {

    /**
     * TMLogRecord table:
     * Holds all the txlog records
     *
     * CREATE TABLE MQJMSBG<schemaVersion>[C<clusterID>|S<brokerID>] (
     *     NAME VARCHAR(100) NOT NULL,\
     *     BROKER_ID VARCHAR(100) NOT NULL,\
     *     CREATED_TS DOUBLE INTEGER NOT NULL,\
     *     UPDATED_TS DOUBLE INTEGER NOT NULL,\
     *     PRIMARY KEY(NAME))
     *
     * NAME - jmsbridge name
     * BROKER_ID - The Broker ID who owns the jmsbridge
     * CREATED_TS_COLUMN - Timestamp when the entry is created 
     * UPDATED_TS_COLUMN - Timestamp when the entry was last updated 
     */
    public static final String TABLE = "MQJMSBG";
    public static final String TABLE_NAME_PREFIX = TABLE + DBConstants.SCHEMA_VERSION;
    public static final String NAME_COLUMN = "NAME";
    public static final String BROKER_ID_COLUMN = "BROKER_ID";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";
    public static final String UPDATED_TS_COLUMN = "UPDATED_TS";

    /**
     * @param conn database connection
     * @param name jmsbridge name 
     * @param logger_ can be null;
     * @throws Exception
     */
    public void insert(Connection conn,
                       String name,
                       java.util.logging.Logger logger_)
                       throws Exception;

    /**
     * @param conn database connection
     * @param name to identify the TM
     * @param newBrokerId
     * @param expectedBrokerId
     * @param logger_ can be null;
     * @throws Exception
     */
    public void updateBrokerId(Connection conn,
                               String name,
                               String newBrokerId,
                               String expectedBrokerId,
                               java.util.logging.Logger logger_)
                               throws Exception;

    /**
     * @param conn database connection
     * @param name jmsbridge name 
     * @param logger_ can be null;
     * @throws Exception
     */
    public void delete(Connection conn,
                       String name,
                       java.util.logging.Logger logger_)
                       throws Exception;

    /**
     * @param conn database connection
     * @param name jmsbridge name
     * @param logger_ can be null;
     * @return brokerId 
     * @throws Exception
     */
    public String getBrokerId(Connection conn, 
                              String name,
                              java.util.logging.Logger logger_)
                              throws Exception;

    /**
     * @param conn database connection
     * @param name jmsbridge name 
     * @param logger_ can be null;
     * @return updated time
     * @throws Exception
     */
    public long getUpdatedTime(Connection conn, String name,
                               java.util.logging.Logger logger_)
                               throws Exception;

    /**
     * @param conn database connection
     * @param name jmsbridge name 
     * @param logger_ can be null;
     * @return created time
     * @throws Exception
     */
    public long getCreatedTime(Connection conn, String name,
                               java.util.logging.Logger logger_)
                               throws Exception;

    /**
     * Get JMS bridge names owned by a broker
     *
     * @param conn database connection
     * @param brokerID
     * @param logger_ can be null;
     * @return list of names owned by the brokerId
     * @throws Exception
     */
    public List getNamesByBroker(Connection conn, 
                                 String brokerID,
                                 java.util.logging.Logger logger_)
                                 throws Exception;


}
