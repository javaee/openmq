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
 * @(#)HTTPStreams.java	1.12 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net.http;

import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;

import java.net.*;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.Logger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.channels.spi.*;

import java.util.Hashtable;

/**
 * HTTP Input and Output streams.
 */
public class HTTPStreams implements ProtocolStreams
{
    private HttpTunnelSocket socket = null;
    private InputStream is = null;
    private OutputStream os = null;

    private int inputBufferSize = 2048;
    private int outputBufferSize = 2048;

    public HTTPStreams(HttpTunnelSocket soc)
    {
        socket = soc;
    }

    public HTTPStreams(HttpTunnelSocket soc, int inBufSz, int outBufSz)
    {
        socket = soc;
        inputBufferSize = inBufSz;
        outputBufferSize = outBufSz;
    }

    public boolean getBlocking() {
        return true;
    }


    public AbstractSelectableChannel getChannel() {
        return null;
    }


    public InputStream getInputStream() 
        throws IOException
    {
        if (socket == null) 
            throw new
                IOException("Can not get an input stream without a socket");

        if (is == null) {
            synchronized(this) {
                if (is == null) {
                     is = socket.getInputStream();
                     if (inputBufferSize > 0) {
                        is = new BufferedInputStream(is, inputBufferSize);
                     }
                 }
            }
        }
        return is;
    }

    public OutputStream getOutputStream() 
        throws IOException
    {
        if (socket == null) 
           throw new
               IOException("Can not get an output stream without a socket");

        if (os == null) {
            synchronized(this) {
                if (os == null) {
                    os = socket.getOutputStream();
                    if (outputBufferSize > 0) {
                         os = new BufferedOutputStream(os, outputBufferSize);
                    }
                }
            }
        }
        return os;
    }

    public synchronized void close() 
        throws IOException
    {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {}
            is = null;
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {}
            os = null;
        }
        socket.close();
        socket = null;
    }

    public int getLocalPort() {
        return socket.getConnId();
    }

    public int getRemotePort() {
        return -1;
    }


    public InetAddress getLocalAddress() {
        return null;
    }

    public InetAddress getRemoteAddress() {
        HttpTunnelSocket s = socket;
        if (s == null) return null;
        try {
        return s.getRemoteAddress();
        } catch (Exception e) {
        Globals.getLogger().log(Logger.WARNING, "HttpTunnelSocket - "+e.getMessage());
        return null;
        }
    }


    public String toString() {
        return "HTTP connection to " + socket;
    }

    public String toDebugString() {
        return "HTTP connection to " + socket;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }

    public Hashtable getDebugState() {
        if (socket != null) {
            return socket.getDebugState();
        }

        return new Hashtable();
    }
}
    
/*
 * EOF
 */
