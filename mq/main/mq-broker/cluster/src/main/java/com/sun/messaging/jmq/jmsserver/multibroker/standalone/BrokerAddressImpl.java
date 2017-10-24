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
 * @(#)BrokerAddressImpl.java	1.10 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.standalone;

import java.io.*;
import java.util.*;
import com.sun.messaging.jmq.util.UID;
import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;

/**
 * This class implements the <code>BrokerAddress</code> for
 * a standalone broker.
 */
class BrokerAddressImpl extends BrokerAddress {
    public BrokerAddressImpl() {
    }

    public Object clone() {
        try {
            return super.getObjectClone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.equals(this);
    }

    public int hashCode() {
        return 0;
    }

    public boolean getHAEnabled() {
        return false;
    }
    public String getBrokerID() {
        return null;
    }
    public UID getBrokerSessionUID() {
        return null;
    }
    public UID getStoreSessionUID() {
        return null;
    }
    public void setStoreSessionUID(UID uid) {
    }
    public String getInstanceName() {
        return null;
    }

    public String toProtocolString() {
        return null;
    }

    public BrokerAddress fromProtocolString(String s) throws Exception {
        throw new UnsupportedOperationException(this.getClass().getName()+".fromProtocolString");
    }

    public void writeBrokerAddress(DataOutputStream dos) {
    }

    public void writeBrokerAddress(OutputStream os) {
    }

    public void readBrokerAddress(DataInputStream dis) {
    }

    public void readBrokerAddress(InputStream is) {
    }
}

/*
 * EOF
 */
