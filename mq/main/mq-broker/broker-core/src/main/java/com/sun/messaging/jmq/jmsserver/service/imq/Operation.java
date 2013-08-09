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
 * @(#)Operation.java	1.31 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import java.io.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.log.Logger;

/**
 */


public interface Operation
{
    public static final int RUNNING = 0;
    public static final int EXITING = 1;
    public static final int DESTROYED = 2;

    public static final int PROCESS_PACKETS_REMAINING = 0;
    public static final int PROCESS_PACKETS_COMPLETE = 1;
    public static final int PROCESS_WRITE_INCOMPLETE = 2;

    public boolean isValid();

    /**
     * a boolean which returns "true" if the operation has
     * been destroyed or is in code in which the thread can be
     * destroyed w/o any problems.
     */
    public boolean canKill();

    public void setCritical(boolean critical);

    public boolean waitUntilDestroyed(long time);

    public void destroy(boolean goodbye, int greason, String reasonstr);

    public void threadAssigned(OperationRunnable runner, int events) 
               throws IllegalAccessException;

    public void notifyRelease(OperationRunnable runner, int events);

    public void attach(NotificationInfo obj);

    public NotificationInfo attachment();
   
    // ---------------------------------------------

    public boolean process(int events, boolean wait) 
        throws IOException;

    public void wakeup();
    public void suspend();
    public void resume();

}

