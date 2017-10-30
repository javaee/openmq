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

/*
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.manager;

import java.util.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.cluster.api.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Singleton;


/**
 * This class extends ClusterManagerImpl and is used to obtain and
 * distribute cluster information in an HA cluster.
 */

@Service(name = "com.sun.messaging.jmq.jmsserver.cluster.manager.BasicAutoClusterManagerImpl")
@Singleton
public class BasicAutoClusterManagerImpl extends ClusterManagerImpl 
{

    /**
     */
    public BasicAutoClusterManagerImpl() throws BrokerException {
       super();
    }

    public String initialize(MQAddress address) throws BrokerException {
        if (Globals.getClusterID() == null) {
            throw new BrokerException("imq.cluster.clusterid must set");
        }
        String r = super.initialize(address);
        if (!(allBrokers instanceof AutoClusterBrokerMap)) {
            throw new BrokerException(
            "Cluster configuration inconsistent: unexpected class "+allBrokers.getClass());
        }
        return r;
    }

    protected void setupListeners() {
        config.addListener(TRANSPORT_PROPERTY, this);
        config.addListener(HOST_PROPERTY, this);
        config.addListener(PORT_PROPERTY, this);
    }

    /**
     * to be called internally in cluster manager framework
     */
    public ClusteredBroker newClusteredBroker(MQAddress url,
                                   boolean isLocal, UID sid)
                                   throws BrokerException {

       ClusteredBroker cb = super.newClusteredBroker(url, isLocal, sid);
       ((ClusteredBrokerImpl)cb).setConfigBroker(true);
       return cb;
    }

    /**
     * Reload the cluster properties from config 
     *
     */
    public void reloadConfig() throws BrokerException {
        if (!initialized)
            throw new RuntimeException("Cluster not initialized");

        String[] props = { CLUSTERURL_PROPERTY };
        config.reloadProps(Globals.getConfigName(), props, false);
    }

    /**
     */
    protected String addBroker(MQAddress url, 
        boolean isLocal, boolean isConfig, UID uid)
        throws NoSuchElementException, BrokerException {

        if (!initialized) {
            throw new RuntimeException("Cluster not initialized");
        }

        String name = null;
        ClusteredBroker cb = null;
        if (isLocal) {
            synchronized(allBrokers) {
                name  = lookupBrokerID(url);
                if (name == null) {
                    cb = newClusteredBroker(url, isLocal, uid);
                    name = cb.getBrokerName();
                } else {
                    cb = getBroker(name);
                }
                synchronized(allBrokers) {
                    allBrokers.put(name, cb);
                }
            }
        } else {
            name = lookupBrokerID(url);
            if (name != null) {
                cb = getBroker(name);
            }
            if (name == null || cb == null) {
                throw new NoSuchElementException("Unknown broker "+url);
            }
        }
        if (uid != null) {
            cb.setBrokerSessionUID(uid);
        }
        if (isLocal) { 
           cb.setStatus(BrokerStatus.ACTIVATE_BROKER, null);
        }
        brokerChanged(ClusterReason.ADDED,
                      cb.getBrokerName(), null, cb, uid, null); 
        return name;
    }

    protected Map initAllBrokers(MQAddress myaddr) throws BrokerException {

        String cstr = Globals.getConfig().getProperty
                      (Globals.AUTOCLUSTER_BROKERMAP_CLASS_PROP);
        if (cstr == null) {
            return super.initAllBrokers(myaddr);
        }
        try {
            if (Globals.isNucleusManagedBroker()) {
                AutoClusterBrokerMap map = Globals.getHabitat().
                           getService(AutoClusterBrokerMap.class, cstr);
                if (map == null) {
                    throw new BrokerException("Class "+cstr+" not found");
                }
                map.init(this, myaddr);
                return (Map)map;
            } else {
                Class c = Class.forName(cstr);
                Class[] paramTypes = { ClusterManagerImpl.class, MQAddress.class };
                Constructor cons = c.getConstructor(paramTypes);
                Object[] paramArgs = { this, myaddr }; 
                return (Map)cons.newInstance(paramArgs);
            }
        } catch (Exception e) {
             throw new BrokerException(e.getMessage(), e);
        }
    }

    protected LinkedHashSet parseBrokerList()
    throws MalformedURLException {

        String val = config.getProperty(AUTOCONNECT_PROPERTY);
        if (val != null) {
             logger.log(Logger.INFO,
                 BrokerResources.W_IGNORE_PROP_SETTING,
                 AUTOCONNECT_PROPERTY+"="+val);
        }

        val = config.getProperty(Globals.IMQ
                         + ".cluster.brokerlist.manual");
        if (val != null) {
             logger.log(Logger.INFO,
                 BrokerResources.W_IGNORE_PROP_SETTING,
                 Globals.IMQ + ".cluster.brokerlist.manual"+"="+val);
        }
        LinkedHashSet brokers = new LinkedHashSet();
        synchronized(allBrokers) {
            Iterator itr = allBrokers.values().iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                ClusteredBroker b = (ClusteredBroker)o;
                if (!b.isLocalBroker()) {
                    brokers.add(b.getBrokerURL());
                }
            }
        }
        return brokers;
   }

    public String lookupBrokerID(MQAddress address) {

        if (!initialized) {
            throw new RuntimeException("Cluster manager is not initialized");
        }
        try {
             synchronized(allBrokers) {
                 ((AutoClusterBrokerMap)allBrokers).updateMap();
             }
        } catch (BrokerException e) {
             logger.logStack(logger.WARNING, e.getMessage(), e);
        }
        return super.lookupBrokerID(address);
    }

    public Iterator getConfigBrokers() {
        return getKnownBrokers(true);
    }

    public int getConfigBrokerCount() {
        return super.getKnownBrokerCount();
    }

    public Iterator getKnownBrokers(boolean refresh) {

        if (!initialized) {
            throw new RuntimeException("Cluster manager is not initialized");
        }

        if (refresh) {
            try {
                synchronized(allBrokers) {
                    ((AutoClusterBrokerMap)allBrokers).updateMap(true);
                }
            } catch (BrokerException e) {
                logger.logStack(logger.WARNING, e.getMessage(), e);
            }
        }
        return super.getKnownBrokers(refresh);
    }

    public ClusteredBroker getBroker(String brokerid) {

        ClusteredBroker cb = super.getBroker(brokerid);
        if (cb != null) {
            return cb;
        }
        try {
             synchronized(allBrokers) {
                 ((AutoClusterBrokerMap)allBrokers).updateMap(true);
             }
        } catch (BrokerException e) {
             logger.logStack(logger.WARNING, e.getMessage(), e);
        }
        return super.getBroker(brokerid);
    }
}
