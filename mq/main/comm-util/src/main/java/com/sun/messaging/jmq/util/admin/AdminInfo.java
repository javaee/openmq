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
 * @(#)AdminInfo.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.util.admin;

/**
 * Abstract base class for all the admin *Info classes. Basically
 * just provides the implementation for maintaining updateMask.
 */
public class AdminInfo implements java.io.Serializable {

    static final long serialVersionUID = 6731577042303829252L;

    /**
     * A bit mask that subclasses use to indicate if a particular field
     * in the object has been updated or not.
     */
    private int updateMask = 0;

    /*
     * Constructor
     */
    public AdminInfo() {
	reset();
    }

    /*
     * Reset all fields to null values
     */
    public void reset() {
        resetMask();
    }

    /**
     * Clear updateMask so object thinks no fields have been modified
     */
    public void resetMask() {
        updateMask = 0;
    }

    /**
     * Check if a field has been modified
     */
    public boolean isModified(int fieldBit) {
        return ((updateMask & fieldBit) == fieldBit);
    }

    /**
     * Indicate that a field has been modified
     */
    public void setModified(int fieldBit) {
        updateMask = updateMask | fieldBit;
    }

    /**
     * Indicate that a field has NOT been modified
     */
    public void clearModified(int fieldBit) {
        updateMask = updateMask & ~fieldBit;
    }
}
