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
 * @(#)LockTable.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.util.*;
/**
 * this is a generic class which allows you to wait for notification
 * that an event has occurred even if the objects are different but
 * equivalent (generate the same hashCode/isequals == true)
 */

public class LockTable
{
    HashMap notifyTable = new HashMap();
    
    /**
     * Request notification when notifiy is called on the same or
     * equivalent instance of an object. This method should be called
     * before any notification could occur.
     * @param object object to wait for notification on
     * @throws IllegalAccessException indicates the system is already waiting
     *                 on that object
     */
    public  void requestNotify(Object object) throws IllegalAccessException {
        synchronized(notifyTable) {
            if (notifyTable.containsKey(object))
                throw new IllegalAccessException("Already waiting for " + object);
            Object lock = new IDLock();
            notifyTable.put(object, lock);
        }   
    }

    /**
     * Cancel notification on an object. Cleans up any
     * allocated resources. This call does NOT wake up
     * any resources waiting for notification.
     * @param object equivalent object to the one passed into
     *               requestNotify
     */
    public void cancelNotify(Object object) {
        synchronized(notifyTable) {
            notifyTable.remove(object);
        }
    }

    // we cant just sync on interest .. it could be a different
    // object w/ the same contents

    /**
     * Wait for notification on an object.
     *
     * @param object equivalent object to the one passed into
     *               requestNotify
     */
    public void wait(Object object) {
        wait(object, 0 /* no timeout */);    
    }


    /**
     * Waits up to a timeout for notification on an object.
     *
     * @param timeout time (in milliseconds) to wait for the
     *                notification
     * @param object equivalent object to the one passed into
     *               requestNotify
     * @returns true if the notification was received, false
     *        if the system timed out.
     */
    public  boolean wait(Object object, long timeout) {
        // OK first get the lock
        IDLock lock = null;
        synchronized (notifyTable) {
            lock = (IDLock)notifyTable.get(object);
        }
        if (lock == null) return true; // done
        synchronized(lock) {
            if (lock.isValid()) {
                try {
                    lock.wait(timeout);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (lock.isValid() ) {
                // wait did not complete
                return false;
            }
            
            synchronized(notifyTable) {
                lock = (IDLock)notifyTable.remove(object);
            }
            return true;

        }
    }

    /**
     * Notify the system that the operation has completed.
     *
     * @param object equivalent object to the one passed into
     *               requestNotify
     */
    public void notify(Object object) {
        IDLock lock = null;
        synchronized (notifyTable) {
            lock = (IDLock)notifyTable.get(object);
        }
        if (lock == null) return;
        synchronized(lock) {
            lock.destroy();
            lock.notify();
        }
    }
}


class IDLock {
    boolean valid = true;
    public IDLock() {
    }
    public void destroy() {
        valid = false;
    }
    public boolean isValid() {
        return valid;
    }
}

