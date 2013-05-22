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
 * @(#)TransactionLogRecord.java	1.2 06/29/07
 */ 

package com.sun.messaging.jmq.io.txnlog;

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
 * @see TransactionLogWriter
 */

public interface TransactionLogRecord {
 
   /**
    * Get the timestamp of the log record.
    * This value is set by the TransactionLogWriter
    * when writing the entry to the log file.
    *
    * @see #setTimestamp
    * @return the timestamp that this record is created.
    */
   public long getTimestamp();
   
   /**
    * Set the timestamp for this log record.
    * This value is set by the TransactionLogWriter
    * when writing the entry to the log file.
    *
    * @see #getTimestamp
    */
   public void setTimestamp (long timestamp);

   /**
    * Get the sequence number of the log record.
    *
    * @return the sequence number of this log record.
    */
   public long getSequence();
   
   /**
    * Set the sequence number for this log record.
    * This value is set by the TransactionLogWriter
    * when writing the entry to the log file.
    *
    * @param sequenceNumber the number assigned to this 
    * log entry.
    */
   public void setSequence (long sequenceNumber);
   
   /**
    * Get the check point sequence number.  The
    * value is assigned by the TransactionLogWriter
    * right before TransactionLogWriter.write()
    * returns.
    * <p>
    * All records added after a check point contains the same 
    * check point sequence number until a new checkpoint is
    * called.
    * <p>
    * @see TransactionLogWriter#write
    *
    * @return the assigned check point sequence.
    * 
    */
   public long getCheckPointSequence ();

   /**
    * Set the specified check point sequence to the log
    * entry.
    * <p>
    * Each log record is assigned the same check point sequence
    * number until a new checkpoint is called.
    * <p>
    *
    * @see TransactionLogWriter#write
    */
   public void setCheckPointSequence (long cpSequence);
   
   /**
    * Get the record type of the log entry.
    *  
    * @see TransactionLogType
    * @return the type of the log record. 
    */
   public int getType();
   
   /**
    * Set the entry type of this log record.
    * @param type the transaction type of this log entry.
    */
   public void setType(int type);

   /**
    *  Set the log record body bytes to this log record.
    */
   public void setBody(byte[] body);

   /** 
    * Get the log record body bytes from this log entry.
    *
    * @return the body data of this entry.
    */
   public byte[] getBody();
   
   /** 
    * Get the associated exception for this log entry.
    *
    * @return the body data of this entry.
    */
   public Exception getException();
   
   /**
    * Set the associated exception for this log entry.
    * @param exception the exception for this log entry.
    */
   public void setException(Exception exception);

   /**
    * Get the written flag for this log entry.
    * @return true if this record has been written to the log.
    */
   public boolean isWritten();
   
   /**
    * Set the written flag for this log entry.
   * @param flag the written flag for this log entry.
    */
   public void setWritten(boolean flag);

}

