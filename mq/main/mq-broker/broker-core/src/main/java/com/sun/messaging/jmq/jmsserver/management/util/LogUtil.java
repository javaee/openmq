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
 * @(#)LogUtil.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import com.sun.messaging.jmq.util.log.Logger;

public class LogUtil {

    public static String toExternalLogLevel(String internalLogLevel)  {
	if (internalLogLevel.equals("NONE"))  {
	    return (com.sun.messaging.jms.management.server.LogLevel.NONE);
	} else if (internalLogLevel.equals("ERROR"))  {
	    return (com.sun.messaging.jms.management.server.LogLevel.ERROR);
	} else if (internalLogLevel.equals("WARNING"))  {
	    return (com.sun.messaging.jms.management.server.LogLevel.WARNING);
	} else if (internalLogLevel.equals("INFO"))  {
	    return (com.sun.messaging.jms.management.server.LogLevel.INFO);
	} else  {
	    return (com.sun.messaging.jms.management.server.LogLevel.UNKNOWN);
	}
    }

    public static String toInternalLogLevel(String externalLogLevel)  {
	if (externalLogLevel.equals(com.sun.messaging.jms.management.server.LogLevel.NONE))  {
	    return ("NONE");
	} else if (externalLogLevel.equals(com.sun.messaging.jms.management.server.LogLevel.ERROR))  {
	    return ("ERROR");
	} else if (externalLogLevel.equals(com.sun.messaging.jms.management.server.LogLevel.WARNING))  {
	    return ("WARNING");
	} else if (externalLogLevel.equals(com.sun.messaging.jms.management.server.LogLevel.INFO))  {
	    return ("INFO");
	} else  {
	    return ("UNKNOWN");
	}
    }
}
