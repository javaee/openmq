/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)JMSXATopicConnectionImpl.java	1.6 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import java.util.Properties;

import com.sun.jms.spi.xa.*;

/** An XATopicConnection is an active connection to a JMS Pub/Sub provider.
  * A client uses an XATopicConnection to create one or more XATopicSessions
  * for producing and consuming messages.
  *
  * @see javax.jms.XAConnection
  * @see javax.jms.XATopicConnectionFactory
  */

public class JMSXATopicConnectionImpl extends XATopicConnectionImpl {

    public
    JMSXATopicConnectionImpl(Properties configuration, String username,
                        String password, String type) throws JMSException {
        super(configuration, username, password, type);
    }

    /**
     * Create an XATopicSession
     *  
     * @param transacted      ignored.
     * @param acknowledgeMode ignored.
     *  
     * @return a newly created XA topic session.
     *  
     * @exception JMSException if JMS Connection fails to create a
     *                         XA topic session due to some internal error.
     */ 
    public JMSXATopicSession
    createXATopicSession(boolean transacted,
                       int acknowledgeMode) throws JMSException {
 
        checkConnectionState();
 
        //disallow to set client ID after this action.
        setClientIDFlag();
 
        return new JMSXATopicSessionImpl(this, transacted, acknowledgeMode);
    }

    /**
     * get a TopicConnection associated with this XATopicConnection object.
     *  
     * @return a TopicConnection.
     */ 
    public TopicConnection getTopicConnection() {
        return (TopicConnection) this;
    }
}

