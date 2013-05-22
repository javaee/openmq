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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import com.sun.messaging.jmq.io.disk.VRFile;
import com.sun.messaging.jmq.io.disk.VRFileRAF;
import com.sun.messaging.jmq.io.disk.VRecordRAF;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.BaseTransaction;
import com.sun.messaging.jmq.jmsserver.data.TransactionWorkFactory;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.Logger;

/**
 * MessageInfo keeps track of a message and it's ack list. Has methods to parse
 * and persist them.
 */
class TransactionWorkInfo {
	static final short PENDING = -1; // -1=0xffff
	static final short DONE = 0;
	private static final int BYTE_SIZE = 1;
	private static final int SHORT_SIZE = 2;
	private static final int INT_SIZE = 4;
	private static final int LONG_SIZE = 8;

	// each entry for an interest (identified by ConsumerUID) and it's state
	// has: a long (ConsumerUID) and an int (state)
	private static final int ENTRY_SIZE = LONG_SIZE + INT_SIZE;

	private Logger logger = Globals.getLogger();
	private BrokerResources br = Globals.getBrokerResources();

	private BaseTransaction msg; // will set to null after client has it
	// bytes

	// cached info
	private TransactionUID tid;
	private int txnDataSize;

	private PreparedTxnStore parent = null;

	// backing buffer
	private VRecordRAF vrecord = null;

	/**
	 * if this returns successfully, the message and it's interest list are
	 * loaded from backing file message is from an individual file
	 */
	TransactionWorkInfo(PreparedTxnStore p, byte[] data, byte[] ilist)
			throws IOException {

		parent = p;

		try {
			// parse message
			BaseTransaction txnWork = TransactionWorkFactory
					.readFromBytes(data);

			// cache message info
			this.msg = txnWork;
			tid = (TransactionUID) txnWork.getTid();

		} catch (BrokerException be) {
			be.printStackTrace();
			throw new IOException(be.getMessage());
		} catch (IOException e) {
			logger.log(logger.ERROR, parent.storeName
					+ ":failed to parse message from byte array", e);
			throw e;
		}
	}

	/**
	 * if this returns successfully, the message and it's interest list are
	 * loaded from backing file message is from vrfile
	 */
	TransactionWorkInfo(PreparedTxnStore p, VRecordRAF r) throws IOException {
		parent = p;
		vrecord = r;

		// sanity check done while VRecords are loaded

		try {
			// parse message
			BaseTransaction txnWork = parseTransactionWork(r);

			// cache message info
			this.msg = txnWork;
			tid = txnWork.getTid();

		} catch (IOException e) {
			// free the bad VRecord
			parent.getVRFile().free(vrecord);

			throw e;
		}
	}

	/**
	 * if this returns successfully, message and it's interest states are
	 * persisted store message in vrfile
	 */
	TransactionWorkInfo(PreparedTxnStore p, VRFileRAF vrfile,
			BaseTransaction txnWork, byte[] databuf, boolean sync) throws IOException {

		parent = p;

		// cache message info
		tid = txnWork.getTid();
		txnDataSize = databuf.length;

		// format of data in buffer:
		// size of message (packetSize, int)
		// message (a blob)
		// size of interest list (int)
		// each interest entry has:
		// long
		// int

		int bufsize = INT_SIZE + txnDataSize + INT_SIZE;
		if(sync && Store.getDEBUG_SYNC())
		{
			String msg = "TransactionWorkInfor sync() "+tid;
			logger.log(Logger.DEBUG,msg);
		}

		if (!Globals.isMinimumWritesFileStore()) {
			synchronized (vrfile) {
				vrecord = (VRecordRAF) vrfile.allocate(bufsize);

				// start writing
				vrecord.setCookie(PENDING);

				// write packetSize
				vrecord.writeInt(txnDataSize);

				// write message
				
				vrecord.write(databuf);
				

				// done writing
				vrecord.setCookie(DONE);

				if (sync) {
					vrecord.force();
				}
			}
		} else {
			synchronized (vrfile) {
				// byte[] recordData = new byte[]
				// we will write the state and cooky at the same time as data
				// so need another 4 bytes (2 * short);
				ByteBuffer bbuf = ByteBuffer.allocate(bufsize + 4);

				bbuf.putShort(VRFile.STATE_ALLOCATED);
				bbuf.putShort(DONE);
				bbuf.putInt(txnDataSize);

				// write message
				
				bbuf.put(databuf);
				
				byte[] data = bbuf.array();

				vrecord = (VRecordRAF) vrfile.allocateAndWrite(bufsize, data);

				if (sync) {
					vrecord.force();
				}
			}
		}
	}

	/**
	 * if this returns successfully, message and it's interest states are
	 * persisted; store message in individual files
	 */
	TransactionWorkInfo(PreparedTxnStore p, BaseTransaction txnWork,byte[] databuf,
			boolean sync) throws IOException {
//		System.out.println("store in individual file");

		this.parent = p;

		tid = txnWork.getTid();
		if(databuf!=null)
			txnDataSize = databuf.length;
		
		
		parent.writeData(tid, databuf, null, sync);

	}

