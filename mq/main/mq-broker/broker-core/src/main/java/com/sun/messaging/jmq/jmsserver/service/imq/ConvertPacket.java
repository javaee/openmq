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
 * @(#)ConvertPacket.java	1.14 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.handlers.*;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;

/**
 * this class handles converting data from old to new
 * interest types, etc
 * Its special case code (and probably slow) but the idea
 * is to keep the kludgy code centralized
 */



// LKS - XXX update to handle both protocol and packet version

public class ConvertPacket
{
    private Logger logger = Globals.getLogger();

    Hashtable consumer_to_interest = new Hashtable();
    Hashtable interest_to_consumer = new Hashtable();
    Hashtable consumer_to_deliver = new Hashtable();

    IMQConnection con = null;

    int oldversion = 0;
    int targetVersion = 0;

    public ConvertPacket(IMQConnection con, int oldversion, int targetVersion)
    {
        this.con = con;
        this.oldversion = oldversion; 
        this.targetVersion = targetVersion; 
    }


    public void handleReadPacket(Packet msg) {
        // OK .. convert to new version

        // If we are VERSION2 ... dont do anything
        // EXCEPT convert the properties
        if (oldversion == Packet.VERSION2)
            return;
        
        msg.setVersion(targetVersion); 

        int type = msg.getPacketType();
        switch (type) {
            case PacketType.TEXT_MESSAGE:
            case PacketType.BYTES_MESSAGE:
            case PacketType.MAP_MESSAGE:
            case PacketType.STREAM_MESSAGE:
            case PacketType.OBJECT_MESSAGE:
            case PacketType.MESSAGE:
                handleDataRead(msg);
                break;
            case PacketType.DELETE_CONSUMER:
                removeConsumerRequest(msg);
                break;
            case PacketType.ACKNOWLEDGE:
            case PacketType.REDELIVER:
                handleAcknowledgeRead(msg);
                break;
            case PacketType.DELIVER:
                handleDeliverRead(msg);
            default:
        }
    }

    public void handleWritePacket(Packet msg) {

        msg.setVersion(oldversion); 
        if (oldversion == Packet.VERSION2) {
           return;
        }

        int type = msg.getPacketType();
        switch (type) {
            case PacketType.TEXT_MESSAGE:
            case PacketType.BYTES_MESSAGE:
            case PacketType.MAP_MESSAGE:
            case PacketType.STREAM_MESSAGE:
            case PacketType.OBJECT_MESSAGE:
            case PacketType.MESSAGE:
                handleDataWrite(msg);
                break;
            case PacketType.ADD_CONSUMER_REPLY:
                handleConsumerResponse(msg);
                break;
            default:
        }
    }

    static final int OLD_ACK_BLOCK_SIZE =  4 + SysMessageID.ID_SIZE;

    private void handleAcknowledgeRead(Packet msg) {
        if (msg.getTransactionID() != 0) {
                TransactionHandler.convertPacketTid(con, msg);
        }
        
        DataInputStream is = new DataInputStream(
                msg.getMessageBodyStream());
        int size = msg.getMessageBodySize();
        int ackcount = size/OLD_ACK_BLOCK_SIZE;
        int[] clientids = new int[ackcount];
        SysMessageID[] sysids = new SysMessageID[ackcount];
        try {
            for (int i = 0; i < ackcount; i ++) {
                clientids[i] = is.readInt();
                sysids[i] = new SysMessageID();
                sysids[i].readID(is); 
            }
        } catch (IOException ex) {
            logger.log(Logger.INFO,"Internal Error, bad sysmessageid "
                 , ex);
        }             
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // reset the body  XXX - LKS

        try {
            for (int i = 0; i < ackcount; i ++) {
                Long newid = (Long)interest_to_consumer.get(
                                  new Integer(clientids[i]));
                if (newid == null) continue;

                dos.writeLong(newid.longValue());
                sysids[i].writeID(dos); 
            }  
            dos.flush();
            bos.flush();
        } catch (IOException ex) {
            logger.log(Logger.INFO,"Internal Error, unable to convert "
                 + " old packet ", ex);
        }             
        msg.setMessageBody(bos.toByteArray());

        
    }

    // handle transaction
    private void handleDataRead(Packet msg) {
        if (msg.getTransactionID() != 0) {
                TransactionHandler.convertPacketTid(con, msg);
        }
    }

    // handle interest
    private void handleDataWrite(Packet msg) {
        Long newid = new Long(msg.getConsumerID());
        Integer oldid = (Integer)consumer_to_interest.get(newid);
        if (oldid == null) { // try deliver
            oldid = (Integer)consumer_to_deliver.get(newid);
            if (oldid != null && msg.getIsLast())
                consumer_to_deliver.remove(newid);  
        }
        if (oldid == null) { // consumer no longer exists
            Globals.getLogger().log(Logger.DEBUG, 
                   "Throwing out packet, could not find "
                   +"old consumer id for new id " 
                   + newid);
            return; // throw out packet
        }
        msg.setConsumerID((long)oldid.intValue());
    }


    /* map old id -> new ID */
    private void handleConsumerResponse(Packet msg) {
        Hashtable props;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            logger.log(Logger.INFO,"Internal Error, bad propertis "
                 , ex);
            return; // no properties
        }
        Integer intr = (Integer)props.remove("JMQOldConsumerID");
        Long newcid = (Long)props.get("JMQConsumerID");

        /* Map them */
        if (newcid != null && intr != null) {
            consumer_to_interest.put(newcid, intr);
            interest_to_consumer.put(intr, newcid);
        }
    }

   /* map old id -> new ID */
    private void handleDeliverRead(Packet msg) {
        Hashtable props;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            logger.log(Logger.INFO,"Internal Error, bad propertis "
                 , ex);
            return; // no properties
        }

        Integer oldid = (Integer)props.get("JMQConsumerID");

        
        if (oldid != null) {
            ConsumerUID newcid = new ConsumerUID();
            Long longcid = new Long(newcid.longValue());
            props.put("JMQConsumerID", longcid);
            consumer_to_deliver.put(longcid, oldid);
        }
           
    }

    /* map old id -> new ID */
    private void removeConsumerRequest(Packet msg) {
        Hashtable props;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            logger.log(Logger.INFO,"Internal Error, bad propertis "
                 , ex);
            return; // no properties
        }

        Integer oldid = (Integer)props.get("JMQConsumerID");

        if (oldid != null) {
            Long newid = (Long)interest_to_consumer.get(oldid);
            props.put("JMQConsumerID", newid);
            // remove from tables
            consumer_to_interest.remove(newid);
            interest_to_consumer.remove(oldid);
        }
           
    }



}
