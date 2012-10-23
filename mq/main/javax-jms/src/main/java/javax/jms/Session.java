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

import java.io.Serializable;

/** <P>A <CODE>Session</CODE> object is a single-threaded context for producing and consuming 
  * messages. Although it may allocate provider resources outside the Java 
  * virtual machine (JVM), it is considered a lightweight JMS object.
  *
  * <P>A session serves several purposes:
  *
  * <UL>
  *   <LI>It is a factory for its message producers and consumers.
  *   <LI>It supplies provider-optimized message factories.
  *   <LI>It is a factory for <CODE>TemporaryTopics</CODE> and 
  *        <CODE>TemporaryQueues</CODE>. 
  *   <LI> It provides a way to create <CODE>Queue</CODE> or <CODE>Topic</CODE>
  *      objects for those clients that need to dynamically manipulate 
  *      provider-specific destination names.
  *   <LI>It supports a single series of transactions that combine work 
  *       spanning its producers and consumers into atomic units.
  *   <LI>It defines a serial order for the messages it consumes and 
  *       the messages it produces.
  *   <LI>It retains messages it consumes until they have been 
  *       acknowledged.
  *   <LI>It serializes execution of message listeners registered with 
  *       its message consumers.
  *   <LI> It is a factory for <CODE>QueueBrowsers</CODE>.
  * </UL>
  *
  * <P>A session can create and service multiple message producers and 
  * consumers.
  *
  * <P>One typical use is to have a thread block on a synchronous 
  * <CODE>MessageConsumer</CODE> until a message arrives. The thread may then
  * use one or more of the <CODE>Session</CODE>'s <CODE>MessageProducer</CODE>s.
  *
  * <P>If a client desires to have one thread produce messages while others 
  * consume them, the client should use a separate session for its producing 
  * thread.
  *
  * <P>Once a connection has been started, any session with one or more 
  * registered message listeners is dedicated to the thread of control that 
  * delivers messages to it. It is erroneous for client code to use this session
  * or any of its constituent objects from another thread of control. The
  * only exception to this rule is the use of the session or connection 
  * <CODE>close</CODE> method.
  *
  * <P>It should be easy for most clients to partition their work naturally
  * into sessions. This model allows clients to start simply and incrementally
  * add message processing complexity as their need for concurrency grows.
  *
  * <P>The <CODE>close</CODE> method is the only session method that can be 
  * called while some other session method is being executed in another thread.
  *
  * <P>A session may be specified as transacted. Each transacted 
  * session supports a single series of transactions. Each transaction groups 
  * a set of message sends and a set of message receives into an atomic unit 
  * of work. In effect, transactions organize a session's input message 
  * stream and output message stream into series of atomic units. When a 
  * transaction commits, its atomic unit of input is acknowledged and its 
  * associated atomic unit of output is sent. If a transaction rollback is 
  * done, the transaction's sent messages are destroyed and the session's input 
  * is automatically recovered.
  *
  * <P>The content of a transaction's input and output units is simply those 
  * messages that have been produced and consumed within the session's current 
  * transaction.
  *
  * <P>A transaction is completed using either its session's <CODE>commit</CODE>
  * method or its session's <CODE>rollback</CODE> method. The completion of a
  * session's current transaction automatically begins the next. The result is
  * that a transacted session always has a current transaction within which its 
  * work is done.  
  *
  * <P>The Java Transaction Service (JTS) or some other transaction monitor may 
  * be used to combine a session's transaction with transactions on other 
  * resources (databases, other JMS sessions, etc.). Since Java distributed 
  * transactions are controlled via the Java Transaction API (JTA), use of the 
  * session's <CODE>commit</CODE> and <CODE>rollback</CODE> methods in 
  * this context is prohibited.
  *
  * <P>The JMS API does not require support for JTA; however, it does define 
  * how a provider supplies this support.
  *
  * <P>Although it is also possible for a JMS client to handle distributed 
  * transactions directly, it is unlikely that many JMS clients will do this.
  * Support for JTA in the JMS API is targeted at systems vendors who will be 
  * integrating the JMS API into their application server products.
  *
  * @version     2.0
  *
  * @see         javax.jms.QueueSession
  * @see         javax.jms.TopicSession
  * @see         javax.jms.XASession
  */ 
 
public interface Session extends Runnable {

    /** With this acknowledgment mode, the session automatically acknowledges
      * a client's receipt of a message either when the session has successfully 
      * returned from a call to <CODE>receive</CODE> or when the message 
      * listener the session has called to process the message successfully 
      * returns.
      */ 

    static final int AUTO_ACKNOWLEDGE = 1;

    /** With this acknowledgment mode, the client acknowledges a consumed 
      * message by calling the message's <CODE>acknowledge</CODE> method. 
      * Acknowledging a consumed message acknowledges all messages that the 
      * session has consumed.
      *
      * <P>When client acknowledgment mode is used, a client may build up a 
      * large number of unacknowledged messages while attempting to process 
      * them. A JMS provider should provide administrators with a way to 
      * limit client overrun so that clients are not driven to resource 
      * exhaustion and ensuing failure when some resource they are using 
      * is temporarily blocked.
      *
      * @see javax.jms.Message#acknowledge()
      */ 

