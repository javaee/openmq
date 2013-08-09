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
 * %W% %G%
 */ 

package com.sun.messaging.jmq.jmsserver.core;

import java.util.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.PartitionNotFoundException;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.jmsserver.plugin.spi.ProducerSpi;
import com.sun.messaging.jmq.util.log.Logger;

/**
 *
 */

//XXX - it would be nice to add metrics info
// unfortunately we dont know what producer a message
// comes from at this time
public class Producer extends ProducerSpi {
    
    transient Set destinations = null;
    private transient DestinationList DL = Globals.getDestinationList();
    private transient PartitionedStore pstore = null;

    /**
     */
    private Producer(ConnectionUID cuid, DestinationUID duid,
                     String id, PartitionedStore ps) {
        super(cuid, duid, id);
        this.pstore = ps;
    }
   
    public static Producer createProducer(DestinationUID duid,
              ConnectionUID cuid, String id, PartitionedStore ps) 
    {
        Producer producer = new Producer(cuid, duid, id, ps);
        Object old = allProducers.put(producer.getProducerUID(), producer);
        if (duid.isWildcard()) {
            wildcardProducers.add(producer.getProducerUID());
        }
        assert old == null : old;

        return producer;
    }


    public synchronized void destroyProducer() {
        if (getDestinationUID().isWildcard()) {
            wildcardProducers.remove(getProducerUID());
            // remove from each destination
            List[] dss = null;
            try {
                dss = DL.findMatchingIDs(pstore, getDestinationUID());
            } catch (PartitionNotFoundException e) {
                if (DEBUG) {
                logger.log(logger.INFO, 
                "Producer.destroyProducer on "+getDestinationUID()+": "+e.getMessage());
                }
                dss = new List[]{ new ArrayList<DestinationUID>() };
            }
            List duids = dss[0];
            Iterator itr = duids.iterator();
            while (itr.hasNext()) {
                DestinationUID duid = (DestinationUID)itr.next();
                Destination[] dd = DL.getDestination(pstore, duid);
                Destination d = dd[0];
                if (d != null) {
                   d.removeProducer(uid);
                }
            }
        } else {
            Destination[] dd = DL.getDestination(pstore, getDestinationUID());
            Destination d = dd[0];
            if (d != null) {
                d.removeProducer(uid);
            }
        }
        destroy();
    }

    public synchronized void destroy() {
        super.destroy();
        lastResumeFlowSizes.clear();
    }

    public boolean isWildcard() {
        return destination_uid.isWildcard();
    }

    public Set getDestinations() {
        if (this.destinations == null) {
            destinations = new HashSet();
            if (!destination_uid.isWildcard()) {
                destinations.add(destination_uid);
            } else {
                List[] ll = null;
                try {
                    ll = DL.findMatchingIDs(pstore, destination_uid);
                } catch (PartitionNotFoundException e) {
                    if (DEBUG) {
                    logger.log(logger.INFO, 
                    "Producer.getDestinations() on "+getDestinationUID()+": "+e.getMessage());
                    }
                    ll = new List[]{ new ArrayList<DestinationUID>() };
                }
                List l = ll[0];
                Iterator itr = l.iterator();
                while (itr.hasNext()) {
                    DestinationUID duid = (DestinationUID)itr.next();
                    destinations.add(duid);
                }
                    
            }
        }
        return destinations;
    }
}
