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

package com.sun.messaging.visualvm.datasource;

import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

import org.openide.util.ImageUtilities;

import com.sun.messaging.visualvm.datasource.MQResourceDescriptor.ResourceType;
import com.sun.messaging.visualvm.dataview.BrokerView;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.jmx.JmxApplicationException;
import com.sun.tools.visualvm.jmx.JmxApplicationsSupport;

public class BrokerDataSource extends MQDataSource {

	private static final Image CONNECTED_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/brokerconnected.gif", true);

	private static final Image DISCONNECTED_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/brokerdisconnected.gif", true);

	private BrokerView brokerView;

	/**
	 * First part of label
	 */
	private String nameLabel;
	/**
	 * Second part of label, typically "(pid=1234) or "(disconnected)"
	 */
	private String pidLabel;

	/**
	 * Properties used then this instance is a placeholder which is referenced
	 * in the cluster list of an open broker Only used when isOpened=false
	 */
	String brokerAddress;

	/**
	 * Used for connecting to remote broker in a background thread
	 */
	RemoteBrokerConector connector;

	/**
	 * Create a BrokerDataSource representing a broker whose application has
	 * been opened in the current VisualVM session
	 * 
	 * @param app
	 * @param nameLabel
	 *            First part of label, typically either "(Broker host:port)" or
	 *            just "(Broker host)" if not JMX enabled
	 * @param pidLabel
	 *            Second part of label, typically either "(pid 1234)" or
	 *            "(disconnected)" if application not running or connected
	 * @param address
	 */
	public BrokerDataSource(Application app, String nameLabel, String pidLabel, String address) {
		this.application = app;
		this.nameLabel = nameLabel;
		this.pidLabel = pidLabel;
		this.brokerAddress = address;
		this.descriptor = new MQResourceDescriptor(this, nameLabel + " " + pidLabel, ResourceType.BROKER, null,
				CONNECTED_ICON);
	}

	/**
	 * Create a placeholder representing a broker which is referenced in the
	 * cluster list of an open broker, but which has not yet been opened in the
	 * current VisualVM session
	 * 
	 * @param brokerAddress
	 * @param nameLabel
	 *            First part of label, typically either "(Broker host:port)" or
	 *            just "(Broker host)" if not JMX enabled
	 * @param pidLabel
	 *            Second part of label, typically either "(pid 1234)" or
	 *            "(disconnected)" if application not running or connected
	 */
	public BrokerDataSource(String brokerAddress, String nameLabel, String pidLabel) {
		this.nameLabel = nameLabel;
		this.pidLabel = pidLabel;
		this.brokerAddress = brokerAddress;
		this.descriptor = new MQResourceDescriptor(this, this.nameLabel + " " + this.pidLabel,
				ResourceType.BROKER_PROXY, null, DISCONNECTED_ICON);

		// start connector thread
		if (!ClusterAccessUtils.isLocal(brokerAddress)) {
			connector = new RemoteBrokerConector();
			connector.start();
		}

		// note that the connector thread will terminate when the connection is
		// lost
	}

	/**
	 * Update the label of this data source
	 * 
	 * @param nameLabel
	 *            First part of label, typically either "(Broker host:port)" or
	 *            just "(Broker host)" if not JMX enabled
	 * @param pidLabel
	 *            Second part of label, typically either "(pid 1234)" or
	 *            "(disconnected)" if application not running or connected
	 */
	public void updateLabel(String nameLabel, String pidLabel) {
		this.nameLabel = nameLabel;
		this.pidLabel = pidLabel;
		this.descriptor.setName(this.nameLabel + " " + this.pidLabel);
	}

	public void connectToClusteredBroker() throws MQPluginException {

		// connect to port mapper and obtain JMX URL
		String brokerAddress = this.getBrokerAddress();
		String jmxURL = ClusterAccessUtils.getBrokerJMXURL(brokerAddress);

		if (jmxURL == null) {
			// cannot connect to port mapper. Perhaps broker is not running
			throw new MQPluginException("No response from portmapper at " + brokerAddress
					+ ". It is probably not running");
		}

		// open it
		JmxApplicationsSupport jmxApplicationsSupport = JmxApplicationsSupport.getInstance();
		try {
			Application newApplication = jmxApplicationsSupport.createJmxApplication(jmxURL, brokerAddress, "", "");
		} catch (JmxApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		descriptor.setIcon(CONNECTED_ICON);
	}

	public String getBrokerAddress() {
		return brokerAddress;
	}

	public void setBrokerView(BrokerView bv) {
		brokerView = bv;
	}

	public BrokerView getBrokerView() {
		return brokerView;
	}

	public String getNameLabel() {
		return nameLabel;
	}

	public String getPidLabel() {
		return pidLabel;
	}

	class RemoteBrokerConector {

		Timer timer = null;
		private int refreshInterval = 10;

		public void start() {
			if (timer != null) {
				return;
			}

			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					connect();
				}

			};

			timer = new Timer("BrokerDataSource connecting thread");
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

		/**
		 * Try to connect to the remote broker
		 */
		private void connect() {

			// connect to port mapper and obtain JMX URL
			String brokerAddress = getBrokerAddress();
			String jmxURL = ClusterAccessUtils.getBrokerJMXURL(brokerAddress);

			if (jmxURL == null) {
				// cannot connect to port mapper. Perhaps broker is not running
				return;
			}

			// open it
			JmxApplicationsSupport jmxApplicationsSupport = JmxApplicationsSupport.getInstance();
			try {
				Application newApplication = jmxApplicationsSupport.createJmxApplication(jmxURL, brokerAddress, "", "");

				stop();
			} catch (JmxApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

		}
	}

	public void setPlaceholder() {
		
		descriptor.setIcon(DISCONNECTED_ICON);
		if (getBrokerView() != null) {
			getBrokerView().revertToPlaceholder();
		}
		if (connector != null) {
			connector.start();
		}
	}
	
	public void setConnected() {
		
		descriptor.setIcon(CONNECTED_ICON);

	}
}
