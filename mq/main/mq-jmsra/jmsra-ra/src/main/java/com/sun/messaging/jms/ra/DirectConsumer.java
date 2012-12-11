/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Logger;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.jmsclient.ExceptionHandler;
import com.sun.messaging.jmq.jmsclient.MQMessageConsumer;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;
import com.sun.messaging.jmq.jmsservice.JMSAck;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.JMSServiceReply;
import com.sun.messaging.jmq.jmsservice.JMSServiceException;
import com.sun.messaging.jmq.io.JMSPacket;
//import com.sun.messaging.jmq.jmsservice.JMSServiceReply.Status;

/**
 *  DirectConsumer encapsulates JMS MessageConsumer behavior for MQ DIRECT mode
 *  operation.
 */
public class DirectConsumer
        implements MQMessageConsumer,
        javax.jms.QueueReceiver, javax.jms.TopicSubscriber,
        com.sun.messaging.jmq.jmsservice.Consumer
    {
    
    /**
     *  The JMSService for this DirectConsumer
     */
    private JMSService jmsservice;

    /**
     *  The parent DirectSession that created this DirectConsumer
     */
    private DirectSession ds;

    /**
     *  The connectionId of the parent DirectConnection
     */
    private long connectionId;

    /**
     *  The sessionId of the parent DirectSession
     */
    private long sessionId;

    /**
     *  The consumerId for this DirectConsumer
     */
    private long consumerId = 0L;

    /**
     *  The JMS Destination that is associated with this DirectConsumer
     */
    private Destination destination;

    /**
     *  The JMSService Destination that is associated with this DirectConsumer
     */
    private com.sun.messaging.jmq.jmsservice.Destination jmsservice_destination;

    /**
     *  The JMS Message Selector that was used for this DirectConsumer
     */
    private String msgSelector;

    /**
     *  The Durable name associated with this DirectConsumer
     *  (if it is a Durable Consumer)
     */
    private String durableName;

    /**
     *  The clientId associated with this DirectConsumer (for durables only)
     */
    private String clientId;

    /**
     *  The JMS MessageListener associated with this DirectConsumer
     */
    private javax.jms.MessageListener msgListener;

    /**
     *
     */
    private boolean noLocal;
    /**
     *  Holds the closed state of this DirectConsumer
     */
    private boolean isClosed;
    private boolean isClosing;

    /**
     *  Logging
     */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.DirectConsumer";
    private static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    private static transient final String _lgrNameJMSConsumer =
            "javax.jms.MessageConsumer.mqjmsra";
    private static transient final Logger _loggerOC = 
            Logger.getLogger(_lgrNameOutboundConnection);
    private static transient final Logger _loggerJMC =
            Logger.getLogger(_lgrNameJMSConsumer);
    private static transient final String _lgrMIDPrefix = "MQJMSRA_DP";
    private static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    private static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    private static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    private static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    private static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";

    /** Creates a new instance of DirectConsumer */
    public DirectConsumer(DirectSession ds, JMSService jmsservice,
            Destination destination,
            com.sun.messaging.jmq.jmsservice.Destination jmsservice_dest,
            boolean noLocal, String msgSelector, String durableName) {
        Object params[] = new Object[7];
        params[0] = ds;
        params[1] = jmsservice;
        params[2] = destination;
        params[3] = jmsservice_dest;
        params[4] = noLocal;
        params[5] = msgSelector;
        params[6] = durableName;
        _loggerOC.entering(_className, "constructor()", params);        
        this.ds = ds;
        this.jmsservice = jmsservice;
        this.destination = destination;
        this.jmsservice_destination = jmsservice_dest;
        this.connectionId = ds.getConnectionId();
        this.sessionId = ds.getSessionId();
        this.msgSelector = msgSelector;
        this.durableName = durableName;
        this.clientId = ds.getConnection()._getClientID();
        this.noLocal = noLocal;
    }

    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.MessageConsumer
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Close this MessageConsumer
     */
    public synchronized void close()
    throws JMSException {
        _loggerJMC.fine(_lgrMID_INF+"consumerId="+consumerId+":"+"close()");
        //harmless if already closed
        if (isClosed){
            return;
        } else {
            ds.removeConsumer(this);
            if (this.durableName != null) {
                this.ds.dc.removeDurableConsumer(this);
            }
            if (destination != null &&
                    (destination instanceof TemporaryDestination)) {
                this.ds.dc._decrementTemporaryDestinationUsage(
                        (TemporaryDestination)destination);
            }
            this._close();
        }
    }

    /**
     *  Return the JMS MessageListener set on this MessageConsumer
     */
    public javax.jms.MessageListener getMessageListener()
    throws JMSException{
        this._checkIfClosed("getMessageListener()");
        return this.msgListener;
    }

    /**
     *  Return the JMS Message Selector set on this MessageConsumer
     */
    public String getMessageSelector()
    throws JMSException{
        this._checkIfClosed("getMessageSelector()");
        return this.msgSelector;
    }

    /**
     *  Return the next JMS Message that is produced for this MessageConsumer
     *  blocking until that message arrives
     */
    public Message receive()
    throws JMSException{
        String methodName = "receive()";
        this._checkIfClosed(methodName);
        return this.ds._fetchMessage(this.consumerId, 0L, methodName);
    }

    /**
     *  Return the next JMS Message that was produced for this MessageConsumer
     *  that arrives within a specified timeout interval.
     */
    public Message receive(long timeout)
    throws JMSException{
        String methodName = "receive(timeout)";
        this._checkIfClosed(methodName);
        return this.ds._fetchMessage(this.consumerId, timeout, methodName);
    }

    /**
     *  Return the next JMS Message that was produced for this MessageConsumer
     *  if one is immediately available, without waiting.
     */
    public Message receiveNoWait()
    throws JMSException{
        String methodName = "receiveNoWait()";
        this._checkIfClosed(methodName);
        return this.ds._fetchMessage(this.consumerId, -1L, methodName);
    }

	@Override
	public <T> T receiveBody(Class<T> c) throws JMSException {
		return receiveBody(c,0);
	}

	@Override
	public <T> T receiveBody(Class<T> c, long timeout) throws JMSException {
		Message message = receive(timeout);
		if (message==null){
			return null;
		} else {
			return returnPayload(message,c);
		}
	}
	
	@Override
	public <T> T receiveBodyNoWait(Class<T> c) throws JMSException {
		Message message = receiveNoWait();
		if (message==null){
			return null;
		} else {
			return returnPayload(message,c);
		}
	}
	
	private <T> T returnPayload(Message message, Class<T> c) throws JMSException {
		T body = message.getBody(c);
		if (body==null){
			// must be a Message
			// this doesn't have a payload, and we can't return null because this would clash with the "no message received" case,
			// so we throw an exception
			// "Message has no body and so cannot be returned using this method" 
			String errorString = AdministeredObject.cr.getKString(ClientResources.X_MESSAGE_HAS_NO_BODY);
			JMSException jmse = new javax.jms.MessageFormatException(errorString, ClientResources.X_MESSAGE_HAS_NO_BODY);
			ExceptionHandler.throwJMSException(jmse);
		}
		return body;
	}

    /**
     *  Set a JMS MessageListener on this MessageConsumer
     */
    public void setMessageListener(javax.jms.MessageListener msgListener)
    throws JMSException{
        String methodName = "setMessageListener()";
        JMSServiceReply jmsReply = null;
        _loggerJMC.fine(_lgrMID_INF+
                "connectionId="+connectionId+":"+methodName +
                "=" + msgListener);
        this._checkIfClosed(methodName);
        if (msgListener == null){
            //The session is now in sync mode
            this.msgListener = null;
            this.ds._setAsync(false);
        } else {
            //The session is now in async mode
            this.msgListener = msgListener;
            this.ds._setAsync(true);
        }
        //Set this consumer async with the jmsservice
        try {
            jmsReply = this.jmsservice.setConsumerAsync(this.connectionId,
                    this.sessionId, this.consumerId,
                    (msgListener == null ? null : this));
        } catch (JMSServiceException jse) {
            JMSServiceReply.Status status = jse.getJMSServiceReply().getStatus();
            String failure_cause;
            switch (status) {
                case NOT_FOUND:
                    failure_cause = "consumer not found.";
                    break;
                default:
                    failure_cause = "unkown JMSService server error:" +
                            jse.getMessage();
            }
            String exerrmsg = 
                    "setMessageListener on JMSService:" +
                    jmsservice.getJMSServiceID() +
                    " failed for connectionId:"+ connectionId +
                    ", sessionId:" + sessionId +
                    ", consumerId:" + consumerId +
                    " due to " + failure_cause;
            _loggerJMC.severe(exerrmsg);
            JMSException jmsse = new JMSException(exerrmsg);
            jmsse.initCause(jse);
            throw jmsse;
        }
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.MessageConsumer
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.QueueReceiver
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Return the JMS Queue Associated with this QueueReceiver
     */
    public Queue getQueue()
    throws JMSException{
        this._checkIfClosed("getQueue()");
        if (destination instanceof javax.jms.Queue) {
            return (Queue)this.destination;
        } else {
            String excMsg = _lgrMID_EXC + "getQueue():" +
                    "Invalid to Topic destination=" + this.destination;
            _loggerJMC.warning(excMsg);
            throw new JMSException(excMsg);
        }
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.QueueReceiver
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.TopicSubscriber
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Return the JMS Topic Associated with this TopicSubscriber
     */
    public Topic getTopic()
    throws JMSException{
        this._checkIfClosed("getTopic()");
        if (destination instanceof javax.jms.Topic) {
            return (Topic)this.destination;
        } else {
            String excMsg = _lgrMID_EXC + "getTopic():" +
                    "Invalid to Queue destination=" + this.destination;
            _loggerJMC.warning(excMsg);
            throw new JMSException(excMsg);
        }
    }
    /**
     *  Return the NoLocal attribute for this TopicSubscriber
     */
    public boolean getNoLocal()
    throws JMSException{
        this._checkIfClosed("getNoLocal()");
        if (destination instanceof javax.jms.Queue) {
            String excMsg = _lgrMID_EXC + "getNoLocal():" +
                    "Invalid on Queue destination=" + this.destination;
            _loggerJMC.warning(excMsg);
            throw new JMSException(excMsg);
        }
        return this.noLocal;
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.TopicSubscriber
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  methods implementing com.sun.messaging.jmq.jmsservice.Consumer
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Deliver a JMSPacket to the JMS MessageListener endpoint
     */
    public JMSAck deliver(JMSPacket jmsPacket){
        //Delivery must be serialized at the session level
        return this.ds._deliverMessage(this.msgListener, jmsPacket,
                this.consumerId);
    }
    /////////////////////////////////////////////////////////////////////////
    //  end com.sun.messaging.jmq.jmsservice.Consumer
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  MQ methods
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Set the consumerId after acquiring it from the JMSService
     */
    protected synchronized void _setConsumerId(long consumerId){
        if (this.consumerId != 0L) {
            String wrnMsg = _lgrMID_WRN + "_setConsumerId():" +
                    "Attempt to reset Id of:"+this.consumerId+ ":to:"+
                    consumerId +":Ignoring.";
            _loggerJMC.warning(wrnMsg);
        } else {
            this.consumerId = consumerId;
        }
    }

    /**
     *  Return the connectionId for this DirectConsumer
     *
     *  @return The connectionId
     */
    public long getConnectionId() {
        return this.connectionId;
    }

    /**
     *  Return the sessionId for this DirectConsumer
     *
     *  @return The sessionId
     */
    public long getSessionId() {
        return this.sessionId;
    }

    /**
     *  Return the consumerId for this DirectConsumer
     *
     *  @return The consumerId
     */
    public long getConsumerId() {
        return this.consumerId;
    }

    /**
     *  Return the durable name for this DirectConsumer
     *
     *  @return The durable name
     */
    public String getDurableName(){
        return this.durableName;
    }

    /**
     *  Return the closed state of this DirectConsumer
     *
     *  @return {@code true} if this consumer has been closed;
     *          {@code false} otherwise
     */
    public synchronized boolean isClosed() {
        return this.isClosed;
    }

    /**
     *  Check if the DirectConsumer is closed prior to performing an
     *  operation and throw a JMSException if it is closed.
     *
     *  @param methodname The name of the method from which this check is called
     *
     *  @throws JMSException if it is closed
     */
    private void _checkIfClosed(String methodname)
    throws JMSException {
        if (isClosed()) {
            String closedmsg = _lgrMID_EXC + methodname +
                    "MessageConsumer is closed:Id=" + consumerId;
            _loggerJMC.warning(closedmsg);
            throw new javax.jms.IllegalStateException(closedmsg);
        }
    }

    /**
     *  Throw a JMSException with the appropriate message for unsupported
     *  operations.
     *
     *  @param  methodname The method name for which this unsupported
     *          exception is to be thrown.
     */
    private void _unsupported(String methodname)
    throws JMSException {
        String unsupported = _lgrMID_WRN+
                    "consumerId="+consumerId+":"+methodname;
        _loggerJMC.warning(unsupported);
        throw new JMSException(unsupported);
    }

    /**
     *  Close consumer for use when used by session.clos()
     */
    protected synchronized void _close()
    throws JMSException {
        //harmless if already closed
        if (this.isClosed){
            return;
        } else {
            this.isClosing = true;
            //Anything ?
        }
        try {
            //XXX:tharakan:only unsubscribe passes in the durableName
            //pass null here
            //System.out.println("DC:Destroying cnsumerId="+consumerId+":connectionId="+connectionId);
            jmsservice.deleteConsumer(connectionId, sessionId, consumerId, null, clientId);
        } catch (JMSServiceException jmsse){
            _loggerJMC.warning(_lgrMID_WRN+
                    "consumerId="+consumerId+":"+"close():"+
                    "JMSService.deleteConsumer():"+
                    "JMSServiceException="+
                    jmsse.getMessage());
        }
        this.isClosed = true;
        this.isClosing = false;
    }
    /////////////////////////////////////////////////////////////////////////
    //  end MQ methods
    /////////////////////////////////////////////////////////////////////////
}
