/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class TransactionWorkMessageAck {

	DestinationUID destUID;
	SysMessageID sysMessageID;
	ConsumerUID consumerID;

        //for non-newTxnLog processing and persistence
        TransactionAcknowledgement ta = null;

	public TransactionWorkMessageAck() {

	}

	public TransactionWorkMessageAck(DestinationUID dest, SysMessageID sysMessageID,
			ConsumerUID consumerID) {
		this.destUID = dest;
		this.sysMessageID = sysMessageID;
		this.consumerID = consumerID;
	}

	public DestinationUID getDestUID() {
		return destUID;
	}

	public void setDest(DestinationUID dest) {
		this.destUID = dest;
	}

	public SysMessageID getSysMessageID() {
		return sysMessageID;
	}

	public void setSysMessageID(SysMessageID sysMessageID) {
		this.sysMessageID = sysMessageID;
	}

	public ConsumerUID getConsumerID() {
		return consumerID;
	}

	public void setConsumerID(ConsumerUID consumerID) {
		this.consumerID = consumerID;
	}
	
        public void setTransactionAcknowledgement(TransactionAcknowledgement ta) {
            this.ta = ta; 
        }

        public TransactionAcknowledgement getTransactionAcknowledgement() {
            return ta; 
        }

	public String toString()
	{
		StringBuffer result = new StringBuffer("dest=").append(destUID);
		result.append(" sysMessageID=").append(sysMessageID);
		result.append(" consumerID=").append(consumerID);		
		return result.toString();
	}
	
	public void writeWork(DataOutputStream dos) throws IOException {
		dos.writeUTF(destUID.toString()); 
		sysMessageID.writeID(dos);
		dos.writeLong(consumerID.longValue()); 
	}
	
	public void readWork(DataInputStream dis) throws IOException,
			BrokerException {
		String dest = dis.readUTF();
		destUID = new DestinationUID(dest);
		sysMessageID = new SysMessageID();
		sysMessageID.readID(dis);
		long cid = dis.readLong();
		consumerID = new ConsumerUID(cid);
	}
}
