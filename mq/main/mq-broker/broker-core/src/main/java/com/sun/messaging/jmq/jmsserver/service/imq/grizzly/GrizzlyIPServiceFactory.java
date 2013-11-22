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

package com.sun.messaging.jmq.jmsserver.service.imq.grizzly;


import java.util.Map;
import java.io.IOException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.net.Protocol;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQIPServiceFactory;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;


public class GrizzlyIPServiceFactory extends IMQIPServiceFactory
{

    public GrizzlyIPServiceFactory() {
    }

    @Override
    public void checkFactoryHandlerName(String handlerName)
    throws IllegalAccessException {
        String myname = "shared";
        if (!myname.equals(handlerName)) {
            throw new IllegalAccessException(
            "Unexpected service Handler name "+handlerName+", expected "+myname);
        }
    }

    @Override
    public void updateService(Service s) throws BrokerException {
        if (!(s instanceof GrizzlyIPService)) {
            throw new BrokerException(
                br.getKString(br.E_INTERNAL_BROKER_ERROR, 
                "Unexpected service class: "+s));
        }
        GrizzlyIPService ss = (GrizzlyIPService)s;
        try {
            ss.updateService(((GrizzlyProtocolImpl)ss.getProtocol()).getPort(),
                getThreadMin(s.getName()), getThreadMax(s.getName()), false);
        } catch (Exception e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public Service createService(String name, int type)
    throws BrokerException {
        IMQService s =  new GrizzlyIPService(name, type, Globals.getPacketRouter(type),
                                   getThreadMin(name), getThreadMax(name), this);
        long timeout = getPoolTimeout(name);
        if (timeout > 0) {
            s.setDestroyWaitTime(timeout);
        }
        return s;
    }

    protected IMQService createService(String instancename,
        Protocol proto, PacketRouter router, int type, int min, int max)
        throws IOException {
        throw new UnsupportedOperationException("Unexpected call");
    }

    @Override
    protected Map getProtocolParams(String protoname, String prefix) {
        return super.getProtocolParams(protoname, prefix);
    }

}

