/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)Destination.java	1.19 06/28/07
 */ 

package com.sun.messaging;

import com.sun.messaging.jmq.DestinationName;
import com.sun.messaging.jmq.ClientConstants;
import java.util.Properties;
import javax.jms.*;

/**
 * A <code>Destination</code> encapsulates Sun MQ specific configuration information
 * for Sun MQ <code>Destination</code> objects.
 *
 * @see         javax.jms.Destination javax.jms.Destination
 * @see         com.sun.messaging.DestinationConfiguration com.sun.messaging.DestinationConfiguration
 */
public abstract class Destination extends AdministeredObject implements javax.jms.Destination {

    /** The default basename for AdministeredObject initialization */
    private static final String defaultsBase = "Destination";

    /**
     * Constructs an "untitled" Destination.
     */
    public Destination() {
        super(defaultsBase);
    }

    /**
     * Constructs a Destination given the name
     *
     * @param   name The name of the Destination
     * @see     InvalidDestinationException If <code><b>name</b></code> is an invalid destination name
     */
    public Destination (String name) throws InvalidDestinationException {
        super(defaultsBase);
        String errorString;
        if (name == null || "".equals(name)) {
            errorString =
                AdministeredObject.cr.getKString(AdministeredObject.cr.X_INVALID_DESTINATION_NAME);
            throw new InvalidDestinationException(errorString,
                AdministeredObject.cr.X_INVALID_DESTINATION_NAME);
        }
        //Allow temporary destinations to have names that normal destinations cannot
        if (isTemporary()) {
            configuration.put(DestinationConfiguration.imqDestinationName, name);
        } else {
            if (DestinationName.isSyntaxValid(name)) {
                configuration.put(DestinationConfiguration.imqDestinationName, name);
            } else {
                errorString =
                    AdministeredObject.cr.getKString(AdministeredObject.cr.X_INVALID_DESTINATION_NAME, name);
                throw new InvalidDestinationException(errorString,
                    AdministeredObject.cr.X_INVALID_DESTINATION_NAME);
            }
        }
    }

    /**
     * Returns the name of this Destination.
     *  
     * @return the Destination name
     */ 
    public String getName() {
        try {
            return super.getProperty(DestinationConfiguration.imqDestinationName);
        } catch (JMSException e) {
            return ("");
        }
    }

    /**
     * Returns the queue name.
     * 
     * @return the queue name
     *
     * @exception JMSException if a queue access error occurs.
     */
    public String getQueueName() throws JMSException {
        return getName();
    }

    /**
     * Returns the topic name.
     *   
     * @return the topic name
     *
     * @exception JMSException if a topic access error occurs.
     */
    public String getTopicName() throws JMSException {
        return getName();
    }
 
    /**
     * Returns a pretty printed version of the provider specific
     * information for this Destination identity object.
     * 
     * @return the pretty printed string.
     */
    public String toString() {
        return ("Oracle GlassFish(tm) Server MQ Destination\ngetName():\t\t" + getName() + super.toString());
    }
 
    /**
     * Returns whether this is a Queueing type of Destination.
     *
     * @return whether this is a Queueing type of Destination.
     */
    public abstract boolean isQueue();

    /**
     * Returns whether this is a Temporary type of Destination.
     *
     * @return whether this is a Temporary type of Destination.
     */
    public abstract boolean isTemporary();

    /** 
     * Sets the minimum <code>Destination</code> configuration defaults 
     * required of a Sun MQ Destination identity object.
     */ 
    public void setDefaultConfiguration() {
        configuration = new Properties();
        configurationTypes = new Properties();
        configurationLabels = new Properties();          

        configuration.put(DestinationConfiguration.imqDestinationName,
                            DestinationConfiguration.IMQ_INITIAL_DESTINATION_NAME);
        configurationTypes.put(DestinationConfiguration.imqDestinationName,
                            AO_PROPERTY_TYPE_STRING);
        configurationLabels.put(DestinationConfiguration.imqDestinationName,
                            AdministeredObject.cr.L_JMQDESINTATION_NAME);

        configuration.put(DestinationConfiguration.imqDestinationDescription,
                            DestinationConfiguration.IMQ_INITIAL_DESTINATION_DESCRIPTION);
        configurationTypes.put(DestinationConfiguration.imqDestinationDescription,
                            AO_PROPERTY_TYPE_STRING);
        configurationLabels.put(DestinationConfiguration.imqDestinationDescription,
                            AdministeredObject.cr.L_JMQDESINTATION_DESC);
    }

    /**
     * Validates a <code>Destination</code> name.
     *
     * @param name The <code>Destination</code> name.
     *
     * @return <code>true</code> if the name is valid;
     *         <code>false</code> if the name is invalid.
     *
     */
    public Boolean validate_imqDestinationName(String name) {
        if (isTemporary()) {
            if ((name != null) && name.startsWith(ClientConstants.TEMPORARY_DESTINATION_URI_PREFIX)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            return Boolean.valueOf(DestinationName.isSyntaxValid(name));
        }
    }
}
