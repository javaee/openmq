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
 * @(#)ReceiveQueue.java	1.10 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Vector;
import java.io.PrintStream;

/**
 * This Class is used by MessageConsumerImpl and ProtocolHandler(for ack use).
 */

class ReceiveQueue extends SessionQueue {

    private boolean receiveInProcess = false;

    public ReceiveQueue() {
        super();
    }

    public ReceiveQueue (boolean useSequential, int size) {
        super (useSequential, size);
    }

    protected synchronized Object dequeueWait() {
        return dequeueWait (0);
    }

    /**
    * receive with time out.
    */
    protected synchronized Object dequeueWait ( long timeout ) {

        long waitTime = timeout;
        boolean expired = false;

        while ( isEmpty() || isLocked ) {

            if ( isClosed || expired ) {
                return null;
            }

            try {
                if ( timeout == 0 ) {
                    wait (0);
                } else {

                    long st = System.currentTimeMillis();

                    wait(waitTime);

                    if ( isEmpty() || isLocked ) {
                        long elapsed = System.currentTimeMillis() - st;
                        waitTime = waitTime - elapsed;

                        if ( waitTime <= 0 ) {
                            expired = true;
                        }
                    }
                }
            } catch (InterruptedException e) {
            }
        }

        //if still in lock mode or Connection.close() is called
        //don't even check if there is anything in the queue.

        if ( isClosed ) {
            return null;
        }

        //Set this flag so that Connection.stop() will be blocked.
        //This flag is set only when receive() was called and is going to
        //obtain the next available message.
        //NOTE: If used as ack temp queue, this flag has no meaning.
        receiveInProcess = true;

        return dequeue();
    }

    /**
     * This method is called before receive() is returned.  This will wake up
     * the thread from connection.stop(), if any, wait on
     * waitUntilReceiveDone().
     */
    protected synchronized void setReceiveInProcess(boolean state) {
        receiveInProcess = state;
        notifyAll();
    }

    /*
     * This method is called by Session.stop().  This method will block receive()
     * until Connection.start() is called or timeout.
     */
    protected synchronized void stop() {
        isLocked = true;
        waitUntilReceiveIsDone();

        if (debug) {
            Debug.println("receive queue 'stop' called ...");
        }
    }

    /**
     * when a failover occurred, we don't want to wait since the ack may be
     * blocked at the connection.  we simply set the flag and return.
     */
    protected synchronized void stopNoWait() {
        isLocked = true;
    }

    protected synchronized void start() {

        if ( isEmpty() == false ) {
            setIsLocked (false);
        } else {
            isLocked = false;
        }

        if (debug) {
            Debug.println("receive queue 'start' called ...");
        }
    }

    //when Connection.stop is called, each session call method to ensure no
    //messages will be delivered until Connection.start() is called.
    //
    //This method is not returned until SessionReader is locked and blocked.
    protected synchronized void waitUntilReceiveIsDone() {

        try {
            while ( isLocked && receiveInProcess == true ) {
                wait ();
            }
        } catch (InterruptedException e)  {
            ;
        }

    }

    public void dump (PrintStream ps) {
        ps.println ("------ ReceiveQueue dump ------");

        ps.println("isLocked: " + isLocked);
        ps.println("receiveInProcess: " + receiveInProcess );
        ps.println ("isClosed: " + isClosed);

        if ( size() > 0 ) {
            ps.println ("^^^^^^ receive queue super class dump ^^^^^^");
            super.dump(ps);
            ps.println ("^^^^^^ end receive queue super class dump ^^^^^^");
        }
    }

}
