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
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;
import com.sun.messaging.jmq.jmsserver.plugin.spi.DestinationSpi;




/**
 * Handler class which deals with adding and removing destination from the broker
 */
public class DestinationHandler extends PacketHandler 
{
    private Logger logger = Globals.getLogger();
    private static boolean DEBUG = false;
  

    public DestinationHandler() {
    }

    /**
     * Method to handle Destination (create or delete) messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException
    {

        int status = Status.OK;
        String reason = null;

        // XXX - REVISIT 2/25/00 racer
        // do we need to create a reply packet each time ?

        Packet pkt = new Packet(con.useDirectBuffers());
        pkt.setConsumerID(msg.getConsumerID());
        Hashtable hash = new Hashtable();

        Hashtable props = null;
        try {
            props = msg.getProperties();
        } catch (Exception ex) {
            assert false;
            logger.logStack(Logger.ERROR, BrokerResources.E_INTERNAL_BROKER_ERROR, "Unable to create/destroy destination - no properties",ex);
            throw new BrokerException(Globals.getBrokerResources().getString(
           BrokerResources.X_INTERNAL_EXCEPTION,"Can not handle create/destroy destination"));
        }
    
        String destination = (String )props.get("JMQDestination");

        Integer inttype = (Integer )props.get("JMQDestType");

        int type = (inttype == null) ? 0 : inttype.intValue();

        pkt.setPacketType(msg.getPacketType() + 1);
        if (msg.getPacketType() == PacketType.CREATE_DESTINATION) {

            if (DEBUG) {
                logger.log(Logger.DEBUGHIGH, "ConsumerHandler: handle() [ Received AddDestination message {0}]", msg.toString());
            }

            assert destination != null;
            assert inttype != null;

            if (con.isAdminConnection()) {
               type |= DestType.DEST_ADMIN | DestType.DEST_LOCAL 
                     | DestType.DEST_AUTO;
            }
            assert pkt.getPacketType() == PacketType.CREATE_DESTINATION_REPLY;
            try {
                DestinationSpi d = null;
                if (DestType.isTemporary(type)) {
                    // deal w/ versioning .. only store
                    // 3.5 or later
                    boolean storeTemps = con.getConnectionUID().
                            getCanReconnect();
                    long reconnectTime = con.getReconnectInterval();
                    DestinationSpi[] ds = Globals.getCoreLifecycle().createTempDestination(
                            con.getPartitionedStore(),
                            destination, type, con.getConnectionUID(), 
                            storeTemps, reconnectTime);
                    d = ds[0];
                    if (con.getConnectionUID().equals(d.getConnectionUID())) {
                        con.attachTempDestination(d.getDestinationUID());
                    }

                } else if (destination.startsWith(Globals.INTERNAL_PREFIX)) {
                    // do nothing
                } else if (DestinationUID.isWildcard(destination)) {
                    pkt.setWildcard(true);
                    // dont create a destination
                } else {
                    DestinationSpi[] ds = Globals.getCoreLifecycle().getDestination(
                                         con.getPartitionedStore(), destination, 
                                         type, true, !con.isAdminConnection());
                    d = ds[0];
                }

                hash.put("JMQDestType", new Integer(type));
                hash.put("JMQDestUID", destination);

		/*
		 * Set XML Schema validation properties
		 */
                hash.put("JMQValidateXMLSchema", new Boolean(isXMLSchemaValidationOn(d)));
		String uris = getXMLSchemaURIList(d);
		if (uris != null)  {
                    hash.put("JMQXMLSchemaURIList", uris);
		}
                hash.put("JMQReloadXMLSchemaOnFailure", 
				new Boolean(getReloadXMLSchemaOnFailure(d)));

            } catch (BrokerException ex) {
                status = ex.getStatusCode();
                reason = ex.getMessage();
                if (status != Status.CONFLICT) {
                    logger.log(Logger.WARNING, 
                        BrokerResources.W_CREATE_DEST_FAILED, destination, ex);
                } else if (DEBUG) {
                    logger.log(Logger.DEBUG, 
                        BrokerResources.W_CREATE_DEST_FAILED, destination, ex);
                }
            } catch (IOException ex) {
                status = Status.ERROR;
                reason = ex.getMessage();
                logger.log(Logger.WARNING, 
                    BrokerResources.W_CREATE_DEST_FAILED, destination, ex);
            }
        } else { // removing 
            assert msg.getPacketType() == PacketType.DESTROY_DESTINATION;
            assert pkt.getPacketType() == PacketType.DESTROY_DESTINATION_REPLY;

            DestinationSpi d =null;

            try {
                DestinationUID rmuid = DestinationUID.getUID(destination, DestType.isQueue(type));

                if (destination == null) {
                    throw new BrokerException(
                        Globals.getBrokerResources().getString(
                           BrokerResources.X_INTERNAL_EXCEPTION,
                       "protocol error,  destination is null"),
                           Status.NOT_FOUND);
                }
                DestinationSpi[] ds = Globals.getCoreLifecycle().getDestination(
                                              con.getPartitionedStore(), rmuid);
                d = ds[0]; 
                assert (d != null);
                Globals.getCoreLifecycle().removeDestination(con.getPartitionedStore(), rmuid, true, 
                     Globals.getBrokerResources().getString(
                        BrokerResources.M_CLIENT_REQUEST, con.getConnectionUID()));
                con.detachTempDestination(rmuid);
            } catch (BrokerException ex) {
                status = ex.getStatusCode();
                reason = ex.getMessage();
                logger.log(Logger.WARNING, 
                    BrokerResources.W_DESTROY_DEST_FAILED, destination,ex);
            } catch (IOException ex) {
                status = Status.ERROR;
                reason = ex.getMessage();
                logger.log(Logger.WARNING, 
                    BrokerResources.W_DESTROY_DEST_FAILED, destination,ex);
            }

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

    private String getXMLValidationPropName(DestinationSpi d)  {
	if (d == null)  {
	    return (null);
	}

	String name = d.getDestinationName(), propName;

	propName = Globals.IMQ + ".validation.destination."
			+ (d.isQueue() ? "queue" : "topic")
			+ "."
			+ name;
	return (propName);
    }

    private boolean isXMLSchemaValidationOn(DestinationSpi d)  {
	if (d == null)  {
	    return (false);
	}

	/*
	String propName = getXMLValidationPropName(d);

	return(Globals.getConfig().getBooleanProperty(propName));
	*/

	return(d.validateXMLSchemaEnabled());
    }

    private String getXMLSchemaURIList(DestinationSpi d)  {
	String ret = null;

	if (d == null)  {
	    return (null);
	}

	/*
	String propName = getXMLValidationPropName(d) + ".uri";

	ret = Globals.getConfig().getProperty(propName);

	return (ret);
	*/

	ret = d.getXMLSchemaUriList();

	return (ret);
    }

    private boolean getReloadXMLSchemaOnFailure(DestinationSpi d)  {
	if (d == null)  {
	    return (false);
	}

	return(d.reloadXMLSchemaOnFailure());
    }


}
