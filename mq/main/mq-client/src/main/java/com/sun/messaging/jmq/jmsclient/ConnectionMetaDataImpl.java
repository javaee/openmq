/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)ConnectionMetaDataImpl.java	1.18 10/17/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import com.sun.messaging.*;

import java.util.Enumeration;
import java.util.Vector;

import com.sun.messaging.jmq.Version;

/* 
 * ConnectionMetaData provides information describing the Connection.
 */

public class ConnectionMetaDataImpl implements ConnectionMetaData {
    protected static final Version version = new Version();
    protected static final String JMSVersion = "2.0";
    protected static final int JMSMajorVersion = 2;
    protected static final int JMSMinorVersion = 0;
    public static final String JMSProviderName = version.getProductName();
    public static final String providerVersion = "5.1";
    protected static final int providerMajorVersion = 5;
    protected static final int providerMinorVersion = 1;

    public static final String JMSXGroupID = "JMSXGroupID";
    public static final String JMSXGroupSeq = "JMSXGroupSeq";

    public static final String JMSXAppID = "JMSXAppID";
    public static final String JMSXConsumerTXID = "JMSXConsumerTXID";
    public static final String JMSXProducerTXID = "JMSXProducerTXID";
    public static final String JMSXRcvTimestamp = "JMSXRcvTimestamp";
    public static final String JMSXUserID = "JMSXUserID";
    public static final String JMSXDeliveryCount = "JMSXDeliveryCount";

    protected boolean setJMSXAppID = false;
    protected boolean setJMSXConsumerTXID = false;
    protected boolean setJMSXProducerTXID = false;
    protected boolean setJMSXRcvTimestamp = false;
    protected boolean setJMSXUserID = false;

    protected Vector supportedProperties = new Vector(7);

    protected ConnectionImpl connection = null;

    protected ConnectionMetaDataImpl(ConnectionImpl connection) {
        //current connection
        this.connection = connection;
        init();
    }

    private void init() {
        String tmp = null;

        /* 
         * the following three properties are supported by default.
         */ 
        //set by client apps (optional)
        supportedProperties.addElement(JMSXGroupID); 
        //set by client apps (optional)
        supportedProperties.addElement(JMSXGroupSeq); 
        //set by JMS provider (JMS2.0 mandatory)
        supportedProperties.addElement(JMSXDeliveryCount); 

        //test if set app id requested
        tmp = connection.getProperty(ConnectionConfiguration.imqSetJMSXAppID);
        if ( Boolean.valueOf(tmp).booleanValue() ) {
            setJMSXAppID = true;
            supportedProperties.addElement(JMSXAppID);
        }

        //test if set consumer TX ID requested
        tmp = connection.getProperty(ConnectionConfiguration.imqSetJMSXConsumerTXID);
        if ( Boolean.valueOf(tmp).booleanValue() ) {
            setJMSXConsumerTXID = true;
            supportedProperties.addElement(JMSXConsumerTXID);
        }

        //test if set producer TX id requested
        tmp = connection.getProperty(ConnectionConfiguration.imqSetJMSXProducerTXID);
        if ( Boolean.valueOf(tmp).booleanValue() ) {
            setJMSXProducerTXID = true;
            supportedProperties.addElement(JMSXProducerTXID);
        }

        //test if set receive time stamp requested
        tmp = connection.getProperty(ConnectionConfiguration.imqSetJMSXRcvTimestamp);
        if ( Boolean.valueOf(tmp).booleanValue() ) {
            setJMSXRcvTimestamp = true;
            supportedProperties.addElement(JMSXRcvTimestamp);
        }

        //test if set user id requested
        tmp = connection.getProperty(ConnectionConfiguration.imqSetJMSXUserID);
        if ( Boolean.valueOf(tmp).booleanValue() ) {
            setJMSXUserID = true;
            supportedProperties.addElement(JMSXUserID);
        }

    }

    /** Get the JMS version.
      *
      * @return the JMS version.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public String
    getJMSVersion() throws JMSException {
        return JMSVersion;
    }


    /** Get the JMS major version number.
      *
      * @return the JMS major version number.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public int
    getJMSMajorVersion() throws JMSException {
        return JMSMajorVersion;
    }


    /** Get the JMS minor version number.
      *
      * @return the JMS minor version number.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public int
    getJMSMinorVersion() throws JMSException {
        return JMSMinorVersion;
    }


    /** Get the JMS provider name.
      *
      * @return the JMS provider name.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public String
    getJMSProviderName() throws JMSException {
        return JMSProviderName;
    }


    /** Get the JMS provider version.
      *
      * @return the JMS provider version.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public String
    getProviderVersion() throws JMSException {
        return providerVersion;
    }


    /** Get the JMS provider major version number.
      *
      * @return the JMS provider major version number.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public int
    getProviderMajorVersion() throws JMSException {
        return providerMajorVersion;
    }


    /** Get the JMS provider minor version number.
      *
      * @return the JMS provider minor version number.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the meta-data
      *                         retrieval.
      */

    public int
    getProviderMinorVersion() throws JMSException {
        return providerMinorVersion;
    }


    /** Get an enumeration of JMSX Property Names.
      *
      * @return an Enumeration of JMSX PropertyNames.
      *
      * @exception JMSException if some internal error occurs in
      *                         JMS implementation during the property
      *                         names retrieval.
      */

    public Enumeration
    getJMSXPropertyNames() throws JMSException {
        return supportedProperties.elements();
    }

}
