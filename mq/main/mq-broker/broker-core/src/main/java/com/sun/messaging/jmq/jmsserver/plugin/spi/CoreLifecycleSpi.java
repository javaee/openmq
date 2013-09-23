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

package com.sun.messaging.jmq.jmsserver.plugin.spi;

import java.util.List;
import java.util.Hashtable;
import java.util.Iterator;
import java.io.IOException;

import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.PartitionNotFoundException;
import com.sun.messaging.jmq.jmsserver.core.Session;
import com.sun.messaging.jmq.jmsserver.core.ProducerUID;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationList;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.AdminDataHandler;

public abstract class CoreLifecycleSpi { 

    public static final String GFMQ = "GLASSFISH_MQ"; 
    public static final String CHMP = "COHERENCE_MESSAGE_PATTERN";

    protected PacketRouter pktr = null; 

    public CoreLifecycleSpi() {}

    public abstract String getType();

    public PacketRouter getPacketRouter() {
        return pktr;
    }

    public abstract void initDestinations() throws BrokerException;

    public abstract void initSubscriptions() throws BrokerException;

    public abstract void initHandlers(PacketRouter prt, ConnectionManager cm,
        PacketRouter adminprt, AdminDataHandler adminh)
        throws BrokerException;

    public abstract void cleanup(); 

    public DestinationList getDestinationList() {
        return null;
    }

    public int getMaxProducerBatch() {
        return 0;
    }

    /********************************************
     * SessionOp static method
     **********************************************/

    public abstract SessionOpSpi newSessionOp(Session ss); 

    /********************************************
     * Producer static methods
     **********************************************/

    public abstract Hashtable getProducerAllDebugState(); 

    public abstract void clearProducers();

    public abstract Iterator getWildcardProducers();

    public abstract int getNumWildcardProducers();

    public abstract String checkProducer(ProducerUID uid);

    public abstract void updateProducerInfo(ProducerUID uid, String str);

    public abstract Iterator getAllProducers();

    public abstract int getNumProducers();

    public abstract ProducerSpi getProducer(ProducerUID uid);

    public abstract ProducerSpi destroyProducer(ProducerUID uid, String info);

    public abstract ProducerSpi getProducer(String creator);

    /***********************************************
     * Destination static methods
     ************************************************/

    public abstract DestinationSpi[] getDestination(PartitionedStore ps, DestinationUID uid);

    public abstract DestinationSpi[] getDestination(PartitionedStore ps, String name, boolean isQueue)
    throws IOException, BrokerException; 

    public abstract DestinationSpi[] getDestination(PartitionedStore ps, DestinationUID duid, int type,
                                                  boolean autocreate, boolean store)
                                                  throws IOException, BrokerException;

    public abstract DestinationSpi[] getDestination(PartitionedStore ps, String name, int type,
                                                boolean autocreate, boolean store)
                                                throws IOException, BrokerException ;
    public abstract DestinationSpi[] createTempDestination(PartitionedStore ps, String name,
        int type, ConnectionUID uid, boolean store, long time)
        throws IOException, BrokerException;

    public abstract List[] findMatchingIDs(PartitionedStore ps, DestinationUID wildcarduid) 
    throws PartitionNotFoundException;
    
    public abstract DestinationSpi[] removeDestination(PartitionedStore ps,
        String name, boolean isQueue, String reason)
        throws IOException, BrokerException;

    public abstract DestinationSpi[] removeDestination(PartitionedStore ps, DestinationUID uid,
        boolean notify, String reason)
        throws IOException, BrokerException; 

    public abstract boolean canAutoCreate(boolean queue);

    /********************************************
     * Consumer static methods
     **********************************************/

    public abstract ConsumerSpi getConsumer(ConsumerUID uid);

    public abstract int calcPrefetch(ConsumerSpi consumer,  int cprefetch);
}
