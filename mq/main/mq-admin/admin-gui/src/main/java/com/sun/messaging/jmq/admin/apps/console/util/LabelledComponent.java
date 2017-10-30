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
 * @(#)LabelledComponent.java	1.13 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console.util;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class implements a panel that contains a 
 * JLabel and a component horizontally separated by a colon. 
 *       Label: Component
 * It can be in conjunction with LabelValuePanel which aligns
 * a set fo LabelledComponents such that the colons are lined up.
 *       Label1: Component1
 *       Label2: Component2
 *   LongLabel3: LongComponent3
 *
 * A secondary label can also be specified. This label is placed after
 * (to the right) of the component:
 *	label1: Component label2
 * For example:
 *	Time:	[textfield] seconds
 *
 * <P>
 * This class contains methods to get the width of the primary label 
 * and the component, but not the secondary label.
 *
 * <P>
 * THis class also allows for the alignment of the label with respect to
 * the component, to be specified.
 *
 */
public class LabelledComponent extends JPanel  {

    public static final int	NORTH = 0;
    public static final int	CENTER = 1;
    public static final int	SOUTH = 2;

    private JLabel	label;
    private JLabel	label2;
    private JComponent  component;
    private JPanel	panel;
    private int		align = CENTER;
    private Object      userData = null;

    public LabelledComponent(String s, JComponent c)  {
	this(s, c, CENTER);
    }

    public LabelledComponent(String s, JComponent c, int align)  {
	this(new JLabel(s, JLabel.RIGHT), c, null, align);
    }

    public LabelledComponent(String s, JComponent c, String s2)  {
	this(s, c, s2, CENTER);
    }

    public LabelledComponent(String s, JComponent c, String s2, int align)  {
	this(new JLabel(s, JLabel.RIGHT), c, new JLabel(s2, JLabel.RIGHT), align);
    }

    /*
     * Don't really need this
    public LabelledComponent(JLabel l, JComponent c)  {
	this(l, c, null);
    }
    */

    public LabelledComponent(JLabel l, JComponent c, JLabel l2, int align)  {
	label = l;
	label2 = l2;
	component = c;
	this.align = align;
	initPanel();
    }

    public JPanel getLabelledComponent()  {
	return panel;
    }

    public JLabel getLabel()  {
	return label;
    }

    public JComponent getComponent()  {
	return component;
    }

    public int getLabelWidth()  {
	return label.getPreferredSize().width;
    }

    public int getComponentWidth()  {
	return component.getPreferredSize().width;
    }

    public void setClientData(Object userData)  {
	this.userData = userData;
    }

    public Object getClientData()  {
	return this.userData;
    }

    public void setLabelText(String s)  {
	if ((label == null) || (s == null))
	    return;
	
	label.setText(s);
    }

    public void setLabelFont(Font f)  {
	if ((label == null) || (f == null))
	    return;
	
	label.setFont(f);
    }

    public void setEnabled(boolean b)  {
        if (label != null)  {
	    label.setEnabled(b);
	}
        if (label2 != null)  {
	    label2.setEnabled(b);
	}
        if (component != null)  {
	    enableComponents(component, b);
	}
    }

    /*
     * Enable/disable all the components within the comp component.
     */
    private void enableComponents(JComponent comp, boolean b) {
        for (int i = 0; i < comp.getComponentCount(); i++)
            comp.getComponent(i).setEnabled(b);
    }

    private void initPanel()  {

	GridBagLayout gridbag = new GridBagLayout();
	setLayout(gridbag);
	GridBagConstraints gbc = new GridBagConstraints();
	int	labelAnchor;

        /*
    	 * Put the label on the left side.
  	 * against the right of the right side of the 2x1 grid.
	 */
	label.setHorizontalAlignment(JLabel.RIGHT);
	gbc.gridx = 0;
	gbc.gridy = 0;

	switch (align)  {
	case NORTH:
	    labelAnchor = GridBagConstraints.NORTHEAST;
	break;

	case CENTER:
	    labelAnchor = GridBagConstraints.CENTER;
	break;

	case SOUTH:
	    labelAnchor = GridBagConstraints.SOUTHEAST;
	break;

	default:
	    labelAnchor = GridBagConstraints.CENTER;
	break;
	}

	gbc.anchor = labelAnchor;

	/*
	if (c instanceof JScrollPane || c instanceof JPanel)
	    gbc.anchor = GridBagConstraints.NORTHEAST;
	*/

	gridbag.setConstraints(label, gbc);

        /*
    	 * Put the value component on the right side.
  	 * against the left side of the 2x1 grid.
	 * Move it over 5 pixels so that there is a space
	 * after the label.
	 */
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.insets = new Insets(0, 5, 0, 0);  // value is 5 pixels to the right
	gbc.weightx = 1.0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gridbag.setConstraints(component, gbc);

	add(label);
	add(component);

	if (label2 != null)  {
            /*
    	     * Put the right label component on the right of the value.
	     * Move it over 5 pixels so that there is a space
	     * between the value and the right label.
	     */
	    gbc.gridx = 2;
	    gbc.gridy = 0;
	    gbc.insets = new Insets(0, 5, 0, 0);  // value is 5 pixels to the right
	    gbc.anchor = labelAnchor;
	    gbc.weightx = 0;
	    gridbag.setConstraints(label2, gbc);

	    add(label2);
	}

    }
}
