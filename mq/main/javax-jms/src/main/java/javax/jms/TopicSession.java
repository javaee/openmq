/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package javax.jms;

/** A {@code TopicSession} object provides methods for creating 
  * {@code TopicPublisher}, {@code TopicSubscriber}, and 
  * {@code TemporaryTopic} objects. It also provides a method for 
  * deleting its client's durable subscribers.
  *
  *<P>A {@code TopicSession} is used for creating Pub/Sub specific
  * objects. In general, use the  {@code Session} object, and 
  *  use {@code TopicSession}  only to support
  * existing code. Using the {@code Session} object simplifies the 
  * programming model, and allows transactions to be used across the two 
  * messaging domains.
  * 
  * <P>A {@code TopicSession} cannot be used to create objects specific to the 
  * point-to-point domain. The following methods inherit from 
  * {@code Session}, but must throw an 
  * {@code IllegalStateException} 
  * if used from {@code TopicSession}:
  *<UL>
  *   <LI>{@code createBrowser}
  *   <LI>{@code createQueue}
  *   <LI>{@code createTemporaryQueue}
  *</UL>
  *
  * @version     1.1 - April 9, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author       Kate Stout
  *
  * @see         javax.jms.Session
  * @see	 javax.jms.Connection#createSession(boolean, int)
  * @see	 javax.jms.TopicConnection#createTopicSession(boolean, int)
  * @see         javax.jms.XATopicSession#getTopicSession()
  */

public interface TopicSession extends Session {

    /** Creates a topic identity given a {@code Topic} name.
      *
      * <P>This facility is provided for the rare cases where clients need to
      * dynamically manipulate topic identity. This allows the creation of a
      * topic identity with a provider-specific name. Clients that depend 
      * on this ability are not portable.
      *
      * <P>Note that this method is not for creating the physical topic. 
      * The physical creation of topics is an administrative task and is not
      * to be initiated by the JMS API. The one exception is the
      * creation of temporary topics, which is accomplished with the 
      * {@code createTemporaryTopic} method.
      *  
      * @param topicName the name of this {@code Topic}
      *
      * @return a {@code Topic} with the given name
      *
      * @exception JMSException if the session fails to create a topic
      *                         due to some internal error.
      */

    Topic
    createTopic(String topicName) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic.
      *  
      * <P>A client uses a {@code TopicSubscriber} object to receive 
      * messages that have been published to a topic.
      *
      * <P>Regular {@code TopicSubscriber} objects are not durable. 
      * They receive only messages that are published while they are active.
      *
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The subscriber {@code NoLocal} attribute allows a subscriber
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the {@code Topic} to subscribe to
      *  
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      */ 

    TopicSubscriber
    createSubscriber(Topic topic) throws JMSException;


    /** Creates a nondurable subscriber to the specified topic, using a
      * message selector or specifying whether messages published by its
      * own connection should be delivered to it.
      *
      * <P>A client uses a {@code TopicSubscriber} object to receive 
      * messages that have been published to a topic.
      *  
      * <P>Regular {@code TopicSubscriber} objects are not durable. 
      * They receive only messages that are published while they are active.
      *
      * <P>Messages filtered out by a subscriber's message selector will 
      * never be delivered to the subscriber. From the subscriber's 
      * perspective, they do not exist.
      *
      * <P>In some cases, a connection may both publish and subscribe to a 
      * topic. The subscriber {@code NoLocal} attribute allows a subscriber
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the {@code Topic} to subscribe to
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param noLocal if set, inhibits the delivery of messages published
      * by its own connection
      * 
      * @exception JMSException if the session fails to create a subscriber
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */

    TopicSubscriber 
    createSubscriber(Topic topic, 
		     String messageSelector,
		     boolean noLocal) throws JMSException;


