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

package com.sun.messaging.jmq.jmsserver;

import java.util.Iterator;
import java.util.List;

import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQDirectService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsservice.JMSRABroker;
import com.sun.messaging.jmq.jmsservice.JMSService;

/**
 * Wrapper used to start the broker. It wraps a singleton class
 * (only one broker can be running in any process).<P>
 *
 * <u>Example</u><P>
 * <code><PRE>
 *      BrokerProcess bp = BrokerProcess.getBrokerProcess();
 *      try {
 *      
 *          Properties ht = BrokerProcess.convertArgs(args);
 *          int exitcode = bp.start(true, ht, null);
 *          System.out.println("Broker exited with " + exitcode);
 *
 *      } catch (IllegalArgumentException ex) {
 *          System.err.println("Bad Argument " + ex.getMessage());
 *          System.out.println(BrokerProcess.usage());
 *      }
 * </PRE></code>
 */
public class JMSRA_BrokerProcess extends BrokerProcess implements JMSRABroker
{
    private static final String	DEFAULT_DIRECTMODE_SERVICE_NAME = "jmsdirect";

    public JMSRA_BrokerProcess() {
        super();
    }

    /**
     *  Return the default JMS Service that supports 'DIRECT' in-JVM Java EE JMS
     *  clients.
     *
     *  @throws IllegalStateException if the broker is already stopped
     * 
     */
    public JMSService getJMSService() 
			throws IllegalStateException  {
	ServiceManager sm = Globals.getServiceManager();
	JMSService jmsService = getJMSService(DEFAULT_DIRECTMODE_SERVICE_NAME);

	if (jmsService != null)  {
	    return (jmsService);
	}

	/*
	 * If "jmsdirect" is not available, loop through all services
	 */
	List serviceNames = sm.getAllServiceNames();
	Iterator iter = serviceNames.iterator();

	while (iter.hasNext())  {
	    jmsService = getJMSService((String)iter.next());

	    if (jmsService != null)  {
	        return (jmsService);
	    }
	}

	return (null);
    }

    /**
     *  Return the named JMS Service that supports 'DIRECT' in-JVM Java EEJMS
     *  clients.
     *
     *  @param  serviceName The name of the service to return
     *
     *  @throws IllegalStateException if the broker is already stopped
     */
    public JMSService getJMSService(String serviceName) 
				throws IllegalStateException  {
	ServiceManager sm = Globals.getServiceManager();
	Service svc;
	IMQService imqSvc;
	IMQDirectService imqDirectSvc;

	if (sm == null)  {
	    return (null);
	}

	svc = sm.getService(serviceName);

	if (svc == null)  {
	    return (null);
	}

	if (!(svc instanceof IMQService))  {
	    return (null);
	}

	imqSvc = (IMQService)svc;

	if (!imqSvc.isDirect())  {
	    return (null);
	}

	if (!(imqSvc instanceof IMQDirectService))  {
	    return (null);
	}

	imqDirectSvc = (IMQDirectService)imqSvc;

	return imqDirectSvc.getJMSService();
    }
    
}


