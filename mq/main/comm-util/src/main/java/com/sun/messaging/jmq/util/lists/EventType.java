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
 * @(#)EventType.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

/**
 * Class which represents a eventType on a list which may
 * generate a notification.
 * @see Event
 */

public class EventType
{
    private int event = 0;
    private String name = null;

    /**
     * size (count) of object has changed
     */ 
    public static final EventType  SIZE_CHANGED 
                   = new EventType(0, "SIZE_CHANGED");

    /**
     * bytes of object has changed
     * @see Sized
     */ 
    public static final EventType  BYTES_CHANGED 
                   = new EventType(1, "BYTES_CHANGED");


    /**
     * the set of objects has changes (an object has been added
     * or removed)
     */ 
    public static final EventType  SET_CHANGED 
                   = new EventType(2, "SET_CHANGED");

    /**
     * the object has moved to or from an empty state
     */ 
    public static final EventType  EMPTY 
                   = new EventType(3, "EMPTY");

    /**
     * the object has moved to or from a full state
     */ 
    public static final EventType  FULL 
                   = new EventType(4, "FULL");

    /**
     * the object has moved to or from a busy state
     */ 
    public static final EventType BUSY_STATE_CHANGED
                   = new EventType(5, "BUSY_STATE_CHANGED");

    /**
     * a change to the set (an item added or removed) has been
     * requested
     */
    public static final EventType SET_CHANGED_REQUEST
                   = new EventType(6, "SET_CHANGED_REQUEST");

    public static final int EVENT_TYPE_NUM = SET_CHANGED_REQUEST.getEvent()+1;

    protected EventType(int id, String name) {
        event = id;
        this.name = name;
    }

    /**
     * integer value associated with this event type
     * @returns integer value of eventType
     */
    public final int getEvent() {
        return event;
    }

    /**
     * EventType displayed as a string
     * @returns string representing object
     */
    public String toString() {
        return name;
    }

    /**
     * compares this event type against another object.
     * @returns true if the objects are the same
     */
    public boolean equals(Object o) {
        if (o instanceof EventType) {
            return event == ((EventType)o).event;
        }
        return false;
    }

    public int hashCode() {
         return event;
    }


}
