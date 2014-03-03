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

package com.sun.messaging.bridge.api;

import java.util.Properties;
import java.util.HashMap;
import javax.net.ssl.SSLContext;
import org.jvnet.hk2.annotations.Contract;
import javax.inject.Singleton;
import com.sun.messaging.bridge.api.Bridge;

@Contract
@Singleton
public interface BridgeBaseContext 
{

    public static final String PROP_BRIDGE = "bridge";
    public static final String PROP_PREFIX = "PROP_PREFIX";
    public static final String PROP_ADMIN_USER_SUFFIX = ".admin.user";
    public static final String PROP_ADMIN_PASSWORD_SUFFIX = ".admin.password";

    /**
     * SSL configuration properties
     */
    public static final String KEYSTORE_FILE = "javax.net.ssl.keyStore";
    public static final String KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String KEYSTORE_TYPE = "javax.net.ssl.keyStoreType";

    public static final String TRUSTSTORE_FILE = "javax.net.ssl.trustStore";
    public static final String TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String TRUSTSTORE_TYPE = "javax.net.ssl.trustStoreType";

    public static final String KEYSTORE_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    public static final String TRUSTSTORE_ALGORITHM = "ssl.TrustManagerFactory.algorithm";
    public static final String SECURESOCKET_PROTOCOL = "securesocket.protocol";


    /**
     *
     * @return true if embeded in a broker process
     */
    public boolean isEmbeded();

    /**
     * @return true if do network bind
     */
    public boolean doBind();

    /**
     * @return true if the broker does not have its own JVM
     */
    public boolean isEmbededBroker();

    /**
     * @return true if running on nucleus
     */
    public boolean isRunningOnNucleus();

    /**
     * @return true if broker started in silent mode
     */
    public boolean isSilentMode();

    /**
     * @return null if PUService not enabled
     */
    public Object getPUService();

    /**
     *
     * @return the runtime configuration for bridge service manager
     */
    public Properties getBridgeConfig(); 

    /**
     * @return true if JDBC store type
     */
    public boolean isJDBCStoreType() throws Exception;

    /**
     * Get object that implements JDBC persist store for bridges
     *
     * @return null if not JDBC type store
     */
    public Object getJDBCStore() throws Exception;

    /**
     *
     * @return true if the broker has HA enabled 
     */
    public boolean isHAEnabled();

    /**
     *
     * @param props Bridge properties to update 
     */
    public void updateBridgeConfig(Properties props) throws Exception;

    /**
     *
     * @param protocol The MQ Connection Service protocol string, like "tcp", "ssl"
     * @param serviceType The MQ Connection Service type "NORMAL" or "ADMIN"
     */
    public String getBrokerServiceAddress(String protocol, String serviceType) throws Exception; 

    /**
     *
     */
    public String getBrokerHostName(); 

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


    /**
     * Handle global errors like OOM
     *
     * @return true if the method actually did something with the error
     */
	public boolean handleGlobalError(Throwable ex, String reason); 


    /**
     * Register (optional) a service with host  
     */
	public void registerService(String name, String protocol, 
                                String type, int port, HashMap props); 


    /**
     * Get default configuration properties for SSLContext   
     *
     * @return the default configuration properties for SSLContext 
     */
    public Properties getDefaultSSLContextConfig(String caller) throws Exception;

    /**
     * Get unique identifier for this instance 
     *
     * @return an unique identifier for this instance
     */
    public String getIdentityName() throws Exception;
    

    /**
     * Whether start with reset 
     *
     * @return true if start with reset
     */
    public boolean isStartWithReset();

    /**
     * @return true if ok to allocate size bytes of mem
     */
    public boolean allocateMemCheck(long size); 

}

