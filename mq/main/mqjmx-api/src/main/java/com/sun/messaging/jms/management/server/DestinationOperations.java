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
 * @(#)DestinationOperations.java	1.7 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * Class containing information on destination operations.
 */
public class DestinationOperations {
    /** 
     * Create a destination.
     */
    public static final String		CREATE = "create";

    /** 
     * Compact a destination.
     */
    public static final String		COMPACT = "compact";

    /** 
     * Destroy a destination
     */
    public static final String		DESTROY = "destroy";

    /** 
     * Get active consumers IDs
     */
    public static final String		GET_ACTIVE_CONSUMER_IDS = "getActiveConsumerIDs";

    /** 
     * Get backup consumers IDs
     */
    public static final String		GET_BACKUP_CONSUMER_IDS = "getBackupConsumerIDs";

    /** 
     * Get consumer IDs
     */
    public static final String		GET_CONSUMER_IDS = "getConsumerIDs";

    /** 
     * Get connection - relevant for temporary destinations only.
     */
    public static final String		GET_CONNECTION = "getConnection";

    /** 
     * Get list of destination MBean object names.
     */
    public static final String		GET_DESTINATIONS = "getDestinations";

    /** 
     * Get producer IDs
     */
    public static final String		GET_PRODUCER_IDS = "getProducerIDs";

    /** 
     * Get wildcards used on this destination
     * i.e. Wildcards used by consumers/producers that match this destination
     */
    public static final String		GET_WILDCARDS = "getWildcards";

    /** 
     * Get consumer wildcards used on this destination
     */
    public static final String		GET_CONSUMER_WILDCARDS = "getConsumerWildcards";

    /** 
     * Get number of consumers on this destination that use a specific wildcard
     */
    public static final String		GET_NUM_WILDCARD_CONSUMERS = "getNumWildcardConsumers";

    /** 
     * Get producer wildcards used on this destination
     */
    public static final String		GET_PRODUCER_WILDCARDS = "getProducerWildcards";

    /** 
     * Get number of producers on this destination that use a specific wildcard
     */
    public static final String		GET_NUM_WILDCARD_PRODUCERS = "getNumWildcardProducers";

    /** 
     * Pause a destination.
     */
    public static final String		PAUSE = "pause";

    /** 
     * Purge a destination.
     */
    public static final String		PURGE = "purge";

    /** 
     * Resume a destination.
     */
    public static final String		RESUME = "resume";

    /*
     * Class cannot be instantiated
     */
    private DestinationOperations() {
    }
    
}
