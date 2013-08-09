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
 * @(#)AutoRollbackType.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data;

import java.io.Serializable;
import java.io.ObjectStreamException;

public class AutoRollbackType implements Serializable
{

    static final long serialVersionUID = -6704477057825567951L;

    /**
     * descriptive string associated with the type
     */
    private final String name;


    /**
     * int value for the state used when reading/writing
     * the client->broker protocol.
     */
    private final int value;


    /**
     * value for ROLLBACK_ALL used with the protocol.
     */
    private static final int I_ROLLBACK_ALL=1;

    /**
     * value for ROLLBACK_NOT_PREPARED used with the protocol.
     */
    private static final int I_ROLLBACK_NOT_PREPARED=2;

    /**
     * value for ROLLBACK_NEVER used with the protocol.
     */
    private static final int I_ROLLBACK_NEVER=3;


    /**
     * mapping of type (int) values to AutoRollbackType
     */
    private static AutoRollbackType[] bs =new AutoRollbackType[3];


    /**
     * private constructor for AutoRollbackType
     */
    private AutoRollbackType(String name, int value) {
        this.name = name;
        this.value = value;
        bs[value-1]=this;
    }

    /**
     * method which takes an int (retrieved from the
     * persistent store) and converts it to a state
     */
    public static final AutoRollbackType getType(int value) 
    {
        return bs[value-1];
    }

    /**
     * method which returns the int value associated
     * with the state. This method should only be used when the
     * state written to or read from the protocol.
     */
    public int intValue()
    {
        return value;
    }

    /**
     * a string representation of the object
     */
    public String toString() {
        return "AutoRollbackType["+name+"]";
    }

    /**
     * Rollback a transaction of this type when the
     * broker is restarted.
     */
    public static final AutoRollbackType ALL = 
             new AutoRollbackType("ALL",
                      I_ROLLBACK_ALL);


    /**
     * Rollback a transaction of this type if
     * it is not in PREPARED when the broker is restarted.
     */
    public static final AutoRollbackType NOT_PREPARED = 
             new AutoRollbackType("NOT_PREPARED",
                      I_ROLLBACK_NOT_PREPARED);

    /**
     * Never rollback a transaction of this type when 
     * the broker is restarted. The transaction must be
     * COMMITTED, ROLLEDBACK or it must timeout.
     */
    public static final AutoRollbackType NEVER = 
             new AutoRollbackType("NEVER",
                      I_ROLLBACK_NEVER);


    Object readResolve() throws ObjectStreamException
    {
        return getType(value);
    }

}

