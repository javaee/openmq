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
 * @(#)HADBConsumerStateDAOImpl.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.log.Logger;

import java.sql.*;

/**
 * This class implements ConsumerDAO interface for HADB.
 */
class HADBConsumerStateDAOImpl extends ConsumerStateDAOImpl {

    /**
     * Constructor
     * @throws com.sun.messaging.jmq.jmsserver.util.BrokerException
     */
    HADBConsumerStateDAOImpl() throws BrokerException {

        super();
    }

    /**
     * Delete all entries.
     */
    protected void deleteAll( Connection conn, String whereClause,
        String timestampColumn, int chunkSize ) throws BrokerException {

        super.deleteAll( conn, whereClause, CREATED_TS_COLUMN, HADB_CHUNK_SIZE );
    }

    /**
     * Update existing entry.
     * @param conn database connection
     * @param dstUID the destination ID
     * @param sysMsgID the system message ID
     * @param conUID the consumer id
     * @param state the state
     * @throws BrokerException
     */
    public void updateState( Connection conn, DestinationUID dstUID,
        SysMessageID sysMsgID, ConsumerUID conUID, int state )
        throws BrokerException {

        String msgID = sysMsgID.getUniqueName();

        boolean myConn = false;
        PreparedStatement pstmt = null;
        Exception myex = null;
        try {
            // Get a connection
            DBManager dbMgr = DBManager.getDBManager();
            if ( conn == null ) {
                conn = dbMgr.getConnection( true );
                myConn = true;
            }

            if ( Globals.getHAEnabled() ) {
                BrokerDAO dao = dbMgr.getDAOFactory().getBrokerDAO();
                if ( dao.isBeingTakenOver( conn, dbMgr.getBrokerID() ) ) {
                    BrokerException be = new StoreBeingTakenOverException(
                        br.getKString( BrokerResources.E_STORE_BEING_TAKEN_OVER ) );
                    throw be;
                }
            }

            pstmt = dbMgr.createPreparedStatement( conn, updateStateSQL );
            pstmt.setInt( 1, state );
            pstmt.setString( 2, msgID );
            pstmt.setLong( 3, conUID.longValue() );

            if ( pstmt.executeUpdate() == 0 ) {
                // Otherwise we're assuming the entry does not exist
                throw new BrokerException(
                    br.getKString( BrokerResources.E_INTEREST_STATE_NOT_FOUND_IN_STORE,
                    conUID.toString(), msgID ), Status.NOT_FOUND );
            }
        } catch ( Exception e ) {
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
                ex = DBManager.wrapSQLException("[" + updateStateSQL + "]", (SQLException)e);
            } else {
                ex = e;
            }

            throw new BrokerException(
                br.getKString( BrokerResources.X_PERSIST_INTEREST_STATE_FAILED,
                conUID.toString(), sysMsgID.toString() ), ex );
        } finally {
            if ( myConn ) {
                Util.close( null, pstmt, conn, myex );
            } else {
                Util.close( null, pstmt, null, myex );
            }
        }
    }
}
