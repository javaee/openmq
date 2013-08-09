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

/** A {@code Connection} object is a client's active connection to its JMS 
  * provider. It typically allocates provider resources outside the Java virtual
  * machine (JVM).
  *
  * <P>Connections support concurrent use.
  *
  * <P>A connection serves several purposes:
  *
  * <UL>
  *   <LI>It encapsulates an open connection with a JMS provider. It 
  *       typically represents an open TCP/IP socket between a client and 
  *       the service provider software.
  *   <LI>Its creation is where client authentication takes place.
  *   <LI>It can specify a unique client identifier.
  *   <LI>It provides a {@code ConnectionMetaData} object.
  *   <LI>It supports an optional {@code ExceptionListener} object.
  * </UL>
  *
  * <P>Because the creation of a connection involves setting up authentication 
  * and communication, a connection is a relatively heavyweight 
  * object. Most clients will do all their messaging with a single connection.
  * Other more advanced applications may use several connections. The JMS API
  * does 
  * not architect a reason for using multiple connections; however, there may 
  * be operational reasons for doing so.
  *
  * <P>A JMS client typically creates a connection, one or more sessions, 
  * and a number of message producers and consumers. When a connection is
  * created, it is in stopped mode. That means that no messages are being
  * delivered.
  *
  * <P>It is typical to leave the connection in stopped mode until setup 
  * is complete (that is, until all message consumers have been 
  * created).  At that point, the client calls 
  * the connection's {@code start} method, and messages begin arriving at 
  * the connection's consumers. This setup
  * convention minimizes any client confusion that may result from 
  * asynchronous message delivery while the client is still in the process 
  * of setting itself up.
  *
  * <P>A connection can be started immediately, and the setup can be done 
  * afterwards. Clients that do this must be prepared to handle asynchronous 
  * message delivery while they are still in the process of setting up.
  *
  * <P>A message producer can send messages while a connection is stopped.
  * 
  * @see         javax.jms.ConnectionFactory
  * @see         javax.jms.QueueConnection
  * @see         javax.jms.TopicConnection
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  *
  */

public interface Connection extends AutoCloseable {

