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
 * @(#)GetMetricsHandler.java	1.20 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.MetricManager;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.MetricCounters;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.BrokerInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;


public class GetMetricsHandler extends AdminCmdHandler
{

    private static boolean DEBUG = getDEBUG();

    public GetMetricsHandler(AdminDataHandler parent) {
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
                cmd_props);
        }

        int status = Status.OK;
        String errMsg = null;

	String service = (String)cmd_props.get(MessageType.JMQ_SERVICE_NAME);

	String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer type = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);

        Object replyobj = null;
        String msgtype = null;
        if (destination != null) {
            try {
                Destination[] ds = DL.getDestination(null,
                    destination, DestType.isQueue((type == null ? 0 : type.intValue())));
                Destination d = ds[0]; //PART
                if (d == null) {
                    status = Status.NOT_FOUND;
                    int mytype = (type== null ? 0 : type.intValue());
                    errMsg = rb.getString(rb.E_NO_SUCH_DESTINATION, 
                       getDestinationType(mytype), destination);
                } else {
                    replyobj = d.getMetrics();
                }
               
            } catch (Exception ex) {
                int mytype = (type== null ? 0 : type.intValue());
                errMsg = rb.getString(rb.E_NO_SUCH_DESTINATION, 
                		getDestinationType(mytype),destination);
                status = Status.ERROR;

		// log the error
	    	logger.log(Logger.ERROR, rb.E_INTERNAL_BROKER_ERROR,
                	this.getClass().getName() + 
                	": failed to get destination ("+
			DestType.toString(mytype) + ":" +
			destination + ")",  ex);
            }
            msgtype = "DESTINATION";
        } else {	
            ServiceManager sm = Globals.getServiceManager();
            MetricManager mm = Globals.getMetricManager();
            MetricCounters mc = null;

            if (service != null &&
                sm.getServiceType(service) == ServiceType.UNKNOWN) {

                status = Status.NOT_FOUND;
                errMsg = rb.getString(rb.X_NO_SUCH_SERVICE, service);
            } else {
	        // If service is null getMetricCounters() will get counters
	        // for all services
	        mc = mm.getMetricCounters(service);
                if (service != null) {
                    msgtype = "SERVICE";
                }
                replyobj = mc;
            }
        }

	// Send reply
	Packet reply = new Packet(con.useDirectBuffers());
	reply.setPacketType(PacketType.OBJECT_MESSAGE);

        Hashtable pr = new Hashtable();
        if (msgtype != null) {
            pr.put(MessageType.JMQ_BODY_TYPE, msgtype);
        }

	setProperties(reply, MessageType.GET_METRICS_REPLY, status, errMsg,
              pr);

        if (replyobj != null) {
	    setBodyObject(reply, replyobj);
        }
	parent.sendReply(con, cmd_msg, reply);
    return true;
    }
}
