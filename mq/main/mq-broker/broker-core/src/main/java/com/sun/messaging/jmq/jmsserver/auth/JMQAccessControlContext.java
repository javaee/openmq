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
 * @(#)JMQAccessControlContext.java	1.20 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth;

import java.util.Properties;
import java.util.Set;
import java.security.Principal;
import java.security.AccessControlException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.jaas.MQUser;
import com.sun.messaging.jmq.auth.api.server.*;
import com.sun.messaging.jmq.auth.api.server.model.AccessControlModel;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.Globals;

/**
 * JMQ AccessControlContext uses AccessControlModel interface
 * for permission checks agaist a access control model 
 */
public class JMQAccessControlContext implements AccessControlContext
{
    private MQUser mquser;
    private Subject subject;
    private Properties authProps;
    private AccessControlModel acs = null;


    public JMQAccessControlContext(MQUser mquser, Subject subject,
                                   Properties authProperties)
                                   throws LoginException {
        this.mquser = mquser;
        this.subject = subject;
        authProps = authProperties;
        String acEnabled = authProps.getProperty(
                AccessController.PROP_ACCESSCONTROL_ENABLED);
        if (acEnabled != null && acEnabled.equals("false")) {
            return; 
        }
        try {
        loadAccessControlModel();
        } catch (AccessControlException e) {
        throw new LoginException(e.getMessage());
        }
    }

    private void loadAccessControlModel() throws AccessControlException {
        String type = authProps.getProperty(AccessController.PROP_ACCESSCONTROL_PREFIX+"type");
        if (type == null || type.trim().equals("")) {
        throw new AccessControlException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_ACCESSCONTROL_TYPE_NOT_DEFINED));
        }
        String cn = authProps.getProperty(AccessController.PROP_ACCESSCONTROL_PREFIX+type+".class");
        if (cn == null) {
        throw new AccessControlException(
                Globals.getBrokerResources().getKString(
                    BrokerResources.X_ACCESSCONTROL_CLASS_NOT_DEFINED, type));
        }
        try {
        acs = (AccessControlModel)Class.forName(cn).newInstance();
        acs.initialize(type, authProps);
        }
        catch (ClassNotFoundException e) {
            throw new AccessControlException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"ClassNotFoundException: "+e.getMessage()));
        }
        catch (InstantiationException e) {
            throw new AccessControlException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"InstantiationExcetpion: "+e.getMessage()));
        }
        catch (IllegalAccessException e) {
            throw new AccessControlException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"IllegalAccessException: "+e.getMessage()));
        }
        catch (ClassCastException e) {
            throw new AccessControlException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"ClassCastException: "+e.getMessage()));
        }
    }

    public Principal getClientUser() {
        return mquser;
    }

    public Subject getSubject() {
        return subject;
    }

    /**
     * This method is always called for ADMIN service regardless jmq.accesscontrol
     */
    public void checkConnectionPermission(String serviceName,
                                          String serviceType)
                                          throws AccessControlException {
    if (serviceType.equals("ADMIN")) {
        String acEnabled = authProps.getProperty(
                               AccessController.PROP_ACCESSCONTROL_ENABLED);
        if (acEnabled != null && acEnabled.equals("false")) {
            Class mqadminc = null;
            try {
            mqadminc = Class.forName("com.sun.messaging.jmq.auth.jaas.MQAdminGroup"); 
            } catch (ClassNotFoundException e) {
            throw new AccessControlException(Globals.getBrokerResources().getKString(
            BrokerResources.X_INTERNAL_EXCEPTION, "ClassNotFoundException: "+e.getMessage()));
            }
            Set s = subject.getPrincipals(mqadminc);

            if (s == null || s.size() == 0) {
                throw new AccessControlException(
                    Globals.getBrokerResources().getKString(
				    BrokerResources.X_NOT_ADMINISTRATOR, mquser.getName()));
            }
            return;
        }
    }
    if (acs == null) {
        loadAccessControlModel();
    }
    acs.checkConnectionPermission(mquser, serviceName, serviceType, subject);
    }

    public void checkDestinationPermission(String serviceName,
                                           String serviceType,
                                           String operation,
                                           String destination,
                                           String destinationType)
                                           throws AccessControlException {
    if (acs == null) {
        loadAccessControlModel();
    }
    acs.checkDestinationPermission(mquser, serviceName, serviceType, subject,
                                   operation, destination, destinationType);
    }

}
