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
 * @(#)MetricData.java	1.5 06/27/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.Serializable;
import com.sun.messaging.jmq.util.MetricCounters;

/**
 * This class represents metric performance data that is derived
 * from MetricCounters
 */

public class MetricData implements Serializable
{

    public MetricCounters   totals = null;
    public MetricCounters   rates  = null;

    public long     totalMemory;
    public long     freeMemory;

    public long     timestamp;

    public int      nConnections;

    public MetricData() {
        totals = new MetricCounters();
        rates  = new MetricCounters();
        reset();
    }

    /**
     * Reset counters to 0
     */
    public synchronized void reset() {

        totals.reset();
        rates.reset();

        timestamp = 0;
        totalMemory = 0;
        freeMemory = 0;
        nConnections = 0;
    }

    public synchronized void setTotals(MetricCounters counters) {
        totals.reset();
        totals.update(counters);
    }

    public synchronized void setRates(MetricCounters counters) {
        rates.reset();
        rates.update(counters);
    }

    public String toString() {
        String s =

        "Connections: " + nConnections + "    JVM Heap: " +
            totalMemory + " bytes (" + freeMemory + " free)" +
	" Threads: " + totals.threadsActive + " (" + totals.threadsLowWater + "-" + totals.threadsHighWater + ")" + "\n" +
        "      In: " +
        totals.messagesIn + " msgs (" + totals.messageBytesIn +  " bytes)  " +
         totals.packetsIn +  " pkts (" + totals.packetBytesIn  + " bytes)\n" +
        "     Out: " +
        totals.messagesOut + " msgs (" + totals.messageBytesOut +  " bytes)  " +
         totals.packetsOut +  " pkts (" + totals.packetBytesOut  + " bytes)\n" +
        " Rate In: " +
          rates.messagesIn + " msgs/sec (" + rates.messageBytesIn + " bytes/sec)  " +
           rates.packetsIn +  " pkts/sec (" + rates.packetBytesIn + " bytes/sec)\n" +
        "Rate Out: " +
         rates.messagesOut + " msgs/sec (" + rates.messageBytesOut + " bytes/sec)  " +
          rates.packetsOut +  " pkts/sec (" + rates.packetBytesOut + " bytes/sec)";

        return s;
    }
}
