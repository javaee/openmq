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
 * @(#)ConsumerInfo.java	1.3 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * This class contains constants/names for fields in the CompositeData
 * that is returned by the operations of the Consumer Manager Monitor 
 * MBean.
 */
public class ConsumerInfo implements java.io.Serializable  {
    /** 
     * Acknowledge mode
     */
    public static final String		ACKNOWLEDGE_MODE = "AcknowledgeMode";

    /** 
     * Acknowledge mode label
     */
    public static final String		ACKNOWLEDGE_MODE_LABEL 
							= "AcknowledgeModeLabel";

    /** 
     * Client ID
     */
    public static final String		CLIENT_ID = "ClientID";

    /** 
     * Connection ID
     */
    public static final String		CONNECTION_ID = "ConnectionID";

    /** 
     * Consumer ID
     */
    public static final String		CONSUMER_ID = "ConsumerID";

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
     * Durable (whether the consume is a durable or not)
     */
    public static final String		DURABLE = "Durable";

    /** 
     * DurableActive (whether the durable is active or not)
     */
    public static final String		DURABLE_ACTIVE = "DurableActive";

    /** 
     * Durable name
     */
    public static final String		DURABLE_NAME = "DurableName";

    /** 
     * Flow Paused
     */
    public static final String		FLOW_PAUSED = "FlowPaused";

    /** 
     * Host
     */
    public static final String		HOST = "Host";

    /** 
     * Last acknowledge time
     */
    public static final String		LAST_ACK_TIME = "LastAckTime";

    /** 
     * Number of messages held for consumer.
     */
    public static final String		NUM_MSGS = "NumMsgs";

    /** 
     * Number of messages still held for consumer because
     * acks for them from the consumer are still pending.
     */
    public static final String		NUM_MSGS_PENDING_ACKS 
						= "NumMsgsPendingAcks";

    /** 
     * Selector
     */
    public static final String		SELECTOR = "Selector";

    /** 
     * Service Name
     */
    public static final String		SERVICE_NAME = "ServiceName";

    /** 
     * User
     */
    public static final String		USER = "User";

    /** 
     * Wildcard (whether the consumer is a wildcard or not)
     */
    public static final String		WILDCARD = "Wildcard";

    /** 
     * Number of messages still held for consumer because
     * either they are queued for deliver or pending an ack
     */
    public static final String		NUM_MSGS_PENDING 
						= "NumMsgsPending";

    /**
     * Next message which should be delivered
     */

     public static final String NEXT_MESSAGE_ID = "NextMessageID";

    /*
     * Class cannot be instantiated
     */
    private ConsumerInfo() {
    }
}
