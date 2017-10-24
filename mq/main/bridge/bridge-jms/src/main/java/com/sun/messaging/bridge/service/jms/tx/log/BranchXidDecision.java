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
import com.sun.messaging.bridge.service.jms.tx.BranchXid;


/**
 * @author amyk
 */ 

public class BranchXidDecision implements Externalizable {

    //must be same as in GlobalXidDecision values
    public static final int COMMIT = 0;
    public static final int ROLLBACK = 1 ;

    //must not overlap with above 
    public static final int HEUR_COMMIT = 50;
    public static final int HEUR_ROLLBACK = 51 ;
    public static final int HEUR_MIXED = 52;

    private BranchXid _xid = null;
    private int _decision = COMMIT;

    public BranchXidDecision() {}

    public BranchXidDecision(BranchXid xid, int decision) throws Exception {
        if (decision != COMMIT && 
            decision != ROLLBACK &&
            decision != HEUR_COMMIT && 
            decision != HEUR_ROLLBACK &&
            decision != HEUR_MIXED) {

            throw new IllegalArgumentException("Invalid decision value: "+decision); 
        }
        _xid = xid;
        _decision = decision;
    }

    public BranchXid getBranchXid() {
        return _xid;
    }

    public int getBranchDecision() {
        return _decision;
    }

    public boolean isHeuristic() {
        return (_decision == HEUR_COMMIT ||
                _decision == HEUR_ROLLBACK ||
                _decision == HEUR_MIXED); 
    }

    public void setBranchDecision(int d) {
        if (d != COMMIT &&
            d != ROLLBACK &&
            d != HEUR_COMMIT &&
            d != HEUR_ROLLBACK &&
            d != HEUR_MIXED) {
            throw new IllegalArgumentException("Invalid decision value: "+d); 
        }
       _decision = d;
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        _xid.write(out);
        out.writeInt(_decision);
    }

    public void readExternal(ObjectInput in) throws IOException,
                                        ClassNotFoundException {
        _xid = BranchXid.read(in);
        _decision = in.readInt();
    }

    private static String decisionString(int d) {
        switch (d) {
            case COMMIT: return "COMMIT";
            case ROLLBACK: return "ROLLBACK";
            case HEUR_COMMIT: return "HEUR_COMMIT";
            case HEUR_ROLLBACK: return "HEUR_ROLLBACK";
            case HEUR_MIXED: return "HEUR_MIXED";
            default: return "UNKNOWN";
        }
    }
    public String toString() {
        return _xid.toString()+"("+decisionString(_decision)+")";
    }
}
