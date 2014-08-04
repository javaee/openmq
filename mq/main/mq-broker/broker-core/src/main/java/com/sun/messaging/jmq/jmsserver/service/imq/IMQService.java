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
 * @(#)IMQService.java	1.56 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq;

import java.io.*;

import com.sun.messaging.jmq.jmsserver.service.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.auth.AuthCacheData;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.net.*;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.ServiceState;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import java.util.*;




public abstract class IMQService implements Service
{

    protected static boolean DEBUG = false;

    /**
     * the list of connections which this service knows about
     */
    protected ConnectionManager connectionList = Globals.getConnectionManager();

    private boolean serviceRunning = false;
    private boolean shuttingDown = false;

    private int state = ServiceState.UNINITIALIZED;
    private int type = ServiceType.NORMAL;

    protected Logger logger = Globals.getLogger();

    protected String name = null;
    private AuthCacheData authCacheData = new AuthCacheData();

    protected static final long DESTROY_WAIT_DEFAULT = 30000;

    private long serviceDestroyWait = DESTROY_WAIT_DEFAULT;
 
    HashMap serviceprops = null;

    private HashSet serviceress =  new HashSet();
    private ArrayList serviceressListeners =  new ArrayList();


    public IMQService(String name, int type)
    {
        this.name = name;
        this.type = type;
    }

    protected boolean getDEBUG() { 
        return DEBUG;
    }

    protected void addServiceProp(String name, String value) {
        if (serviceprops == null)
            serviceprops = new HashMap();
        serviceprops.put(name, value);
    }

    public void resetCounters()
    {
        List cons = connectionList.getConnectionList(this);
        Iterator itr = cons.iterator();
        while (itr.hasNext()) {
            ((IMQConnection)itr.next()).resetCounters();
        }
    }

    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("name", name);
        ht.put("state", ServiceState.getString(state));
        ht.put("shuttingDown", String.valueOf(isShuttingDown()));
        if (serviceprops != null)
            ht.put("props", new Hashtable(serviceprops));
        ht.put("connections", connectionList.getDebugState(this));
        return ht; 
    }
    public Hashtable getPoolDebugState() {
        return (new Hashtable());
    }


    /*
    public void dumpPool()  {
        pool.debug();
    }
    */

    public int size() {
        List list = connectionList.getConnectionList(this);
        return list.size();
    }
    public List getConsumers() {
        ArrayList list = new ArrayList();
        List cons = connectionList.getConnectionList(this);
        Iterator itr = cons.iterator();
        while (itr.hasNext()) {
            List newList = ((IMQConnection)itr.next()).getConsumers();
            list.addAll(newList);
        }
        return list;
    }

    public List getProducers() {
        ArrayList list = new ArrayList();
        List cons = connectionList.getConnectionList(this);
        Iterator itr = cons.iterator();
        while (itr.hasNext()) {
            List newList = ((IMQConnection)itr.next()).getProducers();
            list.addAll(newList);
        }
        return list;
    }

    public Protocol getProtocol() {
        return (null);
    }

    public String getName() {
        return name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
    
    public int getServiceType() {
        return type;
    }

    public synchronized int getMinThreadpool() {
        return (0);
    }

    public synchronized int getMaxThreadpool() {
        return (0);
    }

    public synchronized int getActiveThreadpool() {
        return (0);
    }

    public void setPriority(int priority) {
    }

    /**
     * @return int[0] min; int[1] max; -1 or null no change
     */
    public synchronized int[] setMinMaxThreadpool(int min, int max) {
        return null;
    }

    public void setDestroyWaitTime(long value) {
        serviceDestroyWait = value;
    }

    public long getDestroyWaitTime() {
        return(serviceDestroyWait);
    }

    public void setServiceRunning(boolean value)  {
	serviceRunning = value;
    }

    public boolean isServiceRunning()  {
	return(serviceRunning);
    }

    public void setShuttingDown(boolean value)  {
	shuttingDown = value;
    }

    public boolean isShuttingDown()  {
	return(shuttingDown);
    }

    public void stopNewConnections() 
        throws IOException, IllegalStateException
    {
        if (state != ServiceState.RUNNING) {
            throw new IllegalStateException(
               Globals.getBrokerResources().getKString(
                   BrokerResources.X_CANT_STOP_SERVICE));
        }
        state = ServiceState.QUIESCED;
    }

    public void startNewConnections() 
        throws IOException
    {
        if (state != ServiceState.QUIESCED && state != ServiceState.PAUSED) {
            throw new IllegalStateException(
               Globals.getBrokerResources().getKString(
                   BrokerResources.X_CANT_START_SERVICE));
        }

        synchronized (this) {
            setState(ServiceState.RUNNING);
            this.notifyAll();
        }
    }

    public void destroyService() {
        if (getState() < ServiceState.STOPPED)
            stopService(true);
        synchronized (this) {
            setState(ServiceState.DESTROYED);
            this.notifyAll();
        }

    }

    public void updateService(int port, int min, int max) 
    throws IOException, PropertyUpdateException, BrokerException {
    }

    public AuthCacheData getAuthCacheData() {
        return authCacheData;     
    }  

    public void removeConnection(ConnectionUID uid, int reason, String str) {
         connectionList.removeConnection(uid,
             true, reason, str);
    }

    public HashMap getServiceProperties() {
        return serviceprops;
    }

    public boolean isDirect() {
        return (false);
    }

    public String toString() {
        return getName();
    }

    public void addServiceRestriction(ServiceRestriction sr) {
        synchronized(serviceress) {
            serviceress.add(sr);
            notifyServiceRestrictionChanged();
        }
    }

    public void removeServiceRestriction(ServiceRestriction sr) {
        synchronized(serviceress) {
            serviceress.remove(sr);
            notifyServiceRestrictionChanged();
        }
    }

    public ServiceRestriction[] getServiceRestrictions() {
        synchronized(serviceress) {
            return (ServiceRestriction[])serviceress.toArray(
                   new ServiceRestriction[serviceress.size()]);
        }
    }

    public void addServiceRestrictionListener(ServiceRestrictionListener l) {
        synchronized(serviceress) {
            serviceressListeners.add(l);
        }
    }

    public void removeServiceRestrictionListener(ServiceRestrictionListener l) {
        synchronized(serviceress) {
            serviceressListeners.remove(l);
        }
    }

    private void notifyServiceRestrictionChanged() {
        synchronized(serviceress) {
            Iterator itr = serviceressListeners.iterator();
            ServiceRestrictionListener l = null;
            while (itr.hasNext()) {
                l = (ServiceRestrictionListener)itr.next();
                l.serviceRestrictionChanged(this);
            }
        }
    }

}

