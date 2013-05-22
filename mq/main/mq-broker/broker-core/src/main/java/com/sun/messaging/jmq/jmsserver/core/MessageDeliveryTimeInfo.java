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

package com.sun.messaging.jmq.jmsserver.core;

import java.util.*;
import com.sun.messaging.jmq.io.SysMessageID;

public class MessageDeliveryTimeInfo 
{
    static Comparator deliveryTimeCompare = new DeliveryTimeComparator();

    static class DeliveryTimeComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {

            if (!(o1 instanceof MessageDeliveryTimeInfo &&
                  o2 instanceof MessageDeliveryTimeInfo)) {
                throw new RuntimeException(
                "Internal Error: unexpected object type passed to "+
                "MessageDeliveryTimeInfo.compare("+o1+", "+o2+")");
            }
            MessageDeliveryTimeInfo di1=(MessageDeliveryTimeInfo)o1;
            MessageDeliveryTimeInfo di2=(MessageDeliveryTimeInfo)o2;
            long diff =  di1.deliveryTime - di2.deliveryTime;
            if (diff != 0L) {  
                return (diff > 0L ? 1:-1);
            }
            SysMessageID sys1 = di1.id;
            SysMessageID sys2 = di2.id;
            diff = sys2.getTimestamp() - sys1.getTimestamp();
            if (diff == 0L) {
                diff = sys2.getSequence() - sys1.getSequence();
            }
            if (diff == 0L) {
                return 0;
            }
            return (diff < 0L ? 1:-1);
        }

        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(Object o) {
            return super.equals(o);
        }
    }

    private SysMessageID id =  null;
    private long deliveryTime = 0L;
    private boolean deliveryDue = false;
    private boolean deliveryReady = false;
    private boolean inprocessing = false;
    private Boolean onTimerState = null; //null, true, false

    private MessageDeliveryTimeTimer readyListener = null;

    public String toString() {
        return "DeliveryTimeInfo["+id+", "+deliveryTime+"]"+deliveryDue;
    }

    public boolean isDeliveryDue() {
        long currtime = System.currentTimeMillis();
        synchronized(this) {
            if (!deliveryDue) {
                deliveryDue = (deliveryTime <= currtime);
            }
            return deliveryDue;
        }
    }

    public synchronized boolean setInProcessing(boolean b) {
        if (!b) {
            inprocessing = false;
            return true;
        }
        if (inprocessing) {
            return false;
        }
        inprocessing = true;
        return true;
    }

    public synchronized Boolean getOnTimerState() {
        return onTimerState;
    }

    public synchronized void setOnTimerState() {
        onTimerState = Boolean.TRUE;
    }

    public synchronized void setOffTimerState() {
        onTimerState = Boolean.FALSE;
    }

    protected synchronized void setDeliveryReadyListener(
                            MessageDeliveryTimeTimer l) {
        readyListener = l;
    }

    protected void cancelTimer() {
        MessageDeliveryTimeTimer listener = null;
        synchronized(this) {
            if (readyListener != null) {
                listener = readyListener;
            }
        }
        if (listener != null) {
            listener.removeMessage(this);
        }
    }

    public void setDeliveryReady() {
        MessageDeliveryTimeTimer listener = null;
        synchronized(this) {
            deliveryReady = true;
            if (readyListener != null) {
                listener = readyListener;
                readyListener = null;
            }
        }
        if (listener != null) {
            listener.deliveryReady(this);
        }
    }

    public synchronized boolean isDeliveryReady() {
        return deliveryReady;
    }

    public static Comparator getComparator() {
        return deliveryTimeCompare;
    }

    public MessageDeliveryTimeInfo(SysMessageID id, long deliveryTime) {
        this.id = id;
        this.deliveryTime = deliveryTime;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public SysMessageID getSysMessageID() {
        return id;
    }

    public int hashCode() {
        return id.hashCode();
    }
    public boolean equals(Object o) {
        if (!(o instanceof MessageDeliveryTimeInfo)) {
            return false;
        }
        MessageDeliveryTimeInfo di = (MessageDeliveryTimeInfo)o;
        if (id == null || di == null || di.id == null) {
            throw new RuntimeException(
            "Internal Error: unexpected values on "+
            "MessageDeliveryTimeInfo.equals("+di+")"+this.id);
        }
        return id.equals(di.id);
    }
}

