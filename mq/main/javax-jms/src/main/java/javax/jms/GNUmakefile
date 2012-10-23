#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2003-2012 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

PACKAGE		= javax.jms
TOPDIR          = ../../../../..
MAKEDEFS 	= $(TOPDIR)/src/buildcfg/Defs.gmk

FILES.java = \
		BytesMessage.java \
		Connection.java \
		ConnectionConsumer.java \
		ConnectionFactory.java \
		ConnectionMetaData.java \
		DeliveryMode.java \
		Destination.java \
		ExceptionListener.java \
		IllegalStateException.java \
		InvalidClientIDException.java \
		InvalidDestinationException.java \
		InvalidSelectorException.java \
		JMSException.java \
		JMSSecurityException.java \
		MapMessage.java \
		Message.java \
		MessageConsumer.java \
		MessageEOFException.java \
		MessageFormatException.java \
		MessageListener.java \
		MessageNotReadableException.java \
		MessageNotWriteableException.java \
		MessageProducer.java \
		ObjectMessage.java \
		Queue.java \
		QueueBrowser.java \
		QueueConnection.java \
		QueueConnectionFactory.java \
		QueueReceiver.java \
		QueueRequestor.java \
		QueueSender.java \
		QueueSession.java \
		ResourceAllocationException.java \
		ServerSession.java \
		ServerSessionPool.java \
		Session.java \
		StreamMessage.java \
		TemporaryQueue.java \
		TemporaryTopic.java \
		TextMessage.java \
		Topic.java \
		TopicConnection.java \
		TopicConnectionFactory.java \
		TopicPublisher.java \
		TopicRequestor.java \
		TopicSession.java \
		TopicSubscriber.java \
		TransactionInProgressException.java \
		TransactionRolledBackException.java \
		XAConnection.java \
		XAConnectionFactory.java \
		XAQueueConnection.java \
		XAQueueConnectionFactory.java \
		XAQueueSession.java \
		XASession.java \
		XATopicConnection.java \
		XATopicConnectionFactory.java \
		XATopicSession.java \
		CompletionListener.java \
		IllegalStateRuntimeException.java \
		InvalidClientIDRuntimeException.java \
		InvalidDestinationRuntimeException.java \
		InvalidSelectorRuntimeException.java \
		JMSRuntimeException.java \
		JMSSecurityRuntimeException.java \
		MessageFormatRuntimeException.java \
		TransactionRolledBackRuntimeException.java \
		JMSConnectionfactory.java \
		JMSConsumer.java \
		JMSContext.java \
		JMSPasswordCredential.java \
		JMSRuntimeException.java \
		JMSSessionMode.java

include $(MAKEDEFS)

all:		classes.dir classes $(SUBDIRS)
clean: 		classes.clean $(SUBDIRS)
debug: 		classes.dir classes.debug $(SUBDIRS)
optimized: 	classes.optimized $(SUBDIRS)

