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
 * @(#)MQObjectName.java	1.14 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

/**
 * Utility class for manipulating Message Queue MBean Object Names.
 */
public class MQObjectName {

    /*
     ****************************
     * Start of private constants
     ****************************
     */

    /*
     * Domain name for MQ MBeans
     */
    private static final String MBEAN_DOMAIN_NAME = "com.sun.messaging.jms.server";

    /*
     * MBean names
     */
    private static final String BROKER			= "Broker";
    private static final String SERVICE_MANAGER		= "ServiceManager";
    private static final String CONNECTION_MANAGER	= "ConnectionManager";
    private static final String DESTINATION_MANAGER	= "DestinationManager";
    private static final String CONSUMER_MANAGER	= "ConsumerManager";
    private static final String PRODUCER_MANAGER	= "ProducerManager";
    private static final String TRANSACTION_MANAGER	= "TransactionManager";
    private static final String SERVICE			= "Service";
    private static final String DESTINATION		= "Destination";
    private static final String CONNECTION		= "Connection";
    private static final String CLUSTER			= "Cluster";
    private static final String LOG			= "Log";
    private static final String JVM			= "JVM";

    /*
     * Strings that represent 'partial' object names. The complete
     * object name is created by appending additional name/value pairs.
     * These constants are used by the utility methods in this class.
     */

    /*
     * These strings are used to specify (via the subtype key) if the object
     * name is for a config or monitor MBean.
     */
    private static final String SUBTYPE_SUFFIX_CONFIG	= ",subtype=Config";
    private static final String SUBTYPE_SUFFIX_MONITOR	= ",subtype=Monitor";

    /**
     * The domain name and the type key property in the ObjectName for a 
     * ServiceConfig MBean. The unique ObjectName for a ServiceConfig MBean can be formed 
     * by appending this string with ",name=<EM>service name</EM>".
     */
    private static final String SERVICE_CONFIG_DOMAIN_TYPE
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + SERVICE 
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * The domain name and the type key property in the ObjectName for a 
     * DestinationConfig MBean. The unique ObjectName for a DestinationConfig MBean can 
     * be formed by appending this string with ",desttype=<EM>destination type</EM>,
     * name=<EM>destination name</EM>".
     */
    private static final String DESTINATION_CONFIG_DOMAIN_TYPE 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + DESTINATION 
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * The domain name and the type key property in the ObjectName for a 
     * ConnectionConfig MBean. The unique ObjectName for a ConnectionConfig 
     * MBean can be formed by appending this string with 
     * ",id=<EM>connection id</EM>".
     */
    private static final String CONNECTION_CONFIG_DOMAIN_TYPE 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONNECTION 
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * The domain name and the type key property in the ObjectName for a 
     * ServiceMonitor MBean. The unique ObjectName for a ServiceMonitor MBean 
     * can be formed by appending this string with ",name=<EM>service name</EM>".
     */
    private static final String SERVICE_MONITOR_DOMAIN_TYPE
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + SERVICE
			        + SUBTYPE_SUFFIX_MONITOR;

    /**
     * The domain name and the type key property in the ObjectName for a 
     * DestinationMonitor MBean. The unique ObjectName for a DestinationMonitor 
     * MBean can be formed by appending this string with 
     * ",desttype=<EM>destination type</EM>, name=<EM>destination name</EM>".
     */
    private static final String DESTINATION_MONITOR_DOMAIN_TYPE 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + DESTINATION
			        + SUBTYPE_SUFFIX_MONITOR;

    /**
     * The domain name and the type key property in the ObjectName for a 
     * ConnectionMonitor MBean. The unique ObjectName for a ConnectionMonitor 
     * MBean can * be formed by appending this string with 
     * ",id=<EM>connection id</EM>".
     */
    private static final String CONNECTION_MONITOR_DOMAIN_TYPE 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONNECTION
				+ SUBTYPE_SUFFIX_MONITOR;

    /*
     **************************
     * End of private constants
     **************************
     */


    /*
     ***************************
     * Start of public constants
     ***************************
     */

    /**
     * String representation of the ObjectName for the Broker Config MBean.
     */
    public static final String BROKER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + BROKER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the 
     * ConnectionManager Config MBean.
     */
    public static final String CONNECTION_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONNECTION_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the 
     * ConsumerManager Config MBean.
     */
    public static final String CONSUMER_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONSUMER_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the 
     * ServiceManager Config MBean.
     */
    public static final String SERVICE_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + SERVICE_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the DestinationManager Config MBean.
     */
    public static final String DESTINATION_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + DESTINATION_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the Cluster Config MBean.
     */
    public static final String CLUSTER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CLUSTER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the Log Config MBean.
     */
    public static final String LOG_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + LOG
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the 
     * ProducerManager Config MBean.
     */
    public static final String PRODUCER_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + PRODUCER_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the 
     * TransactionManager Config MBean.
     */
    public static final String TRANSACTION_MANAGER_CONFIG_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + TRANSACTION_MANAGER
				+ SUBTYPE_SUFFIX_CONFIG;

    /**
     * String representation of the ObjectName for the Broker Monitor MBean.
     */
    public static final String BROKER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + BROKER 
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the ServiceManager 
     * Monitor MBean.
     */
    public static final String SERVICE_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + SERVICE_MANAGER 
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the DestinationManager 
     * Monitor MBean.
     */
    public static final String DESTINATION_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + DESTINATION_MANAGER
				+ SUBTYPE_SUFFIX_MONITOR;
    /**
     * String representation of the ObjectName for the TransactionManager 
     * Monitor MBean.
     */
    public static final String TRANSACTION_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + TRANSACTION_MANAGER
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the ConnectionManager 
     * Monitor MBean.
     */
    public static final String CONNECTION_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONNECTION_MANAGER
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the ConsumerManager 
     * Monitor MBean.
     */
    public static final String CONSUMER_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CONSUMER_MANAGER
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the ProducerManager 
     * Monitor MBean.
     */
    public static final String PRODUCER_MANAGER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + PRODUCER_MANAGER
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the JVM Monitor MBean.
     */
    public static final String JVM_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + JVM
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the Cluster Monitor MBean.
     */
    public static final String CLUSTER_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + CLUSTER
				+ SUBTYPE_SUFFIX_MONITOR;

    /**
     * String representation of the ObjectName for the Log Monitor MBean.
     */
    public static final String LOG_MONITOR_MBEAN_NAME 
    			= MBEAN_DOMAIN_NAME 
				+ ":type=" + LOG
				+ SUBTYPE_SUFFIX_MONITOR;

    /*
     *************************
     * End of public constants
     *************************
     */

    private MQObjectName()  {
    }

    /**
     * Creates ObjectName for service configuration MBean.
     *
     * @param serviceName Name of service.
     * @return ObjectName of Service MBean
     */
    public static ObjectName createServiceConfig(String serviceName)  
				throws MalformedObjectNameException,
					NullPointerException  {
	String s = SERVICE_CONFIG_DOMAIN_TYPE
			+ ",name="
			+ serviceName;

	ObjectName o = new ObjectName(s);

	return (o);
    }
    /**
     * Creates ObjectName for service monitoring MBean.
     *
     * @param serviceName Name of service.
     * @return ObjectName of Service MBean
     */
    public static ObjectName createServiceMonitor(String serviceName)  
				throws MalformedObjectNameException,
					NullPointerException  {

	String s = SERVICE_MONITOR_DOMAIN_TYPE
			+ ",name="
			+ serviceName;

	ObjectName o = new ObjectName(s);
	
	return (o);
    }

    /**
     * Creates ObjectName for destination configuration MBean.
     *
     * @param destinationType Type of destination. One of 
     * DestinationType.TOPIC, DestinationType.QUEUE.
     * @param destinationName Name of destination.
     * @return ObjectName of service MBean
     */
    public static ObjectName createDestinationConfig(String destinationType,
					String destinationName)  
				throws MalformedObjectNameException,
					NullPointerException  {
	String s = DESTINATION_CONFIG_DOMAIN_TYPE
			+ ",desttype="
			+ destinationType
			+ ",name="
			+ ObjectName.quote(destinationName);

	ObjectName o = new ObjectName(s);
	
	return (o);
    }

    /**
     * Creates ObjectName for specified destination monitor MBean.
     *
     * @param destinationType Type of destination. One of 
     * DestinationType.TOPIC, DestinationType.QUEUE.
     * @param destinationName Name of destination.
     * @return ObjectName of DestinationMonitor MBean
     */
    public static ObjectName createDestinationMonitor(String destinationType,
					String destinationName)  
				throws MalformedObjectNameException,
					NullPointerException  {
	String s = DESTINATION_MONITOR_DOMAIN_TYPE
			+ ",desttype="
			+ destinationType
			+ ",name="
			+ ObjectName.quote(destinationName);

	ObjectName o = new ObjectName(s);
	
	return (o);
    }

    /**
     * Creates ObjectName for specified connection configuration MBean.
     *
     * @param id Connection ID
     * @return ObjectName of ConnectionConfig MBean
     */
    public static ObjectName createConnectionConfig(String id)  {
	String s = CONNECTION_CONFIG_DOMAIN_TYPE
			+ ",id="
			+ id;

	ObjectName o = null;
	try  {
	    o = new ObjectName(s);
	} catch (MalformedObjectNameException mfe) {
	    /*
	     * Should not get here
	     */
	    
	    throw new RuntimeException("Failed to create Message Queue object name",
					mfe);
	}

	return (o);
    }

    /**
     * Creates ObjectName for specified connection monitoring MBean.
     *
     * @param id Connection ID
     * @return ObjectName of ConnectionMonitor MBean
     */
    public static ObjectName createConnectionMonitor(String id)  {
	String s = CONNECTION_MONITOR_DOMAIN_TYPE
			+ ",id="
			+ id;

	ObjectName o = null;
	try  {
	    o = new ObjectName(s);
	} catch (MalformedObjectNameException mfe) {
	    /*
	     * Should not get here
	     */
	    
	    throw new RuntimeException("Failed to create Message Queue object name",
					mfe);
	}

	return (o);
    }
}
