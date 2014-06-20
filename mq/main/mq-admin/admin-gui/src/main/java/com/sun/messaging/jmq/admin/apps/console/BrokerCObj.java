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
 * @(#)BrokerCObj.java	1.27 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.util.Vector;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.util.ServiceState;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdminException;

/** 
 * This class is used in the JMQ Administration console
 * to store information related to a particular broker.
 *
 * @see ConsoleObj
 * @see BrokerAdminCObj
 *
 */
public class BrokerCObj extends BrokerAdminCObj  {
    private BrokerServiceListCObj	bSvcListCObj;
    private BrokerDestListCObj		bDestListCObj;
    private BrokerLogListCObj		bLogListCObj;

    private transient BrokerAdmin ba;
    private Properties bkrProps;
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    public BrokerCObj(BrokerAdmin ba) {
        this.ba = ba;

        bSvcListCObj = new BrokerServiceListCObj(this);
        bDestListCObj = new BrokerDestListCObj(this);
	/*
        bLogListCObj = new BrokerLogListCObj();
	*/

        insert(bSvcListCObj, 0);
        insert(bDestListCObj, 1);
	/*
	 * Logs not managed yet in console
        insert(bLogListCObj, 2);
	*/
    }

    public BrokerAdmin getBrokerAdmin() {
	return (ba);
    }

    public Properties getBrokerProps() {
	return bkrProps;
    }

    public BrokerDestListCObj getBrokerDestListCObj() {
	return bDestListCObj;
    }

    public BrokerServiceListCObj getBrokerServiceListCObj() {
	return bSvcListCObj;
    }

    public void setBrokerProps(Properties bkrProps) {
	this.bkrProps = bkrProps;
    }


    public String getExplorerLabel()  {
	if (ba != null)
	    return (ba.getKey());
	else
	    return (acr.getString(acr.getString(acr.I_BROKER)));
    }

    public String getExplorerToolTip()  {
	return (null);
    }

    public ImageIcon getExplorerIcon()  {
	if (ba.isConnected()) {
	    return (AGraphics.adminImages[AGraphics.BROKER]);
	} else  {
	    return (AGraphics.adminImages[AGraphics.BROKER_DISCONNECTED]);
	}

    }

    public String getActionLabel(int actionFlag, boolean forMenu)  {
	if (forMenu)  {
	    switch (actionFlag)  {
	    case ActionManager.CONNECT:
	        return (acr.getString(acr.I_MENU_CONNECT_BROKER));

	    case ActionManager.DISCONNECT:
	        return (acr.getString(acr.I_MENU_DISCONNECT_BROKER));

	    case ActionManager.PAUSE:
	        return (acr.getString(acr.I_MENU_PAUSE_BROKER));

	    case ActionManager.RESUME:
	        return (acr.getString(acr.I_MENU_RESUME_BROKER));

	    case ActionManager.SHUTDOWN:
	        return (acr.getString(acr.I_MENU_SHUTDOWN_BROKER));

	    case ActionManager.RESTART:
	        return (acr.getString(acr.I_MENU_RESTART_BROKER));

	    case ActionManager.QUERY_BROKER:
	        return (acr.getString(acr.I_MENU_QUERY_BROKER));

	    case ActionManager.DELETE:
	        return (acr.getString(acr.I_MENU_DELETE));

	    case ActionManager.PROPERTIES:
	        return (acr.getString(acr.I_MENU_PROPERTIES));
	    }
	} else  {
	    switch (actionFlag)  {
	    case ActionManager.CONNECT:
	        return (acr.getString(acr.I_CONNECT_BROKER));

	    case ActionManager.DISCONNECT:
	        return (acr.getString(acr.I_DISCONNECT_BROKER));

	    case ActionManager.PAUSE:
	        return (acr.getString(acr.I_PAUSE_BROKER));

	    case ActionManager.RESUME:
	        return (acr.getString(acr.I_RESUME_BROKER));

	    case ActionManager.SHUTDOWN:
	        return (acr.getString(acr.I_SHUTDOWN_BROKER));

	    case ActionManager.RESTART:
	        return (acr.getString(acr.I_RESTART_BROKER));

	    case ActionManager.QUERY_BROKER:
	        return (acr.getString(acr.I_QUERY_BROKER));

	    case ActionManager.DELETE:
	        return (acr.getString(acr.I_DELETE));

	    case ActionManager.PROPERTIES:
	        return (acr.getString(acr.I_PROPERTIES));
	    }
	}

	return (null);
    }

    public ImageIcon getActionIcon(int actionFlag)  {
	switch (actionFlag)  {
	case ActionManager.CONNECT:
	    return (AGraphics.adminImages[AGraphics.CONNECT_TO_BROKER]);
	case ActionManager.DISCONNECT:
	    return (AGraphics.adminImages[AGraphics.DISCONNECT_FROM_BROKER]);
	}

	return (null);
    }

    public int getExplorerPopupMenuItemMask()  {
        return (ActionManager.DELETE | ActionManager.PROPERTIES
                | ActionManager.CONNECT | ActionManager.DISCONNECT
                | ActionManager.PAUSE | ActionManager.RESUME
                | ActionManager.SHUTDOWN | ActionManager.RESTART
		| ActionManager.QUERY_BROKER);
    }


    public int getActiveActions()  {
	int mask = 0;
	
	if (ba.isConnected()) {
	    if (isPausable(ba)) {
	      mask |= ActionManager.PAUSE;
	    }
	    
	    if (isResumable(ba)) {
	      mask |= ActionManager.RESUME;
	    }

	    mask |= ActionManager.DELETE
                 | ActionManager.SHUTDOWN | ActionManager.RESTART
		 | ActionManager.DISCONNECT | ActionManager.REFRESH
		 | ActionManager.QUERY_BROKER | ActionManager.PROPERTIES;
	} else {
            mask = ActionManager.DELETE | ActionManager.CONNECT
		| ActionManager.PROPERTIES;
	}
	return (mask);
    }

    public String getInspectorPanelClassName()  {
	return (ConsoleUtils.getPackageName(this) + ".BrokerInspector");
    }

    public String getInspectorPanelId()  {
	return ("Broker");
    }

    public String getInspectorPanelHeader()  {
	return (getInspectorPanelId());
    }

    private boolean isPausable(BrokerAdmin ba) {
	boolean answer = false;

	/*
	 * Consider a broker "pausable" if at least
	 * one service (that is not an ADMIN service) 
	 * is RUNNING.
	 */
	for (java.util.Enumeration e = bSvcListCObj.children(); 
		e.hasMoreElements();) {
	    ConsoleObj node = (ConsoleObj)e.nextElement();
	    if (node instanceof BrokerServiceCObj) {
		ServiceInfo svcInfo = ((BrokerServiceCObj)node).getServiceInfo();
		if (svcInfo != null &&
		    svcInfo.type != ServiceType.ADMIN &&
		    svcInfo.state == ServiceState.RUNNING) {
		    answer = true;
		    break;
		}
	    }
	}

	return answer;
    }

    private boolean isResumable(BrokerAdmin ba) {
	boolean answer = false;

	/*
	 * Consider a broker "resumable" if at least
	 * one service (that is not an ADMIN service) 
	 * is PAUSED.
	 */
	for (java.util.Enumeration e = bSvcListCObj.children(); 
		e.hasMoreElements();) {
	    ConsoleObj node = (ConsoleObj)e.nextElement();
	    if (node instanceof BrokerServiceCObj) {
		ServiceInfo svcInfo = ((BrokerServiceCObj)node).getServiceInfo();
		if (svcInfo != null &&
		    svcInfo.type != ServiceType.ADMIN &&
		    svcInfo.state == ServiceState.PAUSED) {
		    answer = true;
		    break;
		}
	    }
	}
	return answer;
    }
}
