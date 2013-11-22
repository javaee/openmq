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
 */ 

package com.sun.messaging.bridge.admin.resources;

import java.util.ResourceBundle;
import java.util.Locale;
import com.sun.messaging.jmq.util.MQResourceBundle;
import com.sun.messaging.bridge.api.BridgeCmdSharedResources;

/**
 * This class wraps a PropertyResourceBundle, and provides constants
 * to use as message keys. The reason we use constants for the message
 * keys is to provide some compile time checking when the key is used
 * in the source.
 */

public class BridgeManagerResources extends MQResourceBundle implements BridgeCmdSharedResources {

    private static BridgeManagerResources resources = null;

    public static BridgeManagerResources getResources() {
        return getResources(null);
    }

    public static synchronized BridgeManagerResources getResources(Locale locale) {

        if (locale == null) {
            locale = Locale.getDefault();
        }

	    if (resources == null || !locale.equals(resources.getLocale())) { 
	        ResourceBundle b = ResourceBundle.getBundle(
                           "com.sun.messaging.bridge.admin.resources.BridgeManagerResources", locale);
            resources = new BridgeManagerResources(b);
	    }
	    return resources;
    }

    private BridgeManagerResources(ResourceBundle rb) {
        super(rb);
    }


    /***************** Start of message key constants *******************
     * We use numeric values as the keys because the MQ has a requirement
     * that each error message have an associated error code (for 
     * documentation purposes). We use numeric Strings instead of primitive
     * integers because that is what ListResourceBundles support. We could
     * write our own ResourceBundle to support integer keys, but since
     * we'd just be converting them back to strings (to display them)
     * it's unclear if that would be a big win. Also the performance of
     * ListResourceBundles under Java 2 is pretty good.
     * 
     *
     * Note To Translators: Do not copy these message key String constants
     * into the locale specific resource bundles. They are only required
     * in this default resource bundle.
     */

    // 0-999     Miscellaneous messages

    // 1000-1999 Informational Messages
    final public static String I_JMSBRIDGE_NOT_OWNER = "BB1000";
    final public static String I_BRIDGE_ALREADY_LOADED = "BB1001";
    final public static String I_STARTING_BRIDGE = "BB1002";
    final public static String I_BRIDGE_ALREADY_STARTED = "BB1003";
    final public static String I_STARTING_BRIDGE_WITH_PROPS = "BB1004";
    final public static String I_STARTED_BRIDGE = "BB1005";
    final public static String I_BRIDGE_ALREADY_PAUSED = "BB1006";
    final public static String I_PAUSED_BRIDGE = "BB1007";
    final public static String I_PAUSING_BRIDGE = "BB1008";
    final public static String I_RESUMED_BRIDGE = "BB1009";
    final public static String I_RESUMING_BRIDGE = "BB1010";
    final public static String I_BRIDGE_IS_RUNNING = "BB1011";
    final public static String I_BRIDGE_ALREADY_STOPPED = "BB1012";
    final public static String I_STOPPING_BRIDGE = "BB1013";
    final public static String I_STOPPED_BRIDGE = "BB1014";
    final public static String I_LISTING_BRIDGE = "BB1015";
    final public static String I_LISTING_BRIDGE_WITH = "BB1016";

    // 2000-2999 Warning Messages
    final public static String W_EXCEPTION_STOP_BRIDGES = "BB2000";
    final public static String W_EXCEPTION_CLOSE_ADMIN_CONN = "BB2001";

    // 3000-3999 Error Messages
    final public static String E_LOAD_BRIDGE_FAILED    = "BB3000";
    final public static String E_LOAD_BRIDGE_NO_TYPE    = "BB3001";
    final public static String E_LOAD_BRIDGE_NO_CLASS    = "BB3002";
    final public static String E_BRIDGE_NAME_TYPE_NOT_SAME    = "BB3003";
    final public static String E_START_BRIDGE_FAILED    = "BB3004";
    final public static String E_EXCEPTION_OCCURRED_ADMIN_CONN    = "BB3005";
    final public static String E_GET_LOCALE_FAILED    = "BB3006";
    final public static String E_UNABLE_SEND_ADMIN_REPLY    = "BB3007";
    final public static String E_ADMIN_SET_FAULT_FAILED    = "BB3008";
    final public static String E_ADMIN_INVALID_BRIDGE_NAME    = "BB3009";
    final public static String E_ADMIN_INVALID_LINK_NAME    = "BB3010";
    final public static String E_ADMIN_NO_BRIDGE_NAME    = "BB3011";

    // 4000-4999 Exception Messages
    final public static String X_BRIDGE_NO_TYPE    = "BB4000";
    final public static String X_BRIDGE_NO_ADMIN_USER    = "BB4001";
    final public static String X_BRIDGE_NO_ADMIN_PASSWORD    = "BB4002";
    final public static String X_BRIDGE_SERVICE_MANAGER_NOT_RUNNING    = "BB4003";
    final public static String X_BRIDGE_SERVICE_MANAGER_NOT_INITED    = "BB4004";
    final public static String X_BRIDGE_INVALID_TYPE    = "BB4005";
    final public static String X_BRIDGE_NAME_NOT_FOUND    = "BB4006";
    final public static String X_BRIDGE_TYPE_MISMATCH    = "BB4007";
    final public static String X_BRIDGE_PAUSE_NO_TYPE    = "BB4008";
    final public static String X_BRIDGE_RESUME_NO_TYPE    = "BB4009";
    final public static String X_ADMIN_MSG_NOT_QUEUE    = "BB4010";
    final public static String X_ADMIN_MSG_UNEXPECTED_DEST    = "BB4011";
    final public static String X_EXCEPTION_PROCESSING_ADMIN_MSG    = "BB4012";
    final public static String X_UNEXPECTED_ADMIN_MSG_TYPE    = "BB4013";
    final public static String X_ADMIN_DEBUG_NO_ARG    = "BB4014";
    final public static String X_ADMIN_DEBUG_UNSUPPORTED_ARG    = "BB4015";
    final public static String X_ADMIN_DEBUG_NO_NAME    = "BB4016";
    final public static String X_ADMIN_LINK_NAME_NOSUPPORT    = "BB4017";
    final public static String X_BRIDGE_TYPE_NOSUPPORT    = "BB4018";


    /***************** End of message key constants *******************/
}
