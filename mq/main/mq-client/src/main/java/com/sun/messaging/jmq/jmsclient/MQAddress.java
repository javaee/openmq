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

/*
 * @(#)MQAddress.java	1.10 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.*;
import java.net.*;
import java.io.Serializable;

/**
 * This class represents broker address URL.
 */
public class MQAddress extends com.sun.messaging.jmq.io.MQAddress {

    protected static final HashMap handlers = new HashMap();

    private static final String TCP_HANDLER =
        "com.sun.messaging.jmq.jmsclient.protocol.tcp.TCPStreamHandler";

    private static final String SSL_HANDLER =
        "com.sun.messaging.jmq.jmsclient.protocol.ssl.SSLStreamHandler";

    private static final String HTTP_HANDLER =
            "com.sun.messaging.jmq.jmsclient.protocol.http.HTTPStreamHandler";
    
    private static final String DIRECT_HANDLER =
        "com.sun.messaging.jmq.jmsclient.protocol.direct.DirectStreamHandler";

    private static final String WEBSOCKET_HANDLER =
        "com.sun.messaging.jmq.jmsclient.protocol.websocket.WebSocketStreamHandler";

    static {
        handlers.put("jms", TCP_HANDLER);
        handlers.put("ssljms", SSL_HANDLER);
        handlers.put("httpjms", HTTP_HANDLER);
        handlers.put("httpsjms", HTTP_HANDLER);
        handlers.put("admin", TCP_HANDLER);
        handlers.put("ssladmin", SSL_HANDLER);
        handlers.put("httpadmin", HTTP_HANDLER);
        handlers.put("httpsadmin", HTTP_HANDLER);
        handlers.put("direct", DIRECT_HANDLER);
        handlers.put(DEFAULT_WS_SERVICE, WEBSOCKET_HANDLER);
        handlers.put(DEFAULT_WSS_SERVICE, WEBSOCKET_HANDLER);
    }


    protected MQAddress() {
         super();
    }

    /**
     * Parses the given MQ Message Service Address and creates an
     * MQAddress object.
     */
    public static MQAddress 
           createMQAddress(String addr)
        throws MalformedURLException {
        MQAddress ret = new MQAddress();
        ret.initialize(addr);
        return ret;
    }




    public String getHandlerClass() {
        if (isHTTP) {
            return HTTP_HANDLER;
        }
        if (isWebSocket) {
            return WEBSOCKET_HANDLER;
        }
        if (schemeName.equalsIgnoreCase("mqtcp"))
            return TCP_HANDLER;
        if (schemeName.equalsIgnoreCase("mqssl"))
            return SSL_HANDLER;
        if (schemeName.equalsIgnoreCase("direct"))
            return DIRECT_HANDLER;

        String ret = (String) handlers.get(serviceName);
        // assert (ret != null);
        
        if (Debug.debug) {
            ConnectionImpl.getConnectionLogger().info("Handler class: " + ret);
        }
        
        return ret;
    }


    public static void main(String args[]) throws Exception {
        MQAddress addr = createMQAddress(args[0]);
        System.out.println("schemeName = " + addr.getSchemeName());
        if (addr.getIsHTTP())
            System.out.println("URL = " + addr.getURL());
        else {
            System.out.println("host = " + addr.getHostName());
            System.out.println("port = " + addr.getPort());
        }
        System.out.println("serviceName = " + addr.getServiceName());
        System.out.println("handlerClass = " + addr.getHandlerClass());
        System.out.println("isFinal = " + addr.isServicePortFinal());
        System.out.println("properties = " + addr.props);
    }
}

/*
 * EOF
 */
