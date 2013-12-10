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

/*
 * @(#)PacketRouter.java	1.33 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data;


import com.sun.messaging.jmq.io.*;
import java.security.AccessControlException;
import com.sun.messaging.jmq.util.ServiceType;
import com.sun.messaging.jmq.jmsserver.service.*;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.ServiceRestrictionException;
import com.sun.messaging.jmq.jmsserver.util.ServiceRestrictionWaitException;
import com.sun.messaging.jmq.io.PacketUtil;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.FaultInjection;
import com.sun.messaging.jmq.util.log.Logger;

/**
 * Class which handles passing messages read in from
 * a protocol to the correct Message Handler
 */

public class PacketRouter
{

    /**
     * dump all packets comming on
     */
    private static  boolean DEBUG = false;

    public static boolean getDEBUG() {
        return DEBUG;
    }

    private final Logger logger = Globals.getLogger();

    /**
     * list of message handlers for specific message types 
     */
    private PacketHandler list[] = new PacketHandler[PacketType.LAST];

    /**
     * default handler which handles errors and messages which do not
     * have handlers registered 
     */
    private ErrHandler defaultHandler = null;

    private FaultInjection fi = null;

    /**
     * Constructor for PacketRouter class.
     */
    public PacketRouter() {
	defaultHandler = new DefaultHandler();
        fi = FaultInjection.getInjection();
    }

    /**
     * registers a new handler for a specific message id
     *
     * @param id the messageID
     * @param handler the handler to add
     * @throws ArrayIndexOutOfBoundsException if the id is too high
     */
    public void addHandler(int id, PacketHandler handler) 
        throws ArrayIndexOutOfBoundsException 
    {
        if (id > PacketType.LAST) {
            throw new ArrayIndexOutOfBoundsException(Globals.getBrokerResources().getString(
           BrokerResources.X_INTERNAL_EXCEPTION, "Trying to add handler which has no corresponding packet type [ " + id + "]"));
        }
        list[id] = handler;
    }

    /**
     * registers a new handler for a group of message id
     *
     * @param sid the starting message id
     * @param eid the ending message id
     * @param handler the handler to add
     * @throws ArrayIndexOutOfBoundsException if the id is too high
     */
    public void addHandler(int sid, int eid, PacketHandler handler) 
        throws ArrayIndexOutOfBoundsException 
    {
        // NOTE: this is not that efficient, but it should ONLY happen at initialization
        // so I'm not worrying about it
        for (int i = sid; i < eid; i ++ )
            addHandler(i, handler);

    }

    /**
     * Return the handler for a specific packet type.
     *
     * @param id the packet type
     * @throws ArrayIndexOutOfBoundsException if the id is too high
     */
    public PacketHandler getHandler(int id)
        throws ArrayIndexOutOfBoundsException 
    {
        if (id > PacketType.LAST) {
            throw new ArrayIndexOutOfBoundsException(id);
        }
        return list[id];
    }

    /**
     * This routine handles passing messages off to the correct handler.
     * Messages should be first checked for authorization (if any)
     * and then sent to the correct handler.
     * If there is no handler, it should be sent to the default
     * handler (aka error handler).
     *
     * @param con the connection the message was received from
     * @param msg the message received
     *
     */
    public void handleMessage(IMQConnection con, Packet msg) {
        int id = msg.getPacketType();

        if (id < 0) {
            logger.log(Logger.ERROR, Globals.getBrokerResources().getString(
           BrokerResources.X_INTERNAL_EXCEPTION, "invalid packet type {0}",
                    String.valueOf(id)));
            defaultHandler.sendError(con, msg, "invalid packet type " + id,Status.ERROR);
            return;
        }

        PacketHandler handler = null;

        if (id >= PacketType.LAST) {
            handler = defaultHandler;
        } else {
            handler = list[id];
        }

        if (handler == null) {
            handler = defaultHandler;
        }
        try {
            if (handler != defaultHandler) { 
                checkServiceRestriction(msg, con, handler, id, defaultHandler);
                if (!checkAccessControl(msg, con, handler, id)) {
                    return;
                }
            }
            if (fi.FAULT_INJECTION && 
                ((IMQService)con.getService()).getServiceType() != ServiceType.ADMIN &&
                fi.checkFaultAndSleep(FaultInjection.FAULT_PACKET_ROUTER_1_SLEEP, null, true)) {
                fi.unsetFault(FaultInjection.FAULT_PACKET_ROUTER_1_SLEEP);
            }
            boolean freepkt = handler.handle(con, msg);
            if (freepkt == true) {
                msg.destroy();
            }
        } catch (ServiceRestrictionException ex) {
            defaultHandler.sendError(con, msg,
                ex.getMessage(), Status.UNAVAILABLE);
        } catch (ServiceRestrictionWaitException ex) {
            msg.destroy();
            return;
        } catch (BrokerException ex) {
            assert defaultHandler != null;

            if (defaultHandler != null) {
                if (ex.getStatusCode() == Status.UNAVAILABLE) {
                    defaultHandler.sendError(con, msg,
                            ex.getMessage(), Status.UNAVAILABLE);
                } else {
                    defaultHandler.sendError(con, ex, msg);
                }
            }
        } catch (Exception ex) {
            logger.logStack(logger.ERROR, ex.getMessage(), ex);
            defaultHandler.sendError(con, new BrokerException(
                    Globals.getBrokerResources().getKString(
                    BrokerResources.X_INTERNAL_EXCEPTION,
                    "Unexpected Error processing message"), ex), msg);
        }
    }

    private boolean checkAccessControl(Packet msg, IMQConnection con,
                                          PacketHandler handler, int pktype) {
        AccessController ac = con.getAccessController(); 
        if (pktype != PacketType.HELLO && pktype != PacketType.PING &&
                pktype != PacketType.AUTHENTICATE &&
                       pktype != PacketType.GOODBYE) {

            if (!ac.isAuthenticated()) {
                String emsg = Globals.getBrokerResources().getKString(
                        BrokerResources.E_UNEXPECTED_PACKET_NOT_AUTHENTICATED,
                                                 PacketType.getString(pktype));
                defaultHandler.sendError(con, msg, emsg, Status.ERROR); 
                return false;
            }
            try {
                handler.checkPermission(msg, con);
                return true;
            } catch (AccessControlException e) {
                try {                 
                    handler.handleForbidden(con, msg, pktype+1);
                } catch (BrokerException ex) {
                    defaultHandler.sendError(con, ex, msg);
                } catch (Exception ex) {
                    defaultHandler.sendError(con, 
			new BrokerException(
                            Globals.getBrokerResources().getKString(
                            BrokerResources.X_INTERNAL_EXCEPTION,
                           "Unexpected Error processing message"), ex), msg);
                }
            } catch (BrokerException ex) {
                defaultHandler.sendError(con, msg, ex.getMessage(), ex.getStatusCode());
            } catch (Exception ex) {
                defaultHandler.sendError(con,
                    new BrokerException(
                       Globals.getBrokerResources().getKString(
                       BrokerResources.X_INTERNAL_EXCEPTION,
                       "Unexpected Error processing message"), ex), msg);
            }
            return false;
        }    
        else {
            return true;
        }
    }

    private void checkServiceRestriction(Packet msg, IMQConnection con,
                                         PacketHandler handler, int pktype,
                                         ErrHandler defHandler) 
                                         throws BrokerException, Exception {

         if (pktype != PacketType.HELLO && pktype != PacketType.PING &&
             pktype != PacketType.AUTHENTICATE && pktype != PacketType.GOODBYE) {
             handler.checkServiceRestriction(msg, con, defHandler);
         }
    }

}
