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
 * @(#)GroupRunnable.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.group;

import java.io.*;
import java.util.Hashtable;
import com.sun.messaging.jmq.jmsserver.pool.BasicRunnable;
import com.sun.messaging.jmq.jmsserver.pool.ThreadPool;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;


public class GroupRunnable extends BasicRunnable
{

    SelectThread selthr = null;
    protected int ioevents = 0;
    Object threadUpdateLock = new Object();

    boolean paused = false;


    public GroupRunnable(int id, ThreadPool pool) {
        super(id, pool);
    }

    public Hashtable getDebugState() {
        Hashtable ht = super.getDebugState();
        if (selthr == null) {
            ht.put("selthr", "empty");
        } else {
            ht.put("selthr", selthr.getDebugState());
        }
        return ht;
    }

    public  void assignThread(SelectThread selthr, int events) 
        throws IOException
    {
        synchronized (threadUpdateLock) {
            if (this.selthr != null) {
                throw new IOException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    "Error trying to assign " + selthr + 
                     " to  group runnable " + this));
            }
            this.selthr = selthr;
            selthr.assign(this);
            this.ioevents = events;
            assigned(); // wakes us up
        }
    }


    public String toString() {
         return "GroupRun[id ="+ id + ", ioevents=" + ioevents 
                    + ", behavior=" +behaviorToString(behavior)
                    + ", selthr={" + selthr + "}, state=" 
                    + stateToString(state) + "]";
    }

    public void freeThread() {
        synchronized (threadUpdateLock) {
            if (selthr != null) {
                selthr.free(this);
                selthr = null;
                ioevents = 0;
                release();
            }
        }
    }

    public void suspend() {
        super.suspend();
        paused = true;
    }

    public void resume() {
        super.resume();
        synchronized (this) {
            paused = false;
            notify();
        }
    }



    protected void process() 
        throws IOException
    {
        boolean OK = false;

        synchronized (this) {
            while (paused) {
                try {
                    wait();
                } catch (Exception ex) {
                }
            }
        }

        // OK .. determine when to free
        Throwable err = null;
        try { // how to handle ???
            if (selthr != null)
                selthr.processThread();
            OK = true;
        } catch (NullPointerException ex) {
            // if we are shutting the thread down .. there are times
            // when selector may be set to null after the valid check
            // we really dont want to have to synchronized each access
            // SO ... if we get a null pointer exception .. just ignore
            // it and exit the thread ... its what we want to do anyway
            if (selthr != null && selthr.isValid())
                logger.logStack(Logger.WARNING,
                        BrokerResources.E_INTERNAL_BROKER_ERROR, 
                        selthr.getSelector().toString(), ex);
            err = ex;
        } catch (IOException ex) {
            // ignore, its OK
            OK = true;
            err = ex;
        } catch (Exception ex) {
            logger.logStack(Logger.WARNING,
                    BrokerResources.E_INTERNAL_BROKER_ERROR, ex);
            err = ex;
        } finally {
            if (!OK) {
                if (err != null)
                    logger.logStack(Logger.WARNING,"got an unexpected error " + err + " freeing thread " + this, err);
                freeThread();
            }
        }
    }
    
}


