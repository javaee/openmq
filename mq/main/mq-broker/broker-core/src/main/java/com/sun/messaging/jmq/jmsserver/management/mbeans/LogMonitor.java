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
 * @(#)LogMonitor.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.AttributeChangeNotification;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.Version;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;

public class LogMonitor extends MQMBeanReadOnly  {
    private static String[] logNotificationTypes = {
		    LogNotification.LOG_LEVEL_PREFIX + LogLevel.WARNING,
		    LogNotification.LOG_LEVEL_PREFIX + LogLevel.ERROR,
		    LogNotification.LOG_LEVEL_PREFIX + LogLevel.INFO
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    logNotificationTypes,
		    LogNotification.class.getName(),
	            mbr.getString(mbr.I_LOG_NOTIFICATIONS)
		    )
		};

    public LogMonitor()  {
	super();
    }

    public String getMBeanName()  {
	return ("LogMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_LOG_MON_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (null);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (null);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (notifs);
    }

    public void notifyLogMessage(String level, String msg)  {
	LogNotification n;
	n = new LogNotification(LogNotification.LOG_LEVEL_PREFIX + level, 
			this, sequenceNumber++);

	n.setMessage(msg);
	n.setLevel(level);

	sendNotification(n);
    }
}
