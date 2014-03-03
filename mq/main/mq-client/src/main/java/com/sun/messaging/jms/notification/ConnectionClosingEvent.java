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
 * @(#)ConnectionClosingEvent.java	1.3 07/02/07
 */ 

package com.sun.messaging.jms.notification;

import com.sun.messaging.jms.Connection;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

/**
 * MQ Connection closing Event.  This event is generated (if application
 * had set a connection event listener) when MQ client runtime received
 * a notification from MQ broker that a connection is about to be closed
 * due to a soft shutdown.
 */
public class ConnectionClosingEvent extends ConnectionEvent {

    /**
     * Connection closing event code - admin requested shutdown.
     */
    public static final String CONNECTION_CLOSING_ADMIN =
                               ClientResources.E_CONNECTION_CLOSING_ADMIN;

    private long closingTimePeriod = 0;

    /**
     * Construct a ConnectionClosingEvent object associated with the
     * connection specified.
     * @param conn the connection associated with the closing event.
     * @param evCode the event code that represents this event object.
     * @param evMessage the event message that describes this event object.
     * @param timePeriod the closing time period (in milli secs) since the
     *                   broker announces the connection is to be closed.
     */
    public ConnectionClosingEvent
        (Connection conn, String evCode, String evMessage, long timePeriod) {

        super (conn, evCode, evMessage);
        this.closingTimePeriod = timePeriod;
    }

    /**
     * Get the connection closing time period in milli seconds.  The time
     * period is calculated from the announcement time by broker.
     *
     * @return the closing time period.
     */
    public long getClosingTimePeriod() {
        return this.closingTimePeriod;
    }

}
