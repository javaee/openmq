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
 * @(#)HelloHandler.java	1.10 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.io.*;
import java.util.Vector;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.bridge.api.BridgeServiceManager;

public class HelloHandler extends AdminCmdHandler
{

    private static boolean DEBUG = getDEBUG();

    public HelloHandler(AdminDataHandler parent) {
	super(parent);
    }

    public boolean handle(IMQConnection con, Packet cmd_msg,
				       Hashtable cmd_props) {

	if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                "Got Hello: " + cmd_props);
        }

	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

	Hashtable props = new Hashtable();
	props.put(MessageType.JMQ_MESSAGE_TYPE,
		  new Integer(MessageType.HELLO_REPLY));

	props.put(MessageType.JMQ_INSTANCE_NAME, Globals.getConfigName());
	props.put(MessageType.JMQ_STATUS, new Integer(Status.OK));
     
    try {
        if (cmd_msg.getDestination().equals(MessageType.JMQ_BRIDGE_ADMIN_DEST)) {

            BridgeServiceManager bsm = null;
            if (!Globals.bridgeEnabled()) {
                String emsg = rb.getKString(rb.W_BRIDGE_SERVICE_NOT_ENABLED);
                logger.log(Logger.WARNING, emsg);
	            props.put(MessageType.JMQ_STATUS, new Integer(Status.UNAVAILABLE));
                props.put(MessageType.JMQ_ERROR_STRING, emsg);
            } else { 
                bsm = Globals.getBridgeServiceManager();
                if (bsm == null || !bsm.isRunning()) {
                    String emsg = rb.getKString(rb.W_BRIDGE_SERVICE_MANAGER_NOT_RUNNING);
                    logger.log(Logger.WARNING, emsg);
	                props.put(MessageType.JMQ_STATUS, new Integer(Status.UNAVAILABLE));
                    props.put(MessageType.JMQ_ERROR_STRING, emsg);
                } else {
                    reply.setReplyTo(bsm.getAdminDestinationName());
                    reply.setReplyToClass(bsm.getAdminDestinationClassName());
                }
            }
        }
    } catch (Exception e) {
        String emsg = "XXXI18N in processing admin message: "+e.getMessage();
        logger.logStack(Logger.ERROR, emsg, e); 
        props.put(MessageType.JMQ_STATUS, new Integer(Status.ERROR));
        props.put(MessageType.JMQ_ERROR_STRING, emsg);
    }
	reply.setProperties(props);

	parent.sendReply(con, cmd_msg, reply);
    return true;
    }
}
