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
 * @(#)HeartbeatCallback.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.heartbeat.spi;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 */
public interface HeartbeatCallback {

    /**
     * The implementation of this method could check the validity
     * of the received data and throw IOException to indicate the
     * data should be discarded - that is it should not be counted
     * in calculating timeout, e.g.
     *
     * 1. The received data could come from a different store session
     *    of a broker instance running on the endpoint
     * 2. The received data could be a UDP broadcast
     *
     * @param sender The sender where the data received from
     * @param data The data received from the remote endpoint
     *
     * @throws IOException if the data should be discarded
     */
    void
    heartbeatReceived(InetSocketAddress sender, byte[] data) throws IOException;


    /**
     * This method should be called before each send to the endpoint
     *
     * @param key The opaque key associated with this endpoint 
     * @param endpoint The endpoint to send heartbeat to
     *
     * @return array of bytes for sending to the endpoint
     *
     * @throws IOException
     */
    byte[]
    getBytesToSend(Object key, InetSocketAddress endpoint) throws IOException;


    /**
     * Timed out in receiving data from the remote endpoint 
     *
     * @param key The opaque key associated with this endpoint 
     * @param endpoint The endpoint
     * @param reason The IOException if any associated with the timeout or null
     *               
     */
    void
    heartbeatTimeout(Object key, InetSocketAddress endpoint, IOException reason);

    /**
     * Heartbeat send io exception occurred
     *
     * @param key The opaque key associated with this endpoint 
     * @param endpoint The endpoint
     * @param reason The IOException if any associated with the timeout or null
     *               
     */
    void
    heartbeatIOException(Object key, InetSocketAddress endpoint, IOException reason);

}
