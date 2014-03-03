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
 * @(#)BrokerState.java	1.5 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

/**
 * Class containing information on broker states.
 */
public class BrokerState implements java.io.Serializable  {
    /** 
     * Unknown broker state.
     */
    public static final int	     UNKNOWN			= -1;

    /**
     * A broker has started and is operating normally.
     */
    public static final int          OPERATING			= 0;

    /**
     * The broker has started to takeover another broker's message store.
     * This applies to brokers that are part of a HA cluster.
     * 
     */
    public static final int          TAKEOVER_STARTED		= 1;

    /**
     * The broker has completed the takeover another broker's message store.
     * This applies to brokers that are part of a HA cluster.
     */
    public static final int          TAKEOVER_COMPLETE		= 2;

    /**
     * The broker has failed in the attempt to takeover another broker's message store.
     * This applies to brokers that are part of a HA cluster.
     */
    public static final int          TAKEOVER_FAILED		= 3;

    /**
     * The broker has started to quiesce.
     */
    public static final int          QUIESCE_STARTED		= 4;

    /**
     * The broker has finished quiescing.
     */
    public static final int          QUIESCE_COMPLETE		= 5;

    /**
     * The broker is starting to shutdown (either immediately or after a specific grace
     * period) or restart
     */
    public static final int          SHUTDOWN_STARTED		= 6;

    /**
     * The broker is down.
     */
    public static final int          BROKER_DOWN		= 7;
    
    /**
     * The broker is initializing.
     */
    public static final int          INITIALIZING		= 8;

    /*
     * Class cannot be instantiated
     */
    private BrokerState() {
    }

    public static String toString(int state)  {
	switch (state)  {
	case OPERATING:
	    return ("OPERATING");

	case TAKEOVER_STARTED:
	    return ("TAKEOVER_STARTED");

	case TAKEOVER_COMPLETE:
	    return ("TAKEOVER_COMPLETE");

	case TAKEOVER_FAILED:
	    return ("TAKEOVER_FAILED");

	case QUIESCE_STARTED:
	    return ("QUIESCE_STARTED");

	case QUIESCE_COMPLETE:
	    return ("QUIESCE_COMPLETE");

	case SHUTDOWN_STARTED:
	    return ("SHUTDOWN_STARTED");

	case BROKER_DOWN:
	    return ("BROKER_DOWN");
	    
	case INITIALIZING:
	    return ("INITIALIZING");

	default:
	    return ("UNKNOWN");
	}
    }
    
}
