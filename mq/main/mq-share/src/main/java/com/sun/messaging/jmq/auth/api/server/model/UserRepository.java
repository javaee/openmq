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
 * @(#)UserRepository.java	1.14 06/28/07
 */ 

package com.sun.messaging.jmq.auth.api.server.model;

import java.io.*;
import java.util.Properties;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.api.server.*;

/**
 * Interface for plug-in different user repository for authentication.
 * A class implements this interface for a particular user repository
 * canbe used in AuthenticationProtocolHandler.handleResponse() method
 * to authenticate user agaist the particular user repository.
 */

public interface UserRepository {

    /**
     * @return the type of this user repsitory
     */
    public String getType();


    /**
     * This method is called from AuthenticationProtocolHandler to
     * open the user repository before findMatch call
     *
     * @param authType the authentication type
     * @param authProperties auth properties in broker configuration
     * @param cacheData from last getCacheData() call
     *
     * @exception LoginException
     */
    public void open(String authType, 
                     Properties authProperties,
                     Refreshable cacheData) throws LoginException; 

    /**
     * Find the user in the repository and compare the credential with
     * the user's  credential in database 
     *
     * @param user the user name 
     * @param credential its type is a contract between the caller and implementor <BR>
     *                   for "basic" it is the plain password String <BR>
     *                   for "digest" it is MD5 digest user:password (bye[] type) <BR>
     * @param extra additional information 
     * @param matchType must be one of the supported match-types specified by
     *                  the UserRepository implementation class or null if not
     *                  required. The matchType is to tell what type of the credential
     *                  is passed.
     *
     * @return The authenticated subject or null if no match found <BR>
     * <P>
     * @exception LoginException
     */
    public Subject findMatch(String user,
                             Object credential,
                             Object extra, String matchType)
                             throws LoginException;
    
    /**
     * This method is called after findMatch() is successful
     * 
     * The cacheData will be passed to open() call next time on
     * a connection authentication
     *
     * @return A refreshed Refreshable object that need to be cached or
     *         null if no cache data or the cache data is not refreshed
     *         in the last open/findMatch call
     */
    public Refreshable getCacheData();

    /**
     * This method is called after findMatch returns
     *
     * @exception LoginException
     */
    public void close() throws LoginException;

}
