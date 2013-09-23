/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.visualvm.chart;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

@SuppressWarnings("serial")
public class ChartPanel extends JPanel {


    private SimpleXYChartSupport chartSupport;
    
    private String chartedAttribute;
    private String rowIndentifierAttribute;
    private String[] rowIdentifierValues;
    
    /**
     * Create a chart to show the value of a single attribute for one or more items in a MQResourceList
     * @param chartedAttribute
     * @param rowIndentifierAttribute
     * @param rowIdentifierValues
     */
    public ChartPanel(String chartedAttribute,
			String rowIndentifierAttribute, String[] rowIdentifierValues) {
    	super();

		this.chartedAttribute = chartedAttribute;
		this.rowIndentifierAttribute = rowIndentifierAttribute;
		this.rowIdentifierValues = rowIdentifierValues;

        initModels();
        initComponents();
	}
    

    /**
     * Create a chart to show the value of a single attribute for the item represented by a MQAttributeList
     * @param attributeName
     */
    public ChartPanel(String attributeName) {
		super();
		
		this.chartedAttribute = attributeName;
		this.rowIdentifierValues = new String[1];
		rowIdentifierValues[0]="";

        initModels();
        initComponents();
		
	}


    /**
     * Make no-arg constructor private so it is not used
     */
    @SuppressWarnings("unused")
	private ChartPanel() {}



	private void initModels() {
        // Create a descriptor for chart showing decimal values
        // 100: initial y maximum before first values arrive
        // true: any item can be temporarily hidden by the user
        // 30: number of visible values, kind of viewport width
    	//TODO Should be able to configure valuesBuffer
        SimpleXYChartDescriptor chartDescriptor =
                SimpleXYChartDescriptor.decimal(100, true, 1000);

        // Add two items displayed as filled area with a line border
        ////chartDescriptor.addLineFillItems("Item 1", "Item 2");
        // Add two items displayed as lines
        chartDescriptor.addLineItems(rowIdentifierValues);
        
        // Create textual details area above the chart
        String[] detailsItems = new String[rowIdentifierValues.length];
        for (int i = 0; i < rowIdentifierValues.length; i++) {
			detailsItems[i]=rowIdentifierValues[i]+" "+chartedAttribute;
		}
        
        chartDescriptor.setDetailsItems(detailsItems);

        // Create SimpleXYChartSupport instance from the descriptor to access the chart
        chartSupport = ChartFactory.createSimpleXYChart(chartDescriptor);
    }

    private void initComponents() {
        // Makes this JPanel transparent to better fit VisualVM UI (white background)
        setOpaque(false);
        
        setLayout(new BorderLayout());
        
        // add the chart to this JPanel
        add(chartSupport.getChart(), BorderLayout.CENTER);

        // Update the initial values in details area before first values arrive
        String[] initialDetails = new String[rowIdentifierValues.length];
        for (int i = 0; i < rowIdentifierValues.length; i++) {
        	initialDetails[i]="waiting...";
		}
        chartSupport.updateDetails(initialDetails);
    }

	public void updateDetails(long[] values) {
		String[] details = new String[rowIdentifierValues.length];
        for (int i = 0; i < rowIdentifierValues.length; i++) {
        	details[i]=chartSupport.formatDecimal(values[i]);
		}
		chartSupport.updateDetails(details);
	}


	public void addValues(long timestamp, long[] values) {
		chartSupport.addValues(timestamp, values);
	}


	public String getChartedAttribute() {
		return chartedAttribute;
	}


	public String getRowIndentifierAttribute() {
		return rowIndentifierAttribute;
	}


	public String[] getRowIdentifierValues() {
		return rowIdentifierValues;
	}



}
