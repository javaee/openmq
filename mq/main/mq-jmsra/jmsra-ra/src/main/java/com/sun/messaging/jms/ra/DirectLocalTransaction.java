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

package com.sun.messaging.jms.ra;

import javax.resource.ResourceException;
import javax.resource.spi.EISSystemException;


/**
 *  Implements the LocalTransaction interface in DIRECT mode for the Sun MQ RA
 *
 *  @author tharakan
 */

public class DirectLocalTransaction
implements javax.resource.spi.LocalTransaction
{
    /** The connection event listener list */
    //private Vector listeners = null;

    /** The ManagedConnection associated with this LocalTransaction */
    private com.sun.messaging.jms.ra.ManagedConnection mc = null;
    private DirectConnection dc = null;

    private long transactionID = -1L;
 
    protected boolean started = false;
    protected boolean active = false;
 


    /** Constructor */
    public DirectLocalTransaction(com.sun.messaging.jms.ra.ManagedConnection mc,
            DirectConnection dc)
    {
        //System.out.println("MQRA:LT:Constr");
        this.mc = mc;
        this.dc = dc;
    }

    /** Begin a local transaction */
    public synchronized void
    begin()
    throws ResourceException
    {
        //System.out.println("MQRA:LT:begin()");
        try {
            if (!dc.isClosed()) {
                transactionID = this.dc._startTransaction("DirectLocalTransaction.begin()");
            } else {
                ResourceException re = new EISSystemException("MQRA:LT:startTransaction exception:Connection is closed");
                throw re;
            }
            //mc.getConnectionAdapter().getSessionAdapter().startLocalTransaction();
        } catch (Exception ex) {
            ResourceException re = new EISSystemException("MQRA:LT:startTransaction exception:"+
                ex.getMessage());
            re.initCause(ex);
            throw re;
        }
        started = true;
        active = true;
        mc.setLTActive(true);
    }

    /** Commit a local transaction */
    public synchronized void
    commit()
    throws ResourceException
    {
        //System.out.println("MQRA:LT:commit()");
        try {
            if (!dc.isClosed()) {
                this.dc._commitTransaction("DirectLocalTransaction.commit()",
                        this.transactionID);
            } else {
                ResourceException re = new EISSystemException("MQRA:LT:commitTransaction exception:Connection is closed");
                throw re;
            }
        } catch (Exception ex) {
            ResourceException re = new EISSystemException("MQRA:LT:commit exception:"+
                ex.getMessage());
            re.initCause(ex);
            throw re;
        } finally {
            mc.setLTActive(false);
            started = false;
            active = false;
        }
    }

    /** Rollback a local transaction */
    public synchronized void
    rollback()
    throws ResourceException
    {
        //System.out.println("MQRA:LT:rollback()");
        try {
            if (!dc.isClosed()) {
                this.dc._rollbackTransaction("DirectLocalTransaction.rollback()",
                        this.transactionID);
            } else {
                ResourceException re = new EISSystemException("MQRA:LT:rillbackTransaction exception:Connection is closed");
                throw re;
            }
        } catch (Exception ex) {
            ResourceException re = new EISSystemException("MQRA:LT:rollback exception:"+
                ex.getMessage());
            re.initCause(ex);
            throw re;
        } finally {
            mc.setLTActive(false);
            started = false;
            active = false;
        }
    }

    public synchronized long getTransactionID() {
        return transactionID;
    }

    public synchronized boolean started() {
        return started;
    }

    public synchronized boolean isActive() {
        return active;
    }
}

