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
 * @(#)MapList.java	1.8 06/29/07
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


class MapList {
    HashMap map = new HashMap();

    public void initializeService(GroupService svc, int min, int limit, Class selectClass, int mask)
    {
        MapEntry entry = new MapEntry(svc, min, limit,selectClass, mask);
        synchronized (this) {
            map.put(svc.getName(), entry);
        }
    }

    public Hashtable getDebugState(GroupService svc) 
    {
        MapEntry entry = null;
        synchronized (this) {
            entry = (MapEntry)map.get(svc.getName());
        }
        if (entry == null) {
            Hashtable ht = new Hashtable();
            ht.put("Service " + svc, "null");
            return ht;
        }
       
        return entry.getDebugState();
    }

    public void destroy(Service svc) {
        MapEntry entry = null;
        synchronized (this) {
            entry = (MapEntry)map.get(svc.getName());
            if (entry != null) {
                map.remove(svc.getName());
             }
        }
        if (entry != null)
             entry.destroy(
               Globals.getBrokerResources().getKString(
                   BrokerResources.M_SERVICE_SHUTDOWN));
    }

    public SelectThread findThread(GroupService svc) {
        MapEntry entry = null;
        synchronized (this) {
            entry = (MapEntry)map.get(svc.getName());
        }
        if (entry == null) {
             throw new RuntimeException("service does not have thread pool");
        }
        return entry.findThread();
   }
   public synchronized boolean checkRemoveThread(GroupService svc, SelectThread thr, boolean force) 
   {
        MapEntry entry = null;
        synchronized (this) {
           entry = (MapEntry)map.get(svc.getName());
        }
        if (entry == null) {
             throw new RuntimeException("service does not have thread pool");
        }
        return false; 
        // for now, dont remove threads
        //return entry.checkRemoveThread(thr, force);
   }


}
