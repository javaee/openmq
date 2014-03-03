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
 * @(#)MemoryCallback.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.memory;


/**
 * this class is used by the Client->Broker or
 * broker->broker flow control to request notification
 * when messages can resume
 *
 * Any client interested in receiving notifications when
 * either memory levels have changed or a specific level
 * of free memory should implement this method and register
 * using the <I>MemoryManager:registerMemoryCallback</I> method.
 * When memory levels change, updateMemory will be called.
 * <P>
 * To receive callbacks when a specific amount of memory is
 * available OR resume should be called, register the Callback
 * with <I>MemoryManager:notifyWhenAvailable()</I>.
 *
 * @see MemoryManager
 */

public interface MemoryCallback
{
    /**
     * called in respones to a notifyWhenAvailable request
     *
     * @param cnt value of JMQSize at this time
     * @param memory value of JMQBytes at this time
     * @param max value of JMQMaxMsgBytes
     */
    public void resumeMemory(int cnt, long memory, long max);

    /**
     * called when the memory level has been changed
     * because of a state change (e.g. green -> yellow)
     *
     * @param cnt value of JMQSize at this time
     * @param memory value of JMQBytes at this time
     * @param max value of JMQMaxMsgBytes
     */
    public void updateMemory(int cnt, long memory, long max);
}
/*
 * EOF
 */
