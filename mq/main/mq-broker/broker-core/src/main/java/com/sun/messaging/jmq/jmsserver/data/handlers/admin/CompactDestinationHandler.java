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
 * @(#)CompactDestinationHandler.java	1.9 07/12/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Iterator;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.DestType;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.cluster.api.ha.HAMonitorService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class CompactDestinationHandler extends AdminCmdHandler
{
    private static boolean DEBUG = getDEBUG();

    public CompactDestinationHandler(AdminDataHandler parent) {
	super(parent);
    }

    /**
     * Handle the incomming administration message.
     *
     * @param con	The Connection the message came in on.
     * @param cmd_msg	The administration message
     * @param cmd_props The properties from the administration message
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
				       Hashtable cmd_props) {

	if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                "Compacting: " + cmd_props);
        }
        logger.log(Logger.INFO,
              Globals.getBrokerResources().I_COMPACTING,
              cmd_props);

 	String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer type = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);

        int status = Status.OK;
        String errMsg = null;
	boolean compactAll = false;

        HAMonitorService hamonitor = Globals.getHAMonitorService(); 
        if (hamonitor != null && hamonitor.inTakeover()) {
            status = Status.ERROR;
            errMsg =  rb.getString(rb.E_CANNOT_PROCEED_TAKEOVER_IN_PROCESS);

            logger.log(Logger.ERROR, this.getClass().getName() + ": " + errMsg);
	} else  {
	try {
	    if (destination != null) {
		// compact one destination
		Destination[] ds = DL.getDestination(null, destination,
					DestType.isQueue(type.intValue()));
                Destination d = ds[0]; //PART
		if (d != null) {
		    if (d.isPaused()) {
			d.compact();
		    } else {
			status = Status.ERROR;
			String msg = rb.getString(rb.E_DESTINATION_NOT_PAUSED);
			errMsg = rb.getString(rb.X_COMPACT_DST_EXCEPTION,
					destination, msg);
			logger.log(Logger.ERROR, errMsg);
		    }
		} else {
		    status = Status.ERROR;
		    String subError = rb.getString(rb.E_NO_SUCH_DESTINATION,
				getDestinationType(type.intValue()),
				destination);
		    errMsg = rb.getString(rb.X_COMPACT_DST_EXCEPTION,
				destination, subError);
		    logger.log(Logger.ERROR, errMsg);
		}
	    } else {
		Iterator[] itrs = DL.getAllDestinations(null);
                Iterator itr = itrs[0];
		boolean docompact = true;
		while (itr.hasNext()) {
		    // make sure all are paused
		    Destination d = (Destination)itr.next();

		    /*
		     * Skip internal, admin, or temp destinations.
		     * Skipping temp destinations may need to be
		     * revisited.
		     */
		    if (d.isInternal() || d.isAdmin() || d.isTemporary())  {
			continue;
		    }

		    if (!d.isPaused()) {
			docompact = false;
			status = Status.ERROR;
			String msg = rb.getString(
					rb.E_SOME_DESTINATIONS_NOT_PAUSED);
			errMsg = rb.getString(rb.X_COMPACT_DSTS_EXCEPTION,
					msg);
			logger.log(Logger.ERROR, errMsg);
		    }
		}

		if (docompact) {
		    itrs = DL.getAllDestinations(null);
                    itr = itrs[0]; //PART
		    while (itr.hasNext()) {
			Destination d = (Destination)itr.next();

			/*
			 * Skip internal, admin, or temp destinations.
			 * Skipping temp destinations may need to be
			 * revisited.
			 */
			if (d.isInternal() || d.isAdmin() || d.isTemporary())  {
			    continue;
			}

			d.compact();
		    }
		}
	    }
        } catch (Exception e) {
            status = Status.ERROR;
	    if (compactAll)  {
                errMsg = rb.getString( rb.X_COMPACT_DSTS_EXCEPTION, e.toString());
	    } else  {
                errMsg = rb.getString( rb.X_COMPACT_DST_EXCEPTION, 
                            destination, e.toString());
	    }
            logger.log(Logger.ERROR, errMsg, e);
         }
         }

	// Send reply
	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

	setProperties(reply, MessageType.COMPACT_DESTINATION_REPLY,
		status, errMsg);

	parent.sendReply(con, cmd_msg, reply);

	return true;
    }
}
