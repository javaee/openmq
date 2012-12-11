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

package com.sun.messaging.jms.ra;

import javax.jms.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;

import com.sun.messaging.jmq.jmsservice.JMSService;

/**
 *
 */
public abstract class ConnectionMetaData implements javax.jms.ConnectionMetaData {

    /**
     *  Holds the configuration properties of this JMS Connection
     */
    protected Properties connectionProps;

    /**
     *  Holds the JMSX property names that are supported by this JMS Connection
     *  as required by the JMS Specification. The names are returned by the
     *  {@code getJMSXPropertyNames} method.
     *
     *  {@link javax.jms.ConnectionMetaData#getJMSXPropertyNames}
     */
    private Vector <String> supportedProperties = new Vector <String> (7);
    
    /** Creates a new instance of ConnectionMetaData */
    public ConnectionMetaData(Properties connectionProps) {
        this.connectionProps = connectionProps;

        //The first two properties are supported by default and set by apps
        //if needed
        supportedProperties.addElement(
                JMSService.JMSXProperties.JMSXGroupID.toString());
        supportedProperties.addElement(
                JMSService.JMSXProperties.JMSXGroupSeq.toString());

        //The subsequent properties are supported *only* if the connection
        //is configured to support them
        if (hasJMSXAppID())
            supportedProperties.addElement(
                    JMSService.JMSXProperties.JMSXAppID.toString());
        if (hasJMSXUserID())
            supportedProperties.addElement(
                    JMSService.JMSXProperties.JMSXUserID.toString());
        if (hasJMSXProducerTXID())
            supportedProperties.addElement(
                    JMSService.JMSXProperties.JMSXProducerTXID.toString());
        if (hasJMSXConsumerTXID())
            supportedProperties.addElement(
                    JMSService.JMSXProperties.JMSXConsumerTXID.toString());
        if (hasJMSXRcvTimestamp())
            supportedProperties.addElement(
                    JMSService.JMSXProperties.JMSXRcvTimestamp.toString());
        supportedProperties.addElement(
            JMSService.JMSXProperties.JMSXDeliveryCount.toString());
    }

    protected abstract boolean hasJMSXAppID();
    protected abstract boolean hasJMSXUserID();
    protected abstract boolean hasJMSXProducerTXID();
    protected abstract boolean hasJMSXConsumerTXID();
    protected abstract boolean hasJMSXRcvTimestamp();

    /////////////////////////////////////////////////////////////////////////
    //  Methods implementing javax.jms.ConnectionMetaData
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Returns the major version number of the JMS API that this JMS Connection
     *  implements.
     *
     *  @return The major version number of the JMS API that this JMS Connection
     *          implements.
     */
    public int getJMSMajorVersion() throws JMSException {
        return 2;
        //JMSMajorVersion; -> Version.getJMSMajorVersion();
    }

     /**
     *  Returns the minor version number of the JMS API that this JMS Connection
     *  implements.
     *
     *  @return The minor version number of the JMS API that this JMS Connection
     *          implements
     */
    public int getJMSMinorVersion() throws JMSException {
        return 0;
        //JMSMinorVersion; -> Version.getJMSMinorVersion();
    }

    /**
     *  Returns the JMS Provider Name for this JMS Connection
     *
     *  @return The JMS Provider Name for this JMS Connection
     */
    public String getJMSProviderName() throws JMSException {
        return "Oracle GlassFish(tm) Server Message Queue";
        //JMSProviderName; -> Version.getProductName();
    }

    /**
     *  Returns the JMS API Version for this JMS Connection
     *
     *  @return The JMS API Version for this JMS Connection
     */
    public String getJMSVersion() throws JMSException {
        return "2.0";
        //JMSVersion; -> Version.getTargetJMSVersion();
    }

    /**
     *  Returns the JMSX properties that this JMS Connection supports
     *
     *  @return The supported JMSX properties as an Enumeration
     */
    public Enumeration getJMSXPropertyNames() throws JMSException {
        return supportedProperties.elements();
    }

    /**
     *  Returns the JMS Provider's major version number for this JMS Connection
     *
     *  @return The JMS Provider's major version number for this JMS Connection
     */
    public int getProviderMajorVersion() throws JMSException {
        return 5;
        //ProviderMajorVersion; -> Version.getMajorVersion();
    }

    /**
     *  Returns the JMS Provider's minor version number for this JMS Connection
     *
     *  @return The JMS Provider's minor version number for this JMS Connection
     */
    public int getProviderMinorVersion() throws JMSException {
        return 0;
        //ProviderMinorVersion; -> Version.getMinorVersion();
    }

    /**
     *  Returns the JMS API Version for this JMS Connection
     *
     *  @return The JMS API Version for this JMS Connection
     */
    public String getProviderVersion() throws JMSException {
        return "5.0";
        //ProviderVersion; -> Version.getProviderVersion();
    }
    /////////////////////////////////////////////////////////////////////////
    // End methods implementing javax.jms.ConnectionMetaData
    /////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public Properties getConnectionProperties() {
        return this.connectionProps;
    }
}
