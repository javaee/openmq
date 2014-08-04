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
 * @(#)GroupServiceFactory.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.group;

import java.io.IOException;
import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.service.imq.*;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsserver.license.LicenseBase;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.resources.*;


public class GroupServiceFactory extends IMQIPServiceFactory
{
    private static boolean SHARED_ALLOWED = false;

    static {
        try {
            LicenseBase license = Globals.getCurrentLicense(null);
            SHARED_ALLOWED =license.getBooleanProperty(
                       license.PROP_ENABLE_SHAREDPOOL, false);

        } catch (BrokerException ex) {
            SHARED_ALLOWED = false;
        }
    }

    @Override
    public void checkFactoryHandlerName(String handlerName)
    throws IllegalAccessException {
        String myname1 = "shared_old";
        String myname2 = "group_old";
        if (!myname1.equals(handlerName) && !myname2.equals(handlerName)) {
            throw new IllegalAccessException(
            "Unexpected service Handler name "+handlerName+", expected "+myname1);
        }
    }

    public Service createService(String instancename, int type) 
        throws BrokerException
    {
        // see if we need to override properties
        if (!SHARED_ALLOWED) {

            Globals.getLogger().log(Logger.ERROR,
               BrokerResources.E_FATAL_FEATURE_UNAVAILABLE,
               Globals.getBrokerResources().getString(
                    BrokerResources.M_SHARED_THREAD_POOL)); 
            Broker.getBroker().exit(1,
               Globals.getBrokerResources().getKString(
                   BrokerResources.E_FATAL_FEATURE_UNAVAILABLE,
                   Globals.getBrokerResources().getString(
                        BrokerResources.M_SHARED_THREAD_POOL)),
               BrokerEvent.Type.FATAL_ERROR);
        }

        if (!Globals.getConfig().getBooleanProperty(Globals.IMQ +
                "." + instancename + ".override")) {
            Globals.getConfig().put(Globals.IMQ +
                "." + instancename + ".tcp.blocking", "false");
            Globals.getConfig().put(Globals.IMQ +
                "." + instancename + ".tcp.useChannels", "true");
        } else {
            Globals.getLogger().log(Logger.DEBUG,"Overriding shared properties for instance " + instancename);
       }
       return super.createService(instancename, type);

    }



    protected IMQService createService(String instancename, 
           Protocol proto, PacketRouter router, int type, 
           int min, int max)
        throws IOException
    {
        proto.configureBlocking(false);
        return new GroupService(instancename, proto, 
              type, router,  min, max);
    }

    

}
