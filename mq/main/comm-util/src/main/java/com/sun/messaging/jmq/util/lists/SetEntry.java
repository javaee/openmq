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
 * @(#)SetEntry.java	1.8 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

import java.util.*;

/**
 * Entry used in the ordered list. package private
 */

class SetEntry
{
    public static boolean DEBUG = false;

    SetEntry next = null;
    SetEntry previous = null;
    boolean valid = true;
    Object data = null;

    static int ctr = 0;
    int debugid = 0;
    
    /**
     * takes a linked list which starts with the first
     * SetEntry and sorts it
     */
    public SetEntry sort(Comparator comp)
    {
        if (this.next == null) return this;

        // OK, for now we are doing this the slow/easy way
        // the assumption is that this is an infrequent operation
        Comparator realcmp = createSortComparator(comp);
        // stick everything in an array list
        ArrayList al = new ArrayList();
        SetEntry entry = this;
        al.add(this);
        while (entry.next != null) {
            al.add(entry.next);
            entry = entry.next;
        }
        // sort
        Collections.sort(al, comp);
        // now fill in the next entries
        SetEntry back = null;
        for (int i = 0; i < al.size(); i ++) {
            SetEntry fwd = (i < (al.size() -1)) ?
                            (SetEntry)al.get(i+1) : null;
            SetEntry cur =  (SetEntry)al.get(i);
            cur.previous = back;
            cur.next = fwd;
            back = cur;
        }
        return (SetEntry)al.get(0);
    }

    protected Comparator createSortComparator(Comparator comp)
    {
        return new SetEntryComparator(comp);
    }

        
    static class SetEntryComparator implements Comparator {
        Comparator datacmp = null;

        public SetEntryComparator(Comparator c) {
            datacmp = c;        
        }
        public int compare(Object o1, Object o2) {
            if (o1 instanceof SetEntry && o2 instanceof SetEntry) {
                // compare
                Object d1 = ((SetEntry)o1).data;
                Object d2 = ((SetEntry)o2).data;
                return datacmp.compare(d1, d2);
            } else if (o1 instanceof SetEntry) {
                Object d1 = ((SetEntry)o1).data;
                return datacmp.compare(d1, o2);
            } else if (o2 instanceof SetEntry) {
                Object d2 = ((SetEntry)o2).data;
                return datacmp.compare(o1, d2);
            } else if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
                return o1.hashCode() - o2.hashCode();
            }
        }
        public boolean equals(Object o1) {
            return super.equals(o1);
        }
    }

    public SetEntry(Object data) {
        if (DEBUG) {
            debugid = ctr ++; 
        } else {
            debugid = hashCode();
        }
        this.data = data;
    }

    public String toString() {
        return "SetEntry(" + debugid 
            +")[ before(" +
            (previous == null ? null : String.valueOf(previous.debugid)) 
            + ") after(" +
            (next == null ? null : String.valueOf(next.debugid)) 
            +") ] " +data+"]valid="+isValid();
    }
       

    public SetEntry getNext() {
        return next;
    }

    public SetEntry getPrevious() {
        return previous;
    }

    public Object getData() {
        return data;
    }

    public boolean isFirst() {
        return previous == null;
    }

    public boolean isLast() {
        return next == null;
    }

    public boolean isValid() {
        return valid;
    }

    // speed up gc
    public void clear() {
        previous = null;
        next = null;
        data = null;
    }

    public boolean remove() {
        valid = false;
        data = null;
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }
        if (next == null || previous == null)
            return true; // first or last
        assert previous.next == next
              && next.previous == previous;
        return false;
    }

    // returns true if last
    public boolean insertEntryAfter(SetEntry newEntry) {
        newEntry.previous = this;
        newEntry.next = this.next;
        this.next = newEntry;
        if (newEntry.next != null)
            newEntry.next.previous = newEntry;

        assert newEntry.previous == this && this.next == newEntry;
        return newEntry.next == null;
    }

    public boolean insertEntryBefore(SetEntry newEntry) {
        if (this.previous != null)
            this.previous.next = newEntry;
        newEntry.next = this;
        newEntry.previous = this.previous;
        this.previous = newEntry;
        assert newEntry.next == this && this.previous == newEntry;
        return newEntry.previous == null;
    }

}
 
/*
 * EOF
 */
