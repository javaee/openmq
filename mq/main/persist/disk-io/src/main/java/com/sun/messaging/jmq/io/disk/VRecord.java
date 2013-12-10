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
 * @(#)VRecord.java	1.12 06/27/07
 */ 

package com.sun.messaging.jmq.io.disk;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * A VRecord encapsulates a variable sized record backed by a file.
 */
public abstract class VRecord {

    // Info from record header.
    protected int magic = VRFile.RECORD_MAGIC_NUMBER;	// Record's magic number
    protected short state;     // Record's state
    protected int capacity;    // Record buffer's size including header
    protected short cookie = VRFile.RESERVED_SHORT; // application cookie

    /*
     * Force any modifications made to the buffer to be written
     * to physical storage.
     */
    public abstract void force() throws IOException;
    public abstract void setCookie(short cookie) throws IOException;
    public abstract short getCookie();

    /**
     * Return the capacity of this VRecord.
     */
    public int getCapacity() {
	return capacity;
    }

    /**
     * Return the capacity for data (ignoring header) of this VRecord.
     */
    public int getDataCapacity() {
	return capacity - VRFile.RECORD_HEADER_SIZE;
    }

    abstract void free();

    abstract void allocate(short state);

    short getState() {
	return state;
    }

}


