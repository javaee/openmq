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
 * @(#)AuthenticationProtocolHandler.java	1.15 06/28/07
 */ 

package com.sun.messaging.jmq.auth.api.server;

import java.util.Properties;
import javax.security.auth.Refreshable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.api.FailedLoginException;

/**
 * This is broker-side AuthenticationProtocolHandler
 */
public interface AuthenticationProtocolHandler {

    /**
     * This method must return the authentication type it implements. 
     */
    public String getType();

    /**
     * This method is called once before any handleResponse() calls for 
     * this authentication process
     *   
     * @param sequence packet sequence number which can be used as a start
     *                 sequence number for this authentication process
     * @param authProperties contains broker auth properties for this authType
     * @param cacheData The cacheData if any (see getCacheData()). 
     *
     * @return initial authentication request data if any
     *         null if no initial authentication request data
     */
    public byte[] init(int sequence,
                       Properties authProperties,
                       Refreshable cacheData) throws LoginException;

    /**
     * This method is called to handle a authentication response
     *
     * @param authResponse the authentication response data.  This is the
     *                     AUTHENTICATE packet body.
     * @param sequence the packet sequence number
     *
     * @return next request data if any; null if no more request 
     *  Request data will be sent as packet body in AUTHENTICATE_REQUEST
     *                 
     * @exception LoginException if error occurs while handle the response
     * @exception com.sun.messaging.jmq.auth.FailedLoginException if invalid user or credential
     */
    public byte[] handleResponse(byte[] authResponse, int sequence)
                                             throws LoginException;

    /**
     * This method will be called when the connection closes or the service
     * type of the connection is denied to the subject.
     */
    public void logout() throws LoginException;

    /**
     * This method is called when handleReponse() successfully completes.
     *
     * @return a AccessControlContext object associated with the authentication subject
     * The object returned is used for access control after successful authentication 
     *
     */
     public AccessControlContext getAccessControlContext();

     /**
      * This method is called after handleReponse() successfully completes.
      * The object retrieved will be stored into the service instance and
      * on next connection authentication, this object will be passed to
      * init() method call.
      *
      * @return A Refreshable object that is to be cached, 
      *         null if not interest to cache anything
      */
     public Refreshable getCacheData();

}