	/**
	 * Return the message. The message is only cached when it is loaded from the
	 * backing buffer the first time. It will be set to null after it is
	 * retrieved. From then on, we always read it from the backing buffer again.
	 */
	synchronized BaseTransaction getMessage() throws IOException {
		if (msg == null) {
			if (vrecord != null) {
				// read from backing buffer
				try {
					return parseTransactionWork(vrecord);
				} catch (IOException e) {
					logger.log(logger.ERROR, parent.storeName
							+ ":failed to parse message from vrecord("
							+ vrecord + ")", e);
					throw e;
				}
			} else {
				try {
					byte data[] = parent.loadData(tid);
					return parseTransactionWork(data);
				} catch (IOException e) {
					logger.log(logger.ERROR, parent.storeName
							+ ":failed to parse message from byte array", e);
					throw e;
				}
			}
		} else {
			BaseTransaction pkt = msg;
			msg = null;
			return pkt;
		}
	}

	

	// no need to synchronized, value set at object creation and wont change
	int getSize() {
		return txnDataSize;
	}

	// no need to synchronized, value set at object creation and wont change
	TransactionUID getID() {
		return tid;
	}

	/**
	 * clear out all cached info and release the backing buffer
	 */
	synchronized void free(boolean sync) throws IOException {
		if(sync && Store.getDEBUG_SYNC())
		{
			String msg = "TransactionWorkInfo free sync() "+ getID();
			logger.log(Logger.DEBUG,msg);
		}
		if (vrecord != null) {
			parent.getVRFile().free(vrecord);
			if (sync) {
				parent.getVRFile().force();
			}
			vrecord = null;
		} else {
			parent.removeData(tid, sync);
		}

		tid = null;

	}

	private BaseTransaction parseTransactionWork(byte[] data)
			throws IOException {
		try {
			BaseTransaction txnWork = TransactionWorkFactory
					.readFromBytes(data);

			return txnWork;
		} catch (Throwable t) {
			logger.log(logger.ERROR, parent.storeName
					+ ":failed to parse message(size=" + txnDataSize + ") ", t);
			IOException e = new IOException(t.toString());
			e.setStackTrace(t.getStackTrace());
			throw e;
		}
	}

	private BaseTransaction parseTransactionWork(VRecordRAF r)
			throws IOException {

		try {
			r.rewind();

			txnDataSize = r.readInt();

			byte[] data = new byte[txnDataSize];
			r.read(data);
			BaseTransaction txnWork = TransactionWorkFactory
					.readFromBytes(data);

			return txnWork;
		} catch (Throwable t) {
			logger.log(logger.ERROR, parent.storeName
					+ ":failed to parse message(size=" + txnDataSize
					+ ") from vrecord(" + r + ")", t);
			IOException e = new IOException(t.toString());
			e.setStackTrace(t.getStackTrace());
			throw e;
		}
	}

	synchronized void updateState(int state, boolean sync) throws IOException,
			BrokerException {

		// long   (txnId)
		long valOffset = LONG_SIZE + BYTE_SIZE;
		writeIntAtOffset(valOffset, state, sync);

	}

	synchronized void updateCompletion(int completionVal, boolean sync)
			throws IOException, BrokerException {

		// long   (txnId)
		long valOffset = LONG_SIZE + BYTE_SIZE + INT_SIZE;
		writeIntAtOffset(valOffset, completionVal, sync);

	}

	void writeIntAtOffset(long valOffset, int val, boolean sync)
			throws IOException, BrokerException {
		
		if(sync && Store.getDEBUG_SYNC())
		{
			String msg = "TransactionWorkInfo sync() "+tid;
			logger.log(Logger.DEBUG,msg);
		}
		
		if (vrecord != null) {

			// offset into VRecord
			// state gets written immediately after the transactionid

			//short: allocated state
			//short: complete state
			//int:   data size
			//+ type offset (see above)		
			//Globals.getLogger().log(Logger.INFO, " vv writeIntAtOffset = " + val);
			
		//	long offset = SHORT_SIZE + SHORT_SIZE + INT_SIZE + valOffset;
			long offset = SHORT_SIZE + SHORT_SIZE  + valOffset;
			vrecord.writeInt((int) offset, val);
			if (sync) {
				vrecord.force();
			}
		} else {
			// offset into attachment part			

			if (!parent.writeAttachmentData(tid, valOffset, true, val, sync)) {
                            String emsg = br.getKString(br.E_TRANSACTIONID_NOT_FOUND_IN_STORE, tid)+
                                                        ": "+parent.storeName;
                            logger.log(logger.ERROR, emsg);
                            throw new BrokerException(emsg);
			}

		}
	}

}
