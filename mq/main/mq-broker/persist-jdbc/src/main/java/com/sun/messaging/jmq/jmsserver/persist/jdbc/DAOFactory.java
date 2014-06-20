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
 * @(#)DAOFactory.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.jdbc;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.Logger;

import java.util.List;
import java.util.ArrayList;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.ext.JMSBGDAO;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.ext.TMLogRecordDAO;
import com.sun.messaging.jmq.jmsserver.persist.jdbc.ext.TMLogRecordDAOJMSBG;

/**
 * Factory for DAO object.
 */
public abstract class DAOFactory {

    protected VersionDAO versionDAO = null;
    protected BrokerDAO brokerDAO = null;
    protected StoreSessionDAO storeSessionDAO = null;
    protected PropertyDAO propertyDAO = null;
    protected MessageDAO messageDAO = null;
    protected DestinationDAO destinationDAO = null;
    protected ConsumerDAO consumerDAO = null;
    protected ConsumerStateDAO consumerStateDAO = null;
    protected ConfigRecordDAO configRecordDAO = null;
    protected TransactionDAO transactionDAO = null;
    protected List daoList = null;

    //JMSBridgeStore interface support
    protected TMLogRecordDAO tmLogRecordDAOJMSBG = null;
    protected JMSBGDAO jmsbgDAO = null;

    public abstract VersionDAO getVersionDAO() throws BrokerException;

    public abstract BrokerDAO getBrokerDAO() throws BrokerException;

    public abstract StoreSessionDAO getStoreSessionDAO() throws BrokerException;

    public abstract PropertyDAO getPropertyDAO() throws BrokerException;

    public abstract MessageDAO getMessageDAO() throws BrokerException;

    public abstract DestinationDAO getDestinationDAO() throws BrokerException;

    public abstract ConsumerDAO getConsumerDAO() throws BrokerException;

    public abstract ConsumerStateDAO getConsumerStateDAO() throws BrokerException;

    public abstract ConfigRecordDAO getConfigRecordDAO() throws BrokerException;

    public abstract TransactionDAO getTransactionDAO() throws BrokerException;

    public abstract TMLogRecordDAO getTMLogRecordDAOJMSBG() throws BrokerException;
    public abstract JMSBGDAO getJMSBGDAO() throws BrokerException;

    public List getAllDAOs() throws BrokerException {

        if ( daoList == null ) {
            synchronized( this ) {
                if ( daoList == null ) {
                    ArrayList list = new ArrayList(10);
                    list.add( getVersionDAO() );
                    list.add( getBrokerDAO() );
                    list.add( getStoreSessionDAO() );
                    list.add( getPropertyDAO() );
                    list.add( getConfigRecordDAO() );
                    list.add( getConsumerDAO() );
                    list.add( getConsumerStateDAO() );
                    list.add( getDestinationDAO() );
                    list.add( getMessageDAO() );
                    list.add( getTransactionDAO() );
                    list.add( getTMLogRecordDAOJMSBG() );
                    list.add( getJMSBGDAO() );
                    daoList = list;
                }
            }
        }

        return daoList;
    }
}
