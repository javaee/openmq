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

import com.sun.messaging.ums.factory.UMSConnectionFactory;
import com.sun.messaging.ums.common.Constants;
//import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;

/**
 *
 * @author chiaming
 */
public class Authenticator {
    
    private Logger logger = UMSServiceImpl.logger;
    
    private UMSConnectionFactory connFactory = null;
    
    private Properties props = null;
    
    private boolean shouldAuthenticate = true;
    
    private boolean base64encoding = false;
    
    /**
     * contains authenticated user/password
     */
    //private transient Hashtable <String, String> authTable = 
    //        new Hashtable <String, String>();
    
    /**
     * contains a list of current valid clients (uuids)
     */
    //private transient Vector <String> clients = new Vector<String>();
    
    
    private SecuredSid securedSid = null;
    
    /**
     * The constructor is used to validate with the specified JMS provider.
     *  
     * @param umsConnectionFactory
     */
    public Authenticator(UMSConnectionFactory umsConnectionFactory, Properties props) throws JMSException {
        this.connFactory = umsConnectionFactory;
        
        this.props = props;
        
        String tmp = props.getProperty(Constants.JMS_AUTHENTICATE, Constants.JMS_AUTHENTICATE_DEFAULT_VALUE);
        
        this.shouldAuthenticate = Boolean.parseBoolean(tmp);
        
        tmp = props.getProperty(Constants.BASIC_AUTH_TYPE, Constants.BASIC_AUTH_TYPE_DEFAULT_VALUE);
        this.base64encoding = Boolean.parseBoolean(tmp);
        
        this.securedSid = new SecuredSid();
    }
    
    /**
     * Authenticate the provided user/password.
     * 
     * It is intentional to NOT have password cache in the implementation.
     * 
     * Applications MUST use the returned sid after authenticated.
     * 
     * Each invokation of this generates a new sid -- as a new client has been created.
     * 
     * @param user
     * @param password
     * @return
     * @throws javax.jms.JMSException
     */
    public String authenticate(String user, String password) throws JMSException {
        
        if (UMSServiceImpl.debug) {
            logger.info("auth user., user=" + user);
        }
        
        if (this.shouldAuthenticate) {
            
            /**
             * first check if user/pass are valid string
             */
            //if ((user == null) || (password == null) || user.isEmpty() || password.isEmpty()) {
            //    throw new JMSException("User or password cannot be null or empty");
            //}

            //check with server - let JMS server check if user/pass is valid
            authenticateWithJMSServer(user, password);
        }

        if (UMSServiceImpl.debug) {
            logger.info("getting sid" );
        }
        
        //put info to cache
        String sid = nextSid();
        
        if (UMSServiceImpl.debug) {
            logger.info("got., sid=" + sid);
        }
        
        //client is added to client table (in client pool).
        //this.clients.add(sid);

        if (UMSServiceImpl.debug) {
            logger.info("Generated sid for user., user=" + user + ",sid=" + sid);
        }

        return sid;
    }
    
    /**
     * This is called each time a service request (send/receive) is received.
     * 
     * @param uuid
     * @throws javax.jms.JMSException
     */
    public void authenticateSid (String sid) throws JMSException {

        this.securedSid.verifySid(sid);

        //if (clients.contains(sid) == false) {
        //    throw new JMSException("sid is not authenticated.  Use login to get a new sid, expired/invalid sid=" + sid);
        //}
    }
    
    /**
     * called when a client is sweeped or closed.
     * 
     * @param uuid
     * @return
     */
    //public boolean removeSid (String sid) {
    //    return clients.remove(sid);
    //}
   
    /**
     * Check with JMS server if the provided user/password is valid.
     * @param user
     * @param passwrod
     * @throws javax.jms.JMSException
     */
    private void authenticateWithJMSServer(String user, String password) throws JMSException {

        if (UMSServiceImpl.debug) {
            logger.info("Authenticating user, user = " + user);
        }

        Connection conn = null;

        try {
            
            if (user == null) {
                //user default user/password
                conn = this.connFactory.createConnection();
            } else {
                
                if (this.base64encoding) {
                    password = this.securedSid.decode(password);
                }
                
                conn = this.connFactory.createConnection(user, password);
            }
            
            if (UMSServiceImpl.debug) {
                logger.info("User authenticated, user = " + user);
            }

        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex2) {
                    logger.log(Level.WARNING, ex2.getMessage(), ex2);
                }
            } else {

                if (UMSServiceImpl.debug) {
                    logger.info("Authentication failed, user = " + user);
                }

            }
        }

    }
    
    public String nextSid () throws JMSException {
        //return UUID.randomUUID().toString();
        
        return this.securedSid.nextSid();
    }
    
}
