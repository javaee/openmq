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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.DestinationList;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.data.BaseTransaction;
import com.sun.messaging.jmq.jmsserver.data.ClusterTransaction;
import com.sun.messaging.jmq.jmsserver.data.RemoteTransaction;
import com.sun.messaging.jmq.jmsserver.data.TransactionAcknowledgement;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionWork;
import com.sun.messaging.jmq.jmsserver.data.TransactionWorkMessage;
import com.sun.messaging.jmq.jmsserver.data.TransactionWorkMessageAck;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.log.Logger;

public class FromTxnLogConverter {

	Store store;
	FileStore fileStore;
	Logger logger = Globals.getLogger();
	private static boolean DEBUG = false;
	static {
		if (Globals.getLogger().getLevel() <= Logger.DEBUG)
			DEBUG = true;
	}

	public FromTxnLogConverter(Store store) {
		this.store = store;
	}

	public void convertFromTxnLogFormat() {

		// iterate through all transactions

		if (DEBUG) {
			logger.log(Logger.DEBUG, getPrefix() + " convertFromTxnLogFormat");
		}

		fileStore = (FileStore) store;
		TransactionLogManager txnLogManager = fileStore.getTxnLogManager();
		convert(txnLogManager.getLocalTransactionManager(),
				new LocalTxnConverter());
		convert(txnLogManager.getClusterTransactionManager(),
				new ClusterTxnConverter());
		convert(txnLogManager.getRemoteTransactionManager(),
				new RemoteTxnConverter());

	}

	void convert(BaseTransactionManager baseTxnManager, TxnConverter converter) {
		if (DEBUG) {
			logger.log(Logger.DEBUG, getPrefix() + " converting trxns from TxnManager");
		}
		List<BaseTransaction> list = baseTxnManager
				.getAllIncompleteTransactions();
		Iterator<BaseTransaction> iter = list.iterator();
		while (iter.hasNext()) {
			BaseTransaction baseTxn = iter.next();

			try {
				converter.convert(baseTxn);
			} catch (BrokerException be) {
				logger.logStack(Logger.ERROR, be.getMessage(), be);
			} catch (IOException ie) {
				logger.logStack(Logger.ERROR, ie.getMessage(), ie);
			}
		}
	}

	class TxnConverter {

		void convert(BaseTransaction baseTxn) throws BrokerException,
				IOException {
			if (DEBUG) {
				logger.log(Logger.DEBUG, getPrefix() + " convert txn "+baseTxn);
			}

			TransactionUID id = baseTxn.getTid();

			TransactionState ts = baseTxn.getTransactionState();

			int finalState = ts.getState();

			ts.setState(TransactionState.STARTED);

			// TransactionState
			fileStore.storeTransaction(id, ts, true);

			TransactionWork txnWork = baseTxn.getTransactionWork();
			if (txnWork != null)
				convertWork(txnWork, ts, id);

			ts.setState(TransactionState.PREPARED);
			fileStore.updateTransactionState(id, ts, true);

		}

		void convertWork(TransactionWork work, TransactionState ts,
				TransactionUID txid) throws BrokerException, IOException {
			if (DEBUG) {
				logger.log(Logger.DEBUG, getPrefix() + " convertWork ");
			}
                        DestinationList DL = Globals.getDestinationList();
			List<TransactionWorkMessage> sentMsgs = work.getSentMessages();
			Iterator<TransactionWorkMessage> sentIter = sentMsgs.iterator();
			while (sentIter.hasNext()) {
				TransactionWorkMessage msg = sentIter.next();
				if (DEBUG) {
					logger.log(Logger.DEBUG, getPrefix() + " convert sent msg "+msg);
				}
				DestinationUID duid = msg.getDestUID();
				int type = (duid.isQueue() ? DestType.DEST_TYPE_QUEUE
						: DestType.DEST_TYPE_TOPIC);

				// make sure destination exists
				// ( it may have been removed on load if it just contained
				// messages in a transaction)
				Destination[] ds = DL.getDestination(fileStore, duid.getName(),
						type, true, true);

                                Destination dest = ds[0];
				Packet message = msg.getMessage();
				ConsumerUID[] iids = msg.getStoredInterests();
				//if (iids != null) 
				if(false){
					int[] states = new int[iids.length];
					for (int i = 0; i < iids.length; i++) {
						states[i] = 0;
					}

					fileStore.storeMessage(duid, message, iids, states, true);
				} else {
					fileStore.storeMessage(duid, message, true);
				}
			}

			List<TransactionWorkMessageAck> consumedMsgs = work
					.getMessageAcknowledgments();
			Iterator<TransactionWorkMessageAck> consumedIter = consumedMsgs
					.iterator();
			while (consumedIter.hasNext()) {
				TransactionWorkMessageAck msgAck = consumedIter.next();
				if (DEBUG) {
					logger.log(Logger.DEBUG, getPrefix() + " convert consumed msg "+msgAck);
				}
				DestinationUID duid = msgAck.getDestUID();
				int type = (duid.isQueue() ? DestType.DEST_TYPE_QUEUE
						: DestType.DEST_TYPE_TOPIC);
				Destination[] ds = DL.getDestination(fileStore, duid.getName(),
						type, true, true);
                                Destination dest = ds[0];
				dest.load();
				SysMessageID mid = msgAck.getSysMessageID();
				ConsumerUID cid = msgAck.getConsumerID();
				boolean sync = true;
				boolean isLastAck = false;
				TransactionAcknowledgement txAck = new TransactionAcknowledgement(mid, cid, cid);
				 fileStore.storeTransactionAck(txid, txAck, false);
//				fileStore.updateInterestState(duid, mid, cid, Store.INTEREST_STATE_ACKNOWLEDGED,
//						sync, txid, isLastAck);
			}

		}
	}

	class LocalTxnConverter extends TxnConverter {

	}

	class ClusterTxnConverter extends TxnConverter {

		void convert(BaseTransaction baseTxn) throws BrokerException,
				IOException {

			ClusterTransaction clusterTxn = (ClusterTransaction) baseTxn;
			TransactionUID id = baseTxn.getTid();

			TransactionState ts = baseTxn.getTransactionState();

			int finalState = ts.getState();

			ts.setState(TransactionState.STARTED);

			// TransactionState
			fileStore.storeTransaction(id, ts, true);

			TransactionWork txnWork = baseTxn.getTransactionWork();
			if (txnWork != null)
				convertWork(txnWork, ts, id);

			((PartitionedStore)store).updateClusterTransaction(id, clusterTxn
					.getTransactionBrokers(), Destination.PERSIST_SYNC);

			ts.setState(TransactionState.PREPARED);
			fileStore.updateTransactionState(id, ts, true);

		}

	}

	class RemoteTxnConverter extends TxnConverter {
		void convert(BaseTransaction baseTxn) throws BrokerException,
				IOException {

			RemoteTransaction remoteTxn = (RemoteTransaction) baseTxn;
			TransactionUID id = baseTxn.getTid();

			TransactionState ts = baseTxn.getTransactionState();

			((PartitionedStore)store).storeRemoteTransaction(id, ts, remoteTxn.getTxnAcks(),
					remoteTxn.getTxnHomeBroker(), Destination.PERSIST_SYNC);
		}
	}

	private String getPrefix() {
		return Thread.currentThread() + " TransactionConverter.";
	}
}
