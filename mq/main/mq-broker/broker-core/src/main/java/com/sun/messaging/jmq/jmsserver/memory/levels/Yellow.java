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
 * @(#)Yellow.java	1.12 07/02/07
 */ 

package com.sun.messaging.jmq.jmsserver.memory.levels;

import com.sun.messaging.jmq.jmsserver.memory.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.service.ConnectionManager;
import com.sun.messaging.jmq.util.log.*;

public class Yellow extends Green
{

    protected static final int DEFAULT_GC_ITR = 1000;
    protected  int gcIterationCount = 0;


    public Yellow(String name) {
        super(name);
        MEMORY_NAME_KEY = BrokerResources.M_MEMORY_YELLOW;
        gcIterationCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".gcitr", DEFAULT_GC_ITR);
    }

    public int getMessageCount(long freeMemory, int producers) {
       // never divide by 0
       if (producers >= 0) producers = 1;
        return super.getMessageCount(freeMemory,producers)/producers;
    }

    public long getMemory(long freeMemory, int producers) {
       // never divide by 0
       if (producers >= 0) producers = 1;
        return super.getMemory(freeMemory,producers)/producers/2;
    }

    public int gcCount() {
        return 1;
    }

    public int gcIteration() {
        return gcIterationCount;
    }

    public boolean cleanup(int cnt) {
        super.cleanup(cnt);
        logger.log(Logger.INFO,BrokerResources.I_LOW_MEMORY_FREE);
        logger.log(Logger.DEBUG,"Broker is swapping persistent/sent but un-acked  messages");
        Globals.getConnectionManager().cleanupMemory(true /* persist */);

        return true;
    }

    public boolean enter(boolean fromHigher) {
        super.enter(fromHigher);

        if (fromHigher) return false;

        MemoryGlobals.setMEM_FREE_P_ACKED(true);

        return true; // change cnt/etc
    }

    public boolean leave(boolean toHigher)  {
        super.leave(toHigher);

        if (toHigher) {
            // went to higher level, do nothing
            return false;
        }

        MemoryGlobals.setMEM_FREE_P_ACKED( false);

        return false; // dont bother to tell the client that the
                      // counts have changed -> it will fix itsself
    }

}



/*
 * EOF
 */
