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

/** The {@code TopicRequestor} helper class simplifies
  * making service requests.
  *
  * <P>The {@code TopicRequestor} constructor is given a non-transacted 
  * {@code TopicSession} and a destination {@code Topic}. It creates a 
  * {@code TemporaryTopic} for the responses and provides a 
  * {@code request} method that sends the request message and waits 
  * for its reply.
  * <p>
  * This is a very basic request/reply abstraction which assumes the session 
  * is non-transacted with a delivery mode of either AUTO_ACKNOWLEDGE or 
  * DUPS_OK_ACKNOWLEDGE. It is expected that most applications will create 
  * less basic implementations.
  * 
  * @see javax.jms.QueueRequestor
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  * 
  */

public class TopicRequestor {

    TopicSession    session;    // The topic session the topic belongs to.
    TemporaryTopic  tempTopic;
    TopicPublisher  publisher;
    TopicSubscriber subscriber;


    /** Constructor for the {@code TopicRequestor} class.
      * 
      * <P>This implementation assumes the session parameter to be non-transacted,
      * with a delivery mode of either {@code AUTO_ACKNOWLEDGE} or 
      * {@code DUPS_OK_ACKNOWLEDGE}.
      *
      * @param session the {@code TopicSession} the topic belongs to
      * @param topic the topic to perform the request/reply call on
      *
      * @exception JMSException if the JMS provider fails to create the
      *                         {@code TopicRequestor} due to some internal
      *                         error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      */ 

    public 
    TopicRequestor(TopicSession session, Topic topic) throws JMSException {
    	
    	if (topic==null) throw new InvalidDestinationException("topic==null");

	    this.session = session;
        tempTopic    = session.createTemporaryTopic();
        publisher    = session.createPublisher(topic);
        subscriber   = session.createSubscriber(tempTopic);
    }


    /** Sends a request and waits for a reply. The temporary topic is used for
      * the {@code JMSReplyTo} destination; the first reply is returned, 
      * and any following replies are discarded.
      *
      * @param message the message to send
      *  
      * @return the reply message
      *  
      * @exception JMSException if the JMS provider fails to complete the
      *                         request due to some internal error.
      */

    public Message
    request(Message message) throws JMSException {
	message.setJMSReplyTo(tempTopic);
        publisher.publish(message);
	return(subscriber.receive());
    }


    /** Closes the {@code TopicRequestor} and its session.
      *
      * <P>Since a provider may allocate some resources on behalf of a 
      * {@code TopicRequestor} outside the Java virtual machine, clients 
      * should close them when they 
      * are not needed. Relying on garbage collection to eventually reclaim 
      * these resources may not be timely enough.
      *
      * <P>Note that this method closes the {@code TopicSession} object 
      * passed to the {@code TopicRequestor} constructor.
      *  
      * @exception JMSException if the JMS provider fails to close the
      *                         {@code TopicRequestor} due to some internal
      *                         error.
      */

    public void
    close() throws JMSException {

	// publisher and consumer created by constructor are implicitly closed.
	session.close();
	tempTopic.delete();
    }
}
