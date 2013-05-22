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

import javax.jms.JMSException;

import javax.resource.*;
import javax.resource.spi.*;

import java.util.Vector;
import java.util.logging.Logger;

import com.sun.messaging.jmq.jmsclient.notification.BrokerAddressListChangedEvent;

/**
 *  Implements the JMS ExceptionListener interface
 *  and is the generator of events to the ConnectionEventListener
 *  for the SJS MQ RA.
 */

public class ConnectionEventListener
implements  javax.jms.ExceptionListener,
            com.sun.messaging.jms.notification.EventListener
{
    /** The connection event listener list */
    private Vector<javax.resource.spi.ConnectionEventListener> listeners = null;

    /** The ManagedConnection associated with this ConnectionEventListener */
    private com.sun.messaging.jms.ra.ManagedConnection mc = null;

    /* Loggers */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.ConnectionEventListener";
    protected static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    protected static transient final Logger _loggerOC =
            Logger.getLogger(_lgrNameOutboundConnection);
    protected static transient final String _lgrMIDPrefix = "MQJMSRA_CL";
    protected static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    protected static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    protected static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    protected static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    protected static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";
 

    /** Constructor */
    public ConnectionEventListener(com.sun.messaging.jms.ra.ManagedConnection mc)
    {
        _loggerOC.entering(_className, "constructor()", mc);
        listeners = new Vector<javax.resource.spi.ConnectionEventListener>();
        this.mc = mc;
    }

    /** Adds a ConnectionEventListener to the list of listeners */
    public void
    addConnectionEventListener(javax.resource.spi.ConnectionEventListener listener)
    {
        _loggerOC.entering(_className, "addConnectionEventListener()", listener);
        listeners.addElement(listener);
    }

    /** Removes a ConnectionEventListener from the list of listeners */
    public void removeConnectionEventListener(javax.resource.spi.ConnectionEventListener listener)
    {
        _loggerOC.entering(_className, "removeConnectionEventListener()", listener);
        listeners.removeElement(listener);
    }


    /** Sends a ConnectionEvent to the list of registered listeners
     *  
     * @param type The type of event
     *
     * @param ex The Exception (if an exception will be thrown)
     *
     * @param handle The connection handle to set into the ConnectionEvent
     */
    public void
    sendEvent(int type, Exception ex, Object handle)
    {
        Object params[] = new Object[3];
        params[0] = new Integer(type);
        params[1] = ex;
        params[2] = handle;
 
        _loggerOC.entering(_className, "sendEvent()", params);

        Vector list = (Vector)listeners.clone();
        ConnectionEvent cevent = null;
        if (ex != null) {
            cevent = new ConnectionEvent(mc, type, ex);
        } else {
            cevent = new ConnectionEvent(mc, type);
        }
        if (handle != null) {
            cevent.setConnectionHandle(handle);
        }
        for (int i=0; i<list.size(); i++) {
            javax.resource.spi.ConnectionEventListener listener
                = (javax.resource.spi.ConnectionEventListener)list.elementAt(i);
            switch (type) {
                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    listener.connectionErrorOccurred(cevent);
                    break;
                case ConnectionEvent.CONNECTION_CLOSED:
                    listener.connectionClosed(cevent);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    listener.localTransactionStarted(cevent);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    listener.localTransactionCommitted(cevent);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    listener.localTransactionRolledback(cevent);
                    break;
                default:
                    IllegalArgumentException iae = new IllegalArgumentException(_lgrMID_WRN+"sendEvent:Unknown Event="+type);
                    _loggerOC.warning(iae.getMessage());
                    _loggerOC.throwing(_className, "sendEvent()", iae);
                    throw iae;

            }
        }
    }


    //javax.jms.Exceptionlistener interface method
    // 

    /** Upon receipt of a JMS Connection Exception 'onException'
     *  method call, this method sends a CONNECTION_ERROR_OCCURRED
     *  ConnectionEvent to the registered listeners.
     */
    public void
    onException(JMSException jmse)
    {
        //System.err.println("MQRA:CEL:onException():for mc="+mc.getMCId()+" :xacId="+mc.getConnectionAdapter().xac._getConnectionID());
        _loggerOC.warning(_lgrMID_WRN+"onException:for mc="+mc.getMCId()+" :xacId="/*+mc.getConnectionAdapter().xac._getConnectionID()*/);
        sendEvent(ConnectionEvent.CONNECTION_ERROR_OCCURRED, jmse, null);
    }

    //com.sun.messaging.jms.notification.EventListener interface method
    public void
    onEvent(com.sun.messaging.jms.notification.Event evnt)
    {
        _loggerOC.entering(_className, "onEvent()", evnt);
        _loggerOC.info(_lgrMID_INF+"onEvent:Connection Event for mc="+mc.getMCId()+" :xacId="+/*mc.getConnectionAdapter().xac._getConnectionID()+*/":event:"+evnt.toString());

        if (evnt instanceof BrokerAddressListChangedEvent) {
            BrokerAddressListChangedEvent bAddressListChangedEvt =
                    (BrokerAddressListChangedEvent)evnt;
            String addressList = bAddressListChangedEvt.getAddressList();
            if (addressList != null) {
                _loggerOC.info(_lgrMID_INF+"onEvent:Notification Event for mc="
                        + mc.getMCId() + " :xacId=" +
                        /*mc.getConnectionAdapter().xac._getConnectionID()+*/
                        "New AddressList=" + addressList +
                        ":event:"+evnt.toString()
                        );
                mc.getManagedConnectionFactory()._setMessageServiceAddressList(
                        addressList);
            }
        }
    }
}
