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
 * %W% %G%
 */ 

package com.sun.messaging.jmq.jmsserver.plugin.spi;

import java.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsserver.core.Session;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.util.lists.RemoveReason;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionList;
import com.sun.messaging.jmq.jmsserver.service.Connection;



public abstract class SessionOpSpi 
{

    protected transient Session session = null;

    public SessionOpSpi(Session s) {
        this.session = s;
    }

    public abstract Hashtable getDebugState(); 

    /**
     * @param full if true dump packet
     *
     * @return a vector of ack entry debug strings for delivered
     *         messages in this Session
     */
    public abstract Vector<String> getDebugMessages(boolean full);

    public abstract void checkAckType(int type) throws BrokerException;

    /**
     * @return a list of SysMessageID that have been delivered but pending
     *         acknowledgement in this Session
     */
    public abstract List<SysMessageID> getPendingAcks(ConsumerUID uid);

    /**
     * Called right before put the message on the 'wire' to client
     * @param con the consumer the message to be delivered to
     * @param msg the message
     * @return true to deliver the message
     */
    public abstract boolean onMessageDelivery(ConsumerSpi con, Object msg);

    public String toString() {
        return "SessionOp["+session+"]";
    }

    /**
     * Detach a consumer from this session and destory the consumer 
     *
     * @param con the consumer to detach
     * @param id last SysMessageID seen (null indicates all have been seen)
     * @param redeliverPendingConsume - redeliver pending messages
     * @param redeliverAll  ignore id and redeliver all
     */
    public abstract boolean detachConsumer(ConsumerSpi con, SysMessageID id, boolean idInTransaction,
                    boolean redeliverPendingConsume, boolean redeliverAll, Connection conn);

    /**
     * Process transaction ack
     * @param cuid the consumer the message was delivered
     * @param id the message id
     * @param tuid the transaction id
     * @param deliverCnt if > 0, update redelivery count 
     * @return an object specific its handler
     * @throw BrokerException
     */
    public abstract Object ackInTransaction(ConsumerUID cuid, SysMessageID id,
                                            TransactionUID tuid, int deliverCnt) 
                                            throws BrokerException;

    /**
     * Called on closing the session 
     * @param conn the connection this session belongs to
     */
    public abstract void close(Connection conn); 

    /**
     * Handles an undeliverable message. 
     *
     * @param con the consumer the message was delivered to
     * @param id  the message id
     * @param deliverCnt 
     * @param updateDeliveryCntOnly  
     * @return an object specific to its handler 
     * @throw BrokerException
     */
    public abstract Object handleUndeliverable(
                               ConsumerSpi con, SysMessageID id, 
                               int deliverCnt, boolean updateDeliveryCntOnly)
                               throws BrokerException;

    /**
     * Handles undeliverable dead message. 
     *
     * @param con the consumer the message was delivered to
     * @param id  the message id
     * @param deadReason
     * @param thr
     * @param comment 
     * @param deliverCnt 
     * @return an object specific to its handler 
     * @throw BrokerException
     */
    public abstract Object handleDead(ConsumerSpi con,
           SysMessageID id, RemoveReason deadReason, Throwable thr, 
           String comment, int deliverCnt) throws BrokerException;

    /**
     * Acknowledge a message
     *
     * postAckMessage must be called immediately after this call
     *
     * @param cuid the consumer the message was delivered to
     * @param id  the message id
     * @param tuid the transaction id, null if no transaction 
     * @param extra info 
     * @param extra info 
     * @param ackack whether client waiting for a reply 
     * @return an object specific to its handler 
     * @throw BrokerException
     */
    public abstract Object ackMessage(ConsumerUID cuid, SysMessageID id,
            TransactionUID tuid, Object extra1, 
            HashMap extra2, boolean ackack) 
            throws BrokerException;

    public abstract void postAckMessage(ConsumerUID cuid, 
                           SysMessageID id, boolean ackack)
                           throws BrokerException;


    /**
     * @param cuid consumer UID
     * @return true if the session has delivered messages pending for the consumer 
     */
    public abstract boolean hasDeliveredMessages(ConsumerUID cuid);
}