    /** 
     * Creates a {@code Session} object, 
     * specifying {@code transacted} and {@code acknowledgeMode}.
     * <p>
     * This method has been superseded by the method {@code createSession(int sessionMode)}
     * which specifies the same information using a single argument, 
     * and by the method {@code createSession()} which is for use in a Java EE JTA transaction.
     * Applications should consider using those methods instead of this one.
     * <p> 
     * The effect of setting the {@code transacted} and {@code acknowledgeMode} 
     * arguments depends on whether this method is called in a Java SE environment, 
     * in the Java EE application client container, or in the Java EE web or EJB container.
     * If this method is called in the Java EE web or EJB container then the 
     * effect of setting the transacted} and {@code acknowledgeMode} 
     * arguments also depends on whether or not there is an active JTA transaction 
     * in progress.  
     * <p>
     * In a <b>Java SE environment</b> or in <b>the Java EE application client container</b>:
     * <ul>
     * <li>If {@code transacted} is set to {@code true} then the session 
     * will use a local transaction which may subsequently be committed or rolled back 
     * by calling the session's {@code commit} or {@code rollback} methods. 
     * The argument {@code acknowledgeMode} is ignored.
     * <li>If {@code transacted} is set to {@code false} then the session 
     * will be non-transacted. In this case the argument {@code acknowledgeMode}
     * is used to specify how messages received by this session will be acknowledged.
     * The permitted values are 
     * {@code Session.CLIENT_ACKNOWLEDGE}, 
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * For a definition of the meaning of these acknowledgement modes see the links below.
     * </ul>
     * <p>
     * In a <b>Java EE web or EJB container, when there is an active JTA transaction in progress</b>:
     * <ul>
     * <li>Both arguments {@code transacted} and {@code acknowledgeMode} are ignored.
     * The session will participate in the JTA transaction and will be committed or rolled back
     * when that transaction is committed or rolled back, 
     * not by calling the session's {@code commit} or {@code rollback} methods.
     * Since both arguments are ignored, developers are recommended to use 
     * {@code createSession()}, which has no arguments, instead of this method.
     * </ul>
     * <p>
     * In the <b>Java EE web or EJB container, when there is no active JTA transaction in progress</b>:
     * <ul>
     * <li>The argument {@code transacted} is ignored. The session will always be non-transacted,
     * using one of the two acknowledgement modes AUTO_ACKNOWLEDGE and DUPS_OK_ACKNOWLEDGE.
     * <li>The argument {@code acknowledgeMode}
     * is used to specify how messages received by this session will be acknowledged.
     * The only permitted values in this case are  
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * The value {@code Session.CLIENT_ACKNOWLEDGE} may not be used.
     * For a definition of the meaning of these acknowledgement modes see the links below.
     * </ul> 
     * <p>
     * Applications running in the Java EE web and EJB containers must not attempt 
     * to create more than one active (not closed) {@code Session} object per connection. 
     * If this method is called in a Java EE web or EJB container when an active
     * {@code Session} object already exists for this connection then a {@code JMSException} will be thrown.
     * 
     * @param transacted indicates whether the session will use a local transaction.
     * If this method is called in the Java EE web or EJB container then this argument is ignored.
     * 
     * @param acknowledgeMode indicates how messages received by the session will be acknowledged.
     * <ul>
     * <li>If this method is called in a Java SE environment or in the Java EE application client container, 
     * the permitted values are 
     * {@code Session.CLIENT_ACKNOWLEDGE}, 
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}. 
     * <li> If this method is called in the Java EE web or EJB container when there is an active JTA transaction in progress 
     * then this argument is ignored.
     * <li>If this method is called in the Java EE web or EJB container when there is no active JTA transaction in progress, the permitted values are
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * In this case {@code Session.CLIENT_ACKNOWLEDGE} is not permitted.
     * </ul>
     * 
     * @return a newly created  session
     *  
     * @exception JMSException if the {@code Connection} object fails
     *                         to create a session due to 
     *                         <ul>
     *                         <li>some internal error, 
     *                         <li>lack of support for the specific transaction and acknowledgement mode, or
     *                         <li>because this method is being called in a Java EE web or EJB application 
     *                         and an active session already exists for this connection.
     *                         </ul>
     * @since JMS 1.1
     *
     * @see Session#AUTO_ACKNOWLEDGE 
     * @see Session#CLIENT_ACKNOWLEDGE 
     * @see Session#DUPS_OK_ACKNOWLEDGE 
     * 
     * @see javax.jms.Connection#createSession(int) 
     * @see javax.jms.Connection#createSession() 
     */ 

    Session createSession(boolean transacted, int acknowledgeMode) throws JMSException;
    
