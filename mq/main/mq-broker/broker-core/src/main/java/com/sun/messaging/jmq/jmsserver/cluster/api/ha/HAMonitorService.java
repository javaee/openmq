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

/*
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api.ha;

import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import org.jvnet.hk2.annotations.Contract;
import javax.inject.Singleton;

/**
 */
@Contract
@Singleton
public interface HAMonitorService {

     public void init(String brokerID, MQAddress brokerURL, 
         boolean resetTakeoverThenExit) throws Exception;

     /**
      * @return true if in takeover 
      */
     public boolean inTakeover();

     /**
      * @return in seconds 
      */
     public int getMonitorInterval(); 

     /**
      * @return true if d is a destination being taken over 
      */
     public boolean checkTakingoverDestination(Destination d);

     /**
      * @return true if p is a message being taken over
      */
     public boolean checkTakingoverMessage(Packet p);

     /**
      * @return remote broker id running on host:port
      */
     public String getRemoteBrokerIDFromPortMapper(
            String host, int port, String brokerID); 

     /**
      */
     public void takeoverBroker(HAClusteredBroker cb, Object extraInfo1,
                               Object extraInfo2, boolean force)
                               throws BrokerException; 


    /**
     * @return host:port string of the broker that takes over this broker
     *
     * Status code of exception thrown is important
     */
    public String takeoverME(HAClusteredBroker cb,
                           String brokerID, Long syncTimeout)
                           throws BrokerException; 

    public boolean isTakingoverTarget(String brokerID, UID storeSession); 
}
