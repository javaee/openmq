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
 */ 

package com.sun.messaging.jmq.jmsserver.comm;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.Enumeration;
import java.net.InetAddress;
import com.sun.messaging.jmq.jmsserver.license.LicenseManager;
import com.sun.messaging.jmq.jmsserver.license.LicenseBase;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.LoggerManager;
import com.sun.messaging.jmq.jmsserver.util.LockFile;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.BrokerExitCode;
import com.sun.messaging.jmq.jmsserver.audit.api.MQAuditService;
import com.sun.messaging.jmq.jmsserver.audit.api.MQAuditSession;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.File;
/**
 * Singleton class for Logger, BrokerResources, BrokerConfig
 *
 * Other singleton classes which can be considered static
 * once they are retrieved (they do not need to be retrieved
 * from the static method each time they are used) should
 * also be defined here <P>
 */

public class CommGlobals
{
    /**
     * Set of properties to save if save properties flag is true
     * NOTE: * is only supported as the last character at this point
     */
    private static final String[] saveStrings = {
               "imq.cluster.ha",
               "imq.cluster.clusterid",
               "imq.brokerid",
               "imq.persist.store",
               "imq.persist.jdbc.*"
    };

    /**
     * String that prefixes all properties.
     */
    public static final String IMQ = "imq";

    protected static final Object lock = CommGlobals.class;

    protected static BrokerResources br = null;

    protected static Logger logger = null;

    private static boolean clearProps = false;
    private static Properties saveProps = null;

    private static Object myaddrObject = null;

    private static LicenseManager licenseManager = null;
    private static LicenseBase currentLicense = null;

    private static MQAuditSession audit = null;

    //------------------------------------------------------------------------
    //--                 static brokerConfig objects                 --
    //------------------------------------------------------------------------
  
    /**
     * default instance property name. This is the name used for this instance of 
     * the broker IF nothing has been specified on the command line 
     */
    public static final String DEFAULT_INSTANCE = "imqbroker";

    /**
     * instance name used by this BrokerConfig
     */
    protected static String configName = DEFAULT_INSTANCE; 

    /**
     * passed in properties
     */
    protected static Properties parameters = null; 

    /**
     * singleton instance of BrokerConfig
     */
    protected static BrokerConfig config = null;

    private static CommBroker commBroker = null;

    protected static ServiceLocator habitat = null;

    public static void cleanupComm()
    {
        br = null;
        logger = null;

        licenseManager = null;
        currentLicense = null;

        audit = null;
        MQAuditService.clear();

        config = null;
        parameters = null; 
        clearProps = false;
        saveProps = null;
        pathinited = false;

        myaddrObject = null;

        commBroker = null;
        habitat = null;
    }

    protected CommGlobals() {
    }

    /**
     * Get the current license manager object.
     */
    public static LicenseManager getLicenseManager() {
        if (licenseManager == null) {
            licenseManager = new LicenseManager();
        }

        return licenseManager;
    }

    /**
     * Get the current broker license.
     */
    public static LicenseBase getCurrentLicense(String licname)
        throws BrokerException {
        if (currentLicense == null) {
            currentLicense = getLicenseManager().getLicense(licname);
        }
        return currentLicense;
    }

    /**
     * Get audit session
     */
    public static MQAuditSession getAuditSession() {
        if (audit == null) {
            synchronized(lock) {
                if (audit == null) {
                    MQAuditService.init();
                    try {
                        audit = MQAuditService.getAuditSession();
                        LockFile lf = LockFile.getCurrentLockFile();
                        if (lf != null) {
                            audit.setInstance(lf.getInstance(),lf.getHost(), lf.getPort());
                        }
                    } catch (BrokerException ex) {
                        getLogger().logStack(Logger.ERROR, ex.toString(), ex);
                        getCommBroker().exit(BrokerExitCode.ERROR, ex.toString(), BrokerEvent.Type.EXCEPTION);
                    }
                }
            }
        }
        return audit;
    }

    /**
     * 
     * @param params Properties supplied on command line or read from standard input
     * @param clearprops
     * @param saveprops
     */
    public static void init(Properties params, boolean clearprops, boolean saveprops)
    {
        pathinit(null);
        clearProps = clearprops;
        if (params == null) return;

        if (saveprops) {
            saveProps = new Properties();
            for (int i=0; i < saveStrings.length; i++) {
                if (saveStrings[i].endsWith("*")) { // has wildcards
                    // OK - this is a pain, find all matching properties
                    // happily we only support wildcards at the end
                    String match = saveStrings[i].substring(0, saveStrings[i].length() - 1);
                    Enumeration e = params.propertyNames();
                    while (e.hasMoreElements()) {
                        String key = (String)e.nextElement();
                        if (key.startsWith(match)) {
                            String val = params.getProperty(key);
                            saveProps.put(key, val);
                        }
                    }
                    continue;
                }
                String val = params.getProperty(saveStrings[i]);
                if (val != null) saveProps.put(saveStrings[i],
                                 val);
            }
        }

        configName = params.getProperty(IMQ + ".instancename", DEFAULT_INSTANCE);

	// Make sure there is a jmq.home, jmq.varhome and a jmq.instancename
        // property set (these may be used by property variable expansion code).
        params.setProperty(JMQ_VAR_HOME_PROPERTY, JMQ_VAR_HOME);
        params.setProperty(JMQ_LIB_HOME_PROPERTY, JMQ_LIB_HOME);
        params.setProperty(JMQ_ETC_HOME_PROPERTY, JMQ_ETC_HOME);
        params.setProperty(JMQ_INSTANCES_HOME_PROPERTY, JMQ_INSTANCES_HOME);
        params.setProperty(JMQ_HOME_PROPERTY, JMQ_HOME);
        params.setProperty(IMQ + ".instancename", configName);

        parameters = params;

    }

