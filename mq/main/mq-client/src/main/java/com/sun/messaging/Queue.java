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
 * @(#)Queue.java	1.11 06/28/07
 */ 

package com.sun.messaging;

import com.sun.messaging.jmq.DestinationName;
import com.sun.messaging.naming.ReferenceGenerator;
import com.sun.messaging.naming.AdministeredObjectFactory;

/**
 * A <code>Queue</code> represents an identity of a repository of messages
 * used in the JMS Point-To-Point messaging domain.
 *
 * @see         javax.jms.Queue javax.jms.Queue
 */
public class Queue extends com.sun.messaging.BasicQueue implements javax.naming.Referenceable {

    /**
     * Constructs an identity of a Point-To-Point Queue with the default name
     */
    public Queue () {
	super();
    }

    /**
     * Constructs an identity of a Point-To-Point Queue with the given name
     *
     * @param   name The name of the Queue
     */
    public Queue (String name) throws javax.jms.JMSException {
	super(name);
    }

    /**
     * Returns a Reference Object that can be used to reconstruct this object.
     *
     * @return  The Reference Object that can be used to reconstruct this object
     *
     */
    public javax.naming.Reference getReference() {
        return (ReferenceGenerator.getReference(this, AdministeredObjectFactory.class.getName()));
    }

    /**
     * Sets the name of the Queue. This method performs name validatation
     * This is used by an Application Server via the Sun MQ J2EE Resource Adapter
     *
     * @param   name The name of the Queue
     * @throws  IllegalArgumentException if name is invalid
     */
    public void setName (String name) {
        if (DestinationName.isSyntaxValid(name)) {
            configuration.put(DestinationConfiguration.imqDestinationName, name);
        } else {
            throw new IllegalArgumentException("MQ:Queue:Invalid Queue Name - " + name);
        }
    }

    /**
     * Sets a description for this Queue. The description can be any String
     *
     * @param   description The description for this Queue
     */
    public void setDescription (String description) {
        configuration.put(DestinationConfiguration.imqDestinationDescription, description);
    }

    /**
     * Returns the description for this Queue.
     *   
     * @return The description for this Queue
     */  
    public String
    getDescription()
    {
        try { 
            return getProperty(DestinationConfiguration.imqDestinationDescription);
        } catch (javax.jms.JMSException jmse) {
            return "";
        } 
    }

}
