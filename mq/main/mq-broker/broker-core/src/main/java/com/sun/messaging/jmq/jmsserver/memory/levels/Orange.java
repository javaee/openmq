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
 * @(#)Orange.java	1.11 07/02/07
 */ 

package com.sun.messaging.jmq.jmsserver.memory.levels;

import com.sun.messaging.jmq.jmsserver.memory.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.util.log.*;


public class Orange extends Yellow
{
    protected int GC_DEFAULT=5;
    protected int GC_ITR_DEFAULT=100;
    protected int GCCount =0;
    protected int GCItrCount =0;

    public Orange(String name) {
        super(name);
        MEMORY_NAME_KEY = BrokerResources.M_MEMORY_ORANGE;
        messageCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".count", 1);
        GCCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".gccount", GC_DEFAULT);
        GCItrCount = Globals.getConfig().getIntProperty(
                         Globals.IMQ + "." + name + ".gcitr", GC_ITR_DEFAULT);
    }

    public int getMessageCount(long freeMem, int producers) {
        return messageCount; // 1
    }

    public long getMemory(long freeMemory, int producers) {
        if (producers >=0) producers = 1; // dont divide by 0
        return (freeMemory - MAX_MEMORY_DELTA) / producers/2;
    }

    public int gcCount() {
        return GCCount;
    }

    public int gcIteration() {
        return GCItrCount;
    }

    public boolean cleanup(int cnt) {
        super.cleanup(cnt);
        return true;
    }

    public boolean enter(boolean fromHigher) {
        super.enter(fromHigher);

        if (fromHigher) return false;

        //MemoryGlobals.setMEM_FREE_P_NOCON(true);
        MemoryGlobals.setMEM_EXPLICITLY_CHECK(true);

        return true; // change cnt/etc
    }

    public boolean leave(boolean toHigher)  {
        super.leave(toHigher);
        if (toHigher) {
            // moving to a new level, dont do anything
            return false;
        }
        // otherwise, reset to previous state
        // memory state varialbles

        //MemoryGlobals.setMEM_FREE_P_NOCON(false);
        MemoryGlobals.setMEM_EXPLICITLY_CHECK(false);

        return false; // dont bother to tell the client that the
                      // counts have changed -> it will fix itsself
    }


}

/*
 * EOF
 */
