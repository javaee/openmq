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

/** A {@code TopicConnection} object is an active connection to a 
  * publish/subscribe JMS provider. A client uses a {@code TopicConnection} 
  * object to create one or more {@code TopicSession} objects
  * for producing and consuming messages.
  *
  *<P>A {@code TopicConnection} can be used to create a 
  *{@code TopicSession}, from which
  * specialized topic-related objects can be created. 
  * A more general, and recommended approach is to use the 
  * {@code Connection} object. 
  *
  *
  * <P>The {@code TopicConnection} object
  * should be used to support existing code.
  *
  * @see         javax.jms.Connection
  * @see         javax.jms.ConnectionFactory
  * @see	 javax.jms.TopicConnectionFactory
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  * 
  */

public interface TopicConnection extends Connection {

    /** Creates a {@code TopicSession} object.
      *
      * @param transacted indicates whether the session is transacted
      * @param acknowledgeMode indicates whether the consumer or the
      * client will acknowledge any messages it receives; ignored if the session
      * is transacted. Legal values are {@code Session.AUTO_ACKNOWLEDGE}, 
      * {@code Session.CLIENT_ACKNOWLEDGE}, and 
      * {@code Session.DUPS_OK_ACKNOWLEDGE}. 
      *  
      * @return a newly created topic session
      *  
      * @exception JMSException if the {@code TopicConnection} object fails
      *                         to create a session due to some internal error or
      *                         lack of support for the specific transaction
      *                         and acknowledgement mode.
      *
      * @see Session#AUTO_ACKNOWLEDGE 
      * @see Session#CLIENT_ACKNOWLEDGE 
      * @see Session#DUPS_OK_ACKNOWLEDGE 
      */ 

    TopicSession
    createTopicSession(boolean transacted,
                       int acknowledgeMode) throws JMSException;


    /** Creates a connection consumer for this connection (optional operation).
      * This is an expert facility not used by regular JMS clients.
      *  
      * @param topic the topic to access
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered.  A value of null or
      * an empty string indicates that there is no message selector  
      * for the message consumer.
      * @param sessionPool the server session pool to associate with this 
      * connection consumer
      * @param maxMessages the maximum number of messages that can be
      * assigned to a server session at one time
      *
      * @return the connection consumer
      *
      * @exception JMSException if the {@code TopicConnection} object fails
      *                         to create a connection consumer due to some
      *                         internal error or invalid arguments for 
      *                         {@code sessionPool} and 
      *                         {@code messageSelector}.
      * @exception InvalidDestinationException if an invalid topic is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      * @see javax.jms.ConnectionConsumer
      */ 

    ConnectionConsumer
    createConnectionConsumer(Topic topic,
                             String messageSelector,
                             ServerSessionPool sessionPool,
			     int maxMessages)
			     throws JMSException;


    /** Create a durable connection consumer for this connection (optional operation). 
      * This is an expert facility not used by regular JMS clients.
      *                
      * @param topic the topic to access
      * @param subscriptionName durable subscription name
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered.  A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param sessionPool the server session pool to associate with this 
      * durable connection consumer
      * @param maxMessages the maximum number of messages that can be
      * assigned to a server session at one time
      *
      * @return the durable connection consumer
      *  
      * @exception JMSException if the {@code TopicConnection} object fails
      *                         to create a connection consumer due to some
      *                         internal error or invalid arguments for 
      *                         {@code sessionPool} and 
      *                         {@code messageSelector}.
      * @exception InvalidDestinationException if an invalid topic is specified.
      * @exception InvalidSelectorException if the message selector is invalid.
      * @see javax.jms.ConnectionConsumer
      */

    ConnectionConsumer
    createDurableConnectionConsumer(Topic topic,
				    String subscriptionName,
                                    String messageSelector,
                                    ServerSessionPool sessionPool,
				    int maxMessages)
                             throws JMSException;
}
