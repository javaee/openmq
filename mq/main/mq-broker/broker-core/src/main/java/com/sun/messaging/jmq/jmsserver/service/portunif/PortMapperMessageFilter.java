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

package com.sun.messaging.jmq.jmsserver.service.portunif;

import java.util.Properties;
import java.io.IOException;
import java.nio.charset.Charset;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.portunif.PUFilter;
import org.glassfish.grizzly.portunif.PUProtocol;
import com.sun.messaging.portunif.PUService;
import com.sun.messaging.portunif.PortMapperProtocolFinder;
import com.sun.messaging.portunif.PUServiceCallback;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.PortMapper;


public class PortMapperMessageFilter extends BaseFilter {
   
    protected static boolean DEBUG = false;

    /**
     * @param ctx Context of {@link FilterChainContext} processing
     * @return the next action
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleRead(final FilterChainContext ctx)
    throws IOException {

        final Buffer input = ctx.getMessage();

        String data = input.toStringContent(Charset.forName("UTF-8"));
        if (DEBUG) {
            Globals.getLogger().log(Logger.INFO,
            "PortMapperMessageFilter.handleRead called with data="+data+
            " from connection "+ctx.getConnection());
        }
        input.tryDispose();

        return ctx.getInvokeAction();
    }

    /**
     * @param ctx Context of {@link FilterChainContext} processing
     * @return the next action
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleWrite(final FilterChainContext ctx)
    throws IOException {

        final Buffer output = ctx.getMessage();
        output.flip();

        if (DEBUG) {
            Globals.getLogger().log(Logger.INFO,
            "PortMapperMessageFilter.handleWrite called with data size "+output.remaining()+
            " for connection "+ctx.getConnection());
        }
        ctx.setMessage(output);

        return ctx.getInvokeAction();
    }

    public static PUProtocol configurePortMapperProtocol(
        PUService pu, 
        PUServiceCallback cb) throws IOException {

        final FilterChain pmProtocolFilterChain =
                pu.getPUFilterChainBuilder()
                    .add(new PortMapperMessageFilter())
                    .add(new PortMapperServiceFilter(false))
                    .build();
        return new PUProtocol(new PortMapperProtocolFinder(cb, false),
                              pmProtocolFilterChain);
    }

    public static PUProtocol configurePortMapperSSLProtocol(
        PUService pu, 
        PUServiceCallback cb, 
        Properties sslprops, boolean clientAuthRequired)
        throws IOException {

        if (!pu.initializeSSL(sslprops, clientAuthRequired, cb, 
                 Globals.getPoodleFixEnabled(),
                 Globals.getKnownSSLEnabledProtocols("PortMapper"))) {
            throw new IOException(
            "Unexpected: Someone initialized SSL PUService before PortMapper service");
        }

        final FilterChain pmSSLProtocolFilterChain =
                pu.getSSLPUFilterChainBuilder()
                    .add(new PortMapperMessageFilter())
                    .add(new PortMapperServiceFilter(true))
                    .build();
        return new PUProtocol(new PortMapperProtocolFinder(cb, true),
                              pmSSLProtocolFilterChain);
    }
}

