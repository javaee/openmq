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
 * @(#)ConfigStore.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.config;

import java.util.*;
import java.net.*;
import java.io.*;
import com.sun.messaging.jmq.jmsserver.util.*;


/**
 * this interface handles how personal and cluster
 * properties are stored and retrieved.
 *
 * A ConfigStore class should have only have a default
 * constructor.
 */

public interface ConfigStore
{

    /**
     * loads the instance properties
     *
     * @param currentprops already loaded properties (including
     *             system, default and install properties)
     * @param instancename the name used by the broker, passed in at startup
     *
     * @return a properties object with the correct instance properties
     *
     * @throws BrokerException if a fatal error occurs loading the 
     *         config store
     */

    public Properties loadStoredProps(Properties currentprops,
                   String instancename) 
           throws BrokerException;

    /**
     * loads the cluster properties
     *
     * @param currentprops already loaded properties (including
     *             system, default and install properties)
     * @param parameters properties passed in on the command line
     * @param instanceprops properties returned from the 
     *             loadStoredProps method
     *
     * @return a properties object with the correct cluster properties
     *            (or null if there aren't any)
     *
     * @throws BrokerException if a fatal error occurs loading the 
     *         config store
     */

    public Properties loadClusterProps(Properties currentprops,
                     Properties parameters,
                     Properties instanceprops) 
           throws BrokerException;

    /**
     * stores the modified properties
     *
     * @param props the list of properties to store
     *
     * @throws IOException if the property can not be stored
     */

    public void storeProperties(Properties props)
              throws IOException;

    /**
     * Reload the specified properties from the store.
     *
     * @param instancename the name used by the broker, passed in at startup
     *
     * @param propnames Array containing names of the properties
     * to be reloaded.
     *
     * @throws BrokerException if a fatal error occurs loading the 
     *         config store
     */
    public Properties reloadProps(String instancename, String[] propnames)
        throws BrokerException;

    /**
     * Clear out any local property file
     *
     * @param instancename the name used by the broker, passed in at startup
     *
     */
    public void clearProps(String instancename);
}
    
