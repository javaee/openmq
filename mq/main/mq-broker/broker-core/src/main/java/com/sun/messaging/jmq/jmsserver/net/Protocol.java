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
 * @(#)Protocol.java	1.15 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net;

import java.util.Map;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.spi.*;
import java.nio.channels.*;
import java.net.*;

/**
 * This interface class handles a specific type of protocol (e.g. tcp)
 */

public interface Protocol 
{
    public void registerProtocolCallback(ProtocolCallback cb, Object data);

    /**
     * The canPause() method is a temporary workaround for bugid
     * 4435336 for jmq2.0 fcs. The TCP and TLS (SSL) transports
     * always return "true". The HTTPProtocol class always returns
     * false.
     */
    public boolean canPause();

    public ProtocolStreams accept()  
        throws IOException;

    public AbstractSelectableChannel getChannel()
        throws IOException;

    public void configureBlocking(boolean blocking)
        throws UnsupportedOperationException,IOException;

    public void open() 
        throws IOException, IllegalStateException;

    public void close() 
        throws IOException, IllegalStateException;

    public boolean isOpen();

    public void checkParameters(Map params)
        throws IllegalArgumentException;

    /**
     * @return old params if param change cause rebind
     */
    public Map setParameters(Map params) throws IOException;

    public int getLocalPort();

    public String getHostName();

    /**
     * method to set the TCP no delay flag on all
     * sockets created if applicable
     */
    public void setNoDelay(boolean val);

    /**
     * method to set the socket timeout (if any)
     * 0 indicates no timeout
     */
    public void setTimeout(int time);

    /**
     * method to set the input buffer size for a connection
     */
    public void setInputBufferSize(int size);

    /**
     * method to set the output buffer size for a connection
     */
    public void setOutputBufferSize(int size);

    /**
     * method to get the input buffer size for a connection
     */
    public int getInputBufferSize();

    /**
     * method to get the output buffer size for a connection
     */
    public int getOutputBufferSize();

    public boolean getBlocking();
}
 

