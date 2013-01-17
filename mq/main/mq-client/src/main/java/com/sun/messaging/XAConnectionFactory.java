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

/*
 * @(#)XAConnectionFactory.java	1.5 06/28/07
 */ 

package com.sun.messaging;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.XAConnection;
import javax.jms.XAJMSContext;
import javax.jms.XAQueueConnection;
import javax.jms.XATopicConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.messaging.jmq.jmsclient.ContainerType;
import com.sun.messaging.jmq.jmsclient.XAConnectionImpl;
import com.sun.messaging.jmq.jmsclient.XAJMSContextImpl;
import com.sun.messaging.jmq.jmsclient.XAQueueConnectionImpl;
import com.sun.messaging.jmq.jmsclient.XATopicConnectionImpl;

/**
 * An <code>XAConnectionFactory</code> is used to create XAConnections with
 * the Sun MQ Java Message Service (JMS) provider.
 *
 * @see         javax.jms.XAConnectionFactory javax.jms.XAConnectionFactory
 */
public class XAConnectionFactory extends com.sun.messaging.ConnectionFactory implements javax.jms.XAConnectionFactory {
	
    /* The type of container in which this class is operating. See the ContainerType enum for possible values */
    private static ContainerType containerType;

    /**
     * Create an XA connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @return a newly created XA connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */  

    public XAConnection createXAConnection() throws JMSException {
        return createXAConnection(getProperty(ConnectionConfiguration.imqDefaultUsername),
                                  getProperty(ConnectionConfiguration.imqDefaultPassword));
    }

    /**
     * Create an XA connection with specified user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @param username the caller's user name
     * @param password the caller's password
     *   
     * @return a newly created XA connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XAConnection createXAConnection(String username, String password) throws JMSException {
        return new XAConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }
 
    /**
     * Create an XA queue connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *  
     * @return a newly created XA queue connection.
     *  
     * @exception JMSException if JMS Provider fails to create XA queue Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */

    public XAQueueConnection createXAQueueConnection() throws JMSException {
        return createXAQueueConnection(getProperty(ConnectionConfiguration.imqDefaultUsername),
                                        getProperty(ConnectionConfiguration.imqDefaultPassword));
    }

    /**
     * Create an XA queue connection with specific user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *  
     * @param username the caller's user name
     * @param password the caller's password
     *
     * @return a newly created XA queue connection.
     *
     * @exception JMSException if JMS Provider fails to create XA queue Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XAQueueConnection createXAQueueConnection(String username,
                                                     String password) throws JMSException {
        return new XAQueueConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

    /**
     * Create an XA topic connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @return a newly created XA topic connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA topic Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */  

    public XATopicConnection createXATopicConnection() throws JMSException {
        return createXATopicConnection(getProperty(ConnectionConfiguration.imqDefaultUsername),
                                        getProperty(ConnectionConfiguration.imqDefaultPassword));
    }

    /**
     * Create an XA topic connection with specified user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @param username the caller's user name
     * @param password the caller's password
     *   
     * @return a newly created XA topic connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA topi connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XATopicConnection createXATopicConnection(String username, String password) throws JMSException {
        return new XATopicConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

	@Override
	public XAJMSContext createXAContext() {
		return new XAJMSContextImpl(this, getContainerType());
	}

	@Override
	public XAJMSContext createXAContext(String userName, String password) {
		return new XAJMSContextImpl(this, getContainerType(), userName, password);
	}
	
	protected static ContainerType getContainerType(){
		// Overrides the implementation in BasicConnectionFactory which always returns JavaSE
		
		if (containerType==null){
			Boolean inAppClientContainer = false;
			// See Java EE 7 section EE.5.17 "5.17 Application Client Container Property"
			String lookupName = "java:comp/InAppClientContainer";
			try {
				InitialContext ic = new InitialContext();
				inAppClientContainer = (Boolean)ic.lookup(lookupName);
				if (inAppClientContainer){
					containerType=ContainerType.JavaEE_ACC;
				} else {
					containerType=ContainerType.JavaEE_Web_or_EJB;
				}
			} catch (NamingException e) {
				containerType=ContainerType.JavaSE;
			}
		}
		return containerType;
	}
}
