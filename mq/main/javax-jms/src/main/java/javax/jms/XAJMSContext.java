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

import javax.transaction.xa.XAResource;

/**
 * The {@code XAJMSContext} interface extends the capability of
 * {@code JMSContext} by adding access to a JMS provider's support for the Java
 * Transaction API (JTA) (optional). This support takes the form of a
 * {@code javax.transaction.xa.XAResource} object. The functionality of this
 * object closely resembles that defined by the standard X/Open XA Resource
 * interface.
 * 
 * <P>
 * An application server controls the transactional assignment of an
 * {@code XASession} by obtaining its {@code XAResource}. It uses the
 * {@code XAResource} to assign the session to a transaction, prepare and commit
 * work on the transaction, and so on.
 * 
 * <P>
 * An {@code XAResource} provides some fairly sophisticated facilities for
 * interleaving work on multiple transactions, recovering a list of transactions
 * in progress, and so on. A JTA aware JMS provider must fully implement this
 * functionality. This could be done by using the services of a database that
 * supports XA, or a JMS provider may choose to implement this functionality
 * from scratch.
 * 
 * <P>
 * A client of the application server is given what it thinks is an ordinary
 * {@code JMSContext}. Behind the scenes, the application server controls the
 * transaction management of the underlying {@code XAJMSContext}.
 * 
 * <P>
 * The {@code XAJMSContext} interface is optional. JMS providers are not
 * required to support this interface. This interface is for use by JMS
 * providers to support transactional environments. Client programs are strongly
 * encouraged to use the transactional support available in their environment,
 * rather than use these XA interfaces directly.
 * 
 * @version JMS 2.0
 * @since JMS 2.0
 * 
 */

public interface XAJMSContext extends JMSContext {

	/**
	 * Returns the {@code JMSContext} object associated with this
	 * {@code XAJMSContext}.
	 * 
	 * @return the {@code JMSContext} object associated with this
	 *         {@code XAJMSContext}
	 * 
	 */
	JMSContext getContext();

	/**
	 * Returns an {@code XAResource} to the caller.
	 * 
	 * @return an {@code XAResource}
	 */
	XAResource getXAResource();

	/**
	 * Returns whether the session is in transacted mode; this method always
	 * returns true.
	 * 
	 * @return true
	 * 
	 */
	@Override
	boolean getTransacted();

	/**
	 * Throws a {@code TransactionInProgressRuntimeException}, since it should
	 * not be called for an {@code XAJMSContext} object.
	 * 
	 * @exception TransactionInProgressRuntimeException
	 *                if the method is called on an {@code XAJMSContext}.
	 * 
	 */

	@Override
	void commit();

	/**
	 * Throws a {@code TransactionInProgressRuntimeException}, since it should
	 * not be called for an {@code XAJMSContext} object.
	 * 
	 * @exception TransactionInProgressRuntimeException
	 *                if the method is called on an {@code XAJMSContext}.
	 * 
	 */

	@Override
	void rollback();

}
