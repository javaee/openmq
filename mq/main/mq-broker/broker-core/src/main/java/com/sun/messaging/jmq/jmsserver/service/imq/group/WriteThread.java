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
 * @(#)WriteThread.java	1.17 06/29/07
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


class WriteThread extends SelectThread
{
    boolean inSelect = false;
    boolean busy = false;

   
    Object selectLock = new Object();


    public void addNewConnection(IMQIPConnection conn)
        throws IOException
    {
        synchronized(pending_connections) {
            busy=true;
            super.addNewConnection(conn);
        }
    }

    public Hashtable getDebugState() {
        Hashtable ht = super.getDebugState();
        ht.put("TYPE", "WriteThread");
        ht.put("busy", Boolean.valueOf(busy));
        ht.put("inSelect", Boolean.valueOf(inSelect));
        ht.put("selector_cnt", Integer.valueOf(selector_cnt));
        ht.put("WriteThread", String.valueOf(this.hashCode()));
        return ht;
    }


    public void changeInterest(SelectionKey key, int mask, String reason) 
        throws IOException
    {
        super.changeInterest(key,mask, reason);
        wakeup();
    }


    public void wakeup() {
        synchronized(selectLock) {
            Selector s= selector;
            busy=true;
            if (inSelect && s != null) {
                s.wakeup();
            }
            selectLock.notifyAll();
        }
    }
                 
    public WriteThread(Service svc, MapEntry entry) 
        throws IOException
    {
        super(svc, entry);
        type = "write";
        INITIAL_KEY=0; // none
        POSSIBLE_MASK=SelectionKey.OP_WRITE; // none
    } 

    int selector_cnt = 0;

    protected  void process() 
        throws IOException
    {
        busy=false;

        Iterator itr = (new HashSet(all_connections.values())).iterator();
        while (itr.hasNext()) {
            IMQIPConnection con = (IMQIPConnection)itr.next();
            try {
                int ret=con.writeData(false);
                switch(ret) {
                    case Operation.PROCESS_PACKETS_REMAINING:
                        busy=true;
                        break;
                    case Operation.PROCESS_PACKETS_COMPLETE:
                        break;
                    case Operation.PROCESS_WRITE_INCOMPLETE:
                        SelectionKey key = (SelectionKey)key_con_map.get(con);
                        key.interestOps(POSSIBLE_MASK);
                        selector_cnt ++;
                        break;
                }
            } catch (IOException ex) {
                removeConnection(con, ex.toString()); // cancel
                itr.remove();
            }

        }

        Selector s = selector;
/*  NOTE: while it shouldnt be necessary to go into select
    its the only way we can get nio to release our file descriptor
    if we canceled the key
        if (selector_cnt > 0) {
*/
            int cnt = 0;
            if (s != null)  {
                try {
                    cnt = s.selectNow();
                } catch (java.nio.channels.CancelledKeyException ex) {
                    // bug 4944894
                    // nio can throw the cancelledKeyException all the
                    // way up in some cases, this does not indicate that
                    // the selector is closed so the broker should ignore
                    // the issue
                }
            }
            if (cnt > 0) {
                Set keys = s.selectedKeys();
                Iterator keyitr = keys.iterator();
                while (keyitr.hasNext()) {
                    busy=true;
                    SelectionKey key = (SelectionKey)keyitr.next();
                    key.interestOps(INITIAL_KEY);
                    keyitr.remove();
                    selector_cnt --;
                
                }
            }
/* See note above
        }
*/
        synchronized (selectLock) {
            if (!busy) {

                 if (selector_cnt > 0) {
                     inSelect = true;
                 } else {
                    try {
                        selectLock.wait(TIMEOUT);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        if (inSelect) {
            int selectcnt = 0;
            if (s != null)  {
                try {
                    selectcnt = s.select(TIMEOUT);
                } catch (java.nio.channels.CancelledKeyException ex) {
                    // bug 4944894
                    // nio can throw the cancelledKeyException all the
                    // way up in some cases, this does not indicate that
                    // the selector is closed so the broker should ignore
                    // the issue
                }
            }
            if (selectcnt > 0) {
                 Set keys = s.selectedKeys();
                 Iterator keyitr = keys.iterator();
                 while (keyitr.hasNext()) {
                     busy=true;
                     SelectionKey key = (SelectionKey)keyitr.next();
                     key.interestOps(INITIAL_KEY);
                     keyitr.remove();
                     selector_cnt --;
                 } 
            }
        }
        synchronized (selectLock) {
            inSelect = false;
        }
 
    }


}
