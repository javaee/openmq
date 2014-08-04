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
 * @(#)Limitable.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

/**
 * Interface for lists which can have limited
 * capacities
 * @see NFLHashMap
 * @see AbstractNFLSet
 * @see Sized
 */
public interface Limitable
{
    public static final int UNLIMITED_CAPACITY = -1;
    public static final long UNLIMITED_BYTES = -1;

    /** 
     * sets the maximum size of an entry allowed
     * to be added to the collection
     * @param bytes maximum number of bytes for
     *        an object added to the list or
     *        UNLIMITED_BYTES if there is no limit
     */   
    public void setMaxByteSize(long bytes);
 
    /** 
     * returns the maximum size of an entry allowed
     * to be added to the collection
     * @return maximum number of bytes for an object
     *        added to the list  or
     *        UNLIMITED_BYTES if there is no limit
     */   
    public long maxByteSize();
 
    /**
     * Sets the capacity (size limit).
     *
     * @param cnt the capacity for this set (or
     *         UNLIMITED_CAPACITY if unlimited).
     */
    public void setCapacity(int cnt);

    /**
     * Sets the byte capacity. Once the byte capacity
     * is set, only objects which implement Sized
     * can be added to the class
     *
     * @param size the byte capacity for this set (or
     *         UNLIMITED_BYTES if unlimited).
     */
    public void setByteCapacity(long size);


    /**
     * Returns the capacity (count limit) or UNLIMITED_CAPACITY
     * if its not set.
     *
     * @return the capacity of the list
     */
    public int capacity();


    /**
     * Returns the byte capacity (or UNLIMITED_BYTES if its not set).
     *
     * @return the byte capacity for this set.
     */
    public long byteCapacity();



    /**
     * Returns <tt>true</tt> if either the bytes limit
     *         or the count limit is set and
     *         has been reached or exceeded.
     *
     * @return <tt>true</tt> if the count limit is set and
     *         has been reached or exceeded.
     */
    public boolean isFull();


    /**
     * Returns number of entries remaining in the
     *         lists to reach full capacity or
     *         UNLIMITED_CAPACITY if the capacity
     *         has not been set
     *
     * @return the amount of free space
     */
    public int freeSpace();

    /**
     * Returns the number of bytesremaining in the
     *         lists to reach full capacity, 0
     *         if the list is greater than the 
     *         capacity  or UNLIMITED_BYTES if 
     *         the capacity has not been set
     *
     * @return the amount of free space
     */
    public long freeBytes();


    /**
     * Returns the number of bytes used by all entries in this set which implement
     * Sized.  If this
     * set contains more than <tt>Long.MAX_VALUE</tt> elements, returns
     * <tt>Long.MAX_VALUE</tt>.
     *
     * @return the total bytes of data from all objects implementing
     *         Sized in this set.
     * @see Sized
     * @see #size
     */
    public long byteSize();
    
    /**
     * Returns the number of entries in this collection.  If this
     * set contains more than <tt>Long.MAX_VALUE</tt> elements, returns
     * <tt>Long.MAX_VALUE</tt>.
     *
     * @return the total bytes of data from all objects implementing
     *         Sized in this set.
     * @see Sized
     * @see #size
     */
    public int size();

    /**
     * Maximum number of messages stored in this
     * list at any time since its creation.
     *
     * @return the highest number of messages this set
     * has held since it was created.
     */
    public int highWaterCount();

    /**
     * Maximum number of bytes stored in this
     * list at any time since its creation.
     *
     * @return the largest size (in bytes) of
     *  the objects in this list since it was
     *  created.
     */
    public long highWaterBytes();

    /**
     * The largest message (which implements Sized)
     * which has ever been stored in this list.
     *
     * @return the number of bytes of the largest
     *  message ever stored on this list.
     */
    public long highWaterLargestMessageBytes();

    /**
     * Average number of messages stored in this
     * list at any time since its creation.
     *
     * @return the average number of messages this set
     * has held since it was created.
     */
    public float averageCount();

    /**
     * Average number of bytes stored in this
     * list at any time since its creation.
     *
     * @return the largest size (in bytes) of
     *  the objects in this list since it was
     *  created.
     */
    public double averageBytes();

    /**
     * The average message size (which implements Sized)
     * of messages which has been stored in this list.
     *
     * @return the number of bytes of the average
     *  message stored on this list.
     */
    public double averageMessageBytes();

}
