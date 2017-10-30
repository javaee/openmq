/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.util.*;
import javax.jms.*;

import javax.xml.soap.*;

import com.sun.messaging.ums.core.UMSService;
import com.sun.messaging.ums.core.MessageContext;
import com.sun.messaging.ums.core.ServiceContext;
import com.sun.messaging.ums.common.*;
import com.sun.messaging.ums.service.UMSServiceImpl;
//import com.sun.messaging.xml.imq.soap.service.jms.impl.SimpleLogger;
import java.util.logging.Logger;

/**
 * <p>A MQ SOAP Service is a class that implements SOAPService interface and
 * may be used to *register* its service to a MQSOAPServlet as a SOAP
 * service and becomes part of the MQ SOAP Service frame work.
 *
 * <p>A MQ SOAP service consists of the following components:
 *
 * <p>1. Request Handler Chain.  The Request handler can be registered as follows
 * in the web.xml:
 *
 * <p>mq.soap.request.handler.#="MessageHandler class full name"
 *
 * <p>For example,
 *
 * <p>mq.soap.request.handler.1=com.sun.TestMessageListener1
 * <p>mq.soap.request.handler.2=com.sun.TestMessageListener2
 *
 * <p>2. Response Handler Chain.  The Response handler can be registered as
 * follows in the web.xml:
 *
 * <p>mq.soap.response.handler.#=MessageHandler class full name.
 *
 * <p>For example,
 *
 * <p>mq.soap.response.handler.1=com.sun.TestMessageListener1
 * <p>mq.soap.response.handler.2=com.sun.TestMessageListener2
 *
 * <p>3. A service() method to be over ridden by subclass.
 *
 * <p>4. Service lifecycle management methods.  There are four methods defined
 * for life cycle management - init/start/stop/close.  They are used for
 * init/start/stop/close a MQ SOAP Service instance.  Sub class SHOULD
 * implement or over ride the life cycle methods if necessary.
 *
 *
 * @author  chiaming yang
 * 
 * @see     SOAPService
 * @see     MessageHandler
 * @see     MessageHandlerChain
 */
public class SOAP2JMSService extends UMSService {
	
	//setvice context attribute names
	public static final String JMS_CONNECTION = "JMS_CONNECTION";
	public static final String SOAP_FACTORY = "SOAP_FACTORY";
	
	//message context attribute names
	public static final String DESTINATION_NAME = "destination_name";
	public static final String DESTINATION_DOMAIN = "destination_domain";
	
	//private Connection connection = null;
	//private Session session = null;
	
	//private MessageFactory soapFactory = null;
	
	//send service
	//private SendService sendService = null;
	
	//receive service
	//private ReceiveService receiveService = null;
	private static Logger logger = UMSServiceImpl.logger;
    
    /**
     * MQService
     */
    private UMSServiceImpl MQService = null;
    
	/**
     * init this SOAPService with the specified Properties in the parameter.
     */
    public void init (ServiceContext context) throws SOAPException {
        
    	super.init(context);
        
        //should we pass SerViceContext instead?
        MQService = new UMSServiceImpl (this.props);
        
        MQService.init();
        
    }


