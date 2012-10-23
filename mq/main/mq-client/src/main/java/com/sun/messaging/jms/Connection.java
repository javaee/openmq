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
 * @(#)Connection.java	1.4 07/02/07
 */ 

package com.sun.messaging.jms;

import javax.jms.JMSException;
import javax.jms.Session;

import com.sun.messaging.Destination;
import com.sun.messaging.jms.notification.EventListener;

/**
 * This interafce provides the following API for the MQ applications:
 * <p>
 * 1. Provide API to create a MQ NO_ACKNOWLEDGE session.
 * <p>
 * 2. Provide API to set the connection event listener.
 * <p>
 * 3. Provide API to query broker adress and HA state.
 */
public interface Connection extends javax.jms.Connection {

    /** Creates a <CODE>Session</CODE> object.
      *
      * @param acknowledgeMode indicates whether the consumer or the
      * client will acknowledge any messages it receives;
      * Legal values are <code>Session.AUTO_ACKNOWLEDGE</code>,
      * <code>Session.CLIENT_ACKNOWLEDGE</code>,
      * <code>Session.DUPS_OK_ACKNOWLEDGE</code>, and
      * <code>com.sun.messaging.jms.Session.NO_ACKNOWLEDGE</code>
      *
      * @return a newly created  session
      *
      * @exception JMSException if the <CODE>Connection</CODE> object fails
      *                         to create a session due to some internal error or
      *                         lack of support for the specific transaction
      *                         and acknowledgement mode.
      *
      * @see Session#AUTO_ACKNOWLEDGE
      * @see Session#CLIENT_ACKNOWLEDGE
      * @see Session#DUPS_OK_ACKNOWLEDGE
      * @see com.sun.messaging.jms.Session#NO_ACKNOWLEDGE
      */

    public Session
    createSession(int acknowledgeMode) throws JMSException;

    /**
     * Set MQ connection event listener to the current connection.
     *
     * @param listener EventListener
     * @throws JMSException
     */
    public void
    setEventListener (EventListener listener) throws JMSException;

    /**
     * Set consumer event listener on a destination to the current connection. 
     *
     * @param dest the destination on which consumer event is interested 
     * @param listener EventListener
     * @throws JMSException
     * @since 4.5 
     */
    public void
    setConsumerEventListener (Destination dest,
                              EventListener listener)
                              throws JMSException;

    /**
     * Remove a MQ consumer event listener from the current connection.
     *
     * @param dest the destination on which addConsumerEventListener() was called previously 
     * @param listener EventListener
     * @throws JMSException
     * @since 4.5 
     */
    public void
    removeConsumerEventListener (Destination dest) throws JMSException;

    /**
     * Get the broker's address that the connection is connected (related) to.
     *
     * @return the broker's address that the connection is connected (related)
     *         to.
     */
    public String getBrokerAddress();

    /**
     * Get the current connection state.
     *
     * @return true if the connection is connected to a HA broker.
     *         false if not connected to a HA broker.
     */
    public boolean isConnectedToHABroker();

}
