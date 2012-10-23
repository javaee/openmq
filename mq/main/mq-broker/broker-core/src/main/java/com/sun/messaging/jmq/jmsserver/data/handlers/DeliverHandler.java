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
 * @(#)DeliverHandler.java	1.30 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationList;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.io.PacketUtil;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;

import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;



/**
 * Handler class which deals with requests delivering messages
 */
public class DeliverHandler extends PacketHandler 
{
    // An Ack block is a 4 byte interest ID and a SysMessageID
    static final int DELIVER_BLOCK_SIZE =  SysMessageID.ID_SIZE;

    private Logger logger = Globals.getLogger();
    private static boolean DEBUG = false;
    private DestinationList DL = Globals.getDestinationList();

    public DeliverHandler() {
    }

    /**
     * Method to handle DELIVER  messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
   {

        String reason = null;
        Hashtable props = null;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            logger.log(Logger.INFO,"Internal Error: unable to retrieve "+
                " properties from deliver message " + msg, ex);
            props = new Hashtable();

        }

        int size = msg.getMessageBodySize();
        int ackcount = size/SysMessageID.ID_SIZE;
        int mod = size%SysMessageID.ID_SIZE;


        if (ackcount == 0 ) {
            throw new BrokerException(Globals.getBrokerResources().getString(
                BrokerResources.X_INTERNAL_EXCEPTION,"Empty Deliver Message"));
        }
        if (mod != 0) {
            throw new BrokerException(Globals.getBrokerResources().getString(
                BrokerResources.X_INTERNAL_EXCEPTION,"Invalid Deliver Message Size: " + size +
		". Not multiple of " + SysMessageID.ID_SIZE));
        }

        if (DEBUG) {
            logger.log(Logger.DEBUG,"Deliver Message: processing message {0} {1}",
                     msg.toString(), 
                     con.getConnectionUID().toString());
        }

        Long lid = (Long)props.get("JMQConsumerID");

        assert lid != null;
       
        long id = lid == null ? (long)0 : lid.longValue();

        assert id != 0;

        DataInputStream is = new DataInputStream(
		msg.getMessageBodyStream());

        Packet[] sentp = new Packet[ackcount];

        int sentPackets = 0; // actual # packets sent

        try {
            for (int i = 0; i < ackcount; i ++ ) {
                SysMessageID sysid = new SysMessageID();
                sysid.readID(is); 

                PacketReference ref = DL.get(con.getPartitionedStore(), sysid);

                Packet realp = (ref == null ? 
                      null : ref.getPacket());

                if (ref != null && !ref.isInvalid() && realp != null) {
                    //XXX revisit if this should not be 
                    // using a packet (queued instead)
                    Packet p = new Packet(con.useDirectBuffers());
                    p.fill(realp);
                    p.setConsumerID(id);
                    sentp[sentPackets] = p;
                    sentPackets++;
                }
            }
        } catch (Exception ex) {

            logger.logStack(Logger.ERROR,
                  Globals.getBrokerResources().getString(
                   BrokerResources.X_INTERNAL_EXCEPTION,
                  "\tackcnt = " + ackcount + "\n"
                 + PacketUtil.dumpPacket(msg) + "\n"
                 + "\t" + PacketUtil.dumpThrowable(ex)), ex);

            assert false ;
        }

        // OK .. time to set the lbit on the message
        int status = Status.OK;
        try {
            if (sentPackets > 0 ) {
                assert sentp[sentPackets-1] != null;
                sentp[sentPackets-1].setIsLast(true);

             } else {
                reason = "NOT FOUND";
                status= Status.NOT_FOUND;
             }
               

        } catch (Exception ex) {
            logger.logStack(Logger.ERROR,
                    Globals.getBrokerResources().getString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    "\tackcnt = " + ackcount + "\n"
                    + PacketUtil.dumpPacket(msg) + "\n"
                    + "\t" + PacketUtil.dumpThrowable(ex)), ex);
            
            assert false;
            reason = ex.getMessage();
            status = Status.ERROR;
            if (ex instanceof BrokerException)
                status = ((BrokerException)ex).getStatusCode();
        }

        // do we need to create a reply packet each time ?

        Packet pkt = new Packet(con.useDirectBuffers());
        pkt.setConsumerID(msg.getConsumerID());
        pkt.setPacketType(PacketType.DELIVER_REPLY);
        Hashtable hash = new Hashtable();
        hash.put("JMQStatus", new Integer(status));
        if (reason != null)
            hash.put("JMQReason", reason);
        if (((IMQBasicConnection)con).getDumpPacket() ||
            ((IMQBasicConnection)con).getDumpOutPacket())
            hash.put("JMQReqID", msg.getSysMessageID().toString());


        pkt.setProperties(hash);
        con.sendControlMessage(pkt);


        // before 3.5, messages were queued on the connection
        // however -> this means that browsing a queue
        // could never work if a connection is paused
        // 3.5 and beyond, messages are place on the control
        // queue

        for (int j =0; j < sentPackets; j ++) {
            assert sentp[j] != null;
            if (sentp[j] != null)
                con.sendControlMessage(sentp[j]);
        }

        return true;
    }

}
