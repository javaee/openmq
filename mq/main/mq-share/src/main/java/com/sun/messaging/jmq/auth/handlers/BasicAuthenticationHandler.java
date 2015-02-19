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

/*
 * @(#)BasicAuthenticationHandler.java	1.10 06/27/07
 */ 

package com.sun.messaging.jmq.auth.handlers;

import java.io.*;
import java.util.Hashtable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.api.client.*;
import com.sun.messaging.jmq.util.BASE64Encoder;

/**
 * MQ basic authentication request handler
 */

public class BasicAuthenticationHandler implements AuthenticationProtocolHandler {

    private String username = null;
    private String password = null;

    public String getType() {
        return "basic";
    }

    public void init(String username, String password,
                     Hashtable authProperties) throws LoginException {
        this.username = username;
        this.password = password;
    }

    public byte[] handleRequest(byte[] authRequest, int sequence) 
                                throws LoginException {
        if (username == null || password == null) {
            throw new LoginException("null");
        }

        try {

        byte[] response;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeUTF(username); 

        BASE64Encoder encoder = new BASE64Encoder();
        String encodepass = encoder.encode(password.getBytes("UTF8"));
        dos.writeUTF(encodepass);
        dos.flush();
        response = bos.toByteArray();
        dos.close();
        return response;

        } catch (IOException e) {
        throw new LoginException("IOException: "+e.getMessage());  
        }
    }
}
