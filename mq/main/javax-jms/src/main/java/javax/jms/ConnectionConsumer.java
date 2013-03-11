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

/** For application servers, {@code Connection} objects provide a special 
  * facility 
  * for creating a {@code ConnectionConsumer} (optional). The messages it 
  * is to consume are 
  * specified by a {@code Destination} and a message selector. In addition,
  * a {@code ConnectionConsumer} must be given a 
  * {@code ServerSessionPool} to use for 
  * processing its messages.
  *
  * <P>Normally, when traffic is light, a {@code ConnectionConsumer} gets a
  * {@code ServerSession} from its pool, loads it with a single message, and
  * starts it. As traffic picks up, messages can back up. If this happens, 
  * a {@code ConnectionConsumer} can load each {@code ServerSession}
  * with more than one 
  * message. This reduces the thread context switches and minimizes resource 
  * use at the expense of some serialization of message processing.
  *
  * @see javax.jms.Connection#createConnectionConsumer
  * @see javax.jms.Connection#createDurableConnectionConsumer
  * @see javax.jms.QueueConnection#createConnectionConsumer
  * @see javax.jms.TopicConnection#createConnectionConsumer
  * @see javax.jms.TopicConnection#createDurableConnectionConsumer
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  *
  */

public interface ConnectionConsumer {

    /** Gets the server session pool associated with this connection consumer.
      *  
      * @return the server session pool used by this connection consumer
      *  
      * @exception JMSException if the JMS provider fails to get the server 
      *                         session pool associated with this consumer due
      *                         to some internal error.
      */

    ServerSessionPool 
    getServerSessionPool() throws JMSException; 

 
    /** Closes the connection consumer.
      *
      * <P>Since a provider may allocate some resources on behalf of a 
      * connection consumer outside the Java virtual machine, clients should 
      * close these resources when
      * they are not needed. Relying on garbage collection to eventually 
      * reclaim these resources may not be timely enough.
      *  
      * @exception JMSException if the JMS provider fails to release resources 
      *                         on behalf of the connection consumer or fails
      *                         to close the connection consumer.
      */

    void 
    close() throws JMSException; 
}
