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
 * @(#)LinkInfo.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.fullyconnected;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class LinkInfo implements Serializable {

    public static final int SERVICE_LINK = 0;

    /**
     * non-link request must be negative
     */
    public static final int SERVICE_FILE_TRANSFER = -5000;

    BrokerAddressImpl address;
    BrokerAddressImpl configServer;
    Properties matchProps;

    private int serviceRequestType = SERVICE_LINK; 

    public LinkInfo(BrokerAddressImpl address,
        BrokerAddressImpl configServer, Properties matchProps) {
        this.address = address;
        this.configServer = configServer;
        this.matchProps = matchProps;
    }

    public BrokerAddressImpl getAddress() {
        return address;
    }

    public BrokerAddressImpl getConfigServer() {
        return configServer;
    }

    public Properties getMatchProps() {
        return matchProps;
    }

    public void setServiceRequestType(int type) throws BrokerException {
        if (type != SERVICE_FILE_TRANSFER) {
            throw new BrokerException("Unknown link service request type "+type);
        }
        serviceRequestType = type;
    }

    public int getServiceRequestType() {
        return serviceRequestType;
    }

    public boolean isLinkRequest() {
        return serviceRequestType == SERVICE_LINK;
    }

    public boolean isFileTransferRequest() {
        return serviceRequestType == SERVICE_FILE_TRANSFER;
    }

    public String toString() {
        return "Address = " + address + " configServer = " + configServer;
    }
}

/*
 * EOF
 */