    public static BrokerResources getBrokerResources() {
	if (br == null) {
            synchronized(lock) {
	        if (br == null) {
	            br = BrokerResources.getResources(
		    Locale.getDefault());
		}
	    }
	}
	return br;
    }

    public static Logger getLogger() {
	if (logger == null) {
            synchronized(lock) {
	        if (logger == null) {
		    logger = new Logger(JMQ_VAR_HOME);
		    logger.setResourceBundle(getBrokerResources());
		}
	    }
	}
	return logger;
    }

    public static Object getMyAddressObject() {
        return myaddrObject;
    }

    public static void setMyAddressObject(Object o) {
        myaddrObject = o;
    }

    public static CommBroker getCommBroker() {
        return commBroker; 
    }

    public static void setCommBroker(CommBroker b) {
        commBroker = b;
    }

    //------------------------------------------------------------------------
    //--               static methods for the singleton pattern             --
    //------------------------------------------------------------------------
    
    /**
     * method to return the singleton config class
     */
    public static BrokerConfig getConfig() {
        if (config == null) {
            synchronized (lock) {
                if (config == null) {
                    try {
                        config = new BrokerConfig(configName, parameters, clearProps, saveProps);
                    } catch (BrokerException ex) {
                        getLogger().logStack(Logger.ERROR, "Internal Error: Unable to load broker, configuration properties are not available. Exiting", ex.getCause());
                        getCommBroker().exit(-1,
                            "Internal Error: Unable to load broker,"
                            + " configuration properties are not available. Exiting",
                            BrokerEvent.Type.FATAL_ERROR);
                    }


                    // now handle parameters
                    if (parameters != null) {
                        // set any non-jmq properties as system properties

                        Enumeration en = parameters.propertyNames();
                        Properties sysprops = System.getProperties();
                        while (en.hasMoreElements()) {
                            String name = (String)en.nextElement();
                            if (!name.startsWith(IMQ + ".")) {
                                sysprops.put(name, 
                                    parameters.getProperty(name));
                             }
                        }

                    }

                    // First thing we do after reading in configuration
                    // is to initialize the Logger
                    Logger l = getLogger();
                    l.configure(config, IMQ, 
                                (getCommBroker() == null ? false : getCommBroker().isInProcessBroker()), 
                                isJMSRAManagedSpecified(), 
                                (isNucleusManagedBroker() ? habitat:null));
                    // LoggerManager will register as a config listener
                    // to handle dynamic updates to logger properties
                    new LoggerManager(logger, config);
//                    l.open();
                }
            }
        }
        return config;
    }

    public static void setHabitat(ServiceLocator h) {
        habitat = h;
    }

    public static ServiceLocator getHabitat() {
        return habitat;
    }

    public static boolean isNucleusManagedBroker() {
        return getConfig().getBooleanProperty(NUCLEUS_MANAGED_PROPERTY, false);
    }

   /**
     * Return whether the property imq.jmqra.managed was specified
     * on the command line or read from standard input
     * @return
     */
    public static boolean isJMSRAManagedSpecified() {
        if (parameters == null) {
            return false;
        }
        String val = parameters.getProperty(JMSRA_MANAGED_PROPERTY);
        return (val != null && val.trim().toLowerCase().equals("true"));
    }

    public static boolean isJMSRAManagedBroker() {
        return getConfig().getBooleanProperty(JMSRA_MANAGED_PROPERTY, false);
    }

    /**
     * METHOD FOR UNIT TEST ONLY <P>
     * method to re-initialize the config singleton config class (for testing)
     * @param name the name used by the broker, passed in at startup
     */
    public static void reInitializeConfig(String name) {
        config = null;
        if (name == null) name = DEFAULT_INSTANCE;
        configName = name;
    }

    /**
     * method to return the current name of this broker
     */
    public static String getConfigName() {
        return configName;
    }

    /**
     * method to return path name of the instance directory
     */
    public static String getInstanceDir() {
        return JMQ_INSTANCES_HOME + File.separator + configName;
    }

    /**
     * method to return path name of the instance/etc directory
     */
    public static String getInstanceEtcDir() {
        return JMQ_INSTANCES_HOME + File.separator + configName +
			File.separator + JMQ_ETC_HOME_default_etc;
    }

    /*---------------------------------------------
     *          global static variables
     *---------------------------------------------*/

