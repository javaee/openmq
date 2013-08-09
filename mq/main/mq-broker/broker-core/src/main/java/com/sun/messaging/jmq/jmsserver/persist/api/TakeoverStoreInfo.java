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
 * @(#)TakeoverStoreInfo.java	1.8 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.api;

import java.util.List;
import java.util.Date;
import java.util.Map;

/**
 * This immutable object encapsulates general information about
 * the store info of the broker that is being taken over.
 */
public final class TakeoverStoreInfo {

    private String targetName = null;      // Broker that is being taken over
    private List dstList = null;           // Local destination this broker owns
    private Map msgMap = null;             // Msg IDs and corresponding dst IDs this broker owns
    private List txnList = null;           // Transaction this broker owns
    private List remoteTxnList = null;     // Remote txn this broker participates in
    private long lockAcquiredTime = 0L;    // Timestamp when takeover lock is acquired
    private HABrokerInfo savedInfo = null; // Saved state of the broker being taken over
    private List<Long> takeoverStoreSessions = null;    

    /**
     * Constructor
     * @param bkrID the Broker ID that is being taken over
     * @param bkrInfo the saved state of the broker
     * @param ts timestamp when the takeover lock is acquired
     */
    public TakeoverStoreInfo( String bkrID, HABrokerInfo bkrInfo, long ts ) {

        lockAcquiredTime = ts;
        targetName = bkrID;
        savedInfo = bkrInfo;
    }

    public TakeoverStoreInfo(String targetName, long takeoverStartTime) {

        this.lockAcquiredTime = takeoverStartTime;
        this.targetName = targetName;
    }

    /**
     * Return the target name that is being taken over.
     * @return the targetName that is being taken over
     */
    public final String getTargetName() {
        return targetName;
    }

    /**
     * Returns the timestamp when the takeover lock was acquired.
     * @return timestamp when the lock is acquired
     */
    public final long getLockAcquiredTime() {
        return lockAcquiredTime;
    }

    /**
     * Retrieve all local destinations that this broker previously owns.
     * @return a List of all local destinations that this broker owns
     */
    public final List getDestinationList() {
        return dstList;
    }

    /**
     * Retrieve all message IDs and corresponding destination IDs that this broker previously owns.
     * @return a Map of message IDs and corresponding destination IDs
     */
    public final Map getMessageMap() {
        return msgMap;
    }

    /**
     * Retrieve all transactions that this broker previously owns.
     * @return a List of all transactions that this broker owns
     */
    public final List getTransactionList() {
        return txnList;
    }

    /**
     * Retrieve all remote transactions that this broker previously participates in.
     * @return a List of all transactions that this broker owns
     */
    public final List getRemoteTransactionList() {
        return remoteTxnList;
    }

    /**
     * Retrieve the saved state of broker.
     */
    public final HABrokerInfo getSavedBrokerInfo() {
        return savedInfo;
    }

    /**
     * Set local destinations that this broker previously owns.
     * @param list local destination this broker owns
     */
    public final void setDestinationList( List list ) {
        dstList = list;
    }

    /**
     * Set the transactions that this broker previously owns.
     * @param list transaction this broker owns
     */
    public final void setTransactionList( List list ) {
        txnList = list;
    }

    /**
     * Set the remote transactions that this broker previously participates in.
     * @param list transaction this broker owns
     */
    public final void setRemoteTransactionList( List list ) {
        remoteTxnList = list;
    }

    /**
     * Set message IDs and corresponding destination IDs that this broker previously owns.
     * @param map a Map message IDs and corresponding destination IDs
     */
    public final void setMessageMap( Map map ) {
        msgMap = map;
    }

    public final void setTakeoverStoreSessionList( List<Long> list ) {
        takeoverStoreSessions = list;
    }

    public final List<Long> getTakeoverStoreSessionList() {
        return takeoverStoreSessions;
    }
}
