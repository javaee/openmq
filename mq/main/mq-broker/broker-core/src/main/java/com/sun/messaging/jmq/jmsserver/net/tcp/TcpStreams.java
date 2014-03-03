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
 * @(#)TcpStreams.java	1.19 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net.tcp;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * This class handles the input and output streams
 * to a specific connection of a protocol (e.g. with
 * TCP this class will really be a socket and its output
 * streams).
 */

public class TcpStreams implements ProtocolStreams
{
    protected Socket socket = null;
    private volatile InputStream is = null;
    private volatile OutputStream os = null;
    protected boolean blocking = true;


    private int inputBufferSize = 0;
    private int outputBufferSize = 0;

    public TcpStreams(Socket soc)
        throws IOException
    {
        // Default to no buffering
        this(soc, true, 0, 0);
    }
    public boolean getBlocking() {
        return blocking;
    }


    public AbstractSelectableChannel getChannel() {
        if (socket == null) return null;
        return socket.getChannel();
    }

    public TcpStreams(Socket soc, boolean blocking, int inBufSz, int outBufSz)
        throws IOException
    {
        this.blocking = blocking;
        socket = soc;
        if (getChannel() != null)
            getChannel().configureBlocking(blocking);
             
        inputBufferSize = inBufSz;
        outputBufferSize = outBufSz;
    }

    public InputStream getInputStream() 
        throws IOException
    {
        if (socket == null) 
            throw new IOException( Globals.getBrokerResources().getString(
                BrokerResources.X_INTERNAL_EXCEPTION,"Can not get an input stream without a socket"));
         if (is == null) {
             synchronized(this) {
                 if (is == null) {
                     if (socket == null) return null;
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
            throw new IOException( Globals.getBrokerResources().getString(
                BrokerResources.X_INTERNAL_EXCEPTION,"Can not get an output stream without a socket"));
         if (os == null) {
            synchronized(this) {
                if (os == null) {
                    if (socket == null) return null;
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
        if (getChannel() != null)  {
            getChannel().close();
        }
        socket.close();
        socket = null;
  
    }

    public int getLocalPort() {
        if (socket == null) return 0;
        return socket.getLocalPort();
    }

    public int getRemotePort() {
        if (socket == null) return 0;
        return socket.getPort();
    }

    public InetAddress getLocalAddress() {
        if (socket == null) return null;
        return socket.getLocalAddress();
    }

    public InetAddress getRemoteAddress() {
        if (socket == null) return null;
        return socket.getInetAddress();
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }

    public String toString() {
        return "tcp connection to " + socket ;
    }
    public String toDebugString() {
        return toString() + socket + " inBufsz=" + inputBufferSize +
					       ",outBufSz=" + outputBufferSize;
    }

    public java.util.Hashtable getDebugState() {
        return new java.util.Hashtable();
    }
}
    

