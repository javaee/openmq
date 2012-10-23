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
 * @(#)SupportUtil.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util;


import java.lang.StackTraceElement;

import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.Iterator;

public class SupportUtil
{

    public static Hashtable getAllStackTracesAsMap() {
        Hashtable ht = new Hashtable();
        try {
            Class tclass = Thread.class;
            Method m = Thread.class.getMethod("getAllStackTraces", 
                         new Class[0]);
            Map map = (Map)m.invoke(null, new Object[0]);
            Iterator itr = map.keySet().iterator();
            String retstr = "";
            while (itr.hasNext()) {
                Thread thr = (Thread)itr.next();
                StackTraceElement[] stes = (StackTraceElement[])map.get(thr);
                String name=thr + " 0x" + 
                          Long.toHexString(thr.hashCode());
                Vector value = new Vector();
                for (int i=0; i < stes.length; i ++) {
                    value.add(stes[i].toString());
                }
                ht.put(name, value);
            }
        } catch (Throwable thr) {
           ht.put("error",  "Can not getStackTrace " + thr);
            
        }
        return ht;  
    }

    public static String getAllStackTraces(String prefix) {
        try {
            Class tclass = Thread.class;
            Method m = Thread.class.getMethod("getAllStackTraces", 
                         new Class[0]);
            Map map = (Map)m.invoke(null, new Object[0]);
            Iterator itr = map.keySet().iterator();
            String retstr = "";
            while (itr.hasNext()) {
                Thread thr = (Thread)itr.next();
                StackTraceElement[] stes = (StackTraceElement[])map.get(thr);
                retstr += prefix +  thr + " 0x" + 
                          Long.toHexString(thr.hashCode()) +
                          "\n";
                for (int i=0; i < stes.length; i ++)
                    retstr += prefix + "\t" + stes[i] + "\n";
                retstr += "\n";
            }
            return retstr;  
        } catch (Throwable thr) {
           return prefix + "Can not getStackTrace " + thr;
        }

    }

    public static String getStackTrace(String prefix) {
        Thread thr = Thread.currentThread();
        try {
            Class tclass = Thread.class;
            Method m = Thread.class.getMethod("getStackTrace", new Class[0]);
            StackTraceElement[] stes = (StackTraceElement[])m.invoke(thr, new Object[0]);
            String retstr = "";
            retstr += prefix + thr+ " 0x" + 
                      Long.toHexString(thr.hashCode()) +"\n";
            for (int i=0; i < stes.length; i ++) {
                    retstr += prefix + "\t" + stes[i] + "\n";
            }
            return retstr;  
        } catch (Throwable t) {
           return prefix + "Can not getStackTrace " + t;
        }
   }


}
