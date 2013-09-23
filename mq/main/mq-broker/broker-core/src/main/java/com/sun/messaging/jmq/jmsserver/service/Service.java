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
 * @(#)Service.java	1.28 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service;

import java.util.*;
import java.io.*;

/**
 * This interface abstracts the basic methods for sending
 * and receiving data from a client<P>
 *
 * A service will generally have some sort of socket it uses
 * to talk to the outside world, and a threading scheme to handle
 * reading in messages from clients and sending messages back out 
 * to clients
 *
 * Each service will also implement its own Connection interface.
 */

public interface Service
{

    public String getName();

    public Hashtable getDebugState();

    public int getState();

    public int getServiceType();

    public List getProducers();
    public List getConsumers();

    public int size();

    /**
     * start the service running
     */
    public void startService(boolean startPaused);

    /**
     * stop and destroy the service
     * @param all if false, disallow new connections only
     */
    public void stopService(boolean all);

    /**
     * stop allowing new connections
     */
    public void stopNewConnections() 
            throws IOException;

    /**
     * allowing new connections
     */
    public void startNewConnections() 
            throws IOException;

    /**
     * pause the service
     * @param all if true, connections as well as the service
     *            should be paused
     */
    public void pauseService(boolean all);

    /**
     * resume a paused service
     */
    public void resumeService();


    /**
     * destroy a stopped service
     */
    public void destroyService();

    /**
     * cleans up a connection of the service
     */
     public void removeConnection(ConnectionUID con, int reason, String str);


    /**
     * add a service restriction
     */
    public void addServiceRestriction(ServiceRestriction sr);

    /**
     * remove a service restriction
     */
    public void removeServiceRestriction(ServiceRestriction sr);

    /**
     * get all service restrictions
     */
    public ServiceRestriction[] getServiceRestrictions();

    public void addServiceRestrictionListener(ServiceRestrictionListener l);

    public void removeServiceRestrictionListener(ServiceRestrictionListener l);
}
