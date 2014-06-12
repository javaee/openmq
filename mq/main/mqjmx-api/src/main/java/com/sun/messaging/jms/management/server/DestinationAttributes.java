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
 * @(#)DestinationAttributes.java	1.10 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * Class containing information on destination attributes.
 */
public class DestinationAttributes {
    /** 
     * Consumer flow limit
     */
    public static final String		CONSUMER_FLOW_LIMIT = "ConsumerFlowLimit";

    /** 
     * Local Only
     */
    public static final String		LOCAL_ONLY = "LocalOnly";

    /** 
     * Limit behavior
     */
    public static final String		LIMIT_BEHAVIOR = "LimitBehavior";

    /** 
     * LocalDeliveryPreferred
     */
    public static final String		LOCAL_DELIVERY_PREFERRED = "LocalDeliveryPreferred";

    /** 
     * Auto creation of queues.
     */
    public static final String		AUTO_CREATE_QUEUES = "AutoCreateQueues";

    /** 
     * Max number of active consumers for auto created queues.
     */
    public static final String		AUTO_CREATE_QUEUE_MAX_NUM_ACTIVE_CONSUMERS 
							= "AutoCreateQueueMaxNumActiveConsumers";

    /** 
     * Max number of backup consumers for auto created queues.
     */
    public static final String		AUTO_CREATE_QUEUE_MAX_NUM_BACKUP_CONSUMERS 
							= "AutoCreateQueueMaxNumBackupConsumers";

    /** 
     * Auto creation of topics.
     */
    public static final String		AUTO_CREATE_TOPICS = "AutoCreateTopics";

    /** 
     * Connection ID - only for temporary destinations
     */
    public static final String		CONNECTION_ID = "ConnectionID";

    /** 
     * Truncate bodies of messages in DMQ.
     */
    public static final String		DMQ_TRUNCATE_BODY = "DMQTruncateBody";

    /** 
     * Logging of dead messages.
     */
    public static final String		LOG_DEAD_MSGS = "LogDeadMsgs";

    /** 
     * Max size of a message.
     */
    public static final String		MAX_BYTES_PER_MSG = "MaxBytesPerMsg";

    /** 
     * Max number of active consumers.
     */
    public static final String		MAX_NUM_ACTIVE_CONSUMERS 
							= "MaxNumActiveConsumers";

    /** 
     * Max number of backup consumers.
     */
    public static final String		MAX_NUM_BACKUP_CONSUMERS 
							= "MaxNumBackupConsumers";

    /** 
     * Max number of messages.
     */
    public static final String		MAX_NUM_MSGS = "MaxNumMsgs";

    /** 
     * Max number of producers.
     */
    public static final String		MAX_NUM_PRODUCERS = "MaxNumProducers";

    /** 
     * Max total msg bytes
     */
    public static final String		MAX_TOTAL_MSG_BYTES = "MaxTotalMsgBytes";

    /** 
     * Number of destinations
     */
    public static final String		NUM_DESTINATIONS = "NumDestinations";

    /** 
     * Number of messages
     */
    public static final String		NUM_MSGS = "NumMsgs";

    /** 
     * Number of messages originating from producers on remote brokers
     */
    public static final String		NUM_MSGS_REMOTE = "NumMsgsRemote";

    /** 
     * Number of messages held in transaction
     */
    public static final String		NUM_MSGS_HELD_IN_TRANSACTION = "NumMsgsHeldInTransaction";

    /** 
     * Number of messages pending acknowledgement
     */
    public static final String		NUM_MSGS_PENDING_ACKS = "NumMsgsPendingAcks";

    /** 
     * Number of messages in delay delivery
     */
    public static final String		NUM_MSGS_IN_DELAY_DELIVERY = "NumMsgsInDelayDelivery";

    /** 
     * Number of messages in dead message queue.
     */
    public static final String		NUM_MSGS_IN_DMQ = "NumMsgsInDMQ";

    /** 
     * Total message bytes.
     */
    public static final String		TOTAL_MSG_BYTES = "TotalMsgBytes";

    /** 
     * Total message bytes from messages originating from producers on remote brokers.
     */
    public static final String		TOTAL_MSG_BYTES_REMOTE = "TotalMsgBytesRemote";

    /** 
     * Total message bytes held in transaction.
     */
    public static final String		TOTAL_MSG_BYTES_HELD_IN_TRANSACTION = "TotalMsgBytesHeldInTransaction";

    /** 
     * Total message bytes in dead message queue.
     */
    public static final String		TOTAL_MSG_BYTES_IN_DMQ = "TotalMsgBytesInDMQ";

    /** 
     * Average number of active consumers
     */
    public static final String		AVG_NUM_ACTIVE_CONSUMERS = "AvgNumActiveConsumers";

    /** 
     * Average number of backup consumers
     */
    public static final String		AVG_NUM_BACKUP_CONSUMERS = "AvgNumBackupConsumers";

    /** 
     * Average number of consumers
     */
    public static final String		AVG_NUM_CONSUMERS = "AvgNumConsumers";

    /** 
     * Average number of messages
     */
    public static final String		AVG_NUM_MSGS = "AvgNumMsgs";

    /** 
     * Average total message bytes
     */
    public static final String		AVG_TOTAL_MSG_BYTES = "AvgTotalMsgBytes";

    /** 
     * Created by administrator.
     */
    public static final String		CREATED_BY_ADMIN = "CreatedByAdmin";

    /** 
     * Disk reserved.
     */
    public static final String		DISK_RESERVED = "DiskReserved";

    /** 
     * Disk Used.
     */
    public static final String		DISK_USED = "DiskUsed";

    /** 
     * Disk utilization ratio.
     */
    public static final String		DISK_UTILIZATION_RATIO = "DiskUtilizationRatio";

    /** 
     * Msg bytes in
     */
    public static final String		MSG_BYTES_IN = "MsgBytesIn";

    /** 
     * Msg bytes out
     */
    public static final String		MSG_BYTES_OUT = "MsgBytesOut";

    /** 
     * Destination name
     */
    public static final String		NAME = "Name";

    /** 
     * Number of active consumers
     */
    public static final String		NUM_ACTIVE_CONSUMERS = "NumActiveConsumers";

    /** 
     * Number of backup consumers
     */
    public static final String		NUM_BACKUP_CONSUMERS = "NumBackupConsumers";

    /** 
     * Number of consumers
     */
    public static final String		NUM_CONSUMERS = "NumConsumers";

    /** 
     * Number of wildcards
     */
    public static final String		NUM_WILDCARDS = "NumWildcards";

    /** 
     * Number of wildcard consumers
     */
    public static final String		NUM_WILDCARD_CONSUMERS = "NumWildcardConsumers";

    /** 
     * Number of wildcard producers
     */
    public static final String		NUM_WILDCARD_PRODUCERS = "NumWildcardProducers";

    /** 
     * Number of msgs in
     */
    public static final String		NUM_MSGS_IN = "NumMsgsIn";

    /** 
     * Number of msgs out
     */
    public static final String		NUM_MSGS_OUT = "NumMsgsOut";

    /** 
     * Number of producers
     */
    public static final String		NUM_PRODUCERS = "NumProducers";

    /** 
     * Peak message bytes
     */
    public static final String		PEAK_MSG_BYTES = "PeakMsgBytes";

    /** 
     * Peak number of active consumers
     */
    public static final String		PEAK_NUM_ACTIVE_CONSUMERS = "PeakNumActiveConsumers";

    /** 
     * Peak number of backup consumers
     */
    public static final String		PEAK_NUM_BACKUP_CONSUMERS = "PeakNumBackupConsumers";

    /** 
     * Peak number of consumers
     */
    public static final String		PEAK_NUM_CONSUMERS = "PeakNumConsumers";

    /** 
     * Peak number of messages
     */
    public static final String		PEAK_NUM_MSGS = "PeakNumMsgs";

    /** 
     * Peak total message bytes
     */
    public static final String		PEAK_TOTAL_MSG_BYTES = "PeakTotalMsgBytes";

    /** 
     * Destination state.
     */
    public static final String		STATE = "State";

    /**
     * Next Message to be delivered
     */
     public static final String NEXT_MESSAGE_ID = "NextMessageID";


    /** 
     * String representation of destination state.
     */
    public static final String		STATE_LABEL = "StateLabel";

    /** 
     * Attribute indicating if a destination is temporary or not.
     */
    public static final String		TEMPORARY = "Temporary";

    /** 
     * Destination type
     */
    public static final String		TYPE = "Type";

    /** 
     * Use dead message queue.
     */
    public static final String		USE_DMQ = "UseDMQ";

    /*
     * XML schema validation enabled
     */
    public static final String		VALIDATE_XML_SCHEMA_ENABLED 
						= "ValidateXMLSchemaEnabled";

    /**
     * List of XML schema URIs
     */
    public static final String		XML_SCHEMA_URI_LIST 
						= "XMLSchemaURIList";

    /*
     * Reload XML schema on failure
     */
    public static final String		RELOAD_XML_SCHEMA_ON_FAILURE 
						= "ReloadXMLSchemaOnFailure";

    /*
     * Class cannot be instantiated
     */
    private DestinationAttributes() {
    }
    
}
