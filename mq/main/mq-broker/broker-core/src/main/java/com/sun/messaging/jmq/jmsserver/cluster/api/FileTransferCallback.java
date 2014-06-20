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
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

import java.io.*;
import java.util.Map;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * Interface to facilitate file transfer
 */
public interface FileTransferCallback  {

    //module names
    public static final String STORE = "store";

    /**
     * Get the file input stream for the file to be transfered
     *
     * @param filename the relative filename to be transfered
     * @throws BrokerException
     */
    public FileInputStream getFileInputStream(String filename, BrokerAddress to, Map props)
                                              throws BrokerException;

    /**
     * Get the file output stream for file to be transfered over 
     *
     * @param tmpfilename the relative temporary filename to be used during transfer
     * @param first file of the set of files transfering over 
     * @throws BrokerException
     */
    public FileOutputStream getFileOutputStream(String tmpfilename,
                                                String brokerID, String uuid,
                                                boolean firstOfSet,
                                                BrokerAddress from)
                                                throws BrokerException;

    /**
     * Called when the file has been successfully transfered over
     *
     * @param tmpfilename the temporary file name used
     * @param filename the real file name to be renamed to from tmpfilename 
     * @param lastModTime the last modification time of the file 
     * @param success whether the file transfer over is success
     * @param ex if success false, any exception
     */
    public void doneTransfer(String tmpfilename, String filename, 
                             String brokerID, long lastModTime,
                             boolean success, BrokerAddress from)
                             throws BrokerException;

    /**
     * Called when the set of files have been successfully transfered over
     */
    public void allDoneTransfer(String brokerID, String uuid, 
                                BrokerAddress from)
                                throws BrokerException;
}
