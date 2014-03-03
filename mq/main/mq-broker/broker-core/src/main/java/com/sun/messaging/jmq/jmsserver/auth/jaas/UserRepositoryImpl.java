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
 * @(#)UserRepositoryImpl.java	1.11 06/28/07
 */ 
 
package com.sun.messaging.jmq.jmsserver.auth.jaas;

import java.io.*;
import java.util.*;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;
import javax.security.auth.Destroyable;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.StringUtil;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.auth.api.server.model.*;

/**
 * MQ JAAS user repository plugin
 */
public class UserRepositoryImpl implements UserRepository
{
    private static boolean DEBUG = false;

    public static final String TYPE = "jaas";

    public static final String PROP_NAME_SUFFIX = ".name";
    public static final String SUBJECT_HELPER_SUFFIX = ".subjectHelperClass";
    public static final String SUBJECT_HELPER_PROPS_SUFFIX = ".subjectHelperClass.props";
    public static final String SUBJECT_HELPER_EMBEDDED_PROP = Globals.IMQ+".embedded";
    public static final String SUBJECT_HELPER_JMSRA_MANAGED_PROP = Globals.JMSRA_MANAGED_PROPERTY;

	private Logger logger = Globals.getLogger();

    private String authType = null;
    private Properties authProps = null;
    private String name = null;
    private SubjectHelper subjectHelper = null;

    private LoginContext lc = null;

    private Object lock = new Object(); 
    private boolean login = false;
    private boolean logout = false;


    public UserRepositoryImpl() { }

    public String getType() {
        return TYPE;
    }

    public void open(String authType, Properties authProperties,
                     Refreshable cacheData) throws LoginException {
        this.authType = authType;
        this.authProps = authProperties;

        String rep = authProps.getProperty(
              AccessController.PROP_AUTHENTICATION_PREFIX+authType+
                       AccessController.PROP_USER_REPOSITORY_SUFFIX);
        if (rep == null) {
        throw new LoginException(Globals.getBrokerResources().getKString(
             BrokerResources.X_USER_REPOSITORY_NOT_DEFINED, authType));
        }
        if (!rep.equals(TYPE)) {
        String[] args = {rep, TYPE, this.getClass().getName()};
        throw new LoginException(Globals.getBrokerResources().getKString(
             BrokerResources.X_REPOSITORY_TYPE_MISMATCH, args));
        }
        String prefix = AccessController.PROP_USER_REPOSITORY_PREFIX+rep;
        name = authProps.getProperty(prefix+PROP_NAME_SUFFIX);
        if (name == null) {
        throw new LoginException(Globals.getBrokerResources().getKString(
             BrokerResources.X_JAAS_NAME_INDEX_NOT_DEFINED));
        }
        String subjectHelperc = authProps.getProperty(prefix+SUBJECT_HELPER_SUFFIX);
        if (subjectHelperc != null) {
            try {
                subjectHelper = (SubjectHelper)Class.forName(subjectHelperc).newInstance();
                String pstr = authProps.getProperty(prefix+SUBJECT_HELPER_PROPS_SUFFIX);
                Properties props = new Properties();
                props = StringUtil.toProperties(pstr, props);
                if (Broker.isInProcess()) {
                    props.setProperty(SUBJECT_HELPER_EMBEDDED_PROP, "true");
                }
                if (Globals.isJMSRAManagedBroker()) {
                    props.setProperty(SUBJECT_HELPER_JMSRA_MANAGED_PROP, "true");
                }
                subjectHelper.init(name, props, null);
            } catch (Exception e) {
                logger.logStack(logger.ERROR, "Unable to instantiate class "+subjectHelperc, e); 
                throw new LoginException(e.getMessage());
            }
        }
        if (DEBUG) {
            logger.log(logger.INFO, "Using JAAS authentication "+name+
                (subjectHelperc == null ? "":" with subject helper class "+subjectHelperc)); 
        }
    }

    /**
     * Find the user in the repository and compare the credential with
     * the user's  credential  
     *
     * @param user the user name
     * @param credential password (String type) for "basic" is the password
     * @param extra null for basic, nonce if digest
     * @param matchType must be "basic"
     *        
     * @return the authenticated Subject  <BR>
     *         null if no match found <BR>
     *
     * @exception LoginException
     */
    public Subject findMatch(String user, Object credential,
                             Object extra, String matchType)
                             throws LoginException {
        if (matchType != null && matchType.equals(AccessController.AUTHTYPE_BASIC)) {
        return basicFindMatch(user, (String)credential);
        }
        String matchtyp = (matchType == null) ? "null": matchType;
        String[] args = {matchtyp, authType, getType(), AccessController.AUTHTYPE_BASIC};
        throw new LoginException(Globals.getBrokerResources().getKString(
              BrokerResources.X_UNSUPPORTED_USER_REPOSITORY_MATCHTYPE, args));
    }
      
    private Subject basicFindMatch(String user, String userpwd) throws LoginException {
        if (DEBUG) {
        logger.log(Logger.DEBUG, 
        "Authentication type "+AccessController.AUTHTYPE_BASIC+" - "+user+":"+userpwd);
        }
        CallbackHandlerImpl cbh = new CallbackHandlerImpl(authProps, user, userpwd);
        Subject sub = null;
        if (subjectHelper != null) {
            try {
                sub = subjectHelper.makeSubject(user, userpwd);
            } catch (Exception e) {
                String emsg = "Failed to make subject of user "+user;
                logger.logStack(Logger.ERROR, emsg, e);
                LoginException ex= new LoginException(emsg);
                ex.initCause(e);
                throw ex;
            }
        }
        if (sub != null) {
            lc = new LoginContext(name, sub, cbh); 
        } else {
            lc = new LoginContext(name, cbh); 
        }
        synchronized(lock) {
            if (logout) {
                throw new LoginException(Globals.getBrokerResources().getKString(
                      BrokerResources.X_CONNECTION_LOGGEDOUT));
            }
            lc.login();
            login = true;
            cbh.destroy();
            Subject subject = lc.getSubject();
            //XXX ?
            Set creds = subject.getPrivateCredentials();
            Iterator itr = creds.iterator(); 
            Object cred = null; 
            while (itr.hasNext()) {
                try {
                    cred = (Object)itr.next();
                    if (cred == null) continue;
                    if (cred instanceof Destroyable && !((Destroyable)cred).isDestroyed()) {
                       ((Destroyable)cred).destroy();
                    }
                } catch (Exception e) {
                logger.log(logger.WARNING, 
                "XXX Unable to destroy private credential:"+cred.getClass().getName()+ " for "+user);
                }
            }
            return subject;
        }
    }

    public Refreshable getCacheData() {
       if (subjectHelper == null) return null;  
       return subjectHelper.getCacheData();
    }

    public void close() throws LoginException { 
        synchronized(lock) {
            if (!logout) {
                if (login)  {
                    lc.logout(); 
                    logout = true;
                }
            }
        }
    }

}
