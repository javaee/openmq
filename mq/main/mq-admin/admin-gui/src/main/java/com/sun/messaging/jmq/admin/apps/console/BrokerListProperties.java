/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)BrokerListProperties.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.util.Properties;
import java.util.Enumeration;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.util.UserProperties;
import com.sun.messaging.jmq.admin.util.UserPropertiesException;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdminException;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/**
 * This class encapsulates the information needed to save
 * and restore the list of <EM>favourite</EM> object stores in the
 * JMQ Administration Console.
 * 
 * <P>
 * It represents the property object containing the list of
 * brokers. 
 *
 * The format of the property file is:
 * <PRE>
 * version=2.0
 * broker.count=5
 * broker0.key=<STRONG>value</STRONG>
 * broker0.userName=<STRONG>value</STRONG>
 * broker0.password=<STRONG>value</STRONG>
 * broker0.attrs.<STRONG>JMQBrokerHostName=someHost</STRONG>
 * broker0.attrs.<STRONG>JMQBrokerHostPort=somePort</STRONG>
 * broker0.attrs.<STRONG>attrName=value</STRONG>
 * ...
 * </PRE>
 */
public class BrokerListProperties extends UserProperties  {
    
    public final static String		FIRST_VERSION = "2.0";
    public final static String		VERSION = "2.0";

    private final static String		PROP_NAME_VERSION = "version";
    private final static String		PROP_NAME_BROKER_BASENAME = "broker";
    private final static String		PROP_NAME_BROKER_COUNT 
				= PROP_NAME_BROKER_BASENAME + ".count";
    private final static String		PROP_NAME_BROKER_KEY_PREFIX
							= "key";
    private final static String		PROP_NAME_BROKER_RECV_TIMEOUT_PREFIX
							= "receiveTimeout";
    private final static String		PROP_NAME_BROKER_USERNAME_PREFIX
							= "userName";
    private final static String		PROP_NAME_BROKER_PASSWD_PREFIX
							= "password";
    private final static String		PROP_NAME_BROKER_ATTR_PREFIX = "attrs";

    private boolean			DEBUG = false;

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    /**
     * Instantiate a BrokerListProperties object.
     */
    public BrokerListProperties()  {
	super();
	setProperty(PROP_NAME_VERSION, VERSION);
    }

    public String getVersion()  {
	String val =  getProperty(PROP_NAME_VERSION);

	return (val);
    }

    /**
     * Returns the number of brokers
     *
     * @return	The number of brokers
     */
    public int getBrokerCount()  {
	String val =  getProperty(PROP_NAME_BROKER_COUNT);
	int	ret;

	if ((val == null) || val.equals(""))  {
	    return (0);
	}

	try  {
	    ret = Integer.parseInt(val);
	} catch (Exception e)  {
	    ret = 0;
	}

	return (ret);
    }

    /**
     * Returns a BrokerAdmin array containing the BrokerAdmin
     * objects representing brokers the admin console can connect
     * to.
     *
     * @return	Returns a BrokerAdmin array containing the 
     *		BrokerAdmin objects.
     */
    public BrokerAdmin[] getBrokerAdmin() throws BrokerAdminException  {
	int count = getBrokerCount();


	if (count <= 0)  {
	    return (null);
	}

	BrokerAdmin[] baArray = new BrokerAdmin [ count ];

	for (int i = 0; i < count; ++i)  {
	    baArray[i] = getBrokerAdmin(i);
	}

        return (baArray);
    }

