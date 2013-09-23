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
 */ 

package com.sun.messaging.jms.notification;

import com.sun.messaging.Destination;
import com.sun.messaging.jms.Connection;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

/**
 * MQ Consumer Event.  
 * @since 4.5
 */
public class ConsumerEvent extends Event {

    /**
     * Consumer ready event code
     */
    public static final String CONSUMER_READY = ClientResources.E_CONSUMER_READY;


    /**
     * No consumer event code
     */
    public static final String CONSUMER_NOT_READY = ClientResources.E_CONSUMER_NOT_READY;


    /**
     * The broker address that sent the event
     */
    private String brokerAddress = null;

    /**
     * The connection object on which the event was received.
     */
    private transient Connection connection = null;


    /**
     * Construct a MQ consumer event.  
     *
     * <p><code>dest</code> is the {@link com.sun.messaging.Destination} 
     * object that was passed in 
     * {@link com.sun.messaging.jms.Connection#setConsumerEventListener}
     * and is what will be returned by {@link #getDestination()}
     *
     * <p><code>conn</code> is the {@link com.sun.messaging.jms.Connection} 
     * on which this event was received and is what will be returned by 
     * {@link #getConnection()}
     *
     * <p><code>evCode</code> is what will be returned by 
     * {@link #getEventCode()} which can be either 
     * {@link #CONSUMER_READY} or {@link #CONSUMER_NOT_READY}
     *
     * <p><code>evMessage</code> is a description of the <code>evCode</code>
     * and is what will be returned by {@link #getEventMessage()}
     *
     *
     * @param dest the destination on which the event occurred.
     * @param conn the connection on which the event was received
     * @param evCode the event code that represents this event object.
     * @param evMessage the event message that describes this event object.

     */
    public ConsumerEvent (Destination dest, Connection conn,
                          String evCode, String evMessage) {
        super (dest, evCode, evMessage);

        this.connection = conn;
        this.brokerAddress = conn.getBrokerAddress();
    }

    /**
     * Get the connection on which the event was received.
     * @return the connection on which the event was received.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Get the broker's address that sent the event.
     *
     * @return the broker's address that sent the event
     */
    public String getBrokerAddress() {
        return this.brokerAddress;
    }

    /**
     * Get the registered destination on which the event was occurred.
     * @return the registered destination on which the event was occurred.
     */
    public Destination getDestination() {
        return (Destination)this.getSource();
    }

}
