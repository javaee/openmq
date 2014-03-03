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
 * @(#)ServiceTable.java	1.5 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.io.*;
import java.util.*;

/**
 * A table of ServiceEntries.
 */
public class ServiceTable {
    //private static boolean DEBUG = false;

    private String brokerInstanceName = "???";
    private String brokerVersion = "???";

    private Hashtable remoteServices = null;
    private String activeBroker = null;
    private Hashtable  table = null;

    public ServiceTable() {
        remoteServices = new Hashtable();
        table = new Hashtable();
    }

    /**
     * Add a service.
     */
    public void add(ServiceEntry e) {
        table.remove(e.getName());
        table.put(e.getName(), e);
    }

    /**
     * Find a service.
     */
    public ServiceEntry get(String name) {
        return (ServiceEntry) table.get(name);
    }

    /**
     * Get a hashtable containing the service name / ServiceEntry
     * pairs.
     */
    public Hashtable getServices() {
        return table;
    }

    /**
     * Get the address string for a particular service.
     *
     * @param service   Name of service to get port number for
     * @return         address string, or null if the port for
     * service is not known.
     */
    public String getServiceAddress(String service) {
        ServiceEntry se = (ServiceEntry)table.get(service);
        if (se == null) {
            return null;
        } else {
            return se.getAddress();
        }
    }

    /**
     * Get the address string for a particular service.
     *
     * @param type Service type.
     * @param protocol   Service protocol.
     * @return         address string, or null if the port for
     * service is not known.
     */
    public String getServiceAddress(String type, String protocol) {
        String addr = null;
        Enumeration e = table.elements();
        while (e.hasMoreElements()) {
            ServiceEntry se = (ServiceEntry) e.nextElement();
            if (se.getProtocol().equals(protocol) &&
                se.getType().equals(type)) {
                addr = se.getAddress();
                break;
            }
        }
        return addr;
    }

    /**
     * Remove a service.
     */
    public void remove(String name) {
        table.remove(name);
    }

    /**
     * Remove a service. 
     *
     * @param e ServiceEntry to remove.
     */
    public void remove(ServiceEntry e) {
        table.remove(e.getName());
    }

    /**
     * Set the broker instance name
     */
    public void setBrokerInstanceName(String brokerInstanceName) {
        this.brokerInstanceName = brokerInstanceName;
    }

    /**
     * Get the broker instance name
     */
    public String getBrokerInstanceName() {
        return brokerInstanceName;
    }

    /**
     * Set the broker version.
     */
    public void setBrokerVersion(String brokerVersion) {
        this.brokerVersion = brokerVersion;
    }

    /**
     * Get the broker version.
     */
    public String getBrokerVersion() {
        return brokerVersion;
    }

    /**
     * Add a remote service.
     */
    public void addRemoteService(String address) {
        remoteServices.put(address, address);
    }

    /**
     * Remove a remote service.
     */
    public void removeRemoteService(String address) {
        remoteServices.remove(address);
    }

    /**
     * Get the remote service list iterator.
     */
    public Hashtable getRemoteServices() {
        return remoteServices;
    }

    /**
     * Set the active broker address.
     */
    public void setActiveBroker(String address) {
        this.activeBroker = address;
    }

    /**
     * Get the active broker address.
     */
    public String getActiveBroker() {
        return activeBroker;
    }

    public void dumpServiceTable() {
        System.out.println("brokerInstanceName = " + brokerInstanceName);
        System.out.println("brokerVersion = " + brokerVersion);

        System.out.println("active broker = " + activeBroker);

        System.out.println("Remote Services :");
        Enumeration e = remoteServices.keys();
        while (e.hasMoreElements())
            System.out.println("\t" + (String) e.nextElement());

        System.out.println("Local Services :");
        e = table.elements();
        while (e.hasMoreElements())
            System.out.println("\t" +
                ((ServiceEntry) e.nextElement()).toString());
    }
}

/*
 * EOF
 */
