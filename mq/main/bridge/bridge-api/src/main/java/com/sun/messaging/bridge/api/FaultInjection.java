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
 */ 

package com.sun.messaging.bridge.api;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.messaging.jmq.util.RuntimeFaultInjection;
import com.sun.messaging.bridge.api.BridgeBaseContext;

/**
 * All fault target constants start with FAULT_ and
 * only fault target constant starts with FAULT_
 *
 */
public class FaultInjection extends RuntimeFaultInjection
{
     private static BridgeBaseContext _bc = null;

     private Logger _logger = null;

     private static FaultInjection _fault = null;

     /**
      * 1 is before func call 
      * 2 is after func call
      */
     public static final String STAGE_1 = "1";
     public static final String STAGE_2 = "2";

     /*********************************************************************************
      * example usages:
      *
      * imqbridgemgr debug fault -n receive.2 -debug
      * imqbridgemgr debug fault -n xa.prepare.1 -o selector="cfref = 'CF9666'" -debug
      *
      *********************************************************************************/

     /*****************************************************************
      * START of JMS Bridge faults
      *****************************************************************/

     /**
      * faults for transacted links
      *
      * throw XAException(String) on next specified operation with cfref
      ******************************************************************/
     public static final String FAULT_XA_START_1 = "xa.start.1";
     public static final String FAULT_XA_START_2 = "xa.start.2";
     public static final String FAULT_XA_END_1 = "xa.end.1";
     public static final String FAULT_XA_END_2 = "xa.end.2";
     public static final String FAULT_XA_PREPARE_1 = "xa.prepare.1";
     public static final String FAULT_XA_PREPARE_2 = "xa.prepare.2";
     public static final String FAULT_XA_COMMIT_1 = "xa.commit.1";
     public static final String FAULT_XA_COMMIT_2 = "xa.commit.2";
     public static final String FAULT_XA_ROLLBACK_1 = "xa.rollback.1";
     public static final String FAULT_XA_ROLLBACK_2 = "xa.rollback.2";
     public static final String FAULT_XA_RECOVER_1 = "xa.recover.1";

     
     /**
      * faults for both transacted and non-transacted links
      *
      * throw JMSException on next specified operation 
      ***********************************************************/
     public static final String FAULT_RECEIVE_1 = "receive.1";
     public static final String FAULT_RECEIVE_2 = "receive.2";
     public static final String FAULT_TRANSFORM_2 = "transform.2";
     public static final String FAULT_SEND_1 = "send.1";
     public static final String FAULT_SEND_2 = "send.2";

     /**
      * faults for dmq
      *
      * throw JMSException on next specified operation with dmqName 
      **************************************************************/
     public static final String FAULT_DMQ_SEND_1 = "dmq.send.1";
     public static final String FAULT_DMQ_TRANSFORM_2 = "dmq.transform.2";

     /**
      * faults for non-transaced links
      *
      * throw JMSException on next specified operation 
      ******************************************************/
     public static final String FAULT_ACK_1 = "ack.1";
     public static final String FAULT_ACK_2 = "ack.2";
     
     /**
      * fault properties 
      ***********************************************************/
     //for dmq faults
     public static final String DMQ_NAME_PROP = "dmqName";

     //for xa transaction faults
     public static final String CFREF_PROP = "cfref";

     /******************************************************************
      * END of JMS Bridge faults
      ******************************************************************/

     private static final String SLEEP_INTERVAL_PROP = "mqSleepInterval"; //in secs
     private static final int   SLEEP_INTERVAL_DEFAULT = 60;

     /**
      * This method need to be called before constructor
      */
     public static void setBridgeBaseContext(BridgeBaseContext bc) {
         _bc = bc;
     }

     public void setLogger(Logger l) {
         _logger = l;
     }

     public static FaultInjection getInjection()
     {
         if (_fault == null)
             _fault = new FaultInjection();

         return _fault;
     }

     public FaultInjection() {
         super();
         setProcessName((_bc.isEmbeded() ? "BROKER":"PROCESS"));
     }

     protected void exit(int exitCode) {
         logWarn("EXIST JVM from bridge is not supported", null);
     }

     protected String sleepIntervalPropertyName() {
         return SLEEP_INTERVAL_PROP;
     }

     protected int sleepIntervalDefault() {
         return SLEEP_INTERVAL_DEFAULT;
     }

     @Override
     protected void logInfo(String msg, Throwable t) {
         if (_bc != null) {
             _bc.logInfo(msg, t);
         }

         Logger logger = _logger;
         if (logger != null) {
             if (t == null) {
                 logger.log(Level.INFO, msg);
             } else {
                 logger.log(Level.INFO, msg, t);
             }
         }
     }

     @Override
     protected void logWarn(String msg, Throwable t) {
         if (_bc != null) {
             _bc.logWarn(msg, t);
         }

         Logger logger = _logger;
         if (logger != null) {
             if (t == null) {
                 logger.log(Level.WARNING, msg);
             } else {
                 logger.log(Level.WARNING, msg, t);
             }
         }
     }
}
