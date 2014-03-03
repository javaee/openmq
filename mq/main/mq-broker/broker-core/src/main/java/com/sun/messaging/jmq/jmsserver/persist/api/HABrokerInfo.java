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
 * @(#)HABrokerInfo.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.api;

import com.sun.messaging.jmq.jmsserver.cluster.api.BrokerState;

import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.io.*;

/**
 * This immutable object encapsulates general information about
 * a broker in an HA cluster.
 */
public final class HABrokerInfo implements Externalizable {

    static final long serialVersionUID = -6833553314062089908L;

    // broker info update types
    public static final int UPDATE_VERSION = 0;
    public static final int UPDATE_URL = 1;
    public static final int RESET_TAKEOVER_BROKER_READY_OPERATING = 2;
    public static final int RESTORE_HEARTBEAT_ON_TAKEOVER_FAIL = 3;
    public static final int RESTORE_ON_TAKEOVER_FAIL = 4;

    private String id;
    private String takeoverBrokerID;
    private String url;
    private int version;
    private int state;
    private long sessionID;     // Current session ID
    private long heartbeat;
    private List sessionList;   // All sessions IDs own by this broker

    private long takeoverTimestamp = 0;

    /**
     * Constructor for Externalizable interface
     */
    public HABrokerInfo() {
    }

    /**
     * Constructor
     * @param id Broker ID
     * @param takeoverBrokerID Broker ID taken over the store
     * @param url the broker's URL
     * @param version the broker's version
     * @param state the broker's state
     * @param sessionID the broker's session ID
     * @param heartbeat broker's last heartbeat
     */
    public HABrokerInfo( String id, String takeoverBrokerID, String url, int version,
        int state, long sessionID, long heartbeat ) {

        this.id = id;
        this.takeoverBrokerID = ( takeoverBrokerID == null ) ? "" : takeoverBrokerID;
        this.url = url;
        this.version = version;
        this.state = state;
        this.sessionID = sessionID;
        this.heartbeat = heartbeat;

        this.sessionList = Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getTakeoverBrokerID() {
        return takeoverBrokerID;
    }

    public String getUrl() {
        return url;
    }

    public int getVersion() {
        return version;
    }

    public int getState() {
        return state;
    }

    public long getSessionID() {
        return sessionID;
    }

    public List getAllSessions() {
        return sessionList;
    }

    public long getHeartbeat() {
        return heartbeat;
    }

    public void setSessionID( long id ) {
        sessionID = id;
    }

    public void setSessionList( List list ) {
        sessionList = list;
    }

    public void setTakeoverTimestamp(long ts) {
        takeoverTimestamp = ts;
    }

    public long getTakeoverTimestamp() {
        return takeoverTimestamp;
    }

    public String toString() {

        StringBuffer strBuf = new StringBuffer( 128 )
            .append( "(")
            .append( "brokerID=" ).append( id )
            .append( ", URL=" ).append( url )
            .append( ", version=" ).append( version )
            .append( ", state=" ).append( state ).append( " [" )
            .append( BrokerState.getState( state ).toString() ).append( "]" )
            .append( ", sessionID=" ).append( sessionID )
            .append( ", heartbeatTS=").append( heartbeat )
            .append( (heartbeat > 0) ? " [" + new Date( heartbeat ) + "]" : "" )
            .append( ", takeoverBrokerID=" ).append( takeoverBrokerID )
            .append( ")");

        return strBuf.toString();
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {

        id = (String)in.readObject();
        takeoverBrokerID = (String)in.readObject();
        url = (String)in.readObject();
        version = in.readInt();
        state = in.readInt();
        sessionID = in.readLong();
        heartbeat = in.readLong();
        sessionList = (List)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(id);
        out.writeObject(takeoverBrokerID);
        out.writeObject(url);
        out.writeInt(version);
        out.writeInt(state);
        out.writeLong(sessionID);
        out.writeLong(heartbeat);
        out.writeObject(sessionList);
    }

    public static class StoreSession implements Externalizable {

        static final long serialVersionUID = -1619140799512705251L;

        private long id;
        private String brokerID;
        private int isCurrent;
        private String createdBy;
        private long createdTS;

        /**
         * Constructor for Externalizable interface
         */
        public StoreSession() {
        }

        public StoreSession( long id, String brokerID, int isCurrent,
            String createdBy, long createdTS ) {

            this.id = id;
            this.brokerID = brokerID;
            this.isCurrent = isCurrent;
            this.createdBy = createdBy;
            this.createdTS = createdTS;
        }

        public long getID() {
            return id;
        }

        public String getBrokerID() {
            return brokerID;
        }

        public int getIsCurrent() {
            return isCurrent;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public long getCreatedTS() {
            return createdTS;
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {

            id = in.readLong();
            brokerID = (String)in.readObject();
            isCurrent = in.readInt();
            createdBy = (String)in.readObject();
            createdTS = in.readLong();
        }

        public void writeExternal(ObjectOutput out) throws IOException {

            out.writeLong(id);
            out.writeObject(brokerID);
            out.writeInt(isCurrent);
            out.writeObject(createdBy);
            out.writeLong(createdTS);
        }
    }
}

