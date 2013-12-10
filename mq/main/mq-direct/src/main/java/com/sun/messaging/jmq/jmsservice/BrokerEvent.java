/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2011 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)BrokerEvent.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.jmsservice;

import java.util.EventObject;

/**
 *
 */
public class BrokerEvent extends EventObject {

    public static enum Type {
        READY,          // Broker *ready* after successful JMSBroker.start()
        PAUSED,         // Broker 'paused'
        RESUMED,        // Broker 'resumed'
        SHUTDOWN,       // imqcmd shutdown was executed
        RESTART,        // imqcmd restart was executed
        FATAL_ERROR,    // a fatal broker error occurred
        ERROR,          // a serious but non-fatal error occurred
        EXCEPTION,      // an uncaught throwable has been thrown
    }


    /**
     * Shutdown of the broker has been requested through imqcmd
     */
    //LKS public static final int REASON_SHUTDOWN = 0;

    /**
     * Restart of the broker has been requested through imqcmd
     */
    //LKS public static final int REASON_RESTART = 1;

    /**
     * A fatal error of the broker has occurred
     */
    //LKS public static final int REASON_FATAL = 2;

    /**
     * A serious (but non-fatal) error of the broker has occurred
     */
    //LKS public static final int REASON_ERROR = 3;

    /**
     * An uncaught throwable has been thrown
     */
    //LKS public static final int REASON_EXCEPTION = 4;

    /**
     * JMSBroker.stop() was called
     */
    //LKS public static final int REASON_STOP = 5;
    
    /**
     * The Id of this event
     */
    //LKS private int eventId;

    /**
     *  The type of this event
     */
    private BrokerEvent.Type eventType;
    
    /**
     * The message associated with this event
     */
    private String eventMessage;

    /** Creates a new instance of BrokerEvent with source and type */
    public BrokerEvent(Object source, BrokerEvent.Type type) {
        super (source);
        eventType = type;
    }

   /** Creates a new instance of BrokerEvent with source, type and info */
    public BrokerEvent(Object source, BrokerEvent.Type type, String msg) {
        super (source);
        eventType = type;
        eventMessage = msg;
    }

//------------------------------------------------------------------------------

    /**
     *  returns the Type of this event
     *
     *  @return the Type of this event
     */
    public Type getType(){
        return eventType;
    }

    /**
     *  returns the Name of the event Type
     *
     *  @return The name of the event type as declared
     */
    public String getName(){
        return eventType.name();
    }

    /**
     *  returns the Message associated with this event
     *
     *  @return The message associated with this event
     */
    public String getMessage(){
        return eventMessage;
    }

    public String toString() {
        String str = getName();
        if (eventMessage != null)
            str += " : " + eventMessage;
        return str;
    }

}
