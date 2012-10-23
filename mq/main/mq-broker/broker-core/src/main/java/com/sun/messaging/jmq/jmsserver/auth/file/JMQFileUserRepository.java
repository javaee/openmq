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
 * @(#)JMQFileUserRepository.java	1.34 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.file;

import java.io.*;
import java.util.*;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.util.MD5;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.StringUtil;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.auth.jaas.MQUser;
import com.sun.messaging.jmq.auth.jaas.MQGroup;
import com.sun.messaging.jmq.auth.jaas.MQAdminGroup;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.auth.api.server.model.*;

/**
 * JMQ passwd user repository plugin
 */
public class JMQFileUserRepository implements UserRepository
{
    public static final String TYPE = "file";

    public static final String PROP_FILENAME_SUFFIX = TYPE + ".filename";
    public static final String PROP_DIRPATH_SUFFIX = TYPE + ".dirpath";
    public static final String DEFAULT_PW_FILENAME = "passwd";

    private static boolean DEBUG = false;
	private transient Logger logger = Globals.getLogger();

    private static String ADMINGROUP = "admin";
    private String authType;
    private Properties authProps = null;

    public JMQFileUserRepository() { }

    public String getType() {
        return TYPE;
    }

    public void open(String authType, Properties authProperties,
                     Refreshable cacheData) throws LoginException {
        this.authType = authType;
        this.authProps = authProperties;
    }

    /**
     * Find the user in the repository and compare the credential with
     * the user's  credential in database
     *
     * @param user the user name
     * @param credential password (String type) for "basic" is the password
     * @param extra null for basic, nonce if digest
     * @param matchType must be "basic" or "digest"
     *        
     * @return the authenticated subject  <BR>
     *         null if no match found <BR>
     *
     * @exception LoginException
     */
    public Subject findMatch(String user, Object credential,
                             Object extra, String matchType)
                             throws LoginException {
        if (matchType == null ||
            (!matchType.equals(AccessController.AUTHTYPE_BASIC) &&
             !matchType.equals(AccessController.AUTHTYPE_DIGEST))) {
            String matchtyp = (matchType == null) ? "null": matchType;
            String[] args = {matchtyp, authType, getType(),
                AccessController.AUTHTYPE_BASIC+":"+AccessController.AUTHTYPE_DIGEST};
            throw new LoginException(Globals.getBrokerResources().getKString(
                BrokerResources.X_UNSUPPORTED_USER_REPOSITORY_MATCHTYPE, args));
        }

        HashMap userPTable = new HashMap();
        HashMap userRTable = new HashMap();
        try {
            loadUserTable(userPTable, userRTable);
        } catch (IOException e) {
            logger.logStack(logger.ERROR, e.getMessage(), e);
            userPTable = null;
            userRTable = null;
            throw new LoginException(e.getMessage());
        }

        Subject subject = null; 
        if (matchType.equals(AccessController.AUTHTYPE_BASIC)) {
            subject = basicFindMatch(user, (String)credential, userPTable, userRTable);
        }
        else if (matchType.equals(AccessController.AUTHTYPE_DIGEST)) {
            subject = digestFindMatch(user, (String)credential, (String)extra, userPTable, userRTable); 
        }
        userPTable = null;
        userRTable = null;
        return subject;
    }
      
    private Subject basicFindMatch(String user, String userpwd, 
                      HashMap userPTable, HashMap userRTable) throws LoginException {
        if (DEBUG) {
        logger.log(Logger.INFO, "basic matching["+user+":"+userpwd+"]");
        }
        Subject subject = null;
        String passwd = (String)userPTable.get(user);
        if (passwd != null) {
            if (passwd.equals(MD5.getHashString(user+":"+userpwd))) {
                subject = getSubject(user, userRTable);
            }
        }
        return subject;
    }

    private Subject digestFindMatch(String user, String credential, String nonce,
                      HashMap userPTable, HashMap userRTable) throws LoginException {
        if (DEBUG) {
        logger.log(Logger.INFO, "digest matching "+user+"["+credential+":"+nonce+"]");
        }
        Subject subject = null;
        String passwd = (String)userPTable.get(user);
        if (passwd != null) {
            String passwdnonce = MD5.getHashString(passwd+":"+nonce);
            if (credential.equals(passwdnonce)) {
                subject =  getSubject(user, userRTable);
            }
        }
        return subject;
    }
            
    private Subject getSubject(String user, HashMap userRTable) {  
        Subject subject = null;
        final String rolestr = (String)userRTable.get(user);
        if (rolestr != null && !rolestr.trim().equals("")) {
            //subject = new Subject();
            final String tempUser = user;
            //final Subject tempSubject = subject;
            subject = (Subject) java.security.AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    public Object run(){
                        Subject tempSubject = new Subject();
                        tempSubject.getPrincipals().add(new MQUser(tempUser));
                        tempSubject.getPrincipals().add(new MQGroup(rolestr));
                        if (rolestr.equals(ADMINGROUP)) {
                            tempSubject.getPrincipals().add(
                                    new MQAdminGroup(ADMINGROUP));
                        }
                        return tempSubject;
                    }
                }
            );
/*
//            subject = new Subject();
//            subject.getPrincipals().add(new MQUser(user));
//            subject.getPrincipals().add(new MQGroup(rolestr));
//            if (rolestr.equals(ADMINGROUP)) {
//                subject.getPrincipals().add(new MQAdminGroup(ADMINGROUP));
//            }
*/
        }
        return subject;
    }

    private void loadUserTable(HashMap userPTable, HashMap userRTable) throws IOException {
        String rep = authProps.getProperty(
                          AccessController.PROP_AUTHENTICATION_PREFIX +
                          authType +AccessController.PROP_USER_REPOSITORY_SUFFIX);
        if (rep == null) {
        throw new IOException(Globals.getBrokerResources().getKString(
                              BrokerResources.X_USER_REPOSITORY_NOT_DEFINED, authType));
        }
		if (!rep.equals(TYPE)) {
        String[] args = {rep, TYPE, this.getClass().getName()};
        throw new IOException(Globals.getBrokerResources().getKString(
                              BrokerResources.X_REPOSITORY_TYPE_MISMATCH, args));
        }

        File pwdfile = getPasswordFile(authProps, false);

        InputStreamReader fr = null;
        BufferedReader br = null;

        try {
        fr = new InputStreamReader(new FileInputStream(pwdfile), "UTF8");
        br = new BufferedReader(fr);

        String line, name, passwd, role, active;
        while ((line = br.readLine()) != null) {
            name = passwd = role = active = null;
            StringTokenizer st = new StringTokenizer(line, ":", false);
            if (st.hasMoreTokens()) {
                name = st.nextToken(); 
            }
            if (st.hasMoreTokens()) {
                passwd = st.nextToken(); 
            }
            if (st.hasMoreTokens()) {
                role = st.nextToken(); 
            }
            if (st.hasMoreTokens()) {
                active = st.nextToken(); 
            }
            if (DEBUG) {
            logger.log(Logger.INFO, "passwd entry "+name+":"+passwd+":"+role+":"+active);
            }
            if (name !=null && passwd != null && role != null 
                    && active != null && active.equals("1")) {
                userPTable.put(name, passwd);
                userRTable.put(name, role);
            }
        }

        br.close();
        fr.close();

        } catch (IOException ioe) {
        try {
        if (br != null) br.close();
        if (fr != null) fr.close();
        } catch (IOException e) {}
        IOException ex = new IOException(Globals.getBrokerResources().getKString(
                         BrokerResources.E_PW_FILE_READ_ERROR, pwdfile.toString(),
                         ioe.getMessage()));
        ex.initCause(ioe);

        throw ex;
        }
    }

    public Refreshable getCacheData() {
       return null;  
    }

    public void close() throws LoginException { }

    public static String getPasswordDirPath(Properties props, boolean fromUserManager) {
        String passwd_loc = props.getProperty(
                            AccessController.PROP_USER_REPOSITORY_PREFIX
                            +PROP_DIRPATH_SUFFIX, Globals.getInstanceEtcDir());
        if (fromUserManager) {
            passwd_loc = StringUtil.expandVariables(passwd_loc, props);
        }
        return passwd_loc;
    }

    public static File getPasswordFile(Properties props, boolean fromUserManager) {
        String passwd_loc = getPasswordDirPath(props, fromUserManager);
        String f = props.getProperty(
                   AccessController.PROP_USER_REPOSITORY_PREFIX
                   +PROP_FILENAME_SUFFIX, DEFAULT_PW_FILENAME);

        String pwdfile = passwd_loc +File.separator + f;
        return new File(pwdfile);
    }
}
