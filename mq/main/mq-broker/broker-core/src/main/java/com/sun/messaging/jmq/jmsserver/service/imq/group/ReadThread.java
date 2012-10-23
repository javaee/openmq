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
 * @(#)ReadThread.java	1.14 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.group;

import java.util.*;
import java.io.*;
import java.nio.channels.spi.*;
import java.nio.channels.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.imq.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.service.*;
import com.sun.messaging.jmq.jmsserver.pool.*;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;


class ReadThread extends SelectThread
{
    int selector_cnt = 0;

    public ReadThread(Service svc, MapEntry entry) 
        throws IOException
    {
        super(svc, entry);

        type = "read";
        INITIAL_KEY=SelectionKey.OP_READ; // none
        POSSIBLE_MASK=SelectionKey.OP_READ; // none
    } 

    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("TYPE", "ReadThread");
        ht.put("selector_cnt", new Integer(selector_cnt));
        return ht;
    }


    protected void wakeup() {
        Selector s = selector;
        if (s != null)
            s.wakeup();
    }

    protected void process() 
        throws IOException
    {
       Selector s = selector;
       if (s == null)
          throw new IOException("connection gone");
       int cnt =  0;
       try {
          cnt = s.select(TIMEOUT);
       } catch (java.nio.channels.CancelledKeyException ex) {
         // bug 4944894
         // nio can throw the cancelledKeyException all the
         // way up in some cases, this does not indicate that
         // the selector is closed so the broker should ignore
         // the issue

          return;
       }
       if (cnt > 0) {
           Set keys = s.selectedKeys();
           Iterator keyitr = keys.iterator();
           while (keyitr.hasNext()) {
               SelectionKey key = (SelectionKey)keyitr.next();
               IMQIPConnection con = (IMQIPConnection)key.attachment();
               try {
                   int result =  con.readData();
                   // triggers bug 4708106
                   //if (result == Operation.PROCESS_WRITE_INCOMPLETE) {
                       keyitr.remove();
                   //}
               } catch (BrokerException ex) {
                   removeConnection(con, ex.getMessage());
                   keyitr.remove();
               } catch (IOException ex) {
                   String reason = (con.getDestroyReason() == null ?
                        (ex instanceof EOFException ? 
                            Globals.getBrokerResources().getKString(
                    BrokerResources.M_CONNECTION_CLOSE) 
                       : ex.toString()) : con.getDestroyReason());
                   removeConnection(con, reason);
                   keyitr.remove();
               }
           }
       }
    }

}