    /** Creates a durable subscription with the specified name on the
     * specified topic, and creates a {@code TopicSubscriber} 
     * on that durable subscription.
     * <p>
     * This method is identical to the corresponding {@code createDurableConsumer}
     * method except that it returns a {@code TopicSubscriber} rather than a
     * {@code MessageConsumer}.    
     * The term "consumer" applies to both {@code TopicSubscriber} and {@code MessageConsumer} objects.
     * <p>
     * If a durable subscription already exists with the same name 
     * and client identifier (if set) and the same topic and message selector 
     * then this method creates a {@code TopicSubscriber} on the existing durable
     * subscription.
     * <p>
     * A durable subscription is used by a client which needs to receive
     * all the messages published on a topic, including the ones published 
     * when there is no consumer associated with it. 
     * The JMS provider retains a record of this durable subscription 
     * and ensures that all messages from the topic's publishers are retained 
     * until they are delivered to, and acknowledged by,
     * a consumer on this durable subscription
     * or until they have expired.
     * <p>
     * A durable subscription will continue to accumulate messages 
     * until it is deleted using the {@code unsubscribe} method. 
     * <p>
     * A consumer may be created on a durable subscription using the
     * {@code createDurableConsumer} methods on {@code JMSContext},
     * or the {@code createDurableConsumer} and {@code createDurableSubscriber}
     * methods on {@code Session} or {@code TopicSession}.
     * A durable subscription which has a consumer
     * associated with it is described as being active. 
     * A durable subscription which has no consumer
     * associated with it is described as being inactive. 
     * <p>
     * A durable subscription may have more than one active consumer
     * (this was not permitted prior to JMS 2.0).
     * Each message from the subscription will be delivered to only one of the consumers on that subscription.
     * <p>
     * A durable subscription is identified by a name specified by the client
     * and by the client identifier if set. If the client identifier was set
     * when the durable subscription was first created then a client which 
     * subsequently wishes to create a consumer 
     * on that durable subscription must use the same client identifier.
     * <p>
     * If there are no active consumers on the durable subscription 
     * (and no consumed messages from that subscription are still part of a pending transaction 
     * or are not yet acknowledged in the session),
     * and this method is used to create a new consumer on that durable subscription,
     * specifying the same name and client identifier (if set)
     * but a different topic or message selector,
     * then the durable subscription will be deleted and a new one created.   
     * However if there is an active consumer on the durable subscription
     * (or a consumed message from that subscription is still part of a pending transaction 
     * or is not yet acknowledged in the session),
     * and an attempt is made to create an additional consumer, 
     * specifying the same name and client identifier (if set)
     * but a different topic or message selector, 
     * then a {@code JMSException} will be thrown.
     *
     * @param topic the non-temporary {@code Topic} to subscribe to
     * @param name the name used to identify this subscription
     *  
     * @exception JMSException if the session fails to create the durable subscription 
     *            and {@code TopicSubscriber} due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     *
     */ 
    TopicSubscriber
    createDurableSubscriber(Topic topic, 
			    String name) throws JMSException;


