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
 * @(#)DefaultHandler.java	1.23 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data;

import java.util.Hashtable;

import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.Logger;


/**
 * Handler class which deals with handling unexpected messages
 */
public class DefaultHandler extends ErrHandler 
{
    private static boolean DEBUG = false;

    public void sendError(IMQConnection con, BrokerException ex, Packet pkt) {
        // XXX REVISIT 3/7/00 racer
        // add code to return ERROR message
        // also log message

        logger.logStack(Logger.ERROR, 
            BrokerResources.E_INTERNAL_BROKER_ERROR, 
            "Uncaught Exception", ex);
        
        // send the reply
        sendError(con, pkt, ex.getMessage(), ex.getStatusCode());
   }

   // if we get an uncaught exception, we want to make sure
   // that the reply (if any) is sent back to the consumer
   // so it doesnt hang because of a broker error or because
   // the client sent bad protocol
   public void sendError(IMQConnection con, Packet msg, String emsg, int status) {
       sendError(con, 
                 msg.getSendAcknowledge(), 
                 msg.getPacketType(),
                 msg.getConsumerID(),
                 emsg, status);
   }

   public void sendError(IMQConnection con, boolean sendack, 
                         int pktype, long consumerID,
                         String emsg, int status) {
       if (sendack) {
            Packet pkt = new Packet(con.useDirectBuffers());
            pkt.setPacketType(pktype + 1);
            pkt.setConsumerID(consumerID);
            Hashtable hash = new Hashtable();
            hash.put("JMQStatus", Integer.valueOf(status));
            if (emsg != null) {
                hash.put("JMQReason", emsg);
            }
            pkt.setProperties(hash);
            con.sendControlMessage(pkt);
       }
   }


    /**
     * Method to handle messages we don't recognize. If the message
     * has the 'A' bit set then the client is expecting a reply.
     * By convetion reply packet types are the request packet type + 1.
     */
    public boolean handle(IMQConnection con, Packet msg) throws
            BrokerException
    {
	// Check if A bit is set
	if (msg.getSendAcknowledge()) {
            // 'A' bit is set. Send a NOT_IMPLEMENTED reply
            if (DEBUG) {
                logger.log(Logger.DEBUG,
                    "DefaultHandler: replying to unknown packet type: " +
                    msg.getPacketType());
            }
            Packet pkt = new Packet(con.useDirectBuffers());
            pkt.setPacketType(msg.getPacketType() + 1);
            pkt.setConsumerID(msg.getConsumerID());
            Hashtable hash = new Hashtable();
            hash.put("JMQStatus", Integer.valueOf(Status.NOT_IMPLEMENTED));
            pkt.setProperties(hash);
            con.sendControlMessage(pkt);
	} else {
            // No 'A' bit. Silently ignore
            if (DEBUG) {
                logger.log(Logger.DEBUG,
                    "DefaultHandler: ignoring unknown packet type : " +
                    msg.getPacketType());
            }
        }
        return true;
    }

}
