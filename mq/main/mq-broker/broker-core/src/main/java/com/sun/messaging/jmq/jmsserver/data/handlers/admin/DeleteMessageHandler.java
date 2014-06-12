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
 * @(#)DeleteMessageHandler.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.nio.ByteBuffer;
import javax.jms.*;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.util.lists.RemoveReason;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class DeleteMessageHandler extends AdminCmdHandler  {
    private static boolean DEBUG = getDEBUG();

    public DeleteMessageHandler(AdminDataHandler parent) {
        super(parent);
    }

    /**
     * Handle the incomming administration message.
     *
     * @param con    The Connection the message came in on.
     * @param cmd_msg    The administration message
     * @param cmd_props The properties from the administration message
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
                       Hashtable cmd_props) {

        if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                            "Getting messages: " + cmd_props);
        }

        int status = Status.OK;
        String errMsg = null;

        String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer destType = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);
        String msgID = (String)cmd_props.get(MessageType.JMQ_MESSAGE_ID);

	if (destType == null)  {
            errMsg = "DELETE_MESSAGE: destination type not specified";
            logger.log(Logger.ERROR, errMsg);
            status = Status.BAD_REQUEST;
	}
        if (status == Status.OK) { 
            try {
                deleteMessage(msgID, destination, DestType.isQueue(destType.intValue()));
            } catch (Exception e) {
                status = Status.ERROR;
                errMsg= e.getMessage();
                boolean logstack = true;
                if (e instanceof BrokerException) {
                    status = ((BrokerException)e).getStatusCode();
                    if (status == Status.NOT_ALLOWED || status == Status.NOT_FOUND || 
                        status == Status.CONFLICT || status == Status.BAD_REQUEST) {
                        logstack = false;
                    }
                }
                Object[] args = { ""+msgID, ""+destination, e.getMessage() };
                errMsg = rb.getKString(rb.X_ADMIN_DELETE_MSG, args);
                if (logstack) {
                    logger.logStack(Logger.ERROR, errMsg, e);
                } else {
                    logger.log(Logger.ERROR, errMsg, e);
                }
            }
        }
        // Send reply
        Packet reply = new Packet(con.useDirectBuffers());
        reply.setPacketType(PacketType.OBJECT_MESSAGE);
        setProperties(reply, MessageType.DELETE_MESSAGE_REPLY, status, errMsg);
        parent.sendReply(con, cmd_msg, reply);
        return true;
    }

    public void deleteMessage(String msgID,  String destination, boolean isQueue) 
    throws BrokerException, IOException {

	if (destination == null)  {
            String emsg = "DELETE_MESSAGE: destination name not specified";
            throw new BrokerException(emsg, Status.BAD_REQUEST);
	}

	if (msgID == null)  {
            String emsg = "DELETE_MESSAGE: Message ID not specified";
            throw new BrokerException(emsg, Status.BAD_REQUEST);
	}

        Destination[] ds = DL.getDestination(null, destination, isQueue);
        Destination d = ds[0]; //PART
        if (d == null) {
            String emsg = "DELETE_MESSAGE: "+
                           rb.getString(rb.X_DESTINATION_NOT_FOUND, destination);
            throw new BrokerException(emsg, Status.NOT_FOUND);
        }
        if (DEBUG) {
            d.debug();
        }

        logger.log(Logger.INFO, rb.getKString(
                   rb.I_ADMIN_DELETE_MESSAGE, msgID, d.getDestinationUID()));

        SysMessageID sysMsgID = SysMessageID.get(msgID);
        PacketReference pr = DL.get(d.getPartitionedStore(), sysMsgID);
        if (pr == null)  {
            String emsg = "Could not locate message " + msgID+
                          " in destination " + destination;
            throw new BrokerException(emsg, Status.NOT_FOUND);
        }
        if (!pr.isLocal()) {
            Object[] args = { msgID, d.getDestinationUID(), pr.getBrokerAddress() };
            String emsg = rb.getKString(rb.E_ADMIN_DELETE_REMOTE_MSG, args);
            throw new BrokerException(emsg, Status.NOT_ALLOWED);
        }

        Destination.RemoveMessageReturnInfo ret = 
            d.removeMessageWithReturnInfo(sysMsgID, RemoveReason.REMOVE_ADMIN);
        if (ret.inreplacing) {
            String emsg = rb.getKString(rb.E_DELETE_MSG_IN_REPLACING, 
                                        msgID, d.getDestinationUID()); 
            throw new BrokerException(emsg, Status.CONFLICT);
        }
        if (ret.indelivery) {
            String emsg = rb.getKString(rb.X_ADMIN_DELETE_MSG_INDELIVERY, 
                                        msgID, d.getDestinationUID()); 
            throw new BrokerException(emsg, Status.CONFLICT);
        }
        logger.log(logger.INFO, rb.getKString(rb.I_ADMIN_DELETED_MESSAGE, 
                                    msgID, d.getDestinationUID()));  
    }
}
