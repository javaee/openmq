/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)HTTPStreamHandler.java	1.12 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.protocol.http;

import java.io.*;
import java.util.Arrays;
import javax.jms.*;

import com.sun.messaging.PropertyOwner;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;
import com.sun.messaging.jmq.jmsclient.protocol.ssl.SSLUtil;

/**
 * This class is the HTTP protocol handler for the iMQ JMS client
 * implementation.
 */
public class HTTPStreamHandler implements StreamHandler, PropertyOwner {

    /**
     * POODLE fix
     * see http://www.oracle.com/technetwork/java/javase/documentation/cve-2014-3566-2342133.html
     */
    static {
        String[] protocols = SSLUtil.getKnownSSLEnabledProtocols();
        final String orig = Arrays.toString(protocols);
        StringBuffer buf = new StringBuffer();
        int cnt = 0;
        for (String s : protocols) {
            if (s.equals("SSLv3") || s.equals("SSLv2Hello")) {
                continue;
            }
            if (cnt > 0) {
                buf.append(",");    
            }
            buf.append(s);    
            cnt++;
        }
        final String sysval = buf.toString(); 
        final String sysprop = "https.protocols";
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>()
            {
                public Object run() {
                    if (System.getProperty(sysprop) == null) {
                        System.out.println(orig+", System.setProperty: "+sysprop+"="+sysval);
                        System.setProperty(sysprop, sysval);
                    }
                    return null;
                }
            }
        );
    }

    /**
     * Null constructor for use by AdministeredObject when used as a PropertyOwner
     */ 
    public HTTPStreamHandler() {}

    public String[] getPropertyNames() {
        String [] propnames = new String [1];
        propnames[0] = ConnectionConfiguration.imqConnectionURL;
        return propnames;
    }

    public String getPropertyType(String propname) {
        if (ConnectionConfiguration.imqConnectionURL.equals(propname)) {
            return AdministeredObject.AO_PROPERTY_TYPE_STRING;
        }
        return null;
    }

    public String getPropertyLabel(String propname) {
        if (ConnectionConfiguration.imqConnectionURL.equals(propname)) {
            return (AdministeredObject.cr.L_JMQHTTP_URL);
        }
        return null;
    }

    public String getPropertyDefault(String propname) {
        if (ConnectionConfiguration.imqConnectionURL.equals(propname)) {
            return "http://localhost/imq/tunnel";
        }
        return null;
    }
 
    /**
     * Open socket a new connection.
     *
     * @param connection is the ConnectionImpl object.
     * @return a new instance of ConnectionHandler.
     * @exception throws IOException if socket creation failed.
     */
    public ConnectionHandler openConnection(
        Object connection) throws JMSException {
        return new HTTPConnectionHandler(connection);
    }

    public ConnectionHandler openConnection(
        MQAddress addr, ConnectionImpl connection) throws JMSException {
        return new HTTPConnectionHandler(addr, connection);
    }

}

/*
 * EOF
 */
