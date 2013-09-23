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
 * @(#)DedicatedService.java	1.21 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.dedicated;

import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.service.imq.*;
import com.sun.messaging.jmq.jmsserver.service.Connection;
import java.nio.channels.*;
import java.io.IOException;
import com.sun.messaging.jmq.util.GoodbyeReason;
import java.util.*;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.pool.*;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.net.Protocol;
import com.sun.messaging.jmq.jmsserver.net.ProtocolStreams;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.util.log.Logger;


public class DedicatedService extends IMQIPService
{

    public DedicatedService(String name, Protocol protocol,
        int type, PacketRouter router, int min, int max) {
        super(name, protocol, type, router, min, max);
    }

    public RunnableFactory getRunnableFactory() {
        return new OperationRunnableFactory(true /* blocking */);
    }

    public Hashtable getDebugState()
    {
//XXX
        return super.getDebugState();
    }


    public void acceptConnection(IMQIPConnection con)
        throws IOException, BrokerException
    {
        // Get a thread for read

        OperationRunnable read = (OperationRunnable)
                   pool.getAvailRunnable(false);
        OperationRunnable write = (OperationRunnable)
                   pool.getAvailRunnable(false);
        if (read == null || write == null) {
            if (read != null)
                pool.releaseRunnable(read);
            if (write != null)
                pool.releaseRunnable(write);

            String args[] = {this.toString(),
                       String.valueOf(pool.getAssignedCnt()),
                       String.valueOf(pool.getMaximum())};
            logger.log(Logger.WARNING, 
                BrokerResources.E_NOT_ENOUGH_THREADS,
                args);

            pool.debug();
            con.destroyConnection(true, GoodbyeReason.CON_FATAL_ERROR, 
                 Globals.getBrokerResources().getKString(
                 BrokerResources.E_NOT_ENOUGH_THREADS, args));
            throw new BrokerException( 
                Globals.getBrokerResources().getKString(
                    BrokerResources.E_NOT_ENOUGH_THREADS,
                    args),
                BrokerResources.E_NOT_ENOUGH_THREADS,
                (Throwable) null,
                Status.NOT_ALLOWED);

        }
        
        // CR 6798565: start writer thread before reader thread
        startWriterThread(con,read,write);
        startReaderThread(con,read,write);
        
    }
    
    private void startReaderThread(IMQIPConnection con, OperationRunnable read, OperationRunnable write) throws MissingResourceException, BrokerException{

//XXX - workaround to prevent code from breaking when bug 4616064 occurs and
//      provide better output
// XXX

// XXX-start Workaround
        // XXX- workaround for bug (will provide info to track it down)
        boolean assigned = false; 
        while (!assigned) {
            try {
// XXX-end Workaround
                read.assignOperation(con, SelectionKey.OP_READ, 
                                   OperationRunnable.FOREVER);
// XXX-start Workaround
                assigned = true;
            } catch (IllegalAccessException ex) {
                logger.logStack(Logger.ERROR, 
                    BrokerResources.E_INTERNAL_BROKER_ERROR,
                   "assigning read for " + con + " to available thread " 
                   + read, ex);
                pool.debug();
                read = (OperationRunnable)pool.getAvailRunnable(false);
                logger.log(Logger.DEBUG,
                        "Recovering: Assigning new read for " 
                        + con + " to available thread " + read);
                if (read == null) { // bummer
                    String args[] = {this.toString(),
                               String.valueOf(pool.getAssignedCnt()),
                               String.valueOf(pool.getMaximum())};
                    if (write != null)
                        pool.releaseRunnable(write);
                    logger.log(Logger.ERROR, 
                         BrokerResources.E_NOT_ENOUGH_THREADS, args);
                    pool.debug();
                    con.destroyConnection(true, GoodbyeReason.CON_FATAL_ERROR, 
                         Globals.getBrokerResources().getKString(
                         BrokerResources.E_NOT_ENOUGH_THREADS));
                    throw new BrokerException( 
                        Globals.getBrokerResources().getKString(
                            BrokerResources.E_NOT_ENOUGH_THREADS,
                            args),
                        BrokerResources.E_NOT_ENOUGH_THREADS,
                        (Throwable) null,
                        Status.ERROR);
                }
            }
        }
        
    }
        
    
    private void startWriterThread(IMQIPConnection con, OperationRunnable read, OperationRunnable write) throws MissingResourceException, BrokerException{
    
        boolean assigned = false;
        while (!assigned) {
            try {
//XXX-end Workaround
                write.assignOperation(con, SelectionKey.OP_WRITE, 
                                OperationRunnable.FOREVER);
//XXX-start Workaround
                assigned = true;
//XXX-end Workaround
            } catch (IllegalAccessException ex) {
                logger.logStack(Logger.ERROR, 
                    BrokerResources.E_INTERNAL_BROKER_ERROR,
                   "assigning write for " + con 
                   + " to available thread " + read, ex);
                pool.debug();
                write = (OperationRunnable)pool.getAvailRunnable(false);
                logger.log(Logger.DEBUG,
                     "Recovering: Assigning new write for " 
                     + con + " to available thread " + read);
                if (write == null) { // bummer
                    String args[] = {this.toString(),
                               String.valueOf(pool.getAssignedCnt()),
                               String.valueOf(pool.getMaximum())};
                    if (read != null)
                        pool.releaseRunnable(read);
                    logger.log(Logger.ERROR,
                         BrokerResources.E_NOT_ENOUGH_THREADS, args);
                    pool.debug();
                    con.destroyConnection(true, GoodbyeReason.CON_FATAL_ERROR, 
                         Globals.getBrokerResources().getKString(
                         BrokerResources.E_NOT_ENOUGH_THREADS));
                    throw new BrokerException( 
                        Globals.getBrokerResources().getKString(
                            BrokerResources.E_NOT_ENOUGH_THREADS,
                            args),
                        BrokerResources.E_NOT_ENOUGH_THREADS,
                        (Throwable) null,
                        Status.ERROR);
                }
            }
        }
//XXX-end Workaround

    }


}

