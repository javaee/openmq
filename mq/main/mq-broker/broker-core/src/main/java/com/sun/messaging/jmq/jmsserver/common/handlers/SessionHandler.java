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
 * @(#)SessionHandler.java	1.19 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.io.PacketUtil;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Session;
import com.sun.messaging.jmq.jmsserver.core.SessionUID;



public class SessionHandler extends PacketHandler 
{
    private Logger logger = Globals.getLogger();
    private static boolean DEBUG = false;
  

    public SessionHandler() {
    }


    public Session createSession(int stype, String creator, IMQConnection con,
            boolean isIndemp)
        throws BrokerException
    {
        Session session = null;
        if (isIndemp) {
            session = Session.getSession(creator);
        }
        if (session == null) {
            session = Session.createSession(con.getConnectionUID(),
                                            creator, coreLifecycle);
            session.setAckType(stype);
            con.attachSession(session);
        }
        return session;
     }

     public void closeSession(SessionUID sessionID, IMQConnection con, boolean isIndemp)
        throws BrokerException
     {
         if (!isIndemp || Session.getSession(sessionID) != null) {
             assert con.getSession(sessionID) != null;

             Session.closeSession(sessionID);
             con.closeSession(sessionID);
         }
     }


    /**
     * Method to handle Session(add or delete) messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
    {
        int status = Status.OK;
        String reason = null;
        Hashtable hash = new Hashtable(); // return props
        boolean isIndemp = msg.getIndempotent();

        try {
            Hashtable props = msg.getProperties();

            Session session = null;
            if (msg.getPacketType() == PacketType.CREATE_SESSION) {
               Integer  ack = (props == null ? null : 
                    (Integer)props.get("JMQAckMode"));
               // if we dont know, treat like client ack
               int stype = (ack == null ? Session.NONE
                      : ack.intValue());

               session = createSession(stype, msg.getSysMessageID().toString(),
                    con, isIndemp);
               hash.put("JMQSessionID", new Long(
                    session.getSessionUID().longValue()));

            } else {
                assert msg.getPacketType() == PacketType.DESTROY_SESSION;
                Long lsessionid = (Long)props.get("JMQSessionID");
                if (lsessionid == null) {
                    throw new BrokerException(
                        Globals.getBrokerResources().getString(
                             BrokerResources.X_INTERNAL_EXCEPTION,
                             "protocol error, no session"));
                }
                SessionUID sessionID = new SessionUID(
                       lsessionid.longValue());

                closeSession(sessionID, con, isIndemp);

            }
        } catch (Exception ex) {
            boolean log = false;
            reason = ex.getMessage();
            if (ex instanceof BrokerException) {
                status = ((BrokerException)ex).getStatusCode();
                log = false;
            } else {
                status = Status.ERROR;
                log = true;
            }

            if (log) {
                logger.logStack(Logger.INFO,
                   Globals.getBrokerResources().getString(
                      BrokerResources.X_INTERNAL_EXCEPTION,
                        " session "),ex);
            } else {
                logger.log(Logger.INFO, ex.getMessage());
            }
        }
            
        hash.put("JMQStatus", new Integer(status));
        if (reason != null)
            hash.put("JMQReason", reason);

        if (msg.getSendAcknowledge()) {
            Packet pkt = new Packet(con.useDirectBuffers());
            pkt.setConsumerID(msg.getConsumerID()); // correlation ID
            pkt.setPacketType(msg.getPacketType()+1);
            pkt.setProperties(hash);
            con.sendControlMessage(pkt);
        }

        return true;


    }


}