     /** 
     * Creates a {@code Session} object, specifying {@code sessionMode}.
     * <p>
     * The effect of setting the {@code sessionMode}  
     * argument depends on whether this method is called in a Java SE environment, 
     * in the Java EE application client container, or in the Java EE web or EJB container.
     * If this method is called in the Java EE web or EJB container then the 
     * effect of setting the {@code sessionMode} argument also depends on 
     * whether or not there is an active JTA transaction in progress. 
     * <p>
     * In a <b>Java SE environment</b> or in <b>the Java EE application client container</b>:
     * <ul>
     * <li>If {@code sessionMode} is set to {@code Session.SESSION_TRANSACTED} then the session 
     * will use a local transaction which may subsequently be committed or rolled back 
     * by calling the session's {@code commit} or {@code rollback} methods. 
     * <li>If {@code sessionMode} is set to any of 
     * {@code Session.CLIENT_ACKNOWLEDGE}, 
     * {@code Session.AUTO_ACKNOWLEDGE} or
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * then the session will be non-transacted and 
     * messages received by this session will be acknowledged
     * according to the value of {@code sessionMode}.
     * For a definition of the meaning of these acknowledgement modes see the links below.
     * </ul>
     * <p>
     * In a <b>Java EE web or EJB container, when there is an active JTA transaction in progress</b>:
     * <ul>
     * <li>The argument {@code sessionMode} is ignored.
     * The session will participate in the JTA transaction and will be committed or rolled back
     * when that transaction is committed or rolled back, 
     * not by calling the session's {@code commit} or {@code rollback} methods.
     * Since the argument is ignored, developers are recommended to use 
     * {@code createSession()}, which has no arguments, instead of this method.
     * </ul>
     * <p>
     * In the <b>Java EE web or EJB container, when there is no active JTA transaction in progress</b>:
     * <ul>
     * <li>The argument {@code acknowledgeMode} must be set to either of 
     * {@code Session.AUTO_ACKNOWLEDGE} or
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * The session will be non-transacted and messages received by this session will be acknowledged
     * automatically according to the value of {@code acknowledgeMode}.
     * For a definition of the meaning of these acknowledgement modes see the links below.
     * The values {@code Session.SESSION_TRANSACTED} and {@code Session.CLIENT_ACKNOWLEDGE} may not be used.
     * </ul> 
     * <p>
     * Applications running in the Java EE web and EJB containers must not attempt 
     * to create more than one active (not closed) {@code Session} object per connection. 
     * If this method is called in a Java EE web or EJB container when an active
     * {@code Session} object already exists for this connection then a {@code JMSException} will be thrown.
     * 
     * @param sessionMode indicates which of four possible session modes will be used.
     * <ul>
     * <li>If this method is called in a Java SE environment or in the Java EE application client container, 
     * the permitted values are 
     * {@code Session.SESSION_TRANSACTED}, 
     * {@code Session.CLIENT_ACKNOWLEDGE}, 
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}. 
     * <li> If this method is called in the Java EE web or EJB container when there is an active JTA transaction in progress 
     * then this argument is ignored.
     * <li>If this method is called in the Java EE web or EJB container when there is no active JTA transaction in progress, the permitted values are
     * {@code Session.AUTO_ACKNOWLEDGE} and
     * {@code Session.DUPS_OK_ACKNOWLEDGE}.
     * In this case the values {@code Session.TRANSACTED} and {@code Session.CLIENT_ACKNOWLEDGE} are not permitted.
     * </ul>
     * 
     * @return a newly created  session
     *  
     * @exception JMSException if the {@code Connection} object fails
     *                         to create a session due to 
     *                         <ul>
     *                         <li>some internal error, 
     *                         <li>lack of support for the specific transaction and acknowledgement mode, or
     *                         <li>because this method is being called in a Java EE web or EJB application 
     *                         and an active session already exists for this connection.
     *                         </ul>
     * @since JMS 2.0
     *
     * @see Session#SESSION_TRANSACTED 
     * @see Session#AUTO_ACKNOWLEDGE 
     * @see Session#CLIENT_ACKNOWLEDGE 
     * @see Session#DUPS_OK_ACKNOWLEDGE 
     * 
     * @see javax.jms.Connection#createSession(boolean, int) 
     * @see javax.jms.Connection#createSession() 
     */   
    Session createSession(int sessionMode) throws JMSException;
                       
    /** 
     * Creates a {@code Session} object, 
     * specifying no arguments.
     * <p>
     * The behaviour of the session that is created depends on 
     * whether this method is called in a Java SE environment, 
     * in the Java EE application client container, or in the Java EE web or EJB container.
     * If this method is called in the Java EE web or EJB container then the 
     * behaviour of the session also depends on whether or not 
     * there is an active JTA transaction in progress.   
     * <p>
     * In a <b>Java SE environment</b> or in <b>the Java EE application client container</b>:
     * <ul>
     * <li>The session will be non-transacted and received messages will be acknowledged automatically
     * using an acknowledgement mode of {@code Session.AUTO_ACKNOWLEDGE} 
     * For a definition of the meaning of this acknowledgement mode see the link below.
     * </ul>
     * <p>
     * In a <b>Java EE web or EJB container, when there is an active JTA transaction in progress</b>:
     * <ul>
     * <li>The session will participate in the JTA transaction and will be committed or rolled back
     * when that transaction is committed or rolled back, 
     * not by calling the session's {@code commit} or {@code rollback} methods.
     * </ul>
     * <p>
     * In the <b>Java EE web or EJB container, when there is no active JTA transaction in progress</b>:
     * <ul>
     * <li>The session will be non-transacted and received messages will be acknowledged automatically
     * using an acknowledgement mode of {@code Session.AUTO_ACKNOWLEDGE} 
     * For a definition of the meaning of this acknowledgement mode see the link below.
     * </ul> 
     * <p>
     * Applications running in the Java EE web and EJB containers must not attempt 
     * to create more than one active (not closed) {@code Session} object per connection. 
     * If this method is called in a Java EE web or EJB container when an active
     * {@code Session} object already exists for this connection then a {@code JMSException} will be thrown.
     * 
     * @return a newly created  session
     *  
     * @exception JMSException if the {@code Connection} object fails
     *                         to create a session due to 
     *                         <ul>
     *                         <li>some internal error or  
     *                         <li>because this method is being called in a Java EE web or EJB application 
     *                         and an active session already exists for this connection.
     *                         </ul>
     *                       
     * @since JMS 2.0
     *
     * @see Session#AUTO_ACKNOWLEDGE 
     * 
     * @see javax.jms.Connection#createSession(boolean, int) 
     * @see javax.jms.Connection#createSession(int) 
     */ 

