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
 * @(#)AController.java	1.54 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;


import java.awt.Container;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.event.AdminEvent;
import com.sun.messaging.jmq.admin.event.BrokerAdminEvent;
import com.sun.messaging.jmq.admin.event.AdminEventListener;
import com.sun.messaging.jmq.admin.apps.console.event.DialogEvent;
import com.sun.messaging.jmq.admin.apps.console.event.SelectionEvent;
import com.sun.messaging.jmq.admin.apps.console.event.ObjAdminEvent;
import com.sun.messaging.jmq.admin.apps.console.event.ConsoleActionEvent;

/** 
 * The controller component basically listens for events from
 * all the other admin UI components and reacts to them.
 * <P>
 * Unlike the other UI components, the controller does know
 * about all the individual pieces of the admin UI. The controller
 * knows this via the AdminApp object. With it, it can control
 * the entire admin console application.
 * <P>
 * The controller delegates the object administration and broker
 * administration tasks to ObjAdminHandler and BrokerAdminHandler.
 *
 * @see ObjAdminHandler
 * @see BrokerAdminHandler
 */
public class AController implements AdminEventListener  {

    private ObjAdminHandler	objAdminHandler;
    private BrokerAdminHandler	brokerAdminHandler;
    private AboutDialog         aboutDialog = null;

    private AdminApp	app;

    /**
     * Create/initialize the admin explorer GUI component.
     */
    public AController(AdminApp app) {
	this.app = app;
	objAdminHandler = new ObjAdminHandler(app, this);
	brokerAdminHandler = new BrokerAdminHandler(app, this);
    } 

    public void init()  {
	// Turn off setScrollToVisbible() just at startup
	app.getExplorer().setScrollToPath(false);
	objAdminHandler.init();
	brokerAdminHandler.init();
	
	// Now expand all the nodes.
	app.getExplorer().expandAll();

	// Now turn on setScrollToVisbible() back.
	app.getExplorer().setScrollToPath(true);
    }



    /*
     * BEGIN INTERFACE AdminEventListener
     */
    public void adminEventDispatched(AdminEvent e)  {
	int id;
	ConsoleObj selObj;
	
	if (e instanceof DialogEvent)  {
	    handleDialogEvents((DialogEvent)e);
	} else if (e instanceof SelectionEvent)  {
	    handleSelectionEvents((SelectionEvent)e);
	} else if (e instanceof ObjAdminEvent)  {
	    handleObjAdminEvents((ObjAdminEvent)e);
        } else if (e instanceof BrokerAdminEvent)  {
            handleBrokerAdminEvents((BrokerAdminEvent)e);
        } else if (e instanceof ConsoleActionEvent)  {
            handleConsoleActionEvents((ConsoleActionEvent)e);
	}
    }
    /*
     * END INTERFACE AdminEventListener
     */

    private void handleDialogEvents(DialogEvent de) {

	ConsoleObj selObj = app.getSelectedObj();
        //int dialogType = de.getDialogType();

        if (selObj instanceof ObjStoreAdminCObj)   {
	    objAdminHandler.handleDialogEvents(de);
	} else if (selObj instanceof BrokerAdminCObj)  {
	    brokerAdminHandler.handleDialogEvents(de);
	}
    }

    private void handleSelectionEvents(SelectionEvent se) {
        ConsoleObj	selObj = se.getSelectedObj();
	Object		source = se.getSource();
        int		type = se.getType();
	boolean 	fromExplorer = true;

	if (source instanceof Container)  {
	    Container c = (Container)source;
	    
	    if (app.getInspector().isAncestorOf(c))  {
		fromExplorer = false;
	    }
	}

        switch (type)  {
        case SelectionEvent.OBJ_SELECTED:
	    app.setSelectedObj(selObj);

	    if (fromExplorer)  {
	        app.getInspector().clearSelection();

		if (selObj.canBeInspected())  {
	            app.getInspector().inspect(selObj);
		}
	    } else  {
	        app.getExplorer().clearSelection();
	    }

	     /*
	      * Activate/deactive actions, menu items, toolbar buttons.
	      */
	    setActions(selObj);

	    /*
	     * Here for debugging, need to remove when ship.
	    app.getStatusArea().appendText(selObj
				+ " ["
				+ selObj.getClass().getName()
				+ "]"
				+ " selected.\n");
	     */
        break;

        case SelectionEvent.CLEAR_SELECTION:
	    clearSelection();
	break;
        }
    }

    private void handleObjAdminEvents(ObjAdminEvent oae) {
	objAdminHandler.handleObjAdminEvents(oae);
    }


    /*
     * Clears any selected object,
     * Clears the inspector to empty.
     * Clears any menu items that don't apply when
     *   nothing is selected.
     */
    public void clearSelection() {
	app.setSelectedObj(null);
	app.getInspector().inspect(null);
	app.getActionManager().setActiveActions(0);
    }

   /*
    * Set buttons, menus based on selObj.
    */
    public void setActions(ConsoleObj selObj) {

	if (selObj == null) 
	    return;

        /*
         * Activate/deactivate actions based on selected object.
	 */
	app.getActionManager().setActiveActions(selObj.getActiveActions());

	/*
	 * Change labels on menu items based on selected object.
	 */
	app.getMenubar().setConsoleObj(selObj);

	/*
	 * Change tooltips on toolbar buttons based on selected object.
	 */
	app.getToolbar().setConsoleObj(selObj);

    }

    private void handleBrokerAdminEvents(BrokerAdminEvent bae) {
	brokerAdminHandler.handleBrokerAdminEvents(bae);
    }

    private void handleConsoleActionEvents(ConsoleActionEvent cae) {
        int type 		 = cae.getType();

	switch (type)  {
	case ConsoleActionEvent.EXIT:
	    doExit();
	break;

	case ConsoleActionEvent.ABOUT:
	    doAbout();
	break;

	case ConsoleActionEvent.EXPAND_ALL:
	    doExpandAll();
	break;

	case ConsoleActionEvent.COLLAPSE_ALL:
	    doCollapseAll();
	break;

	case ConsoleActionEvent.REFRESH:
	    doRefresh(cae);
	break;
	}
    }

    private void doExit()  {
	System.exit(0);
    }

    private void doAbout()  {
	if (aboutDialog == null) {
	    aboutDialog = new AboutDialog(app.getFrame());
	    aboutDialog.addAdminEventListener(this);
	    aboutDialog.setLocationRelativeTo(app.getFrame());
	}
	aboutDialog.show();
    }

    public void doExpandAll() {
	app.getExplorer().expandAll();
    }

    public void doCollapseAll() {
	app.getExplorer().collapseAll();
    }

    public void doRefresh(ConsoleActionEvent cae) {
	ConsoleObj selObj = app.getSelectedObj();

        if (selObj instanceof ObjStoreAdminCObj)  {
	    objAdminHandler.handleConsoleActionEvents(cae);
	} else if (selObj instanceof BrokerAdminCObj)  {
	    brokerAdminHandler.handleConsoleActionEvents(cae);
	}
    }

}
