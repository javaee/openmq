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

import java.util.Iterator;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 *
 */
public class ConcurrentEndpointConsumer extends EndpointConsumer {

    /**
     *  The number of concurrent delvery threads that will be running
     */
    private static final int numConcurrentConsumers = 20;

    /**
     *  The Vector that holds the list of created DirectConnection objects
     */
    private Vector<DirectConnection> connections = 
            new Vector<DirectConnection>(this.numConcurrentConsumers);

    /** Creates a new instance of ConcurrentEndpointConsumer */
    public ConcurrentEndpointConsumer(com.sun.messaging.jms.ra.ResourceAdapter ra,
            MessageEndpointFactory endpointFactory,
            javax.resource.spi.ActivationSpec spec,
            boolean isRADirect)
    throws ResourceException {
        super(ra, endpointFactory, spec);
        //connections = new Vector<DirectConnection>(this.numConcurrentConsumers);
        //this.onMessageMethod = ra._getOnMessageMethod();
//        try {
//            this.isDeliveryTransacted = 
//                    endpointFactory.isDeliveryTransacted(this.onMessageMethod);
//        } catch (NoSuchMethodException ex) {
//            //Assume delivery is non-transacted
//            //Fix to throw NotSupportedException on activation
//            //ex.printStackTrace();
//        }
    }

    /**
     *  Start the Direct MessageConsumer
     */
    protected void startDirectConsumer()
    throws NotSupportedException {
        //cycle through connections and start them
        DirectConnection dc = null;
        Iterator<DirectConnection> k = this.connections.iterator();
        while (k.hasNext()) {
            dc = k.next();
            try {
                dc.start();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *  Stop the Direct MessageConsumer
     */
    protected void stopDirectConsumer()
    throws Exception {
        
    }

    protected void createDirectMessageConsumer(/*MessageEndpointFactory epFactory,
            String username, String password, String selector,
            boolean isDurable, String subscriptionName,
            int maxRedeliveryCount, boolean noAckDelivery*/)
    throws NotSupportedException {
        try {
            for (int i=0; i<this.numConcurrentConsumers; i++){
        
                //Use method that avoids allocation via the ConnectionManager
                DirectConnection dc = (DirectConnection)
                        dcf._createConnection(username, password);
                this.connections.add(dc);
                /*
                if (effectiveCId != null) {
                    this.dc._setClientID(effectiveCId);
                }
                */
                DirectSession ds = (DirectSession)dc.createSession(false,
                                Session.CLIENT_ACKNOWLEDGE);
                DirectConsumer msgConsumer = (DirectConsumer)
                    (isDurable
                        ? ds.createDurableSubscriber(
                            (Topic)destination,
                            subscriptionName, selector, false)
                        : ds.createConsumer(destination, selector)
                    );
                DirectMessageListener dMsgListener = 
                        new DirectMessageListener(this, this.endpointFactory,
                        dc,
                        this.onMessageMethod,
                        this.isDeliveryTransacted, 
                        this.exRedeliveryAttempts, this.noAckDelivery);
                msgConsumer.setMessageListener(
                        (javax.jms.MessageListener)dMsgListener);
                //dc.start();
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
