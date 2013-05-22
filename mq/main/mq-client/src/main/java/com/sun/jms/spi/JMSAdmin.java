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
 * @(#)JMSAdmin.java	1.5 06/28/07
 */ 

package com.sun.jms.spi;
import javax.jms.*;
import java.util.Set;

/**
 * Interface definition to provide administrative support of the JMS Server. 
 */

public interface JMSAdmin extends JMSRIConstants {

    
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
    Destination createProviderDestination(String destinationName, 
                                          int destinationType, 
					  java.util.Map properties)
        throws JMSException;
    
    /**
     * Get all Destinations.
     * @param destinationType   QUEUE or TOPIC or ALL
     * @return Set of Destinations of destination type.
     * @exception JMSException thrown if set could not be obtained.
     */
    Set getDestinations(int destinationType)
        throws JMSException;

    /**
     * Delete a physical destination within the JMS Provider.
     * @param destinationName
     * @exception JMSException thrown if Queue could not be deleted.
     */
    void deleteProviderDestination(String destinationName)
        throws JMSException;

    /**
     * Get all messages contained within a specified Queue.
     * @param queue
     * @param messageSelector
     * @return QueueBrowser for queue
     * @exception JMSException thrown if browser could not be obtained.
     */
    QueueBrowser createQueueBrowser(Queue queue, String messageSelector)
        throws JMSException;

    /**
     * --- ConnectionFactory
     */

    /**
     * Create a ConnectionFactory.
     * ConnectionFactory is not automatically bound into JNDI namespace.
     *
     * @param connectionType        Either QUEUE or TOPIC. 
     * @param connectionProperties Connection specific properties.
     * @return New created ConnectionFactory.
     * @exception JMSException thrown if connectionFactory could not be created.
     */
    ConnectionFactory createConnectionFactory(int connectionType,
                                              java.util.Map properties)
        throws JMSException;

    /**
     * Create a XAConnectionFactory. 
     * ConnectionFactory is not automatically bound into JNDI namespace.
     *
     * @param connectionType        Either QUEUE or TOPIC.
     * @param connectionProperties Connection specific properties.
     * @return New created XAConnectionFactory. (Object being returned is
     * not in javax.jms standard interface. It is a JMS RI specific interface
     * of XAConnectionFactory. Considering deprecation of 
     * javax.jms.XAConnectionFactory.)
     * @exception JMSException thrown if connectionFactory could not be created.
     */
    Object createXAConnectionFactory(int connectionType,
	      			     java.util.Map properties)
        throws JMSException;

    /**
     * Validate selector string to be used with a JMS Message Consumer.
     * 
     * @param selector   Selector string to validate
     * @exception InvalidSelectorException if the selector is invalid.
     */
    void validateJMSSelector(String selector) throws JMSException;


    /**
     * List all durable subscriptions 
     *
     * @return a set of DurableSubscription objects
     * @exception JMSException thrown if there was an internal provider failure
     *
     * @see com.sun.jms.spi.DurableSubscription
     */
    Set getDurableSubscriptions()
	throws JMSException;
    

    /**
     * List all durable subscriptions consuming from the specified Topic.
     *
     * @param topic the topic to look for durable subscriptions on
     * @return a set of DurableSubscription objects
     * @exception InvalidDestinationException thrown if the specified topic was invalid
     * @exception JMSException thrown if there was an internal provider failure
     *
     * @see com.sun.jms.spi.DurableSubscription
     */
    Set getDurableSubscriptions(Topic topic)
	throws InvalidDestinationException, JMSException;


    /**
     * List all durable subscriptions associated with the specified TopicConnectionFactory
     *
     * @param connFactory the TopicConnectionFactory
     * @return a set of DurableSubscription objects
     * @exception JMSException thrown if there was an internal provider failure
     *
     * @see com.sun.jms.spi.DurableSubscription
     */
    Set getDurableSubscriptions(TopicConnectionFactory connFactory)
	throws JMSException;


    /**
     * Create a durable subscription with the specified parameters in the
     * JMS Provider on the given topic.  Calling this SPI will cause the JMS
     * Provider to seutp a durable subscription on the given topic and then
     * return a descriptor object that can then be used in future administration
     * operations.
     *
     * @param subscriptionName is the logical name of the subscription exactly as it would 
     * be supplied to TopicSession.createDurableSubscriber( Topic, String, String, boolean )
     * @param connFactory is the TopicConnectionFactory that will uniquely identify
     * this subscription when paired with the subscriptionName.  If this parameter is null
     * or does not specify a clientId, it is assumed that the clientId will be specified
     * in the property map.
     * @param topic is the Topic from which this subscription will consume
     * @param messageSelector an optional message selector
     * @param properties optional provider-specific durable subscription properties
     * @return a DurableSubscription descriptor object
     * @exception InvalidDestinationException thrown if the specified topic was invalid
     * @exception InvalidSelectorException thrown if the specified selector was invalid
     * @exception JMSException thrown if there was an internal provider failure or there
     * was an insufficient amount of information specified to uniquely identify the
     * subscription
     * @see javax.jms.TopicSession#createDurableSubscriber( Topic, String, String, boolean )
     */
    DurableSubscription createDurableSubscription(String subscriptionName, 
						  TopicConnectionFactory connFactory,
						  Topic topic,
						  String messageSelector,
						  java.util.Map properties)
	throws InvalidDestinationException, InvalidSelectorException, JMSException;


    /**
     * Delete the specified durable subscription.
     *
     * @param subscriptionName is the logical name of the subscription exactly as it would 
     * be supplied to TopicSession.createDurableSubscriber( Topic, String, String, boolean )
     * @param connFactory is the TopicConnectionFactory that will uniquely identify
     * this subscription when paired with the subscriptionName.  
     * @param topic is the Topic from which the subscriber is consuming from
     * @exception InvalidDestinationException thrown if the specified topic was invalid     
     * @exception JMSException if there are any errors encountered during the delete
     */
    void deleteDurableSubscription(String subscriptionName,
				   TopicConnectionFactory connFactory,
				   Topic topic)
	throws InvalidDestinationException, JMSException;


    /**
     * Delete the specified durable subscription.
     *
     * @param subscription is the DurableSubscription descriptor that identifies
     * the durable subscription that should be deleted.
     * @exception JMSException if there are any errors encountered during the delete
     */
    void deleteDurableSubscription(DurableSubscription subscription) 
	throws javax.jms.JMSException;
}


