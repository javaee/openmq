/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.util.Properties;

import com.sun.messaging.ums.common.Constants;
import com.sun.messaging.ums.resources.UMSResources;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheSweeper extends Thread {

    //private JMSCache cache = null;
    //private ClientPool cache = null;
    //sweep thread check this flag
    
    private boolean isRunning = true;
    //cache object TTL, 7 minutes
    private static final long CACHE_TTL = 7 * 60 * 1000;
    
    //sweep interval, default 2 minutes
    private long sweepInterval = 2 * 60 * 1000;
    
    private long cacheDuration = CACHE_TTL;
    
    private Logger logger = null;
    
    private static final String myName = "CacheSweper";
    
    private Properties props = null;
    
    //private CachedConnectionPool ccpool = null;
    
    //private String provider = null;

    private ArrayList<ClientPool> cacheList = new ArrayList<ClientPool>();
    
    public CacheSweeper(Properties p) {
        
        
        logger = Logger.getLogger(this.getClass().getName());
        
        this.props = p;

        super.setName(myName);

        init();
    }

    private void init() {

        try {

            String tmp = props.getProperty(Constants.CACHE_DURATION);
            if (tmp != null) {
                this.cacheDuration = Long.parseLong(tmp);
            }

            tmp = props.getProperty(Constants.SWEEP_INTERVAL);
            if (tmp != null) {
                this.sweepInterval = Long.parseLong(tmp);
            }

            //this.ccpool = cache.getConnectionPool();

            //logger.info ("CacheSweeper created ...., sweep interval=" + this.sweepInterval + ", cache duration = " + this.cacheDuration);

            String msg = UMSResources.getResources().getKString(UMSResources.UMS_SWEEPER_INIT, this.sweepInterval, this.cacheDuration);
            logger.info(msg);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    public void addClientPool (ClientPool cache) {
        this.cacheList.add(cache);
    }
    
    public void removeClientPool (ClientPool cache) {
        this.cacheList.remove(cache);
    }

    /**
     * check clientId time stamps.  
     * close/remove session/producer/consumer if unused for
     * more than 7 minutes.
     */
    private void sweep() {
        
        int size = this.cacheList.size();
        
        for (int index = 0; index < size; index++) {
            
            ClientPool cache = this.cacheList.get(index);
            CachedConnectionPool ccpool = cache.getConnectionPool();
            
            try {
                
                if (UMSServiceImpl.getDebug()) {
                    logger.info ("CacheSweeper sweeping client cache ...." + cache);
                }
                
                cache.sweep(cacheDuration);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }

            try {
                
                if (UMSServiceImpl.getDebug()) {
                    logger.info ("CacheSweeper sweeping connection cache ...." + ccpool);
                }
                
                ccpool.sweep(cacheDuration);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * wake up every 30 secs and close/remove unused 
     * sessions/producers/consumers
     */
    public void run() {

        while (isRunning = true) {

            try {

                //this.debugLog("CacheSweeper running ...." + this);

                synchronized (this) {
                    wait(sweepInterval);
                    sweep();
                }

            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * close sweep thread
     */
    public void close() {
        synchronized (this) {
            this.isRunning = false;
            notifyAll();
        }
    }

    public String toString() {
        return this.getClass().getName() + ", sweep interval=" + this.sweepInterval + ", cacheDuration=" + this.cacheDuration + ", is running=" + this.isRunning;
    }
}
