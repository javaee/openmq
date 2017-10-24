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
 * @(#)ClusterTxnInquiryInfo.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.raptor;

import java.io.*;
import java.util.*;
import java.nio.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.io.GPacket;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.Status;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.multibroker.ClusterGlobals;
import com.sun.messaging.jmq.jmsserver.multibroker.raptor.ProtocolGlobals;

/**
 * An instance of this class is intended to be used one direction only
 */

public class ClusterTxnInquiryInfo 
{
    protected Logger logger = Globals.getLogger();

    private Long transactionID = null;
    private BrokerAddress txnhome = null;
    private Long replyXid = null;

    private GPacket pkt = null;

    private ClusterTxnInquiryInfo(Long txnID, BrokerAddress txnhome, Long replyXid) {
        this.transactionID = txnID;
        this.txnhome = txnhome;
        this.replyXid = replyXid;
    }

    private ClusterTxnInquiryInfo(GPacket pkt) {
        this.pkt = pkt;
    }

    public static ClusterTxnInquiryInfo newInstance(Long txnID, BrokerAddress txnhome, Long replyXid) {
        return new ClusterTxnInquiryInfo(txnID, txnhome, null); 
    }

    /**
     *
     * @param pkt The GPacket to be unmarsheled
     */
    public static ClusterTxnInquiryInfo newInstance(GPacket pkt) {
        return new ClusterTxnInquiryInfo(pkt);
    }

    public GPacket getGPacket() throws IOException { 

        GPacket gp = GPacket.getInstance();
        gp.setType(ProtocolGlobals.G_TRANSACTION_INQUIRY);
        gp.putProp("transactionID", transactionID);
        gp.setBit(gp.A_BIT, true);
        if (replyXid != null) gp.putProp("X", replyXid);
        if (txnhome != null) gp.putProp("transactionHome", txnhome.toProtocolString());

        return gp;
    }

    public Long getTransactionID() {
        assert ( pkt != null );
        return  (Long)pkt.getProp("transactionID");
    }

    public BrokerAddress getTransactionHome() {
        assert ( pkt != null );
        String home = (String)pkt.getProp("transactionHome");
        if (home == null) return null;
        try {
        return Globals.getMyAddress().fromProtocolString(home);
        } catch (Exception e) {
        Globals.getLogger().log(Globals.getLogger().WARNING,  
        "Unable to get transaction home broker address for TID="+getTransactionID()+":"+e.getMessage());
        }
        return null;
    }

    public Long getXid() {
        assert ( pkt != null );
        return  (Long)pkt.getProp("X");
    }

   /**
    * To be called by sender
    */
    public String toString() {

        if (pkt == null) {
        StringBuffer buf = new StringBuffer();

        buf.append("\n\tTransactionID = ").append(transactionID);

        if (txnhome != null) {
           buf.append("\n\tTransactionHome = ").append(txnhome.toProtocolString());
        }

        if (replyXid != null) {
           buf.append("\n\tXID = ").append(replyXid);
        }

        return buf.toString();
        }

        StringBuffer buf = new StringBuffer();

        if (getTransactionID() != null) {
            buf.append("\n\tTransactionID = ").append(getTransactionID());
        }
        if (pkt.getProp("transactionHome") != null) {
            buf.append("\n\tTransactionHome = ").append((String)pkt.getProp("transactionHome"));
        }

        if (pkt.getProp("X") != null) {
            buf.append("\n\tXID = ").append(pkt.getProp("X"));
        }

        return buf.toString();
    }

}
