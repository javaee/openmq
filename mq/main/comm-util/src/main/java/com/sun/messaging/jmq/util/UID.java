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
 * @(#)UID.java	1.10 07/06/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;
import java.util.Random;

/**
 * An encapsulation of a JMQ Unique ID. A Unique ID is an ID
 * with the following characteristics.
 *
 *  1. Is unique in this VM
 *  2. Will stay unique for a very long time (over 34 years)
 *  3. Will stay unique across VM restarts (due to #2)
 *  4. Can be made more unique by the caller providing a 16 bit prefix
 *
 * For more information see UniqueID.java
 *
 */
public class UID implements Serializable {

    private static final long serialVersionUID = -583620884703541778L;

    // Default prefix to something random
    private static short prefix = (short)((new Random()).nextInt(Short.MAX_VALUE));

    protected long id = 0;

    transient int hashCode = 0;    
    transient String unique_id = null;

    /**
     * Constructs a new UID. You may want to call
     * setPrefix() to specify a prefix before creating any UID, 
     * otherwise a random prefix will be used.
     */
    public UID() {
        this.id = UniqueID.generateID(prefix);
    }

    /**
     * Constructs a new UID using the argument as the identifier.
     * This constructor performs no checking as to the uniqueness of
     * <code>id</code>
     *
     * @param id The identifier to initialize this UID from.
     */
    public UID(long id) {
        this.id = id;
    }

    /**
     * Set the prefix to us for all UID generated. UIDs are guaranteed
     * to be unique in this VM no matter what the prefix. The prefix can
     * be used for additional uniqueness. By default a random prefix is
     * be used.
     *
     * @param p prefix to use
     */
    public static void setPrefix(short p) {
        prefix = p;
    }

    /**
     * Get the prefix being used to generate Ids.
     */
    public static short getPrefix() {
        return prefix;
    }

    /**
     * Return the identifier as a long
     */
    public long longValue() {
        return id;
    }

    /**
     * Return the hash code for the indentifier
     */
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = UniqueID.hashCode(id);
        }
        return hashCode;
    }

    /**
     * Equals
     */
    public boolean equals(Object obj) {
        if (! (obj instanceof UID)) {
            return false;
        }
        return (this.id == ((UID)obj).id);
    }

    /**
     * Return the age in milliseconds of this identifier
     */
    public long age() {
        return UniqueID.age(id);
    }

    /**
     * Return the age in milliseconds of this identifier
     *
     * @param currentTime Current time in milliseconds
     */
    public long age(long currentTime) {
        return UniqueID.age(id, currentTime);
    }

    /**
     * Return the timestamp in milliseconds of this identifier
     */
    public long getTimestamp() {
        return UniqueID.getTimestamp(id);
    }

    /**
     * Return a short string representation of this identifier
     */
    public String toString() {
        return String.valueOf(id);
    }

    /**
     * Return a short string that can be used as a key. This is
     * for backwards compatibility with earlier style identifiers.
     */
    public String getUniqueName() {
        if (unique_id == null) {
            unique_id = this.toString();
        } 
        return unique_id;
    }

    /**
     * Return a long string representation of this identifier
     */
    public String toLongString() {
        return UniqueID.toLongString(id);
    }

    /**
     * Marshals a binary representation of this UID to a 
     * Data OutPut stream. The binary representation is just a long.
     */
    public void writeUID(DataOutputStream out)
        throws IOException {
        out.writeLong(this.id);
    }

    /** 
     * Constructs and returns a new UID instance by unmarshalling
     * a binary representation from an input stream.
     */
    public static UID readUID(DataInputStream in) throws IOException {
        long n = in.readLong();
        return new UID(n);
    }

}
