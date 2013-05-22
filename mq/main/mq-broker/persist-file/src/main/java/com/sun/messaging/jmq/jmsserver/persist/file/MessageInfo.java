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
 * @(#)MessageInfo.java	1.22 08/31/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.file;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.messaging.jmq.io.JMQByteBufferInputStream;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.io.disk.VRFile;
import com.sun.messaging.jmq.io.disk.VRFileRAF;
import com.sun.messaging.jmq.io.disk.VRecordRAF;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.Logger;

/**
 * MessageInfo keeps track of a message and it's ack list.
 * Has methods to parse and persist them.
 */
class MessageInfo {
    static final short	PENDING = -1; // -1=0xffff
    static final short	DONE = 0;
    private static final int	INT_SIZE = 4;
    private static final int	LONG_SIZE = 8;

    // each entry for an interest (identified by ConsumerUID) and it's state
    // has: a long (ConsumerUID) and an int (state)
    private static final int	ENTRY_SIZE = LONG_SIZE + INT_SIZE;

    private Logger logger = Globals.getLogger();
    private BrokerResources br = Globals.getBrokerResources();

    private Packet msg; // will set to null after client has it
    private WeakReference msgBytes = null; // cached of packet as an array of bytes

    // cached info
    private SysMessageID mid;
    private int packetSize;

    private DstMsgStore parent = null;

    // backing buffer
    private VRecordRAF vrecord = null;

    // interest list info
    // iid -> position of state in statearray
    private HashMap iidMap = null;

    // states
    private int[] statearray = null;

    /**
     * if this returns successfully,
     * the message and it's interest list are loaded from backing file
     * message is from an individual file
     */
    MessageInfo(DstMsgStore p, byte[] data, byte[] ilist)
	throws IOException {

	parent = p;

	try {
	    // parse message
	    Packet pkt = parseMessage(data);

	    // cache message info
	    msg = pkt;
	    mid = (SysMessageID)pkt.getSysMessageID().clone();

	    // parse interest list
	    parseInterestList(ilist);
	} catch (IOException e) {
	    logger.log(logger.ERROR, parent.myDestination +
			":failed to parse message from byte array", e);
	    throw e;
	}
    }

    /**
     * if this returns successfully,
     * the message and it's interest list are loaded from backing file
     * message is from vrfile
     */
    MessageInfo(DstMsgStore p, VRecordRAF r) throws IOException {
	parent = p;
	vrecord = r;

	// sanity check done while VRecords are loaded

	try {
	    // parse message
	    Packet pkt = parseMessage(r);

	    // cache message info
	    msg = pkt;
	    mid = (SysMessageID)pkt.getSysMessageID().clone();

	    // parse interest list
	    parseInterestList(r);

	} catch (IOException e) {
	    // free the bad VRecord
	    parent.getVRFile().free(vrecord);

	    throw e;
	}
    }

    /**
     * if this returns successfully, message and it's interest states
     * are persisted
     * store message in vrfile
     */
    MessageInfo(DstMsgStore p, VRFileRAF vrfile, Packet message,
	ConsumerUID[]iids, int[] states, boolean sync) throws IOException {

	parent = p;

	// cache message info
	mid = (SysMessageID)message.getSysMessageID().clone();
	packetSize = message.getPacketSize();

	// format of data in buffer:
	// size of message (packetSize, int)
	// message (a blob)
	// size of interest list (int)
	// each interest entry has:
	//   long
	//   int

	int bufsize = INT_SIZE + packetSize +
			INT_SIZE + (iids.length * ENTRY_SIZE);

	if(sync && Store.getDEBUG_SYNC())
	{
		Globals.getLogger().log(Logger.DEBUG, "sync new MessageInfo "+mid+"with VRFileRAF");
	}
	if(!Globals.isMinimumWritesFileStore())
	{	
        synchronized (vrfile) {
            vrecord = (VRecordRAF)vrfile.allocate(bufsize);

            // start writing
            vrecord.setCookie(PENDING);

            // write packetSize
            vrecord.writeInt(packetSize);

            // write message
            if (parent.useFileChannel) {
                message.writePacket(vrecord.getChannel(), false);
            } else {
                byte[] databuf = message.getBytes();
                vrecord.write(databuf);
                if (Globals.txnLogEnabled()) {
                    // Cache the packet byte array to improve performance
                    // because we'll need to log it soon
                    msgBytes = new WeakReference(databuf);
                }
            }

            // cache and write interest list
            storeStates(vrecord, iids, states, false);

            // done writing
            vrecord.setCookie(DONE);

            if (sync) {
                vrecord.force();
            }
        }
	}
	else
	{
		synchronized (vrfile) {
			//	byte[] recordData = new byte[] 
				 // we will write the state and cooky at the same time as data
				  // so need another 4 bytes (2 * short);
				 ByteBuffer bbuf = ByteBuffer.allocate(bufsize+4);
				 
				 bbuf.putShort(VRFile.STATE_ALLOCATED);
				 bbuf.putShort(DONE);
				 bbuf.putInt(packetSize);
        
			
				// write message
				
				byte[] databuf = message.getBytes();
				bbuf.put(databuf);
				if (Globals.txnLogEnabled()) {
						// Cache the packet byte array to improve performance
						// because we'll need to log it soon
						msgBytes = new WeakReference(databuf);
				}
				

				// cache and write interest list
				
	        	ByteBuffer buf = serializeStates(iids, states);
	        	buf.rewind();
	        	bbuf.put(buf);
	        	
	            byte[] data = bbuf.array();
	        	
				vrecord = (VRecordRAF) vrfile.allocateAndWrite(bufsize,data);

				if (sync) {
					vrecord.force();
				}
			}
	}
    }

    /**
     * if this returns successfully, message and it's interest states
     * are persisted; store message in individual files
     */
    MessageInfo(DstMsgStore p, Packet msg, ConsumerUID[] iids,
	int[] states, boolean sync) throws IOException {

	this.parent = p;

	mid = (SysMessageID)msg.getSysMessageID().clone();
	packetSize = msg.getPacketSize();

	ByteBuffer bbuf = serializeStates(iids, states);
	bbuf.rewind();

	
	if(sync && Store.getDEBUG_SYNC())
	{
		String logmsg = "sync new MessageInfo msg id "+mid + " with individual files";
		Globals.getLogger().log(Logger.DEBUG, logmsg);
	}
	
	if (parent.useFileChannel) {
	    RandomAccessFile raf = parent.getRAF(mid);

	    parent.markWriting(raf);

	    raf.writeLong(packetSize);
	    msg.writePacket(raf.getChannel(), false);

	    long endofdata = raf.getFilePointer();

	    if (bbuf != null) {
		raf.writeLong(bbuf.remaining()); // length of attachment
		raf.getChannel().write(bbuf);
	    } else {
		raf.writeLong(0);
	    }
	    long endoffile = raf.getFilePointer();

	    parent.markGood(raf);

	    if (sync) {
		// bug 5042763:
		// use FileChannel.force(false) to improve file sync performance
		raf.getChannel().force(false);
	    }
	    parent.releaseRAF(mid, raf, endofdata, endoffile);
	} else {
            byte[] attachment = (bbuf != null) ? bbuf.array() : null;
            byte[] databuf = msg.getBytes();
            if (Globals.txnLogEnabled()) {
                // Cache the packet byte array to improve performance
                // because we'll need to log it soon
                msgBytes = new WeakReference(databuf);
            }
            parent.writeData(mid, databuf, attachment, sync);
	}
    }

    /**
     * Return the message.
     * The message is only cached when it is loaded from the backing buffer
     * the first time.
     * It will be set to null after it is retrieved.
     * From then on, we always read it from the backing buffer again.
     */
    synchronized Packet getMessage() throws IOException {
	if (msg == null) {
	    if (vrecord != null) {
		// read from backing buffer
		try {
		    return parseMessage(vrecord);
		} catch (IOException e) {
		    logger.log(logger.ERROR, parent.myDestination +
			":failed to parse message from vrecord("+
			vrecord + ")", e);
		    throw e;
		}
	    } else {
		try {
		    byte data[] = parent.loadData(mid);
		    return parseMessage(data);
		} catch (IOException e) {
		    logger.log(logger.ERROR, parent.myDestination +
			":failed to parse message from byte array", e);
		    throw e;
		}
	    }
	} else {
	    Packet pkt = msg;
	    msg = null;
	    return pkt;
	}
    }

    /**
     * Return the cached message bytes.
     * It will be set to null after it is retrieved.
     *
     * no need to synchronized, value set at object creation and wont change
     */
    byte[] getCachedMessageBytes() {
        byte[] data = null;
        if (msgBytes != null) {
            data = (byte[])msgBytes.get();
            msgBytes = null;
        }
        return data;
    }

    // no need to synchronized, value set at object creation and wont change
    int getSize() {
	return packetSize; 
    }

    // no need to synchronized, value set at object creation and wont change
    SysMessageID getID() {
	return mid;
    }

    /**
     * clear out all cached info
     * and release the backing buffer
     */
    synchronized void free(boolean sync) throws IOException {
    if (sync && Store.getDEBUG_SYNC()) {
			String msg = "sync free msg " + mid;
			Globals.getLogger().log(Logger.DEBUG, msg);
		}
	if (vrecord != null) {
	    parent.getVRFile().free(vrecord);
	    if (sync) {
		parent.getVRFile().force();
	    }
	    vrecord = null;
	} else {
	    parent.removeData(mid, sync);
	}

	mid = null;
	statearray = null;
	if (iidMap != null) {
	    iidMap.clear();
	    iidMap = null;
	}
    }

    synchronized void storeStates(ConsumerUID[] iids, int[] states,
	boolean sync) throws IOException, BrokerException {

	if (iidMap.size() != 0) {
	    // the message has a list already
	    logger.log(logger.WARNING, br.E_MSG_INTEREST_LIST_EXISTS,
				mid.toString());
	    throw new BrokerException(
                br.getString(br.E_MSG_INTEREST_LIST_EXISTS, mid.toString()));
	}

	if (vrecord != null) {
	    // calculate new size needed
	    int total = INT_SIZE + packetSize
			+ INT_SIZE + (iids.length * ENTRY_SIZE);

            VRFileRAF vrfile = parent.getVRFile();
            synchronized (vrfile) {
                if (vrecord.getDataCapacity() < total) {
                    vrecord.rewind();
                    byte data[] = new byte[INT_SIZE+packetSize];
                    vrecord.read(data);

                    // the existing one is not big enough; get another one
                    VRecordRAF newrecord = (VRecordRAF)vrfile.allocate(total);

                    // copy message
                    newrecord.write(data);

                    // free old
                    vrfile.free(vrecord);

                    // cache new
                    vrecord = newrecord;
                }

                // store states
                storeStates(vrecord, iids, states, sync);
            }
	} else {
	    byte[] data = serializeStates(iids, states).array();
	    if(Store.getDEBUG_SYNC())
		{
			String msg = "MessageInfo storeState writeAttachment with sync "+mid;
			logger.log(Logger.DEBUG,msg);
		}
	    if (!parent.writeAttachment(mid, data, sync)) {
		iidMap = null;
		statearray = null;

		logger.log(logger.ERROR, br.E_MSG_NOT_FOUND_IN_STORE,
                    mid, parent.myDestination);
		throw new BrokerException(
                    br.getString(br.E_MSG_NOT_FOUND_IN_STORE,
                        mid, parent.myDestination));
	    }
	}
    }

    synchronized void updateState(ConsumerUID iid, int state, boolean sync)
	throws IOException, BrokerException {

	Integer indexObj = null;
	if (iidMap == null || (indexObj = (Integer)iidMap.get(iid)) == null) {

	    logger.log(logger.ERROR, br.E_INTEREST_STATE_NOT_FOUND_IN_STORE,
			iid.toString(), mid.toString());
	    throw new BrokerException(
			br.getString(br.E_INTEREST_STATE_NOT_FOUND_IN_STORE,
			iid.toString(), mid.toString()));
	}

	int index = indexObj.intValue();
	if (statearray[index] != state) {
	    statearray[index] = state;

	    // if 
	    if(state == PartitionedStore.INTEREST_STATE_DELIVERED && 
               Globals.isDeliveryStateNotPersisted()) {
	    	return;
	    }
	    
	    if (vrecord != null) {

		// offset into VRecord
		// 4+packetSize+offset into interest list
		long offset = INT_SIZE + packetSize +
			INT_SIZE + index * ENTRY_SIZE + (ENTRY_SIZE-INT_SIZE);

		vrecord.writeInt((int)offset, state);
		if (sync) {
			if(Store.getDEBUG_SYNC())
        	{
				String msg = "MessageInfo updateState sync called for msg id "+mid + "consumer "+iid;
        		Globals.getLogger().log(Logger.DEBUG, msg);
        	}
		    vrecord.force();
		}
	    } else {
		// offset into attachment part
		long offset = INT_SIZE + index * ENTRY_SIZE
				+ (ENTRY_SIZE-INT_SIZE);

		if (!parent.writeAttachmentData(mid, offset, state, sync)) {
                    logger.log(logger.ERROR, br.E_MSG_NOT_FOUND_IN_STORE,
                        mid, parent.myDestination);
                    throw new BrokerException(
                        br.getString(br.E_MSG_NOT_FOUND_IN_STORE,
                            mid, parent.myDestination));
		}
	    }
	}
    }

    synchronized int getInterestState(ConsumerUID iid) throws BrokerException {

	Integer indexobj = null;
	if (iidMap == null || (indexobj = (Integer)iidMap.get(iid)) == null) {
	    logger.log(logger.ERROR, br.E_INTEREST_STATE_NOT_FOUND_IN_STORE,
			iid.toString(), mid.toString());
	    throw new BrokerException(
			br.getString(br.E_INTEREST_STATE_NOT_FOUND_IN_STORE,
			iid.toString(), mid.toString()));
	} else {
	    return statearray[indexobj.intValue()];
	}
    }

    synchronized HashMap getInterestStates() {

        HashMap states = new HashMap();
        if (iidMap != null) {
            Set entries = iidMap.entrySet();
            Iterator itor = entries.iterator();
            while (itor.hasNext()) {
                Map.Entry entry = (Map.Entry)itor.next();
                int index = ((Integer)entry.getValue()).intValue();
                states.put(entry.getKey(), Integer.valueOf(statearray[index]));
            }
        }

        return states;
    }

    /**
     * Return ConsumerUIDs whose associated state is not
     * INTEREST_STATE_ACKNOWLEDGED.
     */
    synchronized ConsumerUID[] getConsumerUIDs() {

	ConsumerUID[] ids = new ConsumerUID[0];
	if (iidMap != null) {
	    ArrayList list = new ArrayList();

	    Set entries = iidMap.entrySet();
	    Iterator itor = entries.iterator();
	    while (itor.hasNext()) {
		Map.Entry entry = (Map.Entry)itor.next();
		Integer index = (Integer)entry.getValue();

		if (statearray[index.intValue()] !=
		    PartitionedStore.INTEREST_STATE_ACKNOWLEDGED) {
			list.add(entry.getKey());
		}
	    }
	    ids = (ConsumerUID[])list.toArray(ids);
	}

	return ids;
    }

    /**
     * Check if a a message has been acknowledged by all interests.
     *
     * @return true if all interests have acknowledged the message;
     * false if message has not been routed or acknowledge by all interests
     */
    synchronized boolean hasMessageBeenAck() {

        // To be safe, message is considered unrouted if interest list is empty
        if (statearray != null && statearray.length > 0) {
            for (int i = 0, len = statearray.length; i < len; i++) {
                if (statearray[i] != PartitionedStore.INTEREST_STATE_ACKNOWLEDGED) {
                    return false; // Not all interests have acked
                }
            }

            return true; // Msg has been routed and acked
        }

        return false;
    }

    // load states from backing buffer
    // format:
    // number of entries (int)
    // fixed length entries (iid (long), state (int))
    private void parseInterestList(VRecordRAF r) throws IOException {

	int size = 0;
	try {
	    // position after the size of message and the message
	    r.position(INT_SIZE + packetSize);

	    // read in number of entries
	    size = r.readInt();

	    // sanity check
	    long endofrec = INT_SIZE + packetSize + INT_SIZE
				+ ((long)size * ENTRY_SIZE);
	    if (endofrec > r.getDataCapacity()) {
		throw new Exception("size of interest list is corrupted");
	    }

	    iidMap = new HashMap(size);
	    statearray = new int[size];

	    for (int i = 0; i < size; i++) {
		ConsumerUID iid = new ConsumerUID(r.readLong()); 
		statearray[i] = r.readInt();

		// put in interest id map
		iidMap.put(iid, new Integer(i));
	    }

	    if (Store.getDEBUG()) {
		logger.log(logger.DEBUG, "loaded " + size + " interest states");
	    }
	} catch (Throwable t) {
	    logger.log(logger.ERROR,
			"failed to parse interest list(size=" + size +
			") for msg(size=" + packetSize + ") from vrecord(" +
			r + ")", t);
	    IOException e = new IOException(t.toString());
	    e.setStackTrace(t.getStackTrace());
	    throw e;
	}
    }

    // load states from byte array
    // format:
    // number of entries (int)
    // fixed length entries (iid, state)
    private void parseInterestList(byte[] buf) throws IOException {

	if (buf == null || buf.length == 0) {
	    if (Store.getDEBUG()) {
		logger.log(logger.DEBUGHIGH, "No interest list to load");
	    }
	    return;  // nothing to load
	}

	ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	DataInputStream dis = new DataInputStream(bis);

	// read in number of entries
	int size = dis.readInt();
	iidMap = new HashMap(size);
	statearray = new int[size];

	for (int i = 0; i < size; i++) {
	    ConsumerUID iid = new ConsumerUID(dis.readLong()); 

	    statearray[i] = dis.readInt();

	    // put in interest id map
	    iidMap.put(iid, new Integer(i));
	}
	dis.close();
	bis.close();

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUG, "loaded " + size + " interest states");
	}
    }

    /**
     * parse the message from the byte array.
     */
    private Packet parseMessage(byte[] data) throws IOException {

	packetSize = data.length;

	ByteBuffer databuf = ByteBuffer.wrap(data);
	JMQByteBufferInputStream bis = new JMQByteBufferInputStream(databuf);
	Packet msg = new Packet(false);
        msg.generateTimestamp(false);
        msg.generateSequenceNumber(false);
	msg.readPacket(bis);
	bis.close();
	return msg;
    }

    private Packet parseMessage(VRecordRAF r) throws IOException {

	try {
	    r.rewind();

	    packetSize = r.readInt();

	    Packet pkt = new Packet();
	    pkt.generateTimestamp(false);
	    pkt.generateSequenceNumber(false);

	    // parse message
	    if (parent.useFileChannel) {
		pkt.readPacket(r.getChannel(), false);
	    } else {
		ByteBuffer buf = ByteBuffer.wrap(new byte[packetSize]);
		r.read(buf.array());
		JMQByteBufferInputStream bis =
				new JMQByteBufferInputStream(buf);
		pkt.readPacket(bis);
		bis.close();
	    }

	    return pkt;
	} catch (Throwable t) {
	    logger.log(logger.ERROR, parent.myDestination +
			":failed to parse message(size=" + packetSize +
			") from vrecord(" + r + ")", t);
	    IOException e = new IOException(t.toString());
	    e.setStackTrace(t.getStackTrace());
	    throw e;
	}
    }

    /**
     * Cache the interest list.
     * and write it to backing record
     */
    private void storeStates(VRecordRAF rec, ConsumerUID[] iids, int[] states,
	boolean sync) throws IOException {

	ByteBuffer buf = serializeStates(iids, states);
	buf.rewind();

	// position after the size of message and the message
	rec.position(INT_SIZE + packetSize);
	rec.write(buf);

	if (sync) {
		if(Store.getDEBUG_SYNC())
    	{
    		Globals.getLogger().log(Logger.DEBUG, "sync storeStates mid="+mid);
    	}
	    rec.force();
	}
    }

    private ByteBuffer serializeStates(ConsumerUID[] iids, int[] states) {

	int size = iids.length;
	iidMap = new HashMap(size);
	statearray = new int[size];

	int buflen = INT_SIZE + size * ENTRY_SIZE;
	ByteBuffer buf = ByteBuffer.wrap(new byte[buflen]);

	// write number of entries
	buf.putInt(size);

	for (int i = 0; i < size; i++) {
            buf.putLong(iids[i].longValue());
	    buf.putInt(states[i]);

	    // put in cache
	    iidMap.put(iids[i], new Integer(i));
	    statearray[i] = states[i];
	}
	return buf;
    }
}
