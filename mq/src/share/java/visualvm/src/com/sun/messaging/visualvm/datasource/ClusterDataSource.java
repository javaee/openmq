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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServerConnection;

import org.openide.util.ImageUtilities;

import com.sun.messaging.jms.management.server.BrokerClusterInfo;
import com.sun.messaging.visualvm.datasource.MQResourceDescriptor.ResourceType;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;

public class ClusterDataSource extends MQDataSource {

	private static final Image NODE_ICON = ImageUtilities.loadImage(
			"com/sun/messaging/visualvm/ui/resources/consumers.gif", true);

	public ClusterDataSource(Application app, BrokerDataSource master, String name) {
		super(master);
		application = app;
		this.descriptor = new MQResourceDescriptor(this, name, ResourceType.CLUSTERED_BROKER, null, NODE_ICON);

		// start refresh thread
		Updater updater = new Updater();
		updater.start();

		// note that the refresh thread will terminate when the connection is
		// lost
	}

	/**
	 * Update the list of ClusteredBrokerDataSource nodes beneath this
	 * ClusterDataSource node Nores are created or destroyed as required
	 * 
	 * @param app
	 * @param bkr
	 * @param cds
	 */
	private void updateClusteredBrokers(Application app, BrokerDataSource bkr, ClusterDataSource cds) {

		// Ask the broker what other brokers are in its cluster
		List<Map<String, String>> clusteredBrokers = ClusterAccessUtils.getClusteredBrokerInfo(app);

		// Stage 1: Check whether we need to create any new
		// ClusteredBrokerDataSource nodes

		// Iterate through the entries in the broker list
		for (Map<String, String> thisBrokerInfo : clusteredBrokers) {

			String id = (String) thisBrokerInfo.get(BrokerClusterInfo.ID);
			String address = (String) thisBrokerInfo.get(BrokerClusterInfo.ADDRESS);
			String label;
			if (id != null) {
				label = id + " " + address;
			} else {
				label = address;

			}

			// Do we already have a corresponding ClusteredBrokerDataSource node beneath this ClusterDataSource?
			boolean found = false;
			for (Iterator<DataSource> iterator = cds.getRepository().getDataSources().iterator(); iterator.hasNext();) {
				ClusteredBrokerDataSource thisCBDS = (ClusteredBrokerDataSource) iterator.next();
				if (thisCBDS.getBrokerAddress().equals(address)) {
					found = true;
					break;
				}
			}

			if (!found) {
				// Create a ClusteredBrokerDataSource Broker corresponding to this entry in the
				// broker list and add it beneath this ClusterDataSource
				ClusteredBrokerDataSource cbds = new ClusteredBrokerDataSource(app, bkr, address, "Clustered Broker: ("
						+ label + ")");
				cds.getRepository().addDataSource(cbds);
			}
		}

		// Stage 2: Check whether we need to remove any existing
		// ClusteredBrokerDataSource nodes

		// Iterate through the ClusteredBrokerDataSource nodes beneath this ClusterDataSource
		for (Iterator<DataSource> iterator = cds.getRepository().getDataSources().iterator(); iterator.hasNext();) {

			ClusteredBrokerDataSource thisCBDS = (ClusteredBrokerDataSource) iterator.next();
			// see if there is still a corresponding an entry in the broker list
			boolean found = false;
			for (Map<String, String> thisBrokerInfo : clusteredBrokers) {
				String address = (String) thisBrokerInfo.get(BrokerClusterInfo.ADDRESS);
				if (thisCBDS.getBrokerAddress().equals(address)) {
					found = true;
					break;
				}
			}
			if (!found) {
				// there is no entry in the broker list corresponding to this
				// ClusteredBrokerDataSource node
				// remove it

				thisCBDS.stop();

				// remove node
				iterator.remove();

				// check whether the application has terminated
				// if it is then the data source may already have been removed
				// as part of application cleanup
				// so simply return
				if (ClusterAccessUtils.getMBeanServerConnection(application) == null) {
					return;
				}
				cds.getRepository().removeDataSource(thisCBDS);
			}
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

			timer = new Timer("ClusterDataSource updating thread");
			timer.schedule(task, 0, (refreshInterval * 1000));
		}

		public void stop() {
			if (timer == null) {
				return;
			}

			timer.cancel();
			timer = null;

			System.out.println("Stopping ClusterDataSource updater thread for pid=" + application.getPid());
		}

		public void refresh() {

			MBeanServerConnection mbsc = ClusterAccessUtils.getMBeanServerConnection(application);
			if (mbsc == null) {
				// assume we've lost the connection because the application has
				// terminated
				stop();
				return;
			}
			updateClusteredBrokers(application, (BrokerDataSource) getMaster(), ClusterDataSource.this);
		}
	}

}
