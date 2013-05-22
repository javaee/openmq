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
 */ 

package com.sun.messaging.jmq.util.synchronizer;

/**
 *
 */
public class CloseInProgressSynchronizer 
{

     private static final long WAIT_INTERVAL = 15*1000L; //15 seconds

     //whether it's closed for operation or not
     private boolean closed = false;
     private Object closedLock = new Object();

     //number of operations in progress
     private int inprogressCount = 0;
     private Object inprogressLock = new Object();

     private Object logger = null;

     public CloseInProgressSynchronizer(Object logger) {
         this.logger = logger;
     }

     public void reset() {

         synchronized(closedLock) {
             closed = false; 
             closedLock.notifyAll();
         }
         synchronized (inprogressLock) {
            inprogressCount = 0;
            inprogressLock.notifyAll();
         }
     }

     /**
      * Set closed to true so that no new operation allowed
      */
     public void setClosedAndWait(CloseInProgressCallback cb, String waitlogmsg) {
         synchronized (closedLock) {
             closed = true;
         }

         if (cb != null) {
             cb.beforeWaitAfterSetClosed();
         }

        synchronized (inprogressLock) {
            inprogressLock.notifyAll();
            if (inprogressCount == 0) {
                return;
            }

            logInfo(waitlogmsg);

            long currtime = System.currentTimeMillis();
            long lastlogtime = currtime;

            while (inprogressCount > 0) {
                try {
                    if ((currtime - lastlogtime) >= WAIT_INTERVAL) {
                        logInfo(waitlogmsg);
                        lastlogtime = currtime;
                    }

                    inprogressLock.wait(WAIT_INTERVAL);

                    if (inprogressCount > 0) {
                        currtime = System.currentTimeMillis();
                    }
                } catch (Exception e) {}
            }
        }
    }

     /**
      * @param timeout in seconds, 0 means no timeout 
      *
      * @throws java.util.concurrent.TimeoutException if wait timed out
      * @throws InterruptedException if wait interrupted
      */
    public void setClosedAndWaitWithTimeout(CloseInProgressCallback cb,
                                            int timeout, String waitlogmsg)
                                            throws InterruptedException, 
                                            java.util.concurrent.TimeoutException {
        if (timeout <= 0) {
            setClosedAndWait(cb, waitlogmsg);
            return;
        }

        synchronized(closedLock) {
            closed = true;
        }

        if (cb != null) {
            cb.beforeWaitAfterSetClosed();
        }

        long maxwait = timeout * 1000L;
        long waittime = maxwait;

        synchronized (inprogressLock) {
            inprogressLock.notifyAll();
            if (inprogressCount == 0) {
                return;
            }

            logInfo(waitlogmsg);

            long currtime = System.currentTimeMillis();
            long precurrtime = currtime;
            long lastlogtime = 0L;
            long totalwaited = 0L;

            while (inprogressCount > 0 && (waittime > 0)) {
                try {
                    if ((currtime - lastlogtime) > WAIT_INTERVAL) {
                        logInfo(waitlogmsg);
                        lastlogtime = currtime;
                    }

                    inprogressLock.wait(waittime);
                    precurrtime = currtime;
                    currtime = System.currentTimeMillis();
                    totalwaited += ((currtime - precurrtime) > 0 ?
                                    (currtime - precurrtime):0);

                    if (inprogressCount > 0) {
                        waittime = maxwait - totalwaited;
                    }
                } catch (InterruptedException e) {
                    throw e;
                }
            }
            if (inprogressCount > 0 ) {
                throw new java.util.concurrent.TimeoutException("timeout");
            }
        }
    }


    /**
     * @throws IllegalAccessException if closed
     */
    public void checkClosedAndSetInProgress()
    throws IllegalAccessException {

        synchronized (closedLock) {
            if (closed) {
                throw new IllegalAccessException("closed");
            } 
            setInProgress(true);
        }
    }

    public void setInProgress(boolean flag) {
        synchronized (inprogressLock) {
            if (flag) {
                inprogressCount++;
            } else {
                inprogressCount--;
            }

            if (inprogressCount == 0) {
               inprogressLock.notify();
            }
        }
    }

    /**
     * @timeout timeout in seconds, 0 means no tmieout
     * @throws IllegalAccessException if closed
     * @throws java.util.concurrent.TimeoutException if timeout 
     * @throws InterruptedException if wait interrupted
     */
    public void checkClosedAndSetInProgressWithWait(int timeout, String waitlogmsg)
    throws IllegalStateException, InterruptedException,
    java.util.concurrent.TimeoutException {

        synchronized (closedLock) {
            if (closed) {
                throw new IllegalStateException("closed");
            } 
            setInProgressWithWait(timeout, waitlogmsg);
        }
    }

    /**
     * @param flag
     * @param timeout
     * @throws IllegalAccessException if closed
     * @throws java.util.concurrent.TimeoutException if timeout 
     * @throws InterruptedException if wait interrupted
     */
    private void setInProgressWithWait(int timeout, String waitlogmsg)
    throws IllegalStateException, InterruptedException,
    java.util.concurrent.TimeoutException {

        synchronized(inprogressLock) {
            if (inprogressCount == 0) {
                inprogressCount++;
                return;
            }

            long maxwait = timeout * 1000L;
            long waittime = ((maxwait <= 0 || maxwait > WAIT_INTERVAL) ?
                              WAIT_INTERVAL : maxwait);

            logInfo(waitlogmsg);
            long currtime = System.currentTimeMillis();
            long precurrtime = currtime;
            long lastlogtime = 0L;

            long totalwaited = 0L;

            while (inprogressCount > 0 && !closed && (waittime > 0)) {

                if ((currtime - lastlogtime) > WAIT_INTERVAL) {
                    logInfo(waitlogmsg);
                    lastlogtime = currtime;
                }

                inprogressLock.wait(waittime);
                precurrtime = currtime;
                currtime = System.currentTimeMillis();
                totalwaited += ((currtime - precurrtime) > 0 ?
                                (currtime - precurrtime):0);
                if (inprogressCount > 0) {
                    waittime = (maxwait <= 0 ? 
                                WAIT_INTERVAL : (maxwait - totalwaited));
                }
                if (waittime > WAIT_INTERVAL) {
                     waittime = WAIT_INTERVAL;
                }
            }
            if (closed) {
                throw new IllegalStateException("closed");
            }
            if (inprogressCount == 0) {
                inprogressCount++;
                return;
            }
            throw new java.util.concurrent.TimeoutException("timeout");
        }
    }

    public boolean isClosed() {
        synchronized (closedLock) {
            return closed;
        }
    }

    private void logInfo(String msg) {
        if (logger instanceof com.sun.messaging.jmq.util.LoggerWrapper) {
            ((com.sun.messaging.jmq.util.LoggerWrapper)logger).logInfo(msg, null);
        } else if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger)logger).log(
             java.util.logging.Level.INFO, msg);
        } else {
            System.out.println("INFO: "+msg);
        }
    }

}
