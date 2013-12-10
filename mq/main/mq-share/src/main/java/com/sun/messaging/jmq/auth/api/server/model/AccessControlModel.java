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
 * @(#)AccessControlModel.java	1.10 06/28/07
 */ 

package com.sun.messaging.jmq.auth.api.server.model;

import java.util.Properties;
import java.security.Principal;
import java.security.AccessControlException;
import javax.security.auth.Subject;
import com.sun.messaging.jmq.auth.api.server.*;

/**
 * An AccessControlModel contains access controls which guards access
 * JMQ resources (connections, destinations)
 */

public interface AccessControlModel 
{
    /**
     * @return the type of this access control model
     */
    public String getType();

    /**
     * This method is called immediately after this AccessControlModel
     * has been instantiated and prior to any calls to its other public
     * methods.
     *
	 * @param type The jmq.accesscontrol.type value in authProperties
     * @param authProperties The broker authentication/access control properties
     *
     * @exception AccessControlException
     */
    public void initialize(String type, 
                           Properties authProperties)
                           throws AccessControlException;

    /**
     * load the access control model 
     *
     * @exception AccessControlException
     */
    public void load() throws AccessControlException; 

   /**
    * Check connection permission for the subject
    *
    * @param mqUser The Principal represents the client user 
    *               that associated with the subject
    * @param serviceName The service instance name  (eg. "broker", "admin")
    * @param serviceType The service type for the service instance <BR>
    *                    ("NORMAL" or "ADMIN") <BR>
    * @param subject The subject
    *
    * @exception AccessControlException 
    */
    public void checkConnectionPermission(Principal clientUser,
                                          String serviceName, 
                                          String serviceType,
                                          Subject subject) 
                                          throws AccessControlException ;

   /**
    * Check permission for an operation on a destination for the subject
    *
    * @param clientUser The Principal represents the client user
    *                   that associated with the subject
    * @param serviceName The service instance name  (eg. "broker", "admin")
    * @param serviceType The service type for the service instance  <BR>
    *                    ("NORMAL" or "ADMIN") <BR>
    * @param subject The subject
    * @param operation The operaction ("send", "receive", "browse","publish", "subscribe")
    * @param destination The destination name
    * @param destinationType The destination Type ("queue" or "topic")
    *
    * @exception AccessControlException 
    */
    public void checkDestinationPermission(Principal clientUser,
                                           String serviceName,
                                           String serviceType,
                                           Subject subject,
                                           String operation,
                                           String destination,
                                           String destinationType)
                                           throws AccessControlException; 
}
