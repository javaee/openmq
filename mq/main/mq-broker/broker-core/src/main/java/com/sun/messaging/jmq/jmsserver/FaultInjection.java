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
 * @(#)FaultInjection.java	1.20 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver;

import java.util.Map;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.RuntimeFaultInjection;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.Globals;

/**
 * All fault target constants start with FAULT_ and
 * only fault target constant starts with FAULT_
 *
 */
public class FaultInjection extends RuntimeFaultInjection
{
     static FaultInjection fault = null;

     Logger logger = Globals.getLogger();

     /**
      * 1 is before processing
      * 2 is after processing, before reply
      * 3 is after reply
      */
     public static final String STAGE_1 = "1";
     public static final String STAGE_2 = "2";
     public static final String STAGE_3 = "3";
     public static final String STAGE_1_5 = "1_5";
     public static final String STAGE_1_6 = "1_6";

     public static final String FAULT_TXN_START_1 = "txn.start.1";
     public static final String FAULT_TXN_END_1 = "txn.end.1";
     public static final String FAULT_TXN_PREPARE_1 = "txn.prepare.1";
     public static final String FAULT_TXN_COMMIT_1 = "txn.commit.1";
     public static final String FAULT_TXN_ROLLBACK_1 = "txn.rollback.1";
     public static final String FAULT_TXN_COMMIT_1_1 = "txn.commit.1_1";
     public static final String FAULT_TXN_COMMIT_1_5 = "txn.commit.1_5";
     public static final String FAULT_TXN_COMMIT_1_6 = "txn.commit.1_6";
     public static final String FAULT_TXN_COMMIT_1_7 = "txn.commit.1_7";
     public static final String FAULT_TXN_COMMIT_1_7_1 = "txn.commit.1_7_1";

     public static final String FAULT_TXN_ACK_1_3 = "txn.ack.1_3";
     public static final String FAULT_TXN_ACK_1_5 = "txn.ack.1_5";
     public static final String FAULT_TXN_UPDATE_1_3_END = "txn.update.1_3.end";
     public static final String FAULT_TXN_UPDATE_1_3_PREPARE = "txn.update.1_3.prepare";

     //takes prop DST_NAME_PROP
     public static final String FAULT_TXN_COMMIT_1_8 = "txn.commit.1_8";

     public static final String FAULT_TXN_START_2 = "txn.start.2";
     public static final String FAULT_TXN_END_2 = "txn.end.2";
     public static final String FAULT_TXN_PREPARE_2 = "txn.prepare.2";
     public static final String FAULT_TXN_PREPARE_2_0 = "txn.prepare.2_0";
     public static final String FAULT_TXN_COMMIT_2 = "txn.commit.2";
     public static final String FAULT_TXN_ROLLBACK_2 = "txn.rollback.2";

     public static final String FAULT_TXN_START_3 = "txn.start.3";
     public static final String FAULT_TXN_END_3 = "txn.end.3";
     public static final String FAULT_TXN_PREPARE_3 = "txn.prepare.3";
     public static final String FAULT_TXN_COMMIT_3 = "txn.commit.3";
     public static final String FAULT_TXN_ROLLBACK_3 = "txn.rollback.3";
     public static final String FAULT_TXN_PREPARE_3_KILL_CLIENT = "txn.prepare.3_kill_client";
     public static final String FAULT_TXN_PREPARE_3_CLOSE_CLIENT = "txn.prepare.3_close_client";

     public static final String FAULT_TXN_PERSIST_WORK_1_5 = "txn.persist_work.1_5";

     // after db update but before reply
     public static final String FAULT_TXN_COMMIT_4 = "txn.commit.4";
     public static final String FAULT_TXN_ROLLBACK_4 = "txn.rollback.4";
     public static final String FAULT_TXN_ROLLBACK_DBCONSTATE_CLEARTID = 
                         "txn.rollback.dbconstate.cleartid.exception";


     public static final String FAULT_JDBC_GETCONN_1 = "jdbc.getconn.1";
     public static final String FAULT_CONSUMER_ADD_1 = "consumer.add.1";

     // additional properties supported on acks
     //     mqAckCount - number of acks received
     //     mqIsTransacted - is in transaction
     public static final String MSG_ACKCOUNT_PROP = "mqAckCount";
     public static final String DST_NAME_PROP = "mqDestinationName";
     public static final String BROKERID_PROP = "mqBrokerID"; 
     private static final String SLEEP_INTERVAL_PROP = "mqSleepInterval"; //in secs
     public static final String FAULT_COUNT_PROP = "mqFaultCount";
     private static final int   SLEEP_INTERVAL_DEFAULT = 60; 

     public static final String FAULT_ACK_MSG_1 = "msg.ack.1";
     public static final String FAULT_ACK_MSG_2 = "msg.ack.2";
     public static final String FAULT_ACK_MSG_3 = "msg.ack.3";

     public static final String FAULT_ACK_MSG_1_5 = "msg.ack.1_5";


     // on messages all message properties are set plut
     //      mqMsgCount - mumber of messages received
     //      MQIsTransacted - is in transaction
     public static final String FAULT_SEND_MSG_1 = "msg.send.1";
     public static final String FAULT_SEND_MSG_2 = "msg.send.2";
     public static final String FAULT_SEND_MSG_3 = "msg.send.3";

     public static final String FAULT_SEND_MSG_1_EXCEPTION = "msg.send.1.exception";
     public static final String FAULT_SEND_MSG_1_SLEEP = "msg.send.1.sleep";
     public static final String FAULT_SEND_MSG_1_SLEEP_EXCEPTION = "msg.send.1.sleep.exception";
     public static final String FAULT_PACKET_ROUTER_1_SLEEP = "pkt.router.1.sleep";

     public static final String FAULT_LOAD_DST_1_5 = "load.dst.1_5";

     public static final String FAULT_STORE_DURA_1 = "dura.store.1";

     public static final String FAULT_MSG_EXPIRE_AT_DELIVERY = "msg.expire.at_delivery";
     public static final String FAULT_MSG_DESTROY_1_5_KILL = "msg.destroy.1_5.kill";
     public static final String FAULT_MSG_DESTROY_1_5_EXCEPTION = "msg.destroy.1_5.exception";

     public static final String FAULT_MSG_EXPIRE_REAPER_EXPIRE1 = "msg.expirereaper.expire1";
     public static final String FAULT_CONSUMER_SLEEP_BEFORE_MSGS_REMOVE_NEXT = 
                                      "consumer.sleep.before.msgs.removeNext";

     // on taking over
     public static final String FAULT_HA_TAKEOVER_SWITCH_SS_EXCEPTION = "ha.takeover.switch_ss.exception";
     public static final String FAULT_HA_TAKEOVER_SWITCH_SS_HALT = "ha.takeover.switch_ss.halt";
     public static final String FAULT_HA_TAKEOVER_RESTORE_EXCEPTION = "ha.takeover.restore.exception";
     public static final String FAULT_HA_TAKEOVER_RESTORE_HALT = "ha.takeover.restore.halt";

     public static final String FAULT_BDB_STORE_MERGE_COMMIT_1_EXCEPTION = "bdb.storemerge.commit.1.exception";
     public static final String FAULT_BDB_STORE_MERGE_COMMIT_1_HALT = "bdb.storemerge.commit.1.halt";
     public static final String FAULT_BDB_STORE_MERGE_COMMIT_2_EXCEPTION = "bdb.storemerge.commit.2.exception";
     public static final String FAULT_BDB_STORE_MERGE_COMMIT_2_HALT = "bdb.storemerge.commit.2.halt";

     /******************************************************************** 
      *REMOTE ACK - Protocol 
      *
      *    Support followoing properties
      *    mqAckCount:
      *      consumer broker -
      *        on sending the ?th remote ack of ackType to any remote broker 
      *      message home broker -
      *        on receiving the ?th remote ack of ackType from any remote broker 
      *
      *  Fault ID syntax: msg.remote_ack.<home>.<layer>.<ackType>.<n>
      *
      *  where <home> "home" if fault occurs on msg home broker else ""
      *        <layer> "p" for protocol, "c" for core
      *        <ackType> "" non-txn'ed or 3.5/3.6 cluster protocol  
      *                  "txnack", "txnprepare", "txnrollback", "txncommit" 
      *        <n> meaning for protocol layer ("p")
      *            "1" before send (in buffer) or unpon receive (home) protocol
      *            "2" before receive reply or send (home) reply 
      *            "3" after receive reply or send (home, in buffer) reply
      *
      ********************************************************************/
     public static final String MSG_REMOTE_ACK_TXNACK      = "txnack.";
     public static final String MSG_REMOTE_ACK_IGNORE      = "ackignore.";
     public static final String MSG_REMOTE_ACK_TXNPREPARE  = "txnprepare.";
     public static final String MSG_REMOTE_ACK_TXNROLLBACK = "txnrollback.";
     public static final String MSG_REMOTE_ACK_TXNCOMMIT   = "txncommit.";
     public static final String MSG_REMOTE_ACK_P      = "msg.remote_ack.p.";
     public static final String MSG_REMOTE_ACK_C      = "msg.remote_ack.c.";
     public static final String MSG_REMOTE_ACK_HOME_P = "msg.remote_ack.home.p.";
     public static final String MSG_REMOTE_ACK_HOME_C = "msg.remote_ack.home.c.";

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNACK_1 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNACK+"1";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNACK_2 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNACK+"2";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNACK_3 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNACK+"3";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNACK_1 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNACK+"1";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNACK_2 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNACK+"2";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNACK_3 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNACK+"3";

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNPREPARE_1 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNPREPARE+"1";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNPREPARE_2 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNPREPARE+"2";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNPREPARE_3 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNPREPARE+"3";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNPREPARE_1 =
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNPREPARE+"1";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNPREPARE_2 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNPREPARE+"2";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNPREPARE_3 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNPREPARE+"3";
     public static final String FAULT_MSG_REMOTE_ACK_C_TXNPREPARE_0_5 = 
                            MSG_REMOTE_ACK_C+MSG_REMOTE_ACK_TXNPREPARE+"0_5";
    

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNPREPARE_3_1 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNPREPARE+"3_1";

     public static final String FAULT_MSG_REMOTE_ACK_P_ACKIGNORE_1_EXCEPTION = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_IGNORE+"1.exception";

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNROLLBACK_1_EXCEPTION = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNROLLBACK+"1.exception";

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNROLLBACK_1 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNROLLBACK+"1";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNROLLBACK_2 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNROLLBACK+"2";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNROLLBACK_3 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNROLLBACK+"3";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNROLLBACK_1 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNROLLBACK+"1";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNROLLBACK_2 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNROLLBACK+"2";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNROLLBACK_3 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNROLLBACK+"3";

     public static final String FAULT_MSG_REMOTE_ACK_P_TXNCOMMIT_P_1 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNCOMMIT+"1";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNCOMMIT_P_2 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNCOMMIT+"2";
     public static final String FAULT_MSG_REMOTE_ACK_P_TXNCOMMIT_P_3 = 
                            MSG_REMOTE_ACK_P+MSG_REMOTE_ACK_TXNCOMMIT+"3";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNCOMMIT_1 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNCOMMIT+"1";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNCOMMIT_2 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNCOMMIT+"2";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_TXNCOMMIT_3 = 
                            MSG_REMOTE_ACK_HOME_P+MSG_REMOTE_ACK_TXNCOMMIT+"3";

     public static final String FAULT_MSG_REMOTE_ACK_HOME_C_TXNCOMMIT_1_7 =
                            MSG_REMOTE_ACK_HOME_C+MSG_REMOTE_ACK_TXNCOMMIT+"1_7";

     public static final String FAULT_MSG_REMOTE_ACK_P_1 = MSG_REMOTE_ACK_P+"1";
     public static final String FAULT_MSG_REMOTE_ACK_P_2 = MSG_REMOTE_ACK_P+"2";
     public static final String FAULT_MSG_REMOTE_ACK_P_3 = MSG_REMOTE_ACK_P+"3";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_1 = MSG_REMOTE_ACK_HOME_P+"1";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_2 = MSG_REMOTE_ACK_HOME_P+"2";
     public static final String FAULT_MSG_REMOTE_ACK_HOME_P_3 = MSG_REMOTE_ACK_HOME_P+"3";

     /******************
      *HEARTBEAT faults
      ******************************************************************/
     public static final String FAULT_HB_SEND = "hb.send";
     //takes BROKERID_PROP
     public static final String FAULT_HB_SEND_BROKERID = "hb.send.brokerid";
     public static final String FAULT_HB_RECV = "hb.recv";
     //takes BROKERID_PROP
     public static final String FAULT_HB_RECV_BROKERID = "hb.recv.brokerid";
     public static final String FAULT_HB_SHAREDB = "hb.sharedb";


     public static FaultInjection getInjection()
     {
         if (fault == null)
             fault = new FaultInjection();

         return fault;
     }
     public FaultInjection() {
         super();
         setProcessName("BROKER");
         setLogger(logger);
     }

     public void checkFaultAndThrowBrokerException(String value,
                Map props)
          throws BrokerException
     {
         if (!FAULT_INJECTION) return;
         if (checkFault(value, props)) {
             BrokerException ex = new BrokerException("Fault Insertion: "
                   + value);
             throw ex;
         }    
     }

     protected void exit(int exitCode) {
         // XXX use broker method to shutdown
         System.exit(1);
     }

     protected String sleepIntervalPropertyName() {
         return SLEEP_INTERVAL_PROP;
     }

     protected int sleepIntervalDefault() {
         return SLEEP_INTERVAL_DEFAULT;
     }

}     
