/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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

/**
 * A <code>JMSContext</code> is the main interface in the simplified JMS API
 * introduced for JMS 2.0. This combines in a single object the functionality of
 * two separate objects from the JMS 1.1 API: a <code>Connection</code> and a
 * <code>Session</code>.
 * <p>
 * When an application needs to send messages it use the
 * <code>createProducer</code> method to create a <code>JMSProducer</code> which
 * provides methods to configure and send messages. Messages may be sent either
 * synchronously or asynchronously.
 * <p>
 * When an application needs to receive messages it uses one of several
 * <code>createConsumer</code> or <code>createDurableConsumer</code> methods to
 * create a <code>JMSConsumer</code> . A <code>JMSConsumer</code> provides
 * methods to receive messages either synchronously or asynchronously.
 * <p>
 * In terms of the JMS 1.1 API a <code>JMSContext</code> should be thought of as
 * representing both a <code>Connection</code> and a <code>Session</code>.
 * Although the simplified API removes the need for applications to use those
 * objects, the concepts of connection and session remain important. A
 * connection represents a physical link to the JMS server and a session
 * represents a single-threaded context for sending and receiving messages.
 * <p>
 * A <code>JMSContext</code> may be created by calling one of several
 * <code>createContext</code> methods on a <code>ConnectionFactory</code>. A
 * <code>JMSContext</code> that is created in this way is described as being
 * <i>application-managed</i>. An application-managed <code>JMSContext</code>
 * must be closed when no longer needed by calling its <code>close</code>
 * method.
 * <p>
 * Applications running in the Java EE web and EJB containers may alternatively
 * inject a <code>JMSContext</code> into their application using the
 * <code>@Inject</code> annotation. A <code>JMSContext</code> that is created in
 * this way is described as being <i>container-managed</i>. An
 * application-managed <code>JMSContext</code> will be closed automatically by
 * the container. Applications must not call its <code>close</code> method.
 * <p>
 * Applications running in the Java EE web and EJB containers are not permitted
 * to create more than one active session on a connection so combining them in a
 * single object takes advantage of this restriction to offer a simpler API.
 * <p>
 * However applications running in a Java SE environment or in the Java EE
 * application client container are permitted to create multiple active sessions
 * on the same connection. This allows the same physical connection to be used
 * in multiple threads simultaneously. Such applications which require multiple
 * sessions to be created on the same connection should use one of the
 * <code>createContext</code> methods on the <code>ConnectionFactory</code> to
 * create the first <code>JMSContext</code> and then use the
 * <code>createContext</code> method on <code>JMSContext</code> to create
 * additional <code>JMSContext</code> objects that use the same connection. All
 * these <code>JMSContext</code> objects are application-managed and must be
 * closed when no longer needed by calling their <code>close</code> method.
 * 
 * @version 2.0
 * @since 2.0
 * 
 */
public interface JMSContext {

	/**
	 * Creates a new <code>JMSContext</code> with the specified session mode
	 * using the same connection as this <code>JMSContext</code> and creating a
	 * new session.
	 * <p>
	 * This method does not start the connection. If the connection has not
	 * already been started then it will be automatically started when a
	 * <code>JMSConsumer</code> is created on any of the <code>JMSContext</code>
	 * objects for that connection.
	 * <p>
	 * <ul>
	 * <li>If <code>sessionMode</code> is set to
	 * <code>JMSContext.SESSION_TRANSACTED</code> then the session will use a
	 * local transaction which may subsequently be committed or rolled back by
	 * calling the <code>JMSContext</code>'s <code>commit</code> or
	 * <code>rollback</code> methods.
	 * <li>If <code>sessionMode</code> is set to any of
	 * <code>JMSContext.CLIENT_ACKNOWLEDGE</code>,
	 * <code>JMSContext.AUTO_ACKNOWLEDGE</code> or
	 * <code>JMSContext.DUPS_OK_ACKNOWLEDGE</code>. then the session will be
	 * non-transacted and messages received by this session will be acknowledged
	 * according to the value of <code>sessionMode</code>. For a definition of
	 * the meaning of these acknowledgement modes see the links below.
	 * </ul>
	 * <p>
	 * This method must not be used by applications running in the Java EE web
	 * or EJB containers because doing so would violate the restriction that
	 * such an application must not attempt to create more than one active (not
	 * closed) <code>Session</code> object per connection. If this method is
	 * called in a Java EE web or EJB container then a
	 * <code>JMSRuntimeException</code> will be thrown.
	 * 
	 * @param sessionMode
	 *            indicates which of four possible session modes will be used.
	 *            The permitted values are
	 *            <code>JMSContext.SESSION_TRANSACTED</code>,
	 *            <code>JMSContext.CLIENT_ACKNOWLEDGE</code>,
	 *            <code>JMSContext.AUTO_ACKNOWLEDGE</code> and
	 *            <code>JMSContext.DUPS_OK_ACKNOWLEDGE</code>.
	 * 
	 * @return a newly created JMSContext
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create the JMSContext due to
	 *                <ul>
	 *                <li>some internal error or <li>because this method is
	 *                being called in a Java EE web or EJB application.
	 *                </ul>
	 * @since 2.0
	 * 
	 * @see JMSContext#SESSION_TRANSACTED
	 * @see JMSContext#CLIENT_ACKNOWLEDGE
	 * @see JMSContext#AUTO_ACKNOWLEDGE
	 * @see JMSContext#DUPS_OK_ACKNOWLEDGE
	 * 
	 * @see javax.jms.ConnectionFactory#createContext()
	 * @see javax.jms.ConnectionFactory#createContext(int)
	 * @see javax.jms.ConnectionFactory#createContext(java.lang.String,
	 *      java.lang.String)
	 * @see javax.jms.ConnectionFactory#createContext(java.lang.String,
	 *      java.lang.String, int)
	 * @see javax.jms.JMSContext#createContext(int)
	 */
	JMSContext createContext(int sessionMode);

