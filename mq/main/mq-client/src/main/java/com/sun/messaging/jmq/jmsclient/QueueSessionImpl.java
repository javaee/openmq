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
 * @(#)QueueSessionImpl.java	1.19 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import com.sun.messaging.AdministeredObject;

/** A QueueSession provides methods for creating QueueReceiver's,
  * QueueSender's, QueueBrowser's and TemporaryQueues.
  *
  * <P>If there are messages that have been received but not acknowledged
  * when a QueueSession terminates, these messages will be retained and
  * redelivered when a consumer next accesses the queue.
  *
  * @see         javax.jms.Session
  * @see         javax.jms.QueueConnection#createQueueSession(boolean, int)
  * @see         javax.jms.XAQueueSession#getQueueSession()
  */

public class QueueSessionImpl extends UnifiedSessionImpl implements QueueSession {

    public QueueSessionImpl
            (ConnectionImpl connection, boolean transacted, int ackMode) throws JMSException {

        super (connection, transacted, ackMode);
    }

    public QueueSessionImpl
            (ConnectionImpl connection, int ackMode)
            throws JMSException {

        super (connection, ackMode);
    }

    /**
    * Throws an IllegalStateException as it is an invalid method for this domain.
    *
    * @param topicName the name of this topic
    *
    * @return a Topic with the given name.
    *
    * @exception IllegalStateException Always, since it is an invalid method for
    *            this domain.
    */
    public Topic createTopic(String topicName) throws JMSException {

        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN, "createTopic");
        throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN);
    }

    /**
    * Throws an IllegalStateException as it is an invalid method for this domain.
    *
    * @return a temporary queue identity
    *
    * @exception IllegalStateException Always, since it is an invalid method for
    *            this domain.
    */
    public TemporaryTopic
    createTemporaryTopic() throws JMSException {

        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN,
                                                              "createTemporaryTopic");
        throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN);
    }

    /**
    * Throws an IllegalStateException as it is an invalid method for this domain.
    *
    * @param topic the non-temporary topic to subscribe to
    * @param name the name used to identify this subscription.
    *
    * @exception IllegalStateException Always, since it is an invalid method for
    *            this domain.
    */
    public TopicSubscriber
    createDurableSubscriber(Topic topic, String name) throws JMSException {

        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN,
                                                              "createDurableSubscriber");
        throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN);
    }

    /**
    * Throws an IllegalStateException as it is an invalid method for this domain.
    *
    * @param topic the non-temporary topic to subscribe to
    * @param name the name used to identify this subscription.
    * @param messageSelector only messages with properties matching the
    * message selector expression are delivered. This value may be null.
    * @param noLocal if set, inhibits the delivery of messages published
    * by its own connection.
    *
    * @exception IllegalStateException Always, since it is an invalid method for
    *            this domain.
    */
    public TopicSubscriber
    createDurableSubscriber(Topic topic,
                            String name,
                String messageSelector,
                boolean noLocal) throws JMSException {

        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN,
                                                              "createDurableSubscriber");
        throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN);
    }

    /**
    * Throws an IllegalStateException as it is an invalid method for this domain.
    *
    * @param name the name used to identify this subscription.
    *
    * @exception IllegalStateException Always, since it is an invalid method for
    *            this domain.
    */
    public void
    unsubscribe(String name) throws JMSException {

        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN,
                                                              "unsubscribe");
        throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_ILLEGAL_METHOD_FOR_DOMAIN);
    }

}
