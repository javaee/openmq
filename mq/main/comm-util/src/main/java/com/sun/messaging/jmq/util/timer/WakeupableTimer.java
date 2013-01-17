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

package com.sun.messaging.jmq.util.timer;

public class WakeupableTimer implements Runnable 
{
    private static boolean DEBUG = false;

    private String name = null;
    private long mynexttime = 0L;
    private long nexttime = 0L;
    private long repeatinterval = 0;
    private Thread thr = null;
    private boolean valid = true;
    private boolean wakeup = false;
    private String startString = null;
    private String exitString = null;
    private TimerEventHandler handler = null;


    /**
     * @param delaytime initial delay in millisecs
     * @param repeatInterval wait time in milliseconds to repeat the task;
     *        if 0, wait for wakeup notifications (see also TimerEventHandler.runTask)
     */
    public WakeupableTimer(String name, TimerEventHandler handler, 
                           long delaytime, long repeatInterval,
                           String startString, String exitString) {
        this.name = name;
        this.mynexttime = delaytime + System.currentTimeMillis();
        this.repeatinterval = repeatInterval;
        this.startString = startString;
        this.exitString = exitString;
        this.handler = handler;

        thr = new Thread(this, name);
        thr.start();
    }

    public boolean isTimerThread(Thread t) {
        if (thr == null) {
            return false;
        }
        return (t == thr);
    }

    public synchronized void wakeup() {
        wakeup = true;
        notify();
    }

    /**
     * @param time next time to run task
     */
    public synchronized void wakeup(long time) {
        nexttime = time;
        wakeup = true;
        notify();
    }

    public void cancel() {
        valid = false;
        wakeup();
        thr.interrupt();
    }

    public void run() {
        try {

        handler.handleLogInfo(startString);

        long time = System.currentTimeMillis();
        long waittime = mynexttime - time;
        if (waittime < 0L) {
            waittime = 0L;
        }
        boolean nowaitOn0 = true;
        while (valid) {
            try {

            synchronized(this) {
                while (valid && !wakeup &&
                       !(waittime == 0L && nowaitOn0)) {
                    if (DEBUG) {
                        handler.handleLogInfo(name+" run(): before wait("+waittime+"), valid="+
                            valid+ ", wakeup="+wakeup+", nowaitOn0="+nowaitOn0);
                    }
                    if (nowaitOn0) {
                        nowaitOn0 = false;
                    }
                    try {
                        this.wait(waittime);
                    } catch (InterruptedException ex) {
                    }
                    if (valid && !wakeup && waittime != 0L) {
                        time = System.currentTimeMillis();
                        waittime = mynexttime - time;
                        if (waittime <= 0L) {
                            waittime = 0L;
                            if (repeatinterval > 0L) {
                                waittime = repeatinterval;                              
                            } 
                            break;
                        }
                    }
                }
                if (!valid) {
                    break;
                }
                wakeup = false;

            } //synchronized

            if (DEBUG) {
                handler.handleLogInfo(name+" runTask "+handler.getClass().getName());
            }

            boolean asrequested = false; 
            mynexttime = handler.runTask();
            if (mynexttime > 0L) {
                nowaitOn0 = true;
                asrequested = true;
            }
            if (DEBUG) {
                handler.handleLogInfo(name+" completed run "+
                    handler.getClass().getName()+" with return "+mynexttime);
            }
            time = System.currentTimeMillis();
            if (mynexttime == 0L) {
                mynexttime = time + repeatinterval;
            }
            synchronized(this) {
                if (DEBUG) {
                    handler.handleLogInfo(name+" run() after runTask(), nexttime="+
                                 nexttime+", mynexttime="+mynexttime+", time="+time);
                }
                if (nexttime > 0L && nexttime < mynexttime) {
                    mynexttime = nexttime;
                    nowaitOn0 = true;
                    asrequested = true;
                }
                nexttime = 0L;
            }
            waittime = mynexttime - time;
            if (waittime < 0L) {
                waittime = 0L;
            }
            if (waittime == 0L && !asrequested) {
                nowaitOn0 = false;
            }

            } catch (Throwable e) {
            handler.handleLogWarn(e.getMessage(), e);
            if (e instanceof OutOfMemoryError) {
                handler.handleOOMError(e);
            }
            }
        } //while

        handler.handleLogInfo(exitString);

        } catch (Throwable t) {
        handler.handleLogError(exitString, t);
        handler.handleTimerExit(t);
        }
    }
}
