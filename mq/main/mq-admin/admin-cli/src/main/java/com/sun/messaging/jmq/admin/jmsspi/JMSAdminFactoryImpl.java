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
 * @(#)JMSAdminFactoryImpl.java	1.12 06/27/07
 */

package com.sun.messaging.jmq.admin.jmsspi;

import java.util.Properties;

import javax.jms.*;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsspi.JMSAdmin;
import com.sun.messaging.jmq.jmsspi.JMSAdminFactory;
import com.sun.messaging.jmq.jmsspi.PropertiesHolder;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminResources;

public class JMSAdminFactoryImpl implements JMSAdminFactory {

	private static AdminResources ar = Globals.getAdminResources();

	public final static String DEFAULT_ADMIN_USERNAME = "admin";
	public final static String DEFAULT_ADMIN_PASSWD = "admin";

	/**
	 * This constructor should only be used when no need to communicate with broker e.g. only create administered
	 * objects
	 */
	public JMSAdmin getJMSAdmin() throws JMSException {
		return (getJMSAdmin(false));
	}

	/**
	 * This constructor should only be used when no need to communicate with broker e.g. only create administered
	 * objects
	 * 
	 * @param secure Use secure transport
	 * @return Implementation of JMSAdmin.
	 */
	public JMSAdmin getJMSAdmin(boolean secure) throws JMSException {
		Properties connectionProps = createProviderProperties(null, secure);
		return new JMSAdminImpl(connectionProps, null, DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWD);
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @param brokerPropertiesHolder holder of Properties to be passed to managed broker
	 * @param adminUserName Administrator username
	 * @param password Administrator password (needed for client connections and when starting broker if not specified in brokerProperties)
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL, PropertiesHolder brokerPropertiesHolder, String adminUserName, String adminPassword) throws JMSException {
		return getJMSAdmin(jmsAdminURL, brokerPropertiesHolder, adminUserName, adminPassword, false);
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @param adminUserName Administrator username
	 * @param adminPassword Administrator password
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL, String adminUserName, String adminPassword) throws JMSException {
		return getJMSAdmin(jmsAdminURL, null, adminUserName, adminPassword, false);
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @param brokerPropertiesHolder holder of Properties to be passed to managed broker
	 * @param userName Administrator username
	 * @param password Administrator password (needed for client connections and when starting broker if not specified in brokerProperties)
	 * @param secure Use secure transport
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL, PropertiesHolder brokerPropertiesHolder, String userName, String adminPassword,
			boolean secure) throws JMSException {
		Properties connectionProps = createProviderProperties(jmsAdminURL, secure);
		JMSAdmin admin = new JMSAdminImpl(connectionProps, brokerPropertiesHolder, userName, adminPassword);
		return admin;
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @param adminUserName Administrator username
	 * @param adminPassword Administrator password
	 * @param secure Use secure transport
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException  thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL, String adminUserName, String adminPassword, boolean secure)
			throws JMSException {
		Properties connectionProps = createProviderProperties(jmsAdminURL, secure);
		JMSAdmin admin = new JMSAdminImpl(connectionProps, null, adminUserName, adminPassword);
		return admin;
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL) throws JMSException {
		return getJMSAdmin(jmsAdminURL, false);
	}

	/**
	 * Create/return an instance implementing JMSAdmin.
	 * 
	 * @param jmsAdminURL JMSAdmin URL
	 * @param secure Use secure transport
	 * @return Implementation of JMSAdmin.
	 * @exception JMSException thrown if JMSAdmin could not be created/returned.
	 */
	public JMSAdmin getJMSAdmin(String jmsAdminURL, boolean secure) throws JMSException {
		Properties connectionProps = createProviderProperties(jmsAdminURL, secure);
		JMSAdmin admin = new JMSAdminImpl(connectionProps, null, DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWD);
		return admin;
	}

	private Properties createProviderProperties(String jmsAdminURL, boolean secure) throws JMSException {

		Properties tmpProps = new Properties();

		String host = getBrokerHost(jmsAdminURL);
		int port = getBrokerPort(jmsAdminURL);

		if (host != null) {
			tmpProps.setProperty(ConnectionConfiguration.imqBrokerHostName, host);
		}

		if (port > 0) {
			tmpProps.setProperty(ConnectionConfiguration.imqBrokerHostPort, String.valueOf(port));
		}

		if (secure) {
			tmpProps.setProperty(ConnectionConfiguration.imqConnectionType, "TLS");
		}

		return tmpProps;
	}

	/*
	 * Returns the broker host name. Returns null if not specified.
	 * 
	 * @param brokerHostPort String in the form of host:port
	 * 
	 * @return host value or null if not specified
	 */
	private String getBrokerHost(String brokerHostPort) {
		String host = brokerHostPort;

		if (brokerHostPort == null)
			return (null);

		int i = brokerHostPort.indexOf(':');
		if (i >= 0)
			host = brokerHostPort.substring(0, i);

		if (host == null || host.equals("")) {
			return null;
		}
		return host;
	}

	/*
	 * Returns the broker port number. Return -1 if not specified.
	 * 
	 * @param brokerHostPort String in the form of host:port
	 * 
	 * @return port value or -1 if not specified
	 * 
	 * @throw BrokerAdminException if port value is not valid
	 */
	private int getBrokerPort(String brokerHostPort) throws JMSException {
		int port = -1;

		if (brokerHostPort == null)
			return (port);

		int i = brokerHostPort.indexOf(':');

		if (i >= 0) {
			try {
				port = Integer.parseInt(brokerHostPort.substring(i + 1));

			} catch (Exception e) {
				throw new JMSException(ar.getKString(AdminResources.X_JMSSPI_INVALID_PORT, brokerHostPort));
			}
		}
		return port;
	}
}
