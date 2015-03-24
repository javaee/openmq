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
 * @(#)ObjStorePropsDialog.java	1.17 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import javax.naming.Context;
import javax.swing.JOptionPane;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.apps.console.event.ObjAdminEvent;
import com.sun.messaging.jmq.admin.objstore.ObjStore;
import com.sun.messaging.jmq.admin.objstore.ObjStoreAttrs;
import com.sun.messaging.jmq.admin.objstore.ObjStoreManager;

/** 
 * The inspector component of the admin GUI displays attributes
 * of the currently selected item. It does not know or care about
 * what is currently selected. It is basically told what to display
 * i.e. what object's attributes to display.
 * <P>
 * There are a variety of objects that can be <EM>inspected</EM>:
 * <UL>
 * <LI>Collection of object stores
 * <LI>Individual object stores
 * <LI>Collection of brokers
 * <LI>Individual brokers
 * <LI>Collection of services
 * <LI>Individual services
 * <LI>Collection of Topics
 * <LI>Individual Topics
 * <LI>Collection of Queues
 * <LI>Individual Queues
 * <LI>
 * </UL>
 *
 * For each of the object types above, a different inspector panel
 * is potentially needed for displaying the object's attributes.
 * This will be implemented by having a main panel stacking all the 
 * different property panels in CardLayout. For each object that
 * needs to be inspected, the object needs to be passed in to the
 * inspector as well as it's type.
 */
public class ObjStorePropsDialog extends ObjStoreDialog  {
    
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();
    private static String close[] = {acr.getString(acr.I_DIALOG_CLOSE)};
    private ObjStoreCObj osCObj;

    public ObjStorePropsDialog(Frame parent, ObjStoreListCObj oslCObj)  {
	super(parent, acr.getString(acr.I_OBJSTORE_PROPS), 
				(OK | CANCEL | CLOSE | HELP), oslCObj);
	setHelpId(ConsoleHelpID.OBJECT_STORE_PROPS);
    }

    public void doOK()  {
	
	String osName = null;

	//if (osTextButton.isSelected()) {
	
	    osName = osText.getText();
	    osName = osName.trim();
	    //
	    // Make sure store name is not empty.
	    //
	    if (osName.equals("")) {
	        JOptionPane.showOptionDialog(this,
		    acr.getString(acr.E_NO_OBJSTORE_NAME),
		    acr.getString(acr.I_OBJSTORE_PROPS) + ": " +
                        acr.getString(acr.I_ERROR_CODE,
                        	      AdminConsoleResources.E_NO_OBJSTORE_NAME),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, close, close[0]);
		osText.requestFocus();
	        return;
	    }
/*
	} else if (urlButton.isSelected()) {
           // Make sure a provider.url property was set.
            osName = jndiProps.getProperty(Context.PROVIDER_URL);
            if (osName == null || osName.equals("")) {
                JOptionPane.showOptionDialog(this,
                    acr.getString(acr.E_NO_PROVIDER_URL, Context.PROVIDER_URL),
                    acr.getString(acr.I_OBJSTORE_PROPS) + ": " +
                        acr.getString(acr.I_ERROR_CODE,
                        	      AdminConsoleResources.E_NO_PROVIDER_URL),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, close, close[0]);
	        comboBox.setSelectedItem(Context.PROVIDER_URL);
                valueText.requestFocus();
                return;
            }

	}
*/

	ObjAdminEvent oae;
	ObjStoreAttrs osa = constructAttrs(osName);

	if (osa == null)
	    return;

	oae = new ObjAdminEvent(this, ObjAdminEvent.UPDATE_OBJSTORE);
	oae.setObjStoreID(osName);
	oae.setObjStore((this.osCObj).getObjStore());
	oae.setObjStoreAttrs(osa);
	//oae.setConnectAttempt(checkBox.isSelected());
	oae.setConnectAttempt(false);
	oae.setOKAction(true);
	fireAdminEventDispatched(oae);
    }

    public void doApply() { }

    public void doReset() { }
    public void doCancel() { hide(); }
    public void doClose() { hide(); }
    public void doClear() { }

    public void show(ObjStoreCObj osCObj)  {
	this.osCObj = osCObj;
	ObjStore os = osCObj.getObjStore();

	checkBox.setSelected(true);

	ObjStoreAttrs attrs = os.getObjStoreAttrs();
	jndiProps.clear();
	if (attrs != null) {
	    for (java.util.Enumeration e = attrs.keys(); e.hasMoreElements(); ) {
		String key = (String)e.nextElement();
		jndiProps.setProperty(key, (String)attrs.get(key));
	    }
	    model.fireTableRowsInserted(0, attrs.size()-1);
   	    // Select the first one in the list.
	    if (attrs.size() >= 1) {
		table.setRowSelectionInterval(0, 0);
	    } 
	} else {
	    comboBox.setSelectedIndex(0);
	    delButton.setEnabled(false);
	    chgButton.setEnabled(false);
	    model.fireTableDataChanged();
	    valueText.requestFocus();
	}

	// If Provider URL = object store name, then
	// select provider url button.
	// Otherwise, select use own name button and fill in.
	//
/*
	String urlName = jndiProps.getProperty(Context.PROVIDER_URL);
	if (urlName != null && urlName.equals(os.getID())) {
	    urlButton.setSelected(true);
	    doUrlButton();
	    osText.setText("");
	    urlButton.requestFocus();
	} else {
	    osTextButton.setSelected(true);
	    doOsTextButton();
	    osText.setText(os.getID());
	    osTextButton.requestFocus();
   	}
*/
	osText.setText(os.getID());
	if (os.isOpen()) {
	    setEditable(false);
	} else {
	    setEditable(true);
	}
	
	super.show();
    }

    private ObjStoreAttrs constructAttrs(String osName)  {

	ObjStore os = osCObj.getObjStore();
	//
	// If they changed the os name to something else,
	// make sure it doesn't already exist.
	//
	String prevOsName = os.getID();

	if (!prevOsName.equals(osName) &&	// they changed it
	    osMgr != null && 
	    osMgr.getStore(osName) != null)  {

	    JOptionPane.showOptionDialog(this,
		acr.getString(acr.E_OBJSTORE_NAME_IN_USE, osName),
		acr.getString(acr.I_OBJSTORE_PROPS) + ": " +
                    acr.getString(acr.I_ERROR_CODE,
                    		  AdminConsoleResources.E_OBJSTORE_NAME_IN_USE),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);

            osText.requestFocus();
            osText.selectAll();

	    return (null);
	}

	ObjStoreAttrs osa = new ObjStoreAttrs(osName, osName);

	if (jndiProps == null) {
	    return (osa);
	}

        // Check for any properties that MUST be set.
        if (checkMandatoryProps() == 0) {
	    return null;
	}

	for (java.util.Enumeration e = jndiProps.propertyNames(); 
					e.hasMoreElements();) {
		String propName = (String)e.nextElement();
	 	osa.put(propName, jndiProps.getProperty(propName));
	}

	return (osa);
    }


    protected void setEditable(boolean editable) {

        if (editable) {
            okButton.setVisible(true);
            closeButton.setVisible(false);
            cancelButton.setVisible(true);
            buttonPanel.doLayout();
	
        } else {
            okButton.setVisible(false);
            closeButton.setVisible(true);
            cancelButton.setVisible(false);
            buttonPanel.doLayout();
        }

	super.setEditable(editable);
    }
}
