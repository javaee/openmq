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
            Method m = Thread.class.getMethod("getAllStackTraces", 
                         new Class[0]);
            Map map = (Map)m.invoke(null, new Object[0]);
            Iterator<Map.Entry> itr = map.entrySet().iterator();
            Map.Entry me = null;
            String retstr = "";
            while (itr.hasNext()) {
                me = itr.next();
                Thread thr = (Thread)me.getKey();
                StackTraceElement[] stes = (StackTraceElement[])me.getValue();
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
            Method m = Thread.class.getMethod("getAllStackTraces", 
                         new Class[0]);
            Map map = (Map)m.invoke(null, new Object[0]);
            Iterator<Map.Entry> itr = map.entrySet().iterator();
            Map.Entry me = null;
            StringBuffer retstr = new StringBuffer();
            while (itr.hasNext()) {
                me = itr.next();
                Thread thr = (Thread)me.getKey();
                StackTraceElement[] stes = (StackTraceElement[])me.getValue();
                retstr.append(prefix +  thr + " 0x" + 
                          Long.toHexString(thr.hashCode()) +
                          "\n");
                for (int i=0; i < stes.length; i ++)
                    retstr.append( prefix + "\t" + stes[i] + "\n");
                retstr.append("\n");
            }
            return retstr.toString(); 
        } catch (Throwable thr) {
           return prefix + "Can not getStackTrace " + thr;
        }

    }

    public static String getStackTrace(String prefix) {
        Thread thr = Thread.currentThread();
        try {
            Method m = Thread.class.getMethod("getStackTrace", new Class[0]);
            StackTraceElement[] stes = (StackTraceElement[])m.invoke(thr, new Object[0]);
            StringBuffer retstr = new StringBuffer();
            retstr.append(prefix + thr+ " 0x" + 
                      Long.toHexString(thr.hashCode()) +"\n");
            for (int i=0; i < stes.length; i ++) {
                    retstr.append(prefix + "\t" + stes[i] + "\n");
            }
            return retstr.toString();  
        } catch (Throwable t) {
           return prefix + "Can not getStackTrace " + t;
        }
   }

   public static String getStackTraceString(Throwable e) {
       String str = null;
       try {
           java.io.StringWriter sw = new java.io.StringWriter();
           e.printStackTrace(new java.io.PrintWriter(sw));
           str = sw.toString();
       } catch (Throwable t) {
           str = e.toString();
       }
       return str;
   }

   /***********************************************************
    * BEGIN util of java.lang.instrument.Instrumentation
    * (see http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/Instrumentation.html)
    * These methods should not be called in MQ production code
    ***********************************************************

   private static java.lang.instrument.Instrumentation instrumentation;

   public static void premain(String args, java.lang.instrument.Instrumentation inst) {
       instrumentation = inst;
   }

   public static long getObjectSize(Object o) {
       return instrumentation.getObjectSize(o);
   }

    *******************************************************
    * END util of java.lang.instrument.Instrumentation
    *******************************************************/
 
}
