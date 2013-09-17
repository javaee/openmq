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
 * @(#)StartStopHandler.java	1.34 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.jmsserver.core.Session;
import com.sun.messaging.jmq.jmsserver.core.SessionUID;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;

/**
 * Handler class which deals with starting/stoping the delivery of 
 * messages to a specific connection
 */
public class StartStopHandler extends PacketHandler 
{

    Hashtable hash = new Hashtable();

    public StartStopHandler() {
        hash.put("JMQStatus", Integer.valueOf(Status.OK));

    }

    /**
     * Method to handle Start and Stop messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
    {
        Hashtable props = null;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            throw new RuntimeException("Can not load props", ex);
        }
        Long lsid = (props == null ? null :
             (Long)props.get("JMQSessionID"));

        SessionUID suid = (lsid == null ? null :
              new SessionUID(lsid.longValue()));

        int status = Status.OK;
        String reason = null;

        switch (msg.getPacketType()) {
            case PacketType.START:
                if (suid != null) {
                    boolean bad = false;
                    Session s= Session.getSession(suid);
                    if (s != null && 
                        !s.getConnectionUID().equals(con.getConnectionUID())) {
                        bad = true;
                    }
                    // OK .. the client should never be sending us
                    // a bad session ID, but in reconnect it sometimes
                    // does
                    // handle it gracefully if the client does the 
                    // wrong thing
                    if (s == null) {
                       status = Status.ERROR;
                       String[] args = { ""+suid, "START-SESSION", con.toString() };
                       reason = Globals.getBrokerResources().getKString(
                                BrokerResources.W_RECEIVED_UNKNOWN_SESSIONID, args);
                       logger.log(Logger.WARNING, reason);
                    } else if (bad) {
                       status = Status.ERROR;
                       String[] args = { ""+suid, "START-SESSION", con.toString(), 
                                         s.getConnectionUID().toString() };
                       reason = Globals.getBrokerResources().getKString(
                                BrokerResources.W_RECEIVED_BAD_SESSIONID, args);
                       logger.log(Logger.WARNING, reason);
                    } else {
                        s.resume("START_STOP");
                    }
                } else {
                    con.startConnection();
                }
                break;
            case PacketType.STOP:
                if (suid != null) {
                    boolean bad = false;
                    Session s= Session.getSession(suid);
                    if (s != null && 
                        !s.getConnectionUID().equals(con.getConnectionUID())) {
                        bad = true;
                    }
                    // OK .. the client should never be sending us
                    // a bad session ID, but in reconnect it sometimes
                    // does
                    // handle it gracefully if the client does the 
                    // wrong thing
                    if (s == null) {
                       status = Status.ERROR;
                       String[] args = { ""+suid, "STOP-SESSION", con.toString() };
                       reason = Globals.getBrokerResources().getKString(
                                BrokerResources.W_RECEIVED_UNKNOWN_SESSIONID, args);
                       logger.log(Logger.WARNING, reason);
                    } else if (bad) {
                       status = Status.ERROR;
                       String[] args = { ""+suid, "STOP-SESSION", con.toString(), 
                                         s.getConnectionUID().toString() };
                       reason = Globals.getBrokerResources().getKString(
                                BrokerResources.W_RECEIVED_BAD_SESSIONID, args);
                       logger.log(Logger.WARNING, reason);
                    } else {
                         s.pause("START_STOP");
                    }
                } else {
                    con.stopConnection();
                }
                Packet pkt = new Packet(con.useDirectBuffers());
                pkt.setPacketType(PacketType.STOP_REPLY);
                pkt.setConsumerID(msg.getConsumerID());
                if (((IMQBasicConnection)con).getDumpPacket() ||
                      ((IMQBasicConnection)con).getDumpOutPacket())
                    hash.put("JMQReqID", msg.getSysMessageID().toString());

                hash.put("JMQStatus", Integer.valueOf(status));
                if (reason != null)
                    hash.put("JMQReason", reason);

                pkt.setProperties(hash);
                con.sendControlMessage(pkt);
                break;
        }
        return true;
    }

}
