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
 * @(#)BrokerMQAddress.java	1.4 06/28/07
 */ 
 
package com.sun.messaging.jmq.jmsserver.core;

import java.net.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.util.VerifyAddressException;
import com.sun.messaging.jmq.jmsserver.util.LoopbackAddressException;

public class BrokerMQAddress extends MQAddress
{
    static final long serialVersionUID = 9061061210446233838L;

    private transient InetAddress host = null;
    private transient String tostring = null;
    private transient String hostaddressNport = null;

    protected BrokerMQAddress() {} 

    protected void initialize(String addr) 
        throws MalformedURLException {
        super.initialize(addr);
        serviceName = "";
    }

    protected void initialize(String host, int port)
        throws MalformedURLException {
        super.initialize(host, port);
        serviceName = "";
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof BrokerMQAddress)) return false;

        return toString().equals(((BrokerMQAddress)obj).toString());
    }

    public int hashCode() { 
        return toString().hashCode();
    }

    public String toString() {
        if (tostring != null) return tostring;

        if (getIsHTTP()) return super.toString();  

        tostring = getSchemeName() + "://" + getHostAddressNPort() + "/" + getServiceName();
        return tostring;
 
    }

    private void initHostAddressNPort() throws MalformedURLException {
        hostaddressNport = MQAddress.getMQAddress(
            host.getHostAddress(), port).getHostName()+":"+port;
    }

    public String getHostAddressNPort() {
        return hostaddressNport;
    }

    public InetAddress getHost() { 
        return host;
    }


    public void resolveHostName() throws UnknownHostException {
        if (host == null) {
            String h = getHostName();
            if (h == null || h.equals("") || h.equals("localhost")) {
                host = InetAddress.getLocalHost();
            } else {
                host = InetAddress.getByName(h);
            }
        }
    }

    /**
     * Parses the given MQ Message Service Address and creates an
     * MQAddress object.
     */
    public static BrokerMQAddress createAddress(String host, int port)
        throws MalformedURLException, UnknownHostException {
        BrokerMQAddress ret = new BrokerMQAddress();
        ret.initialize(host, port);
        ret.resolveHostName();
        ret.initHostAddressNPort();
        return ret;
    }

    public static BrokerMQAddress createAddress(String addr)
        throws MalformedURLException, UnknownHostException {
        BrokerMQAddress ret = new BrokerMQAddress();
        ret.initialize(addr);
        ret.resolveHostName();
        ret.initHostAddressNPort();
        return ret;
    }

    /**
     *
     * @param nolocalhost if true no return loopback address
     *
     */
    public static InetAddress resolveBindAddress(String listenHost, 
                                                 boolean nolocalhost)
                                                 throws BrokerException,
                                                 UnknownHostException {
        if (listenHost == null) return null;
        if (listenHost.trim().length() == 0) return null;

        InetAddress iaddr = null; 
        if (nolocalhost && listenHost.equals("localhost")) {
            iaddr = InetAddress.getLocalHost();
        } else {
            iaddr = InetAddress.getByName(listenHost);
        }
        if (!nolocalhost) return iaddr;

        checkLoopbackAddress(iaddr, listenHost);
        return iaddr;
    }

    /**
     *
     */
    public static void checkLoopbackAddress(InetAddress iaddr, String hostname)
                                            throws BrokerException,
                                            UnknownHostException {
        if (iaddr == null) return;

        if (iaddr.isLoopbackAddress()) {
            throw new LoopbackAddressException(Globals.getBrokerResources().getString(
            Globals.getBrokerResources().X_LOOPBACKADDRESS, 
            (hostname == null ? "":hostname+"["+iaddr+"]")));
        }
        return;
    }

}
