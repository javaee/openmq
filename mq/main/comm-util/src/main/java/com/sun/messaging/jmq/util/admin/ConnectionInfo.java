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
 * @(#)ConnectionInfo.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.util.admin;

import com.sun.messaging.jmq.util.MetricCounters;

/**
 * ConnectionInfo encapsulates information about a JMQ Connection. It is used
 * to pass this information between the Broker and an administration client.
 *
 * This class has no updateable fields. The admin client should consider
 * it "read-only"
 */
public class ConnectionInfo extends AdminInfo implements java.io.Serializable {

    /**
     * Broker internal connection ID.
     */
    public byte[]	id;

    /**
     * Connection UUID
     */
    public long         uuid;

    /** 
     * Number of consumers on this connection
     */
    public int          nconsumers = 0;

    /** 
     * Number of producers on this connection
     */
    public int          nproducers = 0;

    /**
     * Remote port number
     */
    public int          remPort = 0;

    /**
     * IP address of client on the connection
     */
    public byte[]	remoteIP;

    /**
     * Metrics for connection
     */
    public MetricCounters metrics;

    /**
     * Name of user authenticated on connection. Null if not authenticated
     * by a user.
     */
    public String	user = "";

    /**
     * JMS ClientID of client on connection
     */
    public String	clientID = "";

    /**
     * User agent string
     */
    public String	userAgent = "";

    /**
     * Service this connection is connected to
     */
    public String	service = "";

    /**
     * Constructor for Consumer.
     */
    public ConnectionInfo() {
	reset();
    }

    public void reset() {
	id = null;
	remoteIP = null;
	metrics = null;
	user = "";
	clientID = "";
        service = "";
        userAgent = "";
    }


    /**
     * Return a string representation of the connection.
     * <pre>
     * dipol@client1(129.144.252.154:0)
     * </pre>
     *
     * @return String representation of connection.
     */
    public String toString() {

	return user + "@" + clientID + "(" +
	    com.sun.messaging.jmq.util.net.IPAddress.rawIPToString(remoteIP, true, true) + ")";
    }

}
