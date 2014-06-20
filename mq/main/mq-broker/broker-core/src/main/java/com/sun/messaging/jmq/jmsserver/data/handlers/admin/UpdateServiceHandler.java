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
 * @(#)UpdateServiceHandler.java	1.14 07/12/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.io.*;
import java.util.Vector;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAMonitorService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.ServiceInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;

public class UpdateServiceHandler extends AdminCmdHandler
{
    private static boolean DEBUG = getDEBUG();

    public UpdateServiceHandler(AdminDataHandler parent) {
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
				       Hashtable cmd_props) 
    {

        if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                cmd_props);
        }

	ServiceInfo info = (ServiceInfo)getBodyObject(cmd_msg);
        int status = Status.OK;
        String errMsg = null;

	ServiceManager sm = Globals.getServiceManager();
        Service svc = null;

        HAMonitorService hamonitor = Globals.getHAMonitorService(); 
        if (hamonitor != null && hamonitor.inTakeover()) {
            status = Status.ERROR;
            errMsg =  rb.getString(rb.E_CANNOT_PROCEED_TAKEOVER_IN_PROCESS);

            logger.log(Logger.ERROR, this.getClass().getName() + ": " + errMsg);
	} else  {
        if (info.name == null || ((svc= sm.getService(info.name)) == null)) {
            status = Status.ERROR;
            errMsg = rb.getString( rb.X_NO_SUCH_SERVICE, 
                (info.name == null ? "<null>" : info.name));
        }
        }

        // OK .. set the service information
        if (status == Status.OK) {
            if (! (svc instanceof IMQService)) {
                status = Status.ERROR;
                errMsg = "Internal Error: can updated non-standard Service";
            } else {
            // XXX - really we want to do this through properties, I need
            // to repair this by fcs
                try {
                    IMQService stsvc = (IMQService)svc;
                    int port = -1;
                    int min = -1;
                    int max = -1;
                    if (info.isModified(info.PORT)) {
                        port = info.port;
                    }
                    if (info.isModified(info.MIN_THREADS)) {
                        min = info.minThreads;
                    } 
                    if (info.isModified(info.MAX_THREADS)) {
                        max = info.maxThreads;
                    }
                    if (port != -1 || min !=-1 || max != -1) {
                        stsvc.updateService(port, min, max);
                    } else {
                        status = Status.ERROR;
                        errMsg = rb.getString( rb.X_NO_SERVICE_PROPS_SET, 
                            info.name);
                    }
                } catch (Exception ex) {
                    logger.logStack(Logger.WARNING, rb.W_ERROR_UPDATING_SERVICE,
			svc.getName(), ex);
                    status = Status.ERROR;
		    errMsg = getMessageFromException(ex);
                }
            }
        }


	// Send reply
	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

	setProperties(reply,
	    MessageType.UPDATE_SERVICE_REPLY, status, errMsg);

	parent.sendReply(con, cmd_msg, reply);
    return true;
    }
}
