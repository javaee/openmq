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
 * @(#)JMSRIConstants.java	1.4 06/27/07
 */ 

package com.sun.jms.spi;
import javax.jms.*;
import java.util.Map;

public interface JMSRIConstants {
    final static int QUEUE = 0;
    final static int TOPIC = 1;
    
    /**
     * Return both QUEUE and TOPIC destinations.
     * @see JMSAdmin#getDestinations(int)
     */
    final static int ALL   = 2; 

    // property identifiers for creating ConnectionFactory.

    /**
     * ConnectionFactory property representing jms service host.
     * This property is optional and defaults to accessing the
     * jms service running on the localhost.
     * @see JMSAdmin#createConnectionFactory(int, Map)
     * @see JMSAdmin#createXAConnectionFactory(int, Map)
     */
    final static String CF_URL       = "url";
 
    /**
     * ConnectionFactory property representing transport to use to 
     * connect from JMS client to JMS Service. <p>
     * Defaults to TRANSPORT_RMIIIOP.
     * @see JMSAdmin#createConnectionFactory(int, Map)
     * @see JMSAdmin#createXAConnectionFactory(int, Map)
     * @see #TRANSPORT_RMIIIOP
     * @see #TRANSPORT_JRMP
     */
    final static String CF_TRANSPORT = "transport";

    /**
     * ConnectionFactory propery representing 
     * ClientID to assign to a connection when created
     * from connection factory created with this property
     * set. It is optional
     * to set this value. JMS will generate a default one.
     * ClientID's are only used in scoping durable subscription's
     * namespaces as of JMS 1.0.2.
     * @see JMSAdmin#createConnectionFactory(int, Map)
     * @see JMSAdmin#createXAConnectionFactory(int, Map)
     */
    final static String CF_CLIENT_ID = "clientId";

    /**
     * List of properties for creating a ConnectionFactory.
     * @see JMSAdmin#createConnectionFactory(int, Map)
     * @see JMSAdmin#createXAConnectionFactory(int, Map)
     */
    final static String[] CF_PROPERTIES = { CF_URL, CF_TRANSPORT, CF_CLIENT_ID };

    /**
     * Values for CF_TRANSPORT.
     * @see #CF_TRANSPORT
     */
    final static String TRANSPORT_RMIIIOP = "rmiiiop";
    final static String TRANSPORT_RMIJRMP = "rmijrmp";


    // Map identifiers for Destination creation.

    /**
     * Boolean Value. If true, create overwrites an exisiting destination with same name.
     * If false, throw a JMSException if destination already exists.
     * Also used for durable subscriptions.
     * Defaults to false if not provided in properties.
     * 
     * @see JMSAdmin#createProviderDestination(String, int, Map)
     * @see JMSAdmin#createDurableSubscription(String, TopicConnectionFactory, Topic, String, Map)
     */
    final static String OVERWRITE = "overwrite";

    /**
     * Boolean Value. If true, create a temporary destination and ignore
     * destinationName provided. Defaults to false if not mentioned in 
     * properties.
     *
     * @see JMSAdmin#createProviderDestination(String, int, Map)
     */
    final static String DESTINATION_IS_TEMPORARY = "isTemporary";


    //EXCEPTION CODES
    /**
     * Error code returned by createServiceDestination() when overwriting an
     * existing destination is not allowed.
     */
    final static String DESTINATION_ALREADY_EXISTS = "destinationAlreadyExists";
}


