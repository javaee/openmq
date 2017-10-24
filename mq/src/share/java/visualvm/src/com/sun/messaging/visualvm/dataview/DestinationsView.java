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

package com.sun.messaging.visualvm.dataview;

import java.awt.Image;

import javax.management.MBeanServerConnection;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

import org.openide.util.ImageUtilities;

import com.sun.messaging.visualvm.datasource.ClusterAccessUtils;
import com.sun.messaging.visualvm.datasource.DestinationsDataSource;
import com.sun.messaging.visualvm.ui.DestinationConfigDestinationList;
import com.sun.messaging.visualvm.ui.DestinationConfigList;
import com.sun.messaging.visualvm.ui.DestinationMonitorDestinationList;
import com.sun.messaging.visualvm.ui.DestinationMonitorList;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class DestinationsView extends DataSourceView {

    private DataViewComponent dvc;
    //Make sure there is an image at this location in your project:
    private static final Image NODE_ICON = ImageUtilities.loadImage(
            "com/sun/messaging/visualvm/ui/resources/folder.gif", true);

    public DestinationsView(DestinationsDataSource dds) {
        // isCloseable=true
        super(dds, "Destinations", NODE_ICON, 60, true);
    }

    @Override
	protected DataViewComponent createComponent() {
        DestinationsDataSource ds = (DestinationsDataSource)getDataSource();
        Application app = ds.getApplication();

        MBeanServerConnection mbsc = ClusterAccessUtils.getMBeanServerConnection(app);

        // create the data area for the master view
        JEditorPane generalDataArea = new JEditorPane();
        generalDataArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        
        // create the master view
        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView("Destination", null, generalDataArea);
        
        // create the configuration for the master view
        boolean isMasterAreaResizable = false;
        DataViewComponent.MasterViewConfiguration masterConfiguration = new DataViewComponent.MasterViewConfiguration(isMasterAreaResizable);
           
        // Add the master view and configuration view to the component:
        dvc = new DataViewComponent(masterView, masterConfiguration);
 
        // 1. create the JPanel for the "Destination manager (monitor)" detail view
        DestinationMonitorList destinationMonitorList = new DestinationMonitorList(dvc);
        destinationMonitorList.setMBeanServerConnection(mbsc);

        // add the "Destination manager (monitor)" detail view to the data view component representing the master view  
        // DataViewComponent.TOP_LEFT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Destination manager (monitor)", null, 10, destinationMonitorList, null), destinationMonitorList.getCorner());	
 
        // 2. create the JPanel for the "Destination manager (config)" detail view
        DestinationConfigList destinationConfigList = new DestinationConfigList(dvc);
        destinationConfigList.setMBeanServerConnection(mbsc);

        // add the "Destination manager (config)" detail view to the data view component representing the master view  
        // DataViewComponent.TOP_RIGHT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Destination manager (config)", null, 10, destinationConfigList, null), destinationConfigList.getCorner());
        
        // 3. create the JPanel for the "Destination list (monitor)" detail view
        DestinationMonitorDestinationList destinationMonitorDestinationList = new DestinationMonitorDestinationList(dvc);
        destinationMonitorDestinationList.setMBeanServerConnection(mbsc);

        // add the "Destination list (monitor)" detail view to the data view component representing the master view  
        // DataViewComponent.BOTTOM_LEFT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Destination list (monitor)", null, 10, destinationMonitorDestinationList, null), destinationMonitorDestinationList.getCorner());	
        
        // 4. create the JPanel for the "Destination list (config)" detail view
        DestinationConfigDestinationList destinationConfigDestinationList = new DestinationConfigDestinationList(dvc);
        destinationConfigDestinationList.setMBeanServerConnection(mbsc);

        // add the "Destination list (config)" detail view to the data view component representing the master view  
        // DataViewComponent.BOTTOM_RIGHT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Destination list (config)", null, 10, destinationConfigDestinationList, null), destinationConfigDestinationList.getCorner() );   
        
        return dvc;

    }

}