	/**
	 * Creates a new <code>JMSProducer</code> object which can be used to
	 * configure and send messages
	 * 
	 * @return A new <code>JMSProducer</code> object
	 * 
	 * @see javax.jms.JMSProducer
	 */
	JMSProducer createProducer();

	/**
	 * Gets the client identifier for the JMSContext's connection.
	 * 
	 * <P>
	 * This value is specific to the JMS provider. It is either preconfigured by
	 * an administrator in a <CODE>ConnectionFactory</CODE> object or assigned
	 * dynamically by the application by calling the <code>setClientID</code>
	 * method.
	 * 
	 * @return the unique client identifier
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to return the client ID for the
	 *                JMSContext's connection due to some internal error.
	 * 
	 **/
	String getClientID();

	/**
	 * Sets the client identifier for the JMSContext's connection.
	 * 
	 * <P>
	 * The preferred way to assign a JMS client's client identifier is for it to
	 * be configured in a client-specific <CODE>ConnectionFactory</CODE> object
	 * and transparently assigned to the <CODE>Connection</CODE> object it
	 * creates.
	 * 
	 * <P>
	 * Alternatively, a client can set the client identifier for the
	 * MessageContext's connection using a provider-specific value. The facility
	 * to set its client identifier explicitly is not a mechanism for overriding
	 * the identifier that has been administratively configured. It is provided
	 * for the case where no administratively specified identifier exists. If
	 * one does exist, an attempt to change it by setting it must throw an
	 * <CODE>IllegalStateException</CODE>. If a client sets the client
	 * identifier explicitly, it must do so immediately after it creates the
	 * JMSContext and before any other action on the JMSContext is taken. After
	 * this point, setting the client identifier is a programming error that
	 * should throw an <CODE>IllegalStateException</CODE>.
	 * 
	 * <P>
	 * The purpose of the client identifier is to associate the JMSContext's
	 * connection and its objects with a state maintained on behalf of the
	 * client by a provider. The only such state identified by the JMS API is
	 * that required to support durable subscriptions.
	 * 
	 * <P>
	 * If another connection with the same <code>clientID</code> is already
	 * running when this method is called, the JMS provider should detect the
	 * duplicate ID and throw an <CODE>InvalidClientIDException</CODE>.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a <code>JMSRuntimeException</code> to be thrown though this
	 * is not guaranteed.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @param clientID
	 *            the unique client identifier
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to set the client ID for the the
	 *                JMSContext's connection for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred or <li>this method has
	 *                been called in a Java EE web or EJB application (though it
	 *                is not guaranteed that an exception is thrown in this
	 *                case) <li>the <code>JMSContext</code> is container-managed
	 *                (injected).
	 *                </ul>
	 * 
	 * @throws InvalidClientIDRuntimeException
	 *             if the JMS client specifies an invalid or duplicate client
	 *             ID.
	 * @throws IllegalStateRuntimeException
	 *             if the JMS client attempts to set the client ID for the
	 *             JMSContext's connection at the wrong time or when it has been
	 *             administratively configured.
	 */

	void setClientID(String clientID);

	/**
	 * Gets the connection metadata for the JMSContext's connection.
	 * 
	 * @return the connection metadata
	 * 
	 * @throws JMSRuntimeException
	 *             if the JMS provider fails to get the connection metadata
	 * 
	 * @see javax.jms.ConnectionMetaData
	 */

	ConnectionMetaData getMetaData();

	/**
	 * Gets the <CODE>ExceptionListener</CODE> object for the JMSContext's
	 * connection. Not every <CODE>Connection</CODE> has an
	 * <CODE>ExceptionListener</CODE> associated with it.
	 * 
	 * @return the <CODE>ExceptionListener</CODE> for the JMSContext's
	 *         connection, or null if no <CODE>ExceptionListener</CODE> is
	 *         associated with that connection.
	 * 
	 * @throws JMSRuntimeException
	 *             if the JMS provider fails to get the
	 *             <CODE>ExceptionListener</CODE> for the JMSContext's
	 *             connection.
	 * @see javax.jms.Connection#setExceptionListener
	 */

