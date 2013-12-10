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
 * @(#)BrokerAddress.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.core;

import java.io.*;
import java.net.*;

import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.util.UID;

/**
 * This class encapsulates the broker address / identifier. The
 * implementation is specific to the broker topology.
 */
public abstract class BrokerAddress 
       implements Cloneable, Serializable {

    static final long serialVersionUID = -8900410708742494160L;

    BrokerMQAddress address = null;


    public BrokerAddress() {
    }

    public BrokerMQAddress getMQAddress() {
        return address;
    }

    public void initialize(String host, int port)
        throws MalformedURLException, UnknownHostException
    {
        address = BrokerMQAddress.createAddress(host, port);
    }

    public void initialize(BrokerMQAddress ba)
        throws MalformedURLException
    {
        address = ba; 
    }
        

    public int getClusterVersion() {
        return -1;
    }

    public abstract boolean getHAEnabled();
    public abstract String getBrokerID();
    public abstract UID getBrokerSessionUID();
    public abstract UID getStoreSessionUID();
    public abstract void setStoreSessionUID(UID uid);
    public abstract String getInstanceName();

    /**
     * Must be provided by topology specific implementation.
     */
    public abstract Object clone();

    /**
     * Makes a shallow copy of the BrokerAddress object using
     * Object.clone().
     */
    protected Object getObjectClone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Must be provided by topology specific implementation.
     */
    public abstract boolean equals(Object obj);

    /**
     * Must be provided by topology specific implementation.
     */
    public abstract int hashCode();

    /**
     * Get Object.hashCode().
     */
    protected int getObjectHashCode() {
        return super.hashCode();
    }

    /**
     * Get the string representation with the syntax used
     * in the configuration file.
     */
    public String toConfigString() {
        return toString();
    }

    /**
     * Get the string representation with syntax used in cluster protocol
     */
    public abstract String toProtocolString();

    public abstract BrokerAddress fromProtocolString(String s) throws Exception;


    /**
     * Writes the broker address to a given <code> DataOutputStream </code>.
     */
    public abstract void writeBrokerAddress(DataOutputStream dos)
        throws IOException;

    /**
     * Writes the broker address to a given <code> OutputStream </code>.
     */
    public void writeBrokerAddress(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        writeBrokerAddress(dos);
    }

    /**
     * Reads the broker address from a given <code> DataInputStream </code>
     */
    public abstract void readBrokerAddress(DataInputStream dis)
        throws IOException;

    /**
     * Reads the broker address from a given <code> InputStream </code>
     */
    public void readBrokerAddress(InputStream is)
        throws IOException {
        DataInputStream dis = new DataInputStream(is);
        readBrokerAddress(dis);
    }

}

/*
 * EOF
 */
