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
 * @(#)GetLicenseHandler.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.util.*;
import java.net.*;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.license.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;

import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;


/**
 * Handler class which deals with the GET_LICENSE message
 * GET_LICENSE requests licensing information so the client can restrict
 * licensed features.
 */
public class GetLicenseHandler extends PacketHandler 
{
    private ConnectionManager connectionList;

    private Logger logger = Globals.getLogger();
    private BrokerResources rb = Globals.getBrokerResources();
    private static boolean DEBUG = false;

    private static boolean ALLOW_C_CLIENTS = false;
    private static boolean CAN_RECONNECT = false;

    static {
        try {
            LicenseBase license = Globals.getCurrentLicense(null);
        } catch (BrokerException ex) {
            
        }
        try {
            LicenseBase license = Globals.getCurrentLicense(null);
            CAN_RECONNECT = license.getBooleanProperty(
                                license.PROP_ENABLE_FAILOVER, false);
        } catch (BrokerException ex) {
            CAN_RECONNECT = false;
        }

    }

    public GetLicenseHandler()
    {
    }

    /**
     * Method to handle GET_LICENSE messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException 
    { 

         if (DEBUG) {
             logger.log(Logger.DEBUGHIGH, "GetLicenseHandler: handle(" + con + ", " + PacketType.getString(msg.getPacketType()) + ")" );
          }

          String reason = "";
	  int    status = Status.OK;

          // Create reply packet
          Packet pkt = new Packet(con.useDirectBuffers());
          pkt.setPacketType(PacketType.GET_LICENSE_REPLY);
          pkt.setConsumerID(msg.getConsumerID());

          Hashtable hash = new Hashtable();
          try {
              // Added licensing description properties
              LicenseBase license = Globals.getCurrentLicense(null);
              hash.put("JMQLicense",
                  license.getProperty(LicenseBase.PROP_LICENSE_TYPE));
              hash.put("JMQLicenseDesc",
                  license.getProperty(LicenseBase.PROP_DESCRIPTION));

              // Copy license properties into packet
              Properties props = license.getProperties();
              Enumeration e = props.propertyNames();
              while (e.hasMoreElements()) {
                  String key = (String)e.nextElement();
                  hash.put(key, props.get(key));
              }
          } catch (BrokerException ex) {
              // This should never happen, but go ahead and at least
              // capture exception here
              reason = ex.toString();
              status = Status.ERROR;
          }

          hash.put("JMQStatus", new Integer(status));
          if (status != Status.OK) {
              hash.put("JMQReason", reason);
           }

          // Set packet properties
          pkt.setProperties(hash);

          // Send message
	  con.sendControlMessage(pkt);

          return true;
    }

}
