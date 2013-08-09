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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.log.Logger;

public class TransactionWork {
	List<TransactionWorkMessage> sentMessages;
	List<TransactionWorkMessageAck> messageAcknowledgments;

	public TransactionWork() {
		sentMessages = new ArrayList<TransactionWorkMessage>();
	}

	public void addMessage(TransactionWorkMessage msg) {
		if (sentMessages == null)
			sentMessages = new ArrayList<TransactionWorkMessage>();
		sentMessages.add(msg);
	}

	public void addMessageAcknowledgement(TransactionWorkMessageAck ack) {
		if (messageAcknowledgments == null)
			messageAcknowledgments = new ArrayList<TransactionWorkMessageAck>();
		messageAcknowledgments.add(ack);
	}

	public List<TransactionWorkMessage> getSentMessages() {
		return sentMessages;
	}

	public void setMessages(List<TransactionWorkMessage> sentMessages) {
		this.sentMessages = sentMessages;
	}

	public List<TransactionWorkMessageAck> getMessageAcknowledgments() {
		return messageAcknowledgments;
	}

	public void setMessageAcknowledgments(
			List<TransactionWorkMessageAck> messageAcknowledgments) {
		this.messageAcknowledgments = messageAcknowledgments;
	}

	public int numSentMessages() {
		if (sentMessages == null)
			return 0;
		return sentMessages.size();
	}

	public int numMessageAcknowledgments() {
		if (messageAcknowledgments == null)
			return 0;
		return messageAcknowledgments.size();
	}

	public void readWork(DataInputStream dis) throws IOException,
			BrokerException {
		
		
		// read sent messages
		int numSentMessages = dis.readInt();
		if (Store.getDEBUG()) {
			Globals.getLogger().log(Logger.DEBUG,
					getPrefix() + "readWork numSentMessages="+ numSentMessages);
		}
		List<TransactionWorkMessage> sentMessages = new ArrayList<TransactionWorkMessage>(
				numSentMessages);
		for (int i = 0; i < numSentMessages; i++) {
			// Reconstruct the message
			
			TransactionWorkMessage workMessage = new TransactionWorkMessage();
			workMessage.readWork(dis);					
			sentMessages.add(workMessage);
		}
		this.setMessages(sentMessages);

		// read message acknowledgements
		int numConsumedMessages = dis.readInt();
		List<TransactionWorkMessageAck> consumedMessages = new ArrayList<TransactionWorkMessageAck>(
				numConsumedMessages);
		for (int i = 0; i < numConsumedMessages; i++) {
			TransactionWorkMessageAck messageAck = new TransactionWorkMessageAck();
			messageAck.readWork(dis);			
			consumedMessages.add(messageAck);
		}
		this.setMessageAcknowledgments(consumedMessages);
	}

	public void writeWork(DataOutputStream dos) throws IOException {
		// Msgs produce section
		dos.writeInt(numSentMessages());
		// Number of msgs (4 bytes)
		if (Store.getDEBUG()) {
			String msg = getPrefix() + " writeWork numSentMessages="
					+ numSentMessages() + " numMessageAcknowledgments="
					+ numMessageAcknowledgments();
			Globals.getLogger().log(Logger.DEBUG, msg);
		}

		if (numSentMessages() > 0) {
			Iterator<TransactionWorkMessage> itr = getSentMessages().iterator();
			while (itr.hasNext()) {
				TransactionWorkMessage workMessage = itr.next();
				workMessage.writeWork(dos);
			}
		}

		// Msgs consume section
		dos.writeInt(numMessageAcknowledgments());
		// Number of acks (4 bytes)
		if (numMessageAcknowledgments() > 0) {
			Iterator<TransactionWorkMessageAck> ackItr = getMessageAcknowledgments()
					.iterator();
			while (ackItr.hasNext()) {
				TransactionWorkMessageAck messageAck = ackItr.next();
				messageAck.writeWork(dos);
			}
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(" num messages ").append(numSentMessages());
		s.append(" num acks ").append(numMessageAcknowledgments());
		String result = super.toString() + new String(s);
		return result;
	}
	
	String getPrefix() {
		return "TransactionWork: " + Thread.currentThread().getName();
	}

}
