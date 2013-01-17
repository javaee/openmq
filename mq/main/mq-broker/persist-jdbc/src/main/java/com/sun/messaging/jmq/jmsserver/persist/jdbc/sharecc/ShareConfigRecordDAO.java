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
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc.sharecc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;

import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.BaseDAO;
import java.sql.Connection;
import java.util.List;

/**
 * This class is an interface for the Shared Configuration Change Record
 * table which will be implemented by database specific code.
 */
public interface ShareConfigRecordDAO extends BaseDAO {

    /**
     * Shared Configuration Change Record table:
     * Holds change records for 
     * . durable subscriptions
     * . administratively created destinations
     * used in a conventional cluster
     *
     * CREATE TABLE MQSHARECC<schemaVersion>C<clusterID> (
     *        SEQ BIGINT NOT NULL AUTO_INCREMENT,
     *        UID VARCHAR(100) NOT NULL,
     *        RECORD MEDIUMBLOB NOT NULL,
     *        TYPE INT NOT NULL,
     *        CREATED_TS BIGINT NOT NULL,
     *        FLAG INT
     * );
     *
     * ID - auto-increment, uniqued
     * UID - global unique identifier for this record
     * RECORD - configuration Record
     * CREATED_TS - local timestamp when the entry was created
     * FLAG - a reserved flag field (current only last 2 bits used - for durable sub share type)  
     */
    public static final String TABLE = "MQSHARECC";
    public static final String TABLE_NAME_PREFIX = TABLE +
                        JDBCShareConfigChangeStore.SCHEMA_VERSION;
    public static final String SEQ_COLUMN = "SEQ";
    public static final String UUID_COLUMN = "UUID";
    public static final String RECORD_COLUMN = "RECORD";
    public static final String TYPE_COLUMN = "TYPE";
    public static final String UKEY_COLUMN = "UKEY";
    public static final String CREATED_TS_COLUMN = "CREATED_TS";
    public static final String FLAG_COLUMN = "FLAG";
    public static final String LOCK_ID_COLUMN = "LOCK_ID";

    ChangeRecordInfo insert( Connection conn, ChangeRecordInfo rec)
                 throws BrokerException;

    void insertResetRecord( Connection conn, ChangeRecordInfo rec, String lockID)
                            throws BrokerException;

    List<ChangeRecordInfo> getRecords( Connection conn, Long seq, String resetUUID, boolean canReset )
    throws BrokerException;

    List<ChangeRecordInfo> getAllRecords( Connection conn, String sql ) 
    throws BrokerException;

    String getLockID( Connection conn )
    throws BrokerException;

    void updateLockID( Connection conn, String newLockID, String oldLockID )
    throws BrokerException;

    void insertAll( List<ChangeRecordInfo> recs, String oldTableName ) 
    throws BrokerException;

}
