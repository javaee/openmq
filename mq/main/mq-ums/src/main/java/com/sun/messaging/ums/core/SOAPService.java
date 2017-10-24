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

package com.sun.messaging.ums.core;

//import javax.xml.messaging.ReqRespListener;

import javax.xml.soap.SOAPException;

/**
 * MQ SOAP Service interface.  This is part of the MQ SOAP Service frame work
 * that is used to implement and provide a new SOAP service in the frame work.
 *
 * <p>A SOAP service consists of the following components:
 *
 * <p>1. Request Handler Chain.  The Request handler can be registered as follows
 * in the web.xml:
 *
 * <p>mq.soap.request.handler.#=MessageHandler class full name.
 *
 * <p>For example,
 *
 * <p>mq.soap.request.handler.1=com.sun.TestMessageListener1
 * <p>mq.soap.request.handler.2=com.sun.TestMessageListener2
 *
 * <P>2. Response Handler Chain.  The Response handler can be registered as
 * follows in the web.xml:
 *
 * <p>mq.soap.response.handler.#=MessageHandler class full name.
 *
 * <p>For example,
 *
 * <P>mq.soap.response.handler.1=com.sun.TestMessageListener1
 * <p>mq.soap.response.handler.2=com.sun.TestMessageListener2
 *
 * <p>3. ReqRespListener onMessage() implementation.
 *
 * <p>4. Service lifecycle management methods.  There are four methods defined
 * for life cycle management - init/start/stop/close.  They are used for
 * init/start/stop/close a MQ SOAP Service instance.  Sub class SHOULD
 * implement or over ride the life cycle methods if necessary.
 *
 *
 * <p>MQSOAPService is a class that provides base implementation of SOApService
 * interface.  A new SOAP service is recommended to sub class MQSOAPService
 * and over ride appropriate methods as needed.  Please see MQSOAPService
 * Javadoc for details.
 *
 * @author  chiaming yang
 * @see     MQSOAPService
 * @see     MessageHandler
 * @see     MessageHandlerChain
 */
public interface SOAPService {

    /**
     * init this SOAPService with the specified Properties in the parameter.
     * This method is called when the service is loaded in the the frame
     * work.
     */
    public void init (ServiceContext context) throws SOAPException;

    /**
     * Get req handler chain in this service.
     */
    public MessageHandlerChain getReqHandlerChain();

    /**
     * Get resp handler chain in this service.
     */
    public MessageHandlerChain getRespHandlerChain();

    /**
     * SOAP service life cycle - start this soap service.
     */
    public void start();

    /**
     * SOAP service life cycle - stop this soap service.
     */
    public void stop();

    /**
     * SOAP service life cycle - close this soap service.
     */
    public void close();

    /**
     * Get the ServiceContext object associated with this SOAP service.
     */
    public ServiceContext getServiceContext();

    /**
     * Get this soap service URI.
     */
    public String getServiceName();
    
    public void service (MessageContext context) throws SOAPException;

}

