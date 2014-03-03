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
 * @(#)Connection.java	1.66 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service;

import java.util.*;
import com.sun.messaging.jmq.jmsserver.service.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

import java.security.Principal;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.auth.JMQAccessControlContext;
import com.sun.messaging.jmq.auth.api.server.AccessControlContext;

import com.sun.messaging.jmq.io.*;

import com.sun.messaging.jmq.util.net.IPAddress;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.plugin.spi.CoreLifecycleSpi;


/**
 * This interface class is responsible for the basic methods needed to
 * send messages back to a client
 *
 * Each service will implement their own version of this 
 * Connection interface..
 */

public abstract class Connection
{

// XXX - lks - move to init method ?
    private static int clockSkewTime = Globals.getConfig().getIntProperty(
                           Globals.IMQ + ".clock.skew.interval", 300);

    private static int clockSkewCheck = Globals.getConfig().getIntProperty(
                           Globals.IMQ + ".clock.skew.checkCnt", 0);


    private int skewCheckCounter = 0;

    private long lastAccess = 0;
    private long lastResponse = 0;

    protected final Logger logger = Globals.getLogger();

    // VERSION STRINGS for PROTOCOL
    public static final int UNKNOWN_PROTOCOL = -1;
    public static final int SWIFT_PROTOCOL = 200;
    public static final int HUMMINGBIRD_PROTOCOL = 201;
    public static final int FALCON_PROTOCOL = 300;
    public static final int RAPTOR_PROTOCOL = PacketType.VERSION350;
    public static final int SHRIKE_PROTOCOL = PacketType.VERSION360;
    public static final int SHRIKE4_PROTOCOL = PacketType.VERSION364;
    public static final int HAWK_PROTOCOL = PacketType.VERSION400;
    public static final int MQ450_PROTOCOL = PacketType.VERSION450;
    public static final int MQ500_PROTOCOL = PacketType.VERSION500;


    // CONNECITON STATE STRINGS
    public static final int STATE_UNAVAILABLE = -1;
    public static final int STATE_CONNECTED = 0;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_AUTH_REQUESTED = 2;
    public static final int STATE_AUTH_RESPONSED = 3;
    public static final int STATE_AUTHENTICATED = 4;
    public static final int STATE_CLEANED = 5;
    public static final int STATE_CLOSED = 6;
    public static final int STATE_DESTROYING = 7;
    public static final int STATE_DESTROYED = 8;

    /**
     * State of the connection
     */
    protected int state = Connection.STATE_UNAVAILABLE;

    /**
     * Service object
     */
    protected Service service = null;

    /**
	 * Authentication/Authorization controller object 
     */
    protected AccessController accessController = null;

    /**
     * Connection ID associated with this Connection
     */
    protected ConnectionUID conId = null;


    /**
     * for reconnectable connections, time to next reconnect
     */
    protected long reconnectInterval = 0;


    /**
     * What version of the protocol the client is speaking. This is
     * set when we process the HELLO message
     */
    protected int clientProtocolVersion = -1;

    /**
     * Additional data tagged onto the connection
     */
    protected Hashtable clientData = null;


    protected boolean isadmin = false;

    protected List consumerInfoRequests = Collections.synchronizedList(new ArrayList());
    protected List connCloseListeners = new ArrayList();

    protected CoreLifecycleSpi coreLifecycle = null;

    public Connection(Service svc) throws BrokerException
    {
        setService(svc);
        isadmin = svc.getServiceType() == ServiceType.ADMIN;
    } 


    public void setCoreLifecycle(CoreLifecycleSpi clc) {
        coreLifecycle = clc;
    }

    /** 
     * The debug state of this object
     */
    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("uid", String.valueOf(conId.longValue()));
        ht.put("service", service.toString());
        ht.put("state", getConnectionStateString(state));
        if (clientData != null)
            ht.put("clientData", clientData.toString());
        else
            ht.put("clientData", "none");
        ht.put("clientProtocol", String.valueOf(clientProtocolVersion));
        ht.put("reconnectInterval", String.valueOf(reconnectInterval));
        ht.put("lastAccess", String.valueOf(lastAccess));
        ht.put("lastResponse", String.valueOf(lastResponse));
        return ht;
    }

    public void setReconnectInterval(long val) 
    {
        reconnectInterval = val;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public boolean isAdminConnection() {
        return isadmin;
    } 

    public static int getHighestSupportedProtocol()
    {
        return MQ500_PROTOCOL;
    }

  
    public abstract void startConnection();
    public abstract void stopConnection();
    public abstract void cleanupConnection();
    public abstract void closeConnection(boolean force, int reason, String reasonStr);
    public abstract void shutdownConnection(String reasonStr);
    public void destroyConnection(boolean force, int reason, String reasonStr)
    {
        service.removeConnection(conId, reason, reasonStr);
    }

    public void notifyConnectionClosed() {
        synchronized(this) {
            Iterator itr = connCloseListeners.iterator();
            ConnectionClosedListener l = null;
            while (itr.hasNext()) {
                l = (ConnectionClosedListener)itr.next();
                l.connectionClosed(this);
            }
        }
    }
    public void addConnectionClosedListener(ConnectionClosedListener l) {
        synchronized(this) {
            connCloseListeners.add(l); 
        }
    }

    public void removeConnectionClosedListener(ConnectionClosedListener l) {
        synchronized(this) {
            connCloseListeners.remove(l); 
        }
    }

    public abstract void logConnectionInfo(boolean closing);
    public abstract String getRemoteConnectionString();


    /**
     * Gets the ConnectionUID for this connection.
     */
    public ConnectionUID getConnectionUID() {
        return this.conId;
    }

    public void setConnectionUID(ConnectionUID conId) {
        this.conId = conId;
    }

    /**
     * Sets the Service for this connection.
     */
    protected void setService(Service id) {
        this.service = id;
    }


    /**
     * Gets the Service for this connection.
     */
    public Service getService() {
        return this.service;
    }

    /**
     * Set what iMQ protocol version the client is using. This is 
     * determined when we get the HELLO message.
     */
    public void setClientProtocolVersion(int version) {
        clientProtocolVersion = version;
    }


    /**
     * Get what iMQ protocol version the client is using
     */
    public int getClientProtocolVersion() {
        return clientProtocolVersion;
    }

    /**
     * Gets the AccessController for this connection.  An AccessController
     * encapsulates a AuthenticationProtocolHandler and AccessControlContext.
     * The later is obtained as result of authentication.
     */
    public AccessController getAccessController() {
        return accessController;
    }

    /**
     * The term, principal, represents a name associated with the 
     * authenticated subject on this connection
     *
     * @exception BrokerException connection not authenticated
     */
    public Principal getAuthenticatedName() throws BrokerException {
	return accessController.getAuthenticatedName();
    }

    /**
     * retrieves the connection state 
     * @return false if connection being destroyed
     */
    public boolean setConnectionState(int state)
    {
        if (state >= STATE_DESTROYED) return true;
        this.state =  state;
        return false;
    }

    public static String getConnectionStateString(int state) {
        switch (state) {
            case Connection.STATE_UNAVAILABLE:
                return "UNAVAILABLE";

            case Connection.STATE_CONNECTED:
                return "CONNECTED";

            case Connection.STATE_INITIALIZED:
                return "INITIALIZED";

            case Connection.STATE_AUTH_REQUESTED:
                return "AUTHENTICATION REQUESTED";

            case Connection.STATE_AUTH_RESPONSED:
                return "AUTHENTICATION RESPONSED";

            case Connection.STATE_AUTHENTICATED:
                return "AUTHENTICATED";

            case Connection.STATE_CLOSED:
                return "CLOSED";

            case Connection.STATE_CLEANED:
                return "CLEANED";

            case Connection.STATE_DESTROYING:
                return "DESTROYING";
            case Connection.STATE_DESTROYED:
                return "DESTROYED";
        }
        return "UNKNOWN";
    }

    /**
     * sets the connection state 
     * @return false if connection being destroyed
     */
    public int getConnectionState() {
        return state;
    }

    /**
     * Place an object (by name) in the client data storage
     * section of the Connection object
     */
    public void addClientData(String name, Object data)
    {
        if (clientData == null)
            clientData = new Hashtable();
        clientData.put(name, data);
    }

    /**
     * remove client data object (by name)
     */
    public void removeClientData(String name) {
        if (clientData == null) return;
        clientData.remove(name);
    }

    /**
     * retrieve client data object (by name)
     */
    public Object getClientData(String name)
    {
        if (clientData == null) return null;
        return clientData.get(name);
    }


    // dont sync .. we dont care who won
    public void updateAccessTime(boolean received) {
        lastAccess = System.currentTimeMillis();
        if (received)
            lastResponse=lastAccess;
    }

    public long getAccessTime() {
        return lastAccess;
    }
    public long getLastResponseTime() {
        return lastResponse;
    }

    public void checkClockSkew(long receivetime, long sendtime,
         long expiretime, long deliverytime) {
        // clockSkewCheck:
        //     -1 never check
        //      0 check once
        //      > 1 check every X requests

        if (clockSkewCheck < 0) {
            // never check
            return;
        }
        // if 0, just check once
        if (clockSkewCheck == 0 && skewCheckCounter > 0) {
            return;
        }
        
        skewCheckCounter ++;

        if (clockSkewCheck > 0 && (
              skewCheckCounter%clockSkewCheck != 0)) {
             // not the right iteration
             return;
        }


        // OK, if we are here, we are checking for skew
        long skewSecsMS = Math.abs(sendtime - receivetime);
        long skewSecs = skewSecsMS/1000;

        // OK, we also want to log something if the clock skew is
        // less than our counter but we are immediately expiring the
        // message
        // So:
        //    - if we receive the message before we sent it
        //           (receivetime > sendtime)
        //      and the expiration time is small (< clockSkewTime)
        //      and the expiration is less than twice the
        //             skew time
        //  log something

        long expirationMS = (expiretime == 0L ? 
                             0L :
                             ((receivetime > sendtime) ?
                               expiretime - sendtime : 0)
                            );

        long expirationSecs = expirationMS/1000;

        if (skewSecs >  clockSkewTime) {
            String msg = BrokerResources.W_CLOCK_SKEW_EARLY;
            // we received the message before we sent
            if (sendtime < receivetime) {
                 msg = BrokerResources.W_CLOCK_SKEW_LATE;
            }
            logger.log(Logger.WARNING, msg,
                getRemoteConnectionString(), String.valueOf(clockSkewTime));
         } else if (expirationMS != 0L && expirationSecs < clockSkewTime &&
               expirationMS < 2*skewSecsMS) {
            logger.log(Logger.WARNING, BrokerResources.W_CLOCK_SKEW_EXPIRING,
                  getRemoteConnectionString());
         }
    }


    protected abstract void sayGoodbye(int reason, String reasonStr);
    protected abstract void flushConnection(long timeout);

    /**
     * verify if the connection still exists
     */
    protected abstract void checkConnection();

    public abstract void cleanupMemory(boolean persistent);


    public void debug(String prefix) {
    }

    public void sendConsumerInfo(int requestType, 
                                 DestinationUID duid,
                                 int destType, int infoType,
                                 boolean sendToWildcard) {
        DestinationUID uid = null;
        synchronized(consumerInfoRequests) {
            Iterator itr = consumerInfoRequests.iterator();
            while (itr.hasNext()) {
                uid = (DestinationUID)itr.next();
                if (!uid.isWildcard()) {
                    if (duid.equals(uid)) {
                        sendConsumerInfo(requestType, uid.getName(),
                                         destType, infoType);
                        break;
                    }
                    continue;
                }
                if (duid.isWildcard() && 
                    uid.getName().equals(duid.getName()) &&
                    duid.isQueue() == uid.isQueue()) {
                    sendConsumerInfo(requestType, uid.getName(), destType, infoType);
                    break;
                }
                if (DestinationUID.match(duid, uid)) {
                    if (sendToWildcard) {
                        sendConsumerInfo(requestType, uid.getName(), destType, infoType);
                    } else {
                        Globals.getConnectionManager().getConsumerInfoNotifyManager().
                                 consumerInfoRequested(this, uid, destType, infoType);
                    }
                } 
            }
        }
    }

    protected abstract void sendConsumerInfo(int requestType, String destName, 
                                             int destType, int infoType);

    public void addConsumerInfoRequest(DestinationUID duid) { 
        consumerInfoRequests.add(duid); 
    }

    public void removeConsumerInfoRequest(DestinationUID duid) { 
        consumerInfoRequests.remove(duid);
    }
}


