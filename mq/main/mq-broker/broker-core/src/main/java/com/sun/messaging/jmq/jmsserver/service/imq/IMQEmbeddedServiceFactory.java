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
 * @(#)IMQEmbeddedServiceFactory.java	10/28/08
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import com.sun.messaging.jmq.jmsserver.service.*;

import java.util.*;
import java.io.*;

import com.sun.messaging.jmq.jmsserver.config.BrokerConfig;
import com.sun.messaging.jmq.jmsserver.config.ConfigListener;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

import com.sun.messaging.jmq.jmsserver.Globals;

import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.jmsserver.net.tcp.*;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.util.log.Logger;

public class IMQEmbeddedServiceFactory extends ServiceFactory
{

    protected static final Logger logger = Globals.getLogger();

    protected BrokerConfig props = Globals.getConfig();

    protected int DEFAULT_DESTROY_TIMEOUT=30;

    @Override
    public void checkFactoryHandlerName(String handlerName)
    throws IllegalAccessException {
        String myname = "mqdirect";
        if (!myname.equals(handlerName)) {
            throw new IllegalAccessException(
            "Unexpected service Handler name "+handlerName+", expected "+myname);
        }
    }

    public  void updateService(Service s)
        throws BrokerException
    {
        IMQService ss = (IMQService)s;
        String name = s.getName();

        // set changes to the service
        int newmin = getThreadMin(name);
        int newmax = getThreadMax(name);
        try {
        ss.setMinMaxThreadpool(newmin, newmax);
        } catch (IllegalArgumentException e) {
            throw new BrokerException(
                     Globals.getBrokerResources().getKString(
                         BrokerResources.X_THREADPOOL_BAD_SET,
                         String.valueOf(newmin),
                         String.valueOf(newmax)),
                     e);
        }

        // Register port with portmapper
        Globals.getPortMapper().addService(name, "none",
            props.getProperty(SERVICE_PREFIX + name + ".servicetype"),
            0, ss.getServiceProperties());
        
    }

// XXX - this is not optimized, but it should rarely happen

    public  void startMonitoringService(Service s)
        throws BrokerException {

        String name = s.getName();

        // add min/max properties
        String bstr = SERVICE_PREFIX + name + ".min_threads";
        props.addListener(bstr, this);


        bstr = SERVICE_PREFIX + name + ".max_threads";
        props.addListener(bstr, this);
    }

    public  void stopMonitoringService(Service s)   
        throws BrokerException
    {
        String name = s.getName();

        // remove min/max properties
        String bstr = SERVICE_PREFIX + name + ".min";
        props.removeListener(bstr, this);


        bstr = SERVICE_PREFIX + name + ".max";
        props.removeListener(bstr, this);
    }


    public  void validate(String name, String value)
        throws PropertyUpdateException {
        // for now, dont bother with validation
    }

    public  boolean update(String name, String value) 
    {

        return true;
    }

    protected int getThreadMin(String instancename) 
    {
        String bstr = SERVICE_PREFIX + instancename + ".min_threads";
        return props.getIntProperty(bstr); 
    }

    protected int getPoolTimeout(String instancename) 
    {
        String bstr = SERVICE_PREFIX + instancename + ".destroy_timeout";

        // get timer and covert to seconds
        return props.getIntProperty(bstr,DEFAULT_DESTROY_TIMEOUT )*1000; 
    }

    protected int getThreadMax(String instancename) 
    {
        String bstr = SERVICE_PREFIX + instancename + ".max_threads";
        return props.getIntProperty(bstr); 
    }

    public Service createService(String instancename, int type) 
        throws BrokerException
    {
        if (DEBUG) {
            logger.log(Logger.DEBUG, " Creating new Service("+ instancename +
                  ": Embedded )");
        }

        Service svc = new IMQEmbeddedService(instancename,
                type, Globals.getPacketRouter(type), getThreadMin(instancename),
                getThreadMax(instancename)); 

        // bug 4433282 -> support optional timeout for pool
        long timeout = getPoolTimeout(instancename);
        if (timeout > 0)
               ((IMQService)svc).setDestroyWaitTime(timeout);
        return svc;
 
    }

}
/*
 * EOF
 */
