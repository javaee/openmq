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
 * @(#)BrokerPasswdDialog.java	1.6 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.naming.Context;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.event.BrokerAdminEvent;
import com.sun.messaging.jmq.admin.apps.console.util.LabelledComponent;
import com.sun.messaging.jmq.admin.apps.console.util.LabelValuePanel;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;

/** 
 * This dialog is used for broker authentication.
 */
public class BrokerPasswdDialog extends AdminDialog implements ActionListener {
    
    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();
    private static String close[] = {acr.getString(acr.I_DIALOG_CLOSE)};

    private JTextField		username;
    private JTextField		password;
    private BrokerAdmin 	ba;

    /**
     * Creates a non-modal dialog using the specified frame as parent and string
     * as title. By default, will contain the following buttons:
     * <UL>
     * <LI>OK
     * <LI>CANCEL
     * <LI>HELP
     * </UL>
     *
     * @param parent the Frame from which the dialog is displayed
     * @param title the String to display in the dialog's title bar
     */
    public BrokerPasswdDialog(Frame parent)  {
	super(parent, acr.getString(acr.I_CONNECT_BROKER), (OK | CANCEL | HELP));
	setHelpId(ConsoleHelpID.CONNECT_BROKER);
    }

    public BrokerPasswdDialog(Frame parent, int whichButtons) {
	super(parent, acr.getString(acr.I_CONNECT_BROKER), whichButtons);
	setHelpId(ConsoleHelpID.CONNECT_BROKER);
    }

    public JPanel createWorkPanel()  {

	JPanel workPanel = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	workPanel.setLayout(gridbag);
	GridBagConstraints c = new GridBagConstraints();
	LabelledComponent items[] = new LabelledComponent[2];

	username = new JTextField(20);
	username.addActionListener(this);
	items[0] = new LabelledComponent(acr.getString(acr.I_BROKER_USERNAME), username);
	password = new JPasswordField(20);
	password.addActionListener(this);
	items[1] = new LabelledComponent(acr.getString(acr.I_BROKER_PASSWD), password);
	
	LabelValuePanel lvp = new LabelValuePanel(items, 5, 5);

	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	gridbag.setConstraints(lvp, c);
	workPanel.add(lvp);

	return (workPanel);
    }

    public void doOK() {

	/*
	 * Note:
	 * Not forcing the username and password to be mandatory,
	 * since the plugin authentication can require anything.
	 */	 
	String usernameValue = username.getText().trim();

        /*
	if (usernameValue.equals("")) {
            JOptionPane.showOptionDialog(this,
		acr.getString(acr.E_NO_PROP_VALUE, "username"),
		acr.getString(acr.I_BROKER),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);
            username.requestFocus();
            return;
	} 
	*/
	
	String passwordValue = password.getText().trim();
 
	/*
	if (passwordValue.equals("")) {
            JOptionPane.showOptionDialog(this,
		acr.getString(acr.E_NO_PROP_VALUE, "password"),
		acr.getString(acr.I_BROKER),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE, null, close, close[0]);
            password.requestFocus();
            return;
	}
	*/

	BrokerAdminEvent bae = 
		new BrokerAdminEvent(this, BrokerAdminEvent.UPDATE_LOGIN);
	bae.setUsername(usernameValue);
	bae.setPassword(passwordValue);
	bae.setOKAction(true);
	fireAdminEventDispatched(bae);

        username.requestFocus();
        if ((usernameValue.length() != 0) && (passwordValue.length() == 0))
            password.requestFocus();
    }

    public void doApply() { }
    public void doReset() { }

    public void doCancel() { hide(); }

    public void doClose() { hide(); }

    public void doClear() { 
	username.setText("");
	password.setText("");
    }

    public void show(BrokerAdmin ba) {
	
	this.ba = ba;

        doClear();
	String usernameValue = ba.getUserName();
	String passwordValue = ba.getPassword();

        username.requestFocus();

        /*
	 * Missing both.
	 */
	if ((usernameValue.length() == 0) && (passwordValue.length() == 0)) {

 	/* 
         * Missing username only.
         */
	} else if (usernameValue.length() == 0) {
	    password.setText(passwordValue);
 	/* 
         * Missing password only.
         */
	} else {
	    username.setText(usernameValue);
            password.requestFocus();
	}

        setDefaultButton(OK);
        super.show();
    }

    /**********************************************************************
     * ActionListener
     */
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == username) {
            password.requestFocus();
        } else if (ev.getSource() == password) {
            doOK();
        } else {
	    super.actionPerformed(ev);
	}

    }
}
