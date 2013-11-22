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

package com.sun.messaging.ums.service;

import com.sun.messaging.xml.MessageTransformer;
import com.sun.messaging.ums.common.MessageUtil;

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import com.sun.messaging.ums.common.Constants;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.MessageProducer;

public class ReceiveServiceImpl implements ReceiveService {
   
    //private Lock lock = null;
    private Properties props = null;
    private ClientPool cache = null;
    //default receive timeout
    private long receiveTimeout = 7000;
    private Logger logger = UMSServiceImpl.logger;
    //cache sweeper
    private CacheSweeper sweeper = null;
    private static final String SERVICE_NAME = "RECEIVE_SERVICE";
    private MessageFactory soapMF = null;
    
    private String provider = null;
    
    private String myName = null;
    
    private static final String UMS_DMQ = "UMS.DMQ";
    
    public ReceiveServiceImpl(String provider, ClientPool cache, CacheSweeper sweeper, Properties p) throws JMSException {
        
        this.provider = provider;
        
        this.sweeper = sweeper;
        
        this.myName = provider + "_" + SERVICE_NAME; 
        
        this.props = p;

        //lock = new Lock();

        //cache = new ClientPool(provider, SERVICE_NAME, props, lock);
        
        //sweeper.addClientPool(cache);

        this.cache = cache;
        
        String tmp = props.getProperty(Constants.IMQ_RECEIVE_TIMEOUT, Constants.IMQ_RECEIVE_TIMEOUT_DEFAULT_VALUE);
        receiveTimeout = Long.parseLong(tmp);

        initSOAPMessageFactory();

        
    }

    private void initSOAPMessageFactory() throws JMSException {

        try {
            soapMF = MessageFactory.newInstance(SOAPConstants.DEFAULT_SOAP_PROTOCOL);
        } catch (Exception ex) {

            JMSException jmse = new JMSException(ex.getMessage());
            jmse.setLinkedException(ex);

            throw jmse;
        }
    }

    public SOAPMessage receive(SOAPMessage request) throws JMSException {
        // TODO Auto-generated method stub

        SOAPMessage reply = null;
        Client client = null;
        
        String user = null;
        String pass = null;

        try {

            /**
            String clientId = MessageUtil.getServiceClientId(request);
             
            if (clientId == null) {
                user = MessageUtil.getServiceAttribute(request, "user");
                pass = MessageUtil.getServiceAttribute(request, "password");             
            }
            
            client = cache.getClient(clientId, user, pass);
            **/
            
            client = cache.getClient(request);
            
            //Object syncObj = lock.getLock(client.getId());
            Object syncObj = client.getLock();

            //Object syncObj = lock.getLock(clientId);

            MessageConsumer consumer = null;

            Message jmsMessage = null;

            if (UMSServiceImpl.debug) {
                logger.info("Receiving message ...");
            }
            
            synchronized (syncObj) {

                //client = cache.getClient(clientId);

                boolean isTopic = MessageUtil.isServiceTopicDomain(request);

                String destName = MessageUtil.getServiceDestinationName(request);

                consumer = client.getConsumer(isTopic, destName);
                
                long timeout = getTimeout (request);

                jmsMessage = consumer.receive(timeout);
            }

            //logger.info ("ReceiveService received message ...");

            if (jmsMessage == null) {
                
                if (UMSServiceImpl.debug) {
                    logger.info ("No messages received ...");
                }
                
            } else if (jmsMessage instanceof BytesMessage) {

                reply = MessageTransformer.SOAPMessageFromJMSMessage(jmsMessage, soapMF);

                if (UMSServiceImpl.debug) {
                    logger.info ("received soap message : " + reply);
                }
                //reply.writeTo(System.out);

            } else {
                //XXX I18N
                logger.warning ("received message is not a JMS BytesMessage type: " + jmsMessage);
                this.sendToDMQ(client, jmsMessage);
            }

        } catch (Throwable ex) {

            ex.printStackTrace();

            if (ex instanceof JMSException) {
                throw (JMSException) ex;
            } else {
                JMSException jmse = new JMSException(ex.getMessage());
                if ( ex instanceof Exception) {
                    jmse.setLinkedException( (Exception)ex);
                }
                throw jmse;
            }

        } finally {
            cache.returnClient(client);
        }

        return reply;
    }
    
    private long getTimeout (SOAPMessage sm) {
        long timeout = 30000;
        
        try {
            timeout = MessageUtil.getServiceReceiveTimeout(sm);
            
            if (timeout <= 0 ||timeout >= 60000) {
                timeout = this.receiveTimeout;
                
                //XXX I18N
                logger.info ("timeout value is between 0 (exclusive) and 60000 (inclusive) milli secs.");
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        return timeout;
    }

    /**
     * receive a TextMessage from MQ.
     * 
     * @param clientId
     * @param destName
     * @param isTopic
     * @return
     * @throws javax.jms.JMSException
     */
    public String receiveText(String sid, String destName, boolean isTopic, long timeout, Map map) throws JMSException {
        // TODO Auto-generated method stub

        String reply = null;

        Client client = null;
               
        String user = null;
        String pass = null;

        try {

            /**
            if (clientId == null) {
                
                String[] ua = (String[]) map.get("user");

                if (ua != null && ua.length == 1) {
                    user = ua[0];
                }

                String[] pa = (String[]) map.get("password");
                if (pa != null && pa.length == 1) {
                    pass = pa[0];
                }
            }

            client = cache.getClient(clientId, user, pass);
            **/
            client = cache.getClient(sid, map);
            //Object syncObj = lock.getLock(client.getId());
            Object syncObj = client.getLock();

            MessageConsumer consumer = null;

            Message jmsMessage = null;

            if (UMSServiceImpl.debug) {
                logger.info ("ReceiveService receiving Text message ...");
            }
            
            synchronized (syncObj) {


                consumer = client.getConsumer(isTopic, destName);

                jmsMessage = consumer.receive(timeout);
            }

            if (UMSServiceImpl.debug) {
                logger.info ("ReceiveService received message ..." + jmsMessage);
            }
            
            if (jmsMessage == null) {
                
                if (UMSServiceImpl.debug) {
                    logger.info ("No messages received ...");
                }
                
            } else if (jmsMessage instanceof TextMessage) {


                reply = ((TextMessage) jmsMessage).getText();

                if (UMSServiceImpl.debug) {
                    logger.info ("received text message : " + reply);
                }
                
            } else {
                
                //XXX I18N
                logger.warning ("received message is not a TextMessage type, message=" + jmsMessage);
                this.sendToDMQ(client, jmsMessage);
            }

        } catch (Exception ex) {

            logger.log(Level.WARNING, ex.getMessage(), ex);

            if (ex instanceof JMSException) {
                throw (JMSException) ex;
            } else {
                JMSException jmse = new JMSException(ex.getMessage());
                jmse.setLinkedException(ex);
                throw jmse;
            }

        } finally {
            cache.returnClient(client);
        }

        return reply;
    }

    /**
     * Messages unable to process is sent to UMS_DMQ
     * @param client
     * @param message
     */
    public void sendToDMQ (Client client, Message message) throws JMSException {
        
        javax.jms.Destination dmq = cache.getJMSDestination(UMS_DMQ, false);
        MessageProducer producer = client.getProducer();
        
        producer.send(dmq, message);
        
        //if (UMSServiceImpl.debug) {
            logger.info("Message sent to DMQ, destination=" + UMS_DMQ + ", message=" + message);
        //}
        
    }

    public void close() {
        
        try {

            //sweeper.removeClientPool(cache);

            this.cache.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
