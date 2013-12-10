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

package com.sun.messaging.bridge.service.jms.tx;

import java.util.logging.Logger;
import java.util.Properties;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;


/**
 *
 * A TransactionManager adapter interface.  At startup, the method call sequences are 
 *     1. instantiation
 *     2. setLogger
 *     4. init(props)
 *     5. if (registerRM()) registerRM(...)  <-- 0 or more times
 *     6. ...
 *     7. shutdown
 *
 * @author amyk
 */
public interface TransactionManagerAdapter {

    /**
     */
    public void setLogger(Logger logger);

    /**
     * This will be the first method to be called after instantiation
     * @param props the properties 
     * @param reset if true, clear existing data  
     */
    public void init(Properties props, boolean reset) throws Exception; 


    /**
     * 
     * @return true if RM pre-registration is required
     */
    public boolean registerRM();

    /**
     * Register a resource manager to the transaction manager
     *
     * @param rmName resource manager name which uniquely identify
     *               the RM in global transactions that it's going 
     *               be participanting
     * @param xar a XAResource object that is representing the RM
     *
     */
    public void registerRM(String rmName, XAResource xar) throws Exception;

    /**
     * Unregister a resource manager from the transaction manager. 
     * Afterward, the resource manager should not participant
     * any global trasanctions that are managed by this transactio manager 
     *
     * @param rmName The resource manage name that is used in registerRM()
     */
    public void unregisterRM(String rmName) throws Exception;

    /**
     * @return the transaction manager object that implements javax.transaction.TransactionManager
     */
    public TransactionManager getTransactionManager() throws Exception;

    /**
     * @return all transactions
     */
    public String[] getAllTransactions() throws Exception;

    /** 
     * Shutdown the transaction manager
     */
    public void shutdown() throws Exception;

}
