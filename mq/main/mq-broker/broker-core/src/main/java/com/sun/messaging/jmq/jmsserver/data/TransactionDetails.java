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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.JMQXid;

public class TransactionDetails {

	private int type;
	private TransactionUID tid;
	private JMQXid xid;
	private int state;
	private boolean complete; //used for cluster transactions

	public TransactionDetails() {

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public TransactionUID getTid() {
		return tid;
	}

	public void setTid(TransactionUID tid) {
		this.tid = tid;
	}

	public void readContent(DataInputStream dis) throws IOException,
			BrokerException {

		//read Type
		type = dis.readByte();

		// read txnID
		long tid = dis.readLong();
		TransactionUID txUID = new TransactionUID(tid);
		this.setTid(txUID);

		// read state
		int state = dis.readInt();
		this.setState(state);
		
		// complete stored as int 
		int completeVal = dis.readInt();
		complete = completeVal==1;

		// read xid if present
		readXid(dis);

	}

	public void writeContent(DataOutputStream dos) throws IOException {
		// write type
		dos.writeByte(type);

		// write transaction
		dos.writeLong(getTid().longValue()); // Transaction ID (8
		// bytes)
		// write txn state
		dos.writeInt(getState());
		// write txn state
		if (complete)
			dos.writeInt(1);
		else
			dos.writeInt(0);

		// write xid if present
		writeXid(dos);

	}

	protected void readXid(DataInputStream dis) throws IOException {

		boolean xidExists = dis.readBoolean();

		if (xidExists) {
			xid = JMQXid.read(dis);
		}
	}

	protected void writeXid(DataOutputStream dos) throws IOException {
		if (getXid() == null) {
			dos.writeBoolean(false);
		} else {
			dos.writeBoolean(true);
			xid.write(dos);
		}

	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("type=").append(type);
		s.append(" state=").append(TransactionState.toString(state));
		s.append(" txnId = ").append(getTid());
		s.append(" Xid = ").append(getXid());
		s.append(" complete = ").append(isComplete());
		return new String(s);
	}

	public JMQXid getXid() {
		return xid;
	}

	public void setXid(JMQXid xid) {
		this.xid = xid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}
