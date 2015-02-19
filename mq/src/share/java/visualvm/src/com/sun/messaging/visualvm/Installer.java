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

package com.sun.messaging.visualvm;

import com.sun.messaging.visualvm.datasource.MQDataSourceDescriptorProvider;
import com.sun.messaging.visualvm.datasource.MQModelProvider;
import com.sun.messaging.visualvm.dataview.BrokerViewProvider;
import com.sun.messaging.visualvm.dataview.ClusterViewProvider;
import com.sun.messaging.visualvm.dataview.ClusteredBrokerViewProvider;
import com.sun.messaging.visualvm.dataview.ConnectionsViewProvider;
import com.sun.messaging.visualvm.dataview.ConsumersViewProvider;
import com.sun.messaging.visualvm.dataview.DestinationsViewProvider;
import com.sun.messaging.visualvm.dataview.LogViewProvider;
import com.sun.messaging.visualvm.dataview.ProducersViewProvider;
import com.sun.messaging.visualvm.dataview.ServicesViewProvider;
import com.sun.messaging.visualvm.dataview.TransactionsViewProvider;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

   private static OpenMQBrokerApplicationTypeFactory INSTANCE = new OpenMQBrokerApplicationTypeFactory();

    // note that I have had to make some classes public ands their initialize methods public

    @Override
    public void restored() {
        ApplicationTypeFactory.getDefault().registerProvider(INSTANCE);
        
        MQDataSourceDescriptorProvider.initialize();
        MQModelProvider.initialize();
        
        // register the view providers for the sub-applications that we will create under the broker or glassfish process
        BrokerViewProvider.initialize();
        ClusterViewProvider.initialize();
        DestinationsViewProvider.initialize();
        ConnectionsViewProvider.initialize();
        ConsumersViewProvider.initialize();
        ProducersViewProvider.initialize();
        ServicesViewProvider.initialize();
        TransactionsViewProvider.initialize();
        LogViewProvider.initialize();
        ClusteredBrokerViewProvider.initialize();

//        DataSourceViewsManager.sharedInstance().addViewProvider(
//            new ChartViewProvider(), Application.class);
    }

    @Override
    public void uninstalled() {
        ApplicationTypeFactory.getDefault().unregisterProvider(INSTANCE);

        MQDataSourceDescriptorProvider.shutdown();
        MQModelProvider.shutdown();

        BrokerViewProvider.unregister();
        ClusterViewProvider.unregister();
        DestinationsViewProvider.unregister();
        ConnectionsViewProvider.unregister();
        ConsumersViewProvider.unregister();
        ProducersViewProvider.unregister();
        ServicesViewProvider.unregister();
        TransactionsViewProvider.unregister();
        LogViewProvider.unregister();
        ClusteredBrokerViewProvider.unregister();
    }


}
