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
 * @(#)LabelValuePanel.java	1.5 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LabelValuePanel extends JPanel {

    private int 		vgap = 5;
    private int 		hgap = 5;
    private JPanel 		panel;
    private LabelledComponent 	items[];

    public LabelValuePanel(LabelledComponent items[])  {
        this.items = items;
	
	init();
    }

    public LabelValuePanel(LabelledComponent items[], int hgap, int vgap)  {
        this.items = items;
  	this.hgap = hgap;
  	this.vgap = vgap;
	
	init();
    }


    public LabelledComponent[] getLabelledComponents() {
	return this.items;
    }

    private void init() {

	int numItems = items.length;
	int longest = 0;

	setBorder(BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));
	/*
	 * Find the longest label while adding the 
	 * LabelledComponents to the panel.
	 */
	GridBagLayout gridbag = new GridBagLayout();
	setLayout(gridbag);
	GridBagConstraints c = new GridBagConstraints();

	for (int i = 0; i < numItems; i++) {
	    if (items[i].getLabelWidth() > longest) {
		longest = items[i].getLabelWidth();
	    }
	    c.gridx = 0;
	    c.gridy = i;
	    c.ipadx = hgap;
	    c.ipady = vgap;
	    c.anchor = GridBagConstraints.WEST;
	    c.weightx = 1.0;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    gridbag.setConstraints(items[i], c);
	    add(items[i]);
	}

	/*
	 * Set the label width to the longest label.
	 * so that they are aligned equally.
	 */
	for (int i = 0; i < items.length; i++) {
	    JLabel l = items[i].getLabel();
	    Dimension dim = l.getPreferredSize();
	    dim.width = longest;
	    l.setPreferredSize(dim);
	}

    }
}
