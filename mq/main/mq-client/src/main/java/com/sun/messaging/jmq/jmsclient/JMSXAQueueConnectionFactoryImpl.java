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
 * @(#)JMSXAQueueConnectionFactoryImpl.java	1.9 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;

import com.sun.jms.spi.xa.*;
import com.sun.messaging.ConnectionConfiguration;;

/**
 * An <code>XAQueueConnectionFactory</code> is used to create XAQueueConnections with
 * a Java Message Service (JMS) Point-to-Point (PTP) provider.
 *
 * @see         javax.jms.XAQueueConnectionFactory javax.jms.XAQueueConnectionFactory
 */

public class JMSXAQueueConnectionFactoryImpl extends com.sun.messaging.QueueConnectionFactory implements JMSXAQueueConnectionFactory {

    /**
     * Constructs a JMSXAQueueConnectionFactory with the default configuration.
     * 
     */
    public JMSXAQueueConnectionFactoryImpl() {
        super("/com/sun/messaging/ConnectionFactory");
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

    public JMSXAQueueConnection createXAQueueConnection() throws JMSException {
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
 
    public JMSXAQueueConnection createXAQueueConnection(String username,
                                                     String password) throws JMSException {
        return new JMSXAQueueConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

}
