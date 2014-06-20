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

package com.sun.messaging.visualvm.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.swing.table.AbstractTableModel;
/**
 * This table model can be used to handle two types of table, depending on the value of the attrsInColumn property
 *       
 * If attrsInColumn=true this table shows a list of resources, with multiple attributes of each.
 * In this case the table will have multiple columns, one for each attribute being displayed
 * The column heading will be the name of the attribute in question
 * 
 * If attrsInColumn=false this table shows a list of attribute values of a single resource.
 * In this case the table will have two columns, headed "Attribute" and "Value".
 * The first column will display the name of each attribute, the second column will be the corresponding value
 *
 */
public abstract class MQResourceListTableModel extends AbstractTableModel {

    protected JMXConnector jmxc = null;
    protected MBeanServerConnection mbsc = null;
    protected String attributeNames[] = new String[0];
    private List dataList = Collections.emptyList();
    Timer timer = null;
    private int interval = 2;
    private boolean attrsInColumn = true;

    public MQResourceListTableModel() {
    }

    public void setAttributes(String attrs[]) {
        attributeNames = attrs;
    }

    public String[] getAttributes() {
        return attributeNames;
    }

    /**
     * Return whether this table shows a list of resources, with multiple attributes of each
     * If this returns false then the table instead shows a list of attribute values of a single resource.
     * @return
    */
    public boolean isAttrsInColumn() {
        return attrsInColumn;
    }

    /**
     * Specify whether this table shows a list of resources, with multiple attributes of each
     * Set to false if the table instead shows a list of attribute values of a single resource.
     * @param attrsInColumn
     */
    public void setAttrsInColumn(boolean attrsInColumn) {
        this.attrsInColumn = attrsInColumn;
    }

    public int getRowCount() {
        if (dataList == null) {
            return (0);
        }

        return (dataList.size());
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (isAttrsInColumn()) {
            return (attributeNames[columnIndex]);
        } else {
            if (columnIndex == 0) {
                return ("Attribute");
            } else if (columnIndex == 1) {
                return ("Value");
            } else {
                return null;
            }
        }
    }

    public int getColumnCount() {
        if (isAttrsInColumn()) {
            return (attributeNames.length);
        } else {
            return (2);
        }
    }

    public Object getRowData(int row) {
        Map.Entry me = null;
        Object obj = null;

        if (isAttrsInColumn()) {
            if (dataList == null) {
                return (null);
            }
            Object rowObject = dataList.get(row);
            if (rowObject instanceof RowData){
            	obj = rowObject;
            } else {
            	me = (Map.Entry) rowObject;
            	obj = me.getValue();
            }
        } else {
            return (attributeNames[row]);
        }

        return (obj);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return (getDataValueAt(dataList, rowIndex, columnIndex));
    }

    public MBeanServerConnection getMBeanServerConnection() {
        return mbsc;
    }

    public void setMBeanServerConnection(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }

    public void load() {

        int oldSize = dataList.size();
        dataList = loadData();
        int newSize = dataList.size();

        // try to avoid calling fireTableDataChanged() since this clears the current selection
        //TODO Improve this to keep selection when a new row is added and clear when current row is removed
        if (oldSize==newSize && newSize>0){
            fireTableRowsUpdated(0,newSize-1);
        } else {
            fireTableDataChanged();
        }
        
        // if there are any associated charts, update them
        updateCharts();
    }

	public void clear() {
        if (dataList == null) {
            return;
        }
        dataList.clear();
        fireTableDataChanged();
    }

    public void start() {
        if (timer != null) {
            return;
        }

        TimerTask task = new TimerTask() {

            @Override
			public void run() {
                load();
            }
        };

        timer = new Timer("Table updating thread");
        timer.schedule(task, 0, (interval * 1000));
    }

    public boolean autoLoadStarted() {
        if (timer != null) {
            return (true);
        }
        return (false);
    }

    public void stop() {
        if (timer == null) {
            return;
        }

        timer.cancel();
        timer = null;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public abstract List loadData();

    public abstract Object getDataValueAt(List l, int row, int col);
    
    public abstract void updateCharts();
}
