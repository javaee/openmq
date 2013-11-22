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
 * @(#)BrokerState.java	1.11 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

import java.util.*;

/**
 * This class is a type-safe enum which contains the valid
 * lifecycle states for a broker.
 * It is used by ClusterConfig to set the state of the broker.
 * State is stored as an int in JDBC and used as a typesafe enum
 * within the code.
 * <p>
 * The states are as follows:
 *
 * <table border=1>
 *   <TR><TH>State</TH><TH>Description</TH><TH>Behavior</TH></TR>
 *   <TR>
 *       <TD>INITIALIZING</TD>
 *       <TD>a new broker is starting</TD>
 *       <TD>set by ClusterConfig.addBroker()</TD>
 *   </TR>
 *   <TR>
 *       <TD>OPERATING</TD>
 *       <TD>The broker is processing messages</TD>
 *       <TD>set by Broker at startup</TD>
 *   </TR>
 *   <TR>
 *       <TD>QUIESCE_STARTED</TD>
 *       <TD>The broker has started to quiesce, this means that
 *           new jms connections will not longer be accepted</TD>
 *       <TD>called by BrokerStateHandler.quiesce()</TD>
 *   </TR>
 *   <TR>
 *       <TD>QUIESCE_COMPLETED</TD>
 *       <TD>Quiesce has completed, this means that
 *            all jms connections have exited, the broker can not
 *           be safely shutdown.</TD>
 *       <TD>called by BrokerStateHandler.quiesce()</TD>
 *   </TR>
 *   <TR>
 *       <TD>SHUTDOWN_STARTED</TD>
 *       <TD>The broker is starting to shutdown. It may
 *           shutdown immediately or wait some period of time</TD>
 *       <TD>set by BrokerStateHandler.shutdown()</TD>
 *   </TR>
 *   <TR>
 *       <TD>SHUTDOWN_FAILOVER</TD>
 *       <TD>the broker has finished shutting down, all resources have
 *           been released it should be taken over
 *           (the broker may or may not shutdown, depending
 *           on whether it is running inprocess)</TD>
 *       <TD>set by BrokerStateHandler.shutdown()</TD>
 *   </TR>
 *   <TR>
 *       <TD>SHUTDOWN_COMPLETE</TD>
 *       <TD>the broker has finished shutting down, all resources have
 *           been released but is should NOT be taken over
 *           (the broker may or may not shutdown, depending
 *           on whether it is running inprocess)</TD>
 *       <TD>set by BrokerStateHandler.shutdown()</TD>
 *   </TR>
 *   <TR>
 *       <TD>FAILOVER_PENDING</TD>
 *       <TD>a takeover is pending (not stored to disk)
 *       <TD>set by the TakeoverThread of HAMonitorService()</TD>
 *   </TR>
 *   <TR>
 *       <TD>FAILOVER_STARTED</TD>
 *       <TD>another broker in the cluster has started to takeover
 *           the state of this broker (the broker handling takeover
 *           can be retrieved with HAClusteredBroker.getTakeoverBroker()</TD>
 *       <TD>set by the TakeoverThread of HAMonitorService()</TD>
 *   </TR>
 *   <TR>
 *       <TD>FAILOVER_COMPLETE</TD>
 *       <TD>takeover of a broker has completed</TD>
 *       <TD>set by the TakeoverThread of HAMonitorService()</TD>
 *   </TR>
 *   <TR>
 *       <TD>FAILOVER_FAILED</TD>
 *       <TD>a takeover failed (not stored to disk)
 *       <TD>set by the TakeoverThread of HAMonitorService()</TD>
 *   </TR>
 * </TABLE>
 * <P>
 * <b>NOTE:</b>
 *    <UL><LI>in the non-ha case, states remain in memory</LI>
 *        <LI>for HA, states are stored to the database</LI>
 *    </UL>
 *
 *<p>
 * <b>XXX-TBD:</b> should we store a string or letter not an int
 * in jdbc for readability.
 *
 * 
 */

public class BrokerState
{
    /**
     * descriptive string associated with the state
     */
    private final String name;


    /**
     * int value for the state used when the
     * state is stored in the jdbc store.
     */
    private final int value;

    /**
     * value for INITIALIZING used when the
     * state is stored in the jdbc store.
     */
    public static final int I_INITIALIZING=0;


    /**
     * value for OPERATING used when the
     * state is stored in the jdbc store.
     */
    public static final int I_OPERATING=1;

    /**
     * value for QUIESCE_STARTED used when the
     * state is stored in the jdbc store.
     */
    public static final int I_QUIESCE_STARTED=2;

    /**
     * value for QUIESCE_COMPLETE used when the
     * state is stored in the jdbc store.
     */
    public static final int I_QUIESCE_COMPLETED=3;

    /**
     * value for SHUTDOWN_STARTED used when the
     * state is stored in the jdbc store.
     */
    public static final int I_SHUTDOWN_STARTED=4;

    /**
     * value for SHUTDOWN_COMPLETE used when the
     * state is stored in the jdbc store.
     */
    public static final int I_SHUTDOWN_FAILOVER=5;

    /**
     * value for SHUTDOWN_COMPLETE used when the
     * state is stored in the jdbc store.
     */
    public static final int I_SHUTDOWN_COMPLETE=6;

    /**
     * value for FAILOVER_PENDING 
     */
    public static final int I_FAILOVER_PENDING=9;

    /**
     * value for FAILOVER_STARTED used when the
     * state is stored in the jdbc store.
     */
    public static final int I_FAILOVER_STARTED=7;

    /**
     * value for FAILOVER_COMPLETE used when the
     * state is stored in the jdbc store.
     */
    public static final int I_FAILOVER_COMPLETE=8;

    /**
     * value for FAILOVER_FALIED 
     */
    public static final int I_FAILOVER_FAILED=10; // not stored

    /**
     * value for FAILOVER_PROCESSED
     */
    public static final int I_FAILOVER_PROCESSED=11; //not stored

    /**
     * value for FAILOVER_PREPARED  //4.6, BDB store only 
     */
    public static final int I_FAILOVER_PREPARED=12;

    /**
     * mapping of jdbc values to BrokerStates
     */
    private static BrokerState[] bs =new BrokerState[13];

    /**
     * private constructor for BrokerState
     */
    private BrokerState(String name, int value) {
        this.name = name;
        this.value = value;
        bs[value]=this;
    }

    public static List getAllStates()
    {
        return Arrays.asList(bs);
    }

    /**
     * method which takes an int (retrieved from the
     * persistent store) and converts it to a state
     */
    public static final BrokerState getState(int value) 
    {
        return bs[value];
    }

    /**
     * method which returns the int value associated
     * with the state. This method should only be used when the
     * state is stored in the jdbc store or during the INFO_REQUEST
     * protocol.
     */
    public int intValue()
    {
        return value;
    }

    /**
     * a string representation of the object
     */
    public String toString() {
        return "BrokerState["+name+ "]";
    }

    /**
     * method which return true if it is in an active state
     */
    public boolean isActiveState() {
        boolean notRunning = (
            value == BrokerState.I_FAILOVER_COMPLETE ||
            value == BrokerState.I_SHUTDOWN_FAILOVER ||
            value == BrokerState.I_SHUTDOWN_COMPLETE );
        return !notRunning;
    }

    /**
     * State when a new broker is added to the cluster.
     */
    public static final BrokerState INITIALIZING = 
             new BrokerState("INITIALIZING",
                      I_INITIALIZING);

    /**
     * State used to indicate that the broker is actively processing
     * messages.
     */
    public static final BrokerState OPERATING = 
             new BrokerState("OPERATING",
                      I_OPERATING);


    /**
     * State when a broker has stopped accepting new connections.
     * This means that the broker is being prepared for a safe 
     * shutdown.
     */
    public static final BrokerState QUIESCE_STARTED = 
             new BrokerState("QUIESCE_STARTED",
                      I_QUIESCE_STARTED);


    /**
     * State when a broker has no remaining jms connections or
     * non-persistent messages after quiesce has been called. 
     * The broker may now be safely shutdown
     * without losing non-persistent messages.
     */
    public static final BrokerState QUIESCE_COMPLETED = 
             new BrokerState("QUIESCE_COMPLETED",
                      I_QUIESCE_COMPLETED);


    /**
     * The broker has started shutdown processing.
     */
    public static final BrokerState SHUTDOWN_STARTED = 
             new BrokerState("SHUTDOWN_STARTED",
                      I_SHUTDOWN_STARTED);

    /**
     * The broker has started shutdown processing.
     */
    public static final BrokerState SHUTDOWN_FAILOVER = 
             new BrokerState("SHUTDOWN_FAILOVER",
                      I_SHUTDOWN_FAILOVER);



    /**
     * The broker has completed shutdown processing and is about
     * to exit. 
     */
    public static final BrokerState SHUTDOWN_COMPLETE = 
             new BrokerState("SHUTDOWN_COMPLETE",
                      I_SHUTDOWN_COMPLETE);

    /**
     * Another broker is about to be taken over
     */
    public static final BrokerState FAILOVER_PENDING = 
             new BrokerState("FAILOVER_PENDING",
                      I_FAILOVER_PENDING);


    /**
     * Another broker has begun to take over this brokers store.
     */
    public static final BrokerState FAILOVER_STARTED = 
             new BrokerState("FAILOVER_STARTED",
                      I_FAILOVER_STARTED);


    /**
     * The failover of a broker has completed. A new broker is
     * now handling all of the brokers persistent state, this
     * includes all messages to non-local destinations and the
     * responsibility to reap transactions as necessary.
     */
    public static final BrokerState FAILOVER_COMPLETE = 
             new BrokerState("FAILOVER_COMPLETE",
                      I_FAILOVER_COMPLETE);

    /**
     * The store migration has been prepared by the target broker
     * - used only by BDB store
     */
    public static final BrokerState FAILOVER_PREPARED = 
             new BrokerState("FAILOVER_PREPARED",
                      I_FAILOVER_PREPARED);

    /**
     * The failover of a broker has been processed.
     */
    public static final BrokerState FAILOVER_PROCESSED = 
             new BrokerState("FAILOVER_PROCESSED",
                      I_FAILOVER_PROCESSED);
    /**
     * The failover of a broker failed for some reason (somone else
     * may have taken over the store)
     */
    public static final BrokerState FAILOVER_FAILED = 
             new BrokerState("FAILOVER_FAILED",
                      I_FAILOVER_FAILED);
}
