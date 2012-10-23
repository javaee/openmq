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
 * @(#)ConfigChangeRecord.java	1.24 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.file;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.persist.api.ChangeRecordInfo;

import java.io.*;
import java.util.*;


/**
 * Keep track of all configuration change record.
 * File format: 4 byte magic number then each record is appended
 * to the backing file in this order:
 *	timestamp (long)
 *	length of byte array (int)
 *	byte array
 */
class ConfigChangeRecord {

    Logger logger = Globals.getLogger();
    BrokerResources br = Globals.getBrokerResources();
    protected BrokerConfig config = Globals.getConfig();

    static final String BASENAME = "configrecord"; // basename of data file

    // to make sure we are reading a good file
    static final int MAGIC = 0x12345678;

    // cache all persisted records
    // list of time stamps
    private ArrayList timeList = new ArrayList();

    // list of records
    private ArrayList recordList = new ArrayList();

    private File backingFile = null;
    private RandomAccessFile raf = null;

    // when instantiated, all data are loaded
    ConfigChangeRecord(File topDir, boolean clear) throws BrokerException {

	try {
	    backingFile = new File(topDir, BASENAME);
	    raf = new RandomAccessFile(backingFile, "rw");

	    if (clear) {
		clearAll(false);
		if (Store.getDEBUG()) {
		    logger.log(logger.DEBUGHIGH,
			"ConfigChangeRecord initialized with clear option");
		}
	    } else {
		// true=initialize new file
		loadData(backingFile, raf, timeList, recordList, true);
	    }
	} catch (IOException e) {
	    logger.log(logger.ERROR, br.X_LOAD_CONFIGRECORDS_FAILED, e);
	    throw new BrokerException(
			br.getString(br.X_LOAD_CONFIGRECORDS_FAILED), e);
	}
    }

    /**
     * Append a new record to the config change record store.
     *
     * @param timestamp	The time when this record was created.
     * @param recordData The record data
     * @exception BrokerException if an error occurs while persisting the data
     * @exception NullPointerException	if <code>recordData</code> is
     *			<code>null</code>
     */
    void storeConfigChangeRecord(long timestamp, byte[] recordData,
	boolean sync) throws BrokerException {

	synchronized (timeList) {

	    timeList.add(new Long(timestamp));
	    recordList.add(recordData);

	    try {
		appendFile(timestamp, recordData, sync);
	    } catch (IOException e) {
		timeList.remove(timeList.size()-1);
		recordList.remove(recordList.size()-1);
		logger.log(logger.ERROR, br.X_PERSIST_CONFIGRECORD_FAILED,
                    String.valueOf(timestamp));
		throw new BrokerException(
				br.getString(br.X_PERSIST_CONFIGRECORD_FAILED,
                                    String.valueOf(timestamp)),
				e);
	    }
	}
    }

    /**
     * Retrieve all records in the store since timestamp.
     *
     * @return a list of ChangeRecordInfo, empty list of no records
     */
    public ArrayList<ChangeRecordInfo> getConfigChangeRecordsSince(long timestamp) {

	ArrayList records = new ArrayList();

	synchronized (timeList) {
	    int size = timeList.size();

	    int i = 0;
	    for (; i < size; i++) {
		Long stamp = (Long)timeList.get(i);
		if (stamp.longValue() > timestamp)
		    break;
	    }

	    for (; i < size; i++) {
		records.add(new ChangeRecordInfo((byte[])recordList.get(i),
                        ((Long)timeList.get(i)).longValue()));
	    }

	    return records;
	}
    }

    /**
     * Return all config records with their corresponding timestamps.
     *
     * @return a list of ChangeRecordInfo
     * @exception BrokerException if an error occurs while getting the data
     */
    public List<ChangeRecordInfo> getAllConfigRecords() throws BrokerException {
    ArrayList records = new ArrayList();

    for (int i = 0; i < timeList.size(); i++) {
        records.add(new ChangeRecordInfo((byte[])recordList.get(i), 
                        ((Long)timeList.get(i)).longValue()));
    }
	return records;
    }

    /**
     * Clear all records
     */
    // clear the store; when this method returns, the store has a state
    // that is the same as an empty store (same as when TxnAckList is
    // instantiated with the clear argument set to true
    void clearAll(boolean sync) throws BrokerException {

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUGHIGH,
			"ConfigChangeRecord.clearAll() called");
	}

	synchronized (timeList) {
	    timeList.clear();
	    recordList.clear();
	}

	try {
	    raf.setLength(0);

	    // write magic number
	    raf.writeInt(MAGIC);

	    if (sync) {
		sync();
	    }

	} catch (IOException e) {
	    logger.log(logger.ERROR, br.X_CLEAR_CONFIGTABLE_FAILED, e);
	}
    }

    void close(boolean cleanup) {
	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUGHIGH,
			"ConfigChangeRecord: closing, "+timeList.size()+
			" persisted records");
	}

	try {
	    raf.close();
	} catch (IOException e) {
	    if (Store.getDEBUG()) {
		logger.log(logger.DEBUG,
			"Got IOException while closing:"+backingFile, e);
		e.printStackTrace();
	    }
	}
    }

    /**
     * Get debug information about the store.
     * @return A Hashtable of name value pair of information
     */  
    Hashtable getDebugState() {
	Hashtable t = new Hashtable();
	t.put("Config change records", String.valueOf(timeList.size()));
	return t;
    }

    void printInfo(PrintStream out) {
	out.println("\nConfiguration Change Record");
	out.println("---------------------------");
	out.println("backing file: " + backingFile);
	out.println("number of records: " + timeList.size());
    }

    void sync() throws BrokerException {
	try {
	    // bug 5042763:
	    // don't sync meta data for performance reason
		if(Store.getDEBUG_SYNC())
		{
			String msg = "ConfigChangeRecord sync()";
			logger.log(Logger.DEBUG,msg);
		}
	    raf.getChannel().force(false);
	} catch (IOException e) {
	    throw new BrokerException(
		"Failed to synchronize file: " + backingFile, e);
	}
    }

    private void loadData(File filename, RandomAccessFile dataraf,
	ArrayList t, ArrayList r, boolean init) throws IOException {

	if (dataraf.length() == 0) {
	    if (init) {
		// write magic number
		dataraf.writeInt(MAGIC);
		if (Store.getDEBUG()) {
		    logger.log(logger.DEBUGHIGH,
				"initialized new file with magic number, "
				+ filename);
		}
	    }
	    return;
	}

	loadData(filename, dataraf, t, r);
    }

    /**
     * load data data from file
     * file format:
     *	4 byte magic number (0x12345678)
     *  records....
     * each record has the format:
     *	timestamp(long)
     *	record length(int)
     *	byte[] of record
     */
    private void loadData(File filename, RandomAccessFile dataraf,
	ArrayList t, ArrayList r) throws IOException {

	boolean done = false;

	// no record persisted yet
	if (dataraf.length() == 0) {
	    return;
	}

	// check magic number
	int magic = dataraf.readInt();
	if (magic != MAGIC) {
	    throw new StreamCorruptedException(
		br.getString(br.E_BAD_CONFIGRECORD_FILE, filename));
	}

	long pos = dataraf.getFilePointer();
	// read until the end of file
	while (!done) {
	    try {
		// read until end of file
		long timestamp = dataraf.readLong();
		int len = dataraf.readInt();
		byte[] record = new byte[len];
		int cnt = dataraf.read(record, 0, len);
		pos = dataraf.getFilePointer();

		t.add(new Long(timestamp));
		r.add(record);
                if (cnt < 0) {
                    done = true;
                    break;
                }
                continue;
	    } catch (EOFException e) {
		if (pos != dataraf.getFilePointer()) {
		    // in case broker failed while part of a record
		    // is being written
		    dataraf.setLength(pos);

		    logger.log(logger.WARNING,
			br.W_BAD_CONFIGRECORD, filename, new Long(pos));
		}
		done = true;
                break;
	    } catch (IOException e) {
		logger.log(logger.WARNING,
			br.W_EXCEPTION_LOADING_CONFIGRECORDS,
			new Integer(t.size()), new Long(pos), e);

		// truncate rest of the file
		dataraf.setLength(pos);

		done = true;
                break;
	    }
	}

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUGHIGH,
		"loaded " + t.size() + " records from " + filename);
	}
    }

    // append data to file
    private void appendFile(long timestamp, byte[] buf, boolean sync)
	throws IOException, BrokerException {

	// file position before write
	long pos = raf.getFilePointer();

	try {
	    // write data
	    raf.writeLong(timestamp);
	    raf.writeInt(buf.length);
	    raf.write(buf);

	    if (sync) {
		sync();
	    }

	} catch (IOException e) {
	    // truncate file to end of previous record
	    raf.setLength(pos);
	    throw e;
	}

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUG, "configrecord: appended ts=" + timestamp +
			"; and record of " + buf.length + " bytes to file");
	}
    }
}