	ExceptionListener getExceptionListener();

	/**
	 * Sets an exception listener for the JMSContext's connection.
	 * 
	 * <P>
	 * If a JMS provider detects a serious problem with a connection, it informs
	 * the connection's <CODE>ExceptionListener</CODE>, if one has been
	 * registered. It does this by calling the listener's
	 * <CODE>onException</CODE> method, passing it a <CODE>JMSException</CODE>
	 * object describing the problem.
	 * 
	 * <P>
	 * An exception listener allows a client to be notified of a problem
	 * asynchronously. Some connections only consume messages, so they would
	 * have no other way to learn their connection has failed.
	 * 
	 * <P>
	 * A connection serializes execution of its <CODE>ExceptionListener</CODE>.
	 * 
	 * <P>
	 * A JMS provider should attempt to resolve connection problems itself
	 * before it notifies the client of them.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a <code>JMSRuntimeException</code> to be thrown though this
	 * is not guaranteed.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @param listener
	 *            the exception listener
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to set the exception listener
	 *                for one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred or <li>this method has
	 *                been called in a Java EE web or EJB application (though it
	 *                is not guaranteed that an exception is thrown in this
	 *                case) <li>the <code>JMSContext</code> is container-managed
	 *                (injected).
	 *                </ul>
	 */
	void setExceptionListener(ExceptionListener listener);

	/**
	 * Starts (or restarts) delivery of incoming messages by the JMSContext's
	 * connection. A call to <CODE>start</CODE> on a connection that has already
	 * been started is ignored.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to start message delivery due to
	 *                some internal error.
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to start message delivery due to
	 *                one of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred or <li>the <code>
	 *                JMSContext</code> is container-managed (injected).
	 *                </ul>
	 * 
	 * @see javax.jms.JMSContext#stop
	 */
	void start();

	/**
	 * Temporarily stops the delivery of incoming messages by the JMSContext's
	 * connection. Delivery can be restarted using the <CODE>start</CODE>
	 * method. When the connection is stopped, delivery to all the connection's
	 * message consumers is inhibited: synchronous receives block, and messages
	 * are not delivered to message listeners.
	 * 
	 * <P>
	 * This call blocks until receives and/or message listeners in progress have
	 * completed.
	 * 
	 * <P>
	 * Stopping a connection has no effect on its ability to send messages. A
	 * call to <CODE>stop</CODE> on a connection that has already been stopped
	 * is ignored.
	 * 
	 * <P>
	 * A call to <CODE>stop</CODE> must not return until delivery of messages
	 * has paused. This means that a client can rely on the fact that none of
	 * its message listeners will be called and that all threads of control
	 * waiting for <CODE>receive</CODE> calls to return will not return with a
	 * message until the connection is restarted. The receive timers for a
	 * stopped connection continue to advance, so receives may time out while
	 * the connection is stopped.
	 * 
	 * <P>
	 * If message listeners are running when <CODE>stop</CODE> is invoked, the
	 * <CODE>stop</CODE> call must wait until all of them have returned before
	 * it may return. While these message listeners are completing, they must
	 * have the full services of the connection available to them.
	 * <p>
	 * A message listener must not attempt to stop its own JMSContext as this
	 * would lead to deadlock. The JMS provider must detect this and throw a
	 * javax.jms.IllegalStateRuntimeException.
	 * <p>
	 * For the avoidance of doubt, if an exception listener for the JMSContext's
	 * connection is running when <code>stop</code> is invoked, there is no
	 * requirement for the <code>stop</code> call to wait until the exception
	 * listener has returned before it may return.
	 * <p>
	 * This method must not be used in a Java EE web or EJB application. Doing
	 * so may cause a <code>JMSRuntimeException</code> to be thrown though this
	 * is not guaranteed.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to stop message delivery for one
	 *                of the following reasons:
	 *                <ul>
	 *                <li>an internal error has occurred or <li>this method has
	 *                been called in a Java EE web or EJB application (though it
	 *                is not guaranteed that an exception is thrown in this
	 *                case) <li>the <code>JMSContext</code> is container-managed
	 *                (injected)
	 *                </ul>
	 * 
	 * @see javax.jms.JMSContext#start
	 */
	void stop();

	/**
	 * Specifies whether the underlying connection used by this
	 * <code>JMSContext</code> will be started automatically when a consumer is
	 * created. This is the default behaviour, and it may be disabled by calling
	 * this method with a value of <code>false</code>.
	 * <p>
	 * This method does not itself either start or stop the connection.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @param autoStart
	 *            Whether the underlying connection used by this
	 *            <code>JMSContext</code> will be automatically started when a
	 *            consumer is created.
	 * @exception JMSRuntimeException
	 *                the <code>JMSContext</code> is container-managed
	 *                (injected)
	 * 
	 * @see javax.jms.JMSContext#getAutoStart
	 */
	public void setAutoStart(boolean autoStart);

