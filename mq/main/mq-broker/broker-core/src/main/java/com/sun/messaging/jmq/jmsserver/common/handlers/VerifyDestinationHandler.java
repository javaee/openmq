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
 * %W% %G%
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.util.selector.Selector;
import com.sun.messaging.jmq.util.selector.SelectorFormatException;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.jmsserver.plugin.spi.DestinationSpi;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.DestType;

import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;


import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;


/**
 * Handler class which deals with starting/stoping the delivery of 
 * messages to a specific connection
 */
public class VerifyDestinationHandler extends PacketHandler 
{

    private Logger logger = Globals.getLogger();
    private static boolean DEBUG = false;
  

    public VerifyDestinationHandler() {
    }

    /**
     * Method to handle Destination (create or delete) messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
    {

        int status = Status.OK;
        String reason = null;

        assert msg.getPacketType() == PacketType.VERIFY_DESTINATION;

        Packet pkt = new Packet(con.useDirectBuffers());
        pkt.setConsumerID(msg.getConsumerID());

        pkt.setPacketType(PacketType.VERIFY_DESTINATION_REPLY);

        Hashtable hash = new Hashtable();

        Hashtable props = null;
        String selectorstr = null;
        Integer destType;
        int type = 0;
        try {
            props = msg.getProperties();
            String destination = (String )props.get("JMQDestination");
            Integer inttype = (Integer )props.get("JMQDestType");

            // destination & type required
            assert destination != null;
            assert inttype != null;

            type = (inttype == null) ? 0 : inttype.intValue();
            selectorstr = (String )props.get("JMQSelector");

            if (selectorstr != null) {
                Selector selector = Selector.compile(selectorstr);
                selector = null;
            }

            boolean notFound = false;

            DestinationSpi d = null;
            if (DestinationUID.isWildcard(destination)) {
                // retrieve the list of destinations
                pkt.setWildcard(true);
                // see if there are any destinations that match
                DestinationUID duid = DestinationUID.getUID(destination, type);
                List[] ll = coreLifecycle.findMatchingIDs(con.getPartitionedStore(), duid);
                List l = ll[0];
                if (l.isEmpty()) {
                    notFound = true;
                }

            } else {
                DestinationSpi[] ds =  coreLifecycle.getDestination(con.getPartitionedStore(),
                                                       destination, DestType.isQueue(type));
                d = ds[0];
                notFound = ( d == null);
            }

            if (notFound) {
                // not found
                status = Status.NOT_FOUND;
                reason = "destination not found";
                hash.put("JMQCanCreate", Boolean.valueOf(
                        coreLifecycle.canAutoCreate(DestType.isQueue(type))));
            } else {
                if (d != null)
                    hash.put("JMQDestType", new Integer(d.getType()));
            }
            
        } catch (SelectorFormatException ex) {
            reason = ex.getMessage();
            status = Status.BAD_REQUEST;
            logger.log(Logger.WARNING,BrokerResources.W_SELECTOR_PARSE, 
                 selectorstr, ex);
        } catch (IOException ex) {
            logger.log(Logger.ERROR, BrokerResources.E_INTERNAL_BROKER_ERROR, "Unable to verify destination - no properties",ex);
            reason = ex.getMessage();
            status = Status.ERROR;
        } catch (ClassNotFoundException ex) {
            logger.logStack(Logger.ERROR, BrokerResources.E_INTERNAL_BROKER_ERROR, "Unable to verify destination -bad class",ex);
            reason = ex.getMessage();
            status = Status.ERROR;
        } catch (BrokerException ex) {
            reason = ex.getMessage();
            status = ex.getStatusCode();
            logger.logStack(Logger.DEBUG, BrokerResources.E_INTERNAL_BROKER_ERROR, "Unable to verify destination ",ex);

        } catch (SecurityException ex) {
            reason = ex.getMessage();
            status = Status.FORBIDDEN;
            logger.log(Logger.WARNING,ex.toString(), ex);
        }
    

        hash.put("JMQStatus", new Integer(status));
        if (reason != null)
            hash.put("JMQReason", reason);
        if (((IMQBasicConnection)con).getDumpPacket() ||
                ((IMQBasicConnection)con).getDumpOutPacket()) 
            hash.put("JMQReqID", msg.getSysMessageID().toString());


        pkt.setProperties(hash);
        con.sendControlMessage(pkt);
        return true;
    }

}
