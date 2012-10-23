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
 * @(#)DefaultTrustManager.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.X509TrustManager;


/**
 * DefaultTrustManager, this is used when we are configured to
 * trust the remote host.
 */
public class DefaultTrustManager implements X509TrustManager {
    private static boolean DEBUG = Boolean.getBoolean("httptunnel.debug");
    private Logger logger = Logger.getLogger("Http Tunneling");

    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
        String type) {
        if (DEBUG) {
            log("DefaultTrustManager.checkClientTrusted() " +
                "returning 'true'");
        }

        return;
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
        String type) {
        if (DEBUG) {
            log("DefaultTrustManager.checkServerTrusted() " +
                "returning 'true'");
        }

        return;
    }

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
    }

    private void log(String msg) {
        logger.log(Level.INFO, msg);
    }
}
