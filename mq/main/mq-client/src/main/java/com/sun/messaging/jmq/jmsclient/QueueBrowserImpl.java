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
 * @(#)QueueBrowserImpl.java	1.12 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Enumeration;
import java.util.Vector;
import javax.jms.*;

import com.sun.messaging.AdministeredObject;

/** A client uses a QueueBrowser to look at messages on a queue without
  * removing them.
  *
  * <P>The browse methods return a java.util.Enumeration that is used to scan
  * the queue's messages. It may be an enumeration of the entire content of a
  * queue or it may only contain the messages matching a message selector.
  *
  * <P>Messages may be arriving and expiring while the scan is done. JMS does
  * not require the content of an enumeration to be a static snapshot of queue
  * content. Whether these changes are visible or not depends on the JMS
  * provider.
  *
  * @see         javax.jms.QueueSession#createBrowser(Queue)
  * @see         javax.jms.QueueSession#createBrowser(Queue, String)
  * @see         javax.jms.QueueReceiver
  */

public class QueueBrowserImpl implements QueueBrowser {

    private SessionImpl session = null;
    private Queue queue = null;
    private String messageSelector = null;
    private Vector consumers = new Vector();

    private boolean isClosed = false;

    public QueueBrowserImpl (SessionImpl session,
                             Queue queue) throws JMSException {
        this(session, queue, null);
    }

    public QueueBrowserImpl (SessionImpl session,
                             Queue queue,
                             String selector) throws JMSException {
        if (queue == null) {
            String errorString =
                AdministeredObject.cr.getKString(AdministeredObject.cr.X_INVALID_DESTINATION_NAME, "null");
            throw new InvalidDestinationException(errorString,
                AdministeredObject.cr.X_INVALID_DESTINATION_NAME);
        }
        this.session = session;
        this.queue = queue;
        this.messageSelector = selector;
        init();
    }

    private void init() throws JMSException {
		//ConnectionConsumer workaround 4715054
		session.checkBrowserCreation();
        session.verifyDestination(queue, messageSelector, true);
    }

    /** Get the queue associated with this queue browser.
      *
      * @return the queue
      *
      * @exception JMSException if JMS fails to get the
      *                         queue associated with this Browser
      *                         due to some JMS error.
      */

    public Queue
    getQueue() throws JMSException {
        checkState();
        return queue;
    }


    /** Get this queue browser's message selector expression.
      *
      * @return this queue browser's message selector
      *
      * @exception JMSException if JMS fails to get the
      *                         message selector for this browser
      *                         due to some JMS error.
      */

    public String
    getMessageSelector() throws JMSException {
        checkState();
        return messageSelector;
    }

    /** Get an enumeration for browsing the current queue messages in the
      * order they would be received.
      *
      * @return an enumeration for browsing the messages
      *
      * @exception JMSException if JMS fails to get the
      *                         enumeration for this browser
      *                         due to some JMS error.
      */

    public Enumeration
    getEnumeration() throws JMSException {

        checkState();

        return (Enumeration)(new BrowserConsumer(this, queue,
                                                 messageSelector));
    }

    protected void addBrowserConsumer(BrowserConsumer consumer) {
        consumers.addElement(consumer);
    }

    protected void removeBrowserConsumer(BrowserConsumer consumer) {
        consumers.removeElement(consumer);
    }

    /** Since a provider may allocate some resources on behalf of a
      * QueueBrowser outside the JVM, clients should close them when they
      * are not needed. Relying on garbage collection to eventually reclaim
      * these resources may not be timely enough.
      *
      * @exception JMSException if a JMS fails to close this
      *                         Browser due to some JMS error.
      */

    public void
    close() throws JMSException {
        BrowserConsumer consumer = null;
        for(int i = consumers.size()-1; i >= 0; i--) {
            consumer = (BrowserConsumer)consumers.elementAt(i);
            consumer.close();
        }

        isClosed = true;
    }

    protected SessionImpl getSession() {
        return session;
    }

    protected void checkState() throws JMSException {

        if ( isClosed ) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_BROWSER_CLOSED);
            throw new javax.jms.IllegalStateException(errorString, AdministeredObject.cr.X_BROWSER_CLOSED);
        }

        session.checkSessionState();

    }
}
