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
 * @(#)ObjStoreDestPropsDialog.java	1.17 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JOptionPane;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.apps.console.event.ObjAdminEvent;
import com.sun.messaging.jmq.admin.objstore.ObjStore;

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
public class ObjStoreDestPropsDialog extends ObjStoreDestDialog  {
    
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();
    private static String close[] = {acr.getString(acr.I_DIALOG_CLOSE)};
    private static String okcancel[] = {"OK", "Cancel"};
    private ObjStoreDestCObj osDestCObj;

    public ObjStoreDestPropsDialog(Frame parent)  {
	super(parent, acr.getString(acr.I_OBJSTORE_DEST_PROPS), (OK | CANCEL | HELP));
	setHelpId(ConsoleHelpID.DEST_OBJECT_PROPS);
    }

    public void doOK()  {
        Object object = osDestCObj.getObject();

	// Check to see if the retrieved object's version is
	// compatible with the current product version.
	// No need to check for invalid/missing version number, as
	// an exception must have been already thrown if that 
	// was the case.
        if (object instanceof AdministeredObject) {
	    AdministeredObject adminObj = (AdministeredObject)object;
	    String curVersion = adminObj.getVERSION();
	    String objVersion = adminObj.getStoredVersion();

            if (!adminObj.isStoredVersionCompatible()) {
		int response = JOptionPane.showOptionDialog(this, 
                    acr.getString(acr.W_INCOMPATIBLE_OBJ, objVersion, curVersion),
                    acr.getString(acr.I_OBJSTORE_DEST_PROPS)
                        + ": "
                        + acr.getString(acr.I_WARNING_CODE, acr.W_INCOMPATIBLE_OBJ),
            	    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
		    null, okcancel, okcancel[1]);

	        if (response == JOptionPane.NO_OPTION)
		    return;
	    }
	}

	/*
	 * Lookup Name
	 */
	String lookupName = lookupLabel.getText();
	lookupName = lookupName.trim();

	if (lookupName == null || lookupName.equals("")) {
	    JOptionPane.showOptionDialog(this, 
		acr.getString(acr.E_NO_LOOKUP_NAME),
		acr.getString(acr.I_OBJSTORE_DEST_PROPS) + ": " +
                    acr.getString(acr.I_ERROR_CODE,
                    		  AdminConsoleResources.E_NO_LOOKUP_NAME),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);
	    return;
	}
	
	/*
	 * Destination Type
	 */
	int type = ObjAdminEvent.QUEUE;
	AdministeredObject tempObj = null;

	if (destLabel.getText().equals(acr.getString(acr.I_QUEUE))) {
	    type = ObjAdminEvent.QUEUE;
	    tempObj = (AdministeredObject)new com.sun.messaging.Queue();
	}
	else if (destLabel.getText().equals(acr.getString(acr.I_TOPIC))) {
	    type = ObjAdminEvent.TOPIC;
	    tempObj = (AdministeredObject)new com.sun.messaging.Topic();
	}

	/*
	 * Object Properties (dest name, ...);
	 */
	int i = 0;
	Properties props = tempObj.getConfiguration();
	for (Enumeration e = tempObj.enumeratePropertyNames(); 
				e.hasMoreElements(); i++) {
	    String propName = (String)e.nextElement();
	    String value = textItems[i].getText();
	    value = value.trim();

            // If blank, then use default set in Administered Object
            // so no need to set to "".
            if (!(value.trim()).equals("")) {
                props.put(propName, value);
            }
	}

	
	ObjAdminEvent oae = new ObjAdminEvent(this, ObjAdminEvent.UPDATE_DESTINATION);
	ObjStore os = osDestCObj.getObjStore();

	/*
	 * Set values in the event.
	 */
	oae.setLookupName(lookupName);
	oae.setObjStore(os);  
	oae.setDestinationType(type);
	oae.setObjProperties(props);
        if (checkBox.isSelected())
            oae.setReadOnly(true);
        else
            oae.setReadOnly(false);
	oae.setOKAction(true);
	fireAdminEventDispatched(oae);
    }

    public void doApply()  { }
    public void doReset() { }
    public void doCancel() { hide(); }
    public void doClose() { hide(); }
    public void doClear() { }

    public void show()  { }
    public void show(ObjStoreDestCObj osDestCObj)  {

	this.osDestCObj = osDestCObj;
	ObjStore os = osDestCObj.getObjStore();
	//
	// Set fields to current destination values.
	//
	lookupLabel.setText(osDestCObj.getLookupName());

	Object object = osDestCObj.getObject();
	if (object instanceof com.sun.messaging.Queue)
	    destLabel.setText(acr.getString(acr.I_QUEUE));
	else
	    destLabel.setText(acr.getString(acr.I_TOPIC));

	//
	// Create a temp object and set its default values in the
	// text fields.
	//
	AdministeredObject adminObj = (AdministeredObject)object;
	Properties props = adminObj.getConfiguration();
	int i = 0;
	for (Enumeration e = adminObj.enumeratePropertyNames(); 
					e.hasMoreElements(); i++) {
	    String propName = (String)e.nextElement();
	    try {
	        textItems[i].setText(adminObj.getProperty(propName));
	    } catch (Exception ex) {
	        textItems[i].setText("");
	    }
	}
	//
	// Set Read-only field.
	//
	if (adminObj.isReadOnly())
	    checkBox.setSelected(true);
	else
	    checkBox.setSelected(false);
	
	/*
	 * Set focus to first text item.
	 */
	if (props.size() > 0)
	    textItems[0].requestFocus();

	super.show();
    }

}
