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
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Set;
import java.util.Hashtable;
import java.util.Properties;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.cluster.api.ClusterManager;
import com.sun.messaging.jmq.io.MQAddress;

public class UpdateClusterBrokerListHandler extends AdminCmdHandler
{
    private static boolean DEBUG = getDEBUG();

    public UpdateClusterBrokerListHandler(AdminDataHandler parent) {
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

        int status = Status.OK;
        String msg = null;

        if (DEBUG) {
            logger.log(Logger.INFO, this.getClass().getName()+": "+cmd_props);
        }

        if (Globals.getHAEnabled()) {
            status = Status.ERROR;
            msg =  rb.getKString(rb.E_OP_NOT_APPLY_TO_HA_BROKER, 
                   MessageType.getString(MessageType.UPDATE_CLUSTER_BROKERLIST));
            logger.log(Logger.ERROR, msg);

        } else if (!Globals.isJMSRAManagedBroker()) {
            status = Status.ERROR;
            msg =  rb.getKString(rb.E_BROKER_NOT_JMSRA_MANAGED_IGNORE_OP, 
                   MessageType.getString(MessageType.UPDATE_CLUSTER_BROKERLIST));
            logger.log(Logger.ERROR, msg);
            msg = "BAD REQUEST";

        } else  {
             try {
                 ClusterManager cm = Globals.getClusterManager();
                 MQAddress self = cm.getMQAddress();
                 String brokerlist = (String)cmd_props.get(MessageType.JMQ_CLUSTER_BROKERLIST);
                 Set brokers = cm.parseBrokerList(brokerlist);
                 MQAddress master = (cm.getMasterBroker() == null ? 
                                     null:cm.getMasterBroker().getBrokerURL());
                 logger.log(logger.INFO, rb.getKString(rb.I_UPDATE_BROKERLIST, 
                     self+(master == null ?"]":"("+cm.CONFIG_SERVER+"="+master+")"),
                     "["+brokerlist+"]"));
                 if (master != null && !brokers.contains(master)) {
                     msg = rb.getKString(rb.X_REMOVE_MASTERBROKER_NOT_ALLOWED,
                               master.toString(), brokers.toString()+"["+brokerlist+"]");
                     throw new BrokerException(msg);
                 }
                 if (!brokers.contains(self)) {
                     brokerlist = "";
                 }
                 Properties prop = new Properties(); 
                 prop.put(cm.AUTOCONNECT_PROPERTY, brokerlist);
                 BrokerConfig bcfg = Globals.getConfig();
                 bcfg.updateProperties(prop, true);
             } catch (PropertyUpdateException e) {
                 status = Status.BAD_REQUEST;
                 msg = e.getMessage();
                 logger.log(Logger.WARNING, msg);
             } catch (Exception e) {
                 status = Status.ERROR;
                 msg = e.toString();
                 logger.log(Logger.WARNING, msg);
             }
         }

         // Send reply
	     Packet reply = new Packet(con.useDirectBuffers());
	     reply.setPacketType(PacketType.OBJECT_MESSAGE);

	     setProperties(reply, MessageType.UPDATE_CLUSTER_BROKERLIST_REPLY, status, msg);
         parent.sendReply(con, cmd_msg, reply);

         return true;
    }
}
