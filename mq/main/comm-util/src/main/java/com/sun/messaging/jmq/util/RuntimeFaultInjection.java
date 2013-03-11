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
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import com.sun.messaging.jmq.util.selector.*;

/**
 *
 */
public abstract class RuntimeFaultInjection 
{
     private LoggerWrapper logger = null;
     private java.util.logging.Logger jlogger = null;

     private Set injections = null;
     private Map injectionSelectors = null;
     private Map injectionProps = null;

     private String shutdownMsg = "SHUTING DOWN BECAUSE OF" + " FAULT ";
     private String haltMsg = "HALT BECAUSE OF" + " FAULT ";

     public boolean FAULT_INJECTION = false;

     /**
      * @param name the process name
      */
     public void setProcessName(String name) {
         shutdownMsg = "SHUTING DOWN "+name+" BECAUSE OF" + " FAULT ";
         haltMsg = "HALT "+name+" BECAUSE OF" + " FAULT ";
     }

     public RuntimeFaultInjection() {
         injections = Collections.synchronizedSet(new HashSet());
         injectionSelectors = Collections.synchronizedMap(new HashMap());
         injectionProps = Collections.synchronizedMap(new HashMap());
     }

     protected void setLogger(Object l) {
        if (l instanceof com.sun.messaging.jmq.util.LoggerWrapper) {
            logger = (LoggerWrapper)l;
        } else if (l instanceof java.util.logging.Logger) {
            jlogger = (java.util.logging.Logger)l;
        }
    }

     public void setFault(String fault, String selector)
         throws SelectorFormatException
     {
         setFault(fault, selector, null);
     }

     public void setFault(String fault, String selector, Map props)
         throws SelectorFormatException
     {
         logInfo("Setting Fault "+fault+"[ selector=" + selector + "], [props="+props+"]");
         injections.add(fault);
         if (selector != null && selector.length() != 0) {
            // create a selector and insert
            Selector s = Selector.compile(selector);
            injectionSelectors.put(fault, s);
         }
         if (props != null)
            injectionProps.put(fault, props);

     }

     public void unsetFault(String fault) {
         logInfo("Removing Fault " + fault );
         injections.remove(fault);
         injectionSelectors.remove(fault);
         injectionProps.remove(fault);
     }

     class FaultInjectionException extends Exception
     {
         public String toString() {
             return "FaultInjectionTrace";
         }
     }

     public void setFaultInjection(boolean inject)
     {
         if (FAULT_INJECTION != inject) {
            if (inject) {
                logInfo("Turning on Fault Injection");
            } else {
                logInfo("Turning off Fault Injection");
            }
            FAULT_INJECTION = inject;
         }
     }


     private void logInjection(String fault, Selector sel)
     {
         String str = "Fault Injection: triggered " + fault;
         if (sel != null)
             str += " selector [ " + sel.toString() + "]";

         Exception ex = new FaultInjectionException();
         ex.fillInStackTrace();
         logInfo(str, ex);
     }

     private Map checkFaultGetProps(String fault, Map props)
     {
         if (!FAULT_INJECTION) return null;
         boolean ok = checkFault(fault, props);
         if (!ok) return null;
         Map m = (Map)injectionProps.get(fault);
         if (m == null) m  = new HashMap();
         return m;
     }

     public boolean checkFault(String fault, Map props)
     {
         return checkFault(fault, props, false);              
     }

     private boolean checkFault(String fault, Map props, boolean onceOnly)
     {
         if (!FAULT_INJECTION) return false;
         if (injections.contains(fault))
         {
             Selector s = (Selector)injectionSelectors.get(fault);
             if (s == null) {
                 logInjection(fault, null);
                 if (onceOnly) injections.remove(fault);
                 return true;
             }
             try {
                 boolean match = s.match(props, null); 
                 if (match) {
                     logInjection(fault, s);
                     if (onceOnly) injections.remove(fault);
                     return true;
                 }
                 return false;
             } catch (Exception ex) {
                 logWarn("Unable to apply fault ", ex);
                 return false;
             }
         }

         return false;
     }

