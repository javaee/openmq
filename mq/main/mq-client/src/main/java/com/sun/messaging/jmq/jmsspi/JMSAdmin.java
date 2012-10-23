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
 * @(#)JMSAdmin.java	1.21 07/11/07
 */ 

package com.sun.messaging.jmq.jmsspi;

import java.io.IOException;
import java.util.Map;

import javax.jms.JMSException;

/**
 * Interface definition to provide administrative support of iMQ. 
 */

public interface JMSAdmin extends JMSConstants {

    /**
     * Return the SPI version
     */
    String getVersion();

    /**
     * Create a ConnectionFactory administered object
     *
     * @param type        Either QUEUE or TOPIC.
     * @param properties  Connection specific properties.
     * @return New created ConnectionFactory administered object.
     * @exception JMSException thrown if connectionFactory could not be created.
     */
    Object createConnectionFactoryObject(int type, java.util.Map properties)
           throws JMSException;

    /**
     * Create a Destination administered object
     *
     * @param destinationName The destination name.
     * @param type        Either QUEUE or TOPIC.
     * @param properties destination specific properties.
     * @return New created Destination administered object.
     * @exception JMSException thrown if destination object could not be created.
     */
    Object createDestinationObject(String destinationName,
                                          int type,
                                          java.util.Map properties)
                                          throws JMSException;

    /**
     * Create a XAConnectionFactory administered object
     *
     * @param type        Either QUEUE or TOPIC.
     * @param properties Connection specific properties.
     * @return New created JMSXAConnectionFactory administered object.
     *                     ^^^^^
     * @exception JMSException thrown if XAConnectionFactory could not be created.
     */
    Object createXAConnectionFactoryObject(int type, java.util.Map properties) throws JMSException;

    /**
     * Create a Destination administered object.  The destination name is
     * assumed in properties with key JMQDestinationName=xxxx
     *
     * @param type        Either QUEUE or TOPIC.
     * @param properties destination specific properties.
     * @return New created Destination administered object.
     * @exception JMSException thrown if destination object could not be created.
     */
    Object createDestinationObject(int type,
                                          java.util.Map properties)
                                          throws JMSException;

    /**
     * Wrap a standard JMS ConnectionFactory administered object 
     *
     * @param obj a XAQueue/TopicConnectionFactory or Queue/TopicConnectionFactory object   
     * @return a JMSXAConnectionFactory object
     *                     ^^^^^
     * @exception JMSException if fail to wrap
     */
    Object wrapJMSConnectionFactoryObject(Object obj) throws JMSException; 

    /**
     * @return the client-id property name or null
     */
    String clientIDPropertyName();

    void validateJMSSelector(String selector) throws JMSException;

    /**
     * Returns a map of all the properties that a destination
     * object has. 
     * <P>
     * For each property name returned, a corresponding label that can 
     * be used for output display purposes is also returned.
     * The Map returned contains the property names and property
     * labels as key-value pairs. This information can be used to display 
     * more readable output containing property labels and not property
     * names.
     *
     * @param type Either QUEUE or TOPIC.
     * @return Map containing attribute value pairs of property names
     *		and their display labels.
     */
    public Map getAllDestinationObjectProperties(int type) throws JMSException;

    /**
     * Returns a subset of all the properties that a destination 
     * object has. 
     * <P>
     * This collection of properties can be used to construct a user
     * interface where not all of the object's properties will be 
     * displayed, and only a selected few (more important) ones are.
     * <P>
     * For each property name returned, a corresponding label that can 
     * be used for output display purposes is also returned.
     * The Map returned contains the property names and property
     * labels as key-value pairs. This information can be used to display 
     * more readable output containing property labels and not property
     * names.
     *
     * @param type Either QUEUE or TOPIC.
     * @return Map containing attribute value pairs of property names
     *		and their display labels.
     */
    public Map getDisplayedDestinationObjectProperties(int type) throws JMSException;

    /**
     * Returns a map of all the properties that a connection factory
     * object has. 
     * <P>
     * For each property name returned, a corresponding label that can 
     * be used for output display purposes is also returned.
     * The Map returned contains the property names and property
     * labels as key-value pairs. This information can be used to display 
     * more readable output containing property labels and not property
     * names.
     *
     * @param type Either QUEUE or TOPIC.
     * @return Map containing attribute value pairs of property names
     *		and their display labels.
     */
    public Map getAllConnectionFactoryObjectProperies(int type)
						throws JMSException;

    /**
     * Returns a subset of all the properties that a connection factory 
     * object has. 
     * <P>
     * This collection of properties can be used to construct a user
     * interface where not all of the object's properties will be 
     * displayed, and only a selected few (more important) ones are.
     * <P>
     * For each property name returned, a corresponding label that can 
     * be used for output display purposes is also returned.
     * The Map returned contains the property names and property
     * labels as key-value pairs. This information can be used to display 
     * more readable output containing property labels and not property
     * names.
     *
     * @param type Either QUEUE or TOPIC.
     * @return Map containing attribute value pairs of property names
     *		and their display labels.
     */
    public Map getDisplayedConnectionFactoryObjectProperies(int type)
						throws JMSException;

    /**
     * Connect to the provider.
     * @exception JMSException thrown if connection to the provider
     * cannot be established or if an error occurs
     */
    void connectToProvider() throws JMSException;

    /**
     * Disconnect from the provider.
     * @exception JMSException thrown if connection to the provider cannot
     * be closed or ir an error occurs
     */
    void disconnectFromProvider() throws JMSException;

     /**
     * Create a physical Destination within the JMS Provider using the provided
     * properties to define provider specific attributes.
     * Destination is not automatically bound into JNDI namespace.
     *
     * @param destinationName
     * @param destinationType QUEUE or TOPIC
     * @param properties creation properties.
     * @return Identifier for newly created Destination.
     * @exception JMSException thrown if Queue could not be created.
     */
    void createProviderDestination(String destinationName,
                                   int destinationType,
                                   java.util.Map properties)
                                   throws JMSException;

    /**
     * Delete a physical destination within the JMS Provider.
     * @param type        Either QUEUE or TOPIC.
     * @param destinationName
     * @exception JMSException thrown if Queue could not be deleted.
     */
    void deleteProviderDestination(String destinationName,
	             				   int type)
                                   throws JMSException;

    /**
     * Get all provider destinations.
     * 
     * @return A multi dimensional array containing information
     *         about the JMS destinations. array[0] is a String[]
     *         listing the destination names. array[1] is a
     *         String[] listing the destination types.
     * @exception JMSException thrown if array could not be obtained.
     */
    String[][] getProviderDestinations() throws JMSException;


    /**
     * Make start provider command line
     * 
     * @param iMQHome directory of MQ executables, ignored if argsOnly true
     * @param optArgs Array of optional broker command line arguments.
     * @param serverName Instance name of the server.
     * @param return command line arguments only 
     * @return command line for starting provider
     * @exception IOException thrown if the server startup fails.
     */
    public String[] makeStartProviderCmdLine(String iMQHome,
        String optArgs[], String serverName, boolean argsOnly)
        throws IOException, JMSException;

    /**
     * Start the provider.
     *
     * @param iMQHome directory of MQ executables.
     * @param optArgs Array of optional broker command line arguments.
     * @param serverName Instance name of the server.
     * @exception IOException thrown if the server startup fails.
     */
    void startProvider(String iMQHome, String optArgs[],
        String serverName) throws IOException, JMSException;

    /**
     * Ping the provider.
     * @exception JMSException thrown if ping fails.
     */
    void pingProvider() throws JMSException;

    /**
     * Get the provider instance name
     * @exception JMSException thrown if the get fails.
     */
    String getProviderInstanceName() throws JMSException;

    /**
     * Get the provider VARHOME directory
     * @exception JMSException thrown if the get fails.
     */
    String getProviderVarHome() throws JMSException;

    /**
     * Shutdown the provider.
     * @exception JMSException thrown if the shutdown fails.
     */
    void shutdownProvider() throws JMSException;

    /**
     * Restart the provider.
     * @exception JMSException thrown if the restart fails.
     */
    void restartProvider() throws JMSException;

    /**
     * Return the provider host name
     * @exception JMSException
     */
    String getProviderHostName() throws JMSException;

    /**
     * Return the provider host port number
     * @exception JMSException
     */
    String getProviderHostPort() throws JMSException;

    /**
     * Delete provider instance.
     *
     * @param mqBinDir Location of MQ's bin directory, which contains
     * the executable used to perform the deletion.
     * @param optArgs Optional broker command line arguments.
     * @param serverName instance name of MQ broker to delete
     *
     * @exception JMSException thrown if the delete fails.
     */
    void deleteProviderInstance(String mqBinDir, String optArgs,
			String serverName) 
			throws IOException, JMSException;

    /**
     * Delete provider instance.
     *
     * @param mqBinDir Location of MQ's bin directory, which contains
     * the executable used to perform the deletion.
     * @param optArgs Array of optional broker command line arguments.
     * @param serverName instance name of MQ broker to delete
     *
     * @exception JMSException thrown if the delete fails.
     */
    void deleteProviderInstance(String mqBinDir, String optArgs[],
			String serverName) 
			throws IOException, JMSException;

}
