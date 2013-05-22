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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;
import com.sun.messaging.jmq.util.log.Logger;

public class CheckpointManager implements Runnable {
	
	TransactionLogManager transactionLogManager;
	private BlockingQueue<Checkpoint> checkpointQueue = new ArrayBlockingQueue<Checkpoint>(
			5);
	int numCheckpoints;
	private Thread runner;
	public static final Logger logger = Globals.getLogger();

	
	CheckpointManager(TransactionLogManager transactionLogManager)
	{
		this.transactionLogManager=transactionLogManager;
	}
	
	String getPrefix() {
		return "CheckpointManager: " + Thread.currentThread().getName();
	}
	

	public void run() {
		while (true) {
			try {
				Checkpoint checkpoint = (Checkpoint) checkpointQueue.take();
				transactionLogManager.doCheckpoint();
			} catch (Throwable e) {
				logger.logStack(Logger.ERROR,
						"exception when doing checkpoint", e);

			}
		}
	}
	
	public synchronized void enqueueCheckpoint() {

		if (runner == null) {
			if (Store.getDEBUG()) {
				String msg = getPrefix() + " starting checkpoint runner";
				logger.log(Logger.DEBUG, msg);
			}

			runner = new Thread(this, "Checkpoint runner");
			runner.setDaemon(true);
			runner.start();
		}

		Checkpoint checkpoint = new Checkpoint();
		int queueSize = checkpointQueue.size();
		if (queueSize > 0) {
			logger.log(Logger.ERROR, "enqueued checkpoint request "
					+ numCheckpoints + " when there are still " + queueSize
					+ " request(s) in process");

		}

		try {
			checkpointQueue.put(checkpoint);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		numCheckpoints++;
	}
	
	public class Checkpoint {
	}

}
