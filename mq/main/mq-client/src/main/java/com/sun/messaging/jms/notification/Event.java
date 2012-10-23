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
 * @(#)Event.java	1.3 07/02/07
 */ 

package com.sun.messaging.jms.notification;

/**
 * MQ Event.  This is the super class for all MQ notification
 * events. MQ may notify an application when a specific MQ event is
 * about to occur or occurred.
 * <p>
 */
public class Event extends java.util.EventObject {

    /**
     * MQ event code.
     */
    private String eventCode = null;

    /**
     * MQ event message.  An event message describes a MQ specific event.
     */
    private String eventMessage = null;

    /**
     * Construct a MQ event associated with the source specified.
     *
     * @param source the source associated with the event.
     * @param evCode the event code that represents the this event object.
     * @param evMessage the event message that describes this event object.
     */
    public Event (Object source, String evCode, String evMessage) {
        super (source);

        this.eventCode = evCode;
        this.eventMessage = evMessage;
    }

    /**
     * Get the event code associated with the MQ event.
     * @return the event code associated with the MQ event.
     */
    public String getEventCode() {
        return this.eventCode;
    }

    /**
     * Get the event message associated with the connection event.
     * @return the event message associated with the connection event.
     */
    public String getEventMessage() {
        return this.eventMessage;
    }

    /**
    * Returns a MQ event notification event message.  The format is as follows.
    * <p>
    * eventCode + ":" + eventMessage + ", " + source=" + source.toString().
    *
    * @return a String representation of this EventObject.
    */
    public String toString() {
        return this.getEventCode() + ":" + this.getEventMessage() + ", " +
            super.toString();
    }
}
