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
 * @(#)Event.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

/**
 * Class which represents a event on a list which may
 * generate a notification.
 *
 * @deprecated since 3.0
 * @see EventBroadcaster
 */

public class Event extends java.util.EventObject
{

    /**
     * type of this event
     */
    protected EventType id;

    /**
     * original value before the event occurred
     */
    protected Object original_value;

    /**
     * new value after the event occurred
     */
    protected Object new_value;

    /**
     * reason the event occurred
     */
    protected Reason reason;
    
    /**
     * create a new event without a reason
     */
    public Event(EventType id, Object target, Object original,
           Object newval) 
    {
        this(id, target, original, newval, null);
    }
    
    /**
     * create a new event with a reason
     */
    public Event(EventType id, Object target, Object original,
         Object newval, Reason reason) {
        super(target);
        this.id = id;
        this.reason = reason;
        this.original_value = original;
        this.new_value = newval;
    }
    
    /**
     * @returns the event type for this event
     */
    public EventType getEventType() {
        return id;
    }
   
    
    /**
     * @returns the original object for this event
     *  (may be null)
     */
    public Object getOriginalValue() {
        return original_value;
    }
    
    /**
     * @returns the current object for this event
     *  (may be null)
     */
    public Object getCurrentValue() {
        return new_value;
    }
    
    /**
     * @returns the reasont this event occurred
     *  (may be null)
     */
    public Reason getReason() {
        return reason;
    }
    
    
    /**
     * string representation of this event
     * @returns the string of this object
     */
    public String toString() {
        return id.toString() + " target(" +
               getSource() + ") Reason(" +
               getReason() + ") [was,is]=[" +
          original_value + "," + new_value + "]";
    }

    /**
     * compares this object against another object
     * @param o the object to compare
     * @returns true if the objects are the same, false otherwise
     */
    public boolean equals(Object o) {
        // compares reference to the item
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }


}
