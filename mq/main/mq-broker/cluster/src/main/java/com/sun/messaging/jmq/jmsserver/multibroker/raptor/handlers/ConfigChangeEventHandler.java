/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)ConfigChangeEventHandler.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor.handlers;

import java.io.*;
import com.sun.messaging.jmq.util.*;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.*;

public class ConfigChangeEventHandler extends GPacketHandler {
    private static boolean DEBUG = false;

    public ConfigChangeEventHandler(RaptorProtocol p) {
        super(p);
    }

    public void handle(BrokerAddress sender, GPacket pkt) {
        if (DEBUG)
            logger.log(logger.DEBUG, "ConfigChangeEventHandler");

        if (pkt.getType() == ProtocolGlobals.G_CONFIG_CHANGE_EVENT) {
            handleConfigChangeEvent(sender, pkt);
        }
        else if (pkt.getType() ==
            ProtocolGlobals.G_CONFIG_CHANGE_EVENT_REPLY) {
            handleConfigChangeEventReply(sender, pkt);
        }
        else {
            logger.log(logger.WARNING, "ConfigChangeEventHandler " +
                "Internal error : Cannot handle this packet :" +
                pkt.toLongString());
        }
    }

    public void handleConfigChangeEvent(BrokerAddress sender,
        GPacket pkt) {
        Long xidProp = (Long) pkt.getProp("X");
        p.receiveConfigChangeEvent(sender, xidProp,
            pkt.getPayload().array());
    }

    public void handleConfigChangeEventReply(BrokerAddress sender,
        GPacket pkt) {
	//	Bug ID 6252184 Escalation ID 1-8243878
	//
	//	Backported by Tom Ross tom.ross@sun.com
	//
	//	14 April 2005
	// old line below
	// long xid = ((Long) pkt.getProp("X")).longValue();
	// new line below
	Long xid = (Long)pkt.getProp("X");
        int status = ((Integer)pkt.getProp("S")).intValue();
        String reason = (String)pkt.getProp("reason");

        p.receiveConfigChangeEventReply(sender, xid, status, reason);
    }
}


/*
 * EOF
 */
