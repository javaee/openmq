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
 * @(#)Globals.java	1.15 06/27/07
 */ 

package com.sun.messaging.jmq.admin.util;

import java.io.File;
import java.util.Locale;
import com.sun.messaging.jmq.admin.resources.AdminResources;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/**
 * Singleton class which contains any Globals for the
 * system.<P>
 *
 * Other singleton classes which can be considered static
 * once they are retrieved (they do not need to be retrieved
 * from the static method each time they are used) should
 * also be defined here <P>
 */

public class Globals extends CommonGlobals
{
    private static final Object lock = Globals.class;

    private static AdminResources ar = null;
    private static AdminConsoleResources acr = null;

    private static Globals globals = null;


    //------------------------------------------------------------------------
    //--                 static brokerConfig objects                 --
    //------------------------------------------------------------------------
  
    private Globals() { }

    public static Globals getGlobals() {
        if (globals == null) {
            synchronized(lock) {
                if (globals == null)
                    globals = new Globals();
            }
        }
        return globals;
    }


    public static AdminResources getAdminResources() {
	if (ar == null) {
            synchronized(lock) {
	        if (ar == null) {
	            ar = AdminResources.getResources(Locale.getDefault());
		}
	    }
	}
	return ar;
    }

    public static AdminConsoleResources getAdminConsoleResources() {
	if (acr == null) {
            synchronized(lock) {
	        if (acr == null) {
	            acr = AdminConsoleResources.getResources(Locale.getDefault());
		}
	    }
	}
	return acr;
    }

    /*---------------------------------------------
     *          global static variables
     *---------------------------------------------*/

    public static final String IMQ = "imq";

    /**
     * system property name for the non-editable JMQ home location
     */
    public static final String JMQ_HOME_PROPERTY="imq.home";

    /**
     * system property name for the editable JMQ home location
     */
    public static final String JMQ_VAR_HOME_PROPERTY="imq.varhome";

    /**
     * system property name for the /usr/share/lib location
     */
    public static final String JMQ_LIB_HOME_PROPERTY="imq.libhome";

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
     * location the configuration is using for the non-editable home location
     */
    public static final String JMQ_HOME = System.getProperty(JMQ_HOME_PROPERTY,JMQ_HOME_default); 

    /**
     * location the configuration is using for the editable home location
     */
    public static final String JMQ_VAR_HOME = System.getProperty(JMQ_VAR_HOME_PROPERTY,JMQ_HOME + File.separator + JMQ_VAR_HOME_default);

    /**
     * location the configuration is using for the share lib home location
     */
    public static final String JMQ_LIB_HOME = System.getProperty(JMQ_LIB_HOME_PROPERTY,JMQ_HOME + File.separator + "lib") ;


    /**
     * subdirectory under either the editable or non-editable location where the 
     * configuration files are location
     */
    public static final String JMQ_ADMIN_PROP_LOC = "props"+File.separator + "admin"+File.separator;

}

