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
 * @(#)GroupNotificationInfo.java	1.15 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.group;

import java.util.*;
import java.io.*;
import java.nio.channels.*;
import com.sun.messaging.jmq.jmsserver.service.imq.*;
import com.sun.messaging.jmq.jmsserver.*;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.resources.*;

// class that incaspulates the information needed to turn on
// or off a selector

public class GroupNotificationInfo implements NotificationInfo
{
    private static boolean DEBUG = false;

    protected static int NEXTID=1;

    Logger logger = Globals.getLogger();

    SelectThread readthr;
    SelectThread writethr;

    SelectThread targetRead;
    SelectThread targetWrite;

    SelectionKey readkey;
    SelectionKey writekey;

    boolean valid = true;

    boolean readyToWrite=false;
    int id = 0;
    public GroupNotificationInfo() 
    {
        synchronized (this.getClass()) {
            id = NEXTID ++;
        }
    }

   void targetThreads(SelectThread tr, SelectThread tw) {
        targetRead = tr;
        targetWrite = tw;
   }

    public String getStateInfo() {
        String ret = "[rt,wt]=["+readthr + "," + writethr + "]\n" 
               + "\tReadState: " + (readthr == null ? null : readthr.getStateInfo()) + "\n"
               + "\tWriteState: " + (writethr == null ? null : writethr.getStateInfo()) + "\n";
        return ret;
       
    }

    public String toString() {
        return "GroupNotificationInfo["+id+"]";
    }

    public void setThread(int mask, SelectThread thr, SelectionKey key) {
        if ( (mask & SelectionKey.OP_READ) > 0) {
            setReader(thr, key);
        }
        if ( (mask & SelectionKey.OP_WRITE) > 0) {
            setWriter(thr, key);
        }
    }
    private synchronized void setReader(SelectThread readthr, SelectionKey readkey)
    {
        this.readthr=readthr;
        this.targetRead = null;
        this.readkey=readkey;
   }

    private synchronized void setWriter(SelectThread writethr, SelectionKey writekey)
    {
        this.writethr=writethr;
        this.targetWrite = null;
        this.writekey=writekey;
        setReadyToWrite((IMQConnection)writekey.attachment(), readyToWrite);
        
    }


    public synchronized void setReadyToWrite(IMQConnection con, boolean ready) {
        try {
            if (writethr != null)
                writethr.changeInterest(writekey,(ready?SelectionKey.OP_WRITE : 0), "changeState");
            readyToWrite=ready;
        } catch (IOException ex) {
            // OK .. just means we are going away
            logger.log(Logger.DEBUG, "setReadyToWrite exception", ex);
            if (ex instanceof EOFException) {
                destroy( Globals.getBrokerResources().getKString(
                    BrokerResources.M_CONNECTION_CLOSE));
            } else {
                destroy(ex.toString());
            }
            readyToWrite = false;
        }
     }

    public  synchronized void assigned(IMQConnection con, int events) 
        throws IllegalAccessException
    {
    }

    public synchronized  void released(IMQConnection con, int events)
    {
    }

    public synchronized void destroy(String reason) {
        valid = false;
        if (readthr != null && readkey != null) {
           try {
             readthr.removeConnection((IMQIPConnection)readkey.attachment(), reason);
           } catch (IOException ex) {
                logger.logStack(Logger.DEBUG, 
                     BrokerResources.E_INTERNAL_BROKER_ERROR, 
                     "unable to remove WRITE data.", ex);
           }
        }
        readthr = null;
        readkey = null;

        if (writethr != null && writekey != null) {
           try {
               writethr.removeConnection((IMQIPConnection)writekey.attachment(), reason);
           } catch (IOException ex) {
                logger.logStack(Logger.DEBUG, 
                     BrokerResources.E_INTERNAL_BROKER_ERROR, 
                     "unable to remove READ data.", ex);
           }
        }
        writethr = null;
        writekey = null;

    }

    public void dumpState() {
        dumpState("");
    }

    public void dumpState(String prefix) {
        logger.log(Logger.INFO,prefix + "writethr: " + writethr);
        logger.log(Logger.INFO,prefix + "writekey: " + writekey);
        logger.log(Logger.INFO,prefix + "readthr:  " + readthr);
        logger.log(Logger.INFO,prefix + "readkey:  " + readkey);
        logger.log(Logger.INFO,prefix + "valid:    " + valid);
    }
    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        try {
            ht.put("valid:    " , String.valueOf(valid));
            ht.put("groupInfo:    " , toString());
            ht.put("targetRead: " , String.valueOf(targetRead));
            ht.put("targetWrite: " , String.valueOf(targetWrite));
            ht.put("writethr: " , String.valueOf(writethr));
            ht.put("readthr:  " , String.valueOf(readthr));
            if (targetRead != null)
                ht.put("targetRead_state:  " , targetRead.getDebugState());
            if (targetWrite != null)
                ht.put("targetWrite_state:  " , targetWrite.getDebugState());
            if (readthr != null)
                ht.put("readthr_state:  " , readthr.getDebugState());
            if (writethr != null)
                ht.put("writethr_state:  " , writethr.getDebugState());
            ht.put("readkey:  " , String.valueOf(readkey));
            ht.put("writekey: " , String.valueOf(writekey));
            /* LKS - dont dump
            ht.put("writekey_state: " , getOps(writekey));
            ht.put("readkey_state: " , getOps(readkey));
            */
        } catch (Exception ex) {
ex.printStackTrace();
           ht.put("EXCEPTION", ex.toString());
        }
        return ht;
    }

    String getOps(SelectionKey key) {
        if (key == null)
            return "iOps[key is null]";
        return "iOps["+ getKeyString(key.interestOps()) + "]"
                  + ", rOps["+ getKeyString(key.readyOps()) + "]";
    }

    private String getKeyString(int events) {
        String str = "";
        if ((events & SelectionKey.OP_WRITE) > 0) {
            str += " WRITE ";
        }
        if ((events & SelectionKey.OP_READ) > 0) {
            str += " READ ";
        }
        return str;
    }
    
}
