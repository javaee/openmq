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
 */ 
 
package com.sun.messaging.jmq.jmsserver.auth.jaas;

import java.util.Properties;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;

/**
 */
public interface SubjectHelper 
{

    /**
     * This method is called before each makeSubject() call
     *
     * @param loginModuleName the name of the JAAS LoginModule
     * @param props properties configured for this SubjectHelper
     * @param cacheData the cached data from previous call getCacheData(), null on first call
     *
     * @throws Exception if any failure
     */
    public void init(String loginModuleName, Properties props,
                     Refreshable cacheData)
                     throws Exception;

    /**
     *  Make a Subject object representing the user to be authenticated.
     *
     *  This Subject object will be passed to the JAAS LoginContext for
     *  authentication by the LoginModule 
     *
     * @param username the user name to be authenticatd
     * @param password the password of the user to be authenticated
     * @return a the subject to be authenticated
     *
     * @throws Exception if any failure
     */
    public Subject makeSubject(String username, String password) throws Exception;

    /**
     * This method is called after each makeSubject() call
     * 
     * @return data that need to be cached
     */
    public Refreshable getCacheData(); 
}
