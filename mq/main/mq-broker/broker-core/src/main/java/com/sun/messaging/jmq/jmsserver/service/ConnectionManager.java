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
 * @(#)ConnectionManager.java	1.60 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.lists.WeakValueHashMap;
import com.sun.messaging.jmq.util.GoodbyeReason;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.management.agent.Agent;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.common.handlers.InfoRequestHandler;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.Status;

import com.sun.messaging.jmq.util.timer.MQTimer;

/**
 * This class maintains the list of all connections
 * currently active in the system.
 * It allows ConnectionUID's to be mapped to Connections
 * and may be used to check connection licensing.
 */

public class ConnectionManager extends WeakValueHashMap
{
    private static boolean DEBUG = false;
    private Logger logger = Globals.getLogger();

    long lastConCheck = 0;


    private int limit = 0;

    public static int pingTimeout = Globals.getConfig().getIntProperty(
                           Globals.IMQ + ".ping.interval", 120) * 1000;

    public boolean PING_ENABLED = Globals.getConfig().getBooleanProperty(
                           Globals.IMQ + ".ping.enabled", true);

    public static final long DEFAULT_RECONNECT_INTERVAL = 10000; //millisecs

    private ConnectionWatcher connectionWatcher = null;

    // lock and counters to allow:
    //     routing to access connection manager at same time
    //     prevent adding/removing on connections @ same time
    //     (although multiple add or destroy operations can happen at once)
    // The adding/removing of multiple connections should not happen
    // because there is "state" information on one connection (e.g. the
    //     acked messages) which should be handled before the messages
    //     are forwarded to a new connection

    private int destroyCount =0; 
    private int addCount =0; 
    public Object addLock = new Object();

    private ConsumerInfoNotifyManager cinmgr = null;

    public ConnectionManager(int limit) {
        super("ConnectionManager");
        this.limit = limit;
        cinmgr = new ConsumerInfoNotifyManager(this);
    }

    public Connection matchProperty(String name, Object value)
    {
        List al = getConnectionList(null);
        Iterator itr = al.iterator();
        while (itr.hasNext()) {
            Connection c = (Connection)itr.next();
            Object v = c.getClientData(name);
            if (value == v || 
                (value != null && v != null &&  value.equals(v)))
            {
                return c;
            }
        }
        return null;
    }

    public void removeFromClientDataList(String name, Object value)
    {
        List al = getConnectionList(null);
        Iterator itr = al.iterator();
        while (itr.hasNext()) {
            Connection c = (Connection)itr.next();
            Object v = c.getClientData(name);
            if (v != null && value != null && (v instanceof List)) {
                ((List)v).remove(value);
            }
        }
    }


    private void startTimer() {
        if (PING_ENABLED && connectionWatcher == null) {
            lastConCheck = System.currentTimeMillis();
            MQTimer timer = Globals.getTimer(true);
            connectionWatcher = new ConnectionWatcher();
            try {
                timer.schedule(connectionWatcher, pingTimeout, pingTimeout);
            } catch (IllegalStateException ex) {
                logger.log(Logger.DEBUG,"Timer shutting down", ex);
            }
        }
    }

    private void stopTimer() {
        if (connectionWatcher != null) {
            connectionWatcher.cancel();
            connectionWatcher = null;
        }

    }  


    public void updateConnectionUID(ConnectionUID currentID,
               ConnectionUID oldid)
    {
	synchronized(addLock) {
            if (destroyCount > 0) {
                try {
                    addLock.wait();
                } catch (InterruptedException ex){
                }
             }
             addCount ++;
        }
        try {
            synchronized(this) {
                Object o = this.remove(oldid);
                if (o != null)
                this.put(currentID, o);
            }
        } finally {
             synchronized(addLock) {
                 addCount --;
                 addLock.notifyAll();
             }
        }
    }

    /**
     * add a new connection 
     *
     * @param con connection to add to the list
     * @throws BrokerException if the connection can not
     *       be added (e.g. licensing has expired)
     */
    public  void addConnection(Connection con)
        throws BrokerException
    {
	synchronized(addLock) {
            if (destroyCount > 0) {
                try {
                    addLock.wait();
                } catch (InterruptedException ex){
                }
             }
             addCount ++;
        }
        try {
            if (this.size() > limit) { // throw exception
                  throw new BrokerException(
                       Globals.getBrokerResources().getKString(
                          BrokerResources.X_CONNECTION_LIMIT_EXCEEDED, 
                          con.toString(),
                          String.valueOf(limit)), 
                       BrokerResources.X_CONNECTION_LIMIT_EXCEEDED,
                       (Throwable)null, Status.ERROR);
            }
            synchronized(this) {
                this.put(con.getConnectionUID(), con);
                if (size() == 1 && PING_ENABLED) { // first  connection
                    startTimer();
                }
            }

        } finally {
             synchronized(addLock) {
                 addCount --;
                 addLock.notifyAll();
             }
        }

        if (DEBUG) {
            logger.log(Logger.DEBUG, BrokerResources.I_NEW_CONNECTION,
            con.toString(), String.valueOf(size()));
            logCM(Logger.DEBUGHIGH);
        }
    }


    /**
     * retrive a connection object from a connection ID
     * 
     * @param con connection to remove
     */
    public synchronized Connection getConnection(ConnectionUID id) {
        return (Connection)this.get(id);
    }

    /**
     * code to handle removing ALL reverences to a connection
     * when it goes away or is closed
     */
    public void removeConnection(ConnectionUID id, boolean say_goodbye,  
            int reason, String reasonStr)
    {
        if (!containsKey(id)) return; // already gone
	/*
	 * Unregister/Destroy connection MBeans in the JMX agent in the broker.
	 */
	Agent agent = Globals.getAgent();
	if (agent != null)  {
	    agent.notifyConnectionClose(id.longValue());
	    agent.unregisterConnection(id.longValue());
	}

	synchronized(addLock) {
            if (addCount > 0) {
                try {
                    addLock.wait();
                } catch (InterruptedException ex){
                }
             }
             destroyCount ++;
        }
        try  {
            // first destroy the connection
            Connection con = null;
            synchronized(this) {
                con= (Connection)this.remove(id);
                if (size() == 0 && PING_ENABLED) { // first  connection
                    stopTimer();
                }
            }
            if (con != null) {
                destroyConnectionData(con, say_goodbye, reason, reasonStr);
            }
        } finally {
             synchronized(addLock) {
                 destroyCount --;
                 addLock.notifyAll();
             }
        }
    }

    /**
     * method to clean up all stuff associated with a
     * connection
     */

     private void destroyConnectionData(Connection con, boolean say_goodbye,
                 int reason, String reasonStr)
     {
        if (con == null) return;

        
        if (DEBUG) {
            logger.log(Logger.DEBUG, BrokerResources.I_REMOVE_CONNECTION,
            con.toString(),String.valueOf(size()));
        }


        if (reason == GoodbyeReason.SHUTDOWN_BKR ) {
            con.shutdownConnection(reasonStr);
        } else {
            con.destroyConnection(say_goodbye, reason, reasonStr);
        }


        if (DEBUG)
            logCM(Logger.DEBUGHIGH);
     }


    /**
     * list out all current connections
     */
    private void logCM(int loglevel)
    {
        logger.log(loglevel, "ConnectionManager: " + size());
        Set keys = entrySet();
        Iterator itr = keys.iterator();
        int i = 0;
        while (itr.hasNext()) {
            logger.log(loglevel, "{0}:{1}", String.valueOf(i), itr.next().toString());
            i ++;
        }
    }


    /**
     * remove all connections associated with a Service
     *
     * should only be called when the svc stoped to accept new connections  
     */
    /* FOO
    public  void shutdownAllConnections(Service svc, String reason)
    {
        if (DEBUG)
            logger.log(Logger.DEBUGHIGH, "Removing all connections for  {0} ", svc.toString());

        List cons = getConnectionList(svc);
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
             con = (Connection)cons.get(i);
             destroyConnection(con.getConnectionUID(), true, GoodbyeReason.SHUTDOWN_BKR, reason);
        }

    } */

    public Vector getDebugState(Service svc) {
        List cons = getConnectionList(svc);
        Vector v = new Vector();
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
             con = (Connection)cons.get(i);
             v.add(String.valueOf(con.getConnectionUID().longValue()));
        }
        return v;
    }

    /**
     * Return number of connections for a particular service.
     *
     * @param svc   Service to count connections for. Null to return
     *              total number of connections.
     *
     * @return      Number of open connections for the Service (or
     *              total number of open connections if null svc).
     */
    public synchronized int getNumConnections(Service svc)
    {
        int total = 0;

        Iterator itr = values().iterator();
        while (itr.hasNext()) {
            Connection con = (Connection)itr.next();
            if (svc == null || con.getService() == svc) {
                total++;
            }
        }

        return total;
    }

    /**
     * Returns the list of connections for a particular service.
     *
     * @param svc   Service to count connections for. Null to return
     *              total number of connections.
     *
     * @return      List of open connections for the Service (or
     *              total List of open connections if null svc).
     */
    public synchronized List getConnectionList(Service svc)
    {
        List list = new ArrayList();

        Iterator itr = values().iterator();
        while (itr.hasNext()) {
            Connection con = (Connection)itr.next();
            if (svc == null || con.getService() == svc) {
                list.add(con);
            }
        }

        return list;
    }

    public void debug() {
        List l = getConnectionList(null);
        logger.log(Logger.INFO,"Connection count " + l.size());
        Iterator itr = l.iterator();
        while (itr.hasNext()) {
            Connection c = (Connection)itr.next();
            logger.log(Logger.INFO,"Connection " + c);
            c.debug("\t");
        }
    }

    /**
     * broadcast a message to all connections with flush no-wait
     *
     * should only be called when all services stoped accept new connections
     * @param reason a reason from GoodbyeReason
     * @param msg why the goodbye is being broadcast (debugging)
     */
    public void broadcastGoodbye(int reason, String msg) {
        broadcastGoodbye(reason, msg, null);
    }

    public void broadcastGoodbye(int reason, String msg,
                                 Connection excludedConn) {
        List cons = getConnectionList(null);
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
           con = (Connection)cons.get(i);
           if (excludedConn != null && con == excludedConn) {
               continue;
           } 
           con.sayGoodbye(reason, msg);
        }
    }

    /**
     * flush all control messages
     *
     * should only be called when all services stoped accept new connections
     */
    public void flushControlMessages(long time) {
        List cons = getConnectionList(null);
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
           con = (Connection)cons.get(i);
           con.flushConnection(time);
        }
    }


    /**
     * flush all control messages
     *
     * should only be called when all services stoped accept new connections
     */

    public void checkAllConnections() {
        List cons = getConnectionList(null);
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
            con = (Connection)cons.get(i);
            long access = con.getAccessTime();
            if (lastConCheck != 0 && access != 0 &&
                access < lastConCheck) {
                con.checkConnection();
            }
        }
        lastConCheck = System.currentTimeMillis();
    }

    public long getMaxReconnectInterval() {
        List cons = getConnectionList(null);
        Connection con = null;
        long max = DEFAULT_RECONNECT_INTERVAL, interval = 0L;
        for (int i = cons.size()-1; i >= 0; i--) {
            con = (Connection)cons.get(i);
            interval = con.getReconnectInterval();
            if (interval > max) {
                max = interval;
            }
        }
        return  max;
    }

    public ConsumerInfoNotifyManager getConsumerInfoNotifyManager() {
        return cinmgr;
    }

    protected void sendConsumerInfo(int requestType, DestinationUID duid, 
                                    int destType, int infoType, boolean sendToWildcard) {
        List cons = getConnectionList(null);
        Connection con;
        for (int i = cons.size()-1; i >= 0; i--) {
            con = (Connection)cons.get(i);
            con.sendConsumerInfo(requestType, duid, destType, infoType, sendToWildcard);
        }
    }

    class ConnectionWatcher extends TimerTask {

        public void run() {
            checkAllConnections(); 
        }
    }    


    public void cleanupMemory(boolean persistent) {
        // make sure we quit adding connections unti the
        // connections have been cleaned up
        if (persistent)
            logger.log(Logger.DEBUG,"Swapping all unacknowldged messages from memory (messages will remain persisted )");
        else 
            logger.log(Logger.DEBUG,"Swapping all unacknowldged messages from memory (messages will be swapped to disk)");

    }

}
