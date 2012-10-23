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
 * @(#)AdminConnectionFactory.java	1.13 06/28/07
 */ 

package com.sun.messaging;

import java.util.Properties;
import java.util.HashMap;
import java.net.MalformedURLException;
import javax.management.JMException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.management.JMXMQAddress;
import com.sun.messaging.jmq.jmsclient.GenericPortMapperClient;

/**
 * An <code>AdminConnectionFactory</code> is used by management clients
 * to create JMX connections to the  Message Queue broker. After establishing
 * a connection successfully, a handle to a JMX Connector can be obtained
 * which can then be used for management or monitoring operations.
 * <P>
 * The sample code below obtains a JMX Connector that communicates with the
 * default RMI based connector on the broker that is running on the default
 * host and port (localhost and port 7676).
 * The administrator username and password used here is the default 
 * <CODE>admin</CODE> and <CODE>admin</CODE>.
 * <P>
 * <PRE>
 *     import javax.management.*;
 *     import javax.management.remote.*;
 *     import com.sun.messaging.AdminConnectionFactory;
 *     ...
 *     AdminConnectionFactory acf;
 *
 *     acf = new AdminConnectionFactory();
 *     System.out.println("JMXServiceURL used: " + acf.getJMXServiceURL().toString());
 * 
 *     JMXConnector jmxc = acf.createConnection();
 *
 *     // Proceed to manage/monitor the broker using the JMX Connector
 *     // obtained above.
 *     ...
 * </PRE>
 * <P>
 * The sample code below obtains a JMX Connector that communicates with the
 * default RMI connector on the broker that is running on the host 
 * <CODE>myhost</CODE> on port 7979.
 * The administrator username and password used here is <CODE>admin1</CODE> 
 * and <CODE>adminpasswd</CODE>.
 * <P>
 * <PRE>
 *     import javax.management.*;
 *     import javax.management.remote.*;
 *     import com.sun.messaging.AdminConnectionFactory;
 *     import com.sun.messaging.AdminConnectionConfiguration;
 *     ...
 *     AdminConnectionFactory acf;
 *
 *     acf = new AdminConnectionFactory();
 *     acf.setProperty(AdminConnectionConfiguration.imqAddress,
 *			"myhost:7979");
 *     System.out.println("JMXServiceURL used: " + acf.getJMXServiceURL().toString());
 * 
 *     JMXConnector jmxc = acf.createConnection("admin1", "adminpasswd");
 *
 *     // Proceed to manage/monitor the broker using the JMX Connector
 *     // obtained above.
 *     ...
 * </PRE>
 * <P>
 * The sample code below obtains a JMX Connector that communicates with the
 * RMI connector named ssljmxrmi on the broker that is running on the localhost
 * and on port 7676.
 * This is the JMX connector that is configured to use SSL.
 * The administrator username and password used here is the default 
 * <CODE>admin</CODE> and <CODE>admin</CODE>.
 * <P>
 * <PRE>
 *     import javax.management.*;
 *     import javax.management.remote.*;
 *     import com.sun.messaging.AdminConnectionFactory;
 *     import com.sun.messaging.AdminConnectionConfiguration;
 *     ...
 *     AdminConnectionFactory acf;
 *
 *     acf = new AdminConnectionFactory();
 *     acf.setProperty(AdminConnectionConfiguration.imqAddress,
 *			"localhost:7676/ssljmxrmi");
 *     System.out.println("JMXServiceURL used: " + acf.getJMXServiceURL().toString());
 * 
 *     JMXConnector jmxc = acf.createConnection();
 *
 *     // Proceed to manage/monitor the broker using the JMX Connector
 *     // obtained above.
 *     ...
 * </PRE>
 *
 * @see         com.sun.messaging.AdminConnectionConfiguration com.sun.messaging.AdminConnectionConfiguration
 */
public class AdminConnectionFactory extends com.sun.messaging.AdministeredObject {

    /** The default basename for AdministeredObject initialization */
    private static final String defaultsBase = "AdminConnectionFactory";

    /** The default Username and Password for Sun MQ client authentication */
    private static final String DEFAULT_IMQ_ADMIN_USERNAME_PASSWORD = "admin";

    /** The default Username Label */
    private static final String DEFAULT_IMQ_ADMIN_USERNAME_LABEL 
				= "Default Administrator Username";

    /** The default Password Label */
    private static final String DEFAULT_IMQ_ADMIN_PASSWORD_LABEL
				= "Default Administrator Password";

    /**
     * Constructs a AdminConnectionFactory with the default configuration.
     * 
     */
    public AdminConnectionFactory() {
        super(defaultsBase);
    }
 
    /**
     * Constructs a AdminConnectionFactory with the specified configuration.
     * 
     */
    protected AdminConnectionFactory(String defaultsBase) {
        super(defaultsBase);
    }
 
    /**
     * Creates a Connection with the default user identity. The default user identity
     * is defined by the <code>AdminConnectionFactory</code> properties
     * <code><b>imqDefaultAdminUsername</b></code> and <code><b>imqDefaultAdminPassword</b></code>
     * 
     * @return a newly created Connection.
     * 
     * @exception JMException if a JMS error occurs.
     * @see AdminConnectionConfiguration#imqDefaultAdminUsername
     * @see AdminConnectionConfiguration#imqDefaultAdminPassword
     */  
    public JMXConnector createConnection() throws JMException {
	String u = null, p = null;
	try  {
            u = getCurrentConfiguration().getProperty(
			AdminConnectionConfiguration.imqDefaultAdminUsername);
            p = getCurrentConfiguration().getProperty(
			AdminConnectionConfiguration.imqDefaultAdminPassword);
	} catch (Exception e)  {
	}

        return createConnection(u, p);
    }

    /**
     * Creates a Connection with a specified user identity.
     * 
     * @param username the caller's user name
     * @param password the caller's password
     * 
     * @return a newly created connection.
     * 
     * @exception JMException if a JMX error occurs.
     */  
    public JMXConnector createConnection(String username, String password) 
		throws JMException {
	JMXConnector jmxc = null;
	JMXServiceURL url = null;

	url = getJMXServiceURL();
	/*
	System.err.println("url: " + url);
	*/

	try  {
	    HashMap env = new HashMap();
	    String[] credentials = new String[] { username, password };
	    env.put(JMXConnector.CREDENTIALS, credentials);

	    jmxc = JMXConnectorFactory.connect(url, env);
	} catch (Exception e)  {
	    JMException jme 
		= new JMException("Caught exception when creating JMXConnector");
	    jme.initCause(e);

	    throw (jme);
	}

        return (jmxc);
    }

    /**
     * Returns a pretty printed version of the provider specific
     * information for this ConnectionFactory object.
     *
     * @return the pretty printed string.
     */
    public String toString() {
        return ("Oracle GlassFish(tm) Server MQ AdminConnectionFactory" + super.toString());
    }

    /**
     * Returns the relevant JMXServiceURL that is advertised by the 
     * portmapper. This url will be used in connection attempts.
     *
     * @return The relevant JMXServiceURL that is advertised by the 
     * portmapper.
     */
    public JMXServiceURL getJMXServiceURL() throws JMException {
	GenericPortMapperClient pmc;
	JMXMQAddress mqAddr;
	JMXServiceURL url;
	String addr = null, urlString, host, connectorName;
	int port;

	try  {
	    addr = getCurrentConfiguration().getProperty(
			AdminConnectionConfiguration.imqAddress);

	    mqAddr = JMXMQAddress.createAddress(addr);
	    host = mqAddr.getHostName();
	    port = mqAddr.getPort();
	    connectorName = mqAddr.getServiceName();

	    /*
	    System.out.println("ACF: address used: " + addr);
	    System.out.println("\thost: " + host);
	    System.out.println("\tport: " + port);
	    System.out.println("\tconnector: " + connectorName);
	    */

	} catch (Exception e)  {
	    JMException jme 
		= new JMException("Caught exception when parsing address: "
					+ addr);
	    jme.initCause(e);

	    throw (jme);
	}

	/*
	System.out.println("host: " + host);
	System.out.println("port: " + port);
	System.out.println("connectorName: " + connectorName);
	*/

	try  {
	    pmc = new GenericPortMapperClient(host, port);

	    /*
	     * Should add code to check/compare version of client runtime and
	     * broker here.
	     */

	} catch (Exception e)  {
	    JMException jme 
		= new JMException("Caught exception when contacing portmapper.");
	    jme.initCause(e);

	    throw (jme);
	}

	urlString = pmc.getProperty("url", null, "JMX", connectorName);

	if (urlString == null)  {
	    JMException jme = new JMException("No JMXServiceURL was found for connector "
			+ connectorName + ".\n"
			+ "Address used: "
			+ addr);

	    throw (jme);
	}

	try  {
	    url = new JMXServiceURL(urlString);
	} catch (MalformedURLException mfe)  {
	    JMException jme 
		= new JMException("Caught exception when creating JMXServiceURL.");
	    jme.initCause(mfe);

	    throw (jme);
	}
	
	return (url);
    }

    /**
     * Sets the minimum <code>AdminConnectionFactory</code> configuration defaults
     * required to connect to the MQ Administration Service.
     */  
    public void setDefaultConfiguration() {
        configuration = new Properties();
        configurationTypes = new Properties();
        configurationLabels = new Properties();

        configuration.put(AdminConnectionConfiguration.imqDefaultAdminUsername,
                                DEFAULT_IMQ_ADMIN_USERNAME_PASSWORD);
        configurationTypes.put(AdminConnectionConfiguration.imqDefaultAdminUsername,
                                AO_PROPERTY_TYPE_STRING);
        configurationLabels.put(AdminConnectionConfiguration.imqDefaultAdminUsername,
                                DEFAULT_IMQ_ADMIN_USERNAME_LABEL);
 
        configuration.put(AdminConnectionConfiguration.imqDefaultAdminPassword,
                                DEFAULT_IMQ_ADMIN_USERNAME_PASSWORD);
        configurationTypes.put(AdminConnectionConfiguration.imqDefaultAdminPassword,
                                AO_PROPERTY_TYPE_STRING);
        configurationLabels.put(AdminConnectionConfiguration.imqDefaultAdminPassword,
                                DEFAULT_IMQ_ADMIN_PASSWORD_LABEL);
    }

}
