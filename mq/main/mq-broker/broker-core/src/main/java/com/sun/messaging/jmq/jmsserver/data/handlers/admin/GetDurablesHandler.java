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
 * @(#)GetDurablesHandler.java	1.16 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.LinkedHashMap;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.admin.DurableInfo;
import com.sun.messaging.jmq.util.admin.ConsumerInfo;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;

public class GetDurablesHandler extends AdminCmdHandler
{
    private static boolean DEBUG = getDEBUG();

    public GetDurablesHandler(AdminDataHandler parent) {
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
                 cmd_props);
        }
	String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);

	// Send reply
	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);
        int status = Status.OK;

        Vector v = null;
        String err = null;
        try {
                DestinationUID duid = null;
                if (destination != null) 
                    duid= DestinationUID.getUID(
                         destination, false);
    
                Set s = Subscription.getAllSubscriptions(duid);
    
                v = new Vector();
    
                Iterator itr = s.iterator();
                while (itr.hasNext()) {
                    Subscription sub = (Subscription)itr.next();
                    DurableInfo di = new DurableInfo();
                    di.isDurable = sub.isDurable();
                    di.isShared = sub.getShared();
                    di.isJMSShared = sub.getJMSShared();
                    if (di.isDurable) {
                        di.name = sub.getDurableName();
                    } else if (di.isJMSShared) {
                        di.name = sub.getNDSubscriptionName();
                    }
                    di.clientID = sub.getClientID();
                    di.isActive = sub.isActive();
                    di.uidString = String.valueOf(sub.getConsumerUID().longValue());
                    List children = sub.getChildConsumers();
                    di.activeCount = children.size();
                    di.activeConsumers = new LinkedHashMap<String, ConsumerInfo>();
                    Iterator itr1 = children.iterator();
                    while (itr1.hasNext()) {
                        Consumer c = (Consumer)itr1.next();
                        ConsumerInfo cinfo = new ConsumerInfo();
                        cinfo.connection =  new ConnectionInfo();
                        cinfo.connection.uuid = c.getConsumerUID().getConnectionUID().longValue();
                        cinfo.uidString = String.valueOf(c.getConsumerUID().longValue());
                        ConsumerUID uid = c.getStoredConsumerUID();
                        if (uid != null) {
                            cinfo.subuidString = String.valueOf(uid.longValue());
                        }
                        BrokerAddress addr =  c.getConsumerUID().getBrokerAddress();
                        if (addr != null) {
                            cinfo.brokerAddressShortString = addr.getMQAddress().
                                getHostAddressNPort()+
                                (addr.getBrokerID() == null ? "":"["+addr.getBrokerID()+"]");
                        }
                        di.activeConsumers.put(cinfo.uidString,  cinfo);
                    }
                    di.nMessages = sub.numInProcessMsgs();
                    di.consumer = new ConsumerInfo();
                    //Ok, I'm not setting id because it really should be an int, maybe later
                    di.consumer.destination = sub.getDestinationUID().getName();
                    di.consumer.type = DestType.DEST_TYPE_TOPIC;
                    di.consumer.selector = sub.getSelectorStr();
                    // not bothering with the connection this time either
                    di.consumer.connection = null;
                    
                    v.add(di);
                }

        } catch (BrokerException ex) {
            err = ex.getMessage();
            status = ex.getStatusCode();
        }

	setProperties(reply, MessageType.GET_DURABLES_REPLY,
		status, err);

	setBodyObject(reply, v);
	parent.sendReply(con, cmd_msg, reply);
    return true;
    }
}
