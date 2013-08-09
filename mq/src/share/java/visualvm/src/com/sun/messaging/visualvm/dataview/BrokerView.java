/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2010 Oracle and/or its affiliates. All rights reserved.
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
import javax.swing.JOptionPane;

import org.openide.util.ImageUtilities;

import com.sun.messaging.visualvm.datasource.BrokerDataSource;
import com.sun.messaging.visualvm.datasource.ClusterAccessUtils;
import com.sun.messaging.visualvm.datasource.MQPluginException;
import com.sun.messaging.visualvm.ui.BrokerMonitorList;
import com.sun.messaging.visualvm.ui.BrokerPropertyList;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class BrokerView extends DataSourceView {

	private DataViewComponent dvc;
	private JEditorPane generalDataArea;
	private DataViewComponent.DetailsView brokerMonitor;
	private DataViewComponent.DetailsView brokerProperties;
	private boolean initialising = false;
	
	// Make sure there is an image at this location in your project:
	private static final Image NODE_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/broker.gif", true);

	public BrokerView(BrokerDataSource bds) {
		super(bds, "Broker", NODE_ICON, 60, false);
		bds.setBrokerView(this);
	}

	@Override
	protected DataViewComponent createComponent() {

		initialising = true;
		try {

			String errorMessage = "";

			BrokerDataSource ds = (BrokerDataSource) getDataSource();
			Application app = ds.getApplication();

			if (app == null) {
				// the BrokerDataSource is still a placeholder
				// open it
				try {
					ds.connectToClusteredBroker();

					// wait for the application to be opened
					app = ds.getApplication();
					long TIMEOUT = 10000;
					long startTime = System.currentTimeMillis();
					while (app == null) {
						Thread.sleep(1000);
						app = ds.getApplication();
						if (System.currentTimeMillis() - startTime > 10000) {
							// give up
							break;
						}
					}
					if (app == null) {
						errorMessage = "Cannot connect to broker: (timeout after " + TIMEOUT + "ms";
						JOptionPane.showMessageDialog(null, errorMessage);

					}
				} catch (MQPluginException e) {
					errorMessage = "Cannot connect to broker: " + e.getMessage();
					JOptionPane.showMessageDialog(null, errorMessage);
				} catch (InterruptedException e) {
					errorMessage = "Cannot connect to broker: " + e.getMessage();
					JOptionPane.showMessageDialog(null, errorMessage);
				}
			}

			boolean jmxEnabled = false;
			JmxModel jmxModel = null;
			if (app != null) {
				// check that the application is JMX-enabled

				jmxModel = JmxModelFactory.getJmxModelFor(app);
				if (jmxModel == null || jmxModel.getConnectionState() != JmxModel.ConnectionState.CONNECTED) {
					jmxEnabled = false;
				} else {
					jmxEnabled = true;
				}
			}

			// create the data area for the master view
			generalDataArea = new JEditorPane();
			generalDataArea.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

			// create the master view
			DataViewComponent.MasterView masterView = new DataViewComponent.MasterView("Broker Overview", null,
					generalDataArea);

			// create the configuration for the master view
			boolean isMasterAreaResizable = false;
			DataViewComponent.MasterViewConfiguration masterConfiguration = new DataViewComponent.MasterViewConfiguration(
					isMasterAreaResizable);

			// Add the master view and configuration view to the component:
			dvc = new DataViewComponent(masterView, masterConfiguration);

			if (app == null) {
				writeErrorMessage(errorMessage, generalDataArea);
			} else if (!jmxEnabled) {
				writeErrorMessage("Broker is not JMX enabled.<br/>" + "To enable JMX, either use a Java 6 JRE<br/>"
						+ "or use a Java 5 JRE and specify <tt>-Dcom.sun.management.jmxremote</tt> ", generalDataArea);
			} else {
				createSubPanels(app);
			}

		} finally {
			initialising = false;
		}
		return dvc;

	}
	
	public void createSubPanels(Application app){

            if (generalDataArea==null){
                System.out.println("foo");
            }
		
		generalDataArea.setText("");
		
        MBeanServerConnection mbsc = ClusterAccessUtils.getMBeanServerConnection(app);

		// 1. create the JPanel for the "Broker monitor" detail view
		BrokerMonitorList brokerMonitorList = new BrokerMonitorList(dvc);
		brokerMonitorList.setMBeanServerConnection(mbsc);

		// add the "broker monitor" detail view to the data view component
		// representing the master view
		brokerMonitor = new DataViewComponent.DetailsView("Broker monitor", null, 10, brokerMonitorList, null);
		// DataViewComponent.TOP_LEFT
		dvc.addDetailsView(brokerMonitor,brokerMonitorList.getCorner());

		// 2. create the JPanel for the "Selected broker properties" detail
		// view
		BrokerPropertyList brokerPropertyList = new BrokerPropertyList(dvc);
		brokerPropertyList.setMBeanServerConnection(mbsc);

		// add the "broker properties" detail view to the data view
		// component representing the master view
		brokerProperties = new DataViewComponent.DetailsView("Selected broker properties", null, 10,
				brokerPropertyList, null);
		// DataViewComponent.BOTTOM_LEFT
		dvc.addDetailsView(brokerProperties, brokerPropertyList.getCorner());
	}
	
	/**
	 * Convert this BrokerView back to a placeholder
	 */
	public void revertToPlaceholder() {
		generalDataArea.setText("Application has terminated");
		
		dvc.removeDetailsView(brokerMonitor);
		dvc.removeDetailsView(brokerProperties);
	}
	
	public void writeErrorMessage(String message, JEditorPane jep) {
		StringBuffer textBuffer = new StringBuffer(200);
		textBuffer.append("<html><body>");
		textBuffer.append(message);
		textBuffer.append("</body></html>");
		jep.setContentType("text/html");
		jep.setText(textBuffer.toString());
	}

	public void writeErrorMessage(JEditorPane jep) {
		StringBuffer textBuffer = new StringBuffer(200);
		textBuffer.append("<html><body>");
		textBuffer.append("Broker is not JMX enabled.<br/>" + "To enable JMX, either use a Java 6 JRE<br/>"
				+ "or use a Java 5 JRE and specify <tt>-Dcom.sun.management.jmxremote</tt> ");
		textBuffer.append("</body></html>");
		jep.setContentType("text/html");
		jep.setText(textBuffer.toString());
	}

	public boolean isInitialising() {
		return initialising;
	}



}
