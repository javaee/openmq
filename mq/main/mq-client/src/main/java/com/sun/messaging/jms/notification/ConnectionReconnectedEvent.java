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
 * @(#)ConnectionReconnectedEvent.java	1.4 07/02/07
 */ 

package com.sun.messaging.jms.notification;

import com.sun.messaging.jms.Connection;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

/**
 * MQ Connection Reconnected Event is generated and delivered to the event
 * listener if the MQ client runtime reconnected to a broker and an event
 * listener is set to the MQ connection.
 * <p>
 * The application can obtain the current broker's address from the API
 * provided.
 */
public class ConnectionReconnectedEvent extends ConnectionEvent {

    /**
     * Connection reconnected event code.
     */
    public static final String CONNECTION_RECONNECTED =
                  ClientResources.E_CONNECTION_RECONNECTED;


    /**
     * Connection reconnect event code - reconnected to the same broker.
     */
    //public static final String CONNECTION_RECONNECTED_SAME_BROKER =
    //              ClientResources.E_CONNECTION_RECONNECTED_SAME_BROKER;
    /**
     * Connection reconnect event code - reconnected to a different broker.
     */
    //public static final String CONNECTION_RECONNECTED_DIFF_BROKER =
    //              ClientResources.E_CONNECTION_RECONNECTED_DIFF_BROKER;

    /**
     * Construct a connection reconnect event.
     *
     * @param conn the connection associated with this event object.
     * @param evCode the event code that represents this event object.
     * @param evMessage the event message that describes this event object.

     */
    public ConnectionReconnectedEvent
        (Connection conn, String evCode, String evMessage) {

        super (conn, evCode, evMessage);
    }

}
