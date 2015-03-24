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
 * @(#)HeartbeatImpl.java	1.17 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.heartbeat;

import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.PortUnreachableException;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.MQThreadGroup;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.multibroker.heartbeat.spi.Heartbeat;
import com.sun.messaging.jmq.jmsserver.multibroker.heartbeat.spi.HeartbeatCallback;

/**
 */
public class HeartbeatImpl implements Heartbeat {
    private static boolean DEBUG = false;

    private Logger logger = Globals.getLogger();
	private BrokerResources br = Globals.getBrokerResources();

    public static final int DEFAULT_HEARTBEAT_INTERVAL = 2;
    public static final int DEFAULT_TIMEOUT_THRESHOLD = 3;

    private int heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL; //in seconds
    private int timeoutThreshold = DEFAULT_TIMEOUT_THRESHOLD * heartbeatInterval;

    private HeartbeatCallback cb = null;
    private InetSocketAddress bindEndpoint = null;

    private MQThreadGroup threadGroup = null;
    private Sender sender = null;
    private Receiver receiver = null;

    private boolean started = false;
    
    private Map endpoints = Collections.synchronizedMap(new LinkedHashMap());

    public
    HeartbeatImpl() {};

    public String
    getName() {
        return null;
    }

    public String
    getProtocol() {
        return "udp";
    }

    /**
     * Initialization
     *
     * @param cb The HeartbeatCallback
     *
     * @throws IOException if failed to initialize
     */
    public void
    init(InetSocketAddress endpoint,  HeartbeatCallback cb) throws IOException {
        this.threadGroup =  new MQThreadGroup("Heartbeat", logger,
                            br.getKString(br.W_UNCAUGHT_EXCEPTION_IN_THREAD));
        this.bindEndpoint = endpoint;
        this.cb =cb;
        receiver = new Receiver();
        sender = new Sender();
        started = false;
    }


    /**
     * Stop  
     *
     * @throws IOException if failed to stop
     */
    public synchronized void
    stop() throws IOException {
        sender.close();
        receiver.close();
    }


    /**
     *
     * @param addr The remote IP address
     * @param port The remote port number
     * @param privData An opaque data associated with this endpoint
     * @param dataLength The expected data length 
     *
     * @throws IOException
     */
    public synchronized void
    addEndpoint(Object key, InetSocketAddress endpoint, int dataLength) throws IOException {
        endpoints.put(key, endpoint);
        try {
        sender.add(endpoint);
        } catch (SocketException e) {
        endpoints.remove(endpoint);
        throw e;
        }
        receiver.add(endpoint, dataLength);
        if (!started) {
            receiver.start(); 
            sender.start();
            started = true;
        } 
    }


    /**
     */
    public synchronized boolean 
    removeEndpoint(Object key, InetSocketAddress endpoint) throws IOException {
        endpoints.remove(key);
        if (!endpoints.containsValue(endpoint)) {
            sender.remove(endpoint);
            return true;
        }
        return false; 
    }


    /**
     *
     * @param 
     */
    public InetSocketAddress 
    getBindEndpoint() {
        return bindEndpoint;
    }

    /**
     *
     * @param interval The inteval between each heartbeat in seconds
     */
    public void
    setHeartbeatInterval(int interval) {
        heartbeatInterval = interval;
    }


    /**
     *
     * @return The heartbeat interval
     */
    public int 
    getHeartbeatInterval() {
        return heartbeatInterval;
    }


    /**
     * Timeout when heartbeat message not received for period of threshold*interval
     * from a remote endpoint
     *
     * @param threshold in terms of number of times of heartbeat interval
     */
    public void
    setTimeoutThreshold(int threshold) {
        timeoutThreshold = threshold;
    }


    /**
     *
     * @return The heartbeat timeout threshold
     */
    public int
    getTimeoutThreshold() {
        return timeoutThreshold;
    }


    class Receiver extends Thread {
     
        DatagramSocket ds = null;
        DatagramPacket dp = null;
        boolean refreshSize = false;
        boolean closed =  false;
        int bufSize = 0;

        Vector senderAddrs = new Vector();

        Receiver() throws IOException {
            super(threadGroup, "Heartbeat Receiver");
            setPriority(Thread.MAX_PRIORITY);
            setDaemon(true);
            ds = new DatagramSocket(bindEndpoint);
            logger.log(Logger.INFO, br.getKString(br.I_CLUSTER_HB_BIND,
                       bindEndpoint.getAddress()+":"+bindEndpoint.getPort()));
	    }

        void add(InetSocketAddress endpoint, int dataLength) {
            if (dataLength > bufSize) {
                bufSize = dataLength;
                refreshSize = true;
            }
            senderAddrs.add(endpoint.getAddress());
        }

        public void run() {

            String exitmsg = br.getKString(br.M_THREAD_EXITING, super.getName());
            try  {

            while (!closed) {

                try {
                    if ((ds == null || ds.isClosed()) && !closed) {
                        ds = new DatagramSocket(bindEndpoint);
                    }
                    if (closed) {
                        ds.close();
                        continue;
                    }
               
                    if (dp == null || refreshSize) {
                        byte[] buf = new byte[bufSize];
                        if (dp == null) dp = new DatagramPacket(buf, buf.length);
                        dp.setData(buf);
                    }
                    logger.log(logger.DEBUGHIGH, "Heartbeat receiving ..");

                    ds.receive(dp);
                    logger.log(logger.DEBUGHIGH, "Heartbeat received heartbeat " +
                               " from " +dp.getSocketAddress()+":"+ dp.getPort());

                    InetSocketAddress sender = (InetSocketAddress)dp.getSocketAddress();
                    if (!senderAddrs.contains(sender.getAddress())) {
                        logger.log(logger.WARNING, br.getKString(
                                   br.W_CLUSTER_HB_IGNORE_UNKNOWN_SENDER, sender)); 
                        continue;
                    }
                    try {
                    cb.heartbeatReceived(sender, dp.getData());
                    } catch (IOException e) {
                    if (DEBUG) {
                    logger.log(logger.INFO, e.getMessage() + " from "+ 
                                    dp.getSocketAddress()+":"+ dp.getPort()+". Ignore"); 
                    }
                    }
                }
                catch (Throwable t) {
                    if (!closed) {
                    logger.logStack(logger.WARNING, super.getName() +": "+ t.getMessage(), t);
                    }
                    continue;
                }
            } //while

            } finally {

            if (ds != null) ds.close();
            if (!closed) {
            logger.log(logger.WARNING, exitmsg);
            }

            }
        }

        void close() {
            closed = true;
            interrupt();
            if (ds != null) ds.close();
        }

    }

    class Sender extends Thread {

        Map dss = Collections.synchronizedMap(new LinkedHashMap()); 
        DatagramPacket dp = null;
        boolean closed = false;

        Sender() throws IOException {
            super(threadGroup, "Heartbeat Sender");
            setPriority(Thread.MAX_PRIORITY);
            setDaemon(true);
            byte[] buf = new byte[1]; 
            dp = new DatagramPacket(buf, buf.length);
	    }



        /**
         */
        void add(InetSocketAddress endpoint) throws SocketException {
            if (closed) {
                logger.log(Logger.DEBUG, "Heartbeat.Sender.addEnpoint: closed, ignore");
                return;
            }
            DatagramSocket ds = new DatagramSocket(0, bindEndpoint.getAddress());
			ds.connect(endpoint.getAddress(), endpoint.getPort());
            dss.put(endpoint, ds);
        }

        void remove(InetSocketAddress endpoint) {
            DatagramSocket ds = (DatagramSocket)dss.remove(endpoint);
            if (ds !=  null) {
                ds.disconnect();
                ds.close();
            }
        }

        public void run() {
            String exitmsg = br.getKString(br.M_THREAD_EXITING, super.getName());
            try  {

	        while (!closed) {
                try {

                Object[] keys = null;
                synchronized(endpoints) {
                    Set ks = endpoints.keySet();
                    keys = (Object[])ks.toArray(new Object[ks.size()]);
                }
                for (int i = 0; (i < keys.length) && !closed; i++) {

                    try {

                    InetSocketAddress endpoint = (InetSocketAddress)endpoints.get(keys[i]);
                    byte[] data = cb.getBytesToSend(keys[i], endpoint);
                    if (data == null) {
                       logger.log(Logger.WARNING, 
                              "Heartbeat.Sender: no data send to "+ endpoint+ " for "+keys[i]);
                       continue;
                    }
                    dp.setSocketAddress(endpoint);
                    dp.setData(data);
                    DatagramSocket ds = (DatagramSocket)dss.get(endpoint);
                    if (ds == null) {
                       logger.log(Logger.DEBUG, "Heartbeat.Sender: Endpoint "+ endpoint+ " for "+keys[i]
                                                 +" removed. no send");
                       continue;
                    }
                    if (ds.isClosed() && !closed) {
                        logger.log(Logger.DEBUG, "Heartbeat.Sender: Endoint "+ endpoint+" for "+keys[i]
                                                 +" removed. no send");
                        ds = new DatagramSocket(0, bindEndpoint.getAddress());
                        ds.connect(endpoint);
                    }
                    if (!ds.isConnected() && !closed) {
                        ds.connect(endpoint);  //XXX reconnect exception ??
                    }
                    if (closed) continue;
		            try {
                        ds.send(dp);
		            } catch (IOException e) {
                        cb.heartbeatIOException(keys[i], endpoint, e);  
                    }

                    } catch (Exception e) {
                       logger.logStack(Logger.WARNING, e.getMessage(), e);
                    }
		        }
                if (closed) continue;
                try {
                Thread.sleep(heartbeatInterval*1000L);
                } catch (InterruptedException e) {}

            } catch (Throwable t) {
            logger.logStack(logger.WARNING, super.getName() +": "+ t.getMessage(), t);
            continue;
            }
	        } //while

            } finally {
            if (!closed) {
            logger.log(logger.WARNING, exitmsg);
            }
            }
	    }
     
        void close() { 
            closed = true;
            interrupt();
            Set ks = null; 
            InetSocketAddress[] iaddrs = null;
            synchronized(dss) {
                ks = dss.keySet();
                iaddrs = (InetSocketAddress[])ks.toArray(new InetSocketAddress[ks.size()]);
            }
            for (int i = 0; i < iaddrs.length; i++) {
                remove(iaddrs[i]);
            }
        }
    }
}