    /**
     * To be over ridden by sub class.
     */
    public void service(MessageContext context) throws SOAPException {

        try {
            
            SOAPMessage request = context.getRequestMessage();
            
            String provider = MQService.getProvider(request);
           
            SOAPMessage response = null;

            String serviceName = MessageUtil.getServiceName (request);
            
            String destName = MessageUtil.getServiceAttribute(request, Constants.DESTINATION_NAME);
            
            String domain = MessageUtil.getServiceAttribute(request, Constants.DOMAIN);
            if (domain == null) {
                domain = Constants.QUEUE_DOMAIN;
            }
            
            if (Constants.SERVICE_VALUE_LOGIN.equals(serviceName)) {
                
                //login request
                response = MessageUtil.createResponseMessage(request);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_LOGIN_REPLY);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, Constants.SERVICE_STATUS_VALUE_OK);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                
                //authenticate
                String sid = MQService.authenticate(request);
                MessageUtil.setServiceAttribute(response, Constants.CLIENT_ID, sid);
                
                context.setResponseMessage(response);

            } else if (Constants.SERVICE_VALUE_CLOSE.equals(serviceName)) {
                
                MQService.closeClient(request);
                
                response = MessageUtil.createResponseMessage(request);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_CLOSE_REPLY);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, Constants.SERVICE_STATUS_VALUE_OK);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                
                //authenticate
                //String sid = MQService.authenticate(request);
                //MessageUtil.setServiceAttribute(response, Constants.CLIENT_ID, sid);
                
                context.setResponseMessage(response);
                
                
            } else if (Constants.SERVICE_VALUE_SEND_MESSAGE.equals(serviceName)) {
                
                MQService.send(request);
               
                response = MessageUtil.createResponseMessage(request);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_SEND_MESSAGE_REPLY);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, Constants.SERVICE_STATUS_VALUE_OK);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                
                MessageUtil.setServiceAttribute(response, Constants.DESTINATION_NAME, destName);
                
                MessageUtil.setServiceAttribute(response, Constants.DOMAIN, domain);
                
                context.setResponseMessage(response);
                
            } else if (Constants.SERVICE_VALUE_RECEIVE_MESSAGE.equals(serviceName)) {
                
                response = MQService.receive(request);
               
                //System.out.println ("@@@@@@@@@@@@@@@@@@@@@@@@@ received: \n" );
                //response.writeTo(System.out);
                
                String statusCode = null;
                if (response ==null) {
                   
                    response = MessageUtil.createResponseMessage(request);
                    statusCode = Constants.SERVICE_STATUS_VALUE_NO_MESSAGE;
                    
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_RECEIVE_MESSAGE_REPLY);
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, statusCode);
                
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                    
                    MessageUtil.setServiceAttribute(response, Constants.DESTINATION_NAME, destName);
                
                    MessageUtil.setServiceAttribute(response, Constants.DOMAIN, domain);
                    
                    //String destname = MessageUtil.getServiceAttribute(request, Constants.DESTINATION_NAME);
                    //MessageUtil.setServiceAttribute(response, Constants.DESTINATION_NAME, destname);
                    
                } else {
                    
                    response = MessageUtil.createResponseMessage2 (request, response);
                    
                    statusCode = Constants.SERVICE_STATUS_VALUE_OK;
                
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_RECEIVE_MESSAGE_REPLY);
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, statusCode);
                
                    MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                    
                    MessageUtil.setServiceAttribute(response, Constants.DESTINATION_NAME, destName);
                
                    MessageUtil.setServiceAttribute(response, Constants.DOMAIN, domain);
                    
                    //String destname = MessageUtil.getServiceAttribute(request, Constants.DESTINATION_NAME);
                    //MessageUtil.setServiceAttribute(response, Constants.DESTINATION_NAME, destname);
                            
                }
                
                //System.out.println ("\n @@@@@@@@@@@@@@@@@@@@ after service tag: \n");
                //response.writeTo(System.out);
                
                context.setResponseMessage(response);
            } else if (Constants.SERVICE_VALUE_COMMIT.equals(serviceName)) {
                
                if (UMSServiceImpl.debug) {
                    logger.info("***** committing transaction ....");
                }
                
                MQService.commit (request);
               
                response = MessageUtil.createResponseMessage(request);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_COMMIT_REPLY);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, Constants.SERVICE_STATUS_VALUE_OK);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                
                context.setResponseMessage(response);
                
            } else if (Constants.SERVICE_VALUE_ROLLBACK.equals(serviceName)) {
                
                if (UMSServiceImpl.debug) {
                    logger.info("***** rolling back transaction ....");
                }
                
                MQService.rollback (request);
               
                response = MessageUtil.createResponseMessage(request);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_NAME, Constants.SERVICE_VALUE_ROLLBACK_REPLY);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_STATUS_NAME, Constants.SERVICE_STATUS_VALUE_OK);
                
                MessageUtil.setServiceAttribute(response, Constants.SERVICE_PROVIDER_ATTR_NAME, provider);
                
                context.setResponseMessage(response);
            }
            
        } catch (Exception e) {
            SOAPException soape = new SOAPException(e);

            throw soape;
        }

    }
       
    public void close() {
    	try {
            this.MQService.destroy();  	    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public UMSServiceImpl getJMSService() {
        return this.MQService;
    }
    
}