    Session createSession() throws JMSException;    
    
    
    /** Gets the client identifier for this connection.
      *  
      * <P>This value is specific to the JMS provider.  It is either preconfigured 
      * by an administrator in a {@code ConnectionFactory} object
      * or assigned dynamically by the application by calling the
      * {@code setClientID} method.
      * 
      * 
      * @return the unique client identifier
      *  
      * @exception JMSException if the JMS provider fails to return
      *                         the client ID for this connection due
      *                         to some internal error.
      *
      **/
    String
    getClientID() throws JMSException;


    /** Sets the client identifier for this connection.
     *  
     * <P>The preferred way to assign a JMS client's client identifier is for
     * it to be configured in a client-specific {@code ConnectionFactory}
     * object and transparently assigned to the {@code Connection} object
     * it creates.
     * 
     * <P>Alternatively, a client can set a connection's client identifier
     * using a provider-specific value. The facility to set a connection's
     * client identifier explicitly is not a mechanism for overriding the
     * identifier that has been administratively configured. It is provided
     * for the case where no administratively specified identifier exists.
     * If one does exist, an attempt to change it by setting it must throw an
     * {@code IllegalStateException}. If a client sets the client identifier
     * explicitly, it must do so immediately after it creates the connection 
     * and before any other
     * action on the connection is taken. After this point, setting the
     * client identifier is a programming error that should throw an
     * {@code IllegalStateException}.
     *
     * <P>The purpose of the client identifier is to associate a connection and
     * its objects with a state maintained on behalf of the client by a 
     * provider. The only such state identified by the JMS API is that required
     * to support durable subscriptions.
     *
     * <P>If another connection with the same {@code clientID} is already running when
     * this method is called, the JMS provider should detect the duplicate ID and throw
     * an {@code InvalidClientIDException}.
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a {@code JMSException} to be thrown though this is not guaranteed.
     * 
     * @param clientID the unique client identifier
     * 
     * @exception JMSException if the JMS provider fails to set the client ID for the the connection
     *                         for one of the following reasons:
     *                         <ul>
     *                         <li>an internal error has occurred or  
     *                         <li>this method has been called in a Java EE web or EJB application 
     *                         (though it is not guaranteed that an exception is thrown in this case)
     *                         </ul>  
     * @exception InvalidClientIDException if the JMS client specifies an
     *                         invalid or duplicate client ID.
     * @exception IllegalStateException if the JMS client attempts to set
     *       a connection's client ID at the wrong time or
     *       when it has been administratively configured.
     */
    void setClientID(String clientID) throws JMSException;

 
    /** Gets the metadata for this connection.
      *  
      * @return the connection metadata
      *  
      * @exception JMSException if the JMS provider fails to
      *                         get the connection metadata for this connection.
      *
      * @see javax.jms.ConnectionMetaData
      */

    ConnectionMetaData
    getMetaData() throws JMSException;

    /**
     * Gets the {@code ExceptionListener} object for this connection. 
     * Not every {@code Connection} has an {@code ExceptionListener}
     * associated with it.
     *
     * @return the {@code ExceptionListener} for this connection, or null. 
     *              if no {@code ExceptionListener} is associated
     *              with this connection.
     *
     * @exception JMSException if the JMS provider fails to
     *                         get the {@code ExceptionListener} for this 
     *                         connection. 
     * @see javax.jms.Connection#setExceptionListener
     */

