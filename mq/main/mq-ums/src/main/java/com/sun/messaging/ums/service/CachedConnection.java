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

package com.sun.messaging.ums.service;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.jms.Connection;
import javax.jms.JMSException;

import com.sun.messaging.ums.common.Constants;
import java.util.logging.Logger;

public class CachedConnection {

    private Connection conn = null;
    private ArrayList<Client> clients = new ArrayList<Client>();
    private Properties props = null;
    private static final String MAX_CLIENTS = "100";
    private int maxClients = Integer.parseInt(MAX_CLIENTS);
    private boolean isClosed = false;
    private Semaphore available = null;
    private Logger logger = UMSServiceImpl.logger;
    private long timestamp = 0;

    public CachedConnection(Connection conn, Properties props) {
        this.conn = conn;
        this.props = props;

        init();
    }

    private void init() {

        try {

            String tmp = props.getProperty(Constants.MAX_CLIENT_PER_CONNECTION, MAX_CLIENTS);
            maxClients = Integer.parseInt(tmp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        available = new Semaphore(maxClients, true);

    }

    public Connection getConnection() {
        checkClosed();
        return conn;
    }

    public synchronized void add(Client client) {
        checkClosed();
        clients.add(client);
    }

    public synchronized void remove(Client client) {
        clients.remove(client);
    }

    public synchronized int size() {
        return clients.size();
    }

    /**
     * Called by CachedConnectionPool.
     * @return
     */
    protected boolean reachedMaxCapacity() {
        return (available.availablePermits() == 0);
    }

    /**
     * Called by CachedConnectionPool.
     */
    protected void acquire() {
        
        if (UMSServiceImpl.debug) {
            logger.info ("acquiring semaphore permit, available#: " + available.availablePermits());
        }
        
        available.acquireUninterruptibly();
        this.setTimestamp();
    }

    /**
     * Called by CachedConnectionPool.
     */
    protected void release() {

        available.release();
        this.setTimestamp();
        
        if (UMSServiceImpl.debug) {
            logger.info ("released semaphore, available#: " + available.availablePermits());
        }
    }

    protected boolean inUse() {
        
        boolean isInuse = (available.availablePermits() < this.maxClients);
        
        if (UMSServiceImpl.debug) {
            logger.info ("is inuse = " + isInuse + ", available permits=" + available.availablePermits() + " , max capacity=" + maxClients);
        }
        
        return isInuse;
    }

    /**
     * This should be called from the pool.
     * 
     * @throws JMSException
     */
    protected synchronized void close() throws JMSException {

        if (this.available.availablePermits() != this.maxClients) {

            throw new RuntimeException(
                    "Attemp to close a connection with Clients associate with it.");
        }

        this.isClosed = true;

        //XXX remove from pool

        conn.close();
    }

    private synchronized void checkClosed() {

        if (isClosed) {
            throw new UMSServiceException("Cached connection is closed.");
        }
    }

    private synchronized void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public synchronized long getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        return "CachedConnection, conn=" + this.conn + ", available permits=" + this.available.availablePermits() + ", max capacity=" + this.maxClients;
    }
}
