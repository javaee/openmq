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
 * @(#)JAASAccessControlModel.java	1.7 06/28/07
 */ 
 
package com.sun.messaging.jmq.jmsserver.auth.acl;

import java.util.Map;
import java.util.Properties;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.security.Permission;
import java.security.AccessControlException;
import java.security.Policy;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.auth.jaas.*;
import com.sun.messaging.jmq.auth.api.server.*;
import com.sun.messaging.jmq.auth.api.server.model.*;

/**
 * JAASAccessControlModel 
 */

public class JAASAccessControlModel implements AccessControlModel {

    public static final String TYPE = "jaas";

    public static final String PROP_PERMISSION_FACTORY = TYPE + ".permissionFactory";
    public static final String PROP_PERMISSION_FACTORY_PRIVATE = TYPE + ".permissionFactoryPrivate";
    public static final String PROP_POLICY_PROVIDER = TYPE + ".policyProvider";

    //private static boolean DEBUG = false;
    private Logger logger = Globals.getLogger();

    //private String type;
    private Properties authProps;

    private PermissionFactory permFactory = null;
    private String permFactoryPrivate = null;
    //private Policy policyProvider = null;

    public String getType() {
        return TYPE;
    }

    /**
     * This method is called immediately after this AccessControlModel
     * has been instantiated and prior to any calls to its other public
     * methods.
     *
	 * @param type the jmq.accesscontrol.type
     * @param authProperties broker auth properties
     */
    public void initialize(String type, Properties authProperties)
                                     throws AccessControlException {
        //this.type = type;
        if (!type.equals(TYPE)) {
            String[] args = {type, TYPE, this.getClass().getName()};
            String emsg = Globals.getBrokerResources().getKString(
                       BrokerResources.X_ACCESSCONTROL_TYPE_MISMATCH, args);
            logger.log(Logger.ERROR, emsg);
            throw new AccessControlException(emsg);
        }
        authProps = authProperties;

        String pfclass = authProps.getProperty(
                         AccessController.PROP_ACCESSCONTROL_PREFIX+
                         PROP_PERMISSION_FACTORY);
        String ppclass = authProps.getProperty(
                         AccessController.PROP_ACCESSCONTROL_PREFIX+
                         PROP_POLICY_PROVIDER); 
        try {
            if (pfclass != null) {
                permFactory = (PermissionFactory)Class.forName(pfclass).newInstance();
            }
            //if (ppclass != null) policyProvider = (Policy)Class.forName(ppclass).newInstance();
            if (ppclass != null) {
                Class.forName(ppclass).newInstance();
            }
        } catch (Exception e) {
            logger.logStack(Logger.ERROR, e.getMessage(), e);
            throw new AccessControlException(e.getClass().getName()+": "+e.getMessage());
        }

        permFactoryPrivate = (String)authProps.getProperty(
                         AccessController.PROP_ACCESSCONTROL_PREFIX+
                         PROP_PERMISSION_FACTORY_PRIVATE); 

        load();
    }

    public void load() throws AccessControlException {

        try {
            Policy.getPolicy().refresh(); 
        } catch (SecurityException e) {
           AccessControlException ace = new AccessControlException(e.toString());
           ace.initCause(e);
           throw ace;
        }
    }

   /**
    *
    * Check connection permission 
    *
    * @param clientUser The Principal represents the client user that is
    *                   associated with the subject
    * @param serviceName the service instance name  (eg. "broker", "admin")
    * @param serviceType the service type for the service instance 
    *                    ("NORMAL" or "ADMIN")
    * @param subject the authenticated subject
    *
    * @exception AccessControlException 
    */
    public void checkConnectionPermission(Principal clientUser, 
                                          String serviceName, 
                                          String serviceType,
                                          Subject subject) 
                                          throws AccessControlException {

       Permission perm;
       try {
           perm = permFactory.newPermission(permFactoryPrivate, 
                                 PermissionFactory.CONN_RESOURCE_PREFIX+
                                 serviceType, (String)null, (Map)null);
       } catch (Exception e) {
           logger.logStack(Logger.ERROR, e.toString(), e);
           AccessControlException ace = new AccessControlException(e.toString());
           ace.initCause(e);
           throw ace;
       }
       try {
           checkPermission(subject, perm); 
       } catch (AccessControlException e) {
           AccessControlException ace = new AccessControlException(e.getMessage()+": "+
		                clientUser+" ["+subject.getPrincipals()+"]");
           ace.initCause(e);
           throw ace;
       }
    }

   /**
    * Check permission for an operation on a destination for this role
    *
    * @param clientUser The Principal represents the client user that is
    *                   associated with the subject
    * @param serviceName the service instance name  (eg. "broker", "admin")
    * @param serviceType the service type for the service instance 
    *                    ("NORMAL" or "ADMIN")
    * @param subject the authenticated subject
    * @operation the operaction 
    * @destination the destination
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
                                           throws AccessControlException {
       Permission perm;
       try {
           if (operation.equals(PacketType.AC_DESTCREATE)) {
           perm = permFactory.newPermission(permFactoryPrivate, 
                                 PermissionFactory.AUTO_RESOURCE_PREFIX+
                                 PermissionFactory.DEST_QUEUE, (String)null, (Map)null);
           } else {
           perm = permFactory.newPermission(permFactoryPrivate, 
                                 PermissionFactory.DEST_RESOURCE_PREFIX+
                                 PermissionFactory.DEST_QUEUE_PREFIX+destination,
                                 operation, (Map)null);
           }
       } catch (Exception e) {
           logger.logStack(Logger.ERROR, e.toString(), e);
           AccessControlException ace = new AccessControlException(e.toString());
           ace.initCause(e);
           throw ace;
       }
       try {
           checkPermission(subject, perm); 
       } catch (AccessControlException e) {
           AccessControlException ace = new AccessControlException(e.getMessage()+": "+
		                clientUser+" ["+subject.getPrincipals()+"]");
           ace.initCause(e);
           throw ace;
       }
    }

    private void checkPermission(Subject subject, Permission p) 
                                     throws AccessControlException {

        final Permission perm = p;
        Subject.doAsPrivileged(subject, new PrivilegedAction() {
                public Object run() {
                    java.security.AccessController.checkPermission(perm);
                    return null; // nothing to return
                }
        }, null);
       
    }

}
