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

package com.sun.messaging.bridge.admin.bridgemgr;

import java.util.Properties;
import java.util.Enumeration;

/**
 * This class encapsulates the information that the user
 * has provided to perform any JMQ Bridge Administration
 * task. It contains properties that describe:
 * <UL>
 * <LI>the type of command
 * <LI>the command argument, options
 * <LI>etc..
 * </UL>
 */

public class BridgeMgrProperties extends Properties
                      implements BridgeMgrOptions {
    
    public BridgeMgrProperties() {
        super();
    }

    /**
     * Returns the command string. e.g. <EM>stop</EM>.
     */
    public String getCommand() {
        return (getProperty(PropName.CMD));
    }

    /**
     * Returns the command argument string. e.g. <EM>bridge</EM>.
     */
    public String getCommandArg() {
	    return (getProperty(PropName.CMDARG));
    }

    /**
     */
    public String getBridgeType() {
	    return (getProperty(PropName.OPTION_BRIDGE_TYPE));
    }

    /**
     */
    public String getBridgeName() {
	    return (getProperty(PropName.OPTION_BRIDGE_NAME));
    }

    /**
     */
    public String getLinkName() {
	    return (getProperty(PropName.OPTION_LINK_NAME));
	}

    /**
     * Returns the broker host:port.
     */
    public String getBrokerHostPort() {
        return (getProperty(PropName.OPTION_BROKER_HOSTPORT));
    }

    /**
     */
    public String getAdminUserId() {
	    return (getProperty(PropName.OPTION_ADMIN_USERID));
    }

    /**
     */
    public String getAdminPasswd() {
	    return (getProperty(PropName.OPTION_ADMIN_PRIVATE_PASSWD));
	}

    /**
     * Returns the admin passfile (file containing admin password).
     */
    public String getAdminPassfile() {
	    return getProperty(PropName.OPTION_ADMIN_PASSFILE);
    }


    /**
     * Returns whether force mode was specified by the user.
     * Force mode is when no user interaction will be needed.
     *
     * @return true if force mode is set, false otherwise
     */
    public boolean forceModeSet() {
        String s = getProperty(PropName.OPTION_FORCE);

        if (s == null) return false;

        if (s.equalsIgnoreCase(Boolean.TRUE.toString())) {
           return true;
        } else if (s.equalsIgnoreCase(Boolean.FALSE.toString())) {
           return  false;
	    }

        return false;
    }


    /**
     * Returns whether debug mode was specified.
     * This is not a public/documented mode. It's main use
     * is as a back door to get debug information.
     *
     * @return true if debug mode is set, false otherwise 
     */
    public boolean debugModeSet()  {
        String s = getProperty(PropName.OPTION_DEBUG);
        if (s == null) return false; 

        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))  {
            return true;
        } else if (s.equalsIgnoreCase(Boolean.FALSE.toString()))  {
            return false;
        }
        return false;
    }

    /**
     * Returns whether no-check mode was specified.
     * This is not a public/documented mode. It's main use
     * is as a back door to force imqbridgemgr to accept undocumented
     * options to be set.
     *
     * @return true if no-check mode is set, false otherwise
     */
    public boolean noCheckModeSet()  {
        String s = getProperty(PropName.OPTION_NOCHECK);

        if (s == null) return false;

        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))  {
            return true;
        } else if (s.equalsIgnoreCase(Boolean.FALSE.toString()))  {
            return false;
        }

	    return false;
    }

    /**
     * Returns whether admin debug mode was specified.
     * This is not a public/documented mode. It's main use
     * is as a back door to get debug information about the
     * admin connection made to the broker.
     *
     * @return  true if admin debug mode is set, false otherwise 
     */
    public boolean adminDebugModeSet()  {
        String s = getProperty(PropName.OPTION_ADMIN_DEBUG);

        if (s == null) return false;
        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))  {
            return true;
        } else if (s.equalsIgnoreCase(Boolean.FALSE.toString()))  {
            return false;
        }

        return false;
    }

    /**
     */
    public boolean useSSLTransportSet()  {
        String s = getProperty(PropName.OPTION_SSL);

        if (s == null) return false;

        if (s.equalsIgnoreCase(Boolean.TRUE.toString()))  {
            return true;
        } else if (s.equalsIgnoreCase(Boolean.FALSE.toString()))  {
            return false;
        }

        return false;
    }

    /**
     * @return the receive timeout in seconds
     */
    public int getReceiveTimeout()  {
        String s = getProperty(PropName.OPTION_RECV_TIMEOUT);

        if (s == null)  return -1;

        int ret;
        try {
            ret = Integer.parseInt(s);
        } catch (NumberFormatException nfe)  {
	        ret = -1;
        }

        return ret;
    }

    /**
     * Returns the number of 'receive()' retries.
     */
    public int getNumRetries()  {
        String s = getProperty(PropName.OPTION_NUM_RETRIES);

        if (s == null) return -1;

        int ret;
        try {
	        ret = Integer.parseInt(s);
        } catch (NumberFormatException nfe)  {
	        ret = -1;
        }

        return ret;
    }

    /**
     * Returns a Properties object containing the system
     * properties to set.
     *
     * @return  A Properties object containing system properties
     *      to set
     */
    public Properties getSysProps()  {
    Properties  props = new Properties();
    String      targetAttrs = PropName.OPTION_SYS_PROPS + ".";
    int     targetAttrsLen = targetAttrs.length();

        for (Enumeration e = propertyNames();  e.hasMoreElements() ;) {
        String propName = (String)e.nextElement();

        if (propName.startsWith(targetAttrs))  {
            String newPropName, value;

            newPropName = propName.substring(targetAttrsLen);
            value = getProperty(propName);

            props.put(newPropName, value);
        }

        }

        return (props);
    }

    /**
     */
    public String getTargetName()  {
        return (getProperty(PropName.OPTION_TARGET_NAME));
    }


    /**
     * Returns a Properties object containing the properties
     * specified for the target object. The properties are
     * normalized.
     *
     * @return  A Properties object containing properties
     *      for the target object.
     */
    public Properties getTargetAttrs()  {
    Properties  props = new Properties();
    String      targetAttrs = PropName.OPTION_TARGET_ATTRS + ".";
    int     targetAttrsLen = targetAttrs.length();

        for (Enumeration e = propertyNames();  e.hasMoreElements() ;) {

        String propName = (String)e.nextElement();

        if (propName.startsWith(targetAttrs))  {
            String newPropName, value;

            newPropName = propName.substring(targetAttrsLen);
            value = getProperty(propName);

            props.put(newPropName, value);
        }
        }

        return (props);
    }



}