	/**
	 * Returns whether the underlying connection used by this
	 * <code>JMSContext</code> will be started automatically when a consumer is
	 * created.
	 * 
	 * @return whether the underlying connection used by this
	 *         <code>JMSContext</code> will be started automatically when a
	 *         consumer is created.
	 * 
	 * @see javax.jms.JMSContext#setAutoStart
	 */
	public boolean getAutoStart();

	/**
	 * Closes the JMSContext
	 * <p>
	 * This closes the underlying session and any underlying producers and
	 * consumers. If there are no other active (not closed) JMSContext objects
	 * using the underlying connection then this method also closes the
	 * underlying connection.
	 * 
	 * <P>
	 * Since a provider typically allocates significant resources outside the
	 * JVM on behalf of a connection, clients should close these resources when
	 * they are not needed. Relying on garbage collection to eventually reclaim
	 * these resources may not be timely enough.
	 * 
	 * <P>
	 * Closing a connection causes all temporary destinations to be deleted.
	 * 
	 * <P>
	 * When this method is invoked, it should not return until message
	 * processing has been shut down in an orderly fashion. This means that all
	 * message listeners that may have been running have returned, and that all
	 * pending receives have returned. A close terminates all pending message
	 * receives on the connection's sessions' consumers. The receives may return
	 * with a message or with null, depending on whether there was a message
	 * available at the time of the close. If one or more of the connection's
	 * sessions' message listeners is processing a message at the time when
	 * connection <CODE>close</CODE> is invoked, all the facilities of the
	 * connection and its sessions must remain available to those listeners
	 * until they return control to the JMS provider.
	 * <p>
	 * A message listener must not attempt to close its own JMSContext as this
	 * would lead to deadlock. The JMS provider must detect this and throw a
	 * javax.jms.IllegalStateRuntimeException.
	 * <p>
	 * For the avoidance of doubt, if an exception listener for the JMSContext's
	 * connection is running when <code>close</code> is invoked, there is no
	 * requirement for the <code>close</code> call to wait until the exception
	 * listener has returned before it may return.
	 * <P>
	 * Closing a connection causes any of its sessions' transactions in progress
	 * to be rolled back. In the case where a session's work is coordinated by
	 * an external transaction manager, a session's <CODE>commit</CODE> and
	 * <CODE>rollback</CODE> methods are not used and the result of a closed
	 * session's work is determined later by the transaction manager.
	 * <p>
	 * Closing a connection does NOT force an acknowledgment of
	 * client-acknowledged sessions.
	 * 
	 * <P>
	 * Invoking the <CODE>acknowledge</CODE> method of a received message from a
	 * closed connection's session must throw an
	 * <CODE>IllegalStateException</CODE>. Closing a closed connection must NOT
	 * throw an exception.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to close the
	 *                <code>JMSContext</code> for one of the following reasons:
	 *                <ul>
	 *                <li> an internal error has occurred. For example, a
	 *                failure to release resources or to close a socket
	 *                connection can cause this exception to be thrown. <li> the
	 *                <code>JMSContext</code> is container-managed (injected)
	 *                </ul>
	 */
	void close();

	/**
	 * With this session mode, the JMSContext's session automatically
	 * acknowledges a client's receipt of a message either when the session has
	 * successfully returned from a call to <CODE>receive</CODE> or when the
	 * message listener the session has called to process the message
	 * successfully returns.
	 */

	static final int AUTO_ACKNOWLEDGE = Session.AUTO_ACKNOWLEDGE;

	/**
	 * With this session mode, the client acknowledges a consumed message by
	 * calling the message's <CODE>acknowledge</CODE> method. Acknowledging a
	 * consumed message acknowledges all messages that the session has consumed.
	 * 
	 * <P>
	 * When this session mode is used, a client may build up a large number of
	 * unacknowledged messages while attempting to process them. A JMS provider
	 * should provide administrators with a way to limit client overrun so that
	 * clients are not driven to resource exhaustion and ensuing failure when
	 * some resource they are using is temporarily blocked.
	 * 
	 * @see javax.jms.Message#acknowledge()
	 */

	static final int CLIENT_ACKNOWLEDGE = Session.CLIENT_ACKNOWLEDGE;

	/**
	 * This session mode instructs the JMSContext's session to lazily
	 * acknowledge the delivery of messages. This is likely to result in the
	 * delivery of some duplicate messages if the JMS provider fails, so it
	 * should only be used by consumers that can tolerate duplicate messages.
	 * Use of this mode can reduce session overhead by minimizing the work the
	 * session does to prevent duplicates.
	 */

	static final int DUPS_OK_ACKNOWLEDGE = Session.DUPS_OK_ACKNOWLEDGE;

	/**
	 * This session mode instructs the JMSContext's session to deliver and
	 * consume messages in a local transaction which will be subsequently
	 * committed by calling <CODE>commit</CODE> or rolled back by calling
	 * <CODE>rollback</CODE>.
	 */
	static final int SESSION_TRANSACTED = Session.SESSION_TRANSACTED;

