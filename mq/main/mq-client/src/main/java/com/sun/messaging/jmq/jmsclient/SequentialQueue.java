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
 * @(#)SequentialQueue.java	1.6 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Vector;

/**
 * <p>
 * Queue structure to store messages in a serial order.
 * Messages received in a Session are stored in its received order.
 * </P>
 */

public class SequentialQueue implements MessageQueue {
    //queue structure.
    private Vector queue = null;

    /**
     * default constructor.
     */
    public SequentialQueue() {
        queue = new Vector ();
    }

    /**
     * constructor with init queue size.
     * @param size the init size for the queue size.
     */
    public SequentialQueue(int size) {
        queue = new Vector (size);
    }

    /**
     * constructor with init queue size and increment number.
     * @param size the init size for the queue size.
     * @param increment number to increase queue size the when reached
     *                  the init size.
     */
    public SequentialQueue(int size, int increment) {
        queue = new Vector (size, increment);
    }

    /**
     * get queue size
     */
     public int size() {
        return queue.size();
     }

     /**
      * check if the queue size is empty.
      * @return true if the queue size is empty.
      */
     public boolean isEmpty() {
        return queue.isEmpty();
     }

    /**
     * Clears all elements from the queue
     **/
    public void clear () {
        queue.clear();
    }

    /**
     * Enqueues an object in the queue.
     * @param nobj new object to be enqueued
     */
    public void enqueue(Object nobj) {
        queue.addElement(nobj);
    }

    /**
     * Dequeues an element from the queue.
     * @return dequeued object, or null if empty queue
    */
    public Object dequeue() {
        //var to hold element to be returned.
        Object obj = null;

        /**
         * not synced since we have only one thread in the session that
         * access this queue.
         *
         * Note: added sync for general purpose.
         */
        synchronized (queue) {
            if (queue.isEmpty() == false) {
                obj = queue.remove(0);
            }
        }

        return obj;
    }

    /**
     * Get all elements in the queue and return as an array
     * of objects.
     *
     * @return an array of objects in the queue.
     */
    public Object[] toArray() {
        return queue.toArray();
    }

    /**
     * remove obj from the queue.
     * @param obj obj to be removed from the queue.
     * @return true if object is in the queue and removed.  Otherwise,
     *         return false.
     */
    public boolean remove (Object obj) {
        return queue.remove(obj);
    }

    /**
     * Adds the specified object to the front of the queue.
     * @param nobj new object to be added to the front of the queue
     */
	@Override
	public void enqueueFirst(Object nobj) {
		// This method was added for PriorityQueue, not for SequentialQueue
		// so has not been implemented   
		throw new RuntimeException("This method is not yet implemented");
	}

}