     public void checkFaultAndThrowIOException(String value,
                Map props)
          throws IOException
     {
         if (!FAULT_INJECTION) return;
         if (checkFault(value, props)) {
             IOException ex = new IOException("Fault Insertion: "
                   + value);
             throw ex;
         }    
     }

     public void checkFaultAndThrowException(String value,
                Map props, String ex_class)
          throws Exception
     {
         checkFaultAndThrowException(value, props, ex_class, false);
     }

     public void checkFaultAndThrowException(String value,
                Map props, String ex_class, boolean onceOnly)
          throws Exception
     {
         if (!FAULT_INJECTION) return;
         if (checkFault(value, props, onceOnly)) {
             Class c = Class.forName(ex_class);
             Class[] paramTypes = { String.class };
             Constructor cons = c.getConstructor(paramTypes);
             Object[] paramArgs = { new String("Fault Injection: " +value) };
             Exception ex = (Exception)cons.newInstance(paramArgs);
             throw ex;
         }    
     }

     public void checkFaultAndThrowError(String value, Map props)
          throws Error
     {
         if (!FAULT_INJECTION) return;
         if (checkFault(value, props)) {
             // XXX use exclass to create exception
             Error ex = new Error("Fault Insertion: "
                   + value);
             throw ex;
         }    
     }

     public void checkFaultAndExit(String value,
                Map props, int exitCode, boolean nice)
     {
         if (!FAULT_INJECTION) return;
         if (checkFault(value, props)) {
             if (nice) {
                 logInfo(shutdownMsg + value);
                 exit(exitCode);
             } else {
                 logInfo(haltMsg + value);
                 Runtime.getRuntime().halt(exitCode);
             }
         }
     }

     protected abstract void exit(int exitCode);
     protected abstract String sleepIntervalPropertyName();
     protected abstract int sleepIntervalDefault();

     public boolean checkFaultAndSleep(String value, Map props) {
         return checkFaultAndSleep(value, props, false);
     }

     public boolean checkFaultAndSleep(String value, Map props, boolean unsetFaultBeforeSleep)
     {
         if (!FAULT_INJECTION) {
             return false;
         }
         Map p = checkFaultGetProps(value, props);
         if (p == null) {
             return false;
         }
         String str = (String)p.get(sleepIntervalPropertyName());
         int secs = sleepIntervalDefault();
         if (str != null)  {
             try {
                 secs = Integer.valueOf(str).intValue();
             } catch (Exception e) {}
         }
         if (secs <= 0) {
             secs = sleepIntervalDefault();
         }
         if (unsetFaultBeforeSleep) {
             unsetFault(value);
         }
         logInfo("BEFORE SLEEP "+secs +"(seconds) BECAUSE OF FAULT "+value);
         try {
             Thread.sleep(secs*1000);
         } catch (Exception e) {
             logInfo("SLEEP "+secs +"(seconds) FAULT ("+value+
                                    ") interrupted: "+e.getMessage());
         }
         logInfo("AFTER SLEEP "+secs +"(seconds) BECAUSE OF FAULT "+value);
         return true;
     }

     private void logInfo(String msg) {
         logInfo(msg, null);
     }

     protected void logInfo(String msg, Throwable t) {
        if (logger != null) {
            logger.logInfo(msg, t);
        } else if (jlogger != null) {
            if (t == null) {
            jlogger.log(java.util.logging.Level.INFO, msg);
            } else {
            jlogger.log(java.util.logging.Level.INFO, msg, t);
            }
        }
    }

    protected void logWarn(String msg, Throwable t) {
        if (logger != null) {
            logger.logWarn(msg, t);
        } else if (jlogger != null) {
            if (t == null) {
            jlogger.log(java.util.logging.Level.WARNING, msg);
            } else {
            jlogger.log(java.util.logging.Level.WARNING, msg, t);
            }
        }
    }
}     
