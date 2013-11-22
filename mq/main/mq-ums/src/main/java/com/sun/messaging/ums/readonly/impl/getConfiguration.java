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

package com.sun.messaging.ums.readonly.impl;

import com.sun.messaging.ums.common.Constants;
import com.sun.messaging.ums.dom.util.XMLDataBuilder;
import com.sun.messaging.ums.provider.openmq.ProviderDestinationService;
import com.sun.messaging.ums.readonly.ReadOnlyMessageFactory;
import com.sun.messaging.ums.readonly.ReadOnlyRequestMessage;
import com.sun.messaging.ums.readonly.ReadOnlyResponseMessage;
import com.sun.messaging.ums.readonly.ReadOnlyService;
import com.sun.messaging.ums.service.DestinationService;
import com.sun.messaging.ums.service.UMSServiceException;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author chiaming
 */
public class getConfiguration implements ReadOnlyService {
    
    private Properties initParams = null;
    
    /**
     * initialize with the servlet init params.
     * @param props
     */
    public void init(Properties initParams) {
        this.initParams = initParams;
    }
    
    public ReadOnlyResponseMessage request(ReadOnlyRequestMessage request) {

        try {
        	
        	// authenticate by trying to create a JMX connection to the broker 
            ProviderDestinationService pds = DestinationService.getProviderDestinationService(null);
            String user = request.getMessageProperty(Constants.USER);
            String pass = request.getMessageProperty(Constants.PASSWORD);
            pds.authenticate(user, pass);

            String respMsg = null;

            //create a new instance of ums xml document.
            Document doc = XMLDataBuilder.newUMSDocument();

            //get the root element
            Element root = XMLDataBuilder.getRootElement(doc);

            //create the first child element
            Element baddr = XMLDataBuilder.createUMSElement(doc, "BrokerAddress");

            //set text value to the first child
            XMLDataBuilder.setElementValue(doc, baddr, this.initParams.getProperty(Constants.IMQ_BROKER_ADDRESS, "localhost:7676"));

            //add the first child to the root element
            XMLDataBuilder.addChildElement(root, baddr);
            
            //create the auth child element
            Element auth = XMLDataBuilder.createUMSElement(doc, "JMSAuthenticate");

            //set text value to the auth child
            XMLDataBuilder.setElementValue(doc, auth, this.initParams.getProperty(Constants.JMS_AUTHENTICATE));

            //add to the root element
            XMLDataBuilder.addChildElement(root, auth);
            
            //create the auth child element
            Element cacheTime = XMLDataBuilder.createUMSElement(doc, "CacheDuration");

            //set text value
            XMLDataBuilder.setElementValue(doc, cacheTime, this.initParams.getProperty(Constants.CACHE_DURATION,"420000"));

            //add  to the root element
            XMLDataBuilder.addChildElement(root, cacheTime);
            
            //create the sweep child element
            Element sweepTime = XMLDataBuilder.createUMSElement(doc, "SweepInterval");

            //set text value
            XMLDataBuilder.setElementValue(doc, sweepTime, this.initParams.getProperty(Constants.SWEEP_INTERVAL,"120000"));

            //add  to the root element
            XMLDataBuilder.addChildElement(root, sweepTime);
            
            //create the sweep child element
            Element receiveTimeout = XMLDataBuilder.createUMSElement(doc, "ReceiveTimeout");

            //set text value
            XMLDataBuilder.setElementValue(doc, receiveTimeout, this.initParams.getProperty(Constants.RECEIVE_TIMEOUT,"7000"));

            //add  to the root element
            XMLDataBuilder.addChildElement(root, receiveTimeout);
            
            //create the sweep child element
            Element maxClient = XMLDataBuilder.createUMSElement(doc, "MaxClientsPerConnection");

            //set text value
            XMLDataBuilder.setElementValue(doc, maxClient, this.initParams.getProperty(Constants.MAX_CLIENT_PER_CONNECTION,"100"));

            //add  to the root element
            XMLDataBuilder.addChildElement(root, maxClient);

            //transform xml document to a string
            respMsg = XMLDataBuilder.domToString(doc);

            ReadOnlyResponseMessage response = ReadOnlyMessageFactory.createResponseMessage();

            response.setResponseMessage(respMsg);

            return response;

        } catch (Exception e) {

            UMSServiceException umse = new UMSServiceException(e);

            throw umse;
        }
    }
   

}
