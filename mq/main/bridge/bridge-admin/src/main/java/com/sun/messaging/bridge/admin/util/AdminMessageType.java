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

package com.sun.messaging.bridge.admin.util;

import com.sun.messaging.jmq.util.admin.MessageType;
/**
 * This class describes MQ bridge admin protocol messages 
 */
public class AdminMessageType {

    public static final String JMQ_BRIDGE_ADMIN_DEST   = MessageType.JMQ_BRIDGE_ADMIN_DEST;

    public static enum PropName {   
        ;
        public static final String MESSAGE_TYPE    = MessageType.JMQ_MESSAGE_TYPE;  //Integer
        public static final String PROTOCOL_LEVEL  = MessageType.JMQ_PROTOCOL_LEVEL; //String
        public static final String INSTANCE_NAME   = MessageType.JMQ_INSTANCE_NAME; //String
        public static final String STATUS          = MessageType.JMQ_STATUS;         //Integer
        public static final String ERROR_STRING    = MessageType.JMQ_ERROR_STRING;   //String
       

        public static final String BRIDGE_NAME     = "JMQBridgeName";    //String
        public static final String BRIDGE_TYPE     = "JMQBridgeType";    //String
        public static final String LINK_NAME       = "JMQLinkName";      //String

        public static final String CMD_ARG         = "JMQCommandArg";    //String
        public static final String TARGET          = "JMQTarget";        //String

        public static final String LOCALE_LANG      = "JMQLocaleLanguage";  //String
        public static final String LOCALE_COUNTRY   = "JMQLocaleCountry";   //String
        public static final String LOCALE_VARIANT    = "JMQLocaleVariant";   //String

        //debug mode
        public static final String DEBUG    = "JMQDebug";   //Boolean
   
        public static final String ASYNC_STARTED    = "JMQAsyncStarted";   //Boolean

    }

    public static enum Type {
        ;
        public static final int NULL              = 0;

        public static final int DEBUG             = 16;
        public static final int DEBUG_REPLY       = 17;

        public static final int LIST              = 18;
        public static final int LIST_REPLY        = 19;

        public static final int PAUSE             = 20;
        public static final int PAUSE_REPLY       = 21;

        public static final int RESUME            = 22;
        public static final int RESUME_REPLY      = 23;

        public static final int START             = 24;
        public static final int START_REPLY       = 25;

        public static final int STOP              = 26;
        public static final int STOP_REPLY        = 27;

        public static final int HELLO             = MessageType.HELLO;      //28
        public static final int HELLO_REPLY       = MessageType.HELLO_REPLY; //29


        public static final int LAST              = 30;
    }

    public static final String[] names = {
        "NULL",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "TBD",
        "DEBUG",
        "DEBUG_REPLY",
        "LIST",
        "LIST_REPLY",
        "PAUSE",
        "PAUSE_REPLY",
        "RESUME",
        "RESUME_REPLY",
        "START",
        "START_REPLY",
        "STOP",
        "STOP_REPLY",
        "HELLO",
        "HELLO_REPLY",
    	"LAST"
    };

    /**
     */
    public static String getString(int type) {
        if (type < 0 || type >= Type.LAST) {
            return "INVALID_TYPE("+type+")";
        }
        return names[type] + "(" + type + ")";
    }

    /**
     *
     * The returned value can be used in the JMQProtocolLevel property of
     * the HELLO message 
     *
     * @return bridge admin protocol version
     */
    public static int getProtocolVersion() {
        return 440;
    }
}
