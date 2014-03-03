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
 * @(#)BrokerConstants.java	1.32 07/11/07
 */ 

package com.sun.messaging.jmq.admin.bkrutil;

/**
 * Interface containing constants related to broker administration.
 *
 * This currently holds property names that are shared/common across
 * all broker admin tools.
 */
public interface BrokerConstants  {

    /*
     * Property names for broker attributes
     *
     */
    public static String PROP_NAME_BKR_INSTANCE_NAME	= "imq.instancename";
    public static String PROP_NAME_BKR_PRIMARY_PORT	= "imq.portmapper.port";
    public static String PROP_NAME_BKR_AUTOCREATE_TOPIC	= "imq.autocreate.topic";
    public static String PROP_NAME_BKR_AUTOCREATE_QUEUE	= "imq.autocreate.queue";
    public static String PROP_NAME_BKR_QUEUE_DELIVERY_POLICY= "imq.queue.deliverypolicy";
    public static String PROP_NAME_BKR_LOG_LEVEL	= ".level";
    public static String PROP_NAME_BKR_LOG_ROLL_SIZE	= "java.util.logging.FileHandler.limit";
    public static String PROP_NAME_BKR_LOG_ROLL_INTERVAL= "imq.log.file.rolloversecs";
    /*
    public static String PROP_NAME_BKR_METRIC_INTERVAL	= "imq.metrics.interval";
    */
    public static String PROP_NAME_BKR_MAX_MSG		= "imq.system.max_count";
    public static String PROP_NAME_BKR_MAX_TTL_MSG_BYTES= "imq.system.max_size";
    public static String PROP_NAME_BKR_MAX_MSG_BYTES	= "imq.message.max_size";

    public static String PROP_NAME_BKR_CUR_MSG		= "imq.system.current_count";
    public static String PROP_NAME_BKR_CUR_TTL_MSG_BYTES= "imq.system.current_size";

    public static String PROP_NAME_BKR_CLS_BKRLIST	= "imq.cluster.brokerlist";
    public static String PROP_NAME_BKR_CLS_BKRLIST_ACTIVE= "imq.cluster.brokerlist.active";
    public static String PROP_NAME_BKR_CLS_CFG_SVR	= "imq.cluster.masterbroker";
    public static String PROP_NAME_BKR_CLS_URL		= "imq.cluster.url";
    public static String PROP_NAME_BKR_CLS_CLUSTER_ID	= "imq.cluster.clusterid";
    public static String PROP_NAME_BKR_CLS_HA		= "imq.cluster.ha";
    public static String PROP_NAME_BKR_STORE_MIGRATABLE	= "imq.storemigratable";
    public static String PROP_NAME_BKR_PARTITION_MIGRATABLE	= "imq.partitionmigratable";
    public static String PROP_NAME_BKR_CLS_BROKER_ID	= "imq.brokerid";
    public static String PROP_NAME_BKR_PRODUCT_VERSION	= "imq.product.version";
    public static String PROP_NAME_BKR_AUTOCREATE_QUEUE_MAX_ACTIVE_CONS
					= "imq.autocreate.queue.maxNumActiveConsumers";
    public static String PROP_NAME_BKR_AUTOCREATE_QUEUE_MAX_BACKUP_CONS
					= "imq.autocreate.queue.maxNumBackupConsumers";
    public static String PROP_NAME_BKR_LOG_DEAD_MSGS
					= "imq.destination.logDeadMsgs";
    public static String PROP_NAME_BKR_DMQ_TRUNCATE_MSG_BODY
					= "imq.destination.DMQ.truncateBody";
    public static String PROP_NAME_BKR_AUTOCREATE_DESTINATION_USE_DMQ
                                        = "imq.autocreate.destination.useDMQ";
    public static String PROP_NAME_BKR_IS_EMBEDDED= "imq.embedded";
    public static String PROP_NAME_BKR_VARHOME		= "imq.varhome";
    public static String PROP_NAME_BKR_LICENSE_DESC	= "imq.license.description";
    public static String PROP_NAME_DMQ_CUR_MSG		= "imq.dmq.current_count";
    public static String PROP_NAME_DMQ_CUR_TTL_MSG_BYTES= "imq.dmq.current_size";

    /*
     * Property names returned in Hashtables for GET_TRANSACTION admin message
     */
    public static String PROP_NAME_TXN_ID		= "txnid";
    public static String PROP_NAME_TXN_XID		= "xid";
    public static String PROP_NAME_TXN_NUM_MSGS		= "nmsgs";
    public static String PROP_NAME_TXN_NUM_ACKS		= "nacks";
    public static String PROP_NAME_TXN_USER		= "user";
    public static String PROP_NAME_TXN_CLIENTID		= "clientid";
    public static String PROP_NAME_TXN_TIMESTAMP	= "timestamp";
    public static String PROP_NAME_TXN_CONNECTION	= "connection";
    public static String PROP_NAME_TXN_CONNECTION_ID	= "connectionid";
    public static String PROP_NAME_TXN_STATE		= "state";

    /*
     * Property names returned in Hashtables in GET_CONNECTIONS admin message
     */
    public static String PROP_NAME_CXN_CXN_ID		= "cxnid";
    public static String PROP_NAME_CXN_CLIENT_ID	= "clientid";
    public static String PROP_NAME_CXN_HOST		= "host";
    public static String PROP_NAME_CXN_PORT		= "port";
    public static String PROP_NAME_CXN_USER		= "user";
    public static String PROP_NAME_CXN_NUM_PRODUCER	= "nproducers";
    public static String PROP_NAME_CXN_NUM_CONSUMER	= "nconsumers";
    public static String PROP_NAME_CXN_CLIENT_PLATFORM	= "clientplatform";
    public static String PROP_NAME_CXN_SERVICE		= "service";

    /*
     * Property names returned in Hashtables in GET_JMX admin message
     */
    public static String PROP_NAME_JMX_NAME		= "name";
    public static String PROP_NAME_JMX_ACTIVE		= "active";
    public static String PROP_NAME_JMX_URL		= "url";

    /*
     * Valid values for broker log level.
     */
    public static String[] BKR_LOG_LEVEL_VALID_VALUES	= {
					    "NONE",
					    "ERROR",
					    "WARNING",
					    "INFO"
						};

    /*
     * Queue flavour property names, as expected by the broker
     */
    public static String PROP_NAME_QUEUE_FLAVOUR_SINGLE         = "single";
    public static String PROP_NAME_QUEUE_FLAVOUR_FAILOVER       = "failover";
    public static String PROP_NAME_QUEUE_FLAVOUR_ROUNDROBIN     = "round-robin";

    /*
     * Valid values for broker log level.
     * Note: Indices for the strings below need to match the
     * array contents.
     */
    public static String[] BKR_LIMIT_BEHAV_VALID_VALUES	= {
					    "FLOW_CONTROL",
					    "REMOVE_OLDEST",
					    "REJECT_NEWEST",
					    "REMOVE_LOW_PRIORITY"
						};
    public static String LIMIT_BEHAV_FLOW_CONTROL = BKR_LIMIT_BEHAV_VALID_VALUES[0];
    public static String LIMIT_BEHAV_RM_OLDEST = BKR_LIMIT_BEHAV_VALID_VALUES[1];
    public static String LIMIT_BEHAV_REJECT_NEWEST = BKR_LIMIT_BEHAV_VALID_VALUES[2];
    public static String LIMIT_BEHAV_RM_LOW_PRIORITY = BKR_LIMIT_BEHAV_VALID_VALUES[3];

    /*
     * Transaction types
     */
    public static int TXN_LOCAL		= 0;
    public static int TXN_CLUSTER	= 1;
    public static int TXN_REMOTE	= 2;
    public static int TXN_UNKNOWN	= -1;

}
