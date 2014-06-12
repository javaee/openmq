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
 * @(#)UserMgrUtils.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

import java.io.*;

import com.sun.messaging.jmq.util.Password;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

/** 
 * This class contains utility methods used by jmqusermgr.
 */
public class UserMgrUtils implements UserMgrOptions  {

    /**
     * Constructor
     */
    public UserMgrUtils() {
    } 

    /**
     * Return user input. Throws an exception if an error occurred.
     */
    public static String getUserInput(UserMgrProperties userMgrProps,
			String question) throws UserMgrException  {
        return(getUserInput(userMgrProps, question, null));
    }

    /**
     * Return user input. Return defResponse if no response ("") was
     * given. Throws an exception if an error occurred.
     */
    public static String getUserInput(UserMgrProperties userMgrProps,
			String question, String defResponse) throws UserMgrException  {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    Output.stdOutPrint(question);
	    String s = in.readLine();

            if (s == null) {
                throw new EOFException("null");
            }
	    if (s.equals("") && (defResponse != null))  {
	        s = defResponse;
	    }
	    return(s);

        } catch (IOException ioex) {
	    UserMgrException ex = 
		new UserMgrException(UserMgrException.PROBLEM_GETTING_INPUT);
	    ex.setProperties(userMgrProps);
	    ex.setLinkedException(ioex);
	    
	    throw (ex);
        }
    }    

    /**
     * Return password input.  
     */
    public static String getPasswordInput(UserMgrProperties userMgrProps,
			String question) {

	Password pw = new Password();
    if (pw.echoPassword()) {
        Output.stdOutPrintln(Globals.getBrokerResources().
                getString(BrokerResources.W_ECHO_PASSWORD));
    }
	Output.stdOutPrint(question);
	return pw.getPassword();

    }

}    