    static final int CLIENT_ACKNOWLEDGE = 2;

    /** This acknowledgment mode instructs the session to lazily acknowledge 
      * the delivery of messages. This is likely to result in the delivery of 
      * some duplicate messages if the JMS provider fails, so it should only be 
      * used by consumers that can tolerate duplicate messages. Use of this  
      * mode can reduce session overhead by minimizing the work the 
      * session does to prevent duplicates.
      */

    static final int DUPS_OK_ACKNOWLEDGE = 3;
    
    /** This value may be passed as the argument to the 
     * method <code>createSession(int sessionMode)</code>
     * on the <code>Connection</code> object
     * to specify that the session should use a local transaction.
     * <p>
     * This value is returned from the method 
     * <CODE>getAcknowledgeMode</CODE> if the session is using a local transaction,
     * irrespective of whether the session was created by calling the
     * method <code>createSession(int sessionMode)</code> or the 
     * method <code>createSession(boolean transacted, int acknowledgeMode)</code>.
     */
    static final int SESSION_TRANSACTED = 0;

    /** Creates a <CODE>BytesMessage</CODE> object. A <CODE>BytesMessage</CODE> 
      * object is used to send a message containing a stream of uninterpreted 
      * bytes.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 
    BytesMessage 
    createBytesMessage() throws JMSException; 

 
    /** Creates a <CODE>MapMessage</CODE> object. A <CODE>MapMessage</CODE> 
      * object is used to send a self-defining set of name-value pairs, where 
      * names are <CODE>String</CODE> objects and values are primitive values 
      * in the Java programming language.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    MapMessage 
    createMapMessage() throws JMSException; 

 
    /** Creates a <CODE>Message</CODE> object. The <CODE>Message</CODE> 
      * interface is the root interface of all JMS messages. A 
      * <CODE>Message</CODE> object holds all the 
      * standard message header information. It can be sent when a message 
      * containing only header information is sufficient.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    Message
    createMessage() throws JMSException;


    /** Creates an <CODE>ObjectMessage</CODE> object. An 
      * <CODE>ObjectMessage</CODE> object is used to send a message 
      * that contains a serializable Java object.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      *  
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    ObjectMessage
    createObjectMessage() throws JMSException; 


    /** Creates an initialized <CODE>ObjectMessage</CODE> object. An 
      * <CODE>ObjectMessage</CODE> object is used 
      * to send a message that contains a serializable Java object.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      *  
      * @param object the object to use to initialize this message
      *
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    ObjectMessage
    createObjectMessage(Serializable object) throws JMSException;

 
    /** Creates a <CODE>StreamMessage</CODE> object. A 
      * <CODE>StreamMessage</CODE> object is used to send a 
      * self-defining stream of primitive values in the Java programming 
      * language.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      * 
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */

    StreamMessage 
    createStreamMessage() throws JMSException;  

 
    /** Creates a <CODE>TextMessage</CODE> object. A <CODE>TextMessage</CODE> 
      * object is used to send a message containing a <CODE>String</CODE>
      * object.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      * 
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    TextMessage 
    createTextMessage() throws JMSException; 


    /** Creates an initialized <CODE>TextMessage</CODE> object. A 
      * <CODE>TextMessage</CODE> object is used to send 
      * a message containing a <CODE>String</CODE>.
      * <p>
      * The message object returned may be sent using any <code>Session</code> or <code>JMSContext</code>. 
      * It is not restricted to being sent using the <code>JMSContext</code> used to create it.
      * <p>
      * The message object returned may be optimised for use with the JMS provider
      * used to create it. However it can be sent using any JMS provider, not just the 
      * JMS provider used to create it.
      * 
      * @param text the string used to initialize this message
      *
      * @exception JMSException if the JMS provider fails to create this message
      *                         due to some internal error.
      */ 

    TextMessage
    createTextMessage(String text) throws JMSException;


    /** Indicates whether the session is in transacted mode.
      *  
      * @return true if the session is in transacted mode
      *  
      * @exception JMSException if the JMS provider fails to return the 
      *                         transaction mode due to some internal error.
      */ 

    boolean
    getTransacted() throws JMSException;
    
    /** Returns the acknowledgement mode of the session. The acknowledgement
     * mode is set at the time that the session is created. If the session is
     * transacted, the acknowledgement mode is ignored.
     *
     *@return            If the session is not transacted, returns the 
     *                  current acknowledgement mode for the session.
     *                  If the session
     *                  is transacted, returns SESSION_TRANSACTED.
     *
     *@exception JMSException   if the JMS provider fails to return the 
     *                         acknowledgment mode due to some internal error.
     *
     *@see Connection#createSession
     *@since 1.1
     */
    int 
    getAcknowledgeMode() throws JMSException;


    /** Commits all messages done in this transaction and releases any locks
      * currently held.
      *
      * @exception JMSException if the JMS provider fails to commit the
      *                         transaction due to some internal error.
      * @exception TransactionRolledBackException if the transaction
      *                         is rolled back due to some internal error
      *                         during commit.
      * @exception IllegalStateException if the method is not called by a 
      *                         transacted session.
      */