    /**
     * Returns a BrokerAdmin object for the given
     * index. 
     *
     * @param	index	Index for specifying the
     *		broker in question.
     *				
     * @return	The BrokerAdmin object for the given
     * 		index. 
     */
    public BrokerAdmin getBrokerAdmin(int index)  throws BrokerAdminException {
	BrokerAdmin	ba;
	Properties	brokerAttrs = new Properties();
	String		basePropName, brokerAttrsStr, 
			keyPropName,
			usernamePropName, passwdPropName;
	//String        timeoutPropName;
	String		keyValue, timeoutValue, usernameValue, passwdValue;
	int		brokerAttrsLen;
	long		timeout;

	/*
	 * Construct base property name string:
	 *	broker0.
	 */
	basePropName = PROP_NAME_BROKER_BASENAME
				+ Integer.toString(index) + ".";

	/*
	 * Construct property name string:
	 *	broker0.key
	 */
	keyPropName = basePropName
			+ PROP_NAME_BROKER_KEY_PREFIX;
	/*
	 * Get key value, if any
	 * The value of this property will be used later.
	 */
	keyValue = getProperty(keyPropName, null);

	/*
	 * Construct property name string:
	 *	broker0.receiveTimeout
	timeoutPropName = basePropName
			+ PROP_NAME_BROKER_RECV_TIMEOUT_PREFIX;
	*/

	/*
	 * For 2.0 FCS, we won't write out or read the timeout value.
	 * Defaulting to -1 here.
	timeoutValue = getProperty(timeoutPropName, "");
	try  {
	    timeout = Long.parseLong(timeoutValue);
	} catch (Exception e)  {
	    timeout = -1;
	}
	 */
	timeout = -1;

	/*
	 * Construct property name string:
	 *	broker0.userName
	 */
	usernamePropName = basePropName
			+ PROP_NAME_BROKER_USERNAME_PREFIX;
	/*
	 * Get username value
	 */
	usernameValue = getProperty(usernamePropName, "");


	/*
	 * Construct property name string:
	 *	broker0.password
	 */
	passwdPropName = basePropName
			+ PROP_NAME_BROKER_PASSWD_PREFIX;
	/*
	 * Get passwd value
	 */
	passwdValue = getProperty(passwdPropName, "");

	/*
	 * Construct base property name string:
	 *	broker0.attrs.
	 */
	brokerAttrsStr = basePropName
				+ PROP_NAME_BROKER_ATTR_PREFIX
				+ ".";

	brokerAttrsLen = brokerAttrsStr.length();

        for (Enumeration e = propertyNames();  e.hasMoreElements() ;) {
	    String propName = (String)e.nextElement();

	    if (propName.startsWith(brokerAttrsStr))  {
	        String newPropName, value;
                newPropName = propName.substring(brokerAttrsLen);
                value = getProperty(propName);
    
                brokerAttrs.setProperty(newPropName, value);
            }
        }
	    
	ba = new BrokerAdmin(brokerAttrs, usernameValue, passwdValue, timeout);

	/*
	 * Set key string if present.
	 */
	if (keyValue != null)  {
	    ba.setKey(keyValue);
	}

	return (ba);
    }

    public void addBrokerAdmin(BrokerAdmin ba)  {
	Properties	brokerAttrs;
	String		basePropName, brokerAttrsStr, 
			keyPropName, timeoutPropName, 
			usernamePropName, passwdPropName;
	String		keyValue, timeoutValue, usernameValue, passwdValue;
	int		brokerAttrsLen;
	long		timeout;
	int		index = getBrokerCount();

	if (DEBUG)  {
	    System.err.println("Setting properties:");
	}

	/*
	 * Construct base property name string:
	 *	broker0.
	 */
	basePropName = PROP_NAME_BROKER_BASENAME + Integer.toString(index) + ".";

	/*
	 * Construct property name string:
	 *	broker0.key
	 */
	keyPropName = basePropName
			+ PROP_NAME_BROKER_KEY_PREFIX;
	/*
	 * Get key value from BrokerAdmin and set it in
	 * properties.
	 */
	keyValue = ba.getKey();
	setProperty(keyPropName, keyValue);


	/*
	 * For 2.0 FCS, we won't write out or read the timeout value.
	 * Construct property name string:
	 *	broker0.receiveTimeout
	timeoutPropName = basePropName
			+ PROP_NAME_BROKER_RECV_TIMEOUT_PREFIX;
	 */
	/*
	 * For 2.0 FCS, we won't write out or read the timeout value.
	 * Get timeout value from BrokerAdmin and set it in
	 * properties.
	timeout = ba.getTimeout();
	setProperty(timeoutPropName, new Long(timeout).toString());
	 */

	/*
	 * Construct property name string:
	 *	broker0.userName
	 */
	usernamePropName = basePropName
			+ PROP_NAME_BROKER_USERNAME_PREFIX;
	/*
	 * Get username value from BrokerAdmin and set it in
	 * properties.
	 */
	usernameValue = ba.getUserName();
	setProperty(usernamePropName, usernameValue);


	/*
	 * Construct property name string:
	 *	broker0.password
	 */
	passwdPropName = basePropName
			+ PROP_NAME_BROKER_PASSWD_PREFIX;
	/*
	 * Get passwd value from BrokerAdmin and set it in
	 * properties.
	 */
	passwdValue = ba.getPassword();
	setProperty(passwdPropName, passwdValue);

	/*
	 * Construct base property name string:
	 *	broker0.attrs.
	 */
	brokerAttrsStr = basePropName
				+ PROP_NAME_BROKER_ATTR_PREFIX
				+ ".";

	brokerAttrs = ba.getBrokerAttrs();


	for (Enumeration e = brokerAttrs.propertyNames();  e.hasMoreElements() ;) {
	    String propName = (String)e.nextElement(), 
				newPropName, newValue;
	    
            newValue = (String)brokerAttrs.getProperty(propName);
	    newPropName = brokerAttrsStr
				+ propName;
	    
	    setProperty(newPropName, newValue);
	}

	index++;
	setProperty(PROP_NAME_BROKER_COUNT, Integer.toString(index));
    }

