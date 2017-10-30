/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.messaging.bridge.admin.handlers;

import java.util.Hashtable;
import java.util.Properties;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.bridge.admin.BridgeServiceManagerImpl;
import com.sun.messaging.bridge.admin.util.AdminMessageType;
import com.sun.messaging.bridge.api.FaultInjection;
import com.sun.messaging.bridge.api.BridgeException;
import com.sun.messaging.bridge.admin.resources.BridgeManagerResources;

/**
 * handler for DEBUG message.
 */
public class DebugHandler extends AdminCmdHandler
{
    public DebugHandler(AdminMessageHandler parent, BridgeServiceManagerImpl bsm) {
        super(parent, bsm);
    }

    /**
     * When called, parent has set reply message type property
     *
     * throw exception if let parent handle sendReply
     */
    public void handle(Session session,
                       ObjectMessage msg, ObjectMessage reply,
                       BridgeManagerResources bmr)
                       throws BridgeException, Exception {
        int msgtype = msg.getIntProperty(AdminMessageType.PropName.MESSAGE_TYPE);
        if (msgtype != AdminMessageType.Type.DEBUG) {
           throw new BridgeException("Unexpected bridge admin message type "+
                                      AdminMessageType.getString(msgtype));
       }

       String debugArg = msg.getStringProperty(AdminMessageType.PropName.CMD_ARG);
       String target = msg.getStringProperty(AdminMessageType.PropName.TARGET);
       if (debugArg == null) {
           throw new BridgeException(_bmr.getKString(_bmr.X_ADMIN_DEBUG_NO_ARG));
       }
       if (!debugArg.trim().equals("fault")) {
           throw new BridgeException(_bmr.getKString(_bmr.X_ADMIN_DEBUG_UNSUPPORTED_ARG, debugArg));
       }
       if (target == null || target.trim().length() == 0) {
           throw new BridgeException(_bmr.getKString(_bmr.X_ADMIN_DEBUG_NO_NAME, debugArg));
       }
       Properties props = (Properties)msg.getObject();

       String faultName = target;
       String faultSelector = (String)props.getProperty("selector");
       FaultInjection fi = FaultInjection.getInjection();
       boolean faultOn = true;

       String enabledStr = props.getProperty("enabled");
       if (enabledStr != null && enabledStr.equalsIgnoreCase("false")) {
           fi.unsetFault(faultName);
       } else {
           fi.setFaultInjection(true);
           try {
               fi.setFault(faultName, faultSelector, props);
           } catch (Exception e) {
               _bc.logError(_bmr.getKString(_bmr.E_ADMIN_SET_FAULT_FAILED, faultName), e);
               throw e;
           }
       }
       parent.sendReply(session, msg, reply, Status.OK, (String)null, bmr);
   }

}
