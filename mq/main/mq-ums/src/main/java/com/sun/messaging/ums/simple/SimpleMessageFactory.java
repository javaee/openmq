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

package com.sun.messaging.ums.simple;

import com.sun.messaging.ums.service.UMSServiceImpl;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author chiaming
 */
public class SimpleMessageFactory {
    
    public static final String UTF8 =  "UTF-8";
    
    private static Logger logger = UMSServiceImpl.logger;
    
    public static SimpleMessage 
            createMessage (Map props, InputStream in) throws IOException {
        
        String body = readHttpBody(props, in);
        
        SimpleMessage message = new SimpleMessage(props, body);
        
        //message.setMessageProperties(props);
        
        //message.setText(body);
        
        return message;
    }
   
    
    public static String readHttpBody(Map props, InputStream in) throws IOException {
        
        String text = null;
        String enc = null;
        
        DataInputStream din = new DataInputStream(in);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] bytes = new byte[1024];

        boolean more = true;
        int len = 0;

        while (more) {

            len = din.read(bytes);

            if (len > 0) {
                baos.write(bytes, 0, len);
            } else if (len < 0) {
                more = false;
            }
        }

        byte[] body = baos.toByteArray();

        //String enc = req.getCharacterEncoding();
        
        if (enc == null) {
            enc = UTF8;
        }
        
        baos.close();
        din.close();

        text = new String(body, enc);
        return text;
    }

}
