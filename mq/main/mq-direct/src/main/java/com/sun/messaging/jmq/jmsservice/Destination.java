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
 * @(#)Destination.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.jmsservice;

/**
 *
 */
public class Destination {

    /**
     *  Enum values that specify the Type of the Destination
     *
     *  @see javax.jms.Destination javax.jms.Destination
     */
    public static enum Type {
        /**
         *  The Destination is a Queue destination as defined by the JMS
         *  Specification
         *
         *  @see javax.jms.Queue javax.jms.Queue
         */
        QUEUE,

        /**
         *  The Destination is a Topic destination as defined by the JMS
         *  Specification
         *
         *  @see javax.jms.Topic javax.jms.Topic
         */
        TOPIC
    }

    /**
     *  Enum values that specify the Life of the Destination
     */
    public static enum Life {
        /**
         *  The Destination is a Standard Destination as defined by the JMS
         *  Specification
         *
         *  @see javax.jms.Queue javax.jms.Queue
         *  @see javax.jms.Topic javax.jms.Topic
         */
        STANDARD,

        /**
         *  The Destination is a TemporaryDestination as defined by the JMS
         *  Specification
         *
         *  @see javax.jms.TemporaryQueue javax.jms.TemporaryQueue
         *  @see javax.jms.TemporaryTopic javax.jms.TemporaryTopic
         */
        TEMPORARY
    }

    /**
     *  Enum values that specify how the physical Destination was created
     */
    public static enum CreationType {
        /**
         *  The Destination is automatically created
         */
        AUTO,

        /**
         *  The Destination is administratively created
         */
        ADMIN
    }  

    /** Enum value that specify the temporary destination name prefix */
    public static enum TemporaryType {
        queue,
        topic
    }

    /** Definition of TemporaryQueue and TemporaryTopic name prefixes */
    public static final String TEMPORARY_DESTINATION_PREFIX =
            "temporary_destination://";
    public static final String TEMPORARY_QUEUE_NAME_PREFIX = "queue/";
    public static final String TEMPORARY_TOPIC_NAME_PREFIX = "topic/";

    /** The name of the Destination */
    private String name;

    /** The Type of the Destination */
    private Type type;

    /** The Life of this Destination */
    private Life life;

    /** The CreationType of this Destination */
    private CreationType creationType;
    
    /** Creates a new instance of a Destination */
    public Destination (String name, Type type, Life life) {
        this.name = name;
        this.type = type;
        this.life = life;
        this.creationType = CreationType.AUTO;
    }

    /**
     *  returns the Name of the Destination
     *
     *  @return The name of the Destination
     */
    public String getName() {
        return name;
    }

    /**
     *  returns the DestinationType of this Destination
     *
     *  @return The DestinationType
     */
    public Type getType() {
        return this.type;
    }

    /**
     *  returns the DestinationLifeSpan of this Destination
     *
     *  @return The DestinationLifeSpan
     */
    public Life getLife() {
        return this.life;
    }

    /**
     *  Set the creationType for this Destination
     *
     *  @param creationType The JMSService.DestinationCreation value
     */
    public void setCreationType(CreationType creationType) {
        this.creationType = creationType;
    }

    /**
     *  Return the creationType for this Destination
     *
     *  @return The creationType
     */
    public CreationType getCreationType() {
        return this.creationType;
    }
}
