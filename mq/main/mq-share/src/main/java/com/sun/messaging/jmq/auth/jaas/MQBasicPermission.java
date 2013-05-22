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
 * @(#)MQBasicPermission.java	1.5 06/27/07
 */ 

package com.sun.messaging.jmq.auth.jaas;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.security.Permission;
import java.security.PermissionCollection;

/**
 * A base permission class for MQ connection and destination
 * auto-creation permissionS
 */

public abstract class MQBasicPermission extends Permission 
                           implements java.io.Serializable
{ 

    private static final long serialVersionUID = 7965671047666454007L;

    private transient boolean wildcard;

    /**
     *
     */
    public MQBasicPermission(String name) {
        super(name);
        init(name);
    }

    /**
     *
     */
    private void init(String name) {
        if (name == null) throw new NullPointerException("name null");

        int len = name.length();
        if (len == 0) throw new IllegalArgumentException("name empty");

        if (len == 1 && name.equals("*")) {
            wildcard = true;
        } else {
            validateName(name);
        }
    }

    public abstract void validateName(String name) throws IllegalArgumentException; 

    /**
     * 
     */
    public boolean implies(Permission p) {
	    if (!(p instanceof MQBasicPermission)) return false;
        if (p.getClass() != getClass()) return false;

        MQBasicPermission that = (MQBasicPermission)p;

        if (this.wildcard) return true;
        if (that.wildcard) return false;

	    return this.getName().equals(that.getName());
    }

    /**
     *
     */
    public boolean equals(Object obj) {
	    if (obj == this) return true;

        if (!(obj instanceof MQBasicPermission)) return false;

        if (obj.getClass() != getClass()) return false;

        MQBasicPermission p = (MQBasicPermission)obj;

        return getName().equals(p.getName());
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
        return "";
    }

    /**
     *
     */
    public PermissionCollection newPermissionCollection() {
	    return new MQBasicPermissionCollection();
    }

    /**
     *
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        init(getName());
    }

}


/**
 *
 */
final class MQBasicPermissionCollection extends PermissionCollection
                                     implements java.io.Serializable
{

    private static final long serialVersionUID = 8958682081876542895L;

    private Map perms = null;
    private Class permClass = null;

    private boolean allowAll = false; 

    /**
     *
     */
    public MQBasicPermissionCollection() {
        perms = new HashMap(11);
        allowAll = false;
    }

    /**
     *
     */
    public void add(Permission p) {

        if (! (p instanceof MQBasicPermission)) {
            throw new IllegalArgumentException("invalid permission: "+p);
        }

        if (isReadOnly()) {
	        throw new SecurityException("Attempt to add to a read only permission "+p);
        }

        MQBasicPermission pm = (MQBasicPermission)p;

        synchronized (this) {
            if (perms.size() == 0) {
                permClass = pm.getClass();
            } else if (pm.getClass() != permClass) {
                throw new IllegalArgumentException("invalid permission: " +p);
            }
            perms.put(pm.getName(), pm);
        }

        if (!allowAll) {
            if (pm.getName().equals("*")) allowAll = true;
        }
    }

    /**
     *
     */
    public boolean implies(Permission p) {
        if (! (p instanceof MQBasicPermission)) return false;

        MQBasicPermission pm = (MQBasicPermission)p;

        if (pm.getClass() != permClass) return false;

        if (allowAll) return true;


        Permission x;
        synchronized (this) {
            x = (Permission) perms.get(p.getName());
        }
        if (x != null) return x.implies(p);

        return false;
	}

    /**
     *
     */
    public Enumeration elements() {
        synchronized (this) {
	        return Collections.enumeration(perms.values());
        }
    }

}
