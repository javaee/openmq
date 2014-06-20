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
 * @(#)SpecialValueField.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console.util;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/**
 * This class is a panel that is used to manage the layout
 * and enable state of components that can have normal values
 * as well as a special value.
 *
 * <P>
 * Examples of this are:
 * <UL>
 * <LI>bytes fields where you also need a way of specifying an 
 *     unlimited quantity.
 * <LI>timer fields to enter time in seconds where you also need
 *     a way of specifying that the timer is off.
 * </UL>
 *
 * <P>
 * This class is used primarily to avoid duplication of layout code
 * and code that enables/disables components. Getting the entered values
 * can be done via normal channels. This class does not have
 * convenience methods for getting the values since different
 * components have different ways of getting this.
 * 
 * <P>
 *
 * This class is implemented as a simple container containing the
 * component (field) that allows input of normal values as well
 * as a radio button that indicates whether these normal values
 * are valid or where some special value/meaning is valid instead.
 *
 * <P>
 *
 * This class allows you to configure:
 * <UL>
 * <LI>What field (JComponent) to use.
 * <LI>The string to display for the special case e.g. "Unlimited" or
 * "Off".
 * </UL>
 */
public class SpecialValueField extends JPanel 
				implements ActionListener  {
    private JComponent		comp;
    private JLabel		specialValueLabel;
    private JRadioButton	specialValueRB,
				normalValueRB;
    private String		specialValueStr;

    private boolean		specialValueSet = true;

    public SpecialValueField(JComponent comp, String specialValueStr)  {
	this.comp = comp;
	this.specialValueStr = specialValueStr;

	initGui();
        setSpecialValueSet(true);
    }

    public boolean isSpecialValueSet()  {
	return(specialValueSet);
    }

    public void setSpecialValueSet(boolean b)  {
	if (b)  {
	    specialValueRB.setSelected(true);
	    doSpecialValueRBSelected();
	} else  {
	    normalValueRB.setSelected(true);
	    doNormalValueRBSelected();
	}
    }

    public JComponent getComponent()  {
	return (comp);
    }

    public void setEnabled(boolean b)  {
	if (comp != null)  {
	    comp.setEnabled(b);
	}
	if (specialValueLabel != null)  {
	    specialValueLabel.setEnabled(b);
	}
	if (specialValueRB != null)  {
	    specialValueRB.setEnabled(b);
	}
	if (normalValueRB != null)  {
	    normalValueRB.setEnabled(b);
	}
    }


    public void actionPerformed(ActionEvent e)  {
        Object source = e.getSource();

	if (source == specialValueRB)  {
	    doSpecialValueRBSelected();
	} else if (source == normalValueRB)  {
	    doNormalValueRBSelected();
	} 
	/*
	else  {
	    super.actionPerformed(e);
	}
	*/
    }

    private void doSpecialValueRBSelected()  {
        specialValueLabel.setEnabled(true);
        comp.setEnabled(false);
	specialValueSet = true;
    }
    private void doNormalValueRBSelected()  {
        specialValueLabel.setEnabled(false);
        comp.setEnabled(true);
	specialValueSet = false;
    }

    private void initGui() {
        GridBagLayout		gbl;
        GridBagConstraints	gbc;

	gbl = new GridBagLayout();
	gbc = new GridBagConstraints();
        setLayout(gbl);

	/*
	 * Common constraints
	 */
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.gridwidth = 1;

	/*
	 * Radio button for selecting special value
	 * (e.g. "Unlimited")
	 */
	gbc.gridx = 0;
	gbc.gridy = 0;
        specialValueRB = new JRadioButton();
        specialValueRB.addActionListener(this);
	gbl.setConstraints(specialValueRB, gbc);
	add(specialValueRB);

	/*
	 * Label for special value e.g. "Unlimited"
	 */
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.BOTH;
        specialValueLabel = new JLabel(specialValueStr, JLabel.LEFT);
	gbl.setConstraints(specialValueLabel, gbc);
	add(specialValueLabel);

	/*
	 * Reset
	 */
	gbc.fill = GridBagConstraints.NONE;

	/*
	 * Radio button for selecting 'normal' value.
	 */
	gbc.gridx = 0;
	gbc.gridy = 1;
        normalValueRB = new JRadioButton();
        normalValueRB.addActionListener(this);
	gbl.setConstraints(normalValueRB, gbc);
	add(normalValueRB);

	/*
	 * The component/field displaying the normal
	 * value.
	 */
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbl.setConstraints(comp, gbc);
	add(comp);

        ButtonGroup bg = new ButtonGroup();
        bg.add(specialValueRB);
        bg.add(normalValueRB);
    }
}

