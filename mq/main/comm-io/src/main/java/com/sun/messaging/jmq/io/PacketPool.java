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
 * @(#)PacketPool.java	1.8 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.ArrayList;

/**
 *
 * A pool of Packets.
 *
 * The latest Packet code makes use of nio direct ByteBuffers. Direct
 * buffers are faster than byte[]s, but are more expensive to allocate.
 * To compensate for this we introduce a packet pool so that packets
 * can be reaused.
 *
 */
public class PacketPool {

    // List of packets. This will grow as needed.
    ArrayList pool = null;

    // Initial size of list of packets.
    static final int INITIALSIZE = 128;

    // Max size of buffer pool in # of packets
    int capacity = INITIALSIZE;
    int size = 0;

    // Diagnostic counters
    int   hits = 0;
    int misses = 0;

    int drops = 0;
    int adds = 0;

    boolean resetPacket = false;
    boolean dontTimestampPacket= false;



    /**
     *Create an empty packet pool with a default capacity (128)
     */
    public PacketPool() {
        pool = new ArrayList(INITIALSIZE);
    }

    /**
     * Create an empty packet pool with a capacity
     */
    public PacketPool(int capacity) {
        this.capacity = capacity;
        pool = new ArrayList(INITIALSIZE);
    }

    /**
     * Create an empty packet pool with a capacity
     */
    public PacketPool(int capacity, boolean resetPacket, boolean dontTiemstamp) {
        this.capacity = capacity;
        pool = new ArrayList(INITIALSIZE);
        this.dontTimestampPacket = dontTiemstamp;
        this.resetPacket = resetPacket;
    }

    /**
     * Set the pool's capacity
     */
    public void setCapacity(int n) {
        this.capacity = n;
    }

    /**
     * Get the pool's capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Get a packet from the pool. If the pool is empty a newly allocated
     * packet is returned.
     */
    public synchronized Packet get() {
        if (size > 0) {
            size--;
	    hits++;
            return (Packet)(pool.remove(pool.size() - 1));
        } else {
            misses++;
            Packet p =  new Packet();
            if (dontTimestampPacket) {
                p.generateSequenceNumber(false);
                p.generateTimestamp(false);
            }
            return p;
        }
    }

    /**
     * Return a packet to the pool. If the pool capacity is exceeded the
     * packet is not placed in the pool (and presumeably left for 
     * garbage collection).
     */
    public void put(Packet p) {
        if (p == null) return;

        if (resetPacket)
            p.reset();
   
        synchronized(this) {
        
            if (size < capacity) {
                // Clear packet and add it to the pool
                size++;
                pool.add(p);
	        adds++;
            } else {
                // Drop it on floor
                drops++;
            }
        }
    }

    /**
     * Empty the pool.
     */
    public synchronized void clear() {
        // We reallocate the ArrayList so it shrinks back to initial size
        pool = null;
        size = 0;
        pool = new ArrayList(INITIALSIZE);
    }

    public String toString() {
        return super.toString() + ": capacity=" + capacity +
                 ", size=" + size;
    }

    public String toDiagString() {
        return toString() + ", hits=" + hits + ", misses=" + misses +
            ", adds=" + adds + ", drops=" + drops;
    }
}
