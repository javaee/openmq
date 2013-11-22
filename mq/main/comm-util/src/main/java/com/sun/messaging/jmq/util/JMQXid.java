/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)JMQXid.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;
import javax.transaction.xa.Xid;

/**
 * JMQ version of the RI's Xid Implementation. We extend XidImpl
 * to add methods for marshalling and unmarshaling an Xid to I/O Streams.
 *
 * @see javax.transaction.xa.Xid
 */
public class JMQXid extends XidImpl {

    // compatibility w/ 3.0.1, 3.5, 3.6
    static final long serialVersionUID = -5632229224716804510L;

    public JMQXid() {
        super();
    }

    public JMQXid(Xid foreignXid) {
        super(foreignXid);
    }

    public boolean isNullXid() {
        return (formatId == NULL_XID && gtLength == 0 && bqLength == 0);
    }

    /**
     * Write the Xid to the specified DataOutputStream. This is an
     * alternative to serialization that is faster, more compact and
     * language independent.
     *
     * The data written is guaranteed to be a fixed size. In particular
     * It will be of size
     *
     *      4 + 2 + Xid.MAXGTRIDSIZE + 2 + Xid.MAXBQUALSIZE 
     *
     * Which in practice will be 4 + 2 + 2 + 64 + 64 = 136 bytes
     *
     * If the globalTransactionId or the branchQualifierId is less than
     * MAX*SIZE bytes, then it will be padded with trailing 0's.
     * 
     * The format of the written data will be:
     *
     *<PRE>
     *    0                   1                   2                   3
     *   |0 1 2 3 4 5 6 7|8 9 0 1 2 3 4 5|6 7 8 9 0 1 2 3|4 5 6 7 8 9 0 1|
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                     format Id                                 |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   | globalTransactionId Length    |   branchQualifier Length      |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                                                               |
     *   +                                                               +
     *   |                     globalTransactionId                       |
     *   +                    MAXGTRIDSIZE bytes                         +
     *                           .  .  .
     *   +                                                               +
     *   |                                                               |
     *   +                                                               +
     *   |                                                               |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                                                               |
     *   +                                                               +
     *   |                     branchQualifier                           |
     *   +                    MAXBQUALSIZE bytes                         +
     *                           .  .  .
     *   +                                                               +
     *   |                                                               |
     *   +                                                               +
     *   |                                                               |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   </PRE>
     *
     */
    public void write(DataOutput out)
        throws IOException {

        out.writeInt(formatId);
        out.writeShort(gtLength);
        out.writeShort(bqLength);

        // These are fixed size arrays. 
        out.write(globalTxnId, 0, MAXGTRIDSIZE);
        out.write(branchQualifier, 0, MAXBQUALSIZE);
    }

    /**
     * Read the Xid from the input stream
     */
    public static JMQXid read(DataInput in)
        throws IOException {

        JMQXid xid = new JMQXid();
        xid.formatId = in.readInt();
        xid.gtLength = in.readShort();
        xid.bqLength = in.readShort();

        // These are fixed size arrays
        in.readFully(xid.globalTxnId, 0, MAXGTRIDSIZE);
        in.readFully(xid.branchQualifier, 0, MAXBQUALSIZE);

        return xid;
    }

    /**
     * Size in bytes that the object will be when marshalled.
     */
    public static int size() {
        return 8 + MAXGTRIDSIZE + MAXBQUALSIZE;
    }
}
