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
 * @(#)TransactionBroker.java	1.10 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAClusteredBroker;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.*;
import java.io.*;

/**
 * A transaction participant broker
 */

public class TransactionBroker implements Externalizable, Cloneable
{
    static final long serialVersionUID = 4331266333483540901L;

    transient private static Logger logger = Globals.getLogger();

    static final int PENDING  = 0;
    static final int COMPLETE = 1;

    BrokerAddress broker = null;
    int state  = PENDING;

    // default construct for uninitialized object
    public TransactionBroker() {
    }

    /**
     */
    public TransactionBroker(BrokerAddress broker) {
        this.broker = broker;
        state = PENDING;
    }

    public TransactionBroker(BrokerAddress broker, boolean completed) {
        this(broker);
        if (completed) state = COMPLETE;
    }

    public BrokerAddress getBrokerAddress() {
        return broker;
    }

    public boolean isCompleted() {
        return state == COMPLETE;
    }

    public void setCompleted(boolean value) {
        state = (value ? COMPLETE : PENDING);
    }

    public boolean copyState(TransactionBroker b) throws BrokerException {
        if (state == b.state) return false;
        if (state == PENDING) {
            state = b.state;
            return true;
        }
        throw new BrokerException(
        "Can't update transaction broker state from "+toString(state)+ " to "+toString(b.state));
    }

    public int hashCode() {
        return broker.hashCode();
    }

    // just compare the hashcode
    public boolean equals(Object o) {
        if (!(o instanceof TransactionBroker)) {
            return false;
        }
        TransactionBroker other = (TransactionBroker)o;
        BrokerAddress thiscurrb = this.getCurrentBrokerAddress();
        BrokerAddress othercurrb = other.getCurrentBrokerAddress();
        boolean sameaddr = ((this.broker).equals(other.broker) ||
                            (thiscurrb != null && 
                             thiscurrb.equals(othercurrb)));
        if (!Globals.getDestinationList().isPartitionMode()) {
            return sameaddr;
        }
        return sameaddr && 
               (this.broker.getStoreSessionUID()).equals(
                other.broker.getStoreSessionUID());
    }

    public BrokerAddress getCurrentBrokerAddress() {
        if (!Globals.getHAEnabled()) {
            return getBrokerAddress();
        }
        String brokerid = null;
        UID ss = broker.getStoreSessionUID();
        if (ss == null) {
            return null;
        }
        brokerid = Globals.getClusterManager().lookupStoreSessionOwner(ss);
        if (brokerid == null) {
            return null;
        }
        if (brokerid.equals(Globals.getMyAddress().getBrokerID())) {
            return Globals.getMyAddress();
        }
        return Globals.getClusterBroadcast().lookupBrokerAddress(brokerid);
    }

    public boolean isSame(UID ssid) {
        if (!Globals.getHAEnabled()) {
            return false;
        }
        UID ss = broker.getStoreSessionUID();
        if (ss.equals(ssid)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "[" + broker.toString() + "]"+
                ((state == COMPLETE) ? "":toString(state));
    }

    private static String toString(int s) {
        if (s == PENDING) {
            return "PENDING";
        }
        if (s == COMPLETE) {
            return "COMPLETE"; 
        }
        return "UNKNOWN";
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {

        state = in.readInt();
        broker = (BrokerAddress)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(state);
        out.writeObject(broker);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error ("This should never happen!");
        }
    }

}
