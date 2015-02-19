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
 * @(#)SessionQueue.java	1.25 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import java.io.PrintStream;

import javax.jms.JMSException;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.AdministeredObject;

/**
 * A synchronized queue interface to allow threads to wait on a dequeue
 * and be notified when another thread enqueues data to the queue.
 */

class SessionQueue implements Traceable {

    private MessageQueue queue = null;

    protected boolean isLocked = false;
    private boolean sessionIsStopped = false;
    protected boolean isClosed = false;

    protected boolean listenerIsSetLate = false;

    protected boolean debug = Debug.debug;

    private long constructTime = 0;

    /**
     * This property (if defined) will make make the system use
     * SequentialQueue instead of PriorityQueue.
     */
    private static String USE_SEQUENTIAL =
    System.getProperty ("imq.useSequentialQueue");

    protected synchronized void
    enqueueNotify (Object pkt) {
        enqueue (pkt);
        notifyAll();
    }

    public SessionQueue() {

        if ( USE_SEQUENTIAL == null ) {
            queue = new PriorityQueue();
        } else {
            queue = new SequentialQueue();
        }
    }

    /**
     *Constructor.
     */
    public SessionQueue(boolean useSequential, int size) {

        if ( useSequential ) {
            queue = new SequentialQueue(size);
        } else {
            queue = new PriorityQueue();
        }
    }

    /**
     * This method is to respond to the JVM bug (MQ 6174742, 6089070).
     * This is called from
     *
     * 1. SessionImpl's constructor, immediately after it is constructed.
     * 2. From this.isEmpty().  Called only when queue is null.
     */
    protected synchronized void validateQueue() {

        //the statement here is on purpose.  set after a new Session is
        //constructed
        if ( constructTime == 0 ) {
            constructTime = System.currentTimeMillis();
        }

        if ( queue == null ) {
            long diff = System.currentTimeMillis() - this.constructTime;
            throw new java.lang.Error
                ("JVM Error. Message Queue is null. Create time: " + constructTime + " duration: " + diff);
        }
    }

    /**
     * queue size
     */
     protected int size() {
        return queue.size();
     }

     /**
      * Check if queue is empty.
      * @return
      */
     protected boolean isEmpty() {

         if ( queue == null ) {
             validateQueue();
         }

         return queue.isEmpty();
     }

    /**
     *Clears all the elements from the queue
     **/
    protected void
    clear () {
        queue.clear();
    }

    /**
     * Get an array of objects from the queue.
     * @return an array of objects from the queue.
     */
    protected Object[] toArray() {
        return queue.toArray();
    }

    /**
     * remove the specified object from the queue.
     * @param obj the object to be removed from the queue.
     * @return true if the object was in the queue and removed.
     */
    protected boolean remove (Object obj) {
        return queue.remove(obj);
    }

    /**
     * Enqueues an object in the queue with no special synchronization.
     * @param nobj new object to be enqueued
     */
    protected void
    enqueue(Object nobj) {

        queue.enqueue(nobj);

        if ( debug ) {
            Debug.println(this);
        }
    }
    
    /**
     * Adds an object to the front of the queue with no special synchronization.
     * @param nobj new object to be added to the front of the queue
     */
    protected void
    enqueueFirst(Object nobj) {

        queue.enqueueFirst(nobj);

        if ( debug ) {
            Debug.println(this);
        }
    }

    /**
    Dequeues an element from the queue without any special synchronization.
    @return dequeued object, or null if empty queue
    */
    protected Object
    dequeue() {

        Object obj = null;

        if (queue.isEmpty() == false) {
            obj = queue.dequeue();
        }

        if ( debug ) {
            Debug.println( this );
        }

        return obj;
    }

    /**
     * If this object is used for SessionReader, when Connection.stop() is
     * called, the SessionReader will eventually come to this method and
     * call setSessionIsStopped().  The thread that blocks on Connection.stop()
     * which calls Session.stop() which calls SessionQueue.stop() will get
     * notified and be able to return.
     *
     */
    protected synchronized Object dequeueWait(long timeout) {
        // if queue is empty  or is stopped (isLocked set to true)
        while ( isEmpty() || isLocked ) {

            if ( isClosed ) {
                return null;
            }

            if ( isLocked ) {
                //set this value so that we are sure the session reader is
                //blocked.
                setSessionIsStopped (true);
            }

            if ( listenerIsSetLate ) {
                /**
                 * listenerIsSetLate flag is reset to false in
                 * SessionReader.deliver() method.
                 */
                return null;
            }

            // wait for notification that queue is not empty
            try  {
                wait(timeout);
                //check if wait timeout.
                if ( isEmpty() && (isLocked == false) && (timeout > 0) ) {
                    // if it is timeout, return null.
                    return null;
                }
            }
            catch (InterruptedException e)  {
                Debug.printStackTrace(e);
                //fall to dequeu below
            }

        }

        return dequeue();
    }

    /**
     * default wait forever.
     * @return
     *
     */
    protected synchronized Object dequeueWait() {
        //dupsOkPerf
        return dequeueWait (0);
    }

    protected synchronized void setIsLocked( boolean state ) {
        //System.out.println ("queue lock state:; " + state);
        isLocked = state;
        notifyAll();
    }

    protected synchronized boolean getIsLocked() {
        return isLocked;
    }

    //Session reader set this value to true if it is locked and in wait mode.
    protected synchronized void setSessionIsStopped( boolean state) {

        if ( debug ) {
            Debug.println("session reader is stopped: " + state);
        }

        sessionIsStopped = state;
        notifyAll();
    }

    /**
     *when Connection.stop is called, each session call this method to ensure no
     *messages will be delivered until Connection.start() is called.
     *
     *This method is not returned until SessionReader is locked and blocked.
     */
    protected synchronized void waitUntilSessionStopped() {

        try {
            while ( isClosed==false && isLocked && sessionIsStopped==false ) {
                wait ();
            }
        } catch (InterruptedException e)  {
            ;
        }

    }

    /**
     * Stop the session reader.
     *
     * This method is called from the thread that calls Connection.stop()
     */
    protected synchronized void stop(boolean doWait) {

        setIsLocked (true);

        if ( doWait ) {
            waitUntilSessionStopped();
        } else {
            sessionIsStopped = true;
        }
    }

    /**
     * Start the session reader
     *
     *  This method is called from the thread that calls Connection.start()
     */
    protected synchronized void start() {
        setIsLocked( false );
        setSessionIsStopped ( false );
    }

    protected synchronized void close() {
        //unlock queue
        isClosed = true;
        setIsLocked (false);
        if ( debug ) {
            Debug.println ("Session queue closed ...");
        }
    }

    protected synchronized boolean waitMaxInterval(long interval) {
        long endtime = System.currentTimeMillis()+interval;
        long waittime = interval;
        while (!isClosed && !isLocked && !sessionIsStopped) {
            try {
                wait(waittime);
                long currtime = System.currentTimeMillis();
                if (currtime >= endtime) {
                    return true;
                }
                waittime = endtime - currtime;
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    protected boolean getIsClosed() {
        return isClosed;
    }

    /**
    Prints the queue to the debug display in a human-readable format.
    */
    public String
    toString() {
        Object tmp;
        StringBuffer strbuf = null;

        int cntr = 0;
        strbuf = new StringBuffer (this.getClass().getName() + ": \n");

        Object[] objs = toArray();
        for (cntr = 0; cntr < objs.length; cntr++) {
            tmp = objs[cntr];
            strbuf.append ("Element " + cntr + " :" + tmp.toString() + "\n");
        }

        return strbuf.toString();
    }

    protected synchronized void setListenerLateNotify() {
        listenerIsSetLate = true;
        notifyAll();
    }

    protected synchronized void setListenerLate(boolean state) {
        listenerIsSetLate = state;
    }

    protected synchronized boolean isListenerSetLate() {
        return listenerIsSetLate;
    }

    //PRIORITYQ
    public void dump ( PrintStream ps ) {
        ps.println ("------ SessionQueue dump ------");
        ps.println( "queue size: " + size() );

        //Get queu array
        Object[] objs = queue.toArray();
        //get array size
        int size = objs.length;
        for ( int i=0; i<size; i++ ) {
            //dump each element.
            Object element = objs[i];
            if ( element instanceof ReadWritePacket ) {
                ((ReadWritePacket)element).dump (ps);
            } else {
                if ( element != null  && element instanceof Traceable) {
                    ((Traceable)element).dump(ps);
                }
            }

        }
    }

    protected Hashtable getDebugState(boolean verbose) {
        Hashtable ht = new Hashtable();
        ht.put("isLocked", Boolean.valueOf(isLocked));
        ht.put("sessionIsStopped", Boolean.valueOf(sessionIsStopped));
        ht.put("isClosed", Boolean.valueOf(isClosed));
        ht.put("listenerIsSetLate", Boolean.valueOf(listenerIsSetLate));
        ht.put("constructTime", Long.valueOf(constructTime));
        ht.put("queueSize", queue.size());
        if (verbose) {
            Vector v = new Vector();
            Object[] objs = queue.toArray();
            int osize = objs.length;
            Object o = null;
            for ( int i = 0; i < osize; i++ ) {
                o = objs[i];  
                if (o instanceof ReadOnlyPacket) {
                    v.add(((ReadOnlyPacket)o).getMessageID()); 
                } else {
                    v.add(o.toString());
                }
            }
            ht.put("queue", v);
        }
        return ht;
    }
}
