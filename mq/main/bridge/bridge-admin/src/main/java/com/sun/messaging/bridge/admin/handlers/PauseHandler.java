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

package com.sun.messaging.bridge.admin.handlers;

import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.bridge.api.BridgeException;
import com.sun.messaging.bridge.admin.resources.BridgeManagerResources;
import com.sun.messaging.bridge.admin.BridgeServiceManagerImpl;
import com.sun.messaging.bridge.admin.util.AdminMessageType;
import com.sun.messaging.bridge.admin.resources.BridgeManagerResources;

public class PauseHandler extends AdminCmdHandler
{

    public PauseHandler(AdminMessageHandler parent, BridgeServiceManagerImpl bsm) {
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
                       throws BridgeException,JMSException, Exception {

        int msgtype = msg.getIntProperty(AdminMessageType.PropName.MESSAGE_TYPE);
        if (msgtype != AdminMessageType.Type.PAUSE) {
           throw new BridgeException(_bmr.getKString(_bmr.X_UNEXPECTED_ADMIN_MSG_TYPE,
                                      AdminMessageType.getString(msgtype)));
       }

       String bnameval = msg.getStringProperty(AdminMessageType.PropName.BRIDGE_NAME);
       String btypeval = msg.getStringProperty(AdminMessageType.PropName.BRIDGE_TYPE);
       String lnameval = msg.getStringProperty(AdminMessageType.PropName.LINK_NAME);

       String bname = (bnameval == null ? null: bnameval.trim());
       String btype = (btypeval == null ? null: btypeval.trim().toUpperCase());
       String lname = (lnameval == null ? null: lnameval.trim());

       if (bname != null && lname != null) {
           if (bname.length() == 0) {
               throw new BridgeException(_bmr.getKString(_bmr.E_ADMIN_INVALID_BRIDGE_NAME, bname));
           }
           if (lname.trim().length() == 0) {
               throw new BridgeException(_bmr.getKString(_bmr.E_ADMIN_INVALID_LINK_NAME, lname));
           }
           _bsm.pauseBridge(bname, new String[]{"-ln", lname}, btype);
           parent.sendReply(session, msg, reply, Status.OK, (String)null, bmr);
           return;
       }

       if (lname != null) {  
           throw new BridgeException(_bmr.getKString(_bmr.X_ADMIN_LINK_NAME_NOSUPPORT, msg)); 
       }

       if (bname != null && bname.length() == 0) {
           throw new BridgeException(_bmr.getKString(_bmr.E_ADMIN_INVALID_BRIDGE_NAME, bname));
       }
       _bsm.pauseBridge(bname, null, btype);
       parent.sendReply(session, msg, reply, Status.OK, (String)null, bmr);
       return;
    }

}
