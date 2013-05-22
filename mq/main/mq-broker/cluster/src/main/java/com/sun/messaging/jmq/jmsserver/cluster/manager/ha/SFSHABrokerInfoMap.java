/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver.cluster.manager.ha;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.persist.api.HABrokerInfo;
import com.sun.messaging.jmq.jmsserver.cluster.api.BrokerState;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusteredBroker;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusterManager;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAClusteredBroker;
import com.sun.messaging.jmq.jmsserver.cluster.manager.ClusterReason;
import com.sun.messaging.jmq.jmsserver.cluster.manager.AutoClusterBrokerMap;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

/**
 *  For shared file system store.
 */
public class SFSHABrokerInfoMap extends HashMap implements Map, AutoClusterBrokerMap
{
        SFSHAClusterManagerImpl parent = null;

        /**
         * Create an instance of HAMap
         * @throws BrokerException 
         */
        public SFSHABrokerInfoMap(SFSHAClusterManagerImpl manager)
        throws BrokerException {
            init(manager, null);
        }

        public void init(ClusterManager mgr, MQAddress addr) throws BrokerException {
            this.parent = (SFSHAClusterManagerImpl)mgr;

            Map map = Globals.getStore().getAllBrokerInfos();
            Iterator itr = map.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry)itr.next();
                String key = (String)entry.getKey();
                HABrokerInfo bi = (HABrokerInfo)entry.getValue();
                HAClusteredBroker cb =  new SFSHAClusteredBrokerImpl(bi.getId(), bi, parent);
                put(key,cb);
                parent.brokerChanged(ClusterReason.ADDED, cb.getBrokerName(),
                                     null, cb, cb.getBrokerSessionUID(), null);
            }
        }


        /**
         * Method which reloads the contents of this map from the
         * current information in the JDBC store.
         * @throws BrokerException 
         */
        public void updateMap() 
            throws BrokerException
        {
            updateMap(false);
        }

        public void updateMap(boolean all)
            throws BrokerException
        {
            if (all) {
                updateHAMapForState(null);
            } else {
                updateHAMapForState(BrokerState.OPERATING);
            } 
        }

        private void updateHAMapForState(BrokerState state)
            throws BrokerException
        {
            Map map = null;
            if (state == null) {
			    map = Globals.getStore().getAllBrokerInfos();
            } else {
			    map = Globals.getStore().getAllBrokerInfoByState(state);
            }

            Iterator itr = map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry)itr.next();
                String key = (String)entry.getKey();
                HABrokerInfo bi = (HABrokerInfo)entry.getValue();
                HAClusteredBrokerImpl impl = (HAClusteredBrokerImpl)get(key);
                if (impl == null) {
                    HAClusteredBroker cb =
                        new SFSHAClusteredBrokerImpl(bi.getId(), bi, parent);
                    put(key,cb);
                    parent.brokerChanged(ClusterReason.ADDED, cb.getBrokerName(),
                                         null, cb, cb.getBrokerSessionUID(), null);
                } else {
                    impl.update(bi);
                }
            }

            if (state == null) {
                itr = entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry entry = (Map.Entry)itr.next();
                    String key = (String)entry.getKey();
                    if (!map.containsKey(key)) {
                        itr.remove();
                        HAClusteredBrokerImpl impl = (HAClusteredBrokerImpl)entry.getValue();
                        parent.brokerChanged(ClusterReason.REMOVED, impl.getBrokerName(),
                                             impl, null, impl.getBrokerSessionUID(), null);
                    }
                }
            }
        }

          
        /**
         * Retrieves the HAClusteredBroker associated with the passed in 
         * broker id. 
         * If the id is not found in the hashtable, the store will be checked.
         * @param key the brokerid to lookup
         * @return the HAClusteredBroker object (or null if one can't be found)
         */
         public Object get(Object key) {
             return get(key, false);
         }

        /**
         * Retrieves the HAClusteredBroker associated with the passed in 
         * broker id. 
         * If the id is not found in the hashtable, the store will be checked.
         * @param key the brokerid to lookup
         * @param update update against store
         * @return the HAClusteredBroker object (or null if one can't be found)
         */
        public Object get(Object key, boolean update) {
            // always check against the backing store
            Object o = super.get(key);
            if (o == null || update) {
                try {
                    HABrokerInfo m= Globals.getStore().getBrokerInfo((String)key);
                    if (m != null && o == null) {
                        HAClusteredBroker cb =  new SFSHAClusteredBrokerImpl((String)key, m, parent);
                        put(key,cb);
                        parent.brokerChanged(ClusterReason.ADDED, cb.getBrokerName(),
                                             null, cb, cb.getBrokerSessionUID(), null);
                        o = cb;
                     }
                     if (m != null && update) {
                         ((HAClusteredBrokerImpl)o).update(m);
                     }
                 } catch (BrokerException ex) {
                     Globals.getLogger().logStack(Logger.WARNING,
                     "Exception while creating broker entry " + key , ex);
                }
            }
            return o;
        }
}

