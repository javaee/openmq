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

package com.sun.messaging.jmq.jmsserver.service.imq.grizzly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import java.util.List;

/**
 */
public class GrizzlyMQPacketDispatchFilter extends BaseFilter {

    private final Attribute<GrizzlyMQIPConnection>
            connAttr =
            Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(
               GrizzlyMQConnectionFilter.GRIZZLY_MQIPCONNECTION_ATTR);

    public GrizzlyMQPacketDispatchFilter() {
    }

    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Connection connection = ctx.getConnection();
        final GrizzlyMQPacketList packetList = ctx.getMessage();
        final List<Packet> list = packetList.getPackets();
        
        GrizzlyMQIPConnection conn = connAttr.get(connection);

        try {
            for (int i = 0; i < list.size(); i++) {
                final Packet packet = list.get(i);
                if (packet == null) {
                    Globals.getLogger().log(Logger.ERROR,
                        "Read null packet from connection "+connection);
                    throw new IOException("Null Packet");
                }
                conn.receivedPacket(packet);
                conn.readData();
            }
        } catch (BrokerException e) {
            Globals.getLogger().logStack(Logger.ERROR, 
                "Failed to process packet from connection "+connection, e);
            throw new IOException(e.getMessage(), e);
        } finally {
            // @TODO investigate. we can dispose buffer, only if nobody still use it asynchronously.
            packetList.recycle(true);
//            packetList.recycle(false);
        }

        return ctx.getInvokeAction();
    }
}
