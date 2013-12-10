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
 * @(#)AdminCmdHandler.java	1.25 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.DestinationList;
import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;

public class AdminCmdHandler
{
    AdminDataHandler parent = null;

    private static boolean DEBUG = false;
    protected DestinationList DL = Globals.getDestinationList();

    public static boolean getDEBUG() {
        return DEBUG;
    }

    protected Logger logger = Globals.getLogger();
    //protected BrokerConfig props = Globals.getConfig();
    protected BrokerResources rb = Globals.getBrokerResources();

    public AdminCmdHandler() {
    }

    public AdminCmdHandler(AdminDataHandler parent) {
	this.parent = parent;
    }

    /**
     * Default handler. Just replies to the message with the
     * correct reply message type and a status of "Not Implemented"
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
				       Hashtable cmd_props) {

	Integer n = (Integer)cmd_props.get(MessageType.JMQ_MESSAGE_TYPE);

	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

	// By convention reply message is the message type + 1
	setProperties(reply, n.intValue() + 1, Status.ERROR, "Not Implemented");

	parent.sendReply(con, cmd_msg, reply);
        return true;
    }

    public static void setProperties(Packet pkt,
        int message_type, int status, String error_string) {
        setProperties(pkt, message_type, status,
             error_string, null);
    }

    public static void setProperties(Packet pkt,
        int message_type, int status, String error_string,
         Hashtable addprops) {

	Hashtable props = null;
        if (addprops != null) {
            props =new Hashtable(addprops);
        } else {
            props =new Hashtable();
        }

	props.put(MessageType.JMQ_MESSAGE_TYPE, Integer.valueOf(message_type));
	props.put(MessageType.JMQ_STATUS, Integer.valueOf(status));
	if (error_string != null) {
	    props.put(MessageType.JMQ_ERROR_STRING, error_string);
	}
	pkt.setProperties(props);
    }

    /**
     * Get object from the body of a packet
     */
    protected Object getBodyObject(Packet pkt) {

	ObjectInputStream ois = null;
	Object o = null;

	// Extract the object from the message body
	try {
	    ois = new ObjectInputStream(pkt.getMessageBodyStream());
	    o = ois.readObject();
        } catch (Exception e) {
	    // Programing error. Do not need to localize
	    logger.logStack(Logger.ERROR, rb.E_INTERNAL_BROKER_ERROR,
                this.getClass().getName() +
	        " : Got exception reading body of administration message:\n" +
		e + "\n" +
		pkt.dumpPacketString(), e);
	} finally {
            if (ois != null) { 
                try {
                ois.close();
                } catch (Exception e) {
                /* ignore */
                }
            }
        }

	return o;
    }

    /**
     * Set an object into the body of a packet
     */
    protected void setBodyObject(Packet pkt, Object o) {

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	try {
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(o);
	    oos.close();
	    pkt.setMessageBody(bos.toByteArray());
	} catch (Exception e) {
	    // Programing error. Do not need to localize
	    logger.logStack(Logger.ERROR, rb.E_INTERNAL_BROKER_ERROR,
	        this.getClass().getName() +
	        " : Got exception writing Vector to admin reply message:\n" +
		e + "\n" + o.toString(), e);
	}
    }

    /**
     * Get a message from an Exception. This basically checks
     * if the exception is a BrokerException and properly formats
     * the linked exceptions message. The string returned does
     * NOT include the exception name.
     */
    public static String getMessageFromException(Exception e) {

	String m = e.getMessage();

	if (e instanceof BrokerException) {
            Throwable root_ex = ((BrokerException)e).getCause();
            if (root_ex == null) return m; // no root cause
	    String lm = root_ex.getMessage();

	    if (lm != null) {
	        m = m + "\n" + lm;
            }
	}
        return m;
    }

    public void waitForHandlersToComplete(int secs)
    {
        parent.waitForHandlersToComplete(secs);
    }
    
    
    public String getDestinationType(int mask) {
		if (DestType.isTopic(mask)) {
			return rb.getString(BrokerResources.M_TOPIC);
		} else if (DestType.isQueue(mask)) {
			return rb.getString(BrokerResources.M_QUEUE);
		} else {
			return "?????";
		}

	}
}
