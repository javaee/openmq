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

import com.sun.messaging.jms.management.server.ConnectionAttributes;
import com.sun.messaging.jms.management.server.ConnectionOperations;
import com.sun.messaging.jms.management.server.MQObjectName;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

@SuppressWarnings("serial")
public class ConnectionMonitorConnectionList extends MultipleMBeanResourceList {

    public ConnectionMonitorConnectionList(DataViewComponent dvc) {
		super(dvc);
	}

	private static String initialDisplayedAttrsList[] = {
    	ConnectionAttributes.CONNECTION_ID, // Primary Attribute
    	ConnectionAttributes.SERVICE_NAME,
	    ConnectionAttributes.CLIENT_ID,
	    ConnectionAttributes.HOST,
	    ConnectionAttributes.PORT,
    	ConnectionAttributes.USER,
	    ConnectionAttributes.NUM_CONSUMERS,
    	ConnectionAttributes.NUM_PRODUCERS
    };
    
    @Override
	public String[] getinitialDisplayedAttrsList() {
        return initialDisplayedAttrsList;
    }
    
    // copied from com.sun.messaging.jmq.jmsserver.management.mbeans.ConnectionMonitor
    private String completeAttrsList[] = {
	    ConnectionAttributes.CLIENT_ID,
	    ConnectionAttributes.CLIENT_PLATFORM,
	    ConnectionAttributes.CONNECTION_ID,
	    ConnectionAttributes.CREATION_TIME,
	    ConnectionAttributes.HOST,
	    ConnectionAttributes.NUM_CONSUMERS,
	    ConnectionAttributes.NUM_PRODUCERS,
	    ConnectionAttributes.PORT,
	    ConnectionAttributes.SERVICE_NAME,
	    ConnectionAttributes.USER
   };    

    @Override
	public String[] getCompleteAttrsList() {
        return completeAttrsList;
    }    
    
	@Override
	public String getPrimaryAttribute() {
		return "ConnectionID";
	}
    
	@Override
	protected String getManagerMBeanName() {
		return MQObjectName.CONNECTION_MANAGER_MONITOR_MBEAN_NAME;
	}

	@Override
	protected String getGetSubMbeanOperationName(){
		return ConnectionOperations.GET_CONNECTIONS;
	}

    @Override
    public void handleItemQuery(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public int getCorner() {
		return DataViewComponent.BOTTOM_LEFT;
	}

}
