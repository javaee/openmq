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

import javax.jms.*;

import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.JMSService.SessionAckMode;

/**
 *  DirectQueueSession ensures correct JMS semantics for JMS APIs that are valid
 *  at javax.jms.Session but invalid at javax.jms.QueueSession
 */
public class DirectQueueSession
extends DirectSession {
    
    /** Creates a new instance of DirectQueueSession */
    public DirectQueueSession(DirectConnection dc,
            JMSService jmsservice, long sessionId, SessionAckMode ackMode)
    throws JMSException {
        super(dc, jmsservice, sessionId, ackMode);
    }

    public TopicSubscriber createDurableSubscriber(Topic topic,
            String name)
    throws JMSException {
        String methodName =
                "createDurableSubscriber(Topic, name)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
    }

    public TopicSubscriber createDurableSubscriber(Topic topic,
            String name, String selector, boolean noLocal)
    throws JMSException {
        String methodName =
                "createDurableSubscriber(Topic, name, selector, noLocal)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
    }

    @Override
	public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        String methodName =
                "createDurableConsumer(Topic topic, String name)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        String methodName =
                "createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        String methodName =
                "createSharedConsumer(Topic topic, String sharedSubscriptionName)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException {
        String methodName =
                "createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        String methodName =
                "createSharedDurableConsumer(Topic topic, String name)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	@Override
	public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) throws JMSException {
        String methodName =
                "createSharedDurableConsumer(Topic topic, String name, String messageSelector)";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
	}

	/**
     *  Create a TemporaryTopic identity object
     */
    public javax.jms.TemporaryTopic createTemporaryTopic()
    throws JMSException {
        String methodName = "createTemporaryTopic()";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
    }

    /**
     *  Create a Topic identity object with the specified topic name
     *
     *  @param topicName The name of the Topic Destination
     *
     *  @throws InvalidDestinationException If the topicName contains illegal
     *          syntax.
     */
    public Topic createTopic(String topicName)
    throws JMSException {
        String methodName = "createTopic()";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
    }

    /**
     *  Unsubscribe the durable subscription specified by name
     */
    public void unsubscribe(String name)
    throws JMSException {
        String methodName = "unsubscribe()";
        String isIllegalMsg = _lgrMID_EXC + methodName +
                    ":Invalid for a QueueSession:sessionId=" + sessionId;
        _loggerJS.warning(isIllegalMsg);
        throw new javax.jms.IllegalStateException(isIllegalMsg);
    }

}
