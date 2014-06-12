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
 * @(#)DurableSubscription.java	1.4 06/27/07
 */ 

package com.sun.jms.spi;
import javax.jms.*;

/**
 * A DurableSubscription is a descriptor of a provider-specific
 * durable subscription used for administration purposes.
 *
 * @see com.sun.jms.spi.JMSAdmin#getDurableSubscriptions()
 * @see com.sun.jms.spi.JMSAdmin#getDurableSubscriptions( Topic )
 * @see com.sun.jms.spi.JMSAdmin#createDurableSubscription( String, TopicConnectionFactory, Topic, String, java.util.Map )
 * @see com.sun.jms.spi.JMSAdmin#deleteDurableSubscription( String, TopicConnectionFactory, Topic )
 * @see com.sun.jms.spi.JMSAdmin#deleteDurableSubscription( DurableSubscription ) 
 */
public interface DurableSubscription {


    /**
     * Accessor for the client ID associated with this durable subscription.
     *
     * @return the subscription's client ID
     * @exception JMSException thrown if there are any internal errors
     * @see javax.jms.Connection#getClientID()
     */
    public String getClientID() throws JMSException;


    /**
     * Accessor for the name of the durable subscription.  This value should
     * be identical to the name that would be supplied to the
     * javax.jms.TopicSession.createDurableSubscription() API - it should
     * not be the internal provider-specific name assigned to the subscription.
     *
     * @return the logical name of the durable subscription
     * @exception JMSException thrown if there are any internal errors
     * @see javax.jms.TopicSession#createDurableSubscriber( Topic, String, String, boolean )
     */
    public String getSubscriptionName() throws JMSException;


    /**
     * Accessor for the topic that the subscription is consuming from.
     *
     * @return the topic that the subscription is consuming from
     * @exception JMSException thrown if there are any internal errors
     */
    public Topic getTopic() throws JMSException;


    /**
     * This method returns a TopicConnectionFactory which a client could
     * use to create a subscriber for this durable subscription.
     *
     * @return the associated TopicConnectionFactory or null if one wasn't specified 
     * during creation
     * @exception JMSException thrown if there are any internal errors
     */
    public TopicConnectionFactory getConnectionFactory() throws JMSException;


    /**
     * Accessor for the message selector
     *
     * @return the message selector
     * @exception JMSException thrown if there are any internal errors
     */
    public String getMessageSelector() throws JMSException;


}

