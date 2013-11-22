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
 * @(#)AdminObjectConstants.java	1.3 07/02/07
 */ 

package com.sun.messaging.naming;

/**
 * This interface defines all constants used for admin objects
 * management.
 */
public interface AdminObjectConstants {

    /**
     * The following format is used for the reference object representing 
     * Destination objects.
     *
     * [0] = reserved for version
     * [1] = reserved for topicName
     *
     *
     * The following format is used for the reference object representing 
     * ConnectionFactory objects.
     *
     *  [0] = reserved for version
     *  [1] = reserved for securityPort
     *  [2] = reserved for JMSXUserID
     *  [3] = reserved for JMSXAppID
     *  [4] = reserved for JMSXProducerTXID
     *  [5] = reserved for JMSXConsumerTXID
     *  [6] = reserved for JMSXRcvTimestamp
     *  [7] = reserved for --
     *  [8] = reserved for host
     *  [9] = reserved for subnet
     * [10] = reserved for ackTimeout
     *
     */

    /** used by both Destination and ConnectionFactory reference objects */
    public static final String REF_VERSION = "version";
    
    /** used only by Destination reference objects */
    public static final String REF_DESTNAME = "destName";
   
    /** used only by ConnectionFactory reference objects */
    public static final String REF_SECURITYPORT = "securityPort";
    public static final String REF_JMSXUSERID = "JMSXUserID";
    public static final String REF_JMSXAPPID = "JMSXAppID";
    public static final String REF_JMSXPRODUCERTXID = "JMSXProducerTXID";
    public static final String REF_JMSXCONSUMERTXID = "JMSXConsumerTXID";
    public static final String REF_JMSXRCVTIMESTAMP = "JMSXRcvTimestamp";
    public static final String REF_PARM = "parm";
    public static final String REF_HOST = "host";
    public static final String REF_SUBNET = "subnet";
    public static final String REF_ACKTIMEOUT = "ackTimeout";

    /** the content of the parm, if the configuration object exists */
    public static final String REF_PARM_CONTENT = "--";
   
    /** JMSXxxx properties */
    public static final String JMSXUSERID = "JMSXUserID";
    public static final String JMSXAPPID = "JMSXAppID";
    public static final String JMSXPRODUCERTXID = "JMSXProducerTXID";
    public static final String JMSXCONSUMERTXID = "JMSXConsumerTXID";
    public static final String JMSXRCVTIMESTAMP = "JMSXRcvTimestamp";

    /** 
     * generic default value: if value is not specified in the reference 
     * object, its value defaults to this value 
     */
    public static final String DEFAULT = "default";

    /** the prefix to the attributes of the ConnectionFactyory objects */
    public static final String PREF_HOST = "-s";
    public static final String PREF_SUBNET = "-n";
    public static final String PREF_ACKTIMEOUT = "-t";

    /** default values for attributes */
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_SUBNET = 0;
    public static final int DEFAULT_SECURITYPORT = 22000;
    public static final int DEFAULT_ACKTIMEOUT = 30000;
}

