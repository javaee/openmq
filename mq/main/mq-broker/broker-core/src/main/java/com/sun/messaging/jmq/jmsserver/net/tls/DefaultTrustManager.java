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
 * @(#)DefaultTrustManager.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.net.tls;

import java.security.cert.*;
import javax.security.cert.X509Certificate;
import javax.net.ssl.*;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;

/**
 * DefaultTrustManager to manage authentication trust decisions for 
 * different types of authentication material.  Here X509TrustManager is 
 * implemented as the keystore is of type JKS which contains certificates
 * of type X509.
 *
 * X509TrustManager is an interface to manage which X509 certificates which
 * are used to authenticate the remote side of a secure socket. Decisions may
 * be based on trusted certificate authorities, certificate revocation lists,
 * online status checking or other means. 
 * 
 * Currently contains some dummy methods.  This should be sufficient for 
 * the 2.0 product.
 *
 * @see java.security.cert.X509TrustManager
 *
 */

public class DefaultTrustManager implements X509TrustManager {

    private static  boolean DEBUG = false;
    Logger logger = Globals.getLogger();

    public void checkClientTrusted(java.security.cert.X509Certificate[] chain) {
        return;
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
				String type) {
        return;
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain) { 
        if (DEBUG) {
	    logger.log(Logger.DEBUGHIGH,"DefaultTrustManager called to validate certs ..");
	    logger.log(Logger.DEBUGHIGH,"returning 'true' for isServerTrusted call ...");
        }
        return;
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			String type) { 
        if (DEBUG) {
	    logger.log(Logger.DEBUGHIGH,"DefaultTrustManager called to validate certs ..");
	    logger.log(Logger.DEBUGHIGH,"returning 'true' for isServerTrusted call ...");
        }
        return;
    }

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
    }
}
