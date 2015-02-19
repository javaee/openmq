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
 * @(#)BrokerServiceCObj.java	1.13 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.util.ServiceState;
import com.sun.messaging.jmq.util.admin.ServiceInfo;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;

/** 
 * This class is used in the JMQ Administration console
 * to store information related to a particular broker
 * service.
 *
 * @see ConsoleObj
 * @see BrokerAdminCObj
 *
 */
public class BrokerServiceCObj extends BrokerAdminCObj  {

    private BrokerCObj bCObj;
    private ServiceInfo svcInfo;
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    public BrokerServiceCObj(BrokerCObj bCObj, ServiceInfo svcInfo) {
        this.bCObj = bCObj;
        this.svcInfo = svcInfo;
    }

    public BrokerAdmin getBrokerAdmin() {
	return (bCObj.getBrokerAdmin());
    }

    public BrokerCObj getBrokerCObj() {
	return (bCObj);
    }

    public ServiceInfo getServiceInfo() {
	return svcInfo;
    }

    public void setServiceInfo(ServiceInfo svcInfo) {
	this.svcInfo = svcInfo;
    }

    public String getExplorerLabel()  {
	if (svcInfo != null)
	    return svcInfo.name;
	else
	    return (acr.getString(acr.I_BROKER_SVC));
    }

    public String getExplorerToolTip()  {
	return (null);
    }

    public ImageIcon getExplorerIcon()  {
	return (AGraphics.adminImages[AGraphics.BROKER_SERVICE]);
    }

    public String getActionLabel(int actionFlag, boolean forMenu)  {
	if (forMenu)  {
	    switch (actionFlag)  {
	    case ActionManager.PAUSE:
	        return (acr.getString(acr.I_MENU_PAUSE_SERVICE));

	    case ActionManager.RESUME:
	        return (acr.getString(acr.I_MENU_RESUME_SERVICE));
	    }
	} else  {
	    switch (actionFlag)  {
	    case ActionManager.PAUSE:
	        return (acr.getString(acr.I_PAUSE_SERVICE));

	    case ActionManager.RESUME:
	        return (acr.getString(acr.I_RESUME_SERVICE));
	    }
        }

	return (null);
    }

    public int getExplorerPopupMenuItemMask()  {
	return (ActionManager.PROPERTIES | ActionManager.PAUSE | ActionManager.RESUME);
    }


    public int getActiveActions()  {
	int mask;

	// REVISIT: for now, no operation is allowed if we are not connected.
	// This should be taken out, as we should disallow selecting a service
	// when it is not connected.
	if (!getBrokerAdmin().isConnected())
	    mask = 0;

	/*
	 * ActionManager.REFRESH is included here to enable refreshing of the 
	 * entire service list
	 */

	// If this is an admin service, no operation is allowed
	else if (svcInfo.type == ServiceType.ADMIN || 
		 svcInfo.state == ServiceState.UNKNOWN)
	    mask = ActionManager.PROPERTIES | ActionManager.REFRESH;
	else if (svcInfo.state == ServiceState.RUNNING)
	    mask = ActionManager.PROPERTIES | ActionManager.PAUSE | ActionManager.REFRESH;
	else if (svcInfo.state == ServiceState.PAUSED)
	    mask = ActionManager.PROPERTIES | ActionManager.RESUME | ActionManager.REFRESH;
	else
	    mask = ActionManager.PROPERTIES | ActionManager.PAUSE | ActionManager.RESUME | ActionManager.REFRESH;

	return (mask);
    }



    public String getInspectorPanelClassName()  {
	return (null);
    }

    public String getInspectorPanelId()  {
	return (null);
    }

    public String getInspectorPanelHeader()  {
	return (null);
    }
}
