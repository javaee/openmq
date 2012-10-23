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
 
/**
 * A client using the simplified JMS API introduced for JMS 2.0 uses a <CODE>JMSConsumer</CODE> 
 * object to receive messages from a destination. A <CODE>JMSConsumer</CODE>
 * object is created by passing a <code>Destination</code> object to one of the
 * <code>createConsumer</code> or <code>createDurableConsumer</code> methods on
 * a <code>JMSContext</code>. 
 * <P>
 * A <CODE>JMSConsumer</CODE> can be created with a message selector. A
 * message selector allows the client to restrict the messages delivered to the
 * <CODE>JMSConsumer</CODE> to those that match the selector.
 * <p>
 * A client may either synchronously receive a <CODE>JMSConsumer</CODE>'s 
 * messages or have the <CODE>JMSConsumer</CODE> asynchronously deliver them 
 * as they arrive. 
 * <p>
 * For synchronous receipt, a client can request the next message from a 
 * <CODE>JMSConsumer</CODE> using one of its <code>receive</code> methods. There are several 
 * variations of <code>receive</code> that allow a client to poll or wait for the next message. 
 * <p>
 * For asynchronous delivery, a client can register a <code>MessageListener</code> object 
 * with a <CODE>JMSConsumer</CODE>.
 * As messages arrive at the <CODE>JMSConsumer</CODE>, it delivers them by calling 
 * the <code>MessageListener</code>'s <code>onMessage</code> method.
 * <p>
 * It is a client programming error for a <code>MessageListener</code> to throw an exception.
 * 
 * @version 2.0
 * 
 * @see javax.jms.JMSContext
 */

public interface JMSConsumer {

    /** Gets this <code>JMSConsumer</code>'s message selector expression.
      *  
      * @return this <code>JMSConsumer</code>'s message selector, or null if no
      *         message selector exists for the <code>JMSConsumer</code> (that is, if 
      *         the message selector was not set or was set to null or the 
      *         empty string)
      *  
      * @exception JMSRuntimeException if the JMS provider fails to get the message
      *            selector due to some internal error.
      */ 

    String getMessageSelector();
    
    /** Gets the <code>JMSConsumer</code>'s <code>MessageListener</code>. 
     * <p>
     * This method must not be used in a Java EE web or EJB application. 
     * Doing so may cause a <code>JMSRuntimeException</code> to be thrown though this is not guaranteed.
     * 
      * @return the <code>JMSConsumer</code>'s <code>MessageListener</code>, or null if one was not set
      *  
      * @exception JMSRuntimeException if the JMS provider fails to get the <code>MessageListener</code>
      *                         for one of the following reasons:
      *                         <ul>
      *                         <li>an internal error has occurred or
      *                         <li>this method has been called in a Java EE web or EJB application 
      *                         (though it is not guaranteed that an exception is thrown in this case)
      *                         </ul>                      
      *                         
      * @see javax.jms.JMSConsumer#setMessageListener(javax.jms.MessageListener)
      */ 
    MessageListener getMessageListener() throws JMSRuntimeException;
    
    /** Sets the <code>JMSConsumer</code>'s <CODE>MessageListener</CODE>.
      * <p>
      * Setting the <CODE>MessageListener</CODE> to null is the equivalent of 
      * unsetting the <CODE>MessageListener</CODE> for the <code>JMSConsumer</code>. 
      * <p>
      * The effect of calling this method
      * while messages are being consumed by an existing listener
      * or the <code>JMSConsumer</code> is being used to consume messages synchronously
      * is undefined.
      * <p>
      * This method must not be used in a Java EE web or EJB application. 
      * Doing so may cause a <code>JMSRuntimeException</code> to be thrown though this is not guaranteed.
      * 
      * @param listener the listener to which the messages are to be 
      *                 delivered
      *  
      * @exception JMSRuntimeException if the JMS provider fails to set the <code>JMSConsumer</code>'s <CODE>MessageListener</CODE>
      *                         for one of the following reasons:
      *                         <ul>
      *                         <li>an internal error has occurred or  
      *                         <li>this method has been called in a Java EE web or EJB application 
      *                         (though it is not guaranteed that an exception is thrown in this case)
      *                         </ul>    
      *                         
      * @see javax.jms.JMSConsumer#getMessageListener()
      */ 
    void setMessageListener(MessageListener listener) throws JMSRuntimeException;
    

