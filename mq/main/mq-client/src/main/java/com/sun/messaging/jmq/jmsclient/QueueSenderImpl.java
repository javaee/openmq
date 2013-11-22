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
 * @(#)QueueSenderImpl.java	1.16 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;

import com.sun.messaging.AdministeredObject;

/** A client uses a QueueSender to send messages to a queue.
  *
  * <P>Normally the Queue is specified when a QueueSender is created and
  * in this case, attempting to use the methods for an unidentified
  * QueueSender will throws an UnsupportedOperationException.
  *
  * <P>In the case that the QueueSender with an unidentified Queue is
  * created, the methods that assume the Queue has been identified throw
  * an UnsupportedOperationException.
  *
  * @see         javax.jms.MessageProducer
  * @see         javax.jms.QueueSession#createSender(Queue)
  */

public class QueueSenderImpl extends MessageProducerImpl implements QueueSender {

    private Queue queue = null;

    public QueueSenderImpl(SessionImpl session, Queue queue) throws JMSException {
        super(session, queue);
        this.queue = queue;
    }

    /** Get the queue associated with this queue sender.
      *
      * @return the queue
      *
      * @exception JMSException if JMS fails to get queue for
      *                         this queue sender
      *                         due to some internal error.
      */
    public Queue
    getQueue() throws JMSException {
        checkState();
        return queue;
    }

    /** Send a message to the queue. Use the QueueSender's default delivery
      * mode, timeToLive and priority.
      *
      * @param message the message to be sent
      *
      * @exception JMSException if JMS fails to send the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with a Queue sender with
      *                         an invalid queue.
      */
    public void
    send(Message message) throws JMSException {

        super.send(message);

    }

    /** Send a message specifying delivery mode, priority and time to
      * live to the queue.
      *
      * @param message the message to be sent
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds).
      *
      * @exception JMSException if JMS fails to send the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with a Queue sender with
      *                         an invalid queue.
      */
    public void
    send(Message message,
     int deliveryMode,
     int priority,
     long timeToLive) throws JMSException {

        super.send(message, deliveryMode, priority, timeToLive);

     }

    /** Send a message to a queue for an unidentified message producer.
      * Use the QueueSender's default delivery mode, timeToLive and priority.
      *
      * <P>Typically a JMS message producer is assigned a queue at creation
      * time; however, JMS also supports unidentified message producers
      * which require that the queue be supplied on every message send.
      *
      * @param queue the queue that this message should be sent to
      * @param message the message to be sent
      *
      * @exception JMSException if JMS fails to send the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid queue.
      */
    public void
    send(Queue queue, Message message) throws JMSException {

        super.send(queue, message);

    }

    /** Send a message to a queue for an unidentified message producer,
      * specifying delivery mode, priority and time to live.
      *
      * <P>Typically a JMS message producer is assigned a queue at creation
      * time; however, JMS also supports unidentified message producers
      * which require that the queue be supplied on every message send.
      *
      * @param queue the queue that this message should be sent to
      * @param message the message to be sent
      * @param deliveryMode the delivery mode to use
      * @param priority the priority for this message
      * @param timeToLive the message's lifetime (in milliseconds).
      *
      * @exception JMSException if JMS fails to send the message
      *                         due to some internal error.
      * @exception MessageFormatException if invalid message specified
      * @exception InvalidDestinationException if a client uses
      *                         this method with an invalid queue.
      */

    public void
    send(Queue queue,
        Message message,
        int deliveryMode,
        int priority,
        long timeToLive) throws JMSException {

        super.send(queue, message, deliveryMode, priority, timeToLive);

     }

}
