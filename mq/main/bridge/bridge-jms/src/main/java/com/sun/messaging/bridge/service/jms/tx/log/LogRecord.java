/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.messaging.bridge.service.jms.tx.log;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Collection;
import com.sun.messaging.bridge.service.jms.tx.GlobalXid;
import com.sun.messaging.bridge.service.jms.tx.BranchXid;
import com.sun.messaging.bridge.service.jms.tx.XAParticipant;


/**
 * @author amyk
 */ 

public class LogRecord implements Externalizable {

    private GlobalXidDecision _gxidd = null;
    private BranchXidDecision[] _bxidds = null;

    public LogRecord() {}

    public LogRecord(GlobalXid gxid, 
                     Collection<XAParticipant> parties,
                     int decision)
                     throws Exception {

        _gxidd = new GlobalXidDecision(gxid, decision);

        _bxidds = new BranchXidDecision[parties.size()];
        XAParticipant party = null;
        Iterator<XAParticipant> itr = parties.iterator();
        int i = 0;
        while( itr.hasNext()) {
            party = itr.next();
            _bxidds[i] = new BranchXidDecision(party.getBranchXid(), decision);
            i++;
        }
    }

    public GlobalXid getGlobalXid() {
        return _gxidd.getGlobalXid();
    }

    public int getGlobalDecision() {
        return _gxidd.getGlobalDecision();
    }

    public BranchXidDecision[] getBranchXidDecisions() {
        return _bxidds;
    }

    public int getBranchCount() {
        return _bxidds.length;
    }

    public void setBranchHeurCommit(BranchXid bxid) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                _bxidds[i].setBranchDecision(BranchXidDecision.HEUR_COMMIT);
            }
        }
        throw new NoSuchElementException("Branch "+bxid+" not found in "+_gxidd); 
    }

    public void setBranchHeurRollback(BranchXid bxid) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                _bxidds[i].setBranchDecision(BranchXidDecision.HEUR_ROLLBACK);
            }
        }
        throw new NoSuchElementException("Branch "+bxid+" not found in "+_gxidd); 
    }

    public void setBranchHeurMixed(BranchXid bxid) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                _bxidds[i].setBranchDecision(BranchXidDecision.HEUR_MIXED);
            }
        }
        throw new NoSuchElementException("Branch "+bxid+" not found in "+_gxidd); 
    }

    public boolean isHeuristicBranch(BranchXid bxid) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                return (_bxidds[i].isHeuristic() || (_bxidds[i].getBranchDecision() != _gxidd.getGlobalDecision()));
            }
        }
        throw new NoSuchElementException(
        "Branch "+bxid+" not found in global transaction "+_gxidd); 
    }

    public int getBranchDecision(BranchXid bxid) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
               return _bxidds[i].getBranchDecision();
            }
        }
        throw new NoSuchElementException("Branch "+bxid+" not found in "+_gxidd); 
    }

    public void setBranchDecision(BranchXid bxid, int d) throws Exception {
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                _bxidds[i].setBranchDecision(d);
            }
        }
        throw new NoSuchElementException("Branch "+bxid+" not found in "+_gxidd); 
    }

    protected void updateClientDataFromBranch(byte[] cd, BranchXid bxid) throws Exception { 
        for (int i = 0; i < _bxidds.length; i++) {
            if (_bxidds[i].getBranchXid().equals(bxid)) {
                cd[i] = (byte) _bxidds[i].getBranchDecision();
            }
        }
        throw new NoSuchElementException(
        "Branch "+bxid+" not found in global transaction "+_gxidd); 
    }

    protected void updateBranchFromClientData(byte[] cd) throws Exception { 
        for (int i = 0; i < _bxidds.length; i++) {
             _bxidds[i].setBranchDecision(cd[i]);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(_gxidd);
        out.writeObject(_bxidds);
    }

    public void readExternal(ObjectInput in) throws IOException,
                                        ClassNotFoundException {

        _gxidd = (GlobalXidDecision)in.readObject();
        _bxidds = (BranchXidDecision[])in.readObject();
        for (int i = 0; i < _bxidds.length; i++) { 
            _bxidds[i].getBranchXid().setFormatId(
                       _gxidd.getGlobalXid().getFormatId());
            _bxidds[i].getBranchXid().setGlobalTransactionId(
                       _gxidd.getGlobalXid().getGlobalTransactionId());
        }
    }

    public byte[] toBytes() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        ObjectOutputStream bos = new ObjectOutputStream(baos);
        bos.writeObject(this);
        bos.close();

        return baos.toByteArray();
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (i = 0; i <_bxidds.length; i++) {
            if (i == 0) sb.append("[");
            if (i > 0) sb.append(", ");
            sb.append(_bxidds[i].toString());
            sb.append(_bxidds[i].toString());
        }
        if (i > 0) sb.append("]");
        return _gxidd.toString()+sb.toString();
    }
}
