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
 * @(#)MQTimer.java	1.12 06/29/07
 */ 

package com.sun.messaging.jmq.util.timer;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.sun.messaging.jmq.resources.*;
import com.sun.messaging.jmq.util.LoggerWrapper;

public class MQTimer extends java.util.Timer {

    private static boolean DEBUG = false;

    private static SharedResources myrb = SharedResources.getResources();
    private static LoggerWrapper logger = null;

    public static void setLogger(LoggerWrapper l) {
        logger = l;
    }

    /**
     * This object causes the timer's task execution thread to exit
     * gracefully when there are no live references to the Timer object and no
     * tasks in the timer queue.  It is used in preference to a finalizer on
     * Timer as such a finalizer would be susceptible to a subclass's
     * finalizer forgetting to call it.
     */
    private Object mqTimerObject = new Object() {
        protected void finalize() throws Throwable {
            if (DEBUG && logger != null) {
                Exception ex = new RuntimeException("MQTimer.mqtimerObject: finalize");
                ex.fillInStackTrace();
                logger.logInfo("Internal Error: timer canceled ", ex);
            }
        }
    };

    public MQTimer() {
        this(false);
    }

    public MQTimer(boolean isDaemon) {
        super("MQTimer-Thread", isDaemon);
    }

    public void initUncaughtExceptionHandler() {
        TimerTask uehtask = new TimerTask() {
           public void run() {
               Thread thr = Thread.currentThread();
               Thread.UncaughtExceptionHandler ueh = thr.getUncaughtExceptionHandler();
               try {
                   thr.setUncaughtExceptionHandler(new MQTimerUncaughtExceptionHandler(ueh));
               } catch (Exception e) {
                   if (logger != null) {
                       logger.logWarn(myrb.getKString(myrb.W_SET_UNCAUGHT_EX_HANDLER_FAIL,
                                      getClass().getName()), null);
                   }
               }
               cancel();
               
           }
        };
        try {
            schedule(uehtask, new Date());
        } catch (Exception ex) {
            if (logger != null) {
                logger.logWarn(myrb.getKString(myrb.W_SCHEDULE_UNCAUGHT_EX_HANDLER_TASK_FAIL,
                               ex.getMessage()), null);
            }
        }
    } 

    class MQTimerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler { 
        Thread.UncaughtExceptionHandler parent = null;

        public MQTimerUncaughtExceptionHandler(Thread.UncaughtExceptionHandler parent) {
            this.parent = parent;
        }
    
        public void uncaughtException(Thread t, Throwable e) { 
            if (logger != null) {
                logger.logError(myrb.getKString(myrb.E_UNCAUGHT_EX_IN_THREAD, 
                                e.getMessage(), t.getName()), e);
            }
            parent.uncaughtException(t, e);
        }
    }

    public void cancel() {
        super.cancel();
        if (logger != null && DEBUG) {
            Exception ex = new RuntimeException("MQTimer: cancel");
            ex.fillInStackTrace();
            logger.logInfo("Internal Error: timer canceled ", ex);
        }
    }
}
