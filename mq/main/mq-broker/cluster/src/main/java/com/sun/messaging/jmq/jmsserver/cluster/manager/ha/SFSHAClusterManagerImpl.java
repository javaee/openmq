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
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.manager.ha;

import java.util.*;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.persist.api.MigratableStoreUtil;
import com.sun.messaging.jmq.jmsserver.persist.api.StoreManager;
import com.sun.messaging.jmq.jmsserver.persist.api.HABrokerInfo;
import com.sun.messaging.jmq.jmsserver.cluster.api.*;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.*;
import com.sun.messaging.jmq.jmsserver.multibroker.BrokerInfo;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import org.jvnet.hk2.annotations.Service;
import javax.inject.Singleton;

// XXX FOR TEST CLASS
import java.io.*;


/**
 * This class extends ClusterManagerImpl and is used to obtain and
 * distribute cluster information in an HA cluster.
 */

@Service(name = "com.sun.messaging.jmq.jmsserver.cluster.manager.ha.SFSHAClusterManagerImpl")
@Singleton
public class SFSHAClusterManagerImpl extends HAClusterManagerImpl 
{

    /**
     * The brokerid associated with the local broker.
     * The local broker is running in the current vm.
     */

    //private String localBrokerId = null;

   /**
    */
   public SFSHAClusterManagerImpl() throws BrokerException
   {
       super();
   }

   public String initialize(MQAddress address)
   throws BrokerException {

       Globals.getStore().updateBrokerInfo(Globals.getBrokerID(),
           HABrokerInfo.UPDATE_URL, null, address.toString());

       String bid = super.initialize(address);
       UID ss = Globals.getStoreSession();
       ((SFSHAClusteredBrokerImpl)getLocalBroker()).setStoreSessionUID(ss);
       return bid;
   }

   protected void checkStore() throws BrokerException {
       if (!StoreManager.isConfiguredBDBSharedFS()) {
           throw new BrokerException(
                 Globals.getBrokerResources().getKString(
                 BrokerResources.E_HA_CLUSTER_INVALID_STORE_CONFIG));
       }
   }

   protected HAClusteredBroker newHAClusteredBroker(String brokerid,
             MQAddress url, int version, BrokerState state, UID session)
                        throws BrokerException {
       return new SFSHAClusteredBrokerImpl(brokerid, url,
                      version, state, session, this);
   }

   protected ClusteredBroker updateBrokerOnActivation(ClusteredBroker broker,
                                                      Object userData) {
       ((SFSHAClusteredBrokerImpl)broker).setStoreSessionUID(
           ((BrokerInfo)userData).getBrokerAddr().getStoreSessionUID());
       ((SFSHAClusteredBrokerImpl)broker).setRemoteBrokerStateOnActivation();
       return broker;
   }

   protected ClusteredBroker updateBrokerOnDeactivation(ClusteredBroker broker,
                                                        Object userData) {
       ((SFSHAClusteredBrokerImpl)broker).setRemoteBrokerStateOnDeactivation();
       return broker;
   }

   /**
    * finds the brokerid associated with the given session.
    *
    * @param uid is the session uid to search for
    * @return the uid associated with the session or null we cant find it.
    */
   public String lookupStoreSessionOwner(UID uid) {

       try {
           if (Globals.getMyAddress().getStoreSessionUID().equals(uid)) {
               return Globals.getBrokerID();
           }
           if (Globals.getStore().ifOwnStoreSession(uid.longValue(), (String)null)) {
               return Globals.getBrokerID(); 
           }
           return Globals.getClusterBroadcast().lookupStoreSessionOwner(uid);
       } catch (Exception ex) {
           logger.logStack(logger.WARNING, ex.getMessage(), ex);
       }
       return null;
   }

   protected Map newHABrokerInfoMap() throws BrokerException {
       return new SFSHABrokerInfoMap(this);
   }

   public ClusteredBroker getBrokerByNodeName(String nodeName) 
   throws BrokerException {

       if (!initialized) {
           throw new RuntimeException("Cluster not initialized");
       }
       HAClusteredBrokerImpl cb = null;
       synchronized (allBrokers) {
           Iterator itr = allBrokers.values().iterator();
           while (itr.hasNext()) {

               cb = (HAClusteredBrokerImpl)itr.next();
               String instn = cb.getInstanceName();
               UID ss = cb.getStoreSessionUID();
               if (instn != null && ss != null) {
                   if (MigratableStoreUtil.makeEffectiveBrokerID(instn, ss).equals(nodeName)) {
                       return cb;
                   }
               }
           }
       }
       return null;
   }

}