    public Object setProperty(String key, String value)  {
	if (DEBUG)  {
            System.err.println("\tSetting property: "
			+ key
			+ "="
			+ value);
	}
	return (super.setProperty(key, value));
    }

    public void load() throws UserPropertiesException, SecurityException  {
	super.load();

	String v = getVersion();

	/*
	 * If no version property found, assume the file is the current version
	 * (i.e. OK)
	 */
	if (v == null)  {
	    return;
	}

        checkVersion(v, VERSION, FIRST_VERSION);
    }

    private void checkVersion(String fileVersionStr, String expectedVersionStr,
				String firstVersionStr) throws UserPropertiesException  {
	double	expectedVersion, fileVersion, firstVersion;

	/*
	 * Convert current version string to double.
	 */
	try  {
	    expectedVersion = Double.parseDouble(expectedVersionStr);
	} catch (NumberFormatException nfe)  {
	    UserPropertiesException upe;

	    expectedVersion = 0;
	    upe = new UserPropertiesException(
		acr.getString(acr.E_BAD_INT_BKR_LIST_VER, expectedVersionStr));
	    throw(upe);
	}

	/*
	 * Convert first version string to double.
	 */
	try  {
	    firstVersion = Double.parseDouble(firstVersionStr);
	} catch (NumberFormatException nfe)  {
	    UserPropertiesException upe;

	    firstVersion = 0;
	    upe = new UserPropertiesException(
		acr.getString(acr.E_BAD_INT_BKR_LIST_VER, firstVersionStr));
	    throw(upe);
	}

	/*
	 * Convert file version string to double.
	 */
	try  {
	    fileVersion = Double.parseDouble(fileVersionStr);
	} catch (NumberFormatException nfe)  {
	    UserPropertiesException upe;
	    Object args[] = new Object [ 4 ];

	    fileVersion = 0;

	    args[0] = getAbsoluteFileName();
	    args[1] = PROP_NAME_VERSION;
	    args[2] = fileVersionStr;
	    args[3] = expectedVersionStr;

            String s = acr.getString(acr.E_BAD_FILE_BKR_LIST_VER, args);

	    upe = new UserPropertiesException(s);
	    throw(upe);
	}

	/*
	 * File version is less than our first version - error !
	 */
	if (fileVersion < firstVersion)  {
	    UserPropertiesException upe;
	    Object args[] = new Object [ 4 ];

	    args[0] = getAbsoluteFileName();
	    args[1] = PROP_NAME_VERSION;
	    args[2] = fileVersionStr;
	    args[3] = expectedVersionStr;

            String s = acr.getString(acr.E_BAD_FILE_BKR_LIST_VER, args);

	    upe = new UserPropertiesException(s);
	    throw (upe);
	}

	/*
	 * File version is greater than our current version - error !
	 */
	if (fileVersion > expectedVersion)  {
	    UserPropertiesException upe;
	    Object args[] = new Object [ 4 ];

	    args[0] = getAbsoluteFileName();
	    args[1] = PROP_NAME_VERSION;
	    args[2] = fileVersionStr;
	    args[3] = expectedVersionStr;

            String s = acr.getString(acr.E_BAD_FILE_BKR_LIST_VER, args);

	    upe = new UserPropertiesException(s);
	    throw (upe);
	}

	/*
	 * Add checks for 
	 *	firstVersion < fileVersion < expectedVersion
	 * here.
	 * Currently we don't have any - since this is our first official
	 * version.
	 */
    }
}
