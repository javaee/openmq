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

package com.sun.messaging.jms.ra;

import java.util.logging.Logger;

import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;

import com.sun.messaging.jmq.jmsservice.JMSService;

public class DirectQueueConnection extends DirectConnection {

	/**
	 * Logging
	 */
	private static transient final String _className = "com.sun.messaging.jms.ra.DirectQueueConnection";
	private static transient final String _lgrNameOutboundConnection = "javax.resourceadapter.mqjmsra.outbound.connection";
	private static transient final String _lgrNameJMSConnection = "javax.jms.Connection.mqjmsra";
	private static transient final Logger _loggerOC = Logger.getLogger(_lgrNameOutboundConnection);
	private static transient final Logger _loggerJC = Logger.getLogger(_lgrNameJMSConnection);
	private static transient final String _lgrMIDPrefix = "MQJMSRA_DC";
	private static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
	private static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
	private static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
	private static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
	private static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";

	public DirectQueueConnection(DirectConnectionFactory cf, JMSService jmsservice, long connectionId, boolean inACC) {
		super(cf, jmsservice, connectionId, inACC);
	}

	@Override
	public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException {

		// JMS spec and CTS tests require a IllegalStateException to be thrown
		String methodName = "createSharedConnectionConsumer(Topic topic, String subscriptionName,String messageSelector, ServerSessionPool sessionPool, int maxMessages)";
		String isIllegalMsg = _lgrMID_EXC + methodName + ":Invalid for a QueueConnection";
		_loggerJC.warning(isIllegalMsg);
		throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		// JMS spec and CTS tests require a IllegalStateException to be thrown
		String methodName = "createSharedDurableConnectionConsumer(Topic topic, String subscriptionName,String messageSelector, ServerSessionPool sessionPool, int maxMessages)";
		String isIllegalMsg = _lgrMID_EXC + methodName + ":Invalid for a QueueConnection";
		_loggerJC.warning(isIllegalMsg);
		throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException {
		// JMS spec and CTS tests require a IllegalStateException to be thrown
		String methodName = "createConnectionConsumer(Queue queue,String messageSelector,ServerSessionPool sessionPool, int maxMessages)";
		String isIllegalMsg = _lgrMID_EXC + methodName + ":Invalid for a QueueConnection";
		_loggerJC.warning(isIllegalMsg);
		throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

}
