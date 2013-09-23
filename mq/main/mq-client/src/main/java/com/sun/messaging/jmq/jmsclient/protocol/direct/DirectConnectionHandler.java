/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsclient.protocol.direct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.ReadOnlyPacket;
import com.sun.messaging.jmq.io.ReadWritePacket;
import com.sun.messaging.jmq.io.PacketDispatcher;
import com.sun.messaging.jmq.jmsclient.ConnectionHandler;
import com.sun.messaging.jmq.jmsclient.ConnectionImpl;
import com.sun.messaging.jmq.jmsclient.MQAddress;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;
import com.sun.messaging.jmq.jmsclient.runtime.ClientRuntime;
import com.sun.messaging.jmq.jmsclient.runtime.impl.BrokerInstanceImpl;
import com.sun.messaging.jmq.jmsclient.runtime.impl.ClientRuntimeImpl;
import com.sun.messaging.jmq.jmsclient.runtime.impl.DirectBrokerInstance;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQDualThreadConnection;
import com.sun.messaging.jmq.jmsservice.DirectBrokerConnection;
import com.sun.messaging.jmq.jmsservice.HandOffQueue;

public class DirectConnectionHandler implements ConnectionHandler {
	
	private HandOffQueue inBoundQ = null;
	private HandOffQueue outBoundQ = null;
	private DirectBrokerConnection directConnection = null;
	
	//private ConnectionImpl connection = null;
	
	private volatile boolean isClosed = false;
	
	private static boolean directDebug = Boolean.getBoolean("imq.direct.debug");
	
	public boolean isDirectMode(){
		return true;
	}
		
	public DirectConnectionHandler (Object connection) throws JMSException {
		
		//this.connection = (ConnectionImpl) connection;
		this.init();
		
		if (directDebug) {
			ConnectionImpl.getConnectionLogger().info("Direct connection handler created...");
		}
	}
	
	public DirectConnectionHandler (MQAddress addr, ConnectionImpl conn) throws JMSException {
		//this.connection = conn;
		this.init();
		
		if (directDebug) {
			ConnectionImpl.getConnectionLogger().info("Direct connection handler created...");
		}
	}
	
	private void init() throws JMSException {
		
		try {
			
			boolean isdirect = ClientRuntime.getRuntime().isEmbeddedBrokerRunning();
			
			if (isdirect) {
				
				//get direct broker instance
				ClientRuntimeImpl runtime = (ClientRuntimeImpl) ClientRuntime.getRuntime();
				//get direct connection
				this.directConnection = runtime.createDirectConnection();
					
				//get inbound q
				this.inBoundQ = this.directConnection.getBrokerToClientQueue();
				//get outbound q
				this.outBoundQ = this.directConnection.getClientToBrokerQueue();
								
			} else {
				//Direct mode must be initialized
				throw new RuntimeException ("Direct broker not initialized for this client runtime.");
			}
			
		} catch (Exception e) {
			
			//ConnectionImpl.getConnectionLogger().log (Level.WARNING, e.getMessage(), e);
			
			e.printStackTrace();
			
			JMSException jmse = new JMSException (e.getMessage());
			jmse.setLinkedException(e);
			
			throw jmse;
		}
	}
	
	/**
	 * This method is used only if "dual-thread" mode is being used and "sync replies" have been enabled
	 * 
	 * Configure the IMQDualThreadConnection to use the specified ReplyDispatcher to process reply packets
	 * 
	 * @param rd The ReplyDispatcher to be configured
	 */
	public void setReplyDispatcher(PacketDispatcher rd){
		((IMQDualThreadConnection)directConnection).setReplyDispatcher(rd);
	}
	
	public void writePacket (ReadWritePacket pkt) throws IOException {
		
		try {
			
			if (isClosed) {
				throw new IOException ("Connection is closed.");
			}
			
			pkt.updateSequenceNumber();

			pkt.updateTimestamp();

			pkt.updateBuffers();
			
			ReadWritePacket newPkt = (ReadWritePacket) pkt.clone();
			this.outBoundQ.put(newPkt);
			
			if (directDebug) {
				System.out.println("Direct connection wrote pkt..." + newPkt);
				//pkt.dump(System.out);
				System.out.flush();
			}
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			IOException ioe = new IOException(e.getMessage());
			throw ioe;
		}
		
		//XXX write to inbound for short-circuit client only testing
		//this.inBoundQ.put(newPkt);
	}
	
	public ReadWritePacket readPacket () throws IOException {
		
		ReadWritePacket pkt = null;
		
		//ConnectionImpl.getConnectionLogger().info("Direct connection reading pkt ...");
		
		try {
			
			if (isClosed == false) {
				pkt = (ReadWritePacket) this.inBoundQ.take();
			}
			
			if (directDebug) {
				System.out.println("Direct connection read pkt..." + pkt);
				//pkt.dump(System.out);
				System.out.flush();
			}
			
			if (isClosed) {
				throw new IOException ("Connection is closed.");
			}
			
		} catch (InterruptedException inte) {
			;
		}
		
		return pkt;
	}
	
	public synchronized void close() throws IOException {
		
		if (isClosed) {
			return;
		}
		
		// TODO Auto-generated method stub
		ReadWritePacket pkt = new ReadWritePacket();
		
		pkt.setPacketType(PacketType.NONE);
		
		this.isClosed = true;
		
		//wake up read channel
		try {
			this.inBoundQ.put(pkt);
		} catch (Exception e) {
			IOException ioe = new IOException (e.getMessage());
			throw ioe;
		}
	}

	public String getBrokerAddress() {
		// TODO Auto-generated method stub
		return "localhost";
	}

	public String getBrokerHostName() {
		// TODO Auto-generated method stub
		return "localhost";
	}

	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLocalPort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Packet fetchReply(){
		return ((IMQDualThreadConnection)directConnection).fetchReply();
	}
	
	public void configure(Properties configuration) throws IOException {
        	        	
	}

}
