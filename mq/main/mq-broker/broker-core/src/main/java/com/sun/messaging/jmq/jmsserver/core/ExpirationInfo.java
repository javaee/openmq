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
 * @(#)ExpirationInfo.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.core;

import java.util.*;
import com.sun.messaging.jmq.io.SysMessageID;

public class ExpirationInfo 
{
    static Comparator expireCompare = new ExpirationComparator();


    static class ExpirationComparator implements Comparator
    {
        public int compare(Object o1, Object o2) 
        {
            if (o1 instanceof ExpirationInfo &&
                o2 instanceof ExpirationInfo) 
            {
                 ExpirationInfo ei1=(ExpirationInfo)o1;
                 ExpirationInfo ei2=(ExpirationInfo)o2;
                 long dif = ei2.expireTime - ei1.expireTime;
                 if (dif == 0) {
                     SysMessageID sys1 = ei1.id;
                     SysMessageID sys2 = ei2.id;
                     dif = sys2.getTimestamp() - sys1.getTimestamp();
                     if (dif == 0)
                        dif = sys2.getSequence() - sys1.getSequence();
                }

                if (dif < 0) return 1;
                if (dif > 0) return -1;
                return 0;
             }
            assert false;
            return o1.hashCode() - o2.hashCode();
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object o1) 
        {
            return super.equals(o1);
        }
    }

    SysMessageID id;
    long expireTime;
    boolean expired = false;
    int reapCount = 0;

    public String toString() {
        return "ExpirationInfo[" + id + "," + expireTime + "]";
    }

    public synchronized boolean isExpired() {
        if (!expired) {
            expired = (expireTime <= System.currentTimeMillis());
        }
        return expired;
    }

    public static Comparator getComparator()
    {
        return expireCompare;
    }

    public ExpirationInfo(SysMessageID id, long expireTime)
    {
        this.id = id;
        this.expireTime = expireTime;
    }
    public long getExpireTime() {
        return expireTime;
    }

    public int getReapCount() {
        return reapCount;
    }

    public void incrementReapCount() {
        reapCount++;
    }

    public void clearReapCount() {
        reapCount = 0;
    }

    public SysMessageID getSysMessageID() {
        return id;
    }

    public int hashCode() {
        return id.hashCode();
    }
    public boolean equals(Object o) {
        if (!(o instanceof ExpirationInfo)) {
            return false;
        }
        ExpirationInfo ei = (ExpirationInfo)o;
        assert id != null && ei != null && ei.id != null;
        return id.equals(ei.id);
    }
}