    /** Receives the next message produced for this <code>JMSConsumer</code>.
      *  
      * <P>This call blocks indefinitely until a message is produced
      * or until this <code>JMSConsumer</code> is closed.
      *
      * <P>If this <CODE>receive</CODE> is done within a transaction, the 
      * JMSConsumer retains the message until the transaction commits.
      *  
      * @return the next message produced for this <code>JMSConsumer</code>, or 
      * null if this <code>JMSConsumer</code> is concurrently closed
      *  
      * @exception JMSRuntimeException if the JMS provider fails to receive the next
      *            message due to some internal error.
      * 
      */ 
 
    Message receive();


    /** Receives the next message that arrives within the specified
      * timeout interval.
      *  
      * <P>This call blocks until a message arrives, the
      * timeout expires, or this <code>JMSConsumer</code> is closed.
      * A <CODE>timeout</CODE> of zero never expires, and the call blocks 
      * indefinitely.
      *
      * @param timeout the timeout value (in milliseconds)
      *
      * @return the next message produced for this <code>JMSConsumer</code>, or 
      * null if the timeout expires or this <code>JMSConsumer</code> is concurrently 
      * closed
      *
      * @exception JMSRuntimeException if the JMS provider fails to receive the next
      *            message due to some internal error.
      */ 

    Message receive(long timeout);


    /** Receives the next message if one is immediately available.
      *
      * @return the next message produced for this <code>JMSConsumer</code>, or 
      * null if one is not available
      *  
      * @exception JMSRuntimeException if the JMS provider fails to receive the next
      *            message due to some internal error.
      */ 

    Message receiveNoWait();


	/**
	 * Closes the <code>JMSConsumer</code>.
	 * 
	 * <P>
	 * Since a provider may allocate some resources on behalf of a
	 * <CODE>JMSConsumer</CODE> outside the Java virtual machine,
	 * clients should close them when they are not needed. Relying on garbage
	 * collection to eventually reclaim these resources may not be timely
	 * enough.
	 * 
	 * <P>
	 * This call blocks until a <CODE>receive</CODE> in progress has completed.
	 * A blocked message consumer <CODE>receive</CODE> call returns null when
	 * this <code>JMSConsumer</code> is closed.
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to close the <code>JMSConsumer</code>
	 *                due to some internal error.
	 */ 

    void close();
    
	/**
	 * Receives the next message produced for this <code>JMSConsumer</code> and
	 * returns its payload, which must be of the specified type
	 * 
	 * <P>
	 * This call blocks indefinitely until a message is produced or until this
	 * <code>JMSConsumer</code> is closed.
	 * 
	 * <P>
	 * If <CODE>receivePayload</CODE> is called within a transaction, the
	 * <code>JMSConsumer</code> retains the message until the transaction commits.
	 * 
	 * @param c
	 *            The class of the payload of the next message.<br/> 
	 *            If the next message is expected to be a <code>TextMessage</code> then 
	 *            this should be set to <code>String.class</code>.<br/>
	 *            If the next message is expected to be a <code>ObjectMessage</code> then 
	 *            this should be set to <code>java.io.Serializable.class</code>. <br/>
	 *            If the next message is expected to be a <code>MapMessage</code> then this
	 *            should be set to <code>java.util.Map.class</code>.<br/>
	 *            If the next message is expected to be a <code>BytesMessage</code> then this
	 *            should be set to <code>byte[].class</code>.<br/>
	 *            If the next message is not of the expected type 
	 *            a <code>ClassCastException</code> will be thrown
	 *            and the message will not be delivered.
	 * 
	 * @return the payload of the next message produced for this
	 *         <code>JMSConsumer</code>, or null if this <code>JMSConsumer</code> is
	 *         concurrently closed
	 * 
	 * @throws JMSRuntimeException
	 *             if the JMS provider fails to receive the next message due
	 *             to some internal error
	 * @throws ClassCastException
	 *             if the next message is not of the expected type
	 */
    <T> T receivePayload(Class<T>  c);
        