    /**
     * system property name for the non-editable JMQ home location
     */
    public static final String JMQ_HOME_PROPERTY=IMQ + ".home";

    /**
     * system property name for the editable JMQ home location
     */
    public static final String JMQ_VAR_HOME_PROPERTY=IMQ + ".varhome";

    /**
     * system property name for the editable IMQ instances home location
     */
    public static final String JMQ_INSTANCES_HOME_PROPERTY=IMQ + ".instanceshome";

    /**
     * system property name for the /etc location
     */
    public static final String JMQ_ETC_HOME_PROPERTY=IMQ + ".etchome";

    /**
     * system property name for the /usr/share/lib location
     */
    public static final String JMQ_LIB_HOME_PROPERTY=IMQ + ".libhome";

    /**
     * default value for the non-editable JMQ home location (used if
     * the system property is not set)
     */
    public static final String JMQ_HOME_default = ".";

    /**
     * default value for the non-editable JMQ home location (used if
     * the system property is not set)
     */
    public static final String JMQ_VAR_HOME_default = "var";

    /**
     * default value for the etc JMQ home location (used if
     * the system property is not set). This is the second
     * location to try.
     */
    public static final String JMQ_ETC_HOME_default_etc = "etc";

    /**
     * default value for the etc JMQ home location (used if
     * the system property is not set) - this is the first location
     * to try.
     */
    public static final String JMQ_ETC_HOME_default_etcmq = "etc/mq";

    /**
     * location the configuration is using for the non-editable home location
     */
    public static String JMQ_HOME; 

    /**
     * location the configuration is using for the editable home location
     */
    public static String JMQ_VAR_HOME;

    /**
     * location the configuration is using for the etc home location
     */
    public static String JMQ_ETC_HOME;

    /**
     * location the configuration is using for the share lib home location
     */
    public static String JMQ_LIB_HOME;


    /**
     * location for storing instance specific data
     */
    public static final String INSTANCES_HOME_DIRECTORY="instances";

    public static String JMQ_INSTANCES_HOME;

    public static boolean pathinited = false;

    public static void pathinit(Properties props)
    {
        if (pathinited) return;
        pathinited = true;
        if (props == null)
            props = System.getProperties();
        String path = props.getProperty(JMQ_HOME_PROPERTY,JMQ_HOME_default);
        try {
             path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
             logger.log(Logger.ERROR, BrokerResources.E_BAD_JMQHOME,
                   path, ex);
        }
        JMQ_HOME = path ; 

        path = props.getProperty(JMQ_VAR_HOME_PROPERTY,JMQ_HOME + File.separator + JMQ_VAR_HOME_default);
        try {
             path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
             logger.log(Logger.ERROR, BrokerResources.E_BAD_JMQVARHOME,
                   path, ex);
        }
        JMQ_VAR_HOME = path ; 

        path = props.getProperty(JMQ_LIB_HOME_PROPERTY,JMQ_HOME + File.separator + "lib");
        try {
             path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
             logger.log(Logger.ERROR, BrokerResources.E_BAD_JMQLIBHOME,
                   path, ex);
        }
        JMQ_LIB_HOME = path ; 

        // BUG: 6812136
        // if would be nice if the right etc home is passed in, but if its not
        // look in two places (etc/mq and etc)
        // this addresses the case where an inprocess broker doesn't set etchome
        // and we have to try and find the right one
        path = props.getProperty(JMQ_ETC_HOME_PROPERTY);
        // see if valid
        if (path != null) {
            try {
                File f = new File(path);
                if (!f.exists()) {
                     getLogger().log(Logger.ERROR, BrokerResources.E_BAD_JMQETCHOME, path);
                } else {
                    path = new File(path).getCanonicalPath();
                }
            } catch (IOException ex) {
                 getLogger().log(Logger.ERROR, BrokerResources.E_BAD_JMQETCHOME, path, ex);
            }
        } else { // default case - try both
            //first try etcmq
            path = JMQ_HOME + File.separator + JMQ_ETC_HOME_default_etcmq;
            File f = new File(path);
            if (!f.exists()) {
                path = JMQ_HOME + File.separator + JMQ_ETC_HOME_default_etc;
                f = new File(path);
            }
            try {
                path = f.getCanonicalPath();
            } catch (IOException ex) {
                 logger.log(Logger.ERROR, BrokerResources.E_BAD_JMQETCHOME,
                       path, ex);
            }
        }
        JMQ_ETC_HOME = path ; 

        JMQ_INSTANCES_HOME=JMQ_VAR_HOME + File.separator
                 + INSTANCES_HOME_DIRECTORY;
    }

    /**
     * subdirectory under either the editable or non-editable location where the 
     * configuration files are location
     */
    public static final String JMQ_BROKER_PROP_LOC = "props"+File.separator + "broker"+File.separator;

    public static final String NUCLEUS_MANAGED_PROPERTY = IMQ + ".nucleus.managed";

    public static final String JMSRA_MANAGED_PROPERTY = IMQ + ".jmsra.managed";

}

