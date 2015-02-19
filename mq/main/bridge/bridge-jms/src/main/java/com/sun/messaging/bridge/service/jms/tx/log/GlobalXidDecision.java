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

package com.sun.messaging.bridge.service.jms.tx.log;

import java.io.*;
import com.sun.messaging.bridge.service.jms.tx.GlobalXid;


/**
 * @author amyk
 */ 

public class GlobalXidDecision implements Externalizable {

    public static final int COMMIT = 0;
    public static final int ROLLBACK = 1;

    private GlobalXid _xid = null;
    private int _decision = COMMIT;

    public GlobalXidDecision() {}

    public GlobalXidDecision(GlobalXid xid, int decision) {

        if (decision != COMMIT && decision != ROLLBACK) {
            throw new IllegalArgumentException(
            "Invalid global decision value: "+decision);
        }
        _xid = xid;
        _decision = decision;
    }

    public GlobalXid getGlobalXid() {
        return _xid;
    }

    public int getGlobalDecision() {
        return _decision;
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        _xid.write(out);
        out.writeInt(_decision);
    }

    public void readExternal(ObjectInput in) throws IOException,
                                        ClassNotFoundException {
        _xid = GlobalXid.read(in);
        _decision = in.readInt();
    }

    private static String decisionString(int d) {
        if (d == COMMIT) return "COMMIT";
        if (d == ROLLBACK) return "ROLLBACK";
        return "UNKNOWN";
    }

    public String toString() { 
        return _xid.toString()+"("+decisionString(_decision)+")";
    }
}
