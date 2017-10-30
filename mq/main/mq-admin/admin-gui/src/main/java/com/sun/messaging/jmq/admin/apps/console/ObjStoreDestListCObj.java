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
 * @(#)ObjStoreDestListCObj.java	1.16 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import javax.swing.ImageIcon;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.objstore.ObjStore;

/** 
 * This class is used in the JMQ Administration console
 * to store information related to the list of destination
 * objects in an object store.
 *
 * @see ConsoleObj
 * @see ObjStoreAdminCObj
 *
 */
public class ObjStoreDestListCObj extends ObjStoreAdminCObj  {
    private transient ObjStore	os = null;
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    /**
     * Create/initialize the admin explorer GUI component.
     */
    public ObjStoreDestListCObj(ObjStore os) {
	this.os = os;
    } 

    public void setObjStore(ObjStore os)  {
	this.os = os;
    }

    public ObjStore getObjStore()  {
	return (os);
    }

    public String getExplorerLabel()  {
	return (acr.getString(acr.I_OBJSTORE_DEST_LIST));
    }

    public String getExplorerToolTip()  {
	return (null);
    }

    public ImageIcon getExplorerIcon()  {
	return (AGraphics.adminImages[AGraphics.OBJSTORE_DEST_LIST]);
    }

    public String getActionLabel(int actionFlag, boolean forMenu)  {
	if (forMenu)  {
	    switch (actionFlag)  {
	    case ActionManager.ADD:
	        return (acr.getString(acr.I_MENU_ADD_OBJSTORE_DEST));
	    }
	} else  {
	    switch (actionFlag)  {
	    case ActionManager.ADD:
	        return (acr.getString(acr.I_ADD_OBJSTORE_DEST));
	    }
	}

	return (null);
    }

    public int getExplorerPopupMenuItemMask()  {
	return (ActionManager.ADD);
    }

    public int getActiveActions()  {
	int mask;

	if (os.isOpen())  {
	    mask = ActionManager.ADD | ActionManager.REFRESH;
	} else  {
	    mask = 0;
	}
	
	return (mask);
    }




    public String getInspectorPanelClassName()  {
	return (ConsoleUtils.getPackageName(this) + ".ObjStoreDestListInspector");
    }

    public String getInspectorPanelId()  {
	return ("Destinations");
    }

    public String getInspectorPanelHeader()  {
	return (getInspectorPanelId());
    }
}
