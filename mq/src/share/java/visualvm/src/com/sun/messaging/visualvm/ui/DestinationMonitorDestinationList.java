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

import java.awt.Frame;

import javax.swing.SwingUtilities;

import com.sun.messaging.jms.management.server.DestinationAttributes;
import com.sun.messaging.jms.management.server.DestinationOperations;
import com.sun.messaging.jms.management.server.MQObjectName;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

@SuppressWarnings("serial")
public class DestinationMonitorDestinationList extends MultipleMBeanResourceList {
	
    public DestinationMonitorDestinationList(DataViewComponent dvc) {
		super(dvc);
	}

	private static String initialDisplayedAttrsList[] = {
    	DestinationAttributes.NAME, // Primary attribute
    	DestinationAttributes.TYPE,
    	DestinationAttributes.NUM_MSGS
    };
    
    @Override
	public String[] getinitialDisplayedAttrsList() {
        return initialDisplayedAttrsList;
    }    
    
    // copied from com.sun.messaging.jmq.jmsserver.management.mbeans.DestinationMonitor
    private String completeAttrsList[] = {
    	    DestinationAttributes.AVG_NUM_ACTIVE_CONSUMERS,
    	    DestinationAttributes.AVG_NUM_BACKUP_CONSUMERS,
    	    DestinationAttributes.AVG_NUM_CONSUMERS,
    	    DestinationAttributes.AVG_NUM_MSGS,
    	    DestinationAttributes.AVG_TOTAL_MSG_BYTES,
    	    DestinationAttributes.CONNECTION_ID,
    	    DestinationAttributes.CREATED_BY_ADMIN,
    	    DestinationAttributes.DISK_RESERVED,
    	    DestinationAttributes.DISK_USED,
    	    DestinationAttributes.DISK_UTILIZATION_RATIO,
    	    DestinationAttributes.MSG_BYTES_IN,
    	    DestinationAttributes.MSG_BYTES_OUT,        
    	    DestinationAttributes.NAME,
    	    DestinationAttributes.NUM_ACTIVE_CONSUMERS,
    	    DestinationAttributes.NUM_BACKUP_CONSUMERS,
    	    DestinationAttributes.NUM_CONSUMERS,
    	    DestinationAttributes.NUM_WILDCARDS,
    	    DestinationAttributes.NUM_WILDCARD_CONSUMERS,
    	    DestinationAttributes.NUM_WILDCARD_PRODUCERS,
    	    DestinationAttributes.NUM_MSGS,
    	    DestinationAttributes.NUM_MSGS_REMOTE,
    	    DestinationAttributes.NUM_MSGS_HELD_IN_TRANSACTION,
    	    DestinationAttributes.NUM_MSGS_IN,
    	    DestinationAttributes.NUM_MSGS_OUT,
    	    DestinationAttributes.NUM_MSGS_PENDING_ACKS,
    	    DestinationAttributes.NUM_PRODUCERS,
    	    DestinationAttributes.PEAK_MSG_BYTES,
    	    DestinationAttributes.PEAK_NUM_ACTIVE_CONSUMERS,
    	    DestinationAttributes.PEAK_NUM_BACKUP_CONSUMERS,
    	    DestinationAttributes.PEAK_NUM_CONSUMERS,
    	    DestinationAttributes.PEAK_NUM_MSGS,
    	    DestinationAttributes.PEAK_TOTAL_MSG_BYTES,
    	    DestinationAttributes.NEXT_MESSAGE_ID,
    	    DestinationAttributes.STATE,
    	    DestinationAttributes.STATE_LABEL,
    	    DestinationAttributes.TEMPORARY,
    	    DestinationAttributes.TOTAL_MSG_BYTES,
    	    DestinationAttributes.TOTAL_MSG_BYTES_REMOTE,
    	    DestinationAttributes.TOTAL_MSG_BYTES_HELD_IN_TRANSACTION,
    	    DestinationAttributes.TYPE
   };    

    @Override
	public String[] getCompleteAttrsList() {
        return completeAttrsList;
    }        
    
	@Override
	public String getPrimaryAttribute() {
		return "Name";
	}
    
	@Override
	protected String getManagerMBeanName() {
		return MQObjectName.DESTINATION_MANAGER_MONITOR_MBEAN_NAME;
	}

	@Override
	protected String getGetSubMbeanOperationName(){
		return DestinationOperations.GET_DESTINATIONS;
	}

    @Override
    public void handleItemQuery(Object obj) {
        QueryDestinationDialog qdd = new QueryDestinationDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        
        
        RowData rowData = (RowData)obj;
        qdd.setDestinationObjectName(rowData.getObjectName());
        qdd.setMBeanServerConnection(getMBeanServerConnection());
        qdd.setLocationRelativeTo((Frame) SwingUtilities.getWindowAncestor(this));
        
        qdd.setVisible(true);
    }

	@Override
	public int getCorner() {
		return DataViewComponent.BOTTOM_LEFT;
	}
}
