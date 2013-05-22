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
 * @(#)TopicPublisherImpl.java	1.21 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;

//import com.sun.messaging.AdministeredObject;

/** A client uses a TopicPublisher for publishing messages on a topic.
  * TopicPublisher is the Pub/Sub variant of a JMS message producer.
  *
  * <P>Normally the Topic is specified when a TopicPublisher is created and
  * in this case, attempting to use the methods for an unidentified
  * TopicPublisher will throws an UnsupportedOperationException.
  *
  * <P>In the case that the TopicPublisher with an unidentified Topic is
  * created, the methods that assume the Topic has been identified throw
  * an UnsupportedOperationException.
  *
  * @see TopicSession#createPublisher(Topic)
  */

public class TopicPublisherImpl extends MessageProducerImpl implements TopicPublisher {
    
	Topic topic = null;
    
	//bug 6360068 - Class defines field that obscures a superclass field
    //SessionImpl session = null;

    public TopicPublisherImpl(SessionImpl session, Topic topic) throws JMSException{
        super(session, topic);
        //this.session = session;
        this.topic = topic;
    }

    /** Get the topic associated with this publisher.
      *
      * @return this publisher's topic
      *
      * @exception JMSException if JMS fails to get topic for
      *                         this topic publisher
      *                         due to some internal error.
      */

    public Topic
    getTopic() throws JMSException {
        checkState();
        return topic;
    }

    /** Publish a Message to the topic
      * Use the topics default delivery mode, timeToLive and priority.
      *
      * @param message the message to publish
      *
      * @exception JMSException if JMS fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with a Topic Publisher with
      *                         an invalid topic.
      */
    public void
    publish(Message message) throws JMSException {

        super.send(message);

    }

    /** Publish a Message to the topic specifying delivery mode, priority
      * and time to live to the topic.
      *
      * @param message the message to publish
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds).
      *
      * @exception JMSException if JMS fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with a Topic Publisher with
      *                         an invalid topic.
      */
    public void
    publish(Message message,
        int deliveryMode,
        int priority,
        long timeToLive) throws JMSException {

            super.send(message, deliveryMode, priority, timeToLive);
    }

    /** Publish a Message to a topic for an unidentified message producer.
      * Use the topics default delivery mode, timeToLive and priority.
      *
      * <P>Typically a JMS message producer is assigned a topic at creation
      * time; however, JMS also supports unidentified message producers
      * which require that the topic be supplied on every message publish.
      *
      * @param topic the topic to publish this message to
      * @param message the message to send
      *
      * @exception JMSException if JMS fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid topic.
      */

    public void
    publish(Topic topic, Message message) throws JMSException {

        super.send(topic, message);

    }

    /** Publish a Message to a topic for an unidentified message producer,
      * specifying delivery mode, priority and time to live.
      *
      * <P>Typically a JMS message producer is assigned a topic at creation
      * time; however, JMS also supports unidentified message producers
      * which require that the topic be supplied on every message publish.
      *
      * @param topic the topic to publish this message to
      * @param message the message to send
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds).
      *
      * @exception JMSException if JMS fails to publish the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid topic.
      */

    public void
    publish(Topic topic,
            Message message,
            int deliveryMode,
            int priority,
            long timeToLive) throws JMSException {

        super.send(topic, message, deliveryMode, priority, timeToLive);

    }

}
