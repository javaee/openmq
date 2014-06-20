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
 * @(#)ProducerInfo.java	1.3 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * This class contains constants/names for fields in the CompositeData
 * that is returned by the operations of the Producer Manager Monitor 
 * MBean.
 */
public class ProducerInfo implements java.io.Serializable  {

    /** 
     * Connection ID
     */
    public static final String		CONNECTION_ID = "ConnectionID";

    /** 
     * Creation Time
     */
    public static final String		CREATION_TIME = "CreationTime";

    /** 
     * Destination Name
     */
    public static final String		DESTINATION_NAME = "DestinationName";

    /** 
     * Destination Names (that match wildcard)
     */
    public static final String		DESTINATION_NAMES = "DestinationNames";

    /** 
     * Destination Type
     */
    public static final String		DESTINATION_TYPE = "DestinationType";

    /** 
     * Flow Paused
     */
    public static final String		FLOW_PAUSED = "FlowPaused";

    /** 
     * Host
     */
    public static final String		HOST = "Host";

    /** 
     * Number of messages sent from producer.
     */
    public static final String		NUM_MSGS = "NumMsgs";

    /** 
     * Producer ID
     */
    public static final String		PRODUCER_ID = "ProducerID";

    /** 
     * Service Name
     */
    public static final String		SERVICE_NAME = "ServiceName";

    /** 
     * User
     */
    public static final String		USER = "User";

    /** 
     * Wildcard (whether the producer is a wildcard or not)
     */
    public static final String		WILDCARD = "Wildcard";

    /*
     * Class cannot be instantiated
     */
    private ProducerInfo() {
    }
}
