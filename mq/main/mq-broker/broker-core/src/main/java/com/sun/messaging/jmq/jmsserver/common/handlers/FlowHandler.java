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
 * @(#)FlowHandler.java	1.20 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.common.handlers;

import java.util.*;
import java.net.*;
import com.sun.messaging.jmq.jmsserver.data.PacketHandler;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.net.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.auth.AuthCacheData;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQBasicConnection;

import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.plugin.spi.ConsumerSpi;

/**
 * handles receiving Flow packet
 */
public class FlowHandler extends PacketHandler 
{

    private Logger logger = Globals.getLogger();
    private static boolean DEBUG = false;


    /**
     * Method to handle flow messages
     */
    public boolean handle(IMQConnection con, Packet msg) 
        throws BrokerException 
    { 

         if (DEBUG) {
             logger.log(Logger.DEBUGHIGH, "FlowHandler: handle() [ Received Flow  Message]");
          }

          assert msg.getPacketType() == PacketType.RESUME_FLOW;


          Hashtable props = null;
          try {
              props = msg.getProperties();
          } catch (Exception ex) {
              logger.logStack(Logger.WARNING, "RESUME-FLOW Packet.getProperties()", ex);
              props = new Hashtable();
          }

          Integer bufsize = null; 

          ConsumerSpi consumer = null;
          if (props != null) {
              bufsize = (Integer)props.get("JMQSize");
              if (bufsize == null) { // try old protocol
                  bufsize = (Integer)props.get("JMQRBufferSize");
              }

              Long cuid = (Long)props.get("JMQConsumerID");
              if (cuid != null) {
                  ConsumerUID tmpuid = new ConsumerUID(cuid.longValue());
                  consumer = coreLifecycle.getConsumer(tmpuid);
              }
          }

          if (DEBUG)
              logger.log(Logger.DEBUG, "Setting JMQRBufferSize -" + bufsize);

          if (consumer != null) {
              // consumer flow control
              int size = (bufsize == null ? -1 : bufsize.intValue());
		      consumerFlow(consumer, size);
          } else {
              // connection flow control
              int size = (bufsize == null ? -1 : bufsize.intValue());
              connectionFlow(con, size);
          }
          return true;
 
    }

    public void consumerFlow(ConsumerSpi consumer, int cprefetch)
    {
          try {
              int prefetch = coreLifecycle.calcPrefetch(consumer, cprefetch);
              consumer.resumeFlow(prefetch);
          } catch (Exception ex) {
              // only happens if client passs bad cprefetch
              // which is < current size .. this is a protocol
              // error 
              logger.logStack(Logger.ERROR,
                  Globals.getBrokerResources().getString(
                      BrokerResources.X_INTERNAL_EXCEPTION,
                       "protocol error, bad rbuf size"), ex);
              consumer.resumeFlow(-1);
          }
    }

    public void connectionFlow(IMQConnection con,int  size)
    {
        con.resumeFlow(size);
    }
}
