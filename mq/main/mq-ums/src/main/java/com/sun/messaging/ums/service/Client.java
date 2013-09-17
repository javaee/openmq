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

package com.sun.messaging.ums.service;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class Client {

    private String sid = null;
    private CachedConnection cc = null;
    private Session session = null;
    private MessageProducer producer = null;
    private MessageConsumer consumer = null;
    private long timestamp = 0;
    
    private Logger logger = UMSServiceImpl.logger;
    
    private Object lock;
    private boolean sweeped = false;
    //XXX use this for sweep
    private boolean inuse = false;
    private CachedConnectionPool ccpool = null;
    
    //do not cache me if set to true. default set to false.
    private boolean noCache = false;
    
    private boolean transacted = false;
    
    /**
     * the destination name that current consumer is associated with.
     * Each client consumer can only associate with one destination at a 
     * time. 
     */
    private String consumerOnDestName = null;

    public Client(String sid, CachedConnectionPool ccPool, boolean transacted)
            throws JMSException {

        this.sid = sid;

        this.ccpool = ccPool;
        
        this.transacted = transacted;

        this.cc = ccPool.getCachedConnection();

        cc.add(this);
        
        this.lock = new Object();

        if (UMSServiceImpl.debug) {
            logger.info ("client created: " + sid + ", transacted=" + transacted);
        }
    }

    public String getId() {
        return this.sid;
    }
    
    public Object getLock() {
        return this.lock;
    }
    
    /**
     * set to true if no cache
     * @param flag
     */
    public void setNoCache (boolean flag) {
        this.noCache = flag;
    }
    
    /**
     * get if no cache mode is true
     * @return
     */
    public boolean getNoCache() {
        return this.noCache;
    }
    
    public void setTransacted (boolean flag) {
        this.transacted = flag;
    }
    
    public boolean getTransacted () {
        return this.transacted;
    }

    public synchronized Session getSession() throws JMSException {

        if (session == null) {
            session = cc.getConnection().createSession(transacted, Session.AUTO_ACKNOWLEDGE);
        }

        this.setTimestamp();

        return session;
    }

    public synchronized MessageProducer getProducer() throws JMSException {

        if (producer == null) {
            getSession();
            producer = session.createProducer(null);
        }

        return producer;
    }

    public synchronized MessageConsumer getConsumer(boolean isTopic, String destName) throws JMSException {

        if (consumer == null) {
            this.createConsumer(isTopic, destName);
        } else {
            
            if (UMSServiceImpl.debug) {
                logger.info("consumer in cache for clientId ... " + sid + ", on dest: " + destName);
            }
            
            if (destName.equals(this.consumerOnDestName) == false) {
            //app tries ti receive on diff dest.
                this.recreateConsumer(isTopic, destName);
            }
        }

        return consumer;
    }

    private synchronized void recreateConsumer(boolean isTopic, String destName) throws JMSException {
        this.consumer.close();
        this.createConsumer (isTopic, destName);
    }

    private synchronized void createConsumer(boolean isTopic, String destName) throws JMSException {
        
        //set current associated dest name
        this.consumerOnDestName = destName;

        Destination dest = null;

        getSession();

        if (UMSServiceImpl.debug) {
            logger.info ("got session ..." + session.toString());
        }
        
        if (isTopic) {
            //debugLog("Creating topic: " + destName);
            dest = session.createTopic(destName);
        } else {
            //debugLog("Creating queue: " + destName);
            dest = session.createQueue(destName);
        }

        //debugLog("Creating consumer on dest: " + destName);
        consumer = session.createConsumer(dest);

        if (UMSServiceImpl.debug) {
            logger.info ("created consumer for clientId=" + sid + ", dest=" + dest + ", isTopic=" + isTopic);
        }
    }

    public synchronized void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
        this.sweeped = false;
    }

    public synchronized long getTimestamp() {
        return this.timestamp;
    }

    public synchronized boolean getSweeped() {
        return this.sweeped;
    }

    public synchronized void setSweeped(boolean flag) {
        this.sweeped = flag;
    }

    public synchronized void setInuse(boolean flag) {
        this.inuse = flag;
    }

    public synchronized boolean getInUse() {
        return this.inuse;
    }

    /**
     * XXX close
     */
    public synchronized void close() {

        try {
            
            //remove uuid from authenticator
            //ccpool.removeSid(this.clientId);
            
            //decrease semaphore
            ccpool.releaseConnection(cc);
            
            //remove myself from cc table
            this.cc.remove(this);
            
            //close session
            if (session != null) {
                this.session.close();
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    public String toString() {
        return "Name=" + this.getClass().getName() + ", ClientId=" + this.sid + ", inuse=" + this.inuse + ", timestamp=" + this.timestamp;
    }
}
