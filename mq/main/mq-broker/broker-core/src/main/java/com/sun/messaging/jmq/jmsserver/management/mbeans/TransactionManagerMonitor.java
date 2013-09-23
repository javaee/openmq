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
 * @(#)TransactionManagerMonitor.java	1.16 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.Vector;
import java.util.Enumeration;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;

import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.data.TransactionList;
import com.sun.messaging.jmq.jmsserver.data.TransactionUID;
import com.sun.messaging.jmq.jmsserver.data.TransactionState;

import com.sun.messaging.jmq.jmsserver.management.util.TransactionUtil;

public class TransactionManagerMonitor extends MQMBeanReadOnly  {
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(TransactionAttributes.NUM_TRANSACTIONS,
					Integer.class.getName(),
					mbr.getString(mbr.I_TXN_MGR_ATTR_NUM_TRANSACTIONS),
					true,
					false,
					false),

	    new MBeanAttributeInfo(TransactionAttributes.NUM_TRANSACTIONS_COMMITTED,
					Long.class.getName(),
					mbr.getString(mbr.I_TXN_MGR_ATTR_NUM_TRANSACTIONS_COMMITTED),
					true,
					false,
					false),

	    new MBeanAttributeInfo(TransactionAttributes.NUM_TRANSACTIONS_ROLLBACK,
					Long.class.getName(),
					mbr.getString(mbr.I_TXN_MGR_ATTR_NUM_TRANSACTIONS_ROLLBACK),
					true,
					false,
					false)
			};

    private static MBeanParameterInfo[] getTransactionInfoByIDSignature = {
		    new MBeanParameterInfo("transactionID", String.class.getName(), 
			mbr.getString(mbr.I_TXN_MGR_OP_PARAM_TXN_ID))
			    };

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo(TransactionOperations.GET_TRANSACTION_IDS,
		mbr.getString(mbr.I_TXN_MGR_OP_GET_TRANSACTION_IDS),
		    null , 
		    String[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(TransactionOperations.GET_TRANSACTION_INFO,
		mbr.getString(mbr.I_TXN_MGR_OP_GET_TRANSACTION_INFO),
		    null , 
		    CompositeData[].class.getName(),
		    MBeanOperationInfo.INFO),

	    new MBeanOperationInfo(TransactionOperations.GET_TRANSACTION_INFO_BY_ID,
		mbr.getString(mbr.I_TXN_MGR_OP_GET_TRANSACTION_INFO_BY_ID),
		    getTransactionInfoByIDSignature, 
		    CompositeData.class.getName(),
		    MBeanOperationInfo.INFO)
		};
	
    private static String[] txnNotificationTypes = {
		    TransactionNotification.TRANSACTION_COMMIT,
		    TransactionNotification.TRANSACTION_PREPARE,
		    TransactionNotification.TRANSACTION_ROLLBACK
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    txnNotificationTypes,
		    TransactionNotification.class.getName(),
		    mbr.getString(mbr.I_TXN_NOTIFICATIONS)
		    )
		};

    private long numTransactionsCommitted = 0;
    private long numTransactionsRollback = 0;

    public TransactionManagerMonitor()  {
	super();
    }

    public Integer getNumTransactions()  {
	TransactionList[] tls = Globals.getDestinationList().getTransactionList(null);
        TransactionList tl = tls[0]; //PART
	Vector transactions = tl.getTransactions(-1);

	return (Integer.valueOf(transactions.size()));
    }

    public Long getNumTransactionsCommitted()  {
	return (Long.valueOf(numTransactionsCommitted));
    }

    public Long getNumTransactionsRollback()  {
	return (Long.valueOf(numTransactionsRollback));
    }

    public void resetMetrics()  {
        numTransactionsCommitted = 0;
        numTransactionsRollback = 0;
    }

    public String[] getTransactionIDs() throws MBeanException  {
	return (TransactionUtil.getTransactionIDs());
    }

    public CompositeData[] getTransactionInfo() throws MBeanException {
	CompositeData cds[] = null;

	try  {
	    cds = TransactionUtil.getTransactionInfo();
	} catch(Exception e)  {
	    handleOperationException(TransactionOperations.GET_TRANSACTION_INFO, e);
	}

	return (cds);
    }

    public CompositeData getTransactionInfoByID(String transactionID) throws MBeanException  {
	CompositeData cd = null;

	try  {
	    cd = TransactionUtil.getTransactionInfo(transactionID);
	} catch(Exception e)  {
	    handleOperationException(TransactionOperations.GET_TRANSACTION_INFO_BY_ID, e);
	}

	return (cd);
    }


    public String getMBeanName()  {
	return ("TransactionManagerMonitor");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_TXN_MGR_MON_DESC));
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (notifs);
    }

    public void notifyTransactionCommit(long id)  {
	TransactionNotification n;
	n = new TransactionNotification(TransactionNotification.TRANSACTION_COMMIT, 
			this, sequenceNumber++);
	n.setTransactionID(Long.toString(id));

	sendNotification(n);

        numTransactionsCommitted++;
    }

    public void notifyTransactionPrepare(long id)  {
	TransactionNotification n;
	n = new TransactionNotification(TransactionNotification.TRANSACTION_PREPARE, 
			this, sequenceNumber++);
	n.setTransactionID(Long.toString(id));

	sendNotification(n);
    }

    public void notifyTransactionRollback(long id)  {
	TransactionNotification n;
	n = new TransactionNotification(TransactionNotification.TRANSACTION_ROLLBACK, 
			this, sequenceNumber++);
	n.setTransactionID(Long.toString(id));

	sendNotification(n);

        numTransactionsRollback++;
    }
}
