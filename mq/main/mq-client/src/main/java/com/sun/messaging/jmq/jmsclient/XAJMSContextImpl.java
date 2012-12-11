package com.sun.messaging.jmq.jmsclient;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.XAJMSContext;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;

public class XAJMSContextImpl extends JMSContextImpl implements XAJMSContext {

	public XAJMSContextImpl(ConnectionFactory connectionFactory, ContainerType containerType, String userName,
			String password) {
		super(connectionFactory, containerType, userName, password);
	}
	
	public XAJMSContextImpl(ConnectionFactory connectionFactory, ContainerType containerType) {
		super(connectionFactory, containerType);
	}

	@Override
	public JMSContext getContext() {
		return this;
	}

	@Override
	public XAResource getXAResource() {
		return ((XASession)session).getXAResource();
	}

	@Override
	public boolean getTransacted() {
		// the API states that this should always return true
		// but the underlying XASession should be able to handle this
		return super.getTransacted();
	}

	@Override
	public void commit() {
		// the API states that this should always return a TransactionInProgressRuntimeException
		// but the underlying XASession should be able to handle this
		super.commit();
	}

	@Override
	public void rollback() {
		// the API states that this should always return a TransactionInProgressRuntimeException
		// but the underlying XASession should be able to handle this
		super.rollback();
	}	
	
	
}
