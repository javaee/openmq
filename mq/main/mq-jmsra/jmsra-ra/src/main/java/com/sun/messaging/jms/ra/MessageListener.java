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

import javax.jms.JMSException;

import javax.resource.*;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.*;

import javax.transaction.xa.XAResource;

import java.lang.reflect.Method;

import java.util.logging.Logger;

/**
 *  Implements the JMS MessageListener interface for the Sun MQ JMS RA
 *  and forwards the messages to the MessageEndpoint created by the
 *  MessageEndpointFactory.
 */

public class MessageListener
implements javax.jms.MessageListener
{
    /** The EndpointConsumer for this MessageListener instance */
    private EndpointConsumer epConsumer = null;

    /** The MessageEndpointFactory for this MessageListener instance */
    private MessageEndpointFactory epFactory = null;

    /** The ActivationSpec for this MessageListener instance */
    private com.sun.messaging.jms.ra.ActivationSpec spec = null;

    /** The MessageEndpoint for this MessageListener instance */
    private MessageEndpoint msgEndpoint = null;

    /** The XAResource for this MessageListener instance */
    private XAResource xar = null;

    /** The onMessage Method that this MessageListener instance will call */
    private Method onMessage = null;

    /** The onMessageRunnerPool that this MessageListener instance will use */
    private OnMessageRunnerPool omrPool = null;

    private boolean transactedDelivery = false;
    private boolean noAckDelivery = false;
    private boolean useRADirect = false;

    /* Loggers */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.ConnectionFactoryAdapter";
    protected static transient final String _lgrNameInboundMessage =
            "javax.resourceadapter.mqjmsra.inbound.message";
    protected static transient final Logger _loggerIM =
            Logger.getLogger(_lgrNameInboundMessage);
    protected static transient final String _lgrMIDPrefix = "MQJMSRA_ML";
    protected static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    protected static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    protected static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    protected static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    protected static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";

//    static {
//        _loggerIM = Logger.getLogger(_lgrNameInboundMessage);
//    }

    /** Constructors */
    public MessageListener(
        EndpointConsumer epConsumer,
        MessageEndpointFactory epFactory,
        ActivationSpec spec)
    {
        this(epConsumer, epFactory, spec, false, false);
    }

    public MessageListener(
        EndpointConsumer epConsumer,
        MessageEndpointFactory epFactory,
        ActivationSpec spec,
        boolean noAckDelivery)
    {
        this(epConsumer, epFactory, spec, noAckDelivery, false);
    }

    /** Constructor */
    public MessageListener(
        EndpointConsumer epConsumer,
        MessageEndpointFactory epFactory,
        ActivationSpec spec,
        boolean noAckDelivery,
        boolean useRADirect)
    {
        Object params[] = new Object[5];
        params[0] = epConsumer;
        params[1] = epFactory;
        params[2] = spec;
        params[3] = Boolean.valueOf(noAckDelivery);
        params[4] = Boolean.valueOf(useRADirect);

        _loggerIM.entering(_className, "constructor()", params);

        //System.out.println("MQRA:ML:Constructor()-omrp:min,max="+spec.getEndpointPoolSteadySize()+","+spec.getEndpointPoolMaxSize());
        this.epConsumer = epConsumer;
        this.epFactory = epFactory;
        this.spec = (com.sun.messaging.jms.ra.ActivationSpec)spec;
        this.noAckDelivery = noAckDelivery;
        this.useRADirect = useRADirect;


        //XXX: This really should be allowed here - but as8b47 blocks until activation is complete
        //omrPool = new OnMessageRunnerPool(epFactory, epConsumer, spec);

        onMessage = epConsumer.getResourceAdapter()._getOnMessageMethod();
        if (!useRADirect) {
            xar = epConsumer.getXASession().getXAResource();
        } else {
            xar = null;
        }
        try {
            transactedDelivery = epFactory.isDeliveryTransacted(onMessage);
        } catch (java.lang.NoSuchMethodException nsme) {
            //Ignore - assume delivery is non-transacted
        }
    }

    public void waitForAllOnMessageRunners() throws JMSException {
        if (omrPool != null) {
            omrPool.waitForAllOnMessageRunners();
        }
    }

    public void releaseOnMessageRunners() {
        if (omrPool != null) {
            omrPool.releaseOnMessageRunners();
        }
    }

    public void invalidateOnMessageRunners() {
        if (omrPool != null) {
            omrPool.invalidateOnMessageRunners();
        }
    }

    // JMS MessageListener interface methods //
    // 

    public void
    onMessage(javax.jms.Message message) {
    	if (!this.useRADirect) {
            if ( epConsumer.xas.isRemoteAckFailed() ) {
                    this.recreateConsumer();
                    return;
            }

            //System.out.println("MQRA:ML:onMessage():Msg="+message.toString());
            //Set message being consumed in RA
            ((com.sun.messaging.jmq.jmsclient.MessageImpl)message)._setConsumerInRA();

            if (spec._deliverySerial()) {
                _onMessage(message);
                return;
            }
        }

        //XXX:Remove once as8 allows createEndpoint in endpointActivation
        if (omrPool == null) {
            omrPool = new OnMessageRunnerPool(this.epFactory, this.epConsumer,
                    this.spec, this.useRADirect);
        }
        //XXX:remove till here

        OnMessageRunner omr;

        try {
            omr = omrPool.getOnMessageRunner();
            omr.onMessage(message);
        } catch (JMSException jmse) {
            System.err.println("MQRA:ML:onMessage:JMSException on getOnMessageRunner");
            jmse.printStackTrace();
        }
    }

    /** Upon receipt of a JMS 'onMessage'  method call,
     *  this method delivers the JMS Message to the MessageEndpoint
     *  that is associated with the EndpointConsumer that is
     *  associated with this MessageListener
     */
    public void
    _onMessage(javax.jms.Message message)
    {
        assert (this.useRADirect == false);
        //System.out.println("MQRA:ML:onMessage()");
        com.sun.messaging.jmq.jmsclient.MessageImpl mqmsg =
            (com.sun.messaging.jmq.jmsclient.MessageImpl)message;
        com.sun.messaging.jmq.jmsclient.SessionImpl mqsess =
            (com.sun.messaging.jmq.jmsclient.SessionImpl)epConsumer.getXASession();

        try {
            transactedDelivery = epFactory.isDeliveryTransacted(onMessage);
        } catch (java.lang.NoSuchMethodException nsme) {
            //Ignore - what should be assumed for the
            //value of transactedDelivery ???
        }

        //XXX:Message Logging?

        //Object[] msgArg = { message };

        msgEndpoint = null;
        //This is not configurable since ideally we'd want
        //createEndpoint to simply block or Unavailable mean
        //that it's never going to be available
        //otherwise we'll never resolve why we couldn't get
        //the endpoint
        for (int i = 1; i < 6; i++ ) {
            try {
                //If it's not deactivated
                if (epConsumer.deactivated != true) {
                    msgEndpoint = epFactory.createEndpoint(xar);
                    break;
                }
            } catch (UnavailableException ue) {
                try {
                    //System.err.println("MQRA:ML:Unavailable:Sleeping for:"+i*200);
                    Thread.sleep(i * 200L);
                } catch (InterruptedException ie) {
                }
            }
        }
        if (msgEndpoint == null) {
            //Could not acquire - shut down delivery of messages in this session
            System.err.println("MQRA:ML:Endpoint Unavailable:Shutting down delivery for "+spec.toString());
            //_logger.log(Level.SEVERE, "MQRA:ML:Endpoint Unavailable:Shutting down delivery for "+spec.toString());
            mqmsg._getSession().closeFromRA();
            //endpoint should be shutdown normally by AS via RA.endpointDeactivation()
            return;
        }

        ClassLoader cl = spec.getContextClassLoader();
        int exRedeliveryAttempts = spec.getEndpointExceptionRedeliveryAttempts();
        int exRedeliveryInterval = spec.getEndpointExceptionRedeliveryInterval();
        //Deliver message to msgEndpoint
        boolean redeliver = true;
        while (redeliver == true) {
            try {
                if (transactedDelivery) {
                    msgEndpoint.beforeDelivery(onMessage);
                }
                try {
                    //System.out.println("MQRA:ML:Delivering to onMessage()");
                    if (cl != null) {
                        //System.out.println("MQRA:ML:Setting ContextClassLoader:"+cl.toString());
                        try {
                            Thread.currentThread().setContextClassLoader(cl);
                        } catch (Exception sccle) {
                            System.err.println("MQRA:ML:Exception setting ContextClassLoader:"+sccle.getMessage());
                        }
                    }
                    ((javax.jms.MessageListener)msgEndpoint).onMessage(message);
                    redeliver = false;
                    //System.out.println("MQRA:ML:Delivered successfully");
                    try {
                        mqsess.acknowledgeFromRAEndpoint(mqmsg);
                    } catch (JMSException jmse) {
                        System.err.println("MQRA:ML:JMSException on acknowledge");
                    }
                } catch (Exception rte) {
                    System.err.println("MQRA:ML:Caught Exception from onMessage():"+rte.getMessage());
                    if (exRedeliveryAttempts > 0) {
                        try {
                            //System.out.println("MQRA:ML:RedeliverInterval-start");
                            Thread.sleep(exRedeliveryInterval);
                            //System.out.println("MQRA:ML:RedeliverInterval-stop");
                        } catch (InterruptedException ie) {
                            //System.err.println("MQRA:ML:RedeliverInterval-interrupted");
                        }
                        exRedeliveryAttempts -= 1;
                    } else {
                        System.err.println("MQRA:ML:Exhausted redeliveryAttempts-shutting down delivery for "+spec.toString());
                        redeliver = false;
                        mqmsg._getSession().closeFromRA();
                    }
                }
                if (transactedDelivery) {
                    msgEndpoint.afterDelivery();
                }
            } catch (Throwable t) {
                System.err.println("MQRA:ML:onMessage caught Throwable-before/on/afterDelivery:Class="+t.getClass().getName()+
                        "Msg="+t.getMessage());
                redeliver = false;
                mqmsg._getSession().closeFromRA();
            } finally {
            	try {
            		msgEndpoint.release();
            	} catch (Exception e) {
            		//System.err.println("MQRA:ML:onMessage Exception-msgEp.release");
                }
            }
        }
    }
    
    /**
     * re-create message consumer for this message listener.
     */
    private void recreateConsumer() {
    	
    	try { 		
    		
    		//1. stop session.
    		//this.epConsumer.xas.stopSession();
    		
    		//2. wait for message delivery to finish
    		this.waitForAllOnMessageRunners();
    		
    		//3. recreate message consumer.
    		this.epConsumer.xas.recreateConsumerForRA();
    		
    	} catch (Exception jmse) {
    		//log severe
    		this.epConsumer.xas.logException(jmse);
    	}
    }

}
