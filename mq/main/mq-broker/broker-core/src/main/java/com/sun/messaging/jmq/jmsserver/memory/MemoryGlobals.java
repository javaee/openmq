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
 * @(#)MemoryGlobals.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.memory;

import com.sun.messaging.jmq.jmsserver.Globals;

/** 
 * This class contains globals which are used for memory mgt
 * through out the broker.
 *
 * Individual MemoryLevelHandlers may change the state of these
 * values
 */


public class MemoryGlobals
{

// Variables controled by various memory management levels
    /**
     * automatically free all persistent messages
     * that have been sent but not acknowledged
     */
    public static boolean MEM_FREE_P_ACKED = false;

    public static void setMEM_FREE_P_ACKED(boolean ack) {
        MEM_FREE_P_ACKED=ack;
    }

    /**
     * automatically swap all non-persistent messages
     * that have been sent but not acknowledged
     * (not currently used)
     */
    public static boolean MEM_FREE_NP_ACKED = false;

    public static void setMEM_FREE_NP_ACKED(boolean ack) {
        MEM_FREE_NP_ACKED=ack;
    }
    

    /**
     * automatically free all persistent messages
     * that have no active consumers
     */
    public static boolean MEM_FREE_P_NOCON = false;

    public static void setMEM_FREE_P_NOCON(boolean ack) {
        MEM_FREE_P_NOCON=ack;
    }

    /**
     * automatically swap all non-persistent messages
     * that have no active consumers
     * (not currently used)
     */
    public static boolean MEM_FREE_NP_NOCON = false;

    public static void setMEM_FREE_NP_NOCON(boolean ack) {
        MEM_FREE_NP_NOCON=ack;
    }

    /**
     * automatically free all persistent messages
     */
    public static boolean MEM_FREE_P_ALL = false;

    public static void setMEM_FREE_P_ALL(boolean ack) {
        MEM_FREE_P_ALL=ack;
    }

    /**
     * automatically swap all non-persistent messages
     */
    public static boolean MEM_FREE_NP_ALL = false;

    public static void setMEM_FREE_NP_ALL(boolean ack) {
        MEM_FREE_NP_ALL=ack;
    }

    /**
     * no longer allow producers
     */
    public static boolean MEM_DISALLOW_PRODUCERS = false; 

    public static void setMEM_DISALLOW_PRODUCERS(boolean ack) {
        MEM_DISALLOW_PRODUCERS=ack;
    }



    /**
     * no longer allow new destinations to be created
     */
    public static boolean MEM_DISALLOW_CREATE_DEST = false;

    public static void setMEM_DISALLOW_CREATE_DEST(boolean ack) {
        MEM_DISALLOW_CREATE_DEST=ack;
    }



// Properties which control basic memory management behavior

    /**
     * determine whether non-persistent messages should be swapped w/
     * the current persistence implementation or the old swapping code
     */

    public static final boolean SWAP_USING_STORE = 
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.swapUsingStore", true);
    public static final boolean SWAP_NP_MSGS =
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.swapNPMsgs", true);
    public static final boolean KEEP_NP_MSGS_AT_START = 
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.keepNPMsgs", false);


    /**
     * always check memory after a packet is read into the
     * system before processing it
     */
    public static boolean MEM_EXPLICITLY_CHECK = 
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.explicitCheck", false);

    public static void setMEM_EXPLICITLY_CHECK(boolean ack) {
        MEM_EXPLICITLY_CHECK=ack;
    }

    /**
     * always check memory after a packet is read into the
     * system is larger than MEM_SIZE_TO_QUICK_CHECK
     */
    public static final boolean MEM_QUICK_CHECK= 
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.quickCheck", false);

    /**
     * Packet size for triggering MEM_QUICK_CHECK
     */
    public static final int MEM_SIZE_TO_QUICK_CHECK =
              Globals.getConfig().getIntProperty(Globals.IMQ +
                  ".memory_management.quickCheckSize", 1024*10);

 
    /**
     * automatically free persistent messages at startup
     * after processing (default)
     */
    public static final boolean MEM_FREE_AT_RESTART = 
              Globals.getConfig().getBooleanProperty(Globals.IMQ +
                  ".memory_management.freeAutomaticallyAtRestart", true);

}


