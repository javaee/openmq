/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.bridge.api;

import java.io.File;
import java.util.Properties;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import javax.net.ssl.SSLContext;
import com.sun.messaging.bridge.api.BridgeBaseContext;
import com.sun.messaging.bridge.api.BridgeUtil;

/**
 * The runtime context for a Bridge Service
 *
 * @author amyk
 */
public interface BridgeContext 
{

    public static final String BRIDGE_PROP_PREFIX = "BRIDGE_PROP_PREFIX"; 

    /**
     * SSL configuration properties
     */
    public static final String KEYSTORE_FILE = BridgeBaseContext.KEYSTORE_FILE;
    public static final String KEYSTORE_PASSWORD = BridgeBaseContext.KEYSTORE_PASSWORD;
    public static final String KEYSTORE_TYPE = BridgeBaseContext.KEYSTORE_TYPE;

    public static final String TRUSTSTORE_FILE = BridgeBaseContext.TRUSTSTORE_FILE;
    public static final String TRUSTSTORE_PASSWORD = BridgeBaseContext.TRUSTSTORE_PASSWORD;
    public static final String TRUSTSTORE_TYPE = BridgeBaseContext.TRUSTSTORE_TYPE;

    public static final String KEYSTORE_ALGORITHM = BridgeBaseContext.KEYSTORE_ALGORITHM;
    public static final String TRUSTSTORE_ALGORITHM = BridgeBaseContext.TRUSTSTORE_ALGORITHM;
    public static final String SECURESOCKET_PROTOCOL = BridgeBaseContext.SECURESOCKET_PROTOCOL;


    /**
     * @return true if it's embeded in a broker process
     */
     public boolean isEmbeded();

    /**
     */
     public boolean doBind();

    /**
     *
     * @return true if the broker does not have its own JVM
     */
     public boolean isEmbededBroker();

    /**
     *
     * @return true if running on nucleus
     */
     public boolean isRunningOnNucleus();

     /**
      * @return true if should disable console logging
      */
     public boolean isSilentMode();

     /**
      * @return null if PUService not enabled
      */
     public Object getPUService(); 

    /**
     *
     * @return the runtime configuration for a bridge service
     */
    public Properties getConfig();

    public String getRootDir();

    public String getLibDir();

    public String getProperty(String suffix); 

    /**
     *
     * @param props additional properties to set to the connection factory
     *
     * @return a JMS connection factory for the bridge service
     */
    public javax.jms.ConnectionFactory getConnectionFactory(
                         Properties props) throws Exception; 
    /**
     *
     * @param props additional properties to set to the XA connection factory
     *
     * @return a JMS XA connection factory for the bridge service
     */
    public javax.jms.XAConnectionFactory getXAConnectionFactory(
                              Properties props) throws Exception; 

    /**
     *
     * @return a JMS connection factory for the bridge service
     */
    public javax.jms.ConnectionFactory getAdminConnectionFactory(Properties props)
                                                throws Exception; 

    /**
     * Handle global errors like OOM
     *
     * @return true if the method actually did something with the error
     */
    public boolean handleGlobalError(Throwable ex, String reason); 

    /**
     * Register (optional) a service with host
     */
    public void registerService(String protocol, String type, 
                                int port, HashMap props); 

    /**
     * Get default configuration properties for SSLContext
     *
     * @return the default configuration properties for SSLContext
     */
    public Properties getDefaultSSLContextConfig() throws Exception; 

    /**
     * Get unique identifier for this instance
     *
     * @return an unique identifier for this instance
     */
    public String getIdentityName() throws Exception; 

    public String getBrokerHostName(); 

    public String getTransactionManagerClass() throws Exception; 

    /**
     * return an empty Properties object if no property set 
     */
    public Properties getTransactionManagerProps() throws Exception; 

    public boolean isJDBCStoreType() throws Exception; 

    public Object getJDBCStore(String type) throws Exception; 

    /**
     * @return true if ok to allocate size bytes of mem
     */
    public boolean allocateMemCheck(long size); 

    public boolean getPoodleFixEnabled();

    public String[] getKnownSSLEnabledProtocols();

    /**
     * Logging method for Bridge Service Manager
     */
    public void logError(String message, Throwable t); 

    /**
     * Logging method for Bridge Service Manager
     */
    public void logWarn(String message, Throwable t); 

    /**
     * Logging method for Bridge Service Manager
     */
    public void logInfo(String message, Throwable t); 

    /**
     * Logging method for Bridge Service Manager
     */
    public void logDebug(String message, Throwable t); 

}
