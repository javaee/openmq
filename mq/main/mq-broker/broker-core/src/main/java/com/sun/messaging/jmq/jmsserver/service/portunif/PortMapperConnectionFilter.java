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

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.transport.TCPNIOConnection;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.service.PortMapper;


public class PortMapperConnectionFilter extends BaseFilter {

    private PortMapper pm = null;

    /**
     * Method is called, when new {@link Connection} was
     * accepted by a {@link org.glassfish.grizzly.Transport}
     *
     * @param ctx the filter chain context
     * @return the next action to be executed by chain
     * @throws java.io.IOException
     */
    @Override
    public NextAction handleAccept(FilterChainContext ctx)
    throws IOException {
        synchronized(this) {
            if (pm == null) {
                pm = Globals.getPortMapper();
                if (pm == null) {//XXX
                    throw new IOException("Broker portmapper not ready yet");
                }
            }
        }
        Logger logger = Globals.getLogger();

        Connection c = ctx.getConnection(); 
        SocketAddress sa = null;
        if (c instanceof TCPNIOConnection) {
            sa = ((TCPNIOConnection)c).getPeerAddress();
            if (sa instanceof InetSocketAddress) {
                if (pm.isPeerAddressAllowed((InetSocketAddress)sa)) {
                    return ctx.getInvokeAction();
                }
                logger.log(logger.WARNING, Globals.getBrokerResources().getKString(
                    BrokerResources.W_REJECT_PEER_ADDRESS_ACCESS_PORTMAPPER, sa));
                c.close();
                return ctx.getStopAction();
            }
            logger.log(logger.WARNING, 
            "PortMapperConnectionFilter.handleAccept: unexpected SocketAddress class "+
             sa.getClass().getName()+": "+sa);
        } else {
            logger.log(logger.WARNING, 
            "PortMapperConnectionFilter.handleAccept: unexpected Grizzly Connection class "+
             c.getClass().getName()+": "+c);
        }
        c.close();
        return ctx.getStopAction();
    }
}
