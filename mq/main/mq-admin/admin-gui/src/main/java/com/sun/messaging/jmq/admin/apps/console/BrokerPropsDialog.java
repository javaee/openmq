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
 * @(#)BrokerPropsDialog.java	1.7 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import javax.swing.JOptionPane;

import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;
import com.sun.messaging.jmq.admin.event.BrokerAdminEvent;

/** 
 * This dialog is used for viewing/changing the connection
 * properties of a broker. 
 * <P>
 * Note: This dialog is not used to query a broker's attributes
 * (as in "imqcmd query bkr").
 * <P>
 */
public class BrokerPropsDialog extends BrokerDialog  {
    private BrokerCObj bCObj;
    private BrokerAdmin ba;

    public BrokerPropsDialog(Frame parent) {
	super(parent, 
		acr.getString(acr.I_BROKER_PROPS), 
		(OK | CANCEL | CLOSE | HELP));
	setHelpId(ConsoleHelpID.BROKER_PROPS);
    }

    public void doOK() {
	String	brokerName = null;

	brokerName = brokerNameTF.getText();
	brokerName = brokerName.trim();

	if (brokerName.equals(""))  {
            JOptionPane.showOptionDialog(this,
            	acr.getString(acr.E_NO_BROKER_NAME),
            	acr.getString(acr.I_ADD_BROKER) 
	    	    + ": " 
	    	    + acr.getString(acr.I_ERROR_CODE, acr.E_NO_BROKER_NAME),
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.ERROR_MESSAGE, null, close, close[0]);
            return;
        }

	// Check to make sure host and port are non-empty
	if (!isValidString (hostTF.getText()) || 
	    !isValidString (portTF.getText())) {

	    JOptionPane.showOptionDialog(this,
                acr.getString(acr.E_NO_BROKER_HOST_PORT),
                acr.getString(acr.I_ADD_BROKER) + ": " 
		        + acr.getString(acr.I_ERROR_CODE, acr.E_NO_BROKER_HOST_PORT),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);
	    return;
	}

        BrokerAdminEvent bae = new BrokerAdminEvent(this, 
				BrokerAdminEvent.UPDATE_BROKER_ENTRY);
	bae.setConnectAttempt(false);
	bae.setBrokerName(brokerName);
	bae.setHost(hostTF.getText());
	bae.setPort(Integer.parseInt(portTF.getText()));
	bae.setUsername(userTF.getText());
	bae.setPassword(String.valueOf(passwdTF.getPassword()));
        bae.setOKAction(true);
        fireAdminEventDispatched(bae);
    }

    public void doCancel() {
	hide(); 
	clearFields();
    }

    // not used
    public void doReset() {}
    public void doApply() {}
    public void doClear() {}
    public void doClose() {
	hide();
	clearFields();
    }

    public void show() {
	if (ba.isConnected()) {
	    setEditable(false);
        } else {
	    setEditable(true);
        }
	super.show();
    }

    public void setBrokerCObj(BrokerCObj bCObj)  {
	String tmp;


	if (bCObj == null)  {
	    clearFields();

	    return;
	}

	ba = bCObj.getBrokerAdmin();

	tmp = ba.getKey();
	brokerNameTF.setText(tmp);

	tmp = ba.getBrokerHost();
	hostTF.setText(tmp);

	tmp = ba.getBrokerPort();
        portTF.setText(tmp);

	tmp = ba.getUserName();
        userTF.setText(tmp);

	tmp = ba.getPassword();
        passwdTF.setText(tmp);

	this.bCObj = bCObj;
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
