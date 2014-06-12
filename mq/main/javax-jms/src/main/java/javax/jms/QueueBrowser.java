/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Enumeration;

/** A client uses a {@code QueueBrowser} object to look at messages on a 
  * queue without removing them.
  *
  * <P>The {@code getEnumeration} method returns a 
  * {@code java.util.Enumeration} that is used to scan 
  * the queue's messages. It may be an enumeration of the entire content of a 
  * queue, or it may contain only the messages matching a message selector.
  *
  * <P>Messages may be arriving and expiring while the scan is done. The JMS API
  * does 
  * not require the content of an enumeration to be a static snapshot of queue 
  * content. Whether these changes are visible or not depends on the JMS 
  * provider.
  * <p>
  * A message must not be returned by a {@code QueueBrowser} before its delivery time has been reached.
  *
  *<P>A {@code QueueBrowser} can be created from either a 
  * {@code Session} or a {@code  QueueSession}. 
  * 
  * @see         javax.jms.Session#createBrowser
  * @see         javax.jms.QueueSession#createBrowser
  * @see         javax.jms.QueueReceiver
  *
  * @version JMS 2.0
  * @since JMS 1.0
  *
  */

public interface QueueBrowser extends AutoCloseable {

    /** Gets the queue associated with this queue browser.
      * 
      * @return the queue
      *  
      * @exception JMSException if the JMS provider fails to get the
      *                         queue associated with this browser
      *                         due to some internal error.
      */

    Queue 
    getQueue() throws JMSException;


    /** Gets this queue browser's message selector expression.
      *  
      * @return this queue browser's message selector, or null if no
      *         message selector exists for the message consumer (that is, if 
      *         the message selector was not set or was set to null or the 
      *         empty string)
      *
      * @exception JMSException if the JMS provider fails to get the
      *                         message selector for this browser
      *                         due to some internal error.
      */

    String
    getMessageSelector() throws JMSException;


    /** Gets an enumeration for browsing the current queue messages in the
      * order they would be received.
      *
      * @return an enumeration for browsing the messages
      *  
      * @exception JMSException if the JMS provider fails to get the
      *                         enumeration for this browser
      *                         due to some internal error.
      */

    Enumeration 
    getEnumeration() throws JMSException;


    /** Closes the {@code QueueBrowser}.
      *
      * <P>Since a provider may allocate some resources on behalf of a 
      * QueueBrowser outside the Java virtual machine, clients should close them
      * when they 
      * are not needed. Relying on garbage collection to eventually reclaim 
      * these resources may not be timely enough.
      *
      * @exception JMSException if the JMS provider fails to close this
      *                         browser due to some internal error.
      */

    void 
    close() throws JMSException;
}
