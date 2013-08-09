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
 * @(#)VRecordRAF.java	1.8 06/27/07
 */ 

package com.sun.messaging.jmq.io.disk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A VRecordRAF encapsulates a section of a file.
 */
public class VRecordRAF extends VRecord {

   private static boolean DEBUG = Boolean.getBoolean("vrfile.debug");

    private VRFileRAF parent;

    private long recordStartAt;	// position of the record in the file

    private int index;	// the current offset in the backing file

    // instantiate with an existing record (sanity checked by caller)
    VRecordRAF(VRFileRAF p, long from, int c, short state) throws IOException {
	this(p, from, c, state, false);

	// read cookie from record
	cookie = p.readShort(recordStartAt + VRFile.RECORD_COOKIE_OFFSET);
    }

    // instantiate with an uninitialized record
    VRecordRAF(VRFileRAF p, long from, int c, short s, boolean dowrite)
	throws IOException {

	if (DEBUG) {
	    System.out.println("VRecordRAF:new record: from "+from +" for "+c);
	}

	parent = p;
	recordStartAt = from;

	capacity = c;
	this.state = s;

	if (dowrite) {
	    ByteBuffer bbuf = ByteBuffer.allocate(VRFile.RECORD_HEADER_SIZE);

	    // write header
	    bbuf.putInt(magic);
	    bbuf.putInt(capacity);
	    bbuf.putShort(state);
	    bbuf.putShort(VRFile.RESERVED_SHORT);
	    bbuf.rewind();

	    p.writeByteBuffer(recordStartAt, bbuf);
	}

	index = VRFile.RECORD_HEADER_SIZE;

	if (DEBUG) {
	    System.out.println("index start at "+index);
	}
    }
    
    
 // instantiate with an initialized record and write data + last record header
	VRecordRAF(VRFileRAF p, long from, int c, short s, byte[] data, byte[] lastRecord)
			throws IOException {

		if (DEBUG) {
			System.out.println("VRecordRAF:new record: from " + from + " for "
					+ c + " data.length="+data.length + " lastRecord.length="+lastRecord.length);
		}

		parent = p;
		recordStartAt = from;

		capacity = c;
		this.state = s;

		ByteBuffer bbuf = ByteBuffer.allocate(c + VRFile.RECORD_HEADER_SIZE);

		// write header
		bbuf.putInt(magic);
		bbuf.putInt(capacity);
		
//		write the state and cookie plus the real data 
		bbuf.put(data);
		bbuf.position(c);
		
//      write the last record data		
		bbuf.put(lastRecord);
		bbuf.rewind();

		p.writeByteBuffer(recordStartAt, bbuf);

		index = VRFile.RECORD_HEADER_SIZE;
		
			
		if (DEBUG) {
			System.out.println("index start at " + index);
		}
	}

    public void setCookie(short c) throws IOException {
	this.cookie = c;
	try {
	    parent.writeShort(
		(recordStartAt + VRFile.RECORD_COOKIE_OFFSET), cookie);

	    if (parent.getSafe()) {
		force();
	    }
	} catch (IOException e) {
	    throw new RuntimeException(toString() + ":setCookie()", e);
	}
    }

    public short getCookie() {
	return cookie;
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

    public String toString() {
	return ("VRecordRAF: start="+recordStartAt+
			"; cap="+capacity+";state="+state+";index="+index);
    }

    // reset any state or data
    void free() {
	index = VRFile.RECORD_HEADER_SIZE;
	state = VRFile.STATE_FREE;

	try {
		if(!parent.isMinimumWrites())
		{
	    parent.writeShort((recordStartAt + VRFile.RECORD_STATE_OFFSET),
				state);
	    parent.writeShort((recordStartAt + VRFile.RECORD_COOKIE_OFFSET),
				VRFile.RESERVED_SHORT);
		}else{
			parent.writeData(recordStartAt + VRFile.RECORD_STATE_OFFSET, freeRecordData);
		}
		
	} catch (IOException e) {
	    throw new RuntimeException(toString() + ":free()", e);
	}
    }

    void allocate(short s) {
	state = s;

	try {
	    parent.writeShort((recordStartAt + VRFile.RECORD_STATE_OFFSET),
				state);
	} catch (IOException e) {
	    throw new RuntimeException(toString() + ":allocate()", e);
	}
    }
    
    void allocateAndWrite(byte[] data) {
    	
    	// here the data array also includes 
    	//  short : the record state
    	//  short : reserved
    	
    	// so start writing from RECORD_STATE_OFFSET

    	try {
    	    parent.writeData((recordStartAt + VRFile.RECORD_STATE_OFFSET),
    				data);
    	} catch (IOException e) {
    	    throw new RuntimeException(toString() + ":allocate()", e);
    	}
    }

    public void writeInt(int i, int v) throws IOException {
	if (i > (getDataCapacity() - VRFile.INT_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write integer at position " + i);
	}
	parent.writeInt((recordStartAt + VRFile.RECORD_HEADER_SIZE + i), v);
    }

    public void writeInt(int v) throws IOException {
	if (index > (capacity - VRFile.INT_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write integer at position " + index);
	}
	parent.writeInt(index + recordStartAt, v);
	index += VRFile.INT_LEN;
	if (DEBUG) {
	    System.out.println("index = "+index+" after writeInt");
	}
    }

    public int readInt() throws IOException {
	if (index > (capacity - VRFile.INT_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to read an integer from position " + index);
	}
	int v = parent.readInt(index + recordStartAt);
	index += VRFile.INT_LEN;
	if (DEBUG) {
	    System.out.println("index = "+index+" after readInt");
	}
	return v;
    }

    public long readLong() throws IOException {
	if (index > (capacity - VRFile.LONG_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to read a long from position " + index);
	}
	long v = parent.readLong(index + recordStartAt);
	index += VRFile.LONG_LEN;
	if (DEBUG) {
	    System.out.println("index = "+index+" after readLong");
	}
	return v;
    }

    public int write(ByteBuffer buf) throws IOException {
	if (index > (capacity - buf.remaining())) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write " + buf.remaining() +
			" bytes at position " + index);
	}
	int len = parent.writeByteBuffer(index + recordStartAt, buf);
	index += len;
	if (DEBUG) {
	    System.out.println("index = "+index+" after write ByteBuffer");
	}
	return len;
    }

    public void write(byte[] buf) throws IOException {
	write(buf, 0, buf.length);
    }

    public void write(byte[] buf, int offset, int len) throws IOException {
	if (index > (capacity - len)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write " + len + " bytes at position " +
			index);
	}
	parent.write(index + recordStartAt, buf, offset, len);
	index += len;
	if (DEBUG) {
	    System.out.println("index = "+index+" after write byte[]");
	}
    }

    public int read(byte[] buf) throws IOException {
	return read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int offset, int len) throws IOException {
	if (index > (capacity - len)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to read " + len + "bytes from position " +
			index);
	}
	int result = parent.read(index + recordStartAt, buf, offset, len);
	index += result;
	if (DEBUG) {
	    System.out.println("index = "+index+" after read byte[]");
	}
	return result;
    }

    public FileChannel getChannel() {
	return parent.getChannel();
    }

    public void rewind() {
	index = VRFile.RECORD_HEADER_SIZE;
	if (DEBUG) {
	    System.out.println("rewind index to "+index);
	}
    }

    public void position(int pos) {
	if (pos > (getDataCapacity())) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to position at position " + pos);
	}
	index = VRFile.RECORD_HEADER_SIZE + pos;
    }

    public int remaining() {
	return (capacity - index);
    }

    public void writeLong(long v) throws IOException {
	if (index > (capacity - VRFile.LONG_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write a long at position " + index);
	}
	parent.writeLong(index + recordStartAt, v);
	index += VRFile.LONG_LEN;
	if (DEBUG) {
	    System.out.println("index = "+index+" after writeLong");
	}
    }

    public int read(ByteBuffer buf) throws IOException {
	if (index > (capacity - buf.remaining())) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to read " + buf.remaining() +
			" bytes at position " + index);
	}
	int len = parent.readByteBuffer(index + recordStartAt, buf);
	index += len;
	if (DEBUG) {
	    System.out.println("index = "+index+" after read ByteBuffer");
	}
	return len;
    }

    public void writeShort(int i, short v) throws IOException {
	if (i > (getDataCapacity() - VRFile.SHORT_LEN)) {
	    throw new IndexOutOfBoundsException(toString() +
			": try to write a short at position " + i);
	}
	parent.writeShort((recordStartAt + VRFile.RECORD_HEADER_SIZE+ i), v);
	if (DEBUG) {
	    System.out.println("this writeShort does not change index");
	}
    }
    /*
     * This is an optimisation so that we can write an array of 4 bytes to the file rather than 2 shorts.
     * Saves on writes so should speed up a bit.
     */
    static byte[] freeRecordData= new byte[4];
    
    static void initFreeRecordData()
    {
    	ByteBuffer b = ByteBuffer.wrap(freeRecordData);
    	b.putShort(VRFile.STATE_FREE);
    	b.putShort(VRFile.RESERVED_SHORT);
    	b.rewind();
    }
    static{
    	initFreeRecordData();
    }
}


