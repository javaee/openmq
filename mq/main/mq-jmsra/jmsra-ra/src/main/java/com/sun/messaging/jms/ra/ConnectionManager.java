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

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

/**
 *  Implements the ConnectionManager interface for the S1 MQ RA.
 *  An instance of this ConnectionManager is used when the RA is
 *  used in a Non-Managed environment/scenario.
 */

public class ConnectionManager
implements java.io.Serializable,
           javax.resource.spi.ConnectionManager,
           javax.resource.spi.ConnectionEventListener
{
    /* Simple partitioned pool implementation */
    // Each pool is keyed by the mcfId of the MCF
    // Each partition is keyed by the userName+clientID on the connection
    //
    // AS ConnectionManager has to store a pool for each MCF
    //    which gets partitioned by Subject info
    private transient Vector connections = null;

    /* Loggers */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.ConnectionManager";
    protected static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    protected static transient final Logger _loggerOC =
            Logger.getLogger(_lgrNameOutboundConnection);
    protected static transient final String _lgrMIDPrefix = "MQJMSRA_CM";
    protected static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    protected static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    protected static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    protected static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    protected static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";
 
    /** Public Constructor */
    public ConnectionManager()
    {
        _loggerOC.entering(_className, "constructor()");

        //PENDING: CM Pooling
        //connections = new Vector();
    }
    
    // ConnectionManager interface methods //
    // 

    /** Allocates a ManagedConnection.
     *
     *  @param mcf The ManagedConnectionFactory to use.
     *  @param cxRequestInfo The ConnectionRequestInfo to use.
     *
     *  @return The ManagedConnection instance
     */
    public Object
    allocateConnection(javax.resource.spi.ManagedConnectionFactory mcf,
            javax.resource.spi.ConnectionRequestInfo cxRequestInfo)
    throws javax.resource.ResourceException
    {
        Object params[] = new Object[2];
        params[0] = mcf;
        params[1] = cxRequestInfo;

        _loggerOC.entering(_className, "allocateConnection()", params);

        javax.resource.spi.ManagedConnection mc = null;
        if (false) {
            //PENDING: CM Pooling
            //_loggerOC.finer(_lgrMID_INF+
            //mc = match and return from connections if non-empty
            return mc; //null
        } else {
            //_loggerOC.finer(_lgrMID_INF+
            mc = mcf.createManagedConnection(null, cxRequestInfo);
            mc.addConnectionEventListener(this);
            return mc.getConnection(null, cxRequestInfo);
        }
    }


    // ConnectionEventListener interface methods
    //

    /** connectionClosed
    *
    *    Close the physical connection
    *   
    */
    public void connectionClosed(javax.resource.spi.ConnectionEvent event)
    {
        _loggerOC.entering(_className, "connectionClosed()", event);
        if (event != null) {
            com.sun.messaging.jms.ra.ManagedConnection mc = (com.sun.messaging.jms.ra.ManagedConnection)event.getSource();
            //connections.add(mc);
            try {
                _loggerOC.fine(_lgrMID_INF+"connectionClosed:event="+event+":cleanup&destroy mc="+mc.toString());
                mc.cleanup();
                mc.destroy();
            } catch (Exception re) {
                _loggerOC.warning(_lgrMID_WRN+"connectionErrorOccurred:Exception on cleanup&destroy:"+re.getMessage()+":event="+event+":mc="+mc.toString());
                re.printStackTrace();
            }
        }
    }

    /** connectionErrorOccurred
    *
    *
    */
    public void connectionErrorOccurred(javax.resource.spi.ConnectionEvent event)
    {
        _loggerOC.entering(_className, "connectionErrorOccurred()", event);
        if (event != null) {
            com.sun.messaging.jms.ra.ManagedConnection mc = (com.sun.messaging.jms.ra.ManagedConnection)event.getSource();
            try {
                _loggerOC.warning(_lgrMID_WRN+"connectionErrorOccurred:event="+event+":Destroying mc="+mc.toString());
                mc.destroy();
            } catch (Exception re) {
                _loggerOC.warning(_lgrMID_WRN+"connectionErrorOccurred:Exception on destroy():"+re.getMessage()+":event="+event+":mc="+mc.toString());
                re.printStackTrace();
            }
        }
    }

    /** localTransactionCommitted
    *
    *
    */
    public void localTransactionCommitted(javax.resource.spi.ConnectionEvent event)
    {
        _loggerOC.entering(_className, "localTransactionCommitted()", event);
    }

    /** localTransactionRolledback
    *
    *
    */
    public void localTransactionRolledback(javax.resource.spi.ConnectionEvent event)
    {
        _loggerOC.entering(_className, "localTransactionRolledback()", event);
    }

    /** localTransactionStarted
    *
    *
    */
    public void localTransactionStarted(javax.resource.spi.ConnectionEvent event)
    {
        _loggerOC.entering(_className, "localTransactionStarted()", event);
    }

    // Public methods
    //

    /** destroy connections
    *
    *
    *  PENDING: CM pooling 
    */
    public void
    destroyConnections()
    {
        if (false) {
        if (connections != null) {
            for (int i=0; i<connections.size(); i++) {
                //System.out.println("MQRA:CM:destroyConnections:destroy mc#:"+i);
                try {
                    ((com.sun.messaging.jms.ra.ManagedConnection)connections.elementAt(i)).destroy();
                } catch (Exception e) {
                    System.err.println("MQRA:CM:destroyConnections:Exception"+e.getMessage());
                    e.printStackTrace();
                }
            }
            connections.clear();
        }
        }
    }
}

