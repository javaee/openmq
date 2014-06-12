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

package com.sun.messaging.ums.resources;

import com.sun.messaging.jmq.util.MQResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author chiaming
 */
public class UMSResources extends MQResourceBundle {

    public static final String UMS_RESOURCE_BUNDLE_NAME =
        "com.sun.messaging.ums.resources.UMSResources";

    private static UMSResources resources = null;

    public static UMSResources getResources() {
        return getResources(null);
    }

    public static synchronized UMSResources getResources(Locale locale) {

        if (locale == null) {
            locale = Locale.getDefault();
        }

    if (resources == null || !locale.equals(resources.getLocale())) {
        ResourceBundle prb =
                ResourceBundle.getBundle(UMS_RESOURCE_BUNDLE_NAME, locale);
        resources = new UMSResources(prb);
    }

    return resources;
    }

    private UMSResources(ResourceBundle rb) {
        super(rb);
    }
    
    
    final public static String UMS_NEW_CLIENT_CREATED = "UMS1000";
    
    final public static String UMS_CLIENT_CLOSED = "UMS1001";
    
    final public static String UMS_SWEEPER_INIT = "UMS1002";
    
    final public static String UMS_PROVIDER_INIT = "UMS1003";
    
    final public static String UMS_DEST_SERVICE_INIT = "UMS1004";
   
    final public static String UMS_AUTH_BASE64_ENCODE = "UMS1005";
    
    final public static String UMS_LOGGER_INIT = "UMS1006";
    
    final public static String UMS_DEFAULT_RECEIVE_TIMEOUT = "UMS1007";
    
    final public static String UMS_CONFIG_INIT = "UMS1008";
    
    final public static String UMS_SERVICE_STARTED = "UMS1009";
}
