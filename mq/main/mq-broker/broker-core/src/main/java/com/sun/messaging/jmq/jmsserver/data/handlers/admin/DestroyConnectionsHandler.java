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
 * @(#)DestroyConnectionsHandler.java	1.10 07/12/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import com.sun.messaging.jmq.util.GoodbyeReason;
import java.util.Hashtable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAMonitorService;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.util.MetricCounters;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.net.IPAddress;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.Service;

public class DestroyConnectionsHandler extends AdminCmdHandler
{

    private static boolean DEBUG = getDEBUG();

    public DestroyConnectionsHandler(AdminDataHandler parent) {
	super(parent);
    }

    /**
     * Handle the incomming administration message.
     *
     * @param con	The Connection the message came in on.
     * @param cmd_msg	The administration message
     * @param cmd_props The properties from the administration message
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
				       Hashtable cmd_props) {

	if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                "DestroyConnections: " + cmd_props);
        }

        ConnectionManager cm = Globals.getConnectionManager();

	String serviceName = (String)cmd_props.get(MessageType.JMQ_SERVICE_NAME);
	Long cxnId = (Long)cmd_props.get(MessageType.JMQ_CONNECTION_ID);

        int status = Status.OK;
        String errMsg = null;

        Service s = null;


        HAMonitorService hamonitor = Globals.getHAMonitorService(); 
        if (hamonitor != null && hamonitor.inTakeover()) {
            status = Status.ERROR;
            errMsg =  rb.getString(rb.E_CANNOT_PROCEED_TAKEOVER_IN_PROCESS);

            logger.log(Logger.ERROR, this.getClass().getName() + ": " + errMsg);
	}

        if (status == Status.OK) {

            ConnectionInfo cxnInfo = null;
            IMQConnection  cxn = null;
            if (cxnId != null) {

                logger.log(Logger.INFO, BrokerResources.I_DESTROY_CXN,
                       String.valueOf(cxnId.longValue()));
                // Get info for one connection
                cxn = (IMQConnection)cm.getConnection(
                                new ConnectionUID(cxnId.longValue()));
                if (cxn != null) {
                    if (DEBUG) {
                        cxn.dump();
                    }
                    cxn.destroyConnection(true, GoodbyeReason.ADMIN_KILLED_CON, 
                       Globals.getBrokerResources().getKString(
                       BrokerResources.M_ADMIN_REQ_CLOSE));
                } else {
                    status = Status.NOT_FOUND;
                    errMsg = rb.getString(rb.E_NO_SUCH_CONNECTION,
                        String.valueOf(cxnId.longValue()));
                }
            }
        }

	// Send reply
	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

	setProperties(reply, MessageType.DESTROY_CONNECTION_REPLY,
		status, errMsg);

	parent.sendReply(con, cmd_msg, reply);
        return true;
    }


}
