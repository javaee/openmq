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
 * @(#)IMQEmbeddedConnection.java  10/28/08
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.ReadOnlyPacket;
import com.sun.messaging.jmq.io.ReadWritePacket;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsservice.DirectBrokerConnection;
import com.sun.messaging.jmq.jmsservice.HandOffQueue;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.IMQBlockingQueue;
import java.util.*;
import java.io.*;
import java.security.Principal;




public class IMQEmbeddedConnection extends IMQIPConnection implements DirectBrokerConnection
{

    IMQBlockingQueue inputQueue;
    IMQBlockingQueue outputQueue;

    class EOF { // note we could also do something like queue the exception
        String reason = null;
        public EOF(String reason) {
            this.reason = reason;
        }
        public String getReason() {
            return reason;
        }
     };


    /**
     * constructor
     */


    public IMQEmbeddedConnection(Service svc, 
             PacketRouter router) 
        throws IOException, BrokerException
    {
        super(svc, null, router);

        inputQueue = new IMQBlockingQueue();
        outputQueue = new IMQBlockingQueue();
    }

    public HandOffQueue getClientToBrokerQueue() {
        return inputQueue;
    }

    public HandOffQueue getBrokerToClientQueue() {
        return outputQueue;
    }

    public boolean isBlocking() {
        return true;
    }

    /** 
     * The debug state of this object
     */
    public synchronized Hashtable getDebugState() {
        Hashtable ht = super.getDebugState();
        // LKS - XXX
        ht.put("transport","Embedded");
        ht.put("inputQueue",inputQueue.toString());
        ht.put("outputQueue",outputQueue.toString());
        return ht;
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
                if (IMQBasicConnection.DEBUG)
                    logger.log(Logger.DEBUG,"Exception getting authentication name "
                        + conId, e );
                        
            }
        }


        String retstr = userString + "@" +
            "Direct" + ":" +
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

// -------------------------------------------------------------------------
//   Basic Connection Management
// -------------------------------------------------------------------------

    public synchronized void closeConnection(
            boolean force, int reason, String reasonStr) 
    { 
        super.closeConnection(force, reason, reasonStr);

        //Stick an EOF packet on the readChannel to wake it up
        EOF eof = new EOF(reasonStr);
        try {
            inputQueue.put(eof);
        } catch (InterruptedException ex) {
            Globals.getLogger().logStack(Logger.DEBUG,"nothing we can do",ex);
        }

    }


// -------------------------------------------------------------------------
//   Sending/Receiving Messages
// -------------------------------------------------------------------------


    protected boolean readInPacket(Packet p)
        throws IOException
    {
        // get and fill packet
        try {
        Object o= (Packet)inputQueue.take();
        if (o instanceof EOF) {
            EOF eof = (EOF)o;
            throw new IOException("Connection has been closed:"+eof.getReason());
        }
        Packet newp = (Packet)o; // note of type ReadWritePacket
        
        // Make a copy
        //
        // IF CLIENT IS MAKING A COPY, this can be a shallow copy
        // Otherwise, this needs to be a deep copy
        //
        p.fill(newp, false); //LKS-XXX: revisit and make sure it should be shallow
        } catch (IOException ex) {
            // rethrow
            throw ex;
        } catch (Exception ex) {
             //LKS-XXX handle better
             Globals.getLogger().logStack(Logger.DEBUG,"Error retrieving message",ex);
             throw new IOException("Issue processing :"+ex);
        }

        return true;
    }

    protected Packet clearReadPacket(Packet p) {
        // XXX - we don't need a new packet if its not message data
        // Revisit
        return null;
    }

    protected boolean writeOutPacket(Packet p) 
        throws IOException
    {
        // write packet
        // it needs to be of type ReadOnlyPacket
        ReadWritePacket rp = new ReadWritePacket();
        // this should be deep
        rp.fill(p, true);

        // stick on the queue
        outputQueue.add(rp); 

        return true;
    }

    protected Packet clearWritePacket(Packet p)
    {
        // not sure if we need to clear this or not
        // XXX- Revisit

        return null;
    }


}