    ExceptionListener 
    getExceptionListener() throws JMSException;


    /** Sets an exception listener for this connection.
      *
      * <P>If a JMS provider detects a serious problem with a connection, it
      * informs the connection's {@code ExceptionListener}, if one has been
      * registered. It does this by calling the listener's
      * {@code onException} method, passing it a {@code JMSException}
      * object describing the problem.
      *
      * <P>An exception listener allows a client to be notified of a problem
      * asynchronously.
      * Some connections only consume messages, so they would have no other 
      * way to learn their connection has failed.
      *
      * <P>A connection serializes execution of its
      * {@code ExceptionListener}.
      *
      * <P>A JMS provider should attempt to resolve connection problems 
      * itself before it notifies the client of them.
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a {@code JMSException} to be thrown though this is not guaranteed.
     * 
      * @param listener the exception listener
      *
     * @exception JMSException if the JMS provider fails to set the exception listener
     *                         for one of the following reasons:
     *                         <ul>
     *                         <li>an internal error has occurred or  
     *                         <li>this method has been called in a Java EE web or EJB application 
     *                         (though it is not guaranteed that an exception is thrown in this case)
     *                         </ul> 
      *
      *
      */

    void 
    setExceptionListener(ExceptionListener listener) throws JMSException;

    /** Starts (or restarts) a connection's delivery of incoming messages.
      * A call to {@code start} on a connection that has already been
      * started is ignored.
      * 
      * @exception JMSException if the JMS provider fails to start
      *                         message delivery due to some internal error.
      *
      * @see javax.jms.Connection#stop
      */

    void
    start() throws JMSException;

 
    /**
	 * Temporarily stops a connection's delivery of incoming messages. Delivery
	 * can be restarted using the connection's {@code start} method. When
	 * the connection is stopped, delivery to all the connection's message
	 * consumers is inhibited: synchronous receives block, and messages are not
	 * delivered to message listeners.
	 * 
	 * <P>
	 * This call blocks until receives and/or message listeners in progress have
	 * completed.
	 * 
	 * <P>
	 * Stopping a connection has no effect on its ability to send messages. A
	 * call to {@code stop} on a connection that has already been stopped
	 * is ignored.
	 * 
	 * <P>
	 * A call to {@code stop} must not return until delivery of messages
	 * has paused. This means that a client can rely on the fact that none of
	 * its message listeners will be called and that all threads of control
	 * waiting for {@code receive} calls to return will not return with a
	 * message until the connection is restarted. The receive timers for a
	 * stopped connection continue to advance, so receives may time out while
	 * the connection is stopped.
	 * 
	 * <P>
	 * If message listeners are running when {@code stop} is invoked, the
	 * {@code stop} call must wait until all of them have returned before
	 * it may return. While these message listeners are completing, they must
	 * have the full services of the connection available to them.
	 * <p>
	 * A message listener must not attempt to stop its own connection as this
	 * would lead to deadlock. The JMS provider must detect this and throw a
	 * <tt>IllegalStateException</tt>.
	 * <p>
	 * For the avoidance of doubt, if an exception listener for this connection
	 * is running when {@code stop} is invoked, there is no requirement for
	 * the {@code stop} call to wait until the exception listener has
	 * returned before it may return.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a {@code JMSException} to be thrown though this is not
	 * guaranteed.
	 * 
	 * @exception IllegalStateException
	 *                this method has been called by a <tt>MessageListener</tt>
	 *                on its own <tt>Connection</tt>
	 * @exception JMSException
	 *                if the JMS provider fails to stop message delivery for one
	 *                of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred or <li>this method has
	 *                been called in a Java EE web or EJB application (though it
	 *                is not guaranteed that an exception is thrown in this
	 *                case)
	 *                </ul>
	 * 
	 * @see javax.jms.Connection#start
	 */

