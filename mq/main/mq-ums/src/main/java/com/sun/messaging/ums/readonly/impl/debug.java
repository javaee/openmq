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
import com.sun.messaging.ums.provider.openmq.ProviderDestinationService;
import com.sun.messaging.ums.readonly.ReadOnlyMessageFactory;
import com.sun.messaging.ums.readonly.ReadOnlyRequestMessage;
import com.sun.messaging.ums.readonly.ReadOnlyResponseMessage;
import com.sun.messaging.ums.readonly.ReadOnlyService;
import com.sun.messaging.ums.service.DestinationService;
import com.sun.messaging.ums.service.UMSServiceException;
import com.sun.messaging.ums.service.UMSServiceImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author chiaming
 */
public class debug implements ReadOnlyService {

    private Properties initParams = null;
    
    /**
     * initialize with the servlet init params.
     * @param props
     */
    public void init(Properties initParams) {
        this.initParams = initParams;
    }
    
    /**
     * The requestProperties contains key/value pair of the request.  
     * Each key/value pair of the requestProperties is obtained from 
     * the request url query string.
     * 
     * The requestProperties parameter contains at least the following none 
     * empty properties. 
     * 
     * "service" and its corresponding value. 
     * "requestURL" 
     * 
     * The requestURL contains the URL the client used to make the request. 
     * The URL contains a protocol, server name, port number, and server path, 
     * but it does not include query string parameters.
     * 
     * Query string is parsed into key/value pair in the requestProperties
     * parameter.
     * 
     * @param props
     * @return  The service implementation must construct a proper formatted
     * java string object and return as the request response.
     */
    public ReadOnlyResponseMessage request (ReadOnlyRequestMessage request) {

        boolean debug = false;

        try {
        	// authenticate by trying to create a JMX connection to the broker 
            ProviderDestinationService pds = DestinationService.getProviderDestinationService(null);
            String user = request.getMessageProperty(Constants.USER);
            String pass = request.getMessageProperty(Constants.PASSWORD);
            pds.authenticate(user, pass);

            String flag = request.getMessageProperty("debug");

            debug = Boolean.valueOf(flag).booleanValue();

            UMSServiceImpl.logger.info("Debug mode is set to ..." + debug + ", user =" +  user);

            UMSServiceImpl.setDebug(debug);

            String respMsg = "service=debug, debug=" + debug;
            
            ReadOnlyResponseMessage response = ReadOnlyMessageFactory.createResponseMessage();
            response.setResponseMessage(respMsg);
            
            return response;
            
        } catch (Exception e) {

            UMSServiceException umse = new UMSServiceException(e);

            throw umse;
        }

    }
    
}