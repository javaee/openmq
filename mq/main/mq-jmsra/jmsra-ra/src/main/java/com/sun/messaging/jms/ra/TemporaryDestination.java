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

//import javax.jms.*;
import javax.jms.JMSException;

import java.net.InetAddress;
import java.util.logging.Logger;

import com.sun.messaging.jmq.ClientConstants;
import com.sun.messaging.jmq.jmsservice.Destination;


/**
 *  TemporaryDestination for DIRECT Mode
 */
public abstract class TemporaryDestination
        extends com.sun.messaging.Destination {

    /**
     *  Logging
     */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.TemporaryDestination";
    private static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    private static transient final String _lgrNameJMSConnection =
            "javax.jms.Connection.mqjmsra";
    private static transient final Logger _loggerOC =
            Logger.getLogger(_lgrNameOutboundConnection);
    private static transient final Logger _loggerJC =
            Logger.getLogger(_lgrNameJMSConnection);
    private static transient final String _lgrMIDPrefix = "MQJMSRA_TD";
    private static transient final String _lgrMID_EET = _lgrMIDPrefix+"1001: ";
    private static transient final String _lgrMID_INF = _lgrMIDPrefix+"1101: ";
    private static transient final String _lgrMID_WRN = _lgrMIDPrefix+"2001: ";
    private static transient final String _lgrMID_ERR = _lgrMIDPrefix+"3001: ";
    private static transient final String _lgrMID_EXC = _lgrMIDPrefix+"4001: ";

    /**
     *  Holds the DirectConnection that this TemporaryDestination was created in
     */
    private DirectConnection dc = null;

    /**
     *  Holds the jmsservice representation of this TemporaryDestination
     */
    private com.sun.messaging.jmq.jmsservice.Destination destination = null;

    /**
     *  Indicates whether this TemporaryDestination is deleted or not
     */
    private boolean deleted = false;

    /**
     *  Indicates the count of local consumers on this TemporaryDestination
     */
    private int consumer_count = 0;

    /**
     *  Creates a new instance of TemporaryDestination for use by
     *  Session.createTemporaryQueue() and Session.createTemporaryTopic()
     */
    protected TemporaryDestination(DirectConnection dc,
            com.sun.messaging.jmq.jmsservice.Destination.Type _type,
            com.sun.messaging.jmq.jmsservice.Destination.TemporaryType _tType)
    throws JMSException {
        super(Destination.TEMPORARY_DESTINATION_PREFIX +
                _tType + "/" +
                dc._getConnectionIdentifierForTemporaryDestination() + "/" +
                dc.nextTemporaryDestinationId());
        String _name = super.getName();
        this.dc = dc;
        this.destination = new com.sun.messaging.jmq.jmsservice.Destination(
                _name, _type,
                com.sun.messaging.jmq.jmsservice.Destination.Life.TEMPORARY);
    }

    /**
     *  Creates a new instance of TemporaryDestination for use when it is not
     *  explicitly created by Session.createTemporary----(); but when one is
     *  needed from either a Message.getJMSReply() or a MessageProducer's 
     *  send or publish methods
     */
    protected TemporaryDestination(String _name,
            com.sun.messaging.jmq.jmsservice.Destination.Type _type)
    throws JMSException {
        super(_name);
        this.destination = new com.sun.messaging.jmq.jmsservice.Destination(
                _name, _type,
                com.sun.messaging.jmq.jmsservice.Destination.Life.TEMPORARY);
    }

    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.TemporaryQueue_&_TemporaryTopic
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Delete a TemporaryDestination
     */
    public void delete()
    throws JMSException {
        this._delete();
        dc.removeTemporaryDestination(this);
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.TemporaryQueue_&_TemporaryTopic
    /////////////////////////////////////////////////////////////////////////

    /**
     *  Return whether this is a temporary destination or not
     */
    public boolean isTemporary(){
        return true;
    }

    /**
     *  Return whether this TemporaryDestination is deleted or not
     */
    public boolean _isDeleted(){
        return deleted;
    }

    /**
     *  Return the Destination that represents this TemporaryDestination
     */
    protected com.sun.messaging.jmq.jmsservice.Destination _getDestination(){
        return this.destination;
    }

    /**
     *  Delete this temporary destination from the JMSService.
     */
    protected void _delete()
    throws JMSException {
        if (dc== null){
            //Cannot delete as this TD does not have an owning Connection
            String deleteMsg = _lgrMID_EXC + "delete()" +
                    ":Can only delete user created TemporaryDestinations";
            _loggerJC.warning(deleteMsg);
            throw new javax.jms.JMSException(deleteMsg);
        }
        if (dc._hasConsumers(this)){
            //Cannot delete as there are consumers on this TD
            String deleteMsg = _lgrMID_EXC + "delete()" +
                    ":Cannot delete TemporaryDestination with active consumers";
            _loggerJC.warning(deleteMsg);
            throw new javax.jms.JMSException(deleteMsg);
        }
        dc._deleteDestination(this, destination);
        this.deleted = true;
    }

    /**
     *  Increment the consumer count for this TemporaryDestination
     */
    protected int _incrementConsumerCount(){
        return ++this.consumer_count;
    }

    /**
     *  Decrement the consumer count for this TemporaryDestination
     */
    protected int _decrementConsumerCount(){
        --this.consumer_count;
        assert this.consumer_count >= 0;
        return this.consumer_count;
    }

    /**
     *  Return the consumer count for this TemporaryDestination
     */
    protected int _getConsumerCount(){
        return this.consumer_count;
    }

}
