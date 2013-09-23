/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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

/*
 * @(#)ClusterUtil.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import java.util.Hashtable;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.OpenDataException;

import com.sun.messaging.jms.management.server.BrokerClusterInfo;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.cluster.api.*;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.jmsserver.core.BrokerMQAddress;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.GetClusterHandler;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.log.Logger;

public class ClusterUtil  {
    /*
     * Broker Cluster Info item names for Config MBeans
     */
    private static final String[] configBrokerInfoItemNames = {
                            BrokerClusterInfo.ADDRESS,
                            BrokerClusterInfo.ID
                    };

    /*
     * Broker Cluster Info item descriptions for Config MBeans
     * TBD: use real descriptions
     */
    private static final String[] configBrokerInfoItemDesc = configBrokerInfoItemNames;

    /*
     * Broker Cluster Info item types for Config MBeans
     */
    private static final OpenType[] configItemTypes = {
			    SimpleType.STRING,		// address
			    SimpleType.STRING		// id
                    };

    /*
     * Broker Cluster Info composite type for Config MBeans
     */
    private static volatile CompositeType configCompType = null;

    public static String getBrokerAddress(String brokerID)  {
        ClusterManager cmgr;
        ClusteredBroker bkr;
	MQAddress	addr;

	if (brokerID == null)  {
	    return (null);
	}

        cmgr = Globals.getClusterManager();
	if (cmgr == null)  {
	    return (null);
	}

        bkr = cmgr.getBroker(brokerID);
	if (bkr == null)  {
	    return (null);
	}

        addr = bkr.getBrokerURL();
	if (addr == null) {
	    return (null);
	}

        return (addr.toString());
    }

    public static String getShortBrokerAddress(String brokerID)  {
	BrokerMQAddress ba = null;
	String longAddr = getBrokerAddress(brokerID);
	Logger logger = Globals.getLogger();

	if (longAddr == null)  {
	    return (null);
	}

	try  {
	    ba = BrokerMQAddress.createAddress(longAddr);
	} catch (Exception e)  {
            BrokerResources	rb = Globals.getBrokerResources();

            logger.log(Logger.WARNING, 
		rb.getString(rb.W_JMX_FAILED_TO_OBTAIN_BKR_ADDRESS_FROM_ID, brokerID),
		e);

	    return (null);
	}

	if (ba == null)  {
	    return (null);
	}

        return (ba.getHost().getHostName() + ":" + ba.getPort());
    }

    public static boolean isMasterBroker(String brokerAddress)  {
	ClusterManager cm = Globals.getClusterManager();
	ClusteredBroker cb = null, master;
	String id = null;
	boolean isMaster = false;

	if (cm == null)  {
	    return (false);
	}

	try  {
            id = cm.lookupBrokerID(BrokerMQAddress.createAddress(brokerAddress));
        } catch (Exception e)  {
	    return (false);
        }

	if ((id == null) || (id.equals("")))  {
            return (false);
        }

	try  {
	    cb = cm.getBroker(id);

	    if (cb == null)  {
                return (false);
	    }
	} catch(Exception e)  {
            return (false);
	}

	master = cm.getMasterBroker();

	if (master == null)  {
	    return (false);
	}

	return (master.equals(cb));
    }

    public static CompositeData getConfigCompositeData(ClusteredBroker cb) 
					throws OpenDataException  {
	Logger logger = Globals.getLogger();
	CompositeData cds = null;

	Hashtable bkrInfo = GetClusterHandler.getBrokerClusterInfo(cb, logger);

	String id = null;

	if (Globals.getHAEnabled())  {
	    id = (String)bkrInfo.get(BrokerClusterInfo.ID);
	}

	Object[] brokerInfoItemValues = {
			    bkrInfo.get(BrokerClusterInfo.ADDRESS),
			    id
			};

        if (configCompType == null)  {
            configCompType = new CompositeType("BrokerClusterInfoConfig", "BrokerClusterInfoConfig", 
                        configBrokerInfoItemNames, configBrokerInfoItemDesc, configItemTypes);
        }

	cds = new CompositeDataSupport(configCompType, 
			configBrokerInfoItemNames, brokerInfoItemValues);
	
	return (cds);
    }
}
