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

/**
 * The {@code XAConnection} interface extends the capability of
 * {@code Connection} by providing an {@code XASession} (optional).
 * 
 * <P>
 * The {@code XAConnection} interface is optional. JMS providers are not required
 * to support this interface. This interface is for use by JMS providers to
 * support transactional environments. Client programs are strongly encouraged
 * to use the transactional support available in their environment, rather than
 * use these XA interfaces directly.
 * 
 * @see javax.jms.XAQueueConnection
 * @see javax.jms.XATopicConnection
 * 
 * @version JMS 2.0
 * @since JMS 1.0
 * 
 */

public interface XAConnection extends Connection {

	/**
	 * Creates an {@code XASession} object.
	 * 
	 * @return a newly created {@code XASession}
	 * 
	 * @exception JMSException
	 *                if the {@code XAConnection} object fails to create an
	 *                {@code XASession} due to some internal error.
	 * 
	 * @since JMS 1.1
	 * 
	 */

	XASession createXASession() throws JMSException;

	/**
	 * Creates an {@code Session} object.
	 * 
	 * @param transacted
	 *            usage undefined
	 * @param acknowledgeMode
	 *            usage undefined
	 * 
	 * @return a newly created {@code Session}
	 * 
	 * @exception JMSException
	 *                if the {@code XAConnection} object fails to create a
	 *                {@code Session} due to some internal error.
	 * 
	 * @since JMS 1.1
	 * 
	 */
	Session createSession(boolean transacted, int acknowledgeMode)
			throws JMSException;
}
