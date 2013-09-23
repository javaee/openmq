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
 * @(#)ConnectionFactory.java	1.25 06/28/07
 */ 

package com.sun.messaging;

import javax.jms.*;
import com.sun.messaging.naming.ReferenceGenerator;
import com.sun.messaging.jmq.jmsclient.QueueConnectionImpl;
import com.sun.messaging.jmq.jmsclient.TopicConnectionImpl;

/**
 * A <code>ConnectionFactory</code> is used to create Connections with
 * the Sun MQ Java Message Service (JMS) provider.
 *
 * @see         javax.jms.ConnectionFactory javax.jms.ConnectionFactory
 * @see         com.sun.messaging.ConnectionConfiguration com.sun.messaging.ConnectionConfiguration
 */
public class ConnectionFactory extends BasicConnectionFactory implements javax.naming.Referenceable {

    /**
     * Constructs a ConnectionFactory with the default configuration.
     *
     */
    public ConnectionFactory() {
        super();
    }

    /**
     * Constructs a ConnectionFactory with the specified configuration.
     *
     */
    protected ConnectionFactory(String defaultsBase) {
        super(defaultsBase);
    }

    /**
     * Creates a Queue Connection with the default user identity. The default user identity
     * is defined by the <code>ConnectionFactory</code> properties
     * <code><b>imqDefaultUsername</b></code> and <code><b>imqDefaultPassword</b></code>
     *   
     * @return a newly created Queue Connection.
     *   
     * @exception JMSException if a JMS error occurs.
     * @see ConnectionConfiguration#imqDefaultUsername
     * @see ConnectionConfiguration#imqDefaultPassword
     */  
    public QueueConnection createQueueConnection() throws JMSException {
        return createQueueConnection(getProperty(ConnectionConfiguration.imqDefaultUsername),
                                     getProperty(ConnectionConfiguration.imqDefaultPassword));
    }
 
    /**
     * Creates a Queue Connection with a specified user identity.
     * 
     * @param username the caller's user name
     * @param password the caller's password
     *
     * @return a newly created queue connection.
     *
     * @exception JMSException if a JMS error occurs.
     */
    public QueueConnection createQueueConnection(String username, String password) throws JMSException {
        return new QueueConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

    /**
     * Creates a Topic Connection with the default user identity. The default user identity
     * is defined by the <code>ConnectionFactory</code> properties
     * <code><b>imqDefaultUsername</b></code> and <code><b>imqDefaultPassword</b></code>
     *   
     * @return a newly created Topic Connection.
     *   
     * @exception JMSException if a JMS error occurs.
     * @see ConnectionConfiguration#imqDefaultUsername
     * @see ConnectionConfiguration#imqDefaultPassword
     */  
    public TopicConnection createTopicConnection() throws JMSException {
        return createTopicConnection(getProperty(ConnectionConfiguration.imqDefaultUsername),
                                     getProperty(ConnectionConfiguration.imqDefaultPassword));
    }
 
    /**
     * Creates a Topic Connection with a specified user identity.
     * 
     * @param username the caller's user name
     * @param password the caller's password
     *
     * @return a newly created topic connection.
     *
     * @exception JMSException if a JMS error occurs.
     */
    public TopicConnection createTopicConnection(String username, String password) throws JMSException {
        return new TopicConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

    /**
     * Returns the reference to this object.
     *   
     * @return  The Reference Object that can be used to reconstruct this object
     *   
     */
    public javax.naming.Reference getReference() {
        return (ReferenceGenerator.getReference(this,
                com.sun.messaging.naming.AdministeredObjectFactory.class.getName()));
    }
}
