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
 * @(#)FileTransactionLogRecord.java	1.2 06/29/07
 */ 

package com.sun.messaging.jmq.io.txnlog.file;

import com.sun.messaging.jmq.io.txnlog.*;

/**
 * Encapsulates a log entry. Used by the TransactionLogWriter to store and 
 * retrieve as a transaction record.
 *<p>
 * Format of data for writing (48 byte header) is specified as follows:
 * <p>
 * 1. Record Magic # (int, 0-3)
 * <p>
 * 2. Record Type (int, 4-7)
 * <p>
 * 3. Record Body Size (int, 8-11)  
 * <p>
 * 4. Timestamp (long, 12-19)
 * <p>
 * 5. Record Sequence Number (long, 20-28)
 * <p>
 * 6. Check Point Sequence (long, 29-36)
 * <p>
 * 7. Record Body Check Sum (long, 37-44)
 * <p>
 * 8. Record Header Reserve (int, 45-48)
 * <p>
 * 9. Record body data (byte[], size defined in #3 above)
 *
 *<p>
 * 
 * @see TransactionLogRecord
 * @see FileTransactionLogWriter
 */

public class FileTransactionLogRecord implements TransactionLogRecord {
    
    private long timestamp = 0;
    
    private long sequence = 0;
    
    private int logType = -1;
    
    private byte[] logBody = null;
    
    private long cpSequence = -1;
    
    private Exception exception;
    
    private boolean isWritten;
    
    /**
     * Create a new entry to write to txn log.  
     * Timestamp, CP sequence, and entry sequence are set before write to the log file.
     */
    public FileTransactionLogRecord () {
    }
    
    /**
     * Construct a FileTransactionLogRecord from an entry read from the log file.
     */
    public FileTransactionLogRecord (long timestamp, int type, long seq) {
        this.timestamp = timestamp;
        this.logType = type;
        this.sequence = seq;
    }
    
    /**
     * returns the timestamp of the entry/
     */
    public long getTimestamp () {
        return timestamp;
    }
    
    public void setTimestamp (long ts) {
        this.timestamp = ts;
    }
    
    /**
     * returns the sequence (combination of timestamp +
     * sequence should be unique for a system)
     */
    public long getSequence () {
        return sequence;
    }
    
    public void setSequence (long seq) {
        this.sequence = seq;
    }
    
    /**
     * retrieves the type of the entry.
     * @see StateType
     * @return an integer which matches to a type
     */
    public int getType () {
        return logType;
    }
    
    public void setType (int type) {
        this.logType = type;
    }
    
    /** sets the formatted bytes for writing or
     * sending.
     */
    public void setBody (byte[] body) {
        this.logBody = body;
    }
    
    /** retrieves the formatted bytes for writing or
     * sending.
     */
    public byte[] getBody () {
        return logBody;
    }
    
    /**
     * This is set by FileTransactionLogWriter after written to the txn log file.
     */
    public void setCheckPointSequence (long cpseq) {
        this.cpSequence = cpseq;
    }
    
    /**
     * Get the cp seq number of this txn log record.
     */
    public long getCheckPointSequence () {
        return this.cpSequence;
    }
    
    /** 
     * Get the associated exception for this log entry.
     *
     * @return the body data of this entry.
     */
    public Exception getException()
    {
    	return exception;
    }
    
    /**
     * Set the associated exception for this log entry.
     * @param exception the exception for this log entry.
     */
    public void setException(Exception exception)
    {
    	this.exception = exception;
    }

    
    /**
     * Get the written flag for this log entry.
     * @return true if this record has been written to the log.
     */
    public boolean isWritten()
    {
    	return isWritten;
    }
    
    /**
     * Set the written flag for this log entry.
    * @param flag the written flag for this log entry.
     */
    public void setWritten(boolean flag)
    {
    	isWritten = flag;
    }
    
    
    public String toString () {
        return "CPSequence="+cpSequence+ ", Sequence=" + sequence+", type="+logType+", timestamp="+timestamp + ", body size="+getBody ().length;
    }
}
