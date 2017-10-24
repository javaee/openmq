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

package com.sun.messaging.visualvm.datasource;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.openide.util.ImageUtilities;

import com.sun.messaging.jmq.io.PortMapperEntry;
import com.sun.messaging.jmq.io.PortMapperTable;
import com.sun.messaging.jms.management.server.BrokerClusterInfo;
import com.sun.messaging.jms.management.server.BrokerState;
import com.sun.messaging.jms.management.server.ClusterOperations;
import com.sun.messaging.jms.management.server.MQObjectName;
import com.sun.messaging.visualvm.datasource.MQResourceDescriptor.ResourceType;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.jmx.JmxApplicationException;
import com.sun.tools.visualvm.jmx.JmxApplicationsSupport;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;

public class ClusteredBrokerDataSource extends MQDataSource {

	private static final Image BROKER_RUNNING_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/brokerrunning.gif", true);
	private static final Image BROKER_DOWN_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/brokerdown.gif", true);
	private static final Image BROKER_OTHER_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/brokerother.gif", true);

	String brokerAddress;
	String name;
	Updater updater;
	
	/**
	 * 
	 * @param app
	 * @param master
	 * @param brokerAddress
	 * @param name
	 */
	public ClusteredBrokerDataSource(Application app, MQDataSource master,
			String brokerAddress, String name) {
		super(master);
		application = app;
		this.name = name;
		this.brokerAddress = brokerAddress;
		this.descriptor = new MQResourceDescriptor(this, name,
				ResourceType.CLUSTERED_BROKER, null, null);

		// start refresh thread
		updater = new Updater();
		updater.start();

		// note that the refresh thread will terminate when the connection is lost
	}

	public String getBrokerAddress() {
		return brokerAddress;
	}

	public void stop(){
		if (updater!=null){
			updater.stop();
			updater=null;
		}
	}

	class Updater {

		Timer timer = null;
		private int refreshInterval = 2;

		public void start() {
			if (timer != null) {
				return;
			}

			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					refresh();
				}
			};

			timer = new Timer("ClusteredBrokerDataSource updating thread");
			timer.schedule(task, 0, (refreshInterval * 1000));
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

		public void refresh() {
            
	        MBeanServerConnection mbsc = ClusterAccessUtils.getMBeanServerConnection(application);
		
			if (mbsc == null) {
				// assume we've lost the connection because the application has terminated
                stop();
				return;
			} else {
				ObjectName objName = null;
				try {
					objName = new ObjectName(
							MQObjectName.CLUSTER_MONITOR_MBEAN_NAME);
				} catch (MalformedObjectNameException e) {
				} catch (NullPointerException e) {
				}

				CompositeData clusteredBrokerData = null;
				try {
					Object opParams[] = { brokerAddress };
					String opSig[] = { String.class.getName() };
					clusteredBrokerData = (CompositeData) mbsc.invoke(objName,
							ClusterOperations.GET_BROKER_INFO_BY_ADDRESS,
							opParams, opSig);
				} catch (InstanceNotFoundException e) {
					// don't log an exception as the broker has probably terminated
				} catch (MBeanException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReflectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (clusteredBrokerData != null) {
					// fetch the BrokerID (only for HA clusters(
					String brokerID = (String) clusteredBrokerData.get(BrokerClusterInfo.ID);					
					
					int state = ((Integer) clusteredBrokerData
							.get(BrokerClusterInfo.STATE)).intValue();
					String stateLabel = (String) clusteredBrokerData
							.get(BrokerClusterInfo.STATE_LABEL);
					if (state == BrokerState.OPERATING) {
						descriptor.setIcon(BROKER_RUNNING_ICON);
					} else if (state == BrokerState.BROKER_DOWN) {
						descriptor.setIcon(BROKER_DOWN_ICON);
					} else {
						descriptor.setIcon(BROKER_OTHER_ICON);
					}
					if (brokerID==null){
						descriptor.setName(brokerAddress + " (" + stateLabel + ")");
					} else {
						// HA
						descriptor.setName(brokerID + " " + brokerAddress + " (" + stateLabel + ")");
					}
				}
			}

		}
	}
	
	public void xconnectToClusteredBroker() {
		
		// connect to port mapper and obtain JMX URL
		String brokerAddress = this.getBrokerAddress();
		String jmxURL = ClusterAccessUtils.getBrokerJMXURL(brokerAddress);
		
		// open it
		JmxApplicationsSupport jmxApplicationsSupport = JmxApplicationsSupport.getInstance();
		try {
			Application newApplication=jmxApplicationsSupport.createJmxApplication(jmxURL,brokerAddress , "", "");
		} catch (JmxApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

}
