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
 * @(#)ServiceAttributes.java	1.7 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * Class containing information on service attributes.
 */
public class ServiceAttributes {
    /** 
     * Service Name
     */
    public static final String		NAME = "Name";

    /** 
     * Service State
     */
    public static final String		STATE = "State";

    /** 
     * String representation of service state
     */
    public static final String		STATE_LABEL = "StateLabel";

    /** 
     * Max threads
     */
    public static final String		MAX_THREADS = "MaxThreads";

    /** 
     * Min threads
     */
    public static final String		MIN_THREADS = "MinThreads";

    /** 
     * Number of connections created
     */
    public static final String		NUM_CONNECTIONS_OPENED = "NumConnectionsOpened";

    /** 
     * Number of connections rejected
     */
    public static final String		NUM_CONNECTIONS_REJECTED = "NumConnectionsRejected";

    /** 
     * Port
     */
    public static final String		PORT = "Port";

    /** 
     * Msg bytes in
     */
    public static final String		MSG_BYTES_IN = "MsgBytesIn";

    /** 
     * Msg bytes out
     */
    public static final String		MSG_BYTES_OUT = "MsgBytesOut";

    /** 
     * Number of active threads
     */
    public static final String		NUM_ACTIVE_THREADS = "NumActiveThreads";

    /** 
     * Number of msgs in
     */
    public static final String		NUM_MSGS_IN = "NumMsgsIn";

    /** 
     * Number of msgs out
     */
    public static final String		NUM_MSGS_OUT = "NumMsgsOut";

    /** 
     * Number of pkts in
     */
    public static final String		NUM_PKTS_IN = "NumPktsIn";

    /** 
     * Number of pkts out
     */
    public static final String		NUM_PKTS_OUT = "NumPktsOut";

    /** 
     * Number of services
     */
    public static final String		NUM_SERVICES = "NumServices";

    /** 
     * Number of connections
     */
    public static final String		NUM_CONNECTIONS = "NumConnections";

    /** 
     * Number of consumers
     */
    public static final String		NUM_CONSUMERS = "NumConsumers";

    /** 
     * Number of producers
     */
    public static final String		NUM_PRODUCERS = "NumProducers";

    /** 
     * Pkt bytes in
     */
    public static final String		PKT_BYTES_IN = "PktBytesIn";

    /** 
     * Pkt bytes out
     */
    public static final String		PKT_BYTES_OUT = "PktBytesOut";

    /** 
     * Thread pool model
     */
    public static final String		THREAD_POOL_MODEL = "ThreadPoolModel";

    /*
     * Class cannot be instantiated
     */
    private ServiceAttributes() {
    }
    
}
