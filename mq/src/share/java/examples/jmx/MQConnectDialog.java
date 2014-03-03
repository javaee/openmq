/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MQConnectDialog extends JDialog 
		implements ActionListener {
    JButton apply, cancel;
    JTextField address, username;
    JPasswordField password;

    private boolean applyHit = false;
    private ActionListener applyListener = null;

    public MQConnectDialog(Frame parent, String title, 
			ActionListener applyListener)  {
	super(parent, title, true);
	this.applyListener = applyListener;
	initContentPane();
	pack();
    }

    public boolean applyDone()  {
	return (applyHit);
    }

    private void initContentPane()  {
	JPanel panel = new JPanel();

	panel.setLayout(new BorderLayout());
	/*
	 * Create 'work' panel
	 */
	JPanel workPanel = createWorkPanel();

	/*
	 * Create button panel
	 */
	JPanel buttonPanel = createButtonPanel();

	panel.add(workPanel, "Center");
	panel.add(buttonPanel, "South");

	getContentPane().add(panel);
    }

    private JPanel createWorkPanel()  {
	JPanel workPanel = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	JLabel l;

	workPanel.setLayout(gridbag);

	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.NONE;
	c.insets = new Insets(2, 2, 2, 2);
	c.ipadx = 0;
	c.ipady = 0;
	c.weightx = 1.0;

	c.gridx = 0;
	c.gridy = 0;
	l = new JLabel("Address:");
	gridbag.setConstraints(l,c);
	workPanel.add(l);

	c.gridx = 1;
	c.gridy = 0;
	address = new JTextField(20);
	gridbag.setConstraints(address,c);
	workPanel.add(address);

	c.gridx = 0;
	c.gridy = 1;
	l = new JLabel("Name:");
	gridbag.setConstraints(l,c);
	workPanel.add(l);

	c.gridx = 1;
	c.gridy = 1;
	username = new JTextField(20);
	gridbag.setConstraints(username, c);
	workPanel.add(username);

	c.gridx = 0;
	c.gridy = 2;
	l = new JLabel("Password:");
	gridbag.setConstraints(l,c);
	workPanel.add(l);

	c.gridx = 1;
	c.gridy = 2;
	password = new JPasswordField(20);
	gridbag.setConstraints(password, c);
	workPanel.add(password);

	return (workPanel);
    }

    public void setAddress(String s)  {
	address.setText(s);
    }
    public String getAddress()  {
	return (address.getText());
    }

    public void setUserName(String s)  {
	username.setText(s);
    }
    public String getUserName()  {
	return (username.getText());
    }

    public void setPassword(String s)  {
	password.setText(s);
    }
    public String getPassword()  {
	return (new String(password.getPassword()));
    }

    private JPanel createButtonPanel()  {
	JPanel buttonPanel = new JPanel();

	buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

	apply = new JButton("Apply");
	apply.addActionListener(this);
	if (applyListener != null)  {
	    apply.addActionListener(applyListener);
	}
	buttonPanel.add(apply);

	cancel = new JButton("Cancel");
	cancel.addActionListener(this);
	buttonPanel.add(cancel);

	return (buttonPanel);
    }

    public void actionPerformed(ActionEvent e)  {
	Object src = e.getSource();

	if (src == apply)  {
	    applyHit = true;
	    setVisible(false);
	} else if (src == cancel)  {
	    applyHit = false;
	    setVisible(false);
	}
    }
}
