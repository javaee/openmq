/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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
 * A {@code CompletionListener} is implemented by the application and may
 * be specified when a message is sent asynchronously.
 * <p>
 * When the sending of the message is complete, the JMS provider notifies the
 * application by calling the {@code onCompletion(Message)} method of the
 * specified completion listener. If the sending if the message fails for any
 * reason, and an exception cannot be thrown by the {@code send} method,
 * then the JMS provider calls the {@code onException(Exception)} method of
 * the specified completion listener.
 * 
 * @see javax.jms.MessageProducer#send(javax.jms.Message,int,int,long,javax.jms.CompletionListener)
 * @see javax.jms.MessageProducer#send(javax.jms.Destination,javax.jms.Message,javax.jms.CompletionListener)
 * @see javax.jms.MessageProducer#send(javax.jms.Destination,javax.jms.Message,int,int,long,javax.jms.CompletionListener)
 * @see javax.jms.JMSProducer#setAsync(javax.jms.CompletionListener)
 * @see javax.jms.JMSProducer#getAsync()
 * 
 * @version JMS 2.0
 * @since JMS 2.0
 * 
 */
public interface CompletionListener {

	/**
	 * Notifies the application that the message has been successfully sent
	 * 
	 * @param message
	 *            the message that was sent.
	 */
	void onCompletion(Message message);

	/**
	 * Notifies user that the specified exception was thrown while attempting to
	 * send the specified message. If an exception occurs it is undefined
	 * whether or not the message was successfully sent.
	 * 
	 * @param message
	 *            the message that was sent.
	 * @param exception
	 *            the exception
	 * 
	 */
	void onException(Message message, Exception exception);
}
