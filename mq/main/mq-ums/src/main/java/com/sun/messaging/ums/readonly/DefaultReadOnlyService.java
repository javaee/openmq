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

package com.sun.messaging.ums.readonly;

import com.sun.messaging.ums.common.Constants;
import com.sun.messaging.ums.service.UMSServiceException;
import com.sun.messaging.ums.service.UMSServiceImpl;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author chiaming
 */
public class DefaultReadOnlyService implements ReadOnlyService {
    
    public static final String REQUEST_URL = "requestURL";
    public static final String SERVICE = "service";
    
    public static final String JMSSERVICE = "JMSService";
    
    //this is the default place to add a new readonly service
    public static final String BASE = "com.sun.messaging.ums.readonly.impl.";
    
    private Properties initParams = null;
    
    /**
     * initialize with the servlet init params.
     * @param props
     */
    public void init(Properties initParams) {
        this.initParams = initParams;
    }
    
    /**
     * A request message contains message properties and message body.
     * 
     * The message body is the http request message body.
     * 
     * The request message properties contains key/value pair of the http request.  
     * Each key/value pair of the requestProperties is obtained from 
     * the request url query string.
     * 
     * The request message properties contains at least the following none 
     * empty properties. 
     * 
     * 1. "service" property. 
     * 2. "requestURL" property.
     * 
     * The requestURL contains the URL the client used to make the request. 
     * The URL contains a protocol, server name, port number, and server path, 
     * but it does not include query string parameters.
     * 
     * Query string is parsed into key/value pair in the requestProperties
     * parameter.
     * 
     * A new readonly service can be created in the ./impl package, with the
     * service name as its class name:
     * 
     * com.sun.messaging.ums.readonly.impl.service
     * 
     * For example, 
     * 
     * http://localhost:8080/ums/simple?service=query1&destination=simpleQ&domain=queue
     * 
     * would result in the following object instantiation.
     * 
     * com.sun.messaging.ums.readonly.impl.query1
     * 
     * 
     * @param request
     * @return  The service implementation must construct a proper formatted
     * java string object and return as the request response.
     */
    
    public ReadOnlyResponseMessage request (ReadOnlyRequestMessage request) {
           
        ReadOnlyResponseMessage resp = null;
        
        try {
            //String svr = getSimpleRequestProperty (Constants.SERVICE_NAME, requestProperties);
            String svr = request.getMessageProperty(Constants.SERVICE_NAME);
            String cname = BASE + svr;
            
            String requestURL = request.getMessageProperty(this.REQUEST_URL);
            UMSServiceImpl.logger.info ("Invoking class, name=" + cname + ", requestURL=" + requestURL );
            
            ReadOnlyService ros = (ReadOnlyService) Class.forName(cname).newInstance();
            
            ros.init(initParams);
            
            resp = ros.request(request);
            
        } catch (Exception e) {
            UMSServiceImpl.logger.log(Level.WARNING, e.getMessage(), e);
            throw new UMSServiceException (e);
        }
        
        return resp;
        
    }

}