	/**
	 * Creates a <CODE>BytesMessage</CODE> object. A <CODE>BytesMessage</CODE>
	 * object is used to send a message containing a stream of uninterpreted
	 * bytes.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	BytesMessage createBytesMessage();

	/**
	 * Creates a <CODE>MapMessage</CODE> object. A <CODE>MapMessage</CODE>
	 * object is used to send a self-defining set of name-value pairs, where
	 * names are <CODE>String</CODE> objects and values are primitive values in
	 * the Java programming language.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	MapMessage createMapMessage();

	/**
	 * Creates a <CODE>Message</CODE> object. The <CODE>Message</CODE> interface
	 * is the root interface of all JMS messages. A <CODE>Message</CODE> object
	 * holds all the standard message header information. It can be sent when a
	 * message containing only header information is sufficient.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	Message createMessage();

	/**
	 * Creates an <CODE>ObjectMessage</CODE> object. An
	 * <CODE>ObjectMessage</CODE> object is used to send a message that contains
	 * a serializable Java object.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	ObjectMessage createObjectMessage();

	/**
	 * Creates an initialized <CODE>ObjectMessage</CODE> object. An
	 * <CODE>ObjectMessage</CODE> object is used to send a message that contains
	 * a serializable Java object.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @param object
	 *            the object to use to initialize this message
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	ObjectMessage createObjectMessage(Serializable object);

	/**
	 * Creates a <CODE>StreamMessage</CODE> object. A <CODE>StreamMessage</CODE>
	 * object is used to send a self-defining stream of primitive values in the
	 * Java programming language.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	StreamMessage createStreamMessage();

	/**
	 * Creates a <CODE>TextMessage</CODE> object. A <CODE>TextMessage</CODE>
	 * object is used to send a message containing a <CODE>String</CODE> object.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	TextMessage createTextMessage();

	/**
	 * Creates an initialized <CODE>TextMessage</CODE> object. A
	 * <CODE>TextMessage</CODE> object is used to send a message containing a
	 * <CODE>String</CODE>.
	 * <p>
	 * The message object returned may be sent using any <code>Session</code> or
	 * <code>JMSContext</code>. It is not restricted to being sent using the
	 * <code>JMSContext</code> used to create it.
	 * <p>
	 * The message object returned may be optimised for use with the JMS
	 * provider used to create it. However it can be sent using any JMS
	 * provider, not just the JMS provider used to create it.
	 * 
	 * @param text
	 *            the string used to initialize this message
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create this message due to
	 *                some internal error.
	 */

	TextMessage createTextMessage(String text);

	/**
	 * Indicates whether the JMSContext's session is in transacted mode.
	 * 
	 * @return true if the session is in transacted mode
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to return the transaction mode
	 *                due to some internal error.
	 */

	boolean getTransacted();

	/**
	 * Returns the session mode of the JMSContext's session. This can be set at
	 * the time that the JMSContext is created. Possible values are
	 * JMSContext.SESSION_TRANSACTED, JMSContext.AUTO_ACKNOWLEDGE,
	 * JMSContext.CLIENT_ACKNOWLEDGE and JMSContext.DUPS_OK_ACKNOWLEDGE
	 * <p>
	 * If a session mode was not specified when the JMSContext was created a
	 * value of JMSContext.AUTO_ACKNOWLEDGE will be returned.
	 * 
	 * @return the session mode of the JMSContext's session
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to return the acknowledgment
	 *                mode due to some internal error.
	 * 
	 * @see Connection#createSession
	 * @since 2.0
	 */
	int getSessionMode();

	/**
	 * Commits all messages done in this transaction and releases any locks
	 * currently held.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to commit the transaction for
	 *                one of the following reasons:
	 *                <ul>
	 *                <li> an internal error has occurred. <li> the <code>
	 *                JMSContext</code> is container-managed (injected)
	 *                </ul>
	 * @exception TransactionRolledBackRuntimeException
	 *                if the transaction is rolled back due to some internal
	 *                error during commit.
	 * @exception IllegalStateRuntimeException
	 *                if the method is not called by a transacted session.
	 * 
	 */

	void commit();

	/**
	 * Rolls back any messages done in this transaction and releases any locks
	 * currently held.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to roll back the transaction for
	 *                one of the following reasons:
	 *                <ul>
	 *                <li> an internal error has occurred. <li> the <code>
	 *                JMSContext</code> is container-managed (injected)
	 *                </ul>
	 * @exception IllegalStateRuntimeException
	 *                if the method is not called by a transacted session.
	 * 
	 */
	void rollback();

	/**
	 * Stops message delivery in the JMSContext's session, and restarts message
	 * delivery with the oldest unacknowledged message.
	 * 
	 * <P>
	 * All consumers deliver messages in a serial order. Acknowledging a
	 * received message automatically acknowledges all messages that have been
	 * delivered to the client.
	 * 
	 * <P>
	 * Restarting a session causes it to take the following actions:
	 * 
	 * <UL>
	 * <LI>Stop message delivery
	 * <LI>Mark all messages that might have been delivered but not acknowledged
	 * as "redelivered"
	 * <LI>Restart the delivery sequence including all unacknowledged messages
	 * that had been previously delivered. Redelivered messages do not have to
	 * be delivered in exactly their original delivery order.
	 * </UL>
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to stop and restart message
	 *                delivery for one of the following reasons:
	 *                <ul>
	 *                <li> an internal error has occurred. <li> the <code>
	 *                JMSContext</code> is container-managed (injected)
	 *                </ul>
	 * @exception IllegalStateRuntimeException
	 *                if the method is called by a transacted session.
	 */

