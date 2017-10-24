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

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.io.Packet;

import java.util.*;
import java.io.IOException;
import java.sql.*;

public class MessageEnumeration implements Enumeration {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Connection conn = null;
        String sql = null; 
        MessageDAOImpl dao = null;
        Packet nextPacket = null;
        Store store = null;
        boolean closed = false;

        MessageEnumeration( ResultSet rs, PreparedStatement pstmt,
                            Connection conn, String sql, 
                            MessageDAOImpl dao, Store store ) {
            this.rs = rs;
            this.pstmt = pstmt;
            this.conn = conn;
            this.sql = sql;
            this.dao = dao;
            this.store = store;
        }

        public boolean hasMoreElements() {
            try {
                nextPacket = (Packet)dao.loadData( rs, true );
                if (nextPacket == null) {
                    if (setClosed()) {
                        try {
                            Util.close(rs, pstmt, conn, null); 
                        } catch (Throwable t) {
                            dao.logger.log(Logger.WARNING, dao.br.getKString(
                            BrokerResources.W_EXCEPTION_CLOSE_MSG_ENUM_RESOURCE, t.toString()));
                        }
                    }
                }
            } catch (Throwable e) {
                Throwable myex = e;
                nextPacket = null;
                if ( e instanceof IOException ) {
                    myex = DBManager.wrapIOException("[" + sql + "]", (IOException)e);
                } else if ( e instanceof SQLException ) {
                    myex = DBManager.wrapSQLException("[" + sql + "]", (SQLException)e);
                } 
                dao.logger.logStack(Logger.ERROR, myex.getMessage(), e);
                if (setClosed()) {
                    try {
                        Util.close(rs, pstmt, conn, myex);
                    } catch (Exception ee) {
                        dao.logger.log(Logger.WARNING, dao.br.getKString(
                        BrokerResources.W_EXCEPTION_CLOSE_MSG_ENUM_RESOURCE, ee.toString()));
                    }
                }
            }
            return (nextPacket != null);
        }

        public Object nextElement() {
            if (nextPacket == null) {
                throw new NoSuchElementException();
            }
            if (store.isClosed()) {
                throw new NoSuchElementException(
                dao.br.getKString(BrokerResources.I_STORE_CLOSING));
            }
            return nextPacket;
        }

        public void cancel() {
            
            if (!closed) {
                try {
                     pstmt.cancel();
                } catch (Throwable t) {
                     dao.logger.log(Logger.WARNING, dao.br.getKString(
                     BrokerResources.W_EXCEPTION_CANCEL_MSG_ENUM, 
                     "["+sql+"]", t.toString()));
                }
            }
        }

        private synchronized boolean setClosed() {
            if (!closed) {
                closed = true;
                return true;
            }
            return false;
        }

        public void close() {
            if (setClosed()) {
                try {
                    Util.close(rs, pstmt, conn, null);
                } catch (Exception e) {
                    dao.logger.log(Logger.WARNING, dao.br.getKString(
                    BrokerResources.W_EXCEPTION_CLOSE_MSG_ENUM_RESOURCE, e.toString()));
                }
            }
        }
}
