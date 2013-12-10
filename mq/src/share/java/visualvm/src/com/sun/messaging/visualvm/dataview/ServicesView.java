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

package com.sun.messaging.visualvm.dataview;

import java.awt.Image;

import javax.management.MBeanServerConnection;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

import org.openide.util.ImageUtilities;

import com.sun.messaging.visualvm.datasource.ClusterAccessUtils;
import com.sun.messaging.visualvm.datasource.ServicesDataSource;
import com.sun.messaging.visualvm.ui.ServiceConfigList;
import com.sun.messaging.visualvm.ui.ServiceConfigServiceList;
import com.sun.messaging.visualvm.ui.ServiceMonitorList;
import com.sun.messaging.visualvm.ui.ServiceMonitorServiceList;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class ServicesView extends DataSourceView {

    private DataViewComponent dvc;
    //Make sure there is an image at this location in your project:
    private static final Image NODE_ICON = ImageUtilities.loadImage(
            "com/sun/messaging/visualvm/ui/resources/services.gif", true);

    public ServicesView(ServicesDataSource dds) {
        // isCloseable=true
        super(dds, "Services", NODE_ICON, 60, true);
    }

    @Override
	protected DataViewComponent createComponent() {
        ServicesDataSource ds = (ServicesDataSource)getDataSource();
        Application app = ds.getApplication();

        MBeanServerConnection mbsc = ClusterAccessUtils.getMBeanServerConnection(app);

        // create the data area for the master view
        JEditorPane generalDataArea = new JEditorPane();
        generalDataArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));
        
        // create the master view
        DataViewComponent.MasterView masterView = new DataViewComponent.MasterView("Services", null, generalDataArea);
        
        // create the configuration for the master view
        boolean isMasterAreaResizable = false;
        DataViewComponent.MasterViewConfiguration masterConfiguration = new DataViewComponent.MasterViewConfiguration(isMasterAreaResizable);
           
        // Add the master view and configuration view to the component:
        dvc = new DataViewComponent(masterView, masterConfiguration);
 
        // 1. create the JPanel for the "service monitor" detail view
        ServiceMonitorList serviceMonitorList = new ServiceMonitorList(dvc);
        serviceMonitorList.setMBeanServerConnection(mbsc);

        // add the "service monitor" detail view to the data view component representing the master view  
        //DataViewComponent.TOP_LEFT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Service manager (monitor)", null, 10, serviceMonitorList, null), serviceMonitorList.getCorner());	
 
        // 2. create the JPanel for the "service config" detail view
        ServiceConfigList serviceConfigList = new ServiceConfigList(dvc);
        serviceConfigList.setMBeanServerConnection(mbsc);

        // add the "service config" detail view to the data view component representing the master view  
        // DataViewComponent.TOP_RIGHT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Service manager (config)", null, 10, serviceConfigList, null), serviceConfigList.getCorner());
        
        // 3. create the JPanel for the "service list (monitor)" detail view
        ServiceMonitorServiceList serviceMonitorServiceList = new ServiceMonitorServiceList(dvc);
        serviceMonitorServiceList.setMBeanServerConnection(mbsc);

        // add the "service list (monitor)" detail view to the data view component representing the master view  
        // DataViewComponent.BOTTOM_LEFT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Service list (monitor)", null, 10, serviceMonitorServiceList, null), serviceMonitorServiceList.getCorner());	
 
        // 4. create the JPanel for the "service list (config)" detail view
        ServiceConfigServiceList serviceConfigServiceList = new ServiceConfigServiceList(dvc);
        serviceConfigServiceList.setMBeanServerConnection(mbsc);

        // add the "service list (config)" detail view to the data view component representing the master view  
        // DataViewComponent.BOTTOM_RIGHT
        dvc.addDetailsView(new DataViewComponent.DetailsView("Service list (config)", null, 10, serviceConfigServiceList, null), serviceConfigServiceList.getCorner() );

     
        
        return dvc;

    }
}
