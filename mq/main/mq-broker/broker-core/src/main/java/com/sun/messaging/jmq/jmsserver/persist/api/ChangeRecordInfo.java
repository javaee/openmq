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
 */ 

package com.sun.messaging.jmq.jmsserver.persist.api;

import com.sun.messaging.jmq.jmsserver.cluster.api.ClusterBroadcast;

/**
 */
public class ChangeRecordInfo {

    public static final int TYPE_RESET_PERSISTENCE = ClusterBroadcast.TYPE_RESET_PERSISTENCE;

    public static final int FLAG_LOCK = 1; 

    //bits in flag for new interest (durable sub)
    public static final int SHARED    = 0x00000001;
    public static final int JMSSHARED = 0x00000002;

    private Long seq = null;
    private String uuid = null;
    private String resetUUID = null;
    private byte[] record = null;
    private int type = 0;
    private String ukey = null;
    private boolean isduraAdd = false;
    private long timestamp = 0;
    private boolean isSelectAll = false;
    private int flag = 0; 

    public ChangeRecordInfo() {}
    
    /*
     * @param ukey must not be null if this object is going to persist store
     */
    public ChangeRecordInfo(Long seq, String uuid, byte[] record,
                            int type, String ukey, long timestamp) {
        this.seq = seq;
        this.uuid = uuid;
        this.record = record;
        this.type = type;
        this.ukey = ukey;
        this.timestamp = timestamp;
    }

    public ChangeRecordInfo(byte[] record, long timestamp) {
        this.record = record;
        this.timestamp = timestamp;
    }

    public void setRecord(byte[] rec) {
        record = rec;
    }

    public byte[] getRecord() {
        return record;
    }

    public void setTimestamp(long ts) {
        timestamp = ts;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }


    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setResetUUID(String uuid) {
        this.resetUUID = uuid;
    }

    public String getResetUUID() {
        return this.resetUUID;
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public String getUKey() {
        return ukey;
    }

    public void setUKey(String k) {
        ukey = k;
    }

    public boolean isDuraAddRecord() {
        return isduraAdd;
    }

    public void setDuraAdd(boolean b) {
        isduraAdd = b;
    }

    public void setFlagBit(int bit) {
        flag |= bit;
    }

    public int getFlag() {
        return flag;
    }
    public static String getFlagString(int f) {
        if ((f & SHARED) == SHARED) {
            if ((f & JMSSHARED) == JMSSHARED) {
                return "jms";
            } 
            return "mq";
        }
        return "";
    }   

    public void setIsSelectAll(boolean b) {
        isSelectAll = b;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    } 

    public String toString() {
        return "seq="+seq+", uuid="+uuid+", type="+type+
            ", timestamp="+timestamp+", resetUUId="+resetUUID;
    }
}
