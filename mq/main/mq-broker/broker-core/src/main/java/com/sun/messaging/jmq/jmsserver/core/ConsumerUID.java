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
 * @(#)ConsumerUID.java	1.27 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.core;

import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;

import java.io.*;

public class ConsumerUID extends com.sun.messaging.jmq.util.UID
    implements Externalizable {

    static final long serialVersionUID = 471544583389431969L;

    protected transient int ackType=Session.NONE;

    protected transient ConnectionUID conuid = null;
    protected transient BrokerAddress brokeraddr = Globals.getMyAddress();

    protected transient boolean shouldStore = false;

    public ConsumerUID() {
        // Allocates a new id
        super();
    }

    public ConsumerUID(long id) {
        // Wraps an existing id
        super(id);
    }

    public ConsumerUID(boolean empty) {
         super(0);
         if (!empty)
             initializeID();
    }

    public boolean shouldStore() {
        return shouldStore;
    }

    public void setShouldStore(boolean store) 
    {
        shouldStore = store;
    }

    /**
     * @deprecated since 3.5
     * for compatibility
     */
    public ConsumerUID(int oldnum) {
         super(oldnum);
    }

    public boolean isEmpty() {
        return id == 0;
    }

    public void initializeID() {
        if (id == 0)
            id = UniqueID.generateID(getPrefix());
    }

    public void clear() {
        id =0;
        conuid = null;
        brokeraddr = null;
        ackType =Session.NONE;
    }

    public void updateUID(ConsumerUID uid) {
        id = uid.id;
    }

    public void copy(ConsumerUID uid) {
        id = uid.id;
        conuid = uid.conuid;
        brokeraddr = uid.brokeraddr;
        ackType = uid.ackType;
    }


    public boolean isAutoAck() {
        return (ackType == Session.AUTO_ACKNOWLEDGE);
    }

    public int getAckType() {
        return ackType;
    }

    public String getAckMode() {
        switch(ackType) {
            case Session.AUTO_ACKNOWLEDGE:
                return "AUTO_ACKNOWLEDGE";
            case Session.DUPS_OK_ACKNOWLEDGE:
                return "DUPS_OK_ACKNOWLEDGE";
            case Session.CLIENT_ACKNOWLEDGE:
                return "CLIENT_ACKNOWLEDGE";
            case Session.NO_ACK_ACKNOWLEDGE :
                return "NO_ACK_ACKNOWLEDGE";
            default:
                return "NONE";
        }
    }

    public boolean isDupsOK() {
        return (ackType == Session.DUPS_OK_ACKNOWLEDGE);
    }

    public boolean isNoAck() {
        return (ackType == Session.NO_ACK_ACKNOWLEDGE);
    }

    public boolean isUnsafeAck() {
        return isDupsOK() || isNoAck();
    }

    public void setAckType(int mode) {
        this.ackType = mode;
    }

    public void setConnectionUID(ConnectionUID cid) {
        this.conuid = cid;
    }
    public ConnectionUID getConnectionUID() {
        return conuid;
    }
    public void setBrokerAddress(BrokerAddress bkraddr) {
        this.brokeraddr = bkraddr;
    }
    public BrokerAddress getBrokerAddress() {
        if (brokeraddr == null)
            brokeraddr = Globals.getMyAddress();
        return this.brokeraddr;
    }

    public boolean isLocal() {
        return brokeraddr == null || this.brokeraddr == Globals.getMyAddress();
    }

    public String toString() {
        return "[consumer:" + super.toString() + ", type="
                 + getAckMode() +"]";
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        id = in.readLong();

        ackType=Session.NONE;
        conuid = null;
        brokeraddr = Globals.getMyAddress();
        shouldStore = false;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(id);
    }

    private void readObject(java.io.ObjectInputStream ois)
        throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        ackType=Session.NONE;
        conuid = null;
        brokeraddr = Globals.getMyAddress();
        shouldStore = false;
    }

}
