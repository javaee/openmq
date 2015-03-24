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

package com.sun.messaging.jmq.jmsserver.service.portunif;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.CompletionHandler; 
import org.glassfish.grizzly.memory.MemoryManager;
import com.sun.messaging.jmq.jmsserver.service.PortMapper;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.util.log.Logger;


public class PortMapperServiceFilter extends BaseFilter {

    private PortMapper pm = null;

    private boolean ssl = false;

    public PortMapperServiceFilter(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     *
     * @param ctx Context of {@link FilterChainContext} processing
     * @return the next action
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleRead(final FilterChainContext ctx)
    throws IOException {

        final Logger logger = Globals.getLogger();

        if (ssl) {
            logger.log(logger.INFO, Globals.getBrokerResources().getKString(
                BrokerResources.I_PORTMAPPER_GOT_CONNECTION, "SSL/TLS",
                ctx.getConnection().getPeerAddress())); 
        }

        ByteArrayOutputStream bos =  new ByteArrayOutputStream();
        synchronized(this) {
            if (pm == null) {
                pm = Globals.getPortMapper();
                if (pm == null) {//XXX
                    throw new IOException("Broker portmapper not ready yet");
                }
            }
            pm.getPortMapTable().write(bos);
        }
        byte[] reply = bos.toByteArray();
        bos.close();

        if (PortMapperMessageFilter.DEBUG) {
            logger.log(logger.INFO, 
            "PortMapperServiceFilter.handleRead() write data size "+reply.length+
            " to connection "+ctx.getConnection());
        }

        final MemoryManager mm = ctx.getConnection().
                            getTransport().getMemoryManager();
        final Buffer output = mm.allocate(reply.length);
        output.put(reply);
        output.allowBufferDispose();

        final CloseCompletionHandler cch = new CloseCompletionHandler(ctx);
        ctx.write(output, new CompletionHandler<WriteResult>() {
                          @Override
                          public void cancelled(){
                          	ctx.getConnection().close(cch);
                          }

                          @Override
                          public void failed(Throwable t) {
                       	  	ctx.getConnection().close(cch);
                          }

                          @Override
                          public void completed(WriteResult w) {
                          	ctx.getConnection().close(cch);
                          }

                          @Override
                          public void updated(WriteResult w) {
                          }
		                  } );

        return ctx.getStopAction();
    }

    private static class CloseCompletionHandler implements CompletionHandler<Connection> {
        Logger logger = Globals.getLogger();
        FilterChainContext ctx = null;

        CloseCompletionHandler(final FilterChainContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void cancelled() {
            logger.log(logger.WARNING, "Close ["+ctx+"] connection cancelled");
        }

        @Override
        public void failed(Throwable t) {
            logger.logStack(logger.WARNING, "Close ["+ctx+"] connection failed", t);
        }

        @Override
        public void completed(Connection c) {
            logger.log(logger.DEBUGHIGH, "Close ["+ctx+":"+c+"] connection complete");
        }

        @Override
        public void updated(Connection c) { }
    }
}

