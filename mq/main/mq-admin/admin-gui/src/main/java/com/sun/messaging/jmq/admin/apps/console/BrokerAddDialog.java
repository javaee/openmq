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
 * @(#)BrokerAddDialog.java	1.11 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import javax.swing.JOptionPane;

import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdminException;
import com.sun.messaging.jmq.admin.event.BrokerAdminEvent;

/** 
 * This dialog is used to add new brokers to the list of
 * brokers displayed in the administration console.
 */
public class BrokerAddDialog extends BrokerDialog  {
    public static final String DEFAULT_BROKER_HOST 	= "localhost";
    public static final String DEFAULT_PRIMARY_PORT 	= "7676";

    private BrokerListCObj blCObj;

    public BrokerAddDialog(Frame parent, BrokerListCObj blCObj) {
	super(parent, acr.getString(acr.I_ADD_BROKER), (OK | RESET | CANCEL | HELP));
	setHelpId(ConsoleHelpID.ADD_BROKER);
	this.blCObj = blCObj;
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
        BrokerAdminEvent bae = new BrokerAdminEvent(this, BrokerAdminEvent.ADD_BROKER);
	bae.setConnectAttempt(false);
	bae.setBrokerName(brokerName);
	bae.setHost(hostTF.getText());
	bae.setPort(Integer.parseInt(portTF.getText()));
	bae.setUsername(userTF.getText());
	bae.setPassword(String.valueOf(passwdTF.getPassword()));
        bae.setOKAction(true);
        fireAdminEventDispatched(bae);
    }

    public void doReset() { 
	reset();
    } 

    public void doCancel() {
	hide(); 
	reset();
    }

    // not used
    public void doApply() {}
    public void doClear() {}
    public void doClose() {}

    public void show() {
	doReset();
	setEditable(true);
	super.show();
    }

    private void reset() {
	brokerNameTF.setText(getBrokerName(acr.getString(acr.I_BROKER_LABEL)));
	hostTF.setText(DEFAULT_BROKER_HOST);
        portTF.setText(DEFAULT_PRIMARY_PORT);
        userTF.setText(BrokerAdmin.DEFAULT_ADMIN_USERNAME);
        passwdTF.setText("");
    }

    protected String getBrokerName(String baseName)  {

	ConsoleBrokerAdminManager baMgr = blCObj.getBrokerAdminManager();

        if (!baMgr.exist(baseName))  {
            return (baseName);
        }

        for (int i = 1; i < 1000; ++i)  {
            String newStr = baseName + i;
            if (!baMgr.exist(newStr))  {
                return (newStr);
            }
        }

        return ("");
    }
}
