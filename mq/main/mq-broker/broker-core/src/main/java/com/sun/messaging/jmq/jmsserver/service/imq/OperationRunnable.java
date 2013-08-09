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
 * @(#)OperationRunnable.java	1.26 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import java.io.*;
import com.sun.messaging.jmq.jmsserver.pool.BasicRunnable;
import com.sun.messaging.jmq.jmsserver.pool.ThreadPool;
import com.sun.messaging.jmq.util.GoodbyeReason;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;


public class OperationRunnable extends BasicRunnable
{
    public static final int FOREVER = -1;
    public static final int UNTIL_DONE = 0;

    Operation op = null;
    int opcnt = 0;
    int OperationCount = 0;
    protected int ioevents = 0;
    protected boolean wait = false;
    Object opUpdateLock = new Object();


    public OperationRunnable(int id, ThreadPool pool, boolean wait) {
        super(id, pool);
        this.wait = wait;
    }

    public void clear() {
        synchronized (opUpdateLock) {
            op = null;
        } 
        release();
    }

    public void suspend() {
        super.suspend();
        if (op != null)
            op.suspend();
    }

    public void resume() {
        super.resume();
        if (op != null)
            op.resume();
    }




    public String toString() {
         return "OpRun[id ="+ id + ", ioevents=" + ioevents 
                    + ", behavior=" +behaviorToString(behavior)
                    + ", op={" + op + "}, state=" 
                    + stateToString(state) + "]";
    }

    public  void assignOperation(Operation newop, 
                                 int ioevents, 
                                 int how_long) 
         throws IllegalAccessException
    {
        synchronized (opUpdateLock) {
            if (op != null) {
                throw new IllegalAccessException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    "Error trying to assign " + newop 
                    + " to  assigned operation " + this));
            }

            this.op = newop;
            op.threadAssigned(this, ioevents); 
            OperationCount = how_long;
            opcnt = 0;
            this.ioevents = ioevents;
        }
        assigned(); // wakes us up
    }

    public void freeOperation() {
        synchronized (opUpdateLock) {

            if (op != null) {
                op.notifyRelease(this, ioevents);
                op = null;
            }
        }
        release();
    }

    public  void destroy(String reason) {
        if (op != null)
            op.destroy(true, GoodbyeReason.OTHER, reason);
        super.destroy();
    }




    protected void process() 
        throws IOException
    {
        Operation myop = null;
        synchronized (opUpdateLock) {
            myop = op;
        }
        
        if (myop == null || state < RUN_ASSIGNED) {
                return;
        }
        if (!myop.isValid()) {
              freeOperation();
              return;
        }
        if (state > RUN_CRITICAL)  {
            if (myop != null) {
                if (myop.isValid())
                    myop.destroy(false, GoodbyeReason.CON_FATAL_ERROR,
                         "invalid operation");
                freeOperation();
            }
            throw new IOException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                   "Exiting"));
        }

        // OK .. determine when to free
        try {
            boolean done = myop.process(ioevents, wait);
            switch (OperationCount) {
                case FOREVER:
                    return;
                case UNTIL_DONE:
                {
                    if (done) {
                        freeOperation();
                    }
                    return;
                }
                default:
                {
                    opcnt ++;
                    if (opcnt >= OperationCount || done) {
                        freeOperation();
                    }
                    return;
                }
            }
        } catch (IOException ex) {
            // OK .. destroy the operation ... its gone
            if (myop != null) {
                if (getDEBUG()) {
                    logger.logStack(Logger.DEBUG,
                       "Debug: Connection going away", ex);
                }
                if (ex instanceof EOFException) {
                    myop.destroy(false, GoodbyeReason.CLIENT_CLOSED,
                        Globals.getBrokerResources().getKString(
                        BrokerResources.M_CONNECTION_CLOSE));
                } else {
                    myop.destroy(false, GoodbyeReason.CON_FATAL_ERROR,
                        ex.toString());
                }
            }
            freeOperation();
        }
    }
    
}


