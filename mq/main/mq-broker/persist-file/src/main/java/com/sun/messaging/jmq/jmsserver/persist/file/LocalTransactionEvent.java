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

package com.sun.messaging.jmq.jmsserver.persist.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.data.BaseTransaction;
import com.sun.messaging.jmq.jmsserver.data.LocalTransaction;
import com.sun.messaging.jmq.jmsserver.data.TransactionAcknowledgement;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;
import com.sun.messaging.jmq.jmsserver.data.TransactionWork;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public abstract class LocalTransactionEvent extends TransactionEvent {

	public static final byte Type1PCommitEvent = 0;
	public static final byte Type2PPrepareEvent = 1;
	public static final byte Type2PCompleteEvent = 2;

	LocalTransaction localTransaction;

	int getType() {
		return BaseTransaction.LOCAL_TRANSACTION_TYPE;
	}

	abstract int getSubType();

	static TransactionEvent create(byte subtype) {
		TransactionEvent result = null;
		switch (subtype) {
		case Type1PCommitEvent:
			result = new LocalTransaction1PCommitEvent();
			break;
		case Type2PPrepareEvent:
			result = new LocalTransaction2PPrepareEvent();
			break;
		case Type2PCompleteEvent:
			result = new LocalTransaction2PCompleteEvent();
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return result;
	}

	public LocalTransaction getLocalTransaction() {
		return localTransaction;
	}

	public void setLocalTransaction(LocalTransaction localTransaction) {
		this.localTransaction = localTransaction;
	}
}

class LocalTransaction1PCommitEvent extends LocalTransactionEvent {

	int getSubType() {
		return Type1PCommitEvent;
	}

	// no need to store transaction info as this will not be long lasting

	// write transaction details
	// write work

	byte[] writeToBytes() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(BaseTransaction.LOCAL_TRANSACTION_TYPE);
		dos.writeByte(Type1PCommitEvent);
		localTransaction.getTransactionDetails().writeContent(dos);
		localTransaction.getTransactionWork().writeWork(dos);

		byte[] data = bos.toByteArray();
		dos.close();
		bos.close();
		return data;
	}

	void readFromBytes(byte[] data) throws IOException, BrokerException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);

		localTransaction = new LocalTransaction();
		dis.skip(2);
		localTransaction.getTransactionDetails().readContent(dis);
		TransactionWork work = new TransactionWork();
		work.readWork(dis);
		localTransaction.setTransactionWork(work);
		dis.close();
		bais.close();
	}

}

class LocalTransaction2PPrepareEvent extends LocalTransactionEvent {

	int getSubType() {
		return Type2PPrepareEvent;
	}

	byte[] writeToBytes() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(BaseTransaction.LOCAL_TRANSACTION_TYPE);
		dos.writeByte(Type2PPrepareEvent);
		localTransaction.getTransactionDetails().writeContent(dos);
		localTransaction.getTransactionWork().writeWork(dos);

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream(1024);
		ObjectOutputStream oos = new ObjectOutputStream(baos2);

		oos.writeObject(localTransaction.getTransactionState());
		oos.close();

		byte[] data = baos2.toByteArray();
		int length = data.length;
		dos.writeInt(length);
		dos.write(data);

		baos2.close();

		byte[] data2 = bos.toByteArray();
		dos.close();
		bos.close();
		return data2;
	}

	void readFromBytes(byte[] data) throws IOException, BrokerException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);

		localTransaction = new LocalTransaction();
		dis.skip(2);
		localTransaction.getTransactionDetails().readContent(dis);
		TransactionWork work = new TransactionWork();
		work.readWork(dis);
		localTransaction.setTransactionWork(work);

		// need to write transaction info here
		int objectBodySize = dis.readInt();

		byte[] objectBody = new byte[objectBodySize];
		dis.read(objectBody);

		ByteArrayInputStream bais2 = new ByteArrayInputStream(objectBody);
		ObjectInputStream ois = new ObjectInputStream(bais2);

		try {

			TransactionState ts = (TransactionState) ois.readObject();
			
			localTransaction.setTransactionState(ts);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ois.close();
		bais2.close();

		dis.close();
		bais.close();
	}
}

class LocalTransaction2PCompleteEvent extends LocalTransactionEvent {

	int getSubType() {
		return Type2PCompleteEvent;
	}

	byte[] writeToBytes() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeByte(BaseTransaction.LOCAL_TRANSACTION_TYPE);
		dos.writeByte(Type2PCompleteEvent);
		localTransaction.getTransactionDetails().writeContent(dos);

		byte[] data = bos.toByteArray();
		dos.close();
		bos.close();
		return data;
	}

	void readFromBytes(byte[] data) throws IOException, BrokerException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);

		localTransaction = new LocalTransaction();
		dis.skip(2);
		localTransaction.getTransactionDetails().readContent(dis);

		dis.close();
		bais.close();
	}
}
