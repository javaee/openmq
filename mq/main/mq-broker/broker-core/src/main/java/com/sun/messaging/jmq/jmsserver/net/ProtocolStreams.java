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
 * @(#)ProtocolStreams.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net;


import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;
import java.nio.*;
import java.nio.channels.spi.*;
import java.nio.channels.*;

import java.util.Hashtable;

/**
 * This class handles the input and output streams
 * to a specific connection of a protocol (e.g. with
 * TCP this class will really be a socket and its output
 * streams).
 */
// NOTE this is currently identical to
// com.sun.messaging.jmq.jmsclient.ConnectionHandler
//
public interface ProtocolStreams
{
    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public void close() throws IOException;
    public int getLocalPort();
    public int getRemotePort();
    public InetAddress getLocalAddress();
    public InetAddress getRemoteAddress();

    /**
     *@deprecated
     */
    public int getInputBufferSize();

    /**
     *@deprecated
     */
    public int getOutputBufferSize();
    public String toString();
    public String toDebugString();

    public boolean getBlocking();
    public AbstractSelectableChannel getChannel();

    public Hashtable getDebugState();
}
    

