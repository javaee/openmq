/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsclient.protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.sun.messaging.jmq.io.ReadWritePacket;
import com.sun.messaging.jmq.jmsclient.ConnectionHandler;
import com.sun.messaging.jmq.jmsclient.Debug;

public abstract class SocketConnectionHandler implements ConnectionHandler {
	
    //default buffer size - String for use with system property.
    private static String defaultBufferSize = "2048";
	
    private boolean debug = Debug.debug;
	
    private InputStream is = null;
    private OutputStream os = null;
    
	protected abstract void closeSocket() throws IOException ;    
	
	public boolean isDirectMode(){
		return false;
	}
	
	public ReadWritePacket readPacket () throws IOException {
		ReadWritePacket pkt = new ReadWritePacket();
		pkt.readPacket(is);
		return pkt;
	}
	
	public void writePacket (ReadWritePacket pkt) throws IOException {
		pkt.writePacket(os);
	}
	
	public void configure(Properties configuration) throws IOException {
        //for output stream
        String prop = getProperty(configuration,"imqOutputBuffer", "true");
        if (prop.equals("true")) {
            String bufsize = getProperty(configuration,"imqOutputBufferSize", defaultBufferSize);
            int outSize = Integer.parseInt(bufsize);
            os = new BufferedOutputStream (getOutputStream(), outSize);
            if (debug) {
                Debug.println("buffered output stream, buffer size: " + outSize);
            }

        } else {
            os = getOutputStream();
        }

        //for input stream
        prop = getProperty(configuration,"imqInputBuffer", "true");
        if (prop.equals("true")) {
            String bufsize = getProperty(configuration,"imqInputBufferSize",
                "2048");
            int inSize = Integer.parseInt(bufsize);
            is = new BufferedInputStream (getInputStream(), inSize);

            if (debug) {
                Debug.println("buffered input stream, buffer size: " + inSize);
            }
        } else {
            is = getInputStream();
        }
	}
	
    /**
     * Returns a configuration property.
     * Uses a System property if non-existant and a default if
     * the System property doesn't exist.
     *
     * @param propname The key with which to retreive the property value.
     * @param propdefault The default value to be returned.
     *
     * @return The property value of the property key <code>propname</code>
     *         If the key <code>propname</code> does not exist, then if a System
     *         property named <code>propname</code> exists, return that, otherwise
     *         return the value <code>propdefault</code>.
     */
    private String getProperty(Properties configuration, String propname, String propdefault) {
        String propval = (String)configuration.get(propname);
        if (propval == null) {
            propval = System.getProperty(propname) ;
        }
        return (propval == null ? propdefault : propval);
    }	
	
	public void close() throws IOException {

	    	getInputStream().close();
	    	is.close();
	    	os.close();
	    	closeSocket();
	}


}

