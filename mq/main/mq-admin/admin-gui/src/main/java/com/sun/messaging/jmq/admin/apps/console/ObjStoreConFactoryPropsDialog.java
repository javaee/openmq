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
 * @(#)ObjStoreConFactoryPropsDialog.java	1.13 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.apps.console.event.ObjAdminEvent;
import com.sun.messaging.jmq.admin.apps.console.util.LabelledComponent;
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
public class ObjStoreConFactoryPropsDialog extends ObjStoreConFactoryDialog  {
    
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();
    private static String close[] = {acr.getString(acr.I_DIALOG_CLOSE)};
    private ObjStoreConFactoryCObj osConFactoryCObj;

    public ObjStoreConFactoryPropsDialog(Frame parent) {

	super(parent, acr.getString(acr.I_OBJSTORE_CF_PROPS), (OK | CANCEL | HELP));
	setHelpId(ConsoleHelpID.CF_OBJECT_PROPS);
    }

    public void doOK()  {

	/*
	 * Lookup Name
	 */
	String lookupName = lookupLabel.getText();
	lookupName = lookupName.trim();

	if (lookupName == null || lookupName.equals("")) {
	    JOptionPane.showOptionDialog(this, 
		acr.getString(acr.E_NO_LOOKUP_NAME),
		acr.getString(acr.I_OBJSTORE_CF_PROPS) + ": " +
                    acr.getString(acr.I_ERROR_CODE,
                    	AdminConsoleResources.E_NO_LOOKUP_NAME),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);
	    return;
	}
	
	/*
	 * Factory Type
	 */
	int type = ObjAdminEvent.QCF;
	AdministeredObject tempObj = null;

	if (cfLabel.getText().equals(acr.getString(acr.I_QCF))) {
	    type = ObjAdminEvent.QCF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.QueueConnectionFactory();
	} else if (cfLabel.getText().equals(acr.getString(acr.I_TCF))) {
	    type = ObjAdminEvent.TCF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.TopicConnectionFactory();
	} else if (cfLabel.getText().equals(acr.getString(acr.I_CF))) {
	    type = ObjAdminEvent.CF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.ConnectionFactory();
	} else if (cfLabel.getText().equals(acr.getString(acr.I_XAQCF))) {
	    type = ObjAdminEvent.XAQCF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.XAQueueConnectionFactory();
	} else if (cfLabel.getText().equals(acr.getString(acr.I_XATCF))) {
	    type = ObjAdminEvent.XATCF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.XATopicConnectionFactory();
	} else if (cfLabel.getText().equals(acr.getString(acr.I_XACF))) {
	    type = ObjAdminEvent.XACF;
	    tempObj = (AdministeredObject)
			new com.sun.messaging.XAConnectionFactory();
	}

	/*
	 * Conn Factory Object Properties.
	 * Go through each of the cfProps, get the userdata which is
	 * the property name, get the value, set it in props.
	 */
	Properties props = tempObj.getConfiguration();
	String propName, propValue, propType = null, propLabel = null;

	for (int i = 0; i < cfProps.size(); i++) {
	    LabelledComponent cfItem = (LabelledComponent)cfProps.elementAt(i);
            propName = (String)cfItem.getClientData();
            if (propName == null) 
		continue;

	    // Remove this propName from the props, no longer applies.
            if (!(cfItem.getComponent().isEnabled())) {
		props.remove(propName);
                continue;
            }

	    try {
                propType = tempObj.getPropertyType(propName);
                propLabel = tempObj.getPropertyLabel(propName);
	    } catch (javax.jms.JMSException jmsex) {
	    	JOptionPane.showOptionDialog(this, 
			jmsex.toString(),
			acr.getString(acr.I_OBJSTORE_CF_PROPS),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, close, close[0]);
		return;
	    }

	    if (propType == null) 
		continue;

	    propValue = getValue(cfItem.getComponent(), propType).trim();

            // If blank, then use default set in Administered Object
            // so no need to set to "".
            if (propValue.equals(""))
                continue;

 	    try {
	        // Calling setProperty() will verify if this value is valid.
		tempObj.setProperty(propName, propValue.trim());
		props.put(propName, propValue.trim());
	    } catch (javax.jms.JMSException jmsex) {
		if (jmsex instanceof com.sun.messaging.InvalidPropertyValueException) {
	    	    JOptionPane.showOptionDialog(this, 
			acr.getString(acr.E_INVALID_VALUE, propLabel),
			acr.getString(acr.I_OBJSTORE_CF_PROPS) + ": " +
                                acr.getString(acr.I_ERROR_CODE,
                                	AdminConsoleResources.E_INVALID_VALUE),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, close, close[0]);
		} else { 
	    	    JOptionPane.showOptionDialog(this, 
			jmsex.toString(),
			acr.getString(acr.I_OBJSTORE_CF_PROPS),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, close, close[0]);
		}
		return;
	    }
	}

	ObjAdminEvent oae = new ObjAdminEvent(this, ObjAdminEvent.UPDATE_CONN_FACTORY);
	ObjStore os = osConFactoryCObj.getObjStore();

	/*
	 * Set values in the event.
	 */
	oae.setLookupName(lookupName);
	oae.setObjStore(os);  
	oae.setFactoryType(type);
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
    public void show(ObjStoreConFactoryCObj osConFactoryCObj)  {

	this.osConFactoryCObj = osConFactoryCObj;
	ObjStore os = osConFactoryCObj.getObjStore();
	//
	// Set fields to current object values.
	//
	lookupLabel.setText(osConFactoryCObj.getLookupName());

	Object object = osConFactoryCObj.getObject();
	if (object instanceof com.sun.messaging.XAQueueConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_XAQCF));
	else if (object instanceof com.sun.messaging.XATopicConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_XATCF));
	else if (object instanceof com.sun.messaging.XAConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_XACF));
	else if (object instanceof com.sun.messaging.QueueConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_QCF));
	else if (object instanceof com.sun.messaging.TopicConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_TCF));
	else if (object instanceof com.sun.messaging.ConnectionFactory)
	    cfLabel.setText(acr.getString(acr.I_CF));

	//
	// Go through each of the component items, get the clientData
	// which is the property name, then get the value of the
	// property from the current adminObj.
	//
	String propName = null;
	String propType = null;
	String value = null;

	AdministeredObject adminObj = (AdministeredObject)object;
	Properties props = adminObj.getConfiguration();

	for (int i = 0; i < cfProps.size(); i++) {
	    LabelledComponent cfItem = (LabelledComponent)cfProps.elementAt(i);
	    JComponent comp = cfItem.getComponent();
	    propName = (String)cfItem.getClientData();
	    if (propName == null)
		continue;

	    try {
		if (comp.isEnabled()) {
                    propType = adminObj.getPropertyType(propName);
                    value = adminObj.getProperty(propName);
                    setValue(comp, propType, value);

                    // XXX This assumes the ConnectionType setting
                    // comes before the getting of the connection handler
                    // properties. Also, hard codes JMQConnectionType
                    // setting.
	            if (comp instanceof JComboBox) {
		        doComboBox((JComboBox)comp);
	 	    }
		}
            } catch (Exception e) {
                System.err.println(e.toString() + ": " + propName);
            }

	}

	// now we should be able to set values.
	AdministeredObject tempObj2 = new com.sun.messaging.QueueConnectionFactory();
	setOtherValues(tempObj2, true);

	// Set read-only box
	if (adminObj.isReadOnly())
	    checkBox.setSelected(true);
	else
	    checkBox.setSelected(false);
	
	
	/*
	 * Set focus to first text item.
	if (props.size() > 0)
	    textItems[0].requestFocus();
	 */

	// Go back to first tab.
	tabbedPane.setSelectedIndex(0);
	super.show();
    }

}
