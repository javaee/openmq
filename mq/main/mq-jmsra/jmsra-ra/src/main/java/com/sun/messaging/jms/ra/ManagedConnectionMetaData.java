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

import javax.resource.*;
import javax.resource.spi.*;

import java.util.logging.Logger;

/**
 *  Implements the ManagedConnectionMetaData interface for the Sun MQ JMS RA
 */

public class ManagedConnectionMetaData
implements javax.resource.spi.ManagedConnectionMetaData
{

    /* Loggers */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.ManagedConnectionMetaData";
    protected static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    protected static transient final Logger _loggerOC =
            Logger.getLogger(_lgrNameOutboundConnection);
    protected static transient final String _lgrMIDPrefix = "MQJMSRA_MM";
    protected static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    protected static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    protected static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    protected static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    protected static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";
 

    /** The ManagedConnection for this ManagedConnectionMetaData instance */
    private com.sun.messaging.jms.ra.ManagedConnection mc = null;

    /** Constructor */
    public ManagedConnectionMetaData(com.sun.messaging.jms.ra.ManagedConnection mc)
    {
        _loggerOC.entering(_className, "constructor()");
        this.mc = mc;
    }
    
    // ManagedConnectionMetaData interface methods //
    // 

    /** Return the Product Name
     *
     *  @return The EIS Product Name
     */
    public String
    getEISProductName()
    throws javax.resource.ResourceException
    {
        _loggerOC.entering(_className, "getEISProductName()");
        try {
            ConnectionAdapter ca = mc.getConnectionAdapter();
            return ca.getMetaData().getJMSProviderName();
        } catch (JMSException jmse) {
            ResourceException re = new EISSystemException(_lgrMID_EXC+"getEISProductName:Failed:"+jmse.getMessage());
            re.initCause(jmse);
            _loggerOC.warning(re.getMessage());
            _loggerOC.throwing(_className, "getEISProductName()", re);
            throw re;
        }
    }

    /** Return the Product Version
     *
     *  @return The EIS Product Version
     */
    public String
    getEISProductVersion()
    throws javax.resource.ResourceException
    {
        _loggerOC.entering(_className, "getEISProductVersion()");
        try {
            ConnectionAdapter ca = mc.getConnectionAdapter();
            return ca.getMetaData().getProviderVersion();
        } catch (JMSException jmse) {
            ResourceException re = new EISSystemException(_lgrMID_EXC+"getEISProductVersion:Failed:"+jmse.getMessage());
            re.initCause(jmse);
            _loggerOC.warning(re.getMessage());
            _loggerOC.throwing(_className, "getEISProductName()", re);
            throw re;
        }
    }

    /** Return the max active connections per managed connection?
     *
     *  @return The max connections
     */
    public int
    getMaxConnections()
    throws javax.resource.ResourceException
    {
        _loggerOC.entering(_className, "getMaxConnections()");
        return 1;
    }

    /** Return the User Name for this managed connection
     *
     *  @return The User Name 
     */
    public String
    getUserName()
    throws javax.resource.ResourceException
    {
        _loggerOC.entering(_className, "getUserName()");
        if (mc.isDestroyed()) {
            javax.resource.spi.IllegalStateException ise = new javax.resource.spi.IllegalStateException(
                        _lgrMID_EXC+"getUserName:Failed:ManagedConnection is destroyed");
            _loggerOC.warning(ise.getMessage());
            _loggerOC.throwing(_className, "getUserName()", ise);
            throw ise;
        } else {
            return mc.getPasswordCredential().getUserName();
        }
    }
}