    void
    commit() throws JMSException;


    /** Rolls back any messages done in this transaction and releases any locks 
      * currently held.
      *
      * @exception JMSException if the JMS provider fails to roll back the
      *                         transaction due to some internal error.
      * @exception IllegalStateException if the method is not called by a 
      *                         transacted session.
      *                                     
      */

    void
    rollback() throws JMSException;


    /** Closes the session.
      *
      * <P>Since a provider may allocate some resources on behalf of a session 
      * outside the JVM, clients should close the resources when they are not 
      * needed. 
      * Relying on garbage collection to eventually reclaim these resources 
      * may not be timely enough.
      *
      * <P>There is no need to close the producers and consumers
      * of a closed session. 
      *
      * <P> This call will block until a <CODE>receive</CODE> call or message 
      * listener in progress has completed. A blocked message consumer
      * <CODE>receive</CODE> call returns <CODE>null</CODE> when this session 
      * is closed.
      * <p>
      * A message listener must not attempt to close its own session as this 
      * would lead to deadlock. The JMS provider must detect this and throw a 
      * javax.jms.IllegalStateException.
      * <p>
      * For the avoidance of doubt, if an exception listener for this session's connection 
      * is running when <code>close</code> is invoked, there is no requirement for 
      * the <code>close</code> call to wait until the exception listener has returned
      * before it may return. 
      * 
      * <P>Closing a transacted session must roll back the transaction
      * in progress.
      * 
      * <P>This method is the only <CODE>Session</CODE> method that can 
      * be called concurrently. 
      *
      * <P>Invoking any other <CODE>Session</CODE> method on a closed session 
      * must throw a <CODE>JMSException.IllegalStateException</CODE>. Closing a 
      * closed session must <I>not</I> throw an exception.
      * 
      * @exception JMSException if the JMS provider fails to close the
      *                         session due to some internal error.
      *                         
      */

    void
    close() throws JMSException;


    /** Stops message delivery in this session, and restarts message delivery
      * with the oldest unacknowledged message.
      *  
      * <P>All consumers deliver messages in a serial order.
      * Acknowledging a received message automatically acknowledges all 
      * messages that have been delivered to the client.
      *
      * <P>Restarting a session causes it to take the following actions:
      *
      * <UL>
      *   <LI>Stop message delivery
      *   <LI>Mark all messages that might have been delivered but not 
      *       acknowledged as "redelivered"
      *   <LI>Restart the delivery sequence including all unacknowledged 
      *       messages that had been previously delivered. Redelivered messages
      *       do not have to be delivered in 
      *       exactly their original delivery order.
      * </UL>
      *
      * @exception JMSException if the JMS provider fails to stop and restart
      *                         message delivery due to some internal error.
      * @exception IllegalStateException if the method is called by a 
      *                         transacted session.
      */ 

    void
    recover() throws JMSException;


    /** Returns the session's distinguished message listener (optional).
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a <code>JMSException</code> to be thrown though this is not guaranteed.
      * 
      * @return the distinguished message listener associated with this session
      *
      * @exception JMSException if the JMS provider fails to get the session's distinguished message  
      *                         listener for one of the following reasons:
      *                         <ul>
      *                         <li>an internal error has occurred
      *                         <li>this method has been called in a Java EE web or EJB application 
      *                         (though it is not guaranteed that an exception is thrown in this case)
      *                         </ul>
      *      
      * @see javax.jms.Session#setMessageListener
      * @see javax.jms.ServerSessionPool
      * @see javax.jms.ServerSession
      */
    MessageListener getMessageListener() throws JMSException; 
    
    /** Sets the session's distinguished message listener (optional).
     *
     * <P>When the distinguished message listener is set, no other form of 
     * message receipt in the session can 
     * be used; however, all forms of sending messages are still supported.
     * 
     * <P>This is an expert facility not used by ordinary JMS clients.
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a <code>JMSException</code> to be thrown though this is not guaranteed.
     * 
     * @param listener the message listener to associate with this session
     *
     * @exception JMSException if the JMS provider fails to set the session's distinguished message  
     *                         listener for one of the following reasons:
     *                         <ul>
     *                         <li>an internal error has occurred
     *                         <li>this method has been called in a Java EE web or EJB application 
     *                         (though it is not guaranteed that an exception is thrown in this case)
     *                         </ul>
     *
     * @see javax.jms.Session#getMessageListener
     * @see javax.jms.ServerSessionPool
     * @see javax.jms.ServerSession
     */
    void setMessageListener(MessageListener listener) throws JMSException;
    
    /**
     * Optional operation, intended to be used only by Application Servers,
     * not by ordinary JMS clients.
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a <code>JMSRuntimeException</code> to be thrown though this is not guaranteed.
     * 
      * @exception JMSRuntimeException if this method has been called in a Java EE web or EJB application 
      *                         (though it is not guaranteed that an exception is thrown in this case)
      *                           
     * @see javax.jms.ServerSession
     */
    public void run();
    
