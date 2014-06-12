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

package com.sun.messaging.portunif;

import java.nio.charset.Charset;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.portunif.PUContext;
import org.glassfish.grizzly.portunif.ProtocolFinder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.nio.transport.TCPNIOConnection;



public class PortMapperProtocolFinder implements ProtocolFinder {

    private static boolean DEBUG = false;
    public static final int PORTMAPPER_VERSION_MAX_LEN = 128;

    private PUServiceCallback callback = null;
    private boolean ssl = false;

    public PortMapperProtocolFinder(PUServiceCallback callback, boolean ssl) {
        this.callback = callback;
        this.ssl = ssl;
    }

    /**
     */
    @Override
    public Result find(final PUContext puContext, final FilterChainContext ctx) {

        final Buffer input = ctx.getMessage();

        int len = input.remaining();
        if (len <= 0) {
            return Result.NEED_MORE_DATA;
        }
        String data = input.toStringContent(Charset.forName("UTF-8"), 0,
                                Math.min(PORTMAPPER_VERSION_MAX_LEN, len));
        int ind1 = data.indexOf("\r");
        int ind2 = data.indexOf("\n");
        if (DEBUG) {
            String logmsg = this+": input="+input.toStringContent()+
                                ", newline index: "+ind1+", "+ind2;
            if (callback != null) {
                callback.logInfo(logmsg);
            } else {
                System.out.println(logmsg);
            }
        }

        if (ind1 == 0 || ind2 == 0) {
            return Result.NOT_FOUND;
        }
        int indmax = Math.max(ind1, ind2);
        if (indmax < 0) {
            if (len >= PORTMAPPER_VERSION_MAX_LEN) {
                return Result.NOT_FOUND;
            }
            return Result.NEED_MORE_DATA;
        }
        
        int indmin = Math.min(ind1, ind2);
        if (!data.substring(0, (indmin < 0 ? indmax:indmin)).matches("\\d+")) {
            if (DEBUG) {
                String logmsg = this+": data not all digits before newline:["+
                                data.substring(0, (indmin < 0 ? indmax:indmin))+"]";
                if (callback !=  null) {
                    callback.logInfo(logmsg);
                } else {
                    System.out.println(logmsg);
                }
            }
            return Result.NOT_FOUND;
        }
        if (DEBUG) {
            String logmsg = this+": FOUND input="+input.toStringContent();
            if (callback !=  null) {
                callback.logInfo(logmsg);
            } else {
                System.out.println(logmsg);
            }
        }
        if (callback == null) {
            return Result.FOUND;
        }
        Connection c = ctx.getConnection();
        if (c instanceof TCPNIOConnection) {
            SocketAddress sa = ((TCPNIOConnection)c).getPeerAddress();
            if (sa instanceof InetSocketAddress) {
                if (callback.allowConnection((InetSocketAddress)sa, ssl)) {
                    return Result.FOUND;
                }
            }
        }
        return Result.NOT_FOUND;
    }
}

