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
 * @(#)BrokerListInspector.java	1.13 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/** 
 * Inspector panel for the list of brokers.
 *
 * @see InspectorPanel
 * @see AInspector
 * @see ConsoleObj
 */
public class BrokerListInspector extends TabledInspector {

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    /**
     * Return the array of Strings containing the collumn labels/headers.
     * @return the array of Strings containing the collumn labels/headers.
     */
    public String[] getColumnHeaders()  {
        String[] columnNames = {acr.getString(acr.I_BROKER_NAME2),
			      acr.getString(acr.I_BROKER_HOST2),
			      acr.getString(acr.I_PRIMARY_PORT),
                              acr.getString(acr.I_CONN_STATUS)};
	return (columnNames);
    }

    /**
     * Returns the Object at a particular cell collumn for a given
     * ConsoleObj object. Each row in the JTable represents one ConsoleObj.
     * This method returns the object/value for the ConsoleObj, for a particular 
     * collumn.
     *
     * @return the Object at a particular cell collumn for a given
     * ConsoleObj object.
     */
    public Object getValueAtCollumn(ConsoleObj conObj, int col)  {

	BrokerCObj	bCObj;

	if (!(conObj instanceof BrokerCObj))  {
	    return null;
	}

	bCObj = (BrokerCObj)conObj;

	if (col == 0) {
	    return (bCObj);
	} else if (col == 1) {
	    return (bCObj.getBrokerAdmin().getBrokerHost());
	} else if (col == 2) {
	    return (bCObj.getBrokerAdmin().getBrokerPort());
	} else if (col == 3) {
	    if (bCObj.getBrokerAdmin().isConnected())
	        return acr.getString(acr.I_CONNECTED);
	    else
	        return acr.getString(acr.I_DISCONNECTED);
	}

	return (null);
    }
}
