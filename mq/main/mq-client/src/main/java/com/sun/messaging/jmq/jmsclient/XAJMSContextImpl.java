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

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSSecurityRuntimeException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;

import com.sun.messaging.jms.MQRuntimeException;
import com.sun.messaging.jms.MQSecurityRuntimeException;

public class XAJMSContextImpl extends JMSContextImpl implements XAJMSContext {

	XAConnection xaConnection;
	XASession xaSession;
	    
	public XAJMSContextImpl(XAConnectionFactory connectionFactory, ContainerType containerType, String userName, String password) {
		super();
		this.containerType = containerType;

		// create connection
		try {
			xaConnection = connectionFactory.createXAConnection(userName, password);
			connection = xaConnection;
		} catch (SecurityException e) {
			JMSSecurityRuntimeException jsre = new com.sun.messaging.jms.MQSecurityRuntimeException(e.getMessage(), null, e);
			ExceptionHandler.throwJMSRuntimeException(jsre);
		} catch (JMSSecurityException e) {
			throw new MQSecurityRuntimeException(e);
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
		// create session
		try {
			xaSession = xaConnection.createXASession();
			session = xaSession;
		} catch (JMSException e) {
			try {
				connection.close();
			} catch (JMSException e1) {
			}
			throw new MQRuntimeException(e);
		}
		initializeForNewConnection();
	}

	public XAJMSContextImpl(XAConnectionFactory connectionFactory, ContainerType containerType) {
		super();
		this.containerType = containerType;

		// create connection
		try {
			xaConnection = connectionFactory.createXAConnection();
			connection = xaConnection;
		} catch (SecurityException e) {
			JMSSecurityRuntimeException jsre = new com.sun.messaging.jms.MQSecurityRuntimeException(e.getMessage(), null, e);
			ExceptionHandler.throwJMSRuntimeException(jsre);
		} catch (JMSSecurityException e) {
			throw new MQSecurityRuntimeException(e);
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
		// create session
		try {
			xaSession = xaConnection.createXASession();
			session = xaSession;
		} catch (JMSException e) {
			try {
				connection.close();
			} catch (JMSException e1) {
			}
			throw new MQRuntimeException(e);
		}
		initializeForNewConnection();
	}

	@Override
	public JMSContext getContext() {
		return this;
	}

	@Override
	public XAResource getXAResource() {
		return xaSession.getXAResource();
	}

	@Override
	public boolean getTransacted() {
		// the API states that this should always return true
		// but the underlying XASession should be able to handle this
		return super.getTransacted();
	}

	@Override
	public void commit() {
		// the API states that this should always return a
		// TransactionInProgressRuntimeException
		// but the underlying XASession should be able to handle this
		super.commit();
	}

	@Override
	public void rollback() {
		// the API states that this should always return a
		// TransactionInProgressRuntimeException
		// but the underlying XASession should be able to handle this
		super.rollback();
	}

}
