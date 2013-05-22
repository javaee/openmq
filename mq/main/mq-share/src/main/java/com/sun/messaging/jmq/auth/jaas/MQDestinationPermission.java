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
 * @(#)MQDestinationPermission.java	1.5 06/27/07
 */ 
 
package com.sun.messaging.jmq.auth.jaas;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;
import java.util.StringTokenizer;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.security.Permission;
import java.security.PermissionCollection;

/**
 *
 * MQ destination permission class
 */

public class MQDestinationPermission extends Permission 
                         implements java.io.Serializable
{ 

	private static final long serialVersionUID = -2435224016059811024L;

	private final static int PRODUCE    = 0x1;
	private final static int CONSUME    = 0x2;
	private final static int BROWSE     = 0x4;
	private final static int NONE       = 0x0;
	private final static int ALL        = PRODUCE|CONSUME|BROWSE;

    private transient boolean wildcard = false;
    private transient boolean isQueue = false;
    private transient String destName = null; 
    private transient int mask = NONE;

	private String actions = null;

    /**
     *
     */
    public MQDestinationPermission(String name, String actions) {
        super(name);
        init(name, actions);
    }

    /**
     *
     */
    private void init(String name, String actions) {
        if (name == null) throw new NullPointerException("name null");

        if (name.trim().startsWith(PermissionFactory.DEST_QUEUE_PREFIX)) {
            isQueue = true;
            destName = name.trim().substring(
                            PermissionFactory.DEST_QUEUE_PREFIX.length()).trim();
        } else if (name.trim().startsWith(PermissionFactory.DEST_TOPIC_PREFIX)) {
            isQueue = false;
            destName = name.trim().substring(
                            PermissionFactory.DEST_TOPIC_PREFIX.length()).trim();
        } else {
            throw new IllegalArgumentException("invalid name " + name);
        }

        int len = destName.length();
        if (len == 1 && destName.equals("*")) wildcard = true;

        if (actions == null) throw new NullPointerException("actions null");

        mask = computeMask(actions); 
        
    }

    public int getMask() {
        return mask;
    }
   
    private int computeMask(String actions) throws IllegalArgumentException {

	    int mask = NONE;

        StringTokenizer token = new StringTokenizer(actions, ",", false);
        String t = null; 
        while (token.hasMoreElements()) {
            t = token.nextToken().trim().toLowerCase();

            int i = 0;
            while (i < t.length()) {
                if (!Character.isSpaceChar(t.charAt(i++))) break;
            }
            if (i > 0) t = t.substring(i-1);

            i = t.length() -1;
            while (i != -1) { 
                if (!Character.isSpaceChar(t.charAt(i))) break;
                i--;
            }
            if (i < t.length()-1) t = t.substring(0, i+1);

            if (t.equals(PermissionFactory.ACTION_PRODUCE)) {
                mask |= PRODUCE;
            } else if (t.equals(PermissionFactory.ACTION_CONSUME)) {
                mask |= CONSUME;
            } else if (t.equals(PermissionFactory.ACTION_BROWSE)) {
                mask |= BROWSE; 
            }
        }
        if (mask == NONE) {
            throw new IllegalArgumentException("invalid actions "+actions);
        }

        return mask;
    }

    /**
     * 
     */
    public boolean implies(Permission p) {
	    if (!(p instanceof MQDestinationPermission)) return false;

        MQDestinationPermission that = (MQDestinationPermission)p;

        if (this.isQueue != that.isQueue) return false;

        return ((this.mask & that.mask) == that.mask) && impliesDestName(that); 
         
    }

    private boolean impliesDestName(MQDestinationPermission that) {        

        if (this.wildcard) return true;
        if (that.wildcard) return false;

	    return this.destName.equals(that.destName);
    }

    /**
     *
     */
    public boolean equals(Object obj) {
	    if (obj == this) return true;

        if (!(obj instanceof MQDestinationPermission)) return false;

        if (obj.getClass() != getClass()) return false;

        MQDestinationPermission that = (MQDestinationPermission)obj;

        return ((this.mask == that.mask) &&
                (this.isQueue == that.isQueue) &&
                (this.destName.equals(that.destName)));
    }

    /**
     *
     */
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     *
     */
    public String getActions() {
        if (actions != null) return actions;

        StringBuffer s = new StringBuffer();
        boolean comma = false;

        if ((mask & PRODUCE) == PRODUCE) {
            comma = true;
            s.append(PermissionFactory.ACTION_PRODUCE);
        }
        if ((mask & CONSUME) == CONSUME) { 
            if (comma) s.append(',');
            comma = true;
            s.append(PermissionFactory.ACTION_CONSUME);
        }
        if ((mask & BROWSE) == BROWSE) { 
            if (comma) s.append(',');
            comma = true;
            s.append(PermissionFactory.ACTION_BROWSE);
        }

        return s.toString();
    }


    /**
     *
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        if (actions == null) getActions();
        s.defaultWriteObject();
    }

    /**
     *
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        init(getName(), actions);
    }
}

