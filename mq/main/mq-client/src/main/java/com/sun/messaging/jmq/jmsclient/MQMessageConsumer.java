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
package com.sun.messaging.jmq.jmsclient;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatRuntimeException;

/**
 * This interface must be implemented by all MQ MesageCOnsumer implementations
 * It adds some additional methods which are required by MQMessageConsumer
 *
 */
public interface MQMessageConsumer extends MessageConsumer {
	
	/**
	 * Receives the next message produced for this {@code MQMessageConsumer} and
	 * returns its body as an object of the specified type. 
	 * This method may be used to receive any type of message except 
	 * for {@code StreamMessage} and {@code Message}, so long as the message
	 * has a body which is capable of being assigned to the specified type.
	 * This means that the specified class or interface must either be the same
	 * as, or a superclass or superinterface of, the class of the message body. 
	 * If the message is not one of the supported types, 
	 * or its body cannot be assigned to the specified type, or it has no body, 
	 * then a {@code MessageFormatRuntimeException} is thrown.
	 * <p>
	 * This method does not give access to the message headers or properties
	 * (such as the {@code JMSRedelivered} message header field or the
	 * {@code JMSXDeliveryCount} message property) and should only be used if
	 * the application has no need to access them.
	 * <P>
	 * This call blocks indefinitely until a message is produced or until this
	 * {@code MQMessageConsumer} is closed.
	 * <p>
	 * If this method is called within a transaction, the
	 * {@code MQMessageConsumer} retains the message until the transaction commits.
	 * <p>
	 * The result of this method throwing a
	 * {@code MessageFormatRuntimeException} depends on the session mode:
	 * <ul>
	 * <li>{@code AUTO_ACKNOWLEDGE} or {@code DUPS_OK_ACKNOWLEDGE}: The JMS
	 * provider will behave as if the unsuccessful call to {@code receiveBody} had
	 * not occurred. The message will be delivered again before any subsequent
	 * messages.
	 * <p>
	 * This is not considered to be redelivery and does not cause the
	 * {@code JMSRedelivered} message header field to be set or the
	 * {@code JMSXDeliveryCount} message property to be incremented.</li>
	 * <li>{@code CLIENT_ACKNOWLEDGE}: The JMS provider will behave as if the
	 * call to {@code receiveBody} had been successful and will not deliver the
	 * message again.
	 * <p>
	 * As with any message that is delivered with a session mode of
	 * {@code CLIENT_ACKNOWLEDGE}, the message will not be acknowledged until
	 * {@code acknowledge} is called on the {@code JMSContext}. If an
	 * application wishes to have the failed message redelivered, it must call
	 * {@code recover} on the {@code JMSContext}. The redelivered message's
	 * {@code JMSRedelivered} message header field will be set and its
	 * {@code JMSXDeliveryCount} message property will be incremented.</li>
	 * 
	 * <li>Transacted session: The JMS provider will behave as if the call to
	 * {@code receiveBody} had been successful and will not deliver the message
	 * again.
	 * <p>
	 * As with any message that is delivered in a transacted session, the
	 * transaction will remain uncommitted until the transaction is committed or
	 * rolled back by the application. If an application wishes to have the
	 * failed message redelivered, it must roll back the transaction. The
	 * redelivered message's {@code JMSRedelivered} message header field will be
	 * set and its {@code JMSXDeliveryCount} message property will be
	 * incremented.</li>
	 * </ul>
	 * 
	 * @param c
	 *            The type to which the body of the next message should be
	 *            assigned.<br/>
	 *            If the next message is expected to be a {@code TextMessage}
	 *            then this should be set to {@code String.class} or another
	 *            class to which a {@code String} is assignable.<br/>
	 *            If the next message is expected to be a {@code ObjectMessage}
	 *            then this should be set to {@code java.io.Serializable.class}
	 *            or another class to which the body is assignable. <br/>
	 *            If the next message is expected to be a {@code MapMessage}
	 *            then this should be set to {@code java.util.Map.class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 *            If the next message is expected to be a {@code BytesMessage}
	 *            then this should be set to {@code byte[].class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 * 
	 * @return the body of the next message produced for this
	 *         {@code MQMessageConsumer}, or null if this {@code MQMessageConsumer} is
	 *         concurrently closed
	 * 
	 * @throws MessageFormatException
	 *             <ul>
	 *             <li>if the message is not one of the supported types listed above
	 *             <li>if the message body cannot be assigned to the specified type
	 *             <li>if the message has no body
	 *             <li>if the message is an {@code ObjectMessage} and object deserialization fails.
	 *             </ul>
	 * @throws JMSException
	 *             if the JMS provider fails to receive the next message due to
	 *             some internal error
	 */
	<T> T receiveBody(Class<T> c) throws JMSException;
	
