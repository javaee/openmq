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
 * @(#)GoodbyeHandler.java	1.41 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.io.IOException;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.util.timer.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.util.GoodbyeReason;

import java.util.*;



/**
 * Handler class which deals with a "goodbye" message which is sent 
 * when a client quits talking to the broker
 */
public class GoodbyeHandler extends PacketHandler 
{

    protected static long timeout = Globals.getConfig().getLongProperty(
             Globals.IMQ + ".goodbye.timeout", 0);

    ConnectionManager conlist = null;

    public GoodbyeHandler(ConnectionManager mgr) {
        conlist = mgr;
        timeout = Globals.getConfig().getLongProperty(
             Globals.IMQ + ".goodbye.timeout", 0);
        GoodbyeTask.initialize(timeout);
    }

    /**
     * Method to handle goodbye messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
    {
        Hashtable props = null;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            logger.logStack(Logger.WARNING, "GOODBY Packet.getProperties()",ex);
            props = new Hashtable();
        }

        boolean notAuthenticated = !con.isAuthenticated();
        if (con.isValid() && notAuthenticated) {
            logger.log(Logger.WARNING,  Globals.getBrokerResources().getKString(
                 BrokerResources.W_RECEIVED_GOODBYE_UNAUTHENTICATED_CONN,
                 con.getConnectionUID().longValue()+"["+con.getRemoteConnectionString()+"]"));
        }

        Boolean blockprop = (props != null ?(Boolean)props.get("JMQBlock") : null);
        boolean block = (blockprop != null && blockprop.booleanValue());

        // send the reply (if necessary)
        con.stopConnection();
        if (block) {
            con.cleanupConnection();
        }
        boolean destroy = false;
        if (msg.getSendAcknowledge()) {
             Packet pkt = new Packet(con.useDirectBuffers());
             pkt.setPacketType(PacketType.GOODBYE_REPLY);
             pkt.setConsumerID(msg.getConsumerID());
             Hashtable hash = new Hashtable();
             hash.put("JMQStatus", new Integer(Status.OK));
             if (((IMQBasicConnection)con).getDumpPacket() ||
                 ((IMQBasicConnection)con).getDumpOutPacket())
                 hash.put("JMQReqID", msg.getSysMessageID().toString());
             pkt.setProperties ( hash );
             con.sendControlMessage(pkt);
             // increase timeout 
             if (con.isBlocking()) {
		 if (con instanceof IMQBasicConnection)  {
		     IMQBasicConnection ipCon = (IMQBasicConnection)con;
                     ipCon.flushControl(timeout);
		 }
                 destroy = true;
             } else {
                 con.setDestroyReason(
                     Globals.getBrokerResources().getKString(
                      BrokerResources.M_CLIENT_SHUTDOWN));
                 GoodbyeTask.addConnection(con.getConnectionUID(),
                     Globals.getBrokerResources().getKString(
                      BrokerResources.M_CLIENT_SHUTDOWN));
             }
        } else {
            destroy = true;
        }
        if (destroy) {
            con.destroyConnection(false /* no reply */,
                  GoodbyeReason.CLIENT_CLOSED, 
                  Globals.getBrokerResources().getKString(
                      BrokerResources.M_CLIENT_SHUTDOWN));
        }
        return true;
    }

}


class GoodbyeTask extends TimerTask
{
    static Logger logger = Globals.getLogger();
    static GoodbyeTask runner = null;

    LinkedList nextSet = new LinkedList();
    LinkedList reasonSet = new LinkedList();

    static long timeout = 0;

    boolean invalid = false;
    private static synchronized GoodbyeTask get() {
        if (runner == null) {
            runner = new GoodbyeTask();
        }
        return runner;
    }

    public static void initialize(long ttl)
    {
        timeout = ttl;
    }

    private GoodbyeTask() {         
        if (timeout <= 0)
            timeout = 300*1000; // 5 minutes
        try { 
            Globals.getTimer(true).schedule(this, timeout, timeout);
        } catch (IllegalStateException ex) {
            logger.logStack(Logger.DEBUG,"Timer canceled ", ex);
            invalid = true;
        }
    }

    private synchronized void _addCon(ConnectionUID conuid, String reason) {
        if (invalid) return;
        reasonSet.add(reason);
        nextSet.add(conuid);
    }

    public static void addConnection(ConnectionUID conuid, String reason) {
        synchronized (GoodbyeTask.class) {
            GoodbyeTask task = runner.get();
            try {
                runner.get()._addCon(conuid, reason);
            } catch (IllegalStateException ex) {
                logger.logStack(Logger.DEBUG,"Timer canceled ", ex);
            }
        }
    }
    public void run() {
        LinkedList list = null;
        LinkedList reasonlist = null;
        synchronized(this.getClass()) {
            synchronized(this) {
                if (nextSet.isEmpty()) {
                    runner.cancel();
                    runner = null;
                } else {
                    list = nextSet;
                    reasonlist = reasonSet;
                    nextSet = new LinkedList();
                    reasonSet = new LinkedList();
                }
            }
        }
        if (list == null) return;
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            ConnectionUID uid = (ConnectionUID)itr.next();
            IMQConnection con = (IMQConnection)Globals.getConnectionManager()
                                .getConnection(uid);
            String reason = null;
            try {
                reason = (String)reasonlist.removeFirst();
            } catch (Exception e) {
                logger.log(Logger.DEBUG,"Can't get reason string for destroying connection " + uid);
            }
            if (reason == null) reason = "REASON NOTFOUND";
            if (con != null && con.isValid()) {
                try {
                    con.destroyConnection(false,GoodbyeReason.CLIENT_CLOSED, reason);
                } catch (Exception ex) {
                    logger.logStack(Logger.DEBUG,"error destroying connection " + con , ex);
                }
            }
            itr.remove();
        }              
    }

}