    void
    stop() throws JMSException;

 
    /** Closes the connection.
      *
      * <P>Since a provider typically allocates significant resources outside 
      * the JVM on behalf of a connection, clients should close these resources
      * when they are not needed. Relying on garbage collection to eventually 
      * reclaim these resources may not be timely enough.
      *
      * <P>There is no need to close the sessions, producers, and consumers
      * of a closed connection.
      *
      * <P>Closing a connection causes all temporary destinations to be
      * deleted.
      *
      * <P>When this method is invoked, it should not return until message
      * processing has been shut down in an orderly fashion. This means that all
      * message 
      * listeners that may have been running have returned, and that all pending 
      * receives have returned. A close terminates all pending message receives 
      * on the connection's sessions' consumers. The receives may return with a 
      * message or with null, depending on whether there was a message available 
      * at the time of the close. If one or more of the connection's sessions' 
      * message listeners is processing a message at the time when connection 
      * {@code close} is invoked, all the facilities of the connection and 
      * its sessions must remain available to those listeners until they return 
      * control to the JMS provider. 
	 * <p>
	 * This method must not return until any incomplete asynchronous send
	 * operations for this <tt>Connection</tt> have been completed and any
	 * <tt>CompletionListener</tt> callbacks have returned. Incomplete sends
	 * should be allowed to complete normally unless an error occurs.
	 * <p>
      * For the avoidance of doubt, if an exception listener for this connection 
      * is running when {@code close} is invoked, there is no requirement for 
      * the {@code close} call to wait until the exception listener has returned
      * before it may return. 
      * 
      * <P>Closing a connection causes any of its sessions' transactions
      * in progress to be rolled back. In the case where a session's
      * work is coordinated by an external transaction manager, a session's 
      * {@code commit} and {@code rollback} methods are
      * not used and the result of a closed session's work is determined
      * later by the transaction manager.
      * Closing a connection does NOT force an 
      * acknowledgment of client-acknowledged sessions.  
      * <p>
      * A message listener must not attempt to close its own connection as this 
      * would lead to deadlock. The JMS provider must detect this and throw a 
      * <tt>IllegalStateException</tt>.
	  * <p>
 	  * A <tt>CompletionListener</tt> callback method must not call
 	  * <tt>close</tt> on its own <tt>Connection</tt>. Doing so will cause an
  	  * <tt>IllegalStateException</tt> to be thrown.
	  * <p>
      * Invoking the {@code acknowledge} method of a received message 
      * from a closed connection's session must throw an 
      * {@code IllegalStateException}.  Closing a closed connection must 
      * NOT throw an exception.
      * 
	  * @exception IllegalStateException
	  *                <ul>
	  *                <li>this method has been called by a <tt>MessageListener
	  *                </tt> on its own <tt>Connection</tt></li> 
	  *                <li>this method has
	  *                been called by a <tt>CompletionListener</tt> callback
	  *                method on its own <tt>Connection</tt></li>
	  *                </ul>
      * @exception JMSException if the JMS provider fails to close the
      *                         connection due to some internal error. For 
      *                         example, a failure to release resources
      *                         or to close a socket connection can cause
      *                         this exception to be thrown.
      *                         
      */

    void 
    close() throws JMSException; 
    
