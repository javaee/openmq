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

package com.sun.messaging.visualvm.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.sun.messaging.visualvm.chart.ChartPanel;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

/**
 * This is the abstract superclass of all JPanels used by the MQ plugin that show tables of things,
 * and which are used in a detail view
 */
@SuppressWarnings("serial")
public abstract class MQList extends javax.swing.JPanel implements MQUIEventListener, TableModelListener {
	
	/**
	 * The DataViewComponent within which this component is being displayed
	 * This is needed to allow this class to create charts in that DataViewComponent
	 */
	private DataViewComponent dvc;
	
	/**
	 * A List of ChartUpdater object, one for each ChartView dependent on this
	 * resource list
	 */
	List<ChartUpdater> chartUpdaters = new ArrayList<ChartUpdater>();
    	
	// prevent subclasses calling default constructor
	@SuppressWarnings("unused")
	private MQList(){		
	}
	
	MQList(DataViewComponent dvc){
		this.dvc=dvc;
	}

	/**
	 * Utility method:
	 * Set the column widths of the supplied table to appropriate values given the size of the heading and the data
	 * 
	 * Based on the example referred to from 
	 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
	 * @param table
	 */
    protected void initColumnSizes(JTable table) {

		TableModel tableModel = table.getModel();
		TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
		
		// this technique seems to under-estimate the width needed, so need to add a fudge factor
		int fudgeFactor=5;
		
		int colCount = table.getColumnModel().getColumnCount();
		int rowCount = table.getRowCount();
		
		for (int iCol = 0; iCol < colCount; iCol++) {
			TableColumn column = table.getColumnModel().getColumn(iCol);

			// calculate width of column heading
			Component headerComp = defaultRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
			int headerWidth = headerComp.getPreferredSize().width;

			// calculate width of widest value in this column
			int maxWidth = 0;

			for (int iRow = 0; iRow < rowCount; iRow++) {
				Object value = tableModel.getValueAt(iRow, iCol);
				Component cellComp = table.getDefaultRenderer(
						tableModel.getColumnClass(iCol)).getTableCellRendererComponent(table, value, false,false, 0, iCol);
				int thisCellWidth = cellComp.getPreferredSize().width;

				if (thisCellWidth > maxWidth) {
					maxWidth = thisCellWidth;
				}
			}

			column.setPreferredWidth(Math.max(headerWidth, maxWidth)+fudgeFactor);
		}
	}

	public DataViewComponent getDvc() {
		return dvc;
	}
	

	
	/**
	 * Return the corner of the master view where this panel should be displayed
	 * 
	 * Possible values are DataViewComponent.TOP_LEFT, DataViewComponent.TOP_RIGHT, 
	 * DataViewComponent.BOTTOM_LEFT, DataViewComponent.BOTTOM_RIGHT
	 * 
	 * @return
	 */
	public abstract int getCorner();
	
    /**
	 * Register for updates to the specified chart
     * @param chartPanel
     */
	public void registerChart(ChartPanel chartPanel) {
		chartUpdaters.add(createChartUpdater(chartPanel));
	}
	
	protected void updateRegisteredCharts() {
		for (ChartUpdater thisChartUpdater : chartUpdaters) {
			thisChartUpdater.updateCharts();
		}
	}

	protected abstract ChartUpdater createChartUpdater(ChartPanel chartPanel);
}
