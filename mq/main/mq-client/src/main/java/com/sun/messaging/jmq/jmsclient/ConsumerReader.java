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
 * @(#)ConsumerReader.java	1.17 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import com.sun.messaging.jmq.io.*;
import java.io.*;
import java.util.logging.Level;
//import java.util.Enumeration;


public abstract class ConsumerReader implements Runnable, Traceable {
    protected Thread sessionThread = null;
    protected ProtocolHandler protocolHandler = null;
    protected ConnectionImpl connection = null;
    protected SessionQueue sessionQueue = null;

    protected boolean isSuspended = false;
    protected boolean isAlive = false;
    protected boolean isPaused = false;

    //for thread naming purpose
    protected static int nextReaderID = 0;
    protected int readerID = 0;

    protected static final String imqConsumerReader = "imqConsumerReader-";

     //the maximum number of messages in the queue not processed.
    //protected int maxQueueSize = 100;
    //protected int minQueueSize = 10;

    //set this to true if wanted to have max and min queue size checked.
    //protected boolean protectMode = false;

    //message suspended state
    //used for checkQueueSize
    //private boolean messageSuspended = false;

    //the message that is delivering/delivered to the message consumer
    //MessageImpl currentMessage = null;

    //debug flag
    protected boolean debug = Debug.debug;

    //dupsOkPerf -- this flag is set by SessionReader sub class.
    protected long timeout = 0;

    public ConsumerReader (ConnectionImpl connection,
                           SessionQueue readQueue) {
        this.connection = connection;
        this.sessionQueue = readQueue;

        readerID = getNextReaderID();

        init();
    }

    protected static synchronized int getNextReaderID() {
        return nextReaderID ++;
    }

    //dupsOkPerf
    protected void setTimeout (long timeout) {
        this.timeout = timeout;
    }

    public void
    init() {
        protocolHandler = connection.getProtocolHandler();

        //maxQueueSize = connection.getMaxQueueSize();
        //minQueueSize = connection.getMinQueueSize();
        //protectMode = connection.getProtectMode();

        /*if ( debug ) {
            Debug.println ("maxqsize: " +  maxQueueSize);
            Debug.println ("minqsize: " + minQueueSize);
            Debug.println ("protect mode: " + protectMode);
        } */

    }

    public synchronized void
    start() {
        if ( sessionThread == null ) {

            if (debug) {
                Debug.println("starting new sessionThread ...");
            }

            sessionThread = new Thread ( this );
            if (connection.hasDaemonThreads()) {
                sessionThread.setDaemon(true);
            }
            //set thread name
            //sessionThread.setName(imqConsumerReader + connection.getConnectionID() + "-" + readerID);
            sessionThread.setName(imqConsumerReader + connection.getLocalID() + "-" + connection.getConnectionID() + "-" + readerID);
            setIsAlive ( true );
            setIsSuspended ( false );
            sessionThread.start();
        } else {

            if (debug) {
                Debug.println("sessionThread was started already ...");
            }

            resume ();
        }
    }

    public void run() {
        //read loop ...
        ReadOnlyPacket packet = null;
        Thread currentThread = Thread.currentThread();

        if ( debug) {
            Debug.println("**** Consumer Reader wait timeout: " + timeout);
        }

        while (sessionThread == currentThread) {
            try {

                //This call blocks until packet comes in.
                //or queue is locked
                //dupsOkPerf
                packet = (ReadOnlyPacket) sessionQueue.dequeueWait(timeout);

                //check if connection is still ok.  For client ack,
                //we want to find out if connection is broken asap.
                //If connection should break, the previous one is the last
                //message delivered.
                //For auto ack and dups ok ack, this has been taken care
                //of already -- exception will be caught below.
                if ( connection.isBroken() ) {
                    sessionThread = null;
                    sessionQueue.close();
                } else {
                    //connection is ok ... so far
                    if (packet != null) {

                        /*if ( protectMode ) {
                            checkQueueSize ();
                        } */

                        if ( getIsAlive() ) {
                            if ( debug ) {
                                Debug.println(this);
                            }

                            deliver(packet);

                        } //isAlive
                    } else { //packet == null
                        deliver();
                    }
                }

            } catch ( Exception e ) {
                //we check if the connection is broken,
                //if yes, we exit the loop.
                //If the exception listener was set, it would be
                //called by ReadChannel.  We just exit gracefully
                //here.
                if ( connection.isBroken() ) {
                    sessionThread = null;
                    sessionQueue.close();
                } else if ( connection.getRecoverInProcess() ) {
                    /* we should stop message delivery to listener
                     * until recovered.
                     * A simple way is just to clean up sesison
                     * queue. We don't want to continue message
                     * delivery when there is a problem in the
                     * connection.
                     */
                     sessionQueue.clear();
                } else {
                    /**
                     * For anything else, we dump exception stack trace.
                     */
                    //Debug.printStackTrace(e);
                	ExceptionHandler.rootLogger.log(Level.WARNING, e.getMessage(), e);
                }
            } catch (Error err) {
                connection.readChannel.setFatalError(err);
                return;
            }

        } //while

        if (debug) {
            Debug.println("sessionReader closed ...");
        }

    }

    /**
     * deliver a message packet to consumer.
     *
     * @param packet the arrived message packet
     *
     * @exception IOException
     * @exception JMSException
     */
    abstract protected void deliver(ReadOnlyPacket packet)
                            throws IOException, JMSException;

    /**
     * The reader thread is waken up without a packet
     *
     * @exception IOException
     * @exception JMSException
     */
    abstract protected void deliver()
                            throws IOException, JMSException;

    /**
     * Stop the session reader.
     */
    protected synchronized void
    stop() {

        if (debug) {
            Debug.println("session reader stopped ...");
        }

        setIsSuspended ( true );
    }

    protected synchronized void
    suspend() {
        if (debug) {
            Debug.println("session reader suspended ...");
        }
        setIsSuspended ( true );
    }

    /**
     * Resume this session reader
     */
    protected synchronized void
    resume () {
        if (debug) {
            Debug.println("session reader resumed ...");
        }
        setIsSuspended ( false );
        notifyAll();
    }

    /**
     * Called by SessionImpl.close().
     *
     */
    protected synchronized void
    close() {
        setIsAlive ( false );
        setIsSuspended ( false );
        sessionThread = null;
        sessionQueue.close();
    }

    /**
     * Get the state of this session reader.
     *
     * @return true if it is suspended.  Otherwise, returns false.
     */
    protected synchronized boolean
    getIsSuspended() {
        return isSuspended;
    }

  /**
   * the state of sessionThread.  If sessionThread is alive, it was started
   * and not died yet.
   *
   */
    protected synchronized boolean
    getIsAlive() {
        return isAlive;
    }

    protected synchronized void
    setIsSuspended ( boolean state ) {
        isSuspended = state;
    }

    protected synchronized void
    setIsAlive ( boolean state ) {
        isAlive = state;
    }

    protected synchronized void setIsPaused (boolean state) {
        isPaused = state;
    }

    protected synchronized boolean getIsPaused() {
        return isPaused;
    }

    //This has been disabled.
    //Flow control is now handled in FlowControl class
   /* protected void checkQueueSize() throws JMSException {

        if ( sessionQueue.size() > maxQueueSize ) {
            if ( messageSuspended == false ) {
                if (debug) {
                    Debug.println("suspend connection cause queue is full ...");
                    Debug.println("queue size = " + sessionQueue.size());
                }

                protocolHandler.suspendMessageDelivery();
                messageSuspended = true;
           }
        } else if ( (sessionQueue.size() <= minQueueSize) ) {
            if ( messageSuspended == true ) {
                protocolHandler.resumeMessageDelivery();
                messageSuspended = false;

                if (debug) {
                    Debug.println("resume connection cause queue is normal ...");
                    Debug.println("queue size = " + sessionQueue.size());
                }
            }
        }
    }*/

    public void dump (PrintStream ps) {
        ps.println ("is alive: " + isAlive );
        ps.println ("is suspended: " + isSuspended);
    }
}
