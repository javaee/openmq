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
 * @(#)IMQEmbeddedConnection.java  10/28/08
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.IMQBlockingQueue;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.core.Session;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.ReadOnlyPacket;
import com.sun.messaging.jmq.io.ReadWritePacket;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsservice.DirectBrokerConnection;
import com.sun.messaging.jmq.jmsserver.Globals;
import java.util.*;
import java.io.*;
import java.net.*;
import java.security.Principal;
import com.sun.messaging.jmq.jmsservice.HandOffQueue;
import com.sun.messaging.jmq.util.net.IPAddress;
import com.sun.messaging.jmq.jmsserver.service.MetricManager;
import com.sun.messaging.jmq.util.lists.*;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;

public abstract class IMQBasicConnection extends IMQConnection 
{
    String remoteConString = null;
    String remoteHostString = null;
    PacketRouter router = null;

    protected int[] pktsOut = new int[PacketType.LAST];
    protected int[] pktsIn = new int[PacketType.LAST];

    protected static final int NO_VERSION=0;
    public static final int CURVERSION = Packet.CURRENT_VERSION;
    public int packetVersion=NO_VERSION;

    // Known data which may be tagged on a connection
    public static final String CLIENT_ID = "client id";
    public static final String TRANSACTION_LIST = "transaction";
    public static final String TRANSACTION_IDMAP = "tidmap";
    public static final String TRANSACTION_CACHE = "txncache";
    public static final String USER_AGENT = "useragent";




    protected static boolean DEBUG = Globals.getConfig().getBooleanProperty(
        Globals.IMQ + ".packet.debug.info");

    protected static boolean DUMP_PACKET = 
        Globals.getConfig().getBooleanProperty(
            Globals.IMQ + ".packet.debug.all");

    protected static boolean OUT_DUMP_PACKET =
        Globals.getConfig().getBooleanProperty(
            Globals.IMQ + ".packet.debug.out");
    protected static boolean IN_DUMP_PACKET = 
        Globals.getConfig().getBooleanProperty(
            Globals.IMQ + ".packet.debug.in");

    public static boolean getDEBUG() {
        return DEBUG;    
    }
    public static boolean getDumpPacket() {
        return DUMP_PACKET;
    }
    public static boolean getDumpInPacket() {
        return IN_DUMP_PACKET;
    }
    public static boolean getDumpOutPacket() {
        return OUT_DUMP_PACKET;
    }

    public static void dumpPacket(boolean on) {
        DUMP_PACKET=on;
    }

    public static void dumpInPacket(boolean on) {
        IN_DUMP_PACKET=on;
    }
    public static void dumpOutPacket(boolean on) {
        OUT_DUMP_PACKET=on;
    }

    protected static byte[] ipAddress = null;

    static {
        try {
            ipAddress = InetAddress.getLocalHost().getAddress();
        } catch (Exception ex) {
            Globals.getLogger().log(Logger.INFO,"Internal Error, could not "
                  + " retrieve local address ", ipAddress);
            ipAddress = new byte[0];
        }
    }

    public boolean METRICS_ON = MetricManager.isEnabled();

    public IMQBasicConnection(Service svc, PacketRouter router)
        throws IOException, BrokerException
    {
        super(svc);
        this.router = router;
    }

    public void setPacketRouter(PacketRouter r) {
        router = r;
    }

    public boolean setConnectionState(int state) {
        this.state = state;
        return true; //default impl
    }
   /**
     * Count an incoming packet
     */
    public void countInPacket(Packet pkt) {
        if (pkt == null) 
            return;
        if (pkt.getPacketType() <= PacketType.MESSAGE &&
            pkt.getPacketType() >= PacketType.TEXT_MESSAGE) {

            // It's a JMS message, update both packet and message counters
            counters.updateIn(1, pkt.getPacketSize(), 1, pkt.getPacketSize());
        } else {
            // It's a control message. Only update packet counters
            counters.updateIn(0, 0, 1, pkt.getPacketSize());
        }
    }

    /**
     * Count outgoing packet
     */
    public void countOutPacket(Packet pkt) {
        if (pkt == null) 
            return;
        if (pkt.getPacketType() <= PacketType.MESSAGE &&
            pkt.getPacketType() >= PacketType.TEXT_MESSAGE) {

            // It's a JMS message, update both packet and message counters
            counters.updateOut(1, pkt.getPacketSize(), 1, pkt.getPacketSize());
        } else {
            // It's a control message. Only update packet counters
            counters.updateOut(0, 0, 1, pkt.getPacketSize());
        }
    }


    public void flushControl(long timeout) {
        //default does nothing
    }

    public int getLocalPort() {
        return 0;
    }

    public void waitForRelease(long time) {
        return;
    }

    protected void sayGoodbye(int reason, String reasonstr) {
        sayGoodbye(false, reason, reasonstr);
    }
    protected void sayGoodbye(boolean force, int reason, String reasonStr) {
        Packet goodbye_pkt = new Packet(useDirectBuffers());
        goodbye_pkt.setPacketType(PacketType.GOODBYE);
        Hashtable hash = new Hashtable();
        hash.put("JMQExit", Boolean.valueOf(force));
        hash.put("JMQGoodbyeReason", new Integer(reason));
        hash.put("JMQGoodbyeReasonString", reasonStr);
        goodbye_pkt.setProperties(hash);
        sendControlMessage(goodbye_pkt);
    }

    protected void sendConsumerInfo(int requestType, String destName, 
                                    int destType, int infoType) {
        if (state >= STATE_CLOSED) return;

        Packet info_pkt = new Packet(useDirectBuffers());
        info_pkt.setPacketType(PacketType.INFO);

        Hashtable props = new Hashtable();
        props.put("JMQRequestType", Integer.valueOf(requestType));
        props.put("JMQStatus", Status.OK);
        info_pkt.setProperties(props);
 
        Hashtable hash = new Hashtable();
        hash.put("JMQDestination", destName);
        hash.put("JMQDestType", Integer.valueOf(destType));
        hash.put("JMQConsumerInfoType", Integer.valueOf(infoType));
        DestinationUID duid = null;
        try {
            duid = DestinationUID.getUID(destName, destType);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(hash);
            oos.flush();
            bos.flush();
            info_pkt.setMessageBody(bos.toByteArray());
            bos.close();
        } catch (Throwable t) {
            logger.log(Logger.WARNING,
            "XXXI18N Error: Unable to send consumer info to client: "+duid, t);
            return;
        }
        sendControlMessage(info_pkt);
    }

    /**
     * default toString method, sub-classes should override
     */
    public String toString() {
        return "IMQConn["+getConnectionUID()+", "+
                getConnectionStateString(state)+", "+
                getRemoteConnectionString()+", "+localsvcstring +"]";
    }

    /**
     * methods used by debugging, subclasses should override
     */
    public String toDebugString() {
        return super.toString() + " state: " + state;
    }


    public String remoteHostString() {
	if (remoteHostString == null) {
	    try {
		InetAddress inetaddr = InetAddress.getByAddress(getRemoteIP());
		remoteHostString = inetaddr.getHostName();
	    } catch (Exception e) {
		remoteHostString=IPAddress.rawIPToString(getRemoteIP(), true, true);
	    }
	}
	return remoteHostString;
    }

    public String getRemoteConnectionString() {
        if (remoteConString != null)
            return remoteConString;

        boolean userset = false;

        String userString = "???";

        if (state >= Connection.STATE_AUTHENTICATED) {
            try {
                Principal principal = getAuthenticatedName();
                if (principal != null) {
                    userString = principal.getName();
                    userset = true;
                }
            } catch (BrokerException e) { 
                if (DEBUG)
                    logger.log(Logger.DEBUG,"Exception getting authentication name "
                        + conId, e );
                        
            }
        }


        String retstr = userString + "@" +
            "Direct2" + ":" +
            getConnectionUID();
        if (userset) remoteConString = retstr;
        return retstr;
    }

    String localsvcstring = null;
    protected String localServiceString() {
        if (localsvcstring != null)
            return localsvcstring;
        localsvcstring = service.getName();
        return localsvcstring;
    }

}



