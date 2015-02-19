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
 * @(#)ObjStoreConFactoryCObj.java	1.12 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import javax.swing.ImageIcon;

import com.sun.messaging.jmq.admin.objstore.ObjStore;

/** 
 * This class is used in the JMQ Administration console
 * to store information related to a particular connection
 * factory object in an object store.
 *
 * @see ConsoleObj
 * @see ObjStoreAdminCObj
 *
 */
public class ObjStoreConFactoryCObj extends ObjStoreAdminCObj  {

    private ObjStoreCObj osCObj = null;
    private transient ObjStore	 os = null;
    private String       lookupName = null;
    private Object       object;

    /**
     * Create/initialize the admin explorer GUI component.
     */
    public ObjStoreConFactoryCObj(ObjStoreCObj osCObj, String lookupName, Object object) {
	this.osCObj = osCObj;
	this.os = osCObj.getObjStore();
        this.lookupName = lookupName;
        this.object = object;
    } 

    public ObjStore getObjStore()  {
	return this.os;
    }

    public String getExplorerLabel()  {
	return this.lookupName;
    }

    public void setLookupName(String lookupName)  {
        this.lookupName = lookupName;
    }

    public String getLookupName()  {
        return this.lookupName;
    }

    public Object getObject()  {
        return this.object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getExplorerToolTip()  {
	return (null);
    }

    public ImageIcon getExplorerIcon()  {
	return (null);
    }

    public ObjStoreCObj getObjStoreCObj()  {
        return this.osCObj;
    }


    public int getExplorerPopupMenuItemMask()  {
	return (getActiveActions());
    }

    public int getActiveActions()  {
	return (ActionManager.DELETE | ActionManager.PROPERTIES | ActionManager.REFRESH);
    }

    public String getInspectorPanelClassName()  {
        return (ConsoleUtils.getPackageName(this) + 
			".ObjStoreConFactoryListInspector");
    }

    public String getInspectorPanelId()  {
	return (null);
    }

    public String getInspectorPanelHeader()  {
	return (null);
    }
}
