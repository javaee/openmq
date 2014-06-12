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
 * @(#)ThreadedListProcessor.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.util;

import java.lang.Thread;
import java.util.List;
import java.util.ArrayList;
import com.sun.messaging.jmq.util.MQThread;


/**
 * this is a simple class which provides for a class which allows
 * you to automatically thread off tasks if they become too time
 * intensive ..
 * new tasks will be added in order to the task list until all
 * tasks have been processes, then the thread will exit
 */

public abstract class ThreadedListProcessor implements Runnable
{
    private Thread thr = null;
    private List list = null;
    private String name = null;

    protected static final long DEFAULT_TIME=10000;

    public ThreadedListProcessor() {
        this(null);
    }

    public ThreadedListProcessor(String name) {
        if (name == null) {
            this.name = this.toString();
        } else {
            this.name = name;
        }
   }

    /**
     * lifetime is the length of time the thread will live
     * if a new object is not added to the process list
     */
    protected long getLifeTime() {
        return DEFAULT_TIME;
    }

    abstract protected boolean startThreading(ThreadedTask e);
    abstract protected void process(ThreadedTask q);


    protected synchronized final void add(ThreadedTask q) {
        if (thr == null && startThreading(q)) {
            if (list == null)
                list = new ArrayList();
            thr = new MQThread(this, name);
            thr.start();
        }
        if (thr != null) {
            list.add(q);
            notifyAll();
        } else {
            process(q);
        }
    }

    public synchronized void clear() {
        if (list != null)
            list.clear();
         thr = null;
    }


    public void run() {
        long time = getLifeTime();
        while (true) {
            ThreadedTask entry = null;
            synchronized (this) {

                if (thr == null) {
                   // we are done, exit the thread
                   break;
                }
                if (list.size() == 0) {
                    try {
                        wait(time);
                    } catch (InterruptedException ex) {
                    }
                }
                if (list.size() <= 0) {
                    // OK .. the thread has been inactive for a while,
                    // let it do
                    thr = null;
                    break;
                }
                entry = (ThreadedTask)list.remove(0);
                if (entry != null) {
                     process(entry);
                     Thread.currentThread().yield();
                }
           }
            
        }
    }

}
