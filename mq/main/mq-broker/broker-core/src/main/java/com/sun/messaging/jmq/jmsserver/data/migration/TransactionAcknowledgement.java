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
 * @(#)TransactionAcknowledgement.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.migration;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.*;
import java.io.*;

/**
 * Acknowledgement for transactions. 
 *
 * Object format prior to 370 filestore, use for migration purpose only.
 * @see com.sun.messaging.jmq.jmsserver.data.TransactionAcknowledgement
 */
public class TransactionAcknowledgement implements Serializable
{
    // compatibility w/ 3.0.1
    static final long serialVersionUID = 1518763750089861353L;

    private static Logger logger = Globals.getLogger();

    transient SysMessageID sysid = null;
    ConsumerUID iid = null;

    // sid is the stored UID associated w/ the ack
    // at this point, we really just want the stored uid
    // BUT in the future we may want the original UID
    // Since we kept iid in the past, we still keep it
    // for support
    ConsumerUID sid = null;

    // default construct for uninitialized object
    public TransactionAcknowledgement() {
    }

    /**
     * Construct the acknowledgement with the specified sysid and iid.
     * @param sysid	message system id
     * @param iid	interest id
     */
    public TransactionAcknowledgement(SysMessageID sysid, ConsumerUID iid,
            ConsumerUID sid) {
        this.sysid = sysid;
        this.iid = iid;
        this.sid = sid;
    }

    /**
     * @return the interest id
     */
    public ConsumerUID getConsumerUID() {
        return iid;
    }

    /**
     * @return the stored interest id
     */
    public ConsumerUID getStoredConsumerUID() {
        return sid;
    }

    /**
     * @return the message system id
     */
    public SysMessageID getSysMessageID() {
	return sysid;
    }

    /**
     * Returns a hash code value for this object.
     * ?? just added the hashCode of sysid and iid together ??
     */
    public int hashCode() {
	return sysid.hashCode() + iid.hashCode();
    }

    // just compare the hashcode
    public boolean equals(Object o) {
	if ((o instanceof TransactionAcknowledgement) &&
	    (hashCode() == o.hashCode())) {
	    return true;
	} else {
	    return false;
	}
    }

    public String toString() {
	return "[" + sysid.toString() + "]" + iid.toString() + ":"
             + sid.toString();
    }

    // for serializing the object
    private void writeObject(ObjectOutputStream s) throws IOException {
	sysid.writeID(new DataOutputStream(s));
	s.writeObject(iid);
	s.writeObject(sid);
    }

    // for serializing the object
    private void readObject(ObjectInputStream s)
	throws IOException, ClassNotFoundException {

	sysid = new SysMessageID();
	sysid.readID(new DataInputStream(s));
	iid = (ConsumerUID)s.readObject();
        try {
	    sid = (ConsumerUID)s.readObject();
        } catch (Exception ex) { // deal w/ missing field in 3.0.1
            logger.log(Logger.DEBUG,"ReadObject: old transaction format");
            sid = iid;
        }
    }

    public Object readResolve() throws ObjectStreamException {
        // Replace w/ the new object
        Object obj = new 
            com.sun.messaging.jmq.jmsserver.data.TransactionAcknowledgement(
                sysid, iid, sid);
        return obj;
    }
}
