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

import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import java.awt.Image;

public class MQResourceDescriptor extends DataSourceDescriptor<MQDataSource> {

	public static enum ResourceType {

		// resource types
		SERVICES, DESTINATIONS, CONSUMERS, PRODUCERS, CONNECTIONS, TRANSACTIONS, CLUSTER, BROKER, CLUSTERED_BROKER, LOG, CLUSTER_ROOT, BROKER_PROXY
	}

	private final ResourceType type;

	public MQResourceDescriptor(MQDataSource ds, String name,
			ResourceType type, String description, Image icon) {
		this(ds, name, type, description, icon, positionFor(type),
				EXPAND_NEVER);
	}

	public MQResourceDescriptor(MQDataSource ds, String name,
			ResourceType type, String description, Image icon, int position,
			int expand) {
		super(ds, name, description, icon, position, expand);
		this.type = type;
	}

	public void changePreferredPosition(int newPosition) {
		setPreferredPosition(newPosition);
	}

	public ResourceType getType() {
		return type;
	}

	public void setIcon(Image newIcon) {
		super.setIcon(newIcon);
	}

	/**
	 * Allow the name of the data source to be dynamically changed
	 */
	public boolean supportsRename() {
		return true;
	}

	private static int positionFor(ResourceType resourceType) {

		int result;
		switch (resourceType) {

		case BROKER:
			result = 1;
			break;
		case CLUSTER:
			result = 2;
			break;
		case CLUSTERED_BROKER:
			result = 3;
			break;
		case SERVICES:
			result = 4;
			break;
		case CONNECTIONS:
			result = 5;
			break;
		case DESTINATIONS:
			result = 6;
			break;
		case CONSUMERS:
			result = 7;
			break;
		case PRODUCERS:
			result = 8;
			break;

		case TRANSACTIONS:
			result = 9;
			break;

		case LOG:
			result = 10;
			break;
		default:
			result = POSITION_AT_THE_END;
			break;
		}

		return result;
	}
}
