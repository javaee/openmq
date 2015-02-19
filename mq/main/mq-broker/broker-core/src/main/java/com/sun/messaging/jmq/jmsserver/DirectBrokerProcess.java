/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver;

import java.util.*;
import java.util.concurrent.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQEmbeddedService;
import com.sun.messaging.jmq.util.ServiceState;
import com.sun.messaging.jmq.jmsservice.BrokerEventListener;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsservice.DirectBrokerConnection;
import com.sun.messaging.jmq.jmsservice.JMSDirectBroker;
import com.sun.messaging.jmq.io.*; //test program only

/**
 * DirectBrokerProcess implementation. It wraps a singleton class
 * (only one broker can be running in any process).<P>
 *
 * <u>Example</u><P>
 * <code><PRE>
 *      DirectBrokerProcess bp = BrokerProcess.getBrokerProcess(BrokerProcess.DIRECT_BROKER);
 *      try {
 *      
 *          Properties ht = BrokerProcess.convertArgs(args);
 *          int exitcode = bp.start();
 *          if (exitcode != 0) { // failure to start
 *              System.out.println("Broker exited with " + exitcode);
 *          }
 *
 *      } catch (IllegalArgumentException ex) {
 *          System.err.println("Bad Argument " + ex.getMessage());
 *          System.out.println(BrokerProcess.usage());
 *      }
 * </PRE></code>
 */
public class DirectBrokerProcess extends BrokerProcess implements JMSDirectBroker
{
    String name = "mqdirect";
    public DirectBrokerProcess() {
        super();
    }


    public int start(boolean inProcess, 
        Properties properties, BrokerEventListener bel, 
        boolean initOnly,  Throwable failStartThrowable)
        throws OutOfMemoryError, IllegalStateException, IllegalArgumentException {

        if (properties == null) {
             properties = new Properties();
        }
        properties.put(Globals.IMQ + ".service.runtimeAdd", name);
        return super.start(inProcess, properties, bel, initOnly, failStartThrowable);
    }

    /**
     * Returns true when the broker is ready to start processing messages
     */
    public boolean directServiceIsUp() {
        IMQService service = (IMQService)Globals.getServiceManager().getService(name);
        if (service == null) return false;
        return service.getState() == ServiceState.RUNNING;
    }

    public DirectBrokerConnection getConnection() {
        IMQEmbeddedService service = (IMQEmbeddedService)Globals.getServiceManager().getService(name);
        try {
            return service.createConnection();
        } catch (Exception ex) {
            Globals.getLogger().logStack(Logger.WARNING, "L10N-XXX: Unable to create connection", ex);
        }
        return null;
    }

}


