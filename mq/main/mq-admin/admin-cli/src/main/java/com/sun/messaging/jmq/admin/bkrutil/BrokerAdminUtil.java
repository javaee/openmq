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
 * @(#)BrokerAdminUtil.java	1.4 06/27/07
 */ 

package com.sun.messaging.jmq.admin.bkrutil;

import com.sun.messaging.jmq.admin.resources.AdminResources;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.util.DestState;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.ServiceState;

/**
 * Class containing useful methods for broker administration.
 */
public class BrokerAdminUtil {

    private static AdminResources ar = Globals.getAdminResources();

    public static String getDestinationType(int mask) {
        if (DestType.isTopic(mask))
            return ar.getString(ar.I_TOPIC);
        else if (DestType.isQueue(mask))
            return ar.getString(ar.I_QUEUE);
        else
            return ar.getString(ar.I_UNKNOWN);
    }

    public static String getDestinationFlavor(int mask) {
        if (DestType.isTopic(mask))
            return "-";
        else if (DestType.isSingle(mask))
            return ar.getString(ar.I_SINGLE);
        else if (DestType.isRRobin(mask))
            return ar.getString(ar.I_RROBIN);
        else if (DestType.isFailover(mask))
            return ar.getString(ar.I_FAILOVER);
        else if (DestType.isQueue(mask))
            return ar.getString(ar.I_SINGLE);  // This is the default
        else
            return ar.getString(ar.I_UNKNOWN);
    }
    
    public static String getDestinationState(int destState) {
    	switch (destState) {
    	
        case DestState.RUNNING:
            return ar.getString(AdminResources.I_DEST_STATE_RUNNING);

        case DestState.CONSUMERS_PAUSED:            
            return ar.getString(AdminResources.I_DEST_STATE_CONSUMERS_PAUSED);

        case DestState.PRODUCERS_PAUSED:
            return ar.getString(AdminResources.I_DEST_STATE_PRODUCERS_PAUSED);

        case DestState.PAUSED:
            return ar.getString(AdminResources.I_DEST_STATE_PAUSED);

    }
    return "UNKNOWN";
    }
    
    public static String getServiceState(int serviceState) {

		switch (serviceState) {
		case ServiceState.UNINITIALIZED:
			return ar.getString(AdminResources.I_SERVICE_STATE_UNINITIALIZED);

		case ServiceState.INITIALIZED:
			return ar.getString(AdminResources.I_SERVICE_STATE_INITIALIZED);

		case ServiceState.STARTED:
			return ar.getString(AdminResources.I_SERVICE_STATE_STARTED);

		case ServiceState.RUNNING:
			return ar.getString(AdminResources.I_SERVICE_STATE_RUNNING);

		case ServiceState.PAUSED:
			return ar.getString(AdminResources.I_SERVICE_STATE_PAUSED);

		case ServiceState.SHUTTINGDOWN:
			return ar.getString(AdminResources.I_SERVICE_STATE_SHUTTINGDOWN);

		case ServiceState.STOPPED:
			return ar.getString(AdminResources.I_SERVICE_STATE_STOPPED);

		case ServiceState.DESTROYED:
			return ar.getString(AdminResources.I_SERVICE_STATE_DESTROYED);

		case ServiceState.QUIESCED:
			return ar.getString(AdminResources.I_SERVICE_STATE_QUIESCED);

		}
		return ar.getString(AdminResources.I_SERVICE_STATE_UNKNOWN);

	}
        	
   

    public static String getActiveConsumers(int mask, int value) {
        if (DestType.isTopic(mask))
            return "-";
        else {
	    if (value == -1) 
		return ar.getString(ar.I_UNLIMITED);
	    else
	    	return Integer.valueOf(value).toString();
	}
    }

    public static String getFailoverConsumers(int mask, int value) {
        if (DestType.isTopic(mask))
            return "-";
        else {
	    if (value == -1) 
		return ar.getString(ar.I_UNLIMITED);
	    else
	    	return Integer.valueOf(value).toString();
	}
    }

    /**
     * see com.sun.messaging.jmq.jmsserver.core.Subscription.getDSubLogString
     */
    public static String getDSubLogString(String clientID, String duraName) {
        return "["+(clientID == null ? "":clientID)+":"+duraName+"]";
    }
}
