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
 * @(#)PasswdDB.java	1.20 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.MD5;
import com.sun.messaging.jmq.util.FileUtil;


/**
 *  This class is used to manage (add, list and delete) users.  Here is a 
 *  piece of code that can be used for testing.
 *
 *
<PRE>
    public static void usage() {
	System.out.println("Usage:" + 
			   "\n\tjava PasswdDB -add <user_name> " +
			   "-password <password>" +
			   "\n\tjava PasswdDB -delete <user_name>" +
			   "\n\tjava PasswdDB -list");
    }
    
    public static void main(String[] args) {
	if (args.length <= 0) {
	    usage();
	    System.exit(0);
	}
	else {
	    PasswdDB umt = new PasswdDB();
	    for (int i = 0 ; i < args.length ; i++) {
		if (DEBUG) {
		    System.out.println(i +"  "+ args[i]);
		}
	    }       
	    if (args[0].equals("-add"))  {
		if (args[2].equals("-password"))  {
		    umt.addUser((String)args[1], (String)args[3]);
		}
	    }
	    else  if (args[0].equals("-list"))  {
        	System.out.println("Listing Users ...");
		umt.listUsers();
	    }
	    else  if (args[0].equals("-delete"))  {
		umt.deleteUser((String)args[1]);
	    }
	    else {
		usage();
	    }
	}
	System.exit(0);	
    }
</PRE>
 *
 */

public class PasswdDB {
    
    private static boolean DEBUG = false;

    public final static char PASSWD_FIELD_SEPARATOR = ':';

    static String pas_file;

    public static void setPasswordFileName(String pfname) {
	pas_file = pfname;
    }

    public String getPasswordFileName() {
	if (pas_file == null) {
	    pas_file = Globals.JMQ_ETC_HOME
		+ File.separator
		+ "passwd";

	    if (DEBUG) {
		System.out.println("Password Filename = " + pas_file);
	    }
	}
	return pas_file;
    }


    /**
     * Add a new user with a password to the JMQ password database.
     *
     * @param user_name User name of the new user
     * @param password Password for the new user
     *
     */
    public void addUser(String user_name, String password, String role) 
				throws UserMgrException {
	Hashtable user_add = getUserTable();

        if (role.equals(UserInfo.ROLE_ANON)) {
	    int count = getUserCount(user_add, UserInfo.ROLE_ANON);

	    if (count != 0)  {
	        UserMgrException ume = 
		    new UserMgrException(UserMgrException.ONLY_ONE_ANON_USER);
	        throw ume;
	    }
        }

	if (DEBUG) {
	    for (Enumeration e=user_add.keys(); e.hasMoreElements();) {
		String key_val = (String)e.nextElement();		
		System.out.println("\t"+ key_val + " = " +  user_add.get(key_val));
	    }
	}
	
	if (!user_add.containsKey(user_name)) {
	    UserInfo	newUserInfo;

	    newUserInfo = new UserInfo(user_name, hashPassword(user_name, password), role);
            user_add.put(user_name, newUserInfo);
            writeUserTable(user_add);
	} else {
	    UserMgrException ume = new UserMgrException(UserMgrException.USER_ALREADY_EXIST);
	    ume.setUserName(user_name);
	    throw ume;
	}
	
	if (DEBUG)  {
	    System.err.println("Users in role ADMIN: " + getUserCount(UserInfo.ROLE_ADMIN));
	    System.err.println("Users in role USER: " + getUserCount(UserInfo.ROLE_USER));
	    System.err.println("Users in role ANONYMOUS: " + getUserCount(UserInfo.ROLE_ANON));
	}
    }
    
    /** 
     * Delete a user from the JMQ Password database
     *
     * @param user_name User name of the user to be deleted
     *
     */
    public void deleteUser(String user_name) throws UserMgrException {
	Hashtable users = getUserTable();


	if (users.containsKey(user_name))  {
	    users.remove(user_name);
            writeUserTable(users);
	} else {
	    UserMgrException ume = new UserMgrException(UserMgrException.USER_NOT_EXIST);
	    ume.setUserName(user_name);
	    throw ume;
	}
	
    }

    /**
     * Modify existing user's info i.e. passwd or active state
     *
     */
    public void updateUser(String user_name, 
				String new_pass, Boolean new_active)
			throws UserMgrException {

        Hashtable musers = getUserTable();
	if (userExists(user_name, musers)) {
            UserInfo	curInfo, updateInfo;
	    String	curPasswd, curRole, newHashedPasswd;
	    boolean	curActive, b;

            curInfo = (UserInfo)musers.get(user_name);
	    curPasswd = curInfo.getPasswd();
	    curRole = curInfo.getRole();
	    curActive = curInfo.isActive();

	    if (new_pass == null)  {
	        newHashedPasswd = curPasswd;
	    } else  {
	        newHashedPasswd = hashPassword(user_name, new_pass);
	    }

	    if (new_active == null)  {
	        b = curActive;
	    } else  {
	        b = new_active.booleanValue();
	    }

	    updateInfo = new UserInfo(user_name, 
				newHashedPasswd,
				curRole, b);

	    musers.put(user_name, updateInfo);
            writeUserTable(musers);
	} else {
	    UserMgrException ume = new UserMgrException(UserMgrException.USER_NOT_EXIST);
	    ume.setUserName(user_name);
	    throw ume;
	}
    }


    public UserInfo getUserInfo(String user) throws UserMgrException {
	Hashtable	users = getUserTable();
	UserInfo	uInfo;

        uInfo = (UserInfo)users.get(user);

	return (uInfo);
    }

    public Enumeration getUsers() throws UserMgrException {
	Hashtable users = getUserTable();
	
	return (users.elements());
    }

    
    /**
     * Check to see if a user is in the JMQ password database
     *
     * @param user_name User name to check in the JMQ password database
     * @return boolean true is found, false, if the user does not exist
     *
     */
    public boolean userExists(String user_name, Hashtable users) {
	if (users.containsKey(user_name)) {
	    return true;
	}
	else {
	    return false;
	}
    }
    
    public int getUserCount(String role) throws UserMgrException  {
	Hashtable users = getUserTable();

	return (getUserCount(users, role));
    }

    private int getUserCount(Hashtable users, String role) throws UserMgrException  {
	int	count = 0;

	if ((role == null) || (role.equals("")))  {
	    return (users.size());
	}

        for (Enumeration e=getUsers(); e.hasMoreElements();) {
            UserInfo oneUser = (UserInfo)e.nextElement();
	    if (oneUser.getRole().equals(role))  {
		count++;
	    }
        }

	return (count);
    }

    private String hashPassword(String u_name, String pass) 
			throws UserMgrException  {
	String s;
	try {
	    s = MD5.getHashString(u_name+":"+pass);
	} catch (Exception ex)  {
	    UserMgrException ume = new UserMgrException(UserMgrException.PASSWD_ENCRYPT_FAIL);
	    ume.setLinkedException(ex);

	    throw ume;
	}

	return (s);
    }

    private Hashtable getUserTable() throws UserMgrException  {
        Hashtable	user_list = new Hashtable ( 13 );
        FileInputStream fis = null;
	InputStreamReader in;
        BufferedReader	br;
        String		passwd_filename = getPasswordFileName(),
			str, u_name, u_pwd, u_role, u_active;
	boolean		active;

	try  {
            fis = new FileInputStream(passwd_filename);
	} catch (FileNotFoundException fnfe)  {
	    UserMgrException ume = new UserMgrException(UserMgrException.PW_FILE_NOT_FOUND);
	    ume.setPasswordFile(FileUtil.getCanonicalPath(passwd_filename));
	    ume.setLinkedException(fnfe);

	    throw ume;
	}

	/*
	 * Try to open an input stream reader using UTF8
	 */
	try  {
	    in = new InputStreamReader(fis, "UTF8");
	} catch (UnsupportedEncodingException usEncEx)  {
	    in = null;
	}

	/*
	 * If the above failed, use the default encoding instead.
	 */
	if (in == null)  {
	    in = new InputStreamReader(fis);
	}

        br = new BufferedReader(in);

	try  {
            while ((str = br.readLine()) != null) {
	        /*
	         * Each entry should look like:
	         *	admin:-2d5455c8583c24eec82c7a1e273ea02e:admin:1
	         * There are 3 colons (':')
	         */
                if (DEBUG) {
                    System.out.println("Uname:Pwd read from file = " + str);
                }
                int ind1 = str.indexOf(PASSWD_FIELD_SEPARATOR);
                int ind2 = str.indexOf(PASSWD_FIELD_SEPARATOR, ind1+1);
                int ind3 = str.lastIndexOf(PASSWD_FIELD_SEPARATOR);
                if (ind1 == -1 || ind2 == -1 || ind3 == -1) {
                    throw new IOException (
                        Globals.getBrokerResources().getString(
                        BrokerResources.X_INTERNAL_EXCEPTION,"format error in passwd file " 
                        + FileUtil.getCanonicalPath(passwd_filename)));
                }
                u_name = str.substring(0, ind1);
                u_pwd  = str.substring(ind1+1, ind2);
                u_role = str.substring(ind2+1, ind3);
                u_active = str.substring(ind3+1);
                if (DEBUG) {
                    System.out.println("User name = " + u_name +
                               " & Password = " + u_pwd + " & role = " + u_role
			       + " & active = " + u_active);
                }

		if (u_active.equals("1"))  {
		    active = true;
		} else  {
		    active = false;
		}
                user_list.put(u_name, new UserInfo(u_name, u_pwd, u_role, active));
            }
        } catch (IOException ioe) {
	    UserMgrException ume = new UserMgrException(UserMgrException.PW_FILE_READ_ERROR);
	    ume.setPasswordFile(FileUtil.getCanonicalPath(passwd_filename));
	    ume.setLinkedException(ioe);

	    throw ume;
	}

        return user_list;
    }

    private void writeUserTable(Hashtable users) throws UserMgrException  {
        String passwd_filename = getPasswordFileName();
	FileOutputStream fos;
	OutputStreamWriter out;

	try  {
            fos = new FileOutputStream(passwd_filename);
	} catch (FileNotFoundException fnfe)  {
	    UserMgrException ume = new UserMgrException(UserMgrException.PW_FILE_NOT_FOUND);
	    ume.setPasswordFile(FileUtil.getCanonicalPath(passwd_filename));
	    ume.setLinkedException(fnfe);

	    throw ume;
	}

	/*
	 * Try to open an output stream writer using UTF8
	 */
	try  {
	    out = new OutputStreamWriter(fos, "UTF8");
        } catch (UnsupportedEncodingException usEncEx)  {
	    out = null;
	}

	/*
	 * If the above failed, use the default encoding instead.
	 */
	if (out == null)  {
	    out = new OutputStreamWriter(fos);
	}

	try  {
            for (Enumeration e=users.keys(); e.hasMoreElements();) {
                String k_val = (String)e.nextElement();
                UserInfo user = (UserInfo)users.get(k_val);
                out.write(user.getPasswdEntry() + "\n");
             }
             out.flush();
             out.close();
	 } catch (IOException ioe) {
	    UserMgrException ume = new UserMgrException(UserMgrException.PW_FILE_WRITE_ERROR);
	    ume.setPasswordFile(passwd_filename);
	    ume.setLinkedException(ioe);

	    throw ume;
	 }
    }
}