    /** Creates a durable subscription with the specified name on the
     * specified topic (if one does not already exist), and creates a {@code TopicSubscriber} 
     * on that durable subscription, specifying a message 
     * selector and whether messages published by its
     * own connection should be added to the durable subscription.
     * <p>
     * <p>
     * This method is identical to the corresponding {@code createDurableConsumer}
     * method except that it returns a {@code TopicSubscriber} rather than a
     * {@code MessageConsumer}.  
     * The term "consumer" applies to both {@code TopicSubscriber} and {@code MessageConsumer} objects.
     * <p>
     * If a durable subscription already exists with the same name 
     * and client identifier (if set) and the same topic and message selector 
     * then this method creates a {@code TopicSubscriber} on the existing durable
     * subscription.
     * <p>
     * A durable subscription is used by a client which needs to receive
     * all the messages published on a topic, including the ones published 
     * when there is no consumer associated with it. 
     * The JMS provider retains a record of this durable subscription 
     * and ensures that all messages from the topic's publishers are retained 
     * until they are delivered to, and acknowledged by,
     * a consumer on this durable subscription
     * or until they have expired.
     * <p>
     * A durable subscription will continue to accumulate messages 
     * until it is deleted using the {@code unsubscribe} method. 
     * <p>
     * A consumer may be created on a durable subscription using the
     * {@code createDurableConsumer} methods on {@code JMSContext},
     * or the {@code createDurableConsumer} and {@code createDurableSubscriber}
     * methods on {@code Session} or {@code TopicSession}.
     * A durable subscription which has a consumer
     * associated with it is described as being active. 
     * A durable subscription which has no consumer
     * associated with it is described as being inactive. 
     * <p>
     * A durable subscription may have more than one active consumer
     * (this was not permitted prior to JMS 2.0).
     * Each message from the subscription will be delivered to only one of the consumers on that subscription.
     * <p>
     * A durable subscription is identified by a name specified by the client
     * and by the client identifier if set. If the client identifier was set
     * when the durable subscription was first created then a client which 
     * subsequently wishes to create a consumer
     * on that durable subscription must use the same client identifier.
     * <p>
     * If there are no active consumers on the durable subscription 
     * (and no consumed messages from that subscription are still part of a pending transaction 
     * or are not yet acknowledged in the session),
     * and this method is used to create a new consumer on that durable subscription,
     * specifying the same name and client identifier (if set)
     * but a different topic or message selector,
     * then the durable subscription will be deleted and a new one created.   
     * However if there is an active consumer on the durable subscription
     * (or a consumed message from that subscription is still part of a pending transaction 
     * or is not yet acknowledged in the session),
     * and an attempt is made to create an additional consumer, 
     * specifying the same name and client identifier (if set)
     * but a different topic or message selector, 
     * then a {@code JMSException} will be thrown.
     * 
     * <P>The {@code NoLocal} argument is for use when the session's 
     * connection is also being used to publish messages to the topic. 
     * If {@code NoLocal} is set to true then messages published
     * to the topic by its own connection will not be added to the
     * durable subscription. The default value of this 
     * argument is false. 
     *
     * @param topic the non-temporary {@code Topic} to subscribe to
     * @param name the name used to identify this subscription
     * @param messageSelector only messages with properties matching the
     * message selector expression are added to the durable subscription.  
     * A value of null or
     * an empty string indicates that there is no message selector 
     * for the durable subscription.
     * @param noLocal if true, messages published by its own connection
     * will not be added to the durable subscription.
     *  
     * @exception JMSException if the session fails to create the durable subscription 
     *                         and {@code TopicSubscriber} due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     *
     */ 
    TopicSubscriber
    createDurableSubscriber(Topic topic,
                            String name, 
			    String messageSelector,
			    boolean noLocal) throws JMSException;


    /** Creates a publisher for the specified topic.
      *
      * <P>A client uses a {@code TopicPublisher} object to publish 
      * messages on a topic.
      * Each time a client creates a {@code TopicPublisher} on a topic, it
      * defines a 
      * new sequence of messages that have no ordering relationship with the 
      * messages it has previously sent.
      *
      * @param topic the {@code Topic} to publish to, or null if this is an
      * unidentified producer
      *
      * @exception JMSException if the session fails to create a publisher
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
     */

    TopicPublisher 
    createPublisher(Topic topic) throws JMSException;


    /** Creates a {@code TemporaryTopic} object. Its lifetime will be that 
      * of the {@code TopicConnection} unless it is deleted earlier.
      *
      * @return a temporary topic identity
      *
      * @exception JMSException if the session fails to create a temporary
      *                         topic due to some internal error.
      */
 
    TemporaryTopic
    createTemporaryTopic() throws JMSException;


    /** Unsubscribes a durable subscription that has been created by a client.
      *  
      * <P>This method deletes the state being maintained on behalf of the 
      * subscriber by its provider.
      *
      * <P>It is erroneous for a client to delete a durable subscription
      * while there is an active {@code TopicSubscriber} for the 
      * subscription, or while a consumed message is part of a pending 
      * transaction or has not been acknowledged in the session.
      *
      * @param name the name used to identify this subscription
      *  
      * @exception JMSException if the session fails to unsubscribe to the 
      *                         durable subscription due to some internal error.
      * @exception InvalidDestinationException if an invalid subscription name
      *                                        is specified.
      */

    void
    unsubscribe(String name) throws JMSException;
}
