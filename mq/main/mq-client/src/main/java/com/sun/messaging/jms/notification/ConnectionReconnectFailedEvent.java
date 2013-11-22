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
 * @(#)ConnectionReconnectFailedEvent.java	1.4 07/02/07
 */ 

package com.sun.messaging.jms.notification;

import com.sun.messaging.jms.Connection;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;
import javax.jms.JMSException;

/**
 * MQ Connection Reconnect Failed Event is generated and delivered to the
 * event listener if a MQ reconnect failed and an event listener is
 * set to the MQ connection.
 * <p>
 * The application can also obtain the current broker's address from the API
 * defined in the ConnectionEvent.
 */
public class ConnectionReconnectFailedEvent extends ConnectionEvent {

    // if there is any exception that caused the connection to be closed,
    //it is set to this event.
    private JMSException exception = null;

    /**
     * Connection reconnect failed event code.
     */
    public static final String CONNECTION_RECONNECT_FAILED =
                  ClientResources.E_CONNECTION_RECONNECT_FAILED;

    /**
     * Connection reconnect failed event code - reconnect to the
     * same broker failed.
     */
    //public static final String CONNECTION_RECONNECT_FAILED_SAME_BROKER =
    //              ClientResources.E_CONNECTION_RECONNECT_FAILED_SAME_BROKER;

    /**
     * Connection reconnect event code - reconnect to a different
     * broker failed.
     */
    //public static final String CONNECTION_RECONNECT_FAILED_DIFF_BROKER =
    //              ClientResources.E_CONNECTION_RECONNECT_FAILED_DIFF_BROKER;

    /**
     * Construct a connection reconnect failed event associated with the
     * specified connection.
     *
     * @param conn the connection associated with the reconnect event.
     *             MQ may automatically reconnect to the same broker
     *             or a different broker depends on the client runtime
     *             configuration.
     * @param evCode the event code that represents this event object.
     * @param evMessage the event message that describes this event object.
     * @param jmse the JMSException that caused this event.

     */
    public ConnectionReconnectFailedEvent
    (Connection conn, String evCode, String evMessage, JMSException jmse) {

        super (conn, evCode, evMessage);

        this.exception = jmse;
    }

    /**
     * Get the JMSException that caused the connection to be closed.
     *
     * @return the JMSException that caused the connection to be closed.
     *         return null if no JMSException associated with this event,
     *         such as connection closed caused by admin requested shutdown.
     */
    public JMSException getJMSException() {
        return exception;
    }

}