	void recover();

	/**
	 * Creates a <CODE>JMSConsumer</CODE> for the specified destination.
	 * 
	 * <P>
	 * A client uses a <CODE>JMSConsumer</CODE> object to receive messages that
	 * have been sent to a destination.
	 * 
	 * @param destination
	 *            the <CODE>Destination</CODE> to access.
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create a <CODE>JMSConsumer</CODE>
	 *                due to some internal error.
	 * @exception InvalidDestinationRuntimeException
	 *                if an invalid destination is specified.
	 */
	JMSConsumer createConsumer(Destination destination);

	/**
	 * Creates a <CODE>JMSConsumer</CODE> for the specified destination, using a
	 * message selector.
	 * <P>
	 * A client uses a <CODE>JMSConsumer</CODE> object to receive messages that
	 * have been sent to a destination.
	 * 
	 * @param destination
	 *            the <CODE>Destination</CODE> to access
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the
	 *            <code>JMSConsumer</code>.
	 * 
	 * @throws JMSRuntimeException
	 *             if the session fails to create a <code>JMSConsumer</code> due
	 *             to some internal error.
	 * @throws InvalidDestinationRuntimeException
	 *             if an invalid destination is specified.
	 * @throws InvalidSelectorRuntimeException
	 *             if the message selector is invalid.
	 */
	JMSConsumer createConsumer(Destination destination, java.lang.String messageSelector);

	/**
	 * Creates a <CODE>JMSConsumer</CODE> for the specified destination,
	 * specifying a message selector and the <code>noLocal</code> parameter.
	 * <P>
	 * A client uses a <CODE>JMSConsumer</CODE> object to receive messages that
	 * have been sent to a destination.
	 * <P>
	 * The <code>noLocal</code> argument is for use when the destination is a
	 * topic and the JMSContext's connection is also being used to publish
	 * messages to that topic. If <code>noLocal</code> is set to true then the
	 * <code>JMSConsumer</code> will not receive messages published to the topic
	 * by its own connection. The default value of this argument is false. If
	 * the destination is a queue then the effect of setting
	 * <code>noLocal</code> to true is not specified.
	 * 
	 * @param destination
	 *            the <CODE>Destination</CODE> to access
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the
	 *            <code>JMSConsumer</code>.
	 * @param noLocal
	 *            if true, and the destination is a topic, then the
	 *            <code>JMSConsumer</code> will not receive messages published
	 *            to the topic by its own connection
	 * 
	 * @throws JMSRuntimeException
	 *             if the session fails to create a <code>JMSConsumer</code> due
	 *             to some internal error.
	 * @throws InvalidDestinationRuntimeException
	 *             if an invalid destination is specified.
	 * @throws InvalidSelectorRuntimeException
	 *             if the message selector is invalid.
	 */
	JMSConsumer createConsumer(Destination destination, java.lang.String messageSelector, boolean noLocal);

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
	 * @throws JMSRuntimeException
	 *             if a Queue object cannot be created due to some internal
	 *             error
	 */
	Queue createQueue(String queueName);

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
	 * @throws JMSRuntimeException
	 *             if a Topic object cannot be created due to some internal
	 *             error
	 */
	Topic createTopic(String topicName);

	/**
	 * Creates a durable subscription with the specified name on the specified
	 * topic, and creates a <code>JMSConsumer</code> on that durable
	 * subscription.
	 * <p>
	 * If a durable subscription already exists with the same name and client
	 * identifier (if set) and the same topic and message selector then this
	 * method creates a <code>JMSConsumer</code> on the existing durable
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
	 * However if there is an active consumer on the durable subscription (or a
	 * consumed message from that subscription is still part of a pending
	 * transaction or is not yet acknowledged in the session), and an attempt is
	 * made to create an additional consumer, specifying the same name and
	 * client identifier (if set) but a different topic or message selector, or,
	 * if the client identifier is set, a different noLocal argument, then a
	 * <code>JMSException</code> will be thrown.
	 * 
	 * @param topic
	 *            the non-temporary <CODE>Topic</CODE> to subscribe to
	 * @param name
	 *            the name used to identify this subscription
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create the durable subscription
	 *                and <code>JMSConsumer</code> due to some internal error.
	 * @exception InvalidDestinationRuntimeException
	 *                if an invalid topic is specified.
	 * 
	 */
	JMSConsumer createDurableConsumer(Topic topic, String name);