	/**
	 * Receives the next message produced for this <code>JMSConsumer</code>  that
	 * arrives within the specified timeout period, and returns its payload,
	 * which must be of the specified type
	 * 
	 * <P>
	 * This call blocks until a message arrives, the timeout expires, or this
	 * <code>JMSConsumer</code> is closed. A timeout of zero never expires, and the
	 * call blocks indefinitely.
	 * 
	 * <P>
	 * If <CODE>receivePayload</CODE> is called within a transaction, the
	 * <code>JMSConsumer</code> retains the message until the transaction commits.
	 * 
	 * @param c
	 *            The class of the payload of the next message.<br/> 
	 *            If the next message is expected to be a <code>TextMessage</code> then 
	 *            this should be set to <code>String.class</code>.<br/>
	 *            If the next message is expected to be a <code>ObjectMessage</code> then 
	 *            this should be set to <code>java.io.Serializable.class</code>. <br/>
	 *            If the next message is expected to be a <code>MapMessage</code> then this
	 *            should be set to <code>java.util.Map.class</code>.<br/>
	 *            If the next message is expected to be a <code>BytesMessage</code> then this
	 *            should be set to <code>byte[].class</code>.<br/>
	 *            If the next message is not of the expected type 
	 *            a <code>ClassCastException</code> will be thrown
	 *            and the message will not be delivered.
	 * 
	 * @return the payload of the next message produced for this
	 *         <code>JMSConsumer</code>, or null if the timeout expires or this <code>JMSConsumer</code> is
	 *         concurrently closed
	 * 
	 * @throws JMSRuntimeException
	 *             if the JMS provider fails to receive the next message due
	 *             to some internal error
	 * @throws ClassCastException
	 *             if the next message is not of the expected type
	 */
    <T> T receivePayload(Class<T> c, long timeout);
    
	/**
	 * Receives the next message produced for this <code>JMSConsumer</code> if one is immediately available 
	 * and returns its payload, which must be of the specified type. 
	 * <p>
	 * If a message is not immediately available null is returned. 
	 * <P>
	 * If <CODE>receivePayloadNoWait</CODE> is called within a transaction, the
	 * <code>JMSConsumer</code> retains the message until the transaction commits.
	 * 
	 * @param c
	 *            The class of the payload of the next message.<br/> 
	 *            If the next message is expected to be a <code>TextMessage</code> then 
	 *            this should be set to <code>String.class</code>.<br/>
	 *            If the next message is expected to be a <code>ObjectMessage</code> then 
	 *            this should be set to <code>java.io.Serializable.class</code>. <br/>
	 *            If the next message is expected to be a <code>MapMessage</code> then this
	 *            should be set to <code>java.util.Map.class</code>.<br/>
	 *            If the next message is expected to be a <code>BytesMessage</code> then this
	 *            should be set to <code>byte[].class</code>.<br/>
	 *            If the next message is not of the expected type 
	 *            a <code>ClassCastException</code> will be thrown
	 *            and the message will not be delivered.
	 * 
	 * @return the payload of the next message produced for this
	 *         <code>JMSConsumer</code>, or null if one is not immediately available 
	 *         or this <code>JMSConsumer</code> is concurrently closed
	 * 
	 * @throws JMSRuntimeException
	 *             if the JMS provider fails to receive the next message due
	 *             to some internal error
	 * @throws ClassCastException
	 *             if the next message is not of the expected type
	 */
    <T> T receivePayloadNoWait(Class<T> c);
    
}