    /** Creates a <CODE>MessageProducer</CODE> to send messages to the specified 
      * destination.
      *
      * <P>A client uses a <CODE>MessageProducer</CODE> object to send 
      * messages to a destination. Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageProducer</CODE> object.
      * 
      * @param destination the <CODE>Destination</CODE> to send to, 
      * or null if this is a producer which does not have a specified 
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

    MessageProducer
    createProducer(Destination destination) throws JMSException;
    
    
       /** Creates a <CODE>MessageConsumer</CODE> for the specified destination.
      * Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
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

    MessageConsumer
    createConsumer(Destination destination) throws JMSException;

       /** Creates a <CODE>MessageConsumer</CODE> for the specified destination, 
      * using a message selector. 
      * Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
      *
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive 
      * messages that have been sent to a destination.
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
    MessageConsumer     
    createConsumer(Destination destination, java.lang.String messageSelector) 
    throws JMSException;
    
    
     /** Creates <CODE>MessageConsumer</CODE> for the specified destination, specifying a
      * message selector and the <code>noLocal</code> parameter.
      *<P> Since <CODE>Queue</CODE> and <CODE>Topic</CODE> 
      * both inherit from <CODE>Destination</CODE>, they can be used in
      * the destination parameter to create a <CODE>MessageConsumer</CODE>.
      * <P>A client uses a <CODE>MessageConsumer</CODE> object to receive 
      * messages that have been published to a destination. 
      *               
      * <P>The <code>noLocal</code> argument is for use when the
      * destination is a topic and the session's connection 
      * is also being used to publish messages to that topic. 
      * If <code>noLocal</code> is set to true then the 
      * <code>MessageConsumer</code> will not receive messages published
      * to the topic by its own connection. The default value of this 
      * argument is false. If the destination is a queue
      * then the effect of setting <code>noLocal</code>
      * to true is not specified.
      *
      * @param destination the <CODE>Destination</CODE> to access 
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      * @param noLocal  - if true, and the destination is a topic,
      *                   then the <code>MessageConsumer</code> will 
      *                   not receive messages published to the topic
      *                   by its own connection. 
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
    MessageConsumer     
    createConsumer(Destination destination, java.lang.String messageSelector, 
    boolean noLocal)   throws JMSException;
    
	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, and creates a <code>MessageConsumer</code> on that
	 * subscription.
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic, and without a message selector, then this method
	 * creates a <code>MessageConsumer</code> on the existing subscription.
	 * <p>
	 * A non-durable shared subscription is used by a client which needs to be
	 * able to share the work of receiving messages from a topic subscription
	 * amongst multiple consumers. A non-durable shared subscription may
	 * therefore have more than one consumer. Each message from the subscription
	 * will be delivered to only one of the consumers on that subscription. Such
	 * a subscription is not persisted and will be deleted (together with any
	 * undelivered messages associated with it) when there are no consumers on
	 * it.
	 * <p>
	 * A consumer may be created on a non-durable shared subscription using the
	 * <code>createSharedConsumer</code> methods on <code>JMSContext</code>,
	 * <code>Session</code> or <code>TopicSession</code>.
	 * <p>
	 * If there is an active consumer on the non-durable shared subscription (or
	 * a consumed message from that subscription is still part of a pending
	 * transaction or is not yet acknowledged in the session), and an attempt is
	 * made to create an additional consumer, specifying the same name but a
	 * different topic or message selector, then a <code>JMSException</code>
	 * will be thrown.
	 * <p>
	 * There is no restriction to prevent a shared non-durable subscription and
	 * a durable subscription having the same name. Such subscriptions would be
	 * completely separate.
	 * 
	 * @param topic
	 *            the <code>Topic</code> to subscribe to
	 * @param sharedSubscriptionName
	 *            the name used to identify the shared non-durable subscription
	 * 
	 * @throws JMSException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>MessageConsumer</code> due to some
	 *             internal error.
	 * @throws InvalidDestinationException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorException
	 *             if the message selector is invalid.
	 * 
	 * @since 2.0
	 */ 
	MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException;

	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, specifying a message selector, and creates a
	 * <code>MessageConsumer</code> on that subscription.
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic and message selector then this method creates a
	 * <code>MessageConsumer</code> on the existing subscription.
	 * <p>
	 * A non-durable shared subscription is used by a client which needs to be
	 * able to share the work of receiving messages from a topic subscription
	 * amongst multiple consumers. A non-durable shared subscription may
	 * therefore have more than one consumer. Each message from the subscription
	 * will be delivered to only one of the consumers on that subscription. Such
	 * a subscription is not persisted and will be deleted (together with any
	 * undelivered messages associated with it) when there are no consumers on
	 * it.
	 * <p>
	 * A consumer may be created on a non-durable shared subscription using the
	 * <code>createSharedConsumer</code> methods on <code>JMSContext</code>,
	 * <code>Session</code> or <code>TopicSession</code>.
	 * <p>
	 * If there is an active consumer on the non-durable shared subscription (or
	 * a consumed message from that subscription is still part of a pending
	 * transaction or is not yet acknowledged in the session), and an attempt is
	 * made to create an additional consumer, specifying the same name but a
	 * different topic or message selector, then a <code>JMSException</code>
	 * will be thrown.
	 * <p>
	 * There is no restriction to prevent a shared non-durable subscription and
	 * a durable subscription having the same name. Such subscriptions would be
	 * completely separate.
	 * 
	 * @param topic
	 *            the <code>Topic</code> to subscribe to
	 * @param sharedSubscriptionName
	 *            the name used to identify the shared non-durable subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are added to the shared non-durable subscription. A
	 *            value of null or an empty string indicates that there is no
	 *            message selector for the shared non-durable subscription.
	 * 
	 * @throws JMSException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>MessageConsumer</code> due to some
	 *             internal error.
	 * @throws InvalidDestinationException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorException
	 *             if the message selector is invalid.
	 * 
	 * @since 2.0
	 */ 
	MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, java.lang.String messageSelector)
			throws JMSException;

	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, specifying a message selector and the
	 * <code>noLocal</code> parameter, and creates a
	 * <code>MessageConsumer</code> on that subscription,
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic and message selector then this method creates a
	 * <code>MessageConsumer</code> on the existing subscription.
	 * <p>
	 * A non-durable shared subscription is used by a client which needs to be
	 * able to share the work of receiving messages from a topic subscription
	 * amongst multiple consumers. A non-durable shared subscription may
	 * therefore have more than one consumer. Each message from the subscription
	 * will be delivered to only one of the consumers on that subscription. Such
	 * a subscription is not persisted and will be deleted (together with any
	 * undelivered messages associated with it) when there are no consumers on
	 * it.
	 * <p>
	 * A consumer may be created on a non-durable shared subscription using the
	 * <code>createSharedConsumer</code> methods on <code>JMSContext</code>,
	 * <code>Session</code> or <code>TopicSession</code>.
	 * <p>
	 * If there is an active consumer on the non-durable shared subscription (or
	 * a consumed message from that subscription is still part of a pending
	 * transaction or is not yet acknowledged in the session), and an attempt is
	 * made to create an additional consumer, specifying the same name but a
	 * different topic or message selector, then a <code>JMSException</code>
	 * will be thrown.
	 * <p>
	 * If <code>noLocal</code> is set to true then messages published to the
	 * topic by its own connection will not be added to the shared non-durable
	 * subscription. The default value of this argument is false.
	 * <p>
	 * There is no restriction to prevent a shared non-durable subscription and
	 * a durable subscription having the same name. Such subscriptions would be
	 * completely separate.
	 * 
	 * @param topic
	 *            the <code>Topic</code> to subscribe to
	 * @param sharedSubscriptionName
	 *            the name used to identify the shared non-durable subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are added to the shared non-durable subscription. A
	 *            value of null or an empty string indicates that there is no
	 *            message selector for the non-durable subscription.
	 * @param noLocal
	 *            if true, messages published by its own connection will not be
	 *            added to the shared non-durable subscription.
	 * 
	 * @throws JMSException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>MessageConsumer</code> due to some
	 *             internal error.
	 * @throws InvalidDestinationException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorException
	 *             if the message selector is invalid.
	 * 
	 * @since 2.0
	 */ 
	MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, java.lang.String messageSelector,
			boolean noLocal) throws JMSException;   
    
	/**
	 * Creates a <code>Queue</code> object which encapsulates a specified
	 * provider-specific queue name.
	 * <p>
	 * The use of provider-specific queue names in an application may render the
	 * application non-portable. Portable applications are recommended to not
	 * use this method but instead look up an administratively-defined
	 * <code>Queue</code> object using JNDI.
	 * <p>
	 * Note that this method simply creates an object that encapsulates the name
	 * of a queue. It does not create the physical queue in the JMS provider.
	 * JMS does not provide a method to create the physical queue, since this
	 * would be specific to a given JMS provider. Creating a physical queue is
	 * provider-specific and is typically an administrative task performed by an 
	 * administrator, though some providers may create them automatically when
	 * needed. The one exception to this is the creation of a temporary queue,
	 * which is done using the <code>createTemporaryQueue</code> method.
	 * 
	 * @param queueName
	 *            A provider-specific queue name
	 * @return a Queue object which encapsulates the specified name
	 * 
	 * @throws JMSException
	 *             if a Queue object cannot be created due to some internal error 
	 */
	Queue createQueue(String queueName) throws JMSException;

	/**
	 * Creates a <code>Topic</code> object which encapsulates a specified
	 * provider-specific topic name.
	 * <p>
	 * The use of provider-specific topic names in an application may render the
	 * application non-portable. Portable applications are recommended to not
	 * use this method but instead look up an administratively-defined
	 * <code>Topic</code> object using JNDI.
	 * <p>
	 * Note that this method simply creates an object that encapsulates the name
	 * of a topic. It does not create the physical topic in the JMS provider.
	 * JMS does not provide a method to create the physical topic, since this
	 * would be specific to a given JMS provider. Creating a physical topic is
	 * provider-specific and is typically an administrative task performed by an
	 * administrator, though some providers may create them automatically when
	 * needed. The one exception to this is the creation of a temporary topic,
	 * which is done using the <code>createTemporaryTopic</code> method.
	 * 
	 * @param topicName
	 *            A provider-specific topic name
	 * @return a Topic object which encapsulates the specified name
	 * 
	 * @throws JMSException
	 *             if a Topic object cannot be created due to some internal
	 *             error
	 */
	Topic createTopic(String topicName) throws JMSException;

     /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue.
      *
      * @param queue the <CODE>queue</CODE> to access
      *
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      *
      * @since 1.1 
      */
    
    /** Creates a durable subscription with the specified name on the
     * specified topic, and creates a <code>TopicSubscriber</code> 
     * on that durable subscription.
     * <p>
     * This method is identical to the corresponding <code>createDurableConsumer</code>
     * method except that it returns a <code>TopicSubscriber</code> rather than a
     * <code>MessageConsumer</code>.    
     * The term "consumer" applies to both <code>TopicSubscriber</code> and <code>MessageConsumer</code> objects.
     * <p>
     * If a durable subscription already exists with the same name 
     * and client identifier (if set) and the same topic, and without a message selector,
     * then this method creates a <code>TopicSubscriber</code> on the existing durable
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
     * until it is deleted using the <code>unsubscribe</code> method. 
     * <p>
     * A consumer may be created on a durable subscription using the
     * <code>createDurableConsumer</code> methods on <code>JMSContext</code>,
     * or the <code>createDurableConsumer</code> and <code>createDurableSubscriber</code>
     * methods on <code>Session</code> or <code>TopicSession</code>.
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
	 * If there are no active consumers on the durable subscription (and no
	 * consumed messages from that subscription are still part of a pending
	 * transaction or are not yet acknowledged in the session), and this method
	 * is used to create a new consumer on that durable subscription, specifying
	 * the same name and client identifier (if set) but a different topic or
	 * message selector, or, if the client identifier is set, a different
	 * noLocal argument, then the durable subscription will be deleted and a new
	 * one created. 
	 * <p>
	 * However if there is an active consumer on the durable
	 * subscription (or a consumed message from that subscription is still part
	 * of a pending transaction or is not yet acknowledged in the session), and
	 * an attempt is made to create an additional consumer, specifying the same
	 * name and client identifier (if set) but a different topic or message
	 * selector, or, if the client identifier is set, a different noLocal
	 * argument, then a <code>JMSException</code> will be thrown.
     *
     * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
     * @param name the name used to identify this subscription
     *  
     * @exception JMSException if the session fails to create the durable subscription 
     *            and <code>TopicSubscriber</code> due to some internal error.
     * @exception InvalidDestinationException if an invalid topic is specified.
     *
     * @since 1.1
     */ 
    TopicSubscriber
    createDurableSubscriber(Topic topic, 
			    String name) throws JMSException;

    /**
	 * Creates a durable subscription with the specified name on the specified
	 * topic (if one does not already exist), specifying a message selector and
	 * the <code>noLocal</code> parameter, and creates a
	 * <code>TopicSubscriber</code> on that durable subscription.
	 * <p>
	 * <p>
	 * This method is identical to the corresponding
	 * <code>createDurableConsumer</code> method except that it returns a
	 * <code>TopicSubscriber</code> rather than a <code>MessageConsumer</code>.
	 * The term "consumer" applies to both <code>TopicSubscriber</code> and
	 * <code>MessageConsumer</code> objects.
	 * <p>
	 * If a durable subscription already exists with the same name and client
	 * identifier (if set) and the same topic and message selector then this
	 * method creates a <code>TopicSubscriber</code> on the existing durable
	 * subscription.
	 * <p>
	 * A durable subscription is used by a client which needs to receive all the
	 * messages published on a topic, including the ones published when there is
	 * no consumer associated with it. The JMS provider retains a record of this
	 * durable subscription and ensures that all messages from the topic's
	 * publishers are retained until they are delivered to, and acknowledged by,
	 * a consumer on this durable subscription or until they have expired.
	 * <p>
	 * A durable subscription will continue to accumulate messages until it is
	 * deleted using the <code>unsubscribe</code> method.
	 * <p>
	 * A consumer may be created on a durable subscription using the
	 * <code>createDurableConsumer</code> methods on <code>JMSContext</code>, or
	 * the <code>createDurableConsumer</code> and
	 * <code>createDurableSubscriber</code> methods on <code>Session</code> or
	 * <code>TopicSession</code>. A durable subscription which has a consumer
	 * associated with it is described as being active. A durable subscription
	 * which has no consumer associated with it is described as being inactive.
	 * <p>
	 * A durable subscription may have more than one active consumer (this was
	 * not permitted prior to JMS 2.0). Each message from the subscription will
	 * be delivered to only one of the consumers on that subscription.
	 * <p>
	 * A durable subscription is identified by a name specified by the client
	 * and by the client identifier if set. If the client identifier was set
	 * when the durable subscription was first created then a client which
	 * subsequently wishes to create a consumer on that durable subscription
	 * must use the same client identifier.
	 * <p>
	 * If there are no active consumers on the durable subscription (and no
	 * consumed messages from that subscription are still part of a pending
	 * transaction or are not yet acknowledged in the session), and this method
	 * is used to create a new consumer on that durable subscription, specifying
	 * the same name and client identifier (if set) but a different topic or
	 * message selector, or, if the client identifier is set, a different
	 * noLocal argument, then the durable subscription will be deleted and a new
	 * one created. 
	 * <p>
	 * However if there is an active consumer on the durable
	 * subscription (or a consumed message from that subscription is still part
	 * of a pending transaction or is not yet acknowledged in the session), and
	 * an attempt is made to create an additional consumer, specifying the same
	 * name and client identifier (if set) but a different topic or message
	 * selector, or, if the client identifier is set, a different noLocal
	 * argument, then a <code>JMSException</code> will be thrown.
	 * <P>
	 * If <code>noLocal</code> is set to true, and the client identifier is set,
	 * then any messages published to the topic using this session's connection,
	 * or any other connection or <code>JMSContext</code> with the same client
	 * identifier, will not be added to the durable subscription. If the client
	 * identifier is unset then setting <code>noLocal</code> to true will cause a
	 * <code>IllegalStateException</code> to be thrown. 
	 * The default value of  <code>noLocal</code> is false.
	 * 
	 * @param topic
	 *            the non-temporary <CODE>Topic</CODE> to subscribe to
	 * @param name
	 *            the name used to identify this subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are added to the durable subscription. A value of
	 *            null or an empty string indicates that there is no message
	 *            selector for the durable subscription.
	 * @param noLocal
	 *            if true, and the client identifier is set, then any messages
	 *            published to the topic using this session's connection, or any
	 *            other connection or <code>JMSContext</code> with the same
	 *            client identifier, will not be added to the durable
	 *            subscription.
	 * @exception JMSException
	 *                if the session fails to create the durable subscription
	 *                and <code>TopicSubscriber</code> due to some internal
	 *                error.
	 * @exception InvalidDestinationException
	 *                if an invalid topic is specified.
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid.
	 * @exception IllegalStateException
	 *                if <code>noLocal</code> is set to <code>true</code>
	 *                but the client identifier is unset
	 * 
	 * @since 1.1
	 */ 
	TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
			throws JMSException;
     
     /** Creates a durable subscription with the specified name on the
      * specified topic, and creates a <code>MessageConsumer</code> 
      * on that durable subscription.
      * <p>
      * If a durable subscription already exists with the same name 
      * and client identifier (if set) and the same topic,
      * and without a message selector,
      * then this method creates a <code>MessageConsumer</code> on the existing durable
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
      * until it is deleted using the <code>unsubscribe</code> method. 
      * <p>
      * A consumer may be created on a durable subscription using the
      * <code>createDurableConsumer</code> methods on <code>JMSContext</code>,
      * or the <code>createDurableConsumer</code> and <code>createDurableSubscriber</code>
      * methods on <code>Session</code> or <code>TopicSession</code>.
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
	 * If there are no active consumers on the durable subscription (and no
	 * consumed messages from that subscription are still part of a pending
	 * transaction or are not yet acknowledged in the session), and this method
	 * is used to create a new consumer on that durable subscription, specifying
	 * the same name and client identifier (if set) but a different topic or
	 * message selector, or, if the client identifier is set, a different
	 * noLocal argument, then the durable subscription will be deleted and a new
	 * one created. 
	 * <p>
	 * However if there is an active consumer on the durable
	 * subscription (or a consumed message from that subscription is still part
	 * of a pending transaction or is not yet acknowledged in the session), and
	 * an attempt is made to create an additional consumer, specifying the same
	 * name and client identifier (if set) but a different topic or message
	 * selector, or, if the client identifier is set, a different noLocal
	 * argument, then a <code>JMSException</code> will be thrown.
      *
      * @param topic the non-temporary <CODE>Topic</CODE> to subscribe to
      * @param name the name used to identify this subscription
      *  
      * @exception JMSException if the session fails to create the durable subscription 
      *            and <code>MessageConsumer</code> due to some internal error.
      * @exception InvalidDestinationException if an invalid topic is specified.
      *
      * @since 2.0
      */ 
     MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException;

	/**
	 * Creates a durable subscription with the specified name on the specified
	 * topic, specifying a message selector and the <code>noLocal</code>
	 * parameter, and creates a <code>MessageConsumer</code> on that durable
	 * subscription.
	 * <p>
	 * If a durable subscription already exists with the same name and client
	 * identifier (if set) and the same topic and message selector then this
	 * method creates a <code>MessageConsumer</code> on the existing durable
	 * subscription.
	 * <p>
	 * A durable subscription is used by a client which needs to receive all the
	 * messages published on a topic, including the ones published when there is
	 * no consumer associated with it. The JMS provider retains a record of this
	 * durable subscription and ensures that all messages from the topic's
	 * publishers are retained until they are delivered to, and acknowledged by,
	 * a consumer on this durable subscription or until they have expired.
	 * <p>
	 * A durable subscription will continue to accumulate messages until it is
	 * deleted using the <code>unsubscribe</code> method.
	 * <p>
	 * A consumer may be created on a durable subscription using the
	 * <code>createDurableConsumer</code> methods on <code>JMSContext</code>, or
	 * the <code>createDurableConsumer</code> and
	 * <code>createDurableSubscriber</code> methods on <code>Session</code> or
	 * <code>TopicSession</code>. A durable subscription which has a consumer
	 * associated with it is described as being active. A durable subscription
	 * which has no consumer associated with it is described as being inactive.
	 * <p>
	 * A durable subscription may have more than one active consumer (this was
	 * not permitted prior to JMS 2.0). Each message from the subscription will
	 * be delivered to only one of the consumers on that subscription.
	 * <p>
	 * A durable subscription is identified by a name specified by the client
	 * and by the client identifier if set. If the client identifier was set
	 * when the durable subscription was first created then a client which
	 * subsequently wishes to create a consumer on that durable subscription
	 * must use the same client identifier.
	 * <p>
	 * If there are no active consumers on the durable subscription (and no
	 * consumed messages from that subscription are still part of a pending
	 * transaction or are not yet acknowledged in the session), and this method
	 * is used to create a new consumer on that durable subscription, specifying
	 * the same name and client identifier (if set) but a different topic or
	 * message selector, or, if the client identifier is set, a different
	 * noLocal argument, then the durable subscription will be deleted and a new
	 * one created. 
	 * <p>
	 * However if there is an active consumer on the durable
	 * subscription (or a consumed message from that subscription is still part
	 * of a pending transaction or is not yet acknowledged in the session), and
	 * an attempt is made to create an additional consumer, specifying the same
	 * name and client identifier (if set) but a different topic or message
	 * selector, or, if the client identifier is set, a different noLocal
	 * argument, then a <code>JMSException</code> will be thrown.
	 * <P>
	 * If <code>noLocal</code> is set to true, and the client identifier is set,
	 * then any messages published to the topic using this session's connection,
	 * or any other connection or <code>JMSContext</code> with the same client
	 * identifier, will not be added to the durable subscription. If the client
	 * identifier is unset then setting <code>noLocal</code> to true will cause a
	 * <code>IllegalStateException</code> to be thrown. 
	 * The default value of  <code>noLocal</code> is false.
	 * 
	 * @param topic
	 *            the non-temporary <CODE>Topic</CODE> to subscribe to
	 * @param name
	 *            the name used to identify this subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are added to the durable subscription. A value of
	 *            null or an empty string indicates that there is no message
	 *            selector for the durable subscription.
	 * @param noLocal
	 *            if true, and the client identifier is set, then any messages
	 *            published to the topic using this session's connection, or any
	 *            other connection or <code>JMSContext</code> with the same
	 *            client identifier, will not be added to the durable
	 *            subscription.
	 * @exception JMSException
	 *                if the session fails to create the durable subscription
	 *                and <code>MessageConsumer</code> due to some internal
	 *                error.
	 * @exception InvalidDestinationException
	 *                if an invalid topic is specified
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid
	 * @exception IllegalStateException
	 *                if <code>noLocal</code> is set to <code>true</code>
	 *                but the client identifier is unset
	 * 
	 * @since 2.0
	 */ 
      MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException;     
    
  /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue.
      *  
      * @param queue the <CODE>queue</CODE> to access
      *
      *  
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      *
      * @since 1.1 
      */ 
    QueueBrowser 
    createBrowser(Queue queue) throws JMSException;


    /** Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on 
      * the specified queue using a message selector.
      *  
      * @param queue the <CODE>queue</CODE> to access
      *
      * @param messageSelector only messages with properties matching the
      * message selector expression are delivered. A value of null or
      * an empty string indicates that there is no message selector 
      * for the message consumer.
      *  
      * @exception JMSException if the session fails to create a browser
      *                         due to some internal error.
      * @exception InvalidDestinationException if an invalid destination
      *                         is specified 
      * @exception InvalidSelectorException if the message selector is invalid.
      *
      * @since 1.1 
      */ 

    QueueBrowser
    createBrowser(Queue queue,
		  String messageSelector) throws JMSException;

    
     /** Creates a <CODE>TemporaryQueue</CODE> object. Its lifetime will be that 
      * of the <CODE>Connection</CODE> unless it is deleted earlier.
      *
      * @return a temporary queue identity
      *
      * @exception JMSException if the session fails to create a temporary queue
      *                         due to some internal error.
      *
      *@since 1.1
      */

    TemporaryQueue
    createTemporaryQueue() throws JMSException;
   

     /** Creates a <CODE>TemporaryTopic</CODE> object. Its lifetime will be that 
      * of the <CODE>Connection</CODE> unless it is deleted earlier.
      *
      * @return a temporary topic identity
      *
      * @exception JMSException if the session fails to create a temporary
      *                         topic due to some internal error.
      *
      * @since 1.1  
      */
 
    TemporaryTopic
    createTemporaryTopic() throws JMSException;


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
      *                         durable subscription due to some internal error.
      * @exception InvalidDestinationException if an invalid subscription name
      *                                        is specified.
      *
      * @since 1.1
      */

    void
    unsubscribe(String name) throws JMSException;
   
}
