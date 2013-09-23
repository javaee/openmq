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
 * @(#)GroupService.java	1.19 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.group;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import java.nio.channels.spi.*;
import java.nio.channels.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.imq.*;
import com.sun.messaging.jmq.jmsserver.service.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.pool.*;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

import com.sun.messaging.jmq.jmsserver.net.Protocol;
import com.sun.messaging.jmq.jmsserver.net.ProtocolStreams;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;

public class GroupService extends IMQIPService
{
    static boolean DEBUG = false;

    Logger getLogger() {
        return logger;
    }


    public GroupService(String name, Protocol protocol,
        int type, PacketRouter router, int min, int max) {
        super(name, protocol, type, router, min, max);
        logger.log(Logger.DEBUG, "Running Group Service");

        serviceReadSelectors.initializeService(this, 
                    ((min/2)+ (min%2)), limit, 
                    readSelectorClass, SelectionKey.OP_READ);
        serviceWriteSelectors.initializeService(this, 
                    (min/2), limit, 
                    writeSelectorClass, SelectionKey.OP_WRITE);

    }

    public Hashtable getDebugState()
    {
        Hashtable ht = super.getDebugState();
        ht.put("readState", getDebugState(true));
        ht.put("writeState", getDebugState(false));
        return ht;
    }

    
    public void acceptConnection(IMQIPConnection con)
        throws IOException, BrokerException
    {
        if (DEBUG) {
            logger.log(Logger.DEBUG, "Adding new Connection {0} ",
                con.toString());
        }
      
        addConnection(this, con);

    }


    static MapList serviceReadSelectors = new MapList();
    static MapList serviceWriteSelectors = new MapList();

    static Class readSelectorClass = null;
    static Class writeSelectorClass = null;


    private static final String pkgname = "com.sun.messaging.jmq.jmsserver."
                                   + "service.imq.group.";
    static {
        try {
            readSelectorClass = Class.forName(pkgname +"ReadThread");
        } catch (Exception ex) {
                Globals.getLogger().logStack(Logger.ERROR, 
                     BrokerResources.E_INTERNAL_BROKER_ERROR, 
                     "unable to create class for handling READ selectors.", ex);
        }
        try {
            writeSelectorClass = Class.forName(pkgname +"WriteThread");
        } catch (Exception ex) {
                Globals.getLogger().logStack(Logger.ERROR, 
                     BrokerResources.E_INTERNAL_BROKER_ERROR, 
                     "unable to create class for handling WRITE selectors.", 
                      ex);
        }
    }



    public static final int UNLIMITED = -1;
    private static final int limit = Globals.getConfig().getIntProperty(
        Globals.IMQ + ".shared.connectionMonitor_limit", 64);


    public static void addConnection(GroupService svc, IMQIPConnection conn) 
        throws IOException
    {
        
        synchronized (GroupService.class) {
            SelectThread readthr = serviceReadSelectors.findThread(svc);
            SelectThread writethr = serviceWriteSelectors.findThread(svc);

            if (readthr == null || writethr == null) {
                  throw new IOException(Globals.getBrokerResources().getKString(
                                BrokerResources.E_INTERNAL_BROKER_ERROR, 
                                " No threads allocated for " 
                                + (readthr == null 
                                    ? (writethr == null ? "both" : "read") 
                                    : "write") 
                                + " selector thread on service " 
                                + svc + " closing connection " + conn));
            }
            GroupNotificationInfo ninfo = new GroupNotificationInfo();
            ninfo.targetThreads(readthr, writethr);
            conn.attach(ninfo);
            readthr.addNewConnection(conn);
            writethr.addNewConnection(conn);
        }

    }


    public static void destroyService(Service svc) {
        synchronized (GroupService.class) {
            serviceReadSelectors.destroy(svc);
            serviceWriteSelectors.destroy(svc);
        }

    }

    public static void dump(PrintStream str) {
        //synchronized (GroupService.class) {
        //}
    }

    public RunnableFactory getRunnableFactory() {
        return new GroupRunnableFactory();
    }

    ThreadPool getPool() {
        return pool;
    }

    public Hashtable getDebugState(boolean read) {
        if (read ) {
            if (serviceReadSelectors == null) {
                Hashtable ht = new Hashtable();
                ht.put("serviceReadSelectors","null");
                return ht; 
            }
            return serviceReadSelectors.getDebugState(this);
        } else {
            if (serviceWriteSelectors == null) {
                Hashtable ht = new Hashtable();
                ht.put("serviceWriteSelectors","null");
                return ht; 
            }
            return serviceWriteSelectors.getDebugState(this);
        }
    }

}



