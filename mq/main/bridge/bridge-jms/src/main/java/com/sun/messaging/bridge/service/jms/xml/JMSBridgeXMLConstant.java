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

package com.sun.messaging.bridge.service.jms.xml;

import java.util.List;
import java.util.Arrays;

/**
 * @author amyk
 */
public class JMSBridgeXMLConstant
{
    public enum Common {
        ;
        public static final String NAME = "name";
        public static final String VALUE = "vaule";
        public static final String REFNAME = "ref-name";
    };

    public enum JMSBRIDGE {
        ;
        public static final String TAG_BRIDGENAME = "message-transfer-tag-bridge-name";
        public static final String LOG_MESSAGE_TRANSFER = "log-message-transfer";

        public static final String TAG_BRIDGENAME_DEFAULT = "false";
        public static final String LOG_MESSAGE_TRANSFER_DEFAULT = "true";

    };

    public enum CF {
        ;
        public static final String CFREF = "connection-factory-ref";
        public static final String REFNAME = "ref-name";

        public static final String LOOKUPNAME = "lookup-name";
        public static final String MULTIRM = "multi-rm";
        public static final String CLIENTID = "clientid";
        public static final String CONNECTATTEMPTS  = "connect-attempts";
        public static final String CONNECTATTEMPTINTERVAL = "connect-attempt-interval-in-seconds";
        public static final String IDLETIMEOUT = "idle-timeout-in-seconds";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

        public static final String MULTIRM_DEFAULT = "false";
        public static final String CONNECTATTEMPTS_DEFAULT  = "-1";
        public static final String CONNECTATTEMPTINTERVAL_DEFAULT = "5";
        public static final String IDLETIMEOUT_DEFAULT = "1800";
    };

    public enum Link {
        ;
        public static final String NAME = "name";
        public static final String ENABLED = "enabled";
        public static final String TRANSACTED = "transacted";

        public static final String ENABLED_DEFAULT = "true";
        public static final String TRANSACTED_DEFAULT = "true";
    };

    public enum Source {
        ;
        public static final String CFREF = "connection-factory-ref";
        public static final String DESTINATIONREF = "destination-ref";

        // properties
        public static final String SELECTOR = "selector";
        public static final String DURABLESUB = "durable-sub";
        public static final String CLIENTID = "clientid";

    };

    public enum Target {
        ;
        public static final String CFREF = "connection-factory-ref";
        public static final String DESTINATIONREF = "destination-ref";

        public static final String STAYCONNECTED = "stay-connected";
        public static final String RETAINREPLYTO = "retain-replyto";
        public static final String MTFCLASS = "message-transformer-class";
        public static final String CONSUMEONTRANSFORMERROR = "consume-no-transfer-on-transform-error";
        public static final String CLIENTID = "clientid";

        public static final String STAYCONNECTED_DEFAULT = "true";
        public static final String RETAINREPLYTO_DEFAULT = "false";
        public static final String CONSUMEONTRANSFORMERROR_DEFAULT = "false";
        public static final String DESTINATIONREF_AS_SOURCE = "AS_SOURCE";
    };

    public enum Destination {
        ;
        public static final String TYPE = "type";
        public static final String NAME = "name";
        public static final String LOOKUPNAME = "lookup-name";
        public static final String REFNAME = "ref-name";

        // constants
        public static final String QUEUE = "queue";
        public static final String TOPIC = "topic";
    };

    public enum DMQ {
        ;
        public static final String CFREF = "connection-factory-ref";
        public static final String DESTINATIONREF = "destination-ref";
        public static final String TIMETOLIVE = "time-to-live-in-millis";
        public static final String STAYCONNECTED = "stay-connected";
        public static final String CLIENTID = "clientid";
        public static final String ENABLED = "enabled";
        public static final String MTFCLASS = "message-transformer-class";
        public static final String SENDATTEMPTS = "send-attempts";
        public static final String SENDATTEMPTINTERVAL = "send-attempt-interval-in-seconds";

        public static final String STAYCONNECTED_DEFAULT = "true";
        public static final String TIMETOLIVE_DEFAULT = "0";
        public static final String ENABLED_DEFAULT = "true";
        public static final String SENDATTEMPTS_DEFAULT = "3";
        public static final String SENDATTEMPTINTERVAL_DEFAULT = "5";
    }

    public enum Element {
        ;
        public static final String JMSBRIDGE = "jmsbridge";
        public static final String LINK = "link";
        public static final String SOURCE = "source";
        public static final String TARGET = "target";
        public static final String DMQ = "dmq";
        public static final String DESTINATION = "destination";
        public static final String CF = "connection-factory";
        public static final String PROPERTY = "property";
        public static final String DESCRIPTION = "description";
    };

    private static List<String> _reservedNames = Arrays.asList(
                                DMQElement.BUILTIN_DMQ_NAME, 
                                DMQElement.BUILTIN_DMQ_DESTNAME, Target.DESTINATIONREF_AS_SOURCE);

    public static void checkReserved(String name)  throws IllegalArgumentException {
        if (name == null) return;

        if (_reservedNames.contains(name.trim()) ||
            _reservedNames.contains(name.trim().toUpperCase()) ||
            _reservedNames.contains(name.trim().toLowerCase())) {
            throw new IllegalArgumentException(name+" is reserved");
        }
    }
}
