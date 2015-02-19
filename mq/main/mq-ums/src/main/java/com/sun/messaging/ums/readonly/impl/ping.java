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

import com.sun.messaging.ums.readonly.DefaultReadOnlyService;
import com.sun.messaging.ums.readonly.ReadOnlyMessageFactory;
import com.sun.messaging.ums.readonly.ReadOnlyRequestMessage;
import com.sun.messaging.ums.readonly.ReadOnlyResponseMessage;
import com.sun.messaging.ums.readonly.ReadOnlyService;
import com.sun.messaging.ums.service.UMSServiceException;
import com.sun.messaging.ums.service.UMSServiceImpl;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author chiaming
 */
public class ping implements ReadOnlyService {
    
    private Properties initParams = null;
    private static long seq = 0;
    
    /**
     * initialize with the servlet init params.
     * @param props
     */
    public void init(Properties initParams) {
        this.initParams = initParams;
    }
    
    public ReadOnlyResponseMessage request (ReadOnlyRequestMessage request) {
        
        try {
            
            String respMsg = null;
            
            Map map = request.getMessageProperties();
            
            String destName = "PING_"+  nextSequence() + "_" + System.currentTimeMillis();
            String msg = destName;
            
            UMSServiceImpl service = (UMSServiceImpl) this.initParams.get(DefaultReadOnlyService.JMSSERVICE);
            
            long start = System.currentTimeMillis();
            
            service.sendText(null, false, destName, msg, map);
            
            String msg2 = service.receiveText(null, destName, false, 30000, map);
            
            long end = System.currentTimeMillis();
            
            if (msg2 != null) {
                respMsg = "Broker is alive, round trip = " + (end - start) + " milli secs.";
            } else {
                respMsg = "Broker is not responding for more than 30 seconds.";
            }
            
            ReadOnlyResponseMessage response = ReadOnlyMessageFactory.createResponseMessage();
            
            response.setResponseMessage(respMsg);
            
            return response;
            
        } catch (Exception e) {

            UMSServiceException umse = new UMSServiceException(e);

            throw umse;
        }
    }
    
    private static synchronized long nextSequence() {
        return ++seq;
    }

}
