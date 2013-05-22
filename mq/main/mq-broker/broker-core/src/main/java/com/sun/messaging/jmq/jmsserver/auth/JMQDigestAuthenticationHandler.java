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
 * @(#)JMQDigestAuthenticationHandler.java	1.21 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth;

import java.io.*;
import java.util.*;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.api.FailedLoginException;
import com.sun.messaging.jmq.util.MD5;
import com.sun.messaging.jmq.auth.jaas.MQUser;
import com.sun.messaging.jmq.auth.api.server.*;
import com.sun.messaging.jmq.auth.api.server.model.UserRepository;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.Globals;

/**
 * "digest" authentication handler
 */
public class JMQDigestAuthenticationHandler implements AuthenticationProtocolHandler {

    private String nonce = null;
    private AccessControlContext acc = null;
    private Properties authProps;
    private Refreshable cacheData = null;
    private boolean cacheDataMaystaled;

    public String getType() {
        return AccessController.AUTHTYPE_DIGEST;
    }

    /**
     * This method is called once before any handleReponse() calls
     *   
     * @param sequence packet sequence number
     * @param authProps authentication properties
     * @param cacheData the cacheData 
     *   
     * @return initial authentication request data if any
     */  
    public byte[] init(int sequence, Properties authProperties,
                       Refreshable cacheData) throws LoginException {
        this.authProps = authProperties;
        String clientip = authProps.getProperty(Globals.IMQ+".clientIP");
        long timestamp = System.currentTimeMillis();
        String seed = authProps.getProperty(Globals.IMQ+".connectionID");
        nonce = MD5.getHashString(clientip+":"+timestamp+seed);
        try {
        return nonce.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
        throw new LoginException(e.getMessage());
        }
    }

    /**
     * @param authResponse the authentication response data.
     *                     This is the AUTHENCATE packet body.
     * @param sequence packet sequence number
     *
     * @return next request data if any; null if no more request.
     *  The request data will be sent as packet body in AUTHENTICATE_REQUEST
     *                 
     * @exception LoginException 
     */
    public byte[] handleResponse(byte[] authResponse, int sequence) throws LoginException {
        Subject subject = null;

        ByteArrayInputStream bis = new ByteArrayInputStream(authResponse);
        DataInputStream dis = new DataInputStream(bis);
        try {
        String username = dis.readUTF();
        String credential = dis.readUTF();
        dis.close();

        String rep = authProps.getProperty(
            AccessController.PROP_AUTHENTICATION_PREFIX+ getType()+
                      AccessController.PROP_USER_REPOSITORY_SUFFIX);
        if (rep == null || rep.trim().equals("")) {
            throw new LoginException(
                Globals.getBrokerResources().getKString(
                BrokerResources.X_USER_REPOSITORY_NOT_DEFINED, getType()));
        }
        String cn = authProps.getProperty(
                    AccessController.PROP_USER_REPOSITORY_PREFIX + rep +".class");
        if (cn == null) {
            throw new LoginException(
            Globals.getBrokerResources().getKString(
            BrokerResources.X_USER_REPOSITORY_CLASS_NOT_DEFINED, rep, getType()));
        }
        UserRepository repository =  (UserRepository)Class.forName(cn).newInstance();
        repository.open(getType(), authProps, cacheData);
        subject = repository.findMatch(username, credential, nonce, getMatchType());
        cacheData = repository.getCacheData();
        repository.close();
        repository = null;
        if (subject == null) {
            FailedLoginException ex = new FailedLoginException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_FORBIDDEN, username));
	    ex.setUser(username);
	    throw ex;
        }

        acc = new JMQAccessControlContext(new MQUser(username), subject, authProps);
        return null;

        } catch (ClassNotFoundException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"ClassNotFoundException: "+e.getMessage()));
        } catch (IOException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"IOException: "+e.getMessage()));
        } catch (InstantiationException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"InstantiationException: "+e.getMessage()));
        } catch (IllegalAccessException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"IllegalAccessException: "+e.getMessage()));
        } catch (ClassCastException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"cLassCastException: "+e.getMessage()));
        }

    }
    
    public void logout() { }

    public AccessControlContext getAccessControlContext() {
        return acc;
    }

    public Refreshable getCacheData() {
        return cacheData;
    }

    public String getMatchType() {
        return getType();
    }
}
