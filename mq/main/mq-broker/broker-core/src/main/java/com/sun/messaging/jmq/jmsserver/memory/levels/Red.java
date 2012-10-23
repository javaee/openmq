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
 * @(#)Red.java	1.11 07/02/07
 */ 

package com.sun.messaging.jmq.jmsserver.memory.levels;

import com.sun.messaging.jmq.jmsserver.memory.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.util.log.*;


public class Red extends MemoryLevelHandler
{
    protected static final boolean SWAP_NON_PERSIST = false;

    protected static final int GC_DEFAULT=10;
    protected static final int GC_ITR_DEFAULT=10;
    protected int GCCount =0;
    protected int GCItrCount =0;

    public Red(String name) {
        super(name);
        MEMORY_NAME_KEY = BrokerResources.M_MEMORY_RED;
        GCCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".gccount", GC_DEFAULT);
        GCItrCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".gcitr", GC_ITR_DEFAULT);
    }

    public int getMessageCount(long freeMem, int producers) {
        return PAUSED;
    }

    public long getMemory(long freeMemory, int producers) {
        return PAUSED;
    }

    public int gcCount() {
        return GCCount;
    }

    public int gcIteration() {
        return GCItrCount;
    }

    public boolean cleanup(int cnt) {
        super.cleanup(cnt);

        switch (cnt) {

            case 0: // clean up persistent messages
                logger.log(Logger.INFO,BrokerResources.I_LOW_MEMORY_FREE);
                logger.log(Logger.DEBUG,"Broker is swapping all persistent messages");
  
                //LKS - XXX 
                //PacketReference.inLowMemoryState(true);
                break;
            default:
                assert false ;
        }

        // if we are on the first iteration and SWAP_NON_PERSIST
        // is true -> return false so we go around for another
        // iteration IF we stay in RED

        return !SWAP_NON_PERSIST || cnt == 1;
    }

    public boolean enter(boolean fromHigher) {
        super.enter(fromHigher);

        if (fromHigher) return true;

        MemoryGlobals.setMEM_FREE_P_ALL(true);
        MemoryGlobals.setMEM_DISALLOW_PRODUCERS(true);
        MemoryGlobals.setMEM_DISALLOW_CREATE_DEST(true);

        try {
            Globals.getClusterBroadcast().pauseMessageFlow();
        } catch (Exception ex) {
             logger.logStack(Logger.DEBUG,"Got exception in Red", ex);
        }

        return true; // change cnt/etc
    }

    public boolean leave(boolean toHigher)  {
        super.leave(toHigher);

        if (toHigher) {
            // we went up a level, dont do anything
            return true;
        }
        MemoryGlobals.setMEM_FREE_NP_ALL(true);
        MemoryGlobals.setMEM_FREE_P_ALL(false);
        MemoryGlobals.setMEM_DISALLOW_PRODUCERS(false);
        MemoryGlobals.setMEM_DISALLOW_CREATE_DEST(false);

        try {
            Globals.getClusterBroadcast().resumeMessageFlow();
        } catch (Exception ex) {
             logger.logStack(Logger.DEBUG,"Got exception in Red", ex);
        }

        return true; // we have to notify, the client wont fix
                     // itsself anymore
    }


}


/*
 * EOF
 */
