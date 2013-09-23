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
 * @(#)VRecordMap.java	1.6 06/27/07
 */ 

package com.sun.messaging.jmq.io.disk;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * A VRecordMap encapsulates a slice of mapped buffer allocated by VRFileMap.
 */
public class VRecordMap extends VRecord {

    private static boolean DEBUG = Boolean.getBoolean("vrfile.debug");


    private VRFileMap vrfile;
    private ByteBuffer bbuf;
    private ByteBuffer databuf; // slice after the header

    private MappedByteBuffer parent;

    // instantiate with an existing record (sanity checked by caller)
    VRecordMap(VRFileMap v, MappedByteBuffer p, ByteBuffer buf) {
	vrfile = v;
	parent = p;
	bbuf = buf;

	// read header
	magic = bbuf.getInt();
	capacity = bbuf.getInt();
	state = bbuf.getShort();
	cookie = bbuf.getShort();

	bbuf.limit(capacity);
	bbuf.position(VRFile.RECORD_HEADER_SIZE);
	databuf = bbuf.slice();
    }

    // instantiate with an uninitialized record
    VRecordMap(VRFileMap v, MappedByteBuffer p, ByteBuffer buf, int size) {
	vrfile = v;
	parent = p;
	bbuf = buf;

	capacity = size;
	state = VRFile.STATE_ALLOCATED;

	// write header
	bbuf.putInt(magic);
	bbuf.putInt(capacity);
	bbuf.putShort(state);
	bbuf.putShort(cookie);

	bbuf.limit(capacity);
	bbuf.position(VRFile.RECORD_HEADER_SIZE);
	databuf = bbuf.slice();
    }

    /**
     * Get the record buffer. Its 'capacity' may be larger than what
     * was requested. Its 'limit' will match what was requested.
     * Whatever is written to the buffer may be written to the backing
     * file, but is not guaranteed to be until force() is called
     * or the VRfile is closed.
     */
    public ByteBuffer getBuffer() {
	return databuf;
    }

    /*
     * Force any modifications made to the buffer to be written
     * to physical storage.
     */
    public void force() throws IOException {
	if (DEBUG) {
	    System.out.println("will do force on "+parent);
	}

	parent.force();
    }

    public void setCookie(short c) throws IOException {
	this.cookie = c;
	bbuf.putShort(VRFile.RECORD_COOKIE_OFFSET, cookie);

	if (vrfile.getSafe()) {
	    force();
	}
    }

    public short getCookie() {
	return cookie;
    }

    public String toString() {
	return "VRecordMap: "+bbuf.toString();
    }

    MappedByteBuffer getParent() {
	return parent;
    }

    void free() {
	state = VRFile.STATE_FREE;
	bbuf.putShort(VRFile.RECORD_STATE_OFFSET, state);
	bbuf.putShort(VRFile.RECORD_COOKIE_OFFSET, VRFile.RESERVED_SHORT);
	databuf.rewind();
    }

    void allocate(short s) {
	state = s;
	bbuf.putShort(VRFile.RECORD_STATE_OFFSET, state);
    }
}


