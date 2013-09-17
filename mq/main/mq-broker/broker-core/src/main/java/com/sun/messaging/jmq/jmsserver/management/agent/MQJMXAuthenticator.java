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
 * @(#)MQJMXAuthenticator.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.agent;

import java.net.InetAddress;
import java.rmi.server.RemoteServer;
import javax.security.auth.Subject;
import javax.management.remote.JMXAuthenticator;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.auth.MQAuthenticator;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

/**
 * Authenticator for MQ JMX clients
 *
 */
public class MQJMXAuthenticator implements JMXAuthenticator {
    private Logger logger = Globals.getLogger();
    private ConnectorServerInfo csi;
    private BrokerResources rb = Globals.getBrokerResources();

    public MQJMXAuthenticator(ConnectorServerInfo csi) {
	this.csi = csi;
    }

    public Subject authenticate(Object credentials)  {
	if (credentials == null)  {
	    String errStr = rb.getString(rb.W_JMX_CONNECTOR_CREDENTIALS_NEEDED, csi.getName());
            logger.log(Logger.WARNING, errStr);
	    throw new SecurityException(errStr);
	}

	if (!(credentials instanceof String[])) {
	    String errStr = rb.getString(rb.W_JMX_CONNECTOR_CREDENTIALS_WRONG_TYPE, csi.getName());
            logger.log(Logger.WARNING, errStr);
	    throw new SecurityException(errStr);
	}

	String[] up = (String[])credentials;
	String username = up[0], passwd = up[1];
	String clientIP = null;

	MQAuthenticator a = null;
	try {
	    a = new MQAuthenticator("admin", ServiceType.ADMIN);
	} catch(Exception e)  {
	    String errStr = rb.getString(rb.W_JMX_AUTHENTICATOR_INIT_FAILED, e.toString());
	    logger.log(Logger.WARNING, errStr);
	    throw new SecurityException(errStr);
	}

	/*
	 * For RMI based connectors, we can get to the client host IP
	 * This can be used for auth/access control if needed
	 */
	if (csi.getConfiguredJMXServiceURL().getProtocol().equals("rmi"))  {
	    try  {
                clientIP = RemoteServer.getClientHost();

		/*
		 * We need the IP address. The following guarantees that.
		 */
		InetAddress clientHostIA = InetAddress.getByName(clientIP);
		clientIP = clientHostIA.getHostAddress();
	    } catch (Exception e) {
	        String errStr 
		    = rb.getString(rb.W_JMX_FAILED_TO_GET_IP, csi.getName(), e.toString());
                logger.log(Logger.WARNING, errStr);
		/*
		 * XXX: Should a SecurityException be thrown here ?
		 * ie is it necessary for most cases ?
		 */
	        throw new SecurityException(errStr);
	    }

		AccessController ac = a.getAccessController();

		if (ac != null)  {
		    ac.setClientIP(clientIP);
		}
	}

	try {
	    a.authenticate(username, passwd);
	} catch(Exception e)  {
	    String errStr 
	    = rb.getString(rb.W_JMX_CONNECTOR_AUTH_FAILED, csi.getName(), e.toString());
	    logger.log(Logger.WARNING, errStr);
	    throw new SecurityException(errStr);
	}

	return new Subject();
    }
}