	/**
	 * Creates a durable subscription with the specified name on the specified
	 * topic, specifying a message selector and the <code>noLocal</code>
	 * parameter, and creates a <code>JMSConsumer</code> on that durable
	 * subscription.
	 * <p>
	 * If a durable subscription already exists with the same name and client
	 * identifier (if set) and the same topic and message selector then this
	 * method creates a <code>JMSConsumer</code> on the existing durable
	 * subscription.
	 * <p>
	 * A durable subscription is used by a client which needs to receive all the
	 * messages published on a topic, including the ones published when there is
	 * no consumer associated with it. The JMS provider retains a record of this
	 * durable subscription and ensures that all messages from the topic's
	 * publishers are retained until they are delivered to, and acknowledged by,
	 * a consumer on this durable subscription or until they have expired.
	 * <p>
	 * A consumer may be created on a durable subscription using the
	 * <code>createDurableConsumer</code> methods on <code>JMSContext</code>, or
	 * the <code>createDurableConsumer</code> and
	 * <code>createDurableSubscriber</code> methods on <code>Session</code> or
	 * <code>TopicSession</code>. A durable subscription will continue to
	 * accumulate messages until it is deleted using the
	 * <code>unsubscribe</code> method.
	 * <p>
	 * A durable subscription which has a consumer associated with it is
	 * described as being active. A durable subscription which has no consumer
	 * associated with it is described as being inactive.
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
	 * However if there is an active consumer on the durable subscription (or a
	 * consumed message from that subscription is still part of a pending
	 * transaction or is not yet acknowledged in the session), and an attempt is
	 * made to create an additional consumer, specifying the same name and
	 * client identifier (if set) but a different topic or message selector, or,
	 * if the client identifier is set, a different noLocal argument, then a
	 * <code>JMSException</code> will be thrown.
	 * <p>
	 * If <code>noLocal</code> is set to true, and the client identifier is set,
	 * then any messages published to the topic using this JMSContext's connection,
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
	 *            published to the topic using this <code>JMSContext</code>, or
	 *            any other <code>JMSContext</code> or connection with the same
	 *            client identifier, will not be added to the durable
	 *            subscription.
	 * @exception JMSRuntimeException
	 *                if the session fails to create the durable subscription
	 *                and <code>JMSConsumer</code> due to some internal error.
	 * @exception InvalidDestinationRuntimeException
	 *                if an invalid topic is specified.
	 * @exception InvalidSelectorRuntimeException
	 *                if the message selector is invalid.
	 * @exception IllegalStateRuntimeException
	 *                if <code>noLocal</code> is set to <code>true</code>
	 *                but the client identifier is unset
	 * 
	 */
	JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal);

	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, and creates a <code>JMSConsumer</code> on that
	 * subscription.
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic, and without a message selector, then this method
	 * creates a <code>JMSConsumer</code> on the existing subscription.
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
	 * different topic or message selector, then a
	 * <code>JMSRuntimeException</code> will be thrown.
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
	 * @throws JMSRuntimeException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>JMSContext</code> due to some internal
	 *             error.
	 * @throws InvalidDestinationRuntimeException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorRuntimeException
	 *             if the message selector is invalid.
	 */
	JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName);

	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, specifying a message selector, and creates a
	 * <code>JMSConsumer</code> on that subscription
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic and message selector then this method creates a
	 * <code>JMSConsumer</code> on the existing subscription.
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
	 * different topic or message selector, then a
	 * <code>JMSRuntimeException</code> will be thrown.
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
	 * @throws JMSRuntimeException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>JMSConsumer</code> due to some
	 *             internal error.
	 * @throws InvalidDestinationRuntimeException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorRuntimeException
	 *             if the message selector is invalid.
	 */
	JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, java.lang.String messageSelector);

	/**
	 * Creates a shared non-durable subscription with the specified name on the
	 * specified topic, specifying a message selector and the
	 * <code>noLocal</code> parameter, and creates a <code>JMSConsumer</code> on
	 * that subscription.
	 * <p>
	 * If a shared non-durable subscription already exists with the same name
	 * and the same topic and message selector then this method creates a
	 * <code>JMSConsumer</code> on the existing subscription.
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
	 * different topic or message selector, then a
	 * <code>JMSRuntimeException</code> will be thrown.
	 * <p>
	 * If <code>noLocal</code> is set to true then messages published to the
	 * topic by its own connection will not be added to the non-durable shared
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
	 *            message selector for the shared non-durable subscription.
	 * @param noLocal
	 *            if true, messages published by its own connection will not be
	 *            added to the non-durable subscription.
	 * 
	 * @throws JMSRuntimeException
	 *             if the session fails to create the shared non-durable
	 *             subscription and <code>JMSConsumer</code> due to some
	 *             internal error.
	 * @throws InvalidDestinationRuntimeException
	 *             if an invalid topic is specified.
	 * @throws InvalidSelectorRuntimeException
	 *             if the message selector is invalid.
	 */
	JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, java.lang.String messageSelector,
			boolean noLocal);

	/**
	 * Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on the
	 * specified queue.
	 * 
	 * @param queue
	 *            the <CODE>queue</CODE> to access
	 * 
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create a browser due to some
	 *                internal error.
	 * @exception InvalidRuntimeDestinationException
	 *                if an invalid destination is specified
	 * 
	 */
	QueueBrowser createBrowser(Queue queue);

	/**
	 * Creates a <CODE>QueueBrowser</CODE> object to peek at the messages on the
	 * specified queue using a message selector.
	 * 
	 * @param queue
	 *            the <CODE>queue</CODE> to access
	 * 
	 * @param messageSelector
	 *            only messages with properties matching the message selector
	 *            expression are delivered. A value of null or an empty string
	 *            indicates that there is no message selector for the message
	 *            consumer.
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create a browser due to some
	 *                internal error.
	 * @exception InvalidRuntimeDestinationException
	 *                if an invalid destination is specified
	 * @exception InvalidRuntimeSelectorException
	 *                if the message selector is invalid.
	 * 
	 */

	QueueBrowser createBrowser(Queue queue, String messageSelector);

	/**
	 * Creates a <CODE>TemporaryQueue</CODE> object. Its lifetime will be that
	 * of the JMSContext's <CODE>Connection</CODE> unless it is deleted earlier.
	 * 
	 * @return a temporary queue identity
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create a temporary queue due to
	 *                some internal error.
	 */

	TemporaryQueue createTemporaryQueue();

	/**
	 * Creates a <CODE>TemporaryTopic</CODE> object. Its lifetime will be that
	 * of the JMSContext's <CODE>Connection</CODE> unless it is deleted earlier.
	 * 
	 * @return a temporary topic identity
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to create a temporary topic due to
	 *                some internal error.
	 * 
	 */

	TemporaryTopic createTemporaryTopic();

	/**
	 * Unsubscribes a durable subscription that has been created by a client.
	 * 
	 * <P>
	 * This method deletes the state being maintained on behalf of the
	 * subscriber by its provider.
	 * <p>
	 * A durable subscription is identified by a name specified by the client
	 * and by the client identifier if set. If the client identifier was set
	 * when the durable subscription was created then a client which
	 * subsequently wishes to use this method to delete a durable subscription
	 * must use the same client identifier.
	 * 
	 * <P>
	 * It is erroneous for a client to delete a durable subscription while there
	 * is an active consumer on that subscription, or while a consumed message
	 * is part of a pending transaction or has not been acknowledged in the
	 * session.
	 * <P>
	 * If the active consumer is represented by a <CODE>JMSConsumer</CODE> then
	 * calling <CODE>close</CODE> on either that object or the
	 * <CODE>JMSContext</CODE> used to create it will render the consumer
	 * inactive and allow the subscription to be deleted.
	 * <P>
	 * If the active consumer was created by calling
	 * <code>setMessageListener</code> on the <CODE>JMSContext</CODE> then
	 * calling <CODE>close</CODE> on the <CODE>JMSContext</CODE> will render the
	 * consumer inactive and allow the subscription to be deleted.
	 * <p>
	 * If the active consumer is represented by a <code>MessageConsumer</code>
	 * or <code>TopicSubscriber</code> then calling <code>close</code> on that
	 * object or on the <code>Session</code> or <code>Connection</code> used to
	 * create it will render the consumer inactive and allow the subscription to
	 * be deleted.
	 * 
	 * @param name
	 *            the name used to identify this subscription
	 * 
	 * @exception JMSRuntimeException
	 *                if the session fails to unsubscribe to the durable
	 *                subscription due to some internal error.
	 * @exception InvalidDestinationRuntimeException
	 *                if an invalid subscription name is specified.
	 * 
	 * 
	 * 
	 */
	void unsubscribe(String name);

	/**
	 * Acknowledges all messages consumed by the JMSContext's session.
	 * <p>
	 * This method is for use when the session has an acknowledgement mode of
	 * CLIENT_ACKNOWLEDGE. If the session is transacted or has an
	 * acknowledgement mode of AUTO_ACKNOWLEDGE or DUPS_OK_ACKNOWLEDGE calling
	 * this method has no effect.
	 * <p>
	 * This method has identical behaviour to the <code>acknowledge</code>
	 * method on <code>Message</code>. A client may individually acknowledge
	 * each message as it is consumed, or it may choose to acknowledge messages
	 * as an application-defined group. In both cases it makes no difference
	 * which of these two methods is used.
	 * <p>
	 * Messages that have been received but not acknowledged may be redelivered.
	 * <p>
	 * This method must not be used if the <code>JMSContext</code> is
	 * container-managed (injected). Doing so will cause a
	 * <code>JMSRuntimeException</code> to be thrown.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to acknowledge the messages for
	 *                one of the following reasons:
	 *                <ul>
	 *                <li> an internal error has occurred. <li> the <code>
	 *                JMSContext</code> is container-managed (injected)
	 *                </ul>
	 * 
	 * @exception IllegalStateException
	 *                if the <code>JMSContext</code> is closed.
	 * 
	 * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
	 * @see javax.jms.Message#acknowledge
	 */

	void acknowledge();

}
