/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)ClusterRouter.java	1.16 07/23/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.router;

import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.core.*;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.selector.SelectorFormatException;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.io.*;


public interface ClusterRouter
{
    public void forwardMessage(PacketReference ref, Collection consumers);

    /* REPLACE THE NEXT SEVERAL PROTOCOL MESSAGES WITH handleCtrlMsg
     */

    public void addConsumer(Consumer c) 
       throws BrokerException, IOException, SelectorFormatException;

    public void removeConsumer(com.sun.messaging.jmq.jmsserver.core.ConsumerUID c,
        Map<TransactionUID, LinkedHashMap<SysMessageID, Integer>> pendingMsgs, boolean cleanup)
        throws BrokerException, IOException;

    public void removeConsumers(ConnectionUID uid)
       throws BrokerException, IOException;

    public void brokerDown(com.sun.messaging.jmq.jmsserver.core.BrokerAddress ba)
       throws BrokerException, IOException;

    public void shutdown();

    /*
     * END REPACEMENT
     */

    public void handleJMSMsg(Packet p, Map<ConsumerUID, Integer> consumers,
                             BrokerAddress sender,
                             boolean sendMsgRedeliver)
                             throws BrokerException;

    public void handleAck(int ackType, SysMessageID sysid, ConsumerUID cuid, 
                          Map optionalProps) throws BrokerException;

    public void handleAck2P(int ackType, SysMessageID[] sysids, ConsumerUID[] cuids, 
                            Map optionalProps, Long txnID, 
                            com.sun.messaging.jmq.jmsserver.core.BrokerAddress txnHomeBroker)
                            throws BrokerException;

    public void handleCtrlMsg(int type, HashMap props)
                              throws BrokerException;

   public Hashtable getDebugState();
}
