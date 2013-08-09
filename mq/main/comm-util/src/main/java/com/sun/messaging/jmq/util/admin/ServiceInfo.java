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
 * @(#)ServiceInfo.java	1.8 06/29/07
 */ 

package com.sun.messaging.jmq.util.admin;

import com.sun.messaging.jmq.util.MetricCounters;

/**
 * ServiceInfo encapsulates information about a JMQ Broker service. It
 * is used to pass this information between the Broker and an
 * administration client.
 */
public class ServiceInfo extends AdminInfo implements java.io.Serializable {

    // Values that are only set by broker
    public String	name;
    public String	protocol;
    public int		type;
    public int		state;
    public int		nConnections;
    public int		currentThreads;
    public boolean      dynamicPort = false;
    public MetricCounters metrics;

    // Values that can be updated by client
    public int		port;
    public int		minThreads;
    public int		maxThreads;

    public static final int PORT            = 0x00000001;
    public static final int MIN_THREADS     = 0x00000002;
    public static final int MAX_THREADS     = 0x00000004;

    private int         updateMask = 0;

    /**
     * Constructor for ServiceInfo.
     *
     */
    public ServiceInfo() {
	reset();
    }

    public void reset() {
	name = null;
	protocol = null;
        type = 0;
	state = 0;
	port = 0;
	nConnections = 0;
	minThreads = 0;
	maxThreads = 0;
	currentThreads = 0;
        //metrics = null;

        resetMask();
    }

    /**
     * Return a string representation of the service. 
     *
     * @return String representation of the service.
     */
    public String toString() {

	return "{" + name + ":" +
		" port=" + port +
		" #connections=" + nConnections +
		" threads=" + currentThreads + "[" +
			minThreads + "," + maxThreads + "]" +
		" state=" + state + "}";
    }

    /**
     * Set the port the service is listening for connections on.
     *
     * @param port	Service's port number. 0 to have the broker
     *                  use a dynamic port.
     */
    public void setPort(int port) {
	this.port = port;
        setModified(PORT);
    }

    /**
     * Set the low water mark for the service's thread pool.
     *
     * @param n	Low water mark for service's thread pool
     */
    public void setMinThreads(int n) {
	this.minThreads = n;
        setModified(MIN_THREADS);
    }

    /**
     * Set the high water mark for the service's thread pool.
     *
     * @param n	High water mark for service's thread pool
     */
    public void setMaxThreads(int n) {
	this.maxThreads = n;
        setModified(MAX_THREADS);
    }

    /**
     * XXX dipol need to remove. Just here during transition period
     *    so admin won't break.
     *
     * Set the high water mark for the service's thread pool.
     *
     * @param n	High water mark for service's thread pool
     */
    public void setName(String name) {
	this.name = name;
    }

}
