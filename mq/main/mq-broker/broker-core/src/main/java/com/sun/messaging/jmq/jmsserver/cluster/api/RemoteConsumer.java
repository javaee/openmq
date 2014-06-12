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
 * @(#)RemoteConsumer.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

/**
 * Class which handled 3.0 remove cluster topic
 * consumers (3.5 clusters will be smarter in
 * later releases
 */


import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.util.selector.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.core.*;

public class RemoteConsumer extends Consumer
{
    transient Set consumers = new HashSet();

    private static boolean DEBUG = false;

    private static Logger logger = Globals.getLogger();

    public RemoteConsumer(DestinationUID duid) 
        throws IOException, SelectorFormatException
    {
        super(duid, null, false, (ConnectionUID)null);
    }

    public int getConsumerCount() {
        synchronized(consumers) {
            return consumers.size();
        }
    }

    public void addConsumer(Consumer c) 
    {
        synchronized(consumers) {
            consumers.add(c);
        }
    }

    public void removeConsumer(Consumer c)
    {
        synchronized(consumers) {
            consumers.remove(c);
        }
    }

    public boolean match(PacketReference msg, Set s)
         throws BrokerException, SelectorFormatException
    {
        boolean match = false;
        Map props = null;
        Map headers = null;
      
        synchronized(consumers) {
            Iterator itr = consumers.iterator();
            Consumer c = (Consumer) itr.next();
            if (c.getSelector() == null) {
                match = true;
                s.add(c);
             } else  {
                 Selector selector = c.getSelector();
        
                 if (props == null && selector.usesProperties()) {
                     try {
                         props = msg.getProperties();
                     } catch (ClassNotFoundException ex) {
                         logger.logStack(Logger.ERROR,"INTERNAL ERROR", ex);
                         props = new HashMap();
                     }
                 }
                 if (headers == null && selector.usesFields()) {
                     headers = msg.getHeaders();
                 }
                 if (selector.match(props, headers)) {
                     match = true;
                     s.add(c);
                 }
           
            }
            return match;
        }
    }

    

    private void readObject(java.io.ObjectInputStream ois)
        throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        consumers = new HashSet();
    }
} 
