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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public abstract class BaseTransaction {
	
	
	public static final long FORMAT_VERSION_1 = 1;
	// add a new format version whenever the format of transactions 
	// or transaction events changes.
	// This version will be stored in the headers of incompleteTransactionStore and txnLog files
	// so we can check if the file is compatible with current software version
	
	
	public static final long CURRENT_FORMAT_VERSION= FORMAT_VERSION_1;
	
	

	public static final int UNDEFINED_TRANSACTION_TYPE = 0;
	public static final int LOCAL_TRANSACTION_TYPE = 1;
	public static final int REMOTE_TRANSACTION_TYPE = 2;
	public static final int CLUSTER_TRANSACTION_TYPE = 3;
	public static final int NON_TRANSACTED_MSG_TYPE = 4;
	public static final int NON_TRANSACTED_ACK_TYPE = 5;
	public static final int MSG_REMOVAL_TYPE = 6;

	TransactionDetails transactionDetails;
	TransactionWork transactionWork;
	TransactionState transactionState;

	byte[] data;

	public BaseTransaction(int type) {
		transactionDetails = new TransactionDetails();
		transactionDetails.setType(type);
	}

	public int getType() {
		return transactionDetails.getType();
	}

	public int getState() {
		return transactionDetails.getState();
	}

	public TransactionUID getTid() {
		return transactionDetails.getTid();
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String toString() {

		return transactionDetails.toString();
	}

	public TransactionWork getTransactionWork() {
		return transactionWork;
	}

	public void setTransactionWork(TransactionWork transactionWork) {
		this.transactionWork = transactionWork;
	}

	public TransactionDetails getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(TransactionDetails transactionDetails) {
		this.transactionDetails = transactionDetails;
	}
	
	public TransactionState getTransactionState() {
		return transactionState;
	}

	public void setTransactionState(TransactionState transactionState) {
		this.transactionState = transactionState;
	}

	String getPrefix() {
		return "BaseTransaction: " + Thread.currentThread().getName() + " "
				+ this.getTid();
	}

	// io methods to read and write to byte array
	public void readFromBytes(byte[] data) throws IOException, BrokerException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);

		readData(dis);

		int objectBodySize = dis.readInt();

		byte[] objectBody = new byte[objectBodySize];
		dis.read(objectBody);

		ByteArrayInputStream bais2 = new ByteArrayInputStream(objectBody);
		ObjectInputStream ois = new ObjectInputStream(bais2);

		try {
			readObjects(ois);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ois.close();
		bais2.close();

		dis.close();
		bais.close();
	}

	public abstract void readData(DataInputStream dis) throws IOException,
			BrokerException;

	public abstract void readObjects(ObjectInputStream ois) throws IOException,
			BrokerException, ClassNotFoundException;

	public byte[] writeToBytes() throws IOException {
		// Log all msgs and acks for producing and consuming txn
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		writeData(dos);

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream(1024);
		ObjectOutputStream oos = new ObjectOutputStream(baos2);

		writeObjects(oos);
		oos.close();

		byte[] data = baos2.toByteArray();
		int length = data.length;
		dos.writeInt(length);
		dos.write(data);

		baos2.close();

		dos.close();
		baos.close();

		byte[] data2 = baos.toByteArray();
		return data2;

	}

	public abstract void writeData(DataOutputStream dos) throws IOException;

	public abstract void writeObjects(ObjectOutputStream oos)
			throws IOException;

	

}
