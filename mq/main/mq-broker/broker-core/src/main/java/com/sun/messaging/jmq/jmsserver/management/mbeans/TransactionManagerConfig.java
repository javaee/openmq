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
 * @(#)TransactionManagerConfig.java	1.15 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Enumeration;
import java.util.Vector;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanException;

import javax.transaction.xa.XAResource;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionList;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;
import com.sun.messaging.jmq.jmsserver.data.RollbackReason;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.jmsserver.data.handlers.TransactionHandler;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.util.JMQXid;

import com.sun.messaging.jms.management.server.*;

public class TransactionManagerConfig extends MQMBeanReadWrite  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(TransactionAttributes.NUM_TRANSACTIONS,
					Integer.class.getName(),
					mbr.getString(mbr.I_TXN_MGR_ATTR_NUM_TRANSACTIONS),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] txnIdParam = {
		    new MBeanParameterInfo("transactionID", String.class.getName(),
					mbr.getString(mbr.I_TXN_MGR_OP_PARAM_TXN_ID))
			    };

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(TransactionOperations.COMMIT,
		mbr.getString(mbr.I_TXN_MGR_OP_COMMIT),
		    txnIdParam, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),

	    new MBeanOperationInfo(TransactionOperations.GET_TRANSACTION_IDS,
		mbr.getString(mbr.I_TXN_MGR_OP_GET_TRANSACTION_IDS),
		    null, 
		    String[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(TransactionOperations.ROLLBACK,
		mbr.getString(mbr.I_TXN_MGR_OP_ROLLBACK),
		    txnIdParam, 
		    Void.TYPE.getName(),
		    MBeanOperationInfo.ACTION),
		};

    public TransactionManagerConfig()  {
	super();
    }

    public Integer getNumTransactions()  {
	TransactionList[] tls = Globals.getDestinationList().getTransactionList(null);
        TransactionList tl = tls[0]; //PART
	Vector transactions = tl.getTransactions(-1);

	return (Integer.valueOf(transactions.size()));
    }

    public void commit(String transactionID) throws MBeanException  {
	doRollbackCommit(transactionID, false);
    }

    public String[] getTransactionIDs() throws MBeanException  {
	TransactionList[] tls = Globals.getDestinationList().getTransactionList(null);
        TransactionList tl = tls[0]; //PART
	Vector transactions = tl.getTransactions(-1);
	String ids[];

	if ((transactions == null) || (transactions.size() == 0))  {
	    return (null);
	}

	ids = new String [ transactions.size() ];

	Enumeration e = transactions.elements();

	int i = 0;
	while (e.hasMoreElements()) {
	    TransactionUID tid = (TransactionUID)e.nextElement();
	    long		txnID = tid.longValue();
	    String id;

	    try  {
	        id = Long.toString(txnID);

	        ids[i] = id;
	    } catch (Exception ex)  {
		handleOperationException(TransactionOperations.GET_TRANSACTION_IDS, ex);
	    }

	    i++;
	}

	return (ids);
    }

    public void rollback(String transactionID) throws MBeanException  {
	doRollbackCommit(transactionID, true);
    }

    public void doRollbackCommit(String transactionID, boolean rollback) 
				throws MBeanException  {
	try  {
	    long longTid = 0;

	    if (transactionID == null)  {
	        throw new Exception("Null transaction ID");
	    }

	    try  {
		longTid = Long.parseLong(transactionID);
	    } catch (Exception e)  {
	        throw new Exception("Invalid transaction ID: " + transactionID);
	    }

	    TransactionUID tid = new TransactionUID(longTid);
	    TransactionList[] tls = Globals.getDestinationList().getTransactionList(null);
            TransactionList tl = null;
	    TransactionState ts = null;
            for (int i = 0; i < tls.length; i++) {
                 tl = tls[i]; 
	         if (tl == null)  {
                     continue;
	         }
	         ts = tl.retrieveState(tid);
	         if (ts == null)  {
                     continue;
                 }
                 break;
            }

	    if (ts == null)  {
	        throw new Exception(rb.getString(rb.E_NO_SUCH_TRANSACTION, tid));
	    }

	    if (ts.getState() != TransactionState.PREPARED)  {
	        throw new Exception(rb.getString(rb.E_TRANSACTION_NOT_PREPARED, tid));
	    }

	    JMQXid xid = tl.UIDToXid(tid);

	    if (xid == null) {
	        throw new Exception(rb.getString(rb.E_INTERNAL_BROKER_ERROR, 
				"Could not find Xid for " + tid));
	    }

	    PacketRouter pr = Globals.getPacketRouter(0);

	    if (pr == null)  {
	        throw new Exception(rb.getString(rb.E_INTERNAL_BROKER_ERROR,
					"Could not locate Packet Router"));
	    }

	    TransactionHandler thandler = (TransactionHandler)
	    			pr.getHandler(PacketType.ROLLBACK_TRANSACTION);

	    if (thandler == null)  {
	        throw new Exception(rb.getString(rb.E_INTERNAL_BROKER_ERROR,
					"Could not locate Transaction Handler"));
	    }

	    if (rollback)  {
	        thandler.doRollback(tl, tid, xid, null,
                                    ts, null, null, RollbackReason.ADMIN);
	    } else  {
		thandler.doCommit(tl, tid, xid, 
                                  Integer.valueOf(XAResource.TMNOFLAGS), 
                                  ts, null, false, null, null);
	    }
	} catch(Exception e)  {
	    String opName;
	    if (rollback)  {
		opName = TransactionOperations.ROLLBACK;
	    } else  {
		opName = TransactionOperations.COMMIT;
	    }

	    handleOperationException(opName, e);
	}
    }


    public String getMBeanName()  {
	return ("TransactionManagerConfig");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_TXN_MGR_CFG_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (null);
    }
}
