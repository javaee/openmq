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
 * @(#)HADBMessageDAOImpl.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.io.Status;

import java.util.*;
import java.sql.*;
import java.io.IOException;

/**
 * This class implements MessageDAO interface for HADB.
 */
class HADBMessageDAOImpl extends MessageDAOImpl {

    /**
     * Constructor
     * @throws com.sun.messaging.jmq.jmsserver.util.BrokerException
     */
    HADBMessageDAOImpl() throws BrokerException {

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
     * Get all message IDs for a broker.
     * Work-around for "HADB-E-12462: Only a single table may be refered when
     * fetching LOB columns".
     * @param conn database connection
     * @param brokerID the broker ID
     * @return a List of all messages the specified broker owns
     * @throws BrokerException
     */
    public List getMessagesByBroker( Connection conn, String brokerID )
        throws BrokerException {

        List list = Collections.EMPTY_LIST;

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

            if ( brokerID == null ) {
                brokerID = dbMgr.getBrokerID();
            }

            List<Long> sessions = dbMgr.getDAOFactory().getStoreSessionDAO()
                .getStoreSessionsByBroker( conn, brokerID );

            if ( !sessions.isEmpty() ) {
                // Retrieve all messages for each session of the target broker
                pstmt = dbMgr.createPreparedStatement( conn, selectMsgsBySessionSQL );

                Iterator<Long> itr = sessions.iterator();
                while ( itr.hasNext() ) {
                    long sessionID = itr.next().longValue();
                    pstmt.setLong( 1, sessionID );
                    ResultSet rs = pstmt.executeQuery();
                    if ( list.isEmpty() ) {
                        list = (List)loadData( rs, false );
                    } else {
                        list.addAll( (List)loadData( rs, false ) );
                    }
                    rs.close();
                }
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
            } else if ( e instanceof IOException ) {
                ex = DBManager.wrapIOException("[" + selectMsgsBySessionSQL + "]", (IOException)e);
            } else if ( e instanceof SQLException ) {
                ex = DBManager.wrapSQLException("[" + selectMsgsBySessionSQL + "]", (SQLException)e);
            } else {
                ex = e;
            }

            throw new BrokerException(
                br.getKString( BrokerResources.E_LOAD_MSG_FOR_BROKER_FAILED,
                    brokerID ), ex );
        } finally {
            if ( myConn ) {
                Util.close( null, pstmt, conn, myex );
            } else {
                Util.close( null, pstmt, null, myex );
            }
        }

        return list;
    }

    /**
     * Check if a msg can be inserted. A BrokerException is thrown if
     * the specified broker is being taken over by another broker (HA mode).
     * @param conn database connection
     * @param msgID message ID
     * @param dstID destination ID
     * @param brokerID broker ID
     * @throws BrokerException if msg cannot be inserted
     */
    @Override
    protected void canInsertMsg( Connection conn, String msgID, String dstID,
        String brokerID ) throws BrokerException {

        if ( Globals.getHAEnabled() ) {
            DBManager dbMgr = DBManager.getDBManager();
            BrokerDAO dao = dbMgr.getDAOFactory().getBrokerDAO();
            if ( dao.isBeingTakenOver( conn, brokerID ) ) {
                try {
                    if ( (conn != null) && !conn.getAutoCommit() ) {
                        conn.rollback();
                    }
                } catch ( SQLException rbe ) {
                    logger.log( Logger.ERROR, BrokerResources.X_DB_ROLLBACK_FAILED+"[canInsertMsg():"+msgID+","+dstID, rbe );
                }
 
                BrokerException be = new StoreBeingTakenOverException(
                    br.getKString( BrokerResources.E_STORE_BEING_TAKEN_OVER ) );
                throw be;
            }
        }
    }
}