	/**
	 * Receives the next message produced for this {@code MQMessageConsumer} 
	 * that arrives within the specified timeout period and
	 * returns its body as an object of the specified type. 
	 * This method may be used to receive any type of message except 
	 * for {@code StreamMessage} and {@code Message}, so long as the message
	 * has a body which is capable of being assigned to the specified type.
	 * This means that the specified class or interface must either be the same
	 * as, or a superclass or superinterface of, the class of the message body. 
	 * If the message is not one of the supported types, 
	 * or its body cannot be assigned to the specified type, or it has no body, 
	 * then a {@code MessageFormatRuntimeException} is thrown.
	 * <p>
	 * This method does not give access to the message headers or properties
	 * (such as the {@code JMSRedelivered} message header field or the
	 * {@code JMSXDeliveryCount} message property) and should only be used if
	 * the application has no need to access them.
	 * <P>
	 * This call blocks until a message arrives, the timeout expires, or this
	 * {@code MQMessageConsumer} is closed. A timeout of zero never expires, and the
	 * call blocks indefinitely.
	 * <p>
	 * If this method is called within a transaction, the
	 * {@code MQMessageConsumer} retains the message until the transaction commits.
	 * <p>
	 * The result of this method throwing a
	 * {@code MessageFormatRuntimeException} depends on the session mode:
	 * <ul>
	 * <li>{@code AUTO_ACKNOWLEDGE} or {@code DUPS_OK_ACKNOWLEDGE}: The JMS
	 * provider will behave as if the unsuccessful call to {@code receiveBody} had
	 * not occurred. The message will be delivered again before any subsequent
	 * messages.
	 * <p>
	 * This is not considered to be redelivery and does not cause the
	 * {@code JMSRedelivered} message header field to be set or the
	 * {@code JMSXDeliveryCount} message property to be incremented.</li>
	 * <li>{@code CLIENT_ACKNOWLEDGE}: The JMS provider will behave as if the
	 * call to {@code receiveBody} had been successful and will not deliver the
	 * message again.
	 * <p>
	 * As with any message that is delivered with a session mode of
	 * {@code CLIENT_ACKNOWLEDGE}, the message will not be acknowledged until
	 * {@code acknowledge} is called on the {@code JMSContext}. If an
	 * application wishes to have the failed message redelivered, it must call
	 * {@code recover} on the {@code JMSContext}. The redelivered message's
	 * {@code JMSRedelivered} message header field will be set and its
	 * {@code JMSXDeliveryCount} message property will be incremented.</li>
	 * 
	 * <li>Transacted session: The JMS provider will behave as if the call to
	 * {@code receiveBody} had been successful and will not deliver the message
	 * again.
	 * <p>
	 * As with any message that is delivered in a transacted session, the
	 * transaction will remain uncommitted until the transaction is committed or
	 * rolled back by the application. If an application wishes to have the
	 * failed message redelivered, it must roll back the transaction. The
	 * redelivered message's {@code JMSRedelivered} message header field will be
	 * set and its {@code JMSXDeliveryCount} message property will be
	 * incremented.</li>
	 * </ul>
	 * 
	 * @param c
	 *            The type to which the body of the next message should be
	 *            assigned.<br/>
	 *            If the next message is expected to be a {@code TextMessage}
	 *            then this should be set to {@code String.class} or another
	 *            class to which a {@code String} is assignable.<br/>
	 *            If the next message is expected to be a {@code ObjectMessage}
	 *            then this should be set to {@code java.io.Serializable.class}
	 *            or another class to which the body is assignable. <br/>
	 *            If the next message is expected to be a {@code MapMessage}
	 *            then this should be set to {@code java.util.Map.class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 *            If the next message is expected to be a {@code BytesMessage}
	 *            then this should be set to {@code byte[].class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 * 
	 * @return the body of the next message produced for this {@code MQMessageConsumer},
	 *         or null if the timeout expires or this {@code MQMessageConsumer} is concurrently closed
	 * @throws JMSException 
	 * 
	 * @throws MessageFormatException
	 *             <ul>
	 *             <li>if the message is not one of the supported types listed above
	 *             <li>if the message body cannot be assigned to the specified type
	 *             <li>if the message has no body
	 *             <li>if the message is an {@code ObjectMessage} and object deserialization fails.
	 *             </ul>
	 * @throws JMSException
	 *             if the JMS provider fails to receive the next message due
	 *             to some internal error
	 */
    <T> T receiveBody(Class<T> c, long timeout) throws JMSException;
    
    
	/**
	 * Receives the next message produced for this {@code MQMessageConsumer} 
	 * if one is immediately available and
	 * returns its body as an object of the specified type. 
	 * This method may be used to receive any type of message except 
	 * for {@code StreamMessage} and {@code Message}, so long as the message
	 * has a body which is capable of being assigned to the specified type.
	 * This means that the specified class or interface must either be the same
	 * as, or a superclass or superinterface of, the class of the message body. 
	 * If the message is not one of the supported types, 
	 * or its body cannot be assigned to the specified type, or it has no body, 
	 * then a {@code MessageFormatRuntimeException} is thrown.
	 * <p>
	 * This method does not give access to the message headers or properties
	 * (such as the {@code JMSRedelivered} message header field or the
	 * {@code JMSXDeliveryCount} message property) and should only be used if
	 * the application has no need to access them.
	 * <P>
	 * If a message is not immediately available null is returned. 
	 * <p>
	 * If this method is called within a transaction, the
	 * {@code MQMessageConsumer} retains the message until the transaction commits.
	 * <p>
	 * The result of this method throwing a
	 * {@code MessageFormatRuntimeException} depends on the session mode:
	 * <ul>
	 * <li>{@code AUTO_ACKNOWLEDGE} or {@code DUPS_OK_ACKNOWLEDGE}: The JMS
	 * provider will behave as if the unsuccessful call to {@code receiveBodyNoWait} had
	 * not occurred. The message will be delivered again before any subsequent
	 * messages.
	 * <p>
	 * This is not considered to be redelivery and does not cause the
	 * {@code JMSRedelivered} message header field to be set or the
	 * {@code JMSXDeliveryCount} message property to be incremented.</li>
	 * <li>{@code CLIENT_ACKNOWLEDGE}: The JMS provider will behave as if the
	 * call to {@code receiveBodyNoWait} had been successful and will not deliver the
	 * message again.
	 * <p>
	 * As with any message that is delivered with a session mode of
	 * {@code CLIENT_ACKNOWLEDGE}, the message will not be acknowledged until
	 * {@code acknowledge} is called on the {@code JMSContext}. If an
	 * application wishes to have the failed message redelivered, it must call
	 * {@code recover} on the {@code JMSContext}. The redelivered message's
	 * {@code JMSRedelivered} message header field will be set and its
	 * {@code JMSXDeliveryCount} message property will be incremented.</li>
	 * 
	 * <li>Transacted session: The JMS provider will behave as if the call to
	 * {@code receiveBodyNoWait} had been successful and will not deliver the message
	 * again.
	 * <p>
	 * As with any message that is delivered in a transacted session, the
	 * transaction will remain uncommitted until the transaction is committed or
	 * rolled back by the application. If an application wishes to have the
	 * failed message redelivered, it must roll back the transaction. The
	 * redelivered message's {@code JMSRedelivered} message header field will be
	 * set and its {@code JMSXDeliveryCount} message property will be
	 * incremented.</li>
	 * </ul>
	 * 
	 * @param c
	 *            The type to which the body of the next message should be
	 *            assigned.<br/>
	 *            If the next message is expected to be a {@code TextMessage}
	 *            then this should be set to {@code String.class} or another
	 *            class to which a {@code String} is assignable.<br/>
	 *            If the next message is expected to be a {@code ObjectMessage}
	 *            then this should be set to {@code java.io.Serializable.class}
	 *            or another class to which the body is assignable. <br/>
	 *            If the next message is expected to be a {@code MapMessage}
	 *            then this should be set to {@code java.util.Map.class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 *            If the next message is expected to be a {@code BytesMessage}
	 *            then this should be set to {@code byte[].class}
	 *            (or {@code java.lang.Object.class}).<br/>
	 * 
	 * @return the body of the next message produced for this {@code MQMessageConsumer},
	 *         or null if one is not immediately available or this {@code MQMessageConsumer} is concurrently closed
	 * 
	 * @throws MessageFormatException
	 *             <ul>
	 *             <li>if the message is not one of the supported types listed above
	 *             <li>if the message body cannot be assigned to the specified type
	 *             <li>if the message has no body
	 *             <li>if the message is an {@code ObjectMessage} and object deserialization fails.
	 *             </ul>
	 *             
	 * @throws JMSException
	 *             if the JMS provider fails to receive the next message due
	 *             to some internal error

	 */
    <T> T receiveBodyNoWait(Class<T> c) throws JMSException;

}