	/**
	 * Creates a connection consumer for this connection (optional operation)
	 * on the specific destination.
	 * <p>
	 * This is an expert facility not used by ordinary JMS clients.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a {@code JMSException} to be thrown though this is not
	 * guaranteed.
	 * 
	 * @param destination
	 *            the destination to access
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the message
	 *            consumer.
	 * @param sessionPool
	 *            the server session pool to associate with this connection
	 *            consumer
	 * @param maxMessages
	 *            the maximum number of messages that can be assigned to a
	 *            server session at one time
	 * 
	 * @return the connection consumer
	 * 
	 * @exception InvalidDestinationException
	 *                if an invalid destination is specified.
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid.
	 * @exception JMSException
	 *                if the {@code Connection} object fails to create a
	 *                connection consumer for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred 
	 *                <li>invalid arguments for {@code sessionPool} and 
	 *                {@code messageSelector} or 
	 *                <li>this method has been called in a Java EE web or EJB
	 *                application (though it is not guaranteed that an exception
	 *                is thrown in this case)
	 *                </ul>
	 * 
	 * @since JMS 1.1
	 * 
	 * @see javax.jms.ConnectionConsumer
	 */
	ConnectionConsumer createConnectionConsumer(Destination destination,
			String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException;
	
	/**
	 * Creates a connection consumer for this connection (optional operation)
	 * on the specific topic using a shared non-durable subscription with
	 * the specified name.
	 * <p>
	 * This is an expert facility not used by ordinary JMS clients.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a {@code JMSException} to be thrown though this is not
	 * guaranteed.
	 * 
	 * @param topic
	 *            the topic to access
	 * @param subscriptionName
	 *            the name used to identify the shared non-durable subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the message
	 *            consumer.
	 * @param sessionPool
	 *            the server session pool to associate with this connection
	 *            consumer
	 * @param maxMessages
	 *            the maximum number of messages that can be assigned to a
	 *            server session at one time
	 * 
	 * @return the connection consumer
	 * 
	 * @exception IllegalStateException
	 *                if called on a {@code QueueConnection}
	 * @exception InvalidDestinationException
	 *                if an invalid destination is specified.
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid.
	 * @exception JMSException
	 *                if the {@code Connection} object fails to create a
	 *                connection consumer for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred 
	 *                <li>invalid arguments for {@code sessionPool} and 
	 *                {@code messageSelector} or 
	 *                <li>this method has been called in a Java EE web or EJB
	 *                application (though it is not guaranteed that an exception
	 *                is thrown in this case)
	 *                </ul>
	 * 
	 * @since JMS 2.0
	 * 
	 * @see javax.jms.ConnectionConsumer
	 */
	ConnectionConsumer createSharedConnectionConsumer(Topic topic,
			String subscriptionName,
			String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException;


	/**
	 * Creates a connection consumer for this connection (optional operation)
	 * on the specific topic using an unshared durable subscription with
	 * the specified name.
	 * <p>
	 * This is an expert facility not used by ordinary JMS clients.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a {@code JMSException} to be thrown though this is not
	 * guaranteed.
	 * 
	 * @param topic
	 *            topic to access
	 * @param subscriptionName
	 *            the name used to identify the unshared durable subscription
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the message
	 *            consumer.
	 * @param sessionPool
	 *            the server session pool to associate with this durable
	 *            connection consumer
	 * @param maxMessages
	 *            the maximum number of messages that can be assigned to a
	 *            server session at one time
	 * 
	 * @return the durable connection consumer
	 * 
	 * @exception IllegalStateException
	 *                if called on a {@code QueueConnection}
	 * @exception InvalidDestinationException
	 *                if an invalid destination is specified.
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid.
	 * @exception JMSException
	 *                if the {@code Connection} object fails to create a
	 *                connection consumer for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred 
	 *                <li>invalid arguments
	 *                for {@code sessionPool} and {@code messageSelector} or 
	 *                <li>this method has been called in a Java EE web or EJB
	 *                application (though it is not guaranteed that an exception
	 *                is thrown in this case)
	 *                </ul>
	 * @since JMS 1.1
	 * 
	 * @see javax.jms.ConnectionConsumer
	 */
	ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException;
	
	/**
	 * Creates a connection consumer for this connection (optional operation)
	 * on the specific topic using a shared durable subscription with
	 * the specified name.
	 * <p>
	 * This is an expert facility not used by ordinary JMS clients.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a {@code JMSException} to be thrown though this is not
	 * guaranteed.
	 * 
	 * @param topic
	 *            topic to access
	 * @param subscriptionName
	 *            the name used to identify the shared durable subscription 
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the message
	 *            consumer.
	 * @param sessionPool
	 *            the server session pool to associate with this durable
	 *            connection consumer
	 * @param maxMessages
	 *            the maximum number of messages that can be assigned to a
	 *            server session at one time
	 * 
	 * @return the durable connection consumer
	 * 
	 * @exception IllegalStateException
	 *                if called on a {@code QueueConnection}
	 * @exception InvalidDestinationException
	 *                if an invalid destination is specified.
	 * @exception InvalidSelectorException
	 *                if the message selector is invalid.
	 * @exception JMSException
	 *                if the {@code Connection} object fails to create a
	 *                connection consumer for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred 
	 *                <li>invalid arguments
	 *                for {@code sessionPool} and {@code messageSelector} or 
	 *                <li>this method has been called in a Java EE web or EJB
	 *                application (though it is not guaranteed that an exception
	 *                is thrown in this case)
	 *                </ul>
	 * @since JMS 2.0
	 * 
	 * @see javax.jms.ConnectionConsumer
	 */
	ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException;
          
}

