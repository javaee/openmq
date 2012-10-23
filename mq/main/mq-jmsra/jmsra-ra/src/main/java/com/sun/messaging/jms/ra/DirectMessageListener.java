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

import java.lang.reflect.Method;

import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 *
 */
public class DirectMessageListener
implements javax.jms.MessageListener {

    /**
     *  MessageListener instance data
     */
    private DirectConnection dc;
    private DirectSession ds;
    private Method onMessageMethod;

    private int dMLId = -1;

    private boolean isDeliveryTransacted = false;
    private int maxRedeliverCount = 1;
    private boolean noAckDelivery = false;

    /** The MessageEndpoint for this DirectMessageListener */
    private MessageEndpoint msgEndpoint = null;

    /**
     *  The XAResource that handles XA transactions for this
     *  DirectMessageListener
     */
    private DirectXAResource dxar = null;

    /* Loggers */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.DirectMessageListener";
    protected static transient final String _lgrNameInboundMessage =
            "javax.resourceadapter.mqjmsra.inbound.message";
    protected static transient final Logger _loggerIM =
            Logger.getLogger(_lgrNameInboundMessage);
    protected static transient final String _lgrMIDPrefix = "MQJMSRA_DML";
    protected static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    protected static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    protected static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    protected static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    protected static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";

    private static int idCounter = 0;

    /** Creates a new instance of DirectMessageListener */
    public DirectMessageListener(EndpointConsumer epConsumer,
            MessageEndpointFactory epFactory, DirectConnection dc,
            DirectSession ds, Method onMessageMethod,
            boolean isDeliveryTransacted,  int maxRedeliverCount,
            boolean noAckDelivery)
    {
        Object params[] = new Object[8];
        params[0] = epConsumer;
        params[1] = epFactory;
        params[2] = dc;
        params[3] = ds;
        params[4] = onMessageMethod;
        params[5] = isDeliveryTransacted;
        params[6] = maxRedeliverCount;
        params[7] = noAckDelivery;

        _loggerIM.entering(_className, "constructor()", params);

        //System.out.println("MQRA:ML:Constructor()-omrp:min,max="+spec.getEndpointPoolSteadySize()+","+spec.getEndpointPoolMaxSize());
        //this.epConsumer = epConsumer;
        //this.epFactory = epFactory;
        //this.spec = (com.sun.messaging.jms.ra.ActivationSpec)spec;

        this.dc = dc;
        this.ds = ds;
        this.onMessageMethod = onMessageMethod;
        this.isDeliveryTransacted = isDeliveryTransacted;
        this.maxRedeliverCount= maxRedeliverCount;
        this.noAckDelivery = noAckDelivery;

        this.dxar = new DirectXAResource(this.dc, this.dc._getJMSService(),
                this.dc.getConnectionId());
        this.dxar._setUsedByMDB(true);
        try {
            this.msgEndpoint = epFactory.createEndpoint(this.dxar);
        } catch (UnavailableException ex) {
            System.out.println("DirectMessageListener-Exception creating Endpoint:"
                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    public void onMessage(javax.jms.Message jmsMsg) {
        DirectPacket dpMsg = (DirectPacket)jmsMsg;
        boolean delivered = false;
        boolean acknowledged = false;
        boolean redeliver = true;
        int redeliverCount = 0;
        while (redeliver == true){
            if (this.isDeliveryTransacted) {
                try {
                    this.msgEndpoint.beforeDelivery(this.onMessageMethod);
                } catch (ResourceException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
            try {
                ((javax.jms.MessageListener)this.msgEndpoint).onMessage(jmsMsg);
                delivered = true;
                redeliver = false;
                try {
                    dpMsg._acknowledgeThisMessageForMDB(this.dxar);
                    acknowledged = true;
                    this.dxar.setRollback(false, null);
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
                
            } catch (Exception rte) {
                //Here if onMessage threw any kind of Exception
                if (redeliverCount > this.maxRedeliverCount){
                    //Turn off redelivery and set cause for rollback if in txn
                    redeliver = false;
                    this.dxar.setRollback(true, rte);
                } else {
                    redeliverCount++;
                }
            }
            if (this.isDeliveryTransacted) {
                try {
                    this.msgEndpoint.afterDelivery();
                } catch (ResourceException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (acknowledged != true){
            //Need to acknowledge as Dead
        }
    }

}
