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
 * @(#)AdminApp.java	1.13 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;

import com.sun.messaging.jmq.admin.objstore.ObjStoreManager;

/**
 * This class defines the interface to the admin GUI application.
 * It represents the central point from which the major GUI 
 * pieces of the admin GUI application and other information
 * can be obtained.
 * <P>
 * A class implementing this interface can be used to run or
 * control the admin application. For the admin console
 * application, this controller can do things like:
 *
 * <UL>
 * <LI>show some status text in the status pane
 * <LI>show some object as being selected in the select pane
 * <LI>tell the canvas to select or deselect something
 * <LI>disable/enable some buttons in the control panel
 * </UL>
 *
 * Currently, the admin console is a main application. This is done
 * via the AdminConsole class which extends AdminApp.
 * <P>
 * Later, if we decide to create an applet version of the console,
 * we would create an applet class also extend AdminApp.
 */
public interface AdminApp  {

    /**
     * Returns the application frame.
     *
     * @return The application frame.
     */
    public Frame		getFrame();

    /**
     * Returns the menubar.
     *
     * @return The menubar.
     */
    public AMenuBar		getMenubar();

    /**
     * Returns the toolbar.
     *
     * @return The toolbar.
     */
    public AToolBar		getToolbar();

    /**
     * Returns the explorer pane. This is the pane that
     * contains the tree.
     *
     * @return The explorer pane.
     */
    public AExplorer		getExplorer();

    /**
     * Returns the inspector pane. This is the pane
     * that shows the attributes of what is currently
     * selected.
     *
     * @return The inspector pane.
     */
    public AInspector		getInspector();

    /**
     * Returns the status area pane.
     *
     * @return The status area pane.
     */
    public AStatusArea		getStatusArea();

    /**
     * Returns the action manager.
     *
     * @return The action manager.
     */
    public ActionManager	getActionManager();

    /**
     * Returns the top level object store list object.
     *
     * @return The top level object store list object.
     */
    public ObjStoreListCObj	getObjStoreListCObj();

    /**
     * Returns the top level broker list object.
     *
     * @return The top level broker list object.
     */
    public BrokerListCObj	getBrokerListCObj();


    /**
     * Sets the selected object in the application.
     */
    public void			setSelectedObj(ConsoleObj obj);

    /**
     * Returns the selected object.
     *
     * @return The selected object.
     */
    public ConsoleObj		getSelectedObj();

    /**
     * Sets the selected objects in the application.
     * <P>
     * Currently, the application only supports single selection
     * so this is not impemented.
     */
    public void			setSelectedObjs(ConsoleObj obj[]);

    /**
     * Returns the selected objects.
     * <P>
     * Currently, the application only supports single selection
     * so this is not impemented.
     *
     * @return the selected objects.
     */
    public ConsoleObj[]		getSelectedObjs();
}
