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
 * @(#)UnifiedSessionImpl.java	1.14 08/02/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;

import java.util.Enumeration;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jms.ra.api.JMSRAManagedConnection;

/** A UnifiedSession provides methods for creating QueueReceiver's,
  * QueueSender's, QueueBrowser's and TemporaryQueues.
  *
  * <P>If there are messages that have been received but not acknowledged
  * when a QueueSession terminates, these messages will be retained and
  * redelivered when a consumer next accesses the queue.
  *
  * @see         javax.jms.Session
  * @see         javax.jms.QueueConnection#createQueueSession(boolean, int)
  * @see         javax.jms.XAQueueSession#getQueueSession()
  * @see         com.sun.messaging.jms.Session
  */

public class UnifiedSessionImpl extends SessionImpl implements com.sun.messaging.jms.Session {
    //Now we support NO_ACKNOWLEDGE mode.
    public UnifiedSessionImpl
            (ConnectionImpl connection, boolean transacted, int ackMode) throws JMSException {

        super (connection, transacted, ackMode);
    }

    public UnifiedSessionImpl
            (ConnectionImpl connection, boolean transacted,
             int ackMode, JMSRAManagedConnection mc) throws JMSException {

        super (connection, transacted, ackMode, mc);
    }

    //Now we support NO_ACKNOWLEDGE mode.
    public UnifiedSessionImpl
            (ConnectionImpl connection, int ackMode)
            throws JMSException {

        super (connection, ackMode);
    }

    /** Create a queue identity given a Queue name.
      *
      * <P>This facility is provided for the rare cases where clients need to
      * dynamically manipulate queue identity. This allows the creation of a
      * queue identity with a provider specific name. Clients that depend
      * on this ability are not portable.
      *
      * <P>Note that this method is not for creating the physical topic.
      * The physical creation of topics is an administration task and not
      * to be initiated by the JMS interface. The one exception is the
      * creation of temporary topics is done using the createTemporaryTopic
      * method.
      *
      * @param queueName the name of this queue
      *
      * @return a Queue with the given name.
      *
      * @exception JMSException if a session fails to create a queue
      *                         due to some JMS error.
      */

    public Queue createQueue(String queueName) throws JMSException {

        checkSessionState();

        return new com.sun.messaging.BasicQueue(queueName);
    }


    /** Create a QueueReceiver to receive messages from the specified queue.
      *
      * @param queue the queue to access
      *
      * @exception JMSException if a session fails to create a receiver
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Queue specified.
      */

    public QueueReceiver
    createReceiver(Queue queue) throws JMSException {

        checkSessionState();

        return new QueueReceiverImpl (this, queue);
    }


    /** Create a QueueReceiver to receive messages from the specified queue.
      *
      * @param queue the queue to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered
      *
      * @exception JMSException if a session fails to create a receiver
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Queue specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      */

    public QueueReceiver
    createReceiver(Queue queue, String messageSelector) throws JMSException {

        checkSessionState();

        return new QueueReceiverImpl (this, queue, messageSelector);
    }

    /** Create a QueueSender to send messages to the specified queue.
      *
      * @param queue the queue to access, or null if this is an unidentifed
      * producer.
      *
      * @exception JMSException if a session fails to create a sender
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Queue specified.
      */

    public QueueSender
    createSender(Queue queue) throws JMSException {

        checkSessionState();

        return new QueueSenderImpl (this, queue);
    }

    /** Create a QueueBrowser to peek at the messages on the specified queue.
      *
      * @param queue the queue to access
      *
      * @exception JMSException if a session fails to create a browser
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Queue specified.
      */

    public QueueBrowser
    createBrowser(Queue queue) throws JMSException {

        checkSessionState();

        return new QueueBrowserImpl(this, queue);
    }


    /** Create a QueueBrowser to peek at the messages on the specified queue.
      *
      * @param queue the queue to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered
      *
      * @exception JMSException if a session fails to create a browser
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Queue specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */

    public QueueBrowser
    createBrowser(Queue queue, String messageSelector) throws JMSException {

        checkSessionState();

        return new QueueBrowserImpl(this, queue, messageSelector);
    }


    /** Create a temporary queue. It's lifetime will be that of the
      * QueueConnection unless deleted earlier.
      *
      * @return a temporary queue identity
      *
      * @exception JMSException if a session fails to create a Temporary Queue
      *                         due to some JMS error.
      */

    public TemporaryQueue
    createTemporaryQueue() throws JMSException {

        checkSessionState();
        return new TemporaryQueueImpl(connection);
    }

    /** Create a topic identity given a Topic name.
      *
      * <P>This facility is provided for the rare cases where clients need to
      * dynamically manipulate topic identity. This allows the creation of a
      * topic identity with a provider specific name. Clients that depend
      * on this ability are not portable.
      *
      * <P>Note that this method is not for creating the physical topic.
      * The physical creation of topics is an administration task and not
      * to be initiated by the JMS interface. The one exception is the
      * creation of temporary topics is done using the createTemporaryTopic
      * method.
      *
      * @param topicName the name of this topic
      *
      * @return a Topic with the given name.
      *
      * @exception JMSException if a session fails to create a topic
      *                         due to some JMS error.
      */

    public Topic createTopic(String topicName) throws JMSException {

        checkSessionState();

        return new com.sun.messaging.BasicTopic(topicName);
    }


    /** Create a non-durable Subscriber to the specified topic.
      *
      * <P>A client uses a TopicSubscriber for receiving messages that have
      * been published to a topic.
      *
      * <P>Regular TopicSubscriber's are not durable. They only receive
      * messages that are published while they are active.
      *
      * <P>In some cases, a connection may both publish and subscribe to a
      * topic. The subscriber NoLocal attribute allows a subscriber to
      * inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is false.
      *
      * @param topic the topic to subscribe to
      *
      * @exception JMSException if a session fails to create a subscriber
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Topic specified.
      */
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {

        checkSessionState();

        return createSubscriber (topic, null, false);
    }


    /** Create a non-durable Subscriber to the specified topic.
      *
      * <P>A client uses a TopicSubscriber for receiving messages that have
      * been published to a topic.
      *
      * <P>Regular TopicSubscriber's are not durable. They only receive
      * messages that are published while they are active.
      *
      * <P>Messages filtered out by a subscriber's message selector will
      * never be delivered to the subscriber. From the subscriber's
      * perspective they simply don't exist.
      *
      * <P>In some cases, a connection may both publish and subscribe to a
      * topic. The subscriber NoLocal attribute allows a subscriber to
      * inhibit the delivery of messages published by its own connection.
      *
      * @param topic the topic to subscribe to
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. This value may be null.
      * @param noLocal if set, inhibits the delivery of messages published
      * by its own connection.
      *
      * @exception JMSException if a session fails to create a subscriber
      *                         due to some JMS error or invalid selector.
      * @exception InvalidDestinationException if invalid Topic specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      */

    public TopicSubscriber
    createSubscriber(Topic topic,
             String messageSelector,
             boolean noLocal) throws JMSException {

        checkSessionState();

        return new TopicSubscriberImpl (this, topic, messageSelector, noLocal);
    }

   /** Creates a durable subscription with the specified name on the
     * specified topic, and creates a <code>TopicSubscriber</code>
     * on that durable subscription.
     * <P>
     * If the durable subscription already exists then this method
     * creates a <code>TopicSubscriber</code> on the existing durable
     * subscription.
     * <p>
     * A durable subscription is used by a client which needs to receive
     * all the messages published on a topic, including the ones published
     * when there is no <code>TopicSubscriber</code> associated with it.
     * The JMS provider retains a record of this durable subscription
     * and ensures that all messages from the topic's publishers are retained
     * until they are delivered to, and acknowledged by,
     * a <code>TopicSubscriber</code> on this durable subscription
     * or until they have expired.
     * <p>
     * A durable subscription will continue to accumulate messages
     * until it is deleted using the <code>unsubscribe</code> method.
     * <p>
     * A durable subscription which has a <code>TopicSubscriber</code>
     * associated with it is described as being active.
     * A durable subscription which has no <code>TopicSubscriber</code>
     * associated with it is described as being inactive.
     * <p>
     * Only one session at a time can have a
     * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
     * <p>
     * A durable subscription is identified by a name specified by the client
     * and by the client identifier if set. If the client identifier was set
     * when the durable subscription was first created then a client which
     * subsequently wishes to create a <code>TopicSunscriber</code>
     * on that durable subscription must use the same client identifier.
     * <p>
     * A client can change an existing durable subscription by calling
     * <code>createDurableSubscriber</code>
     * with the same name and client identifier (if used),
     * and a new topic and/or message selector.
     * Changing a durable subscriber is equivalent to
     * unsubscribing (deleting) the old one and creating a new one.
     *
     * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
     * @param name the name used to identify this subscription
     *
     * @exception JMSException if the session fails to create a subscriber
     * due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     *
     * @since 2.0
     */

    public TopicSubscriber
    createDurableSubscriber(Topic topic, String name) throws JMSException {

        return createDurableSubscriber(topic, name, null, false);
    }

   /** Creates a durable subscription with the specified name on the
     * specified topic, and creates a <code>TopicSubscriber</code>
     * on that durable subscription, specifying a message
     * selector and whether messages published by its
     * own connection should be delivered to it.
     * <P>
     * If the durable subscription already exists then this method
     * creates a <code>TopicSubscriber</code> on the existing durable
     * subscription.
     * <p>
     * A durable subscription is used by a client which needs to receive
     * all the messages published on a topic, including the ones published
     * when there is no <code>TopicSubscriber</code> associated with it.
     * The JMS provider retains a record of this durable subscription
     * and ensures that all messages from the topic's publishers are retained
     * until they are delivered to, and acknowledged by,
     * a <code>TopicSubscriber</code> on this durable subscription
     * or until they have expired.
     * <p>
     * A durable subscription will continue to accumulate messages
     * until it is deleted using the <code>unsubscribe</code> method.
     * <p>
     * A durable subscription which has a <code>TopicSubscriber</code>
     * associated with it is described as being active.
     * A durable subscription which has no <code>TopicSubscriber</code>
     * associated with it is described as being inactive.
     * <p>
     * Only one session at a time can have a
     * <CODE>TopicSubscriber</CODE> for a particular durable subscription.
     * <p>
     * A durable subscription is identified by a name specified by the client
     * and by the client identifier if set. If the client identifier was set
     * when the durable subscription was first created then a client which
     * subsequently wishes to create a <code>TopicSunscriber</code>
     * on that durable subscription must use the same client identifier.
     * <p>
     * A client can change an existing durable subscription by calling
     * <code>createDurableSubscriber</code>
     * with the same name and client identifier (if used),
     * and a new topic and/or message selector.
     * Changing a durable subscriber is equivalent to
     * unsubscribing (deleting) the old one and creating a new one.
     *
     * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
     * @param name the name used to identify this subscription
     * @param messageSelector only messages with properties matching the
     * message selector expression are delivered. A value of null or
     * an empty string indicates that there is no message selector
     * for the message consumer.
     * @param noLocal if set, inhibits the delivery of messages published
     * by its own connection
     *
     * @exception JMSException if the session fails to create a subscriber
     * due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     * @exception InvalidSelectorException if the message selector is invalid.
     *
     * @since 2.0
     */
    public TopicSubscriber
    createDurableSubscriber(Topic topic, String name,
                            String messageSelector,
                            boolean noLocal) 
                            throws JMSException {

        checkSessionState();
        checkTemporaryDestination(topic);
        checkClientIDForNoLocal(noLocal, name, 
                     true /*dura*/, false /*shared*/);
        checkClientIDWithBroker(true /*require clientid*/);
        return new TopicSubscriberImpl (this, topic, name,
                       messageSelector, noLocal, false /*shared*/);
    }

    /** Create a Publisher for the specified topic.
      *
      * <P>A client uses a TopicPublisher for publishing messages on a topic.
      * Each time a client creates a TopicPublisher on a topic, it defines a
      * new sequence of messages that have no ordering relationship with the
      * messages it has previously sent.
      *
      * @param topic the topic to publish to, or null if this is an
      * unidentifed producer.
      *
      * @exception JMSException if a session fails to create a publisher
      *                         due to some JMS error.
      * @exception InvalidDestinationException if invalid Topic specified.
     */

    public TopicPublisher
    createPublisher(Topic topic) throws JMSException {

        checkSessionState();

        return new TopicPublisherImpl ( this, topic );
    }


    /** Create a temporary topic. It's lifetime will be that of the
      * TopicConnection unless deleted earlier.
      *
      * @return a temporary topic identity
      *
      * @exception JMSException if a session fails to create a temporary
      *                         topic due to some JMS error.
      */

    public TemporaryTopic
    createTemporaryTopic() throws JMSException {

        checkSessionState();
        return new TemporaryTopicImpl(connection);
    }


    /** Unsubscribes a durable subscription that has been created by a client.
      *
      * <P>This method deletes the state being maintained on behalf of the
      * subscriber by its provider.
      * <p>
      * A durable subscription is identified by a name specified by the client
      * and by the client identifier if set. If the client identifier was set
      * when the durable subscription was created then a client which
      * subsequently wishes to use this method to
      * delete a durable subscription must use the same client identifier.
      *
      * <P>It is erroneous for a client to delete a durable subscription
      * while there is an active <CODE>MessageConsumer</CODE>
      * or <CODE>TopicSubscriber</CODE> for the
      * subscription, or while a consumed message is part of a pending
      * transaction or has not been acknowledged in the session.
      *
      * @param name the name used to identify this subscription
      *
      * @exception JMSException if the session fails to unsubscribe to the
      * durable subscription due to some internal error.
      * @exception InvalidDestinationException if an invalid subscription name
      * is specified.
      *
      * @since 2.0
      */
    public void
    unsubscribe(String name) throws JMSException {
        MessageConsumerImpl consumer = null;
        boolean deregistered = false;

        checkSessionState();

        //check if the consumer is active
        Enumeration enum2 = consumers.elements();
        while ( enum2.hasMoreElements() ) {
            consumer = (MessageConsumerImpl) enum2.nextElement();
            if ( consumer.getDurable() ) {
                if ( consumer.getDurableName().equals(name) ) {
                    String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_DURABLE_INUSE,
                                         consumer.getDurableName());

                    throw new JMSException(errorString, AdministeredObject.cr.X_DURABLE_INUSE);
                }
            }
        }

        //unsubscribe
        checkClientIDWithBroker(false /*require clientid*/);
        connection.unsubscribe ( name );
        deregistered = true;
    }

    /** Creates a MessageProducer to send messages to the specified destination
      *
      * <P>A client uses a <CODE>MessageProducer</CODE> object to send
      * messages to a destination. Since <CODE>Queue</CODE> and <CODE>Topic</CODE>
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a MessageProducer.
      *
      * @param destination the <CODE>Destination</CODE> to send to,
      * or null if this is an producer which does not have a specified
      * destination.
      *
      * @exception JMSException if the session fails to create a MessageProducer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      * is specified.
      *
      * @since 1.1
      *
     */

    public MessageProducer
    createProducer(Destination destination) throws JMSException {
        if (destination == null) {
            return new MessageProducerImpl(this, destination);
        }
        if (destination instanceof Queue) {
            return createSender((Queue)destination);
        } else {
            if (destination instanceof Topic) {
                return createPublisher((Topic)destination);
            } else {
                String errorString = AdministeredObject.cr.getKString(
                                        AdministeredObject.cr.X_INVALID_DESTINATION_CLASS,
                                        destination.getClass().getName());
                throw new JMSException(errorString, AdministeredObject.cr.X_INVALID_DESTINATION_CLASS);
            }
        }
    }

      /** Creates a <CODE>MessageConsumer</CODE> object to receive messages from the
      * specified destination. Since <CODE>Queue</CODE> and <CODE>Topic</CODE>
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a MessageConsumer.
      *
      * @param destination the <CODE>Destination</CODE> to access.
      *
      * @exception JMSException if the session fails to create a consumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified.
      *
      * @since 1.1
      */

    public MessageConsumer
    createConsumer(Destination destination) throws JMSException {
        return createConsumer(destination, null, false);
    }

      /** Creates a message consumer to the specified destination, using a
      * message selector. Since <CODE>Queue</CODE> and <CODE>Topic</CODE>
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a MessageConsumer.
      *
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive
      * messages that have been semt to a Destination.
      *
      *
      * @param destination the <CODE>Destination</CODE> to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector
      * for the message consumer.
      *
      *
      * @exception JMSException if the session fails to create a MessageConsumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
       * is specified.

      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1
      */
    public MessageConsumer
    createConsumer(Destination destination, java.lang.String messageSelector)
    throws JMSException {
        return createConsumer(destination, messageSelector, false);
    }


     /** Creates a message consumer to the specified destination, using a
      * message selector. This method can specify whether messages published by
      * its own connection should be delivered to it, if the destination is a
      * topic.
      *
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive
      * messages that have been published to a destination. Since
      * <CODE>Queue</CODE> and <CODE>Topic</CODE>
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a MessageConsumer.
      *
      *
      * <P>In some cases, a connection may both publish and subscribe to a
      * topic. The consumer <CODE>NoLocal</CODE> attribute allows a consumer
      * to inhibit the delivery of messages published by its own connection.
      * The default value for this attribute is FALSE. The noLocal value
      *  must be supported by topics.
      *
      * @param destination the <CODE>Destination</CODE> to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector
      * for the message consumer.
      * @param NoLocal  - if True, and the destination is a topic,
      *                   inhibits the delivery of messages published
      *                   by its own connection.  The behavior for NoLocal is
      *                   not specified if the destination is a queue.
      *
      * @exception JMSException if the session fails to create a MessageConsumer
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      * is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1
      *
      */
    public MessageConsumer
    createConsumer(Destination destination, java.lang.String messageSelector,
    boolean NoLocal)   throws JMSException {
        if (destination == null) {
            String errorString = AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_DESTINATION_NOTFOUND, "null" );
            throw new InvalidDestinationException(errorString,
                    AdministeredObject.cr.X_DESTINATION_NOTFOUND);
        }
        if (destination instanceof Queue) {
            return createReceiver((Queue)destination, messageSelector);
        } else {
            if (destination instanceof Topic) {
                return createSubscriber((Topic)destination, messageSelector, NoLocal);
            } else {
                String errorString = AdministeredObject.cr.getKString(
                                        AdministeredObject.cr.X_INVALID_DESTINATION_CLASS,
                                        destination.getClass().getName());
                throw new JMSException(errorString, AdministeredObject.cr.X_INVALID_DESTINATION_CLASS);
            }
        }

    }

    private void checkClientIDWithBroker(boolean clientIDRequired)
    throws JMSException {

        String clientID = connection.getClientID();

	if ( clientIDRequired && clientID == null ) {
            String errorString = AdministeredObject.cr.getKString(
                       AdministeredObject.cr.X_INVALID_CLIENT_ID, "\"\"");
            throw new javax.jms.IllegalStateException (errorString, 
                          AdministeredObject.cr.X_INVALID_CLIENT_ID);
	}

        if (clientID != null) {
            if (connection.getProtocolHandler().isClientIDsent() == false) {
                connection.getProtocolHandler().setClientID( clientID );
            }
        }
    }

    private void checkClientIDForNoLocal(boolean noLocal, 
        String name, boolean durable, boolean shared) 
        throws JMSException {

        if (!noLocal || !(durable || shared)) {
            return;
        }

        String clientID = connection.getClientID();

        if (clientID == null) {
            if (durable && !shared) {
                String errorString = AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_DURA, name);
                throw new javax.jms.IllegalStateException(errorString, 
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_DURA);
            }
            if (durable && shared) {
                String errorString = AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_DURA_SHARE, name);
                throw new javax.jms.IllegalStateException(errorString, 
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_DURA_SHARE);
            }
            if (shared) {
                String errorString = AdministeredObject.cr.getKString(
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_SHARE, name);
                throw new javax.jms.IllegalStateException(errorString, 
                    AdministeredObject.cr.X_NO_CLIENTID_FOR_NOLOCAL_SHARE);
            }
        }
    }

    protected void checkTemporaryDestination(Topic topic) throws JMSException {
        if ((topic == null) || topic instanceof TemporaryTopic) {
            String errorString = AdministeredObject.cr.getKString(
                        AdministeredObject.cr.X_INVALID_DESTINATION_NAME,
                        (topic == null ? "" : topic.getTopicName()));
            throw new InvalidDestinationException(errorString,
                        AdministeredObject.cr.X_INVALID_DESTINATION_NAME);

        }
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic,
        String sharedSubscriptionName) throws JMSException {

        return createSharedConsumer(topic, 
            sharedSubscriptionName, null, false);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic,
        String sharedSubscriptionName, String messageSelector)
        throws JMSException {
        return createSharedConsumer(topic, 
            sharedSubscriptionName, messageSelector, false);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic,
        String sharedSubscriptionName, String messageSelector,
        boolean noLocal) throws JMSException {
        checkSessionState();
        checkTemporaryDestination(topic);
        if (sharedSubscriptionName == null || 
            sharedSubscriptionName.trim().length() == 0) {
            String errorString = AdministeredObject.cr.getKString(
                AdministeredObject.cr.X_INVALID_SHARED_SUBSCRIPTION_NAME, 
                ""+sharedSubscriptionName);
            JMSException jmse = new JMSException(errorString, 
                AdministeredObject.cr.X_INVALID_SHARED_SUBSCRIPTION_NAME);
            ExceptionHandler.throwJMSException(jmse);
        }
        checkClientIDForNoLocal(noLocal, sharedSubscriptionName,
                                false /*dura*/, true /*shared*/);
        checkClientIDWithBroker(false /*require clientid*/);
        return new TopicSubscriberImpl(this, topic, 
            messageSelector, noLocal, sharedSubscriptionName);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name)
    throws JMSException {
        return createDurableSubscriber(topic, name);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name,
        String messageSelector, boolean noLocal) throws JMSException {
        return createDurableSubscriber(topic, name, messageSelector, noLocal);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name)
    throws JMSException {
        return createSharedDurableConsumer(topic, name, null, false);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name,
         String messageSelector, boolean noLocal)
         throws JMSException {

        checkSessionState();
        checkTemporaryDestination(topic);
        checkClientIDForNoLocal(noLocal, name, true /*dura*/, true /*shared*/);
        checkClientIDWithBroker(false /*require clientid*/);
        return new TopicSubscriberImpl (this, topic, name,
                       messageSelector, noLocal, true /*shared*/);
    }

}
