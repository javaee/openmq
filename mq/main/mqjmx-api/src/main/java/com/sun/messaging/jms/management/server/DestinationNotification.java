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
 * @(#)DestinationNotification.java	1.6 07/02/07
 */ 

package com.sun.messaging.jms.management.server;

import javax.management.Notification;

/**
 * Class containing information on consumer notifications.
 *
 * <P>
 * The MQ specific fields in this notification is TBD.
 */
public class DestinationNotification extends MQNotification  {
    /** 
     * A destination was compacted.
     */
    public static final String		DESTINATION_COMPACT = MQNotification.PREFIX + "destination.compact";

    /** 
     * A destination was created.
     */
    public static final String		DESTINATION_CREATE = MQNotification.PREFIX + "destination.create";

    /** 
     * A destination was destroyed.
     */
    public static final String		DESTINATION_DESTROY = MQNotification.PREFIX + "destination.destroy";

    /** 
     * A destination was paused.
     */
    public static final String		DESTINATION_PAUSE = MQNotification.PREFIX + "destination.pause";

    /** 
     * A destination was purged.
     */
    public static final String		DESTINATION_PURGE = MQNotification.PREFIX + "destination.purge";

    /** 
     * A destination was resumed.
     */
    public static final String		DESTINATION_RESUME = MQNotification.PREFIX + "destination.resume";

    private String		destName;
    private String		destType;
    private String		pauseType;
    private boolean		createdByAdmin;

    /**
     * Creates a DestinationNotification object.
     *
     * @param type		The notification type.
     * @param source		The notification source.
     * @param sequenceNumber	The notification sequence number within the source object.
     */
    public DestinationNotification(String type, Object source, long sequenceNumber) {
	super(type, source, sequenceNumber);
    }

    public void setDestinationName(String name)  {
	destName = name;
    }
    public String getDestinationName()  {
	return(destName);
    }

    public void setDestinationType(String type)  {
	destType = type;
    }
    public String getDestinationType()  {
	return(destType);
    }

    public void setPauseType(String pauseType)  {
	this.pauseType = pauseType;
    }
    public String getPauseType()  {
	return(pauseType);
    }

    public void setCreatedByAdmin(boolean createdByAdmin)  {
	this.createdByAdmin = createdByAdmin;
    }
    public boolean getCreatedByAdmin()  {
	return(createdByAdmin);
    }

}
