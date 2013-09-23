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
 * @(#)ClusteredBroker.java	1.11 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

import com.sun.messaging.jmq.io.MQAddress;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.UID;


/**
 * represents an instance of a broker in a cluster
 */
public interface ClusteredBroker
{
    /**
     * A unique identifier assigned to the broker
     * (randomly assigned).<P>
     *
     * This name is only unique to this broker. The
     * broker at this URL may be assigned a different name
     * on another broker in the cluster.
     *
     * @return the name of the broker
     */
    public String getBrokerName();  

    /**
     * Returns the URL to the portmapper of this broker.
     * @return the URL of this broker
     */
    public MQAddress getBrokerURL();  

    /**
     * @return the instance name of this broker, null if not available
     */
     public String getInstanceName();

     /**
      *
      * @param instName the instance name of this broker, can be null
      */
     public void setInstanceName(String instName);

    /**
     * Sets the URL to the portmapper of this broker
     * @param addr the URL of this broker
     */
    public void setBrokerURL(MQAddress addr) throws Exception;

    /**
     * Returns if this is the address of the broker running in this
     * VM.
     * @return true if this is the broker running in the
     *         current vm
     */
    public boolean isLocalBroker();  

    /**
     * Retrieves the status of the broker.
     * @return the status of the broker
     */
    public int getStatus();  

    /**
     * Gets the protocol version of the broker.
     * @return the protocol version (if known) or 0 if
     *     not known.
     */
    public int getVersion();  

    /**
     * Sets the protocol version of the broker.
     * @param version the protocol version
     * @throws UnsupportedOperationException if the version can
     *         not be set for this broker
     */
    public void setVersion(int version) throws Exception;

    /**
     * Sets the status of the broker. Do not hold locks while calling
     * this routine.
     *
     * @param status the broker status to set for this broker
     * @param userData optional data associated with the change
     */
    public void setStatus(int status, Object userData);


    /**
     * Updates the BROKER_UP bit flag on status.
     * 
     * @param up setting for the bit flag (true/false)
     * @param brokerSession 
     * @param userData optional data associated with the change
     */
    public void setBrokerIsUp(boolean up, UID brokerSession, Object userData);

    /**
     * Updates the BROKER_LINK_UP bit flag on status.
     * 
     * @param up setting for the bit flag (true/false)
     * @param userData optional data associated with the change
     */
    public void setBrokerLinkUp(boolean up, Object userData);

    /**
     * Updates the BROKER_INDOUBT bit flag on status.
     * 
     * @param up setting for the bit flag (true/false)
     * @param userData optional data associated with the change
     */
    public void setBrokerInDoubt(boolean indoubt, Object userData);


    /**
     * Destroys the ClusteredBroker.
     */
    public void destroy();

    /**
     * Gets the state of the broker.
     *
     * @return the broker state
     * @throws BrokerException if the state can not be retrieved
     */
    public BrokerState getState()
        throws BrokerException;

    /**
     * Sets the state of the broker.     * @throws IllegalAccessException if the broker does not have
     *               permission to change the broker (e.g. one broker
     *               is updating anothers state).
     * @throws IllegalStateException if the broker state changed
     *               unexpectedly.
     * @throws IllegalArgumentException if the state is not supported
     *               for this cluster type.
     * @param state the new broker state
     */
    public void setState(BrokerState state)
         throws IllegalAccessException, IllegalStateException,
                IllegalArgumentException;

    
    /**
     * Is the broker static or dynmically configured
     */
    public boolean isConfigBroker();


    /**
     * equals method
     */
    public boolean equals(Object o);

    /**
     *  hashcode method
     */
    public int hashCode();


    /**
     * Gets the UID associated with the broker session.
     *
     * @return the broker session uid (if known)
     */
    public UID getBrokerSessionUID();

    /**
     * Sets the UID associated with the broker session.
     *
     * @param uid the new broker session uid 
     */
    public void setBrokerSessionUID(UID uid);

    /**
     * returns if the brokerID was generated.
     * @return true if the ID was generated
     */
    public boolean isBrokerIDGenerated();

    /**
     * used by replicated BDB
     */
    public String getNodeName() throws BrokerException;

}
