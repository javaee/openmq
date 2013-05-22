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
 * @(#)MessageManagerConfig.java	1.3 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.HashMap;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;

import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.management.util.ConsumerUtil;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.lists.RemoveReason;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.AdminDataHandler;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.DeleteMessageHandler;
import com.sun.messaging.jmq.jmsserver.data.handlers.admin.ReplaceMessageHandler;
import com.sun.messaging.jmq.util.log.Logger;

public class MessageManagerConfig extends MQMBeanReadWrite  {
    private static MBeanParameterInfo[] deleteMessageSignature = {
	            new MBeanParameterInfo("destinationType", String.class.getName(), 
		                        mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_TYPE)),
	            new MBeanParameterInfo("destinationName", String.class.getName(), 
		                        mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_NAME)),
		    new MBeanParameterInfo("messageID", String.class.getName(),
			                "Message ID")
			    };

    private static MBeanParameterInfo[] replaceMessageSignature = {
	            new MBeanParameterInfo("destinationType", String.class.getName(), 
		                            mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_TYPE)),
	            new MBeanParameterInfo("destinationName", String.class.getName(), 
		                        mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_NAME)),
		    new MBeanParameterInfo("messageID", String.class.getName(),
			                "Message ID"),
		    new MBeanParameterInfo("messageBody", HashMap.class.getName(),
			                "Message Body")
			    };

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo("deleteMessage",
		"Delete a message in a destination",
		    deleteMessageSignature, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),

	    new MBeanOperationInfo("replaceMessage",
		"Replace a message in a destination",
		    replaceMessageSignature, 
		    String.class.getName(),
		    MBeanOperationInfo.ACTION)
		};


    public MessageManagerConfig()  {
	super();
    }

    public void deleteMessage(String destinationType, 
                              String destinationName,
                              String messageID) 
                              throws MBeanException {
	try {
	    if (destinationType == null)  {
		throw new BrokerException(
                "Admin deleteMessage: destination type not specified", 
                Status.BAD_REQUEST);
	    }

            PacketRouter pr = Globals.getPacketRouter(1);
            if (pr == null)  {
                throw new BrokerException(
                "Admin deleteMessage: Could not locate Admin Packet Router");
            }
            AdminDataHandler dhd = (AdminDataHandler)pr.getHandler(PacketType.OBJECT_MESSAGE);
            DeleteMessageHandler hd = (DeleteMessageHandler)dhd.getHandler(
                com.sun.messaging.jmq.util.admin.MessageType.DELETE_MESSAGE);

            hd.deleteMessage(messageID, destinationName, 
                             destinationType.equals(DestinationType.QUEUE));
           
	} catch (Exception e)  {
            String emsg = e.getMessage();
            boolean logstack = true;
            if (e instanceof BrokerException) {
                int status = ((BrokerException)e).getStatusCode();
                if (status == Status.NOT_ALLOWED || status == Status.NOT_FOUND ||
                    status == Status.CONFLICT || status == Status.BAD_REQUEST) {
                    logstack = false;
                } 
            }
            Object[] args = { messageID, destinationName, e.getMessage() };
            emsg = rb.getKString(rb.X_ADMIN_DELETE_MSG, args);
            if (logstack) {
                logger.logStack(Logger.ERROR, emsg, e);
            } else {
                logger.log(Logger.ERROR, emsg, e);
            }
            handleOperationException("deleteMessage", e);
	}
    }

    public String replaceMessage(String destinationType, String destinationName,
                                 String messageID, HashMap messageBody) 
                                 throws MBeanException {
        String newMsgID = null;

        try {
            if (destinationType == null)  {
                throw new BrokerException(
                "Admin replaceMessage: destination name and type not specified",
                Status.BAD_REQUEST);
            }
         
            PacketRouter pr = Globals.getPacketRouter(1);
            if (pr == null)  {
                throw new BrokerException(
                "Admin deleteMessage: Could not locate Admin Packet Router");
            }
            AdminDataHandler dhd = (AdminDataHandler)pr.getHandler(PacketType.OBJECT_MESSAGE);
            ReplaceMessageHandler hd = (ReplaceMessageHandler)dhd.getHandler(
                com.sun.messaging.jmq.util.admin.MessageType.REPLACE_MESSAGE);

            newMsgID = hd.replaceMessage(messageID, destinationName, messageBody,
                           destinationType.equals(DestinationType.QUEUE));
        } catch (Exception e)  {
            String emsg = e.getMessage();
            boolean logstack = true;
            if (e instanceof BrokerException) {
                int status = ((BrokerException)e).getStatusCode();
                if (status == Status.NOT_ALLOWED || status == Status.NOT_FOUND ||
                    status == Status.CONFLICT || status == Status.BAD_REQUEST) {
                    logstack = false;
                }
            }
            Object[] args = { messageID, destinationName, e.getMessage() };
            emsg = rb.getKString(rb.X_ADMIN_DELETE_MSG, args);
            if (logstack) {
                logger.logStack(Logger.ERROR, emsg, e);
            } else {
                logger.log(Logger.ERROR, emsg, e);
            }
            handleOperationException("replaceMessage", e);
	}

	return (newMsgID);
    }

    public String getMBeanName()  {
	return ("MessageManagerConfig");
    }

    public String getMBeanDescription()  {
	return ("Configuration MBean for Message Manager");
	/*
	return (mbr.getString(mbr.I_MSG_MGR_CFG_DESC));
	*/
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (null);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (null);
    }
}
