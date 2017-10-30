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
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import java.sql.*;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.comm.CommDBManager;
import com.sun.messaging.jmq.jmsserver.cluster.api.BrokerState;

/**
 */
class MySQLBrokerDAOImpl extends BrokerDAOImpl {

     protected static String PROC_IS_BEING_TAKENOVER = null;
     private final String dropStoredProcSQL;

    /**
     * Constructor
     * @throws com.sun.messaging.jmq.jmsserver.util.BrokerException
     */
    MySQLBrokerDAOImpl() throws BrokerException {
        super();

        PROC_IS_BEING_TAKENOVER = 
            "MQ"+JDBCStore.STORE_VERSION+"SP0BKR"+JDBCStore.STORED_PROC_VERSION+
             DBManager.getDBManager().getTableSuffix();

        dropStoredProcSQL = new StringBuffer(128)
            .append( "DROP PROCEDURE IF EXISTS "+PROC_IS_BEING_TAKENOVER)
            .toString();
    }

    @Override
    public void createStoredProc( Connection conn ) throws BrokerException {

        boolean myConn = false;
        Exception myex = null;
        String sql = "";
        Statement stmt = null;
        try {
            DBManager dbMgr = DBManager.getDBManager();
            if ( conn == null ) {
                conn = dbMgr.getConnection( true );
                myConn = true; // Set to true since this is our connection
            }

            sql = new StringBuffer(128)
            .append( "CREATE PROCEDURE " ).append( PROC_IS_BEING_TAKENOVER )
            .append( "( IN brokerID VARCHAR (100), OUT status INT, OUT state INT )" )
            .append( " BEGIN " )
            .append( " SET status=0; " )
            .append( "SELECT " ).append( STATE_COLUMN ).append( " INTO state ")
            .append( "FROM " ).append( tableName )
            .append( " WHERE " ).append( ID_COLUMN )
            .append( " = " ).append( "brokerID; " )
            .append( " IF state=" ).append( BrokerState.I_FAILOVER_PENDING )
            .append( " OR state=" ).append( BrokerState.I_FAILOVER_STARTED )
            .append( " OR state=" ).append( BrokerState.I_FAILOVER_COMPLETE )
            .append( " OR state=" ).append( BrokerState.I_FAILOVER_FAILED )
            .append( " THEN " )
            .append( " SET status=1; " )
            .append( " END IF; " )
            .append ( "END;" ).toString();

            stmt = conn.createStatement();
            try {
                dbMgr.executeUpdateStatement(stmt, sql);
            } catch (SQLException ee) {
                int ec = ee.getErrorCode();
                String et = ee.getSQLState();
                if (!(ec == 1304 && (et == null || et.equals("42000")))) {
                    throw ee;
                } else {
                    logger.log(Logger.INFO, 
                    br.getKString(br.I_STORED_PROC_EXISTS, PROC_IS_BEING_TAKENOVER));
                    return;
                }
            }

            Globals.getLogger().log(Logger.INFO, br.getKString(
                BrokerResources.I_CREATED_STORED_PROC,  PROC_IS_BEING_TAKENOVER));
            if (DEBUG) {
            Globals.getLogger().log(Logger.INFO,  sql);
            }

        } catch (Exception e) {
            myex = e;
            try {
                if ( (conn != null) && !conn.getAutoCommit() ) {
                    conn.rollback();
                }
            } catch ( SQLException rbe ) {
                logger.log( Logger.ERROR, BrokerResources.X_DB_ROLLBACK_FAILED, rbe );
            }

            Exception ex;
            if ( e instanceof BrokerException ) {
                throw (BrokerException)e;
            } else if ( e instanceof SQLException ) {
                ex = CommDBManager.wrapSQLException("[" + sql + "]", (SQLException)e);
            } else {
                ex = e;
            }

            throw new BrokerException("Failed to execute "+sql, ex);
        } finally {
            if ( myConn ) {
                closeSQLObjects( null, stmt, conn, myex );
            } else {
                closeSQLObjects( null, stmt, null, myex );
            }
        }
    }

    @Override
    public void dropStoredProc( Connection conn ) throws BrokerException {

        boolean myConn = false;
        Exception myex = null;
        String sql = dropStoredProcSQL;
        Statement stmt = null;
        try {
            DBManager dbMgr = DBManager.getDBManager();
            if ( conn == null ) {
                conn = dbMgr.getConnection( true );
                myConn = true; // Set to true since this is our connection
            }
            stmt = conn.createStatement();
            dbMgr.executeStatement(stmt, sql);
            if (DEBUG) {
            Globals.getLogger().log(Logger.INFO,  "DONE "+sql);
            }

        } catch (Exception e) {
            myex = e; 
            try {
                if ( (conn != null) && !conn.getAutoCommit() ) {
                    conn.rollback();
                }
            } catch ( SQLException rbe ) {
                logger.log( Logger.ERROR, BrokerResources.X_DB_ROLLBACK_FAILED, rbe );
            }

            Exception ex;
            if ( e instanceof BrokerException ) {
                throw (BrokerException)e;
            } else if ( e instanceof SQLException ) {
                ex = CommDBManager.wrapSQLException("[" + sql + "]", (SQLException)e);
            } else {
                ex = e;
            }

            throw new BrokerException("Failed to execute "+sql, ex);
        } finally {
            if ( myConn ) {
                closeSQLObjects( null, stmt, conn, myex );
            } else {
                closeSQLObjects( null, stmt, null, myex );
            }
        }
    }

}
