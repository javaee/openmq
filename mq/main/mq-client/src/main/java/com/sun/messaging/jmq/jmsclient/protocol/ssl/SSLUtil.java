/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.messaging.jmq.jmsclient.protocol.ssl;

import java.io.*;
import java.util.logging.Logger;
import com.sun.messaging.jmq.util.MQResourceBundle;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import javax.security.cert.X509Certificate;
import javax.jms.*;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;
import com.sun.messaging.jmq.jmsclient.resources.*;
import com.sun.messaging.jmq.jmsclient.protocol.SocketConnectionHandler;


 /**
  */
public class SSLUtil {

    public static SSLSocket makeSSLSocket(String host, int port,
        boolean isHostTrusted, String keystore, String keystorepwd,
        Logger logger, ClientResources cr)
        throws Exception {

        SSLSocketFactory sslFactory;
        if (keystorepwd != null) { 
            SSLContext ctx = getDefaultSSLContext(keystore,
                             keystorepwd, isHostTrusted, logger, cr);
            sslFactory = ctx.getSocketFactory();
        } else {
            if (isHostTrusted) {
                SSLContext ctx = getTrustSSLContext();
                sslFactory = ctx.getSocketFactory();
                if (Debug.debug) {
                    Debug.println("Broker is trusted ...");
                }
            } else {
                sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            }
        }

        //This is here for QA to verify that SSL is used ...
        //XXX chiaming REMOVE
        if (Debug.debug) {
            Debug.println ("Create connection using SSL protocol ...");
            Debug.println ("Broker Host: " + host);
            Debug.println ("Broker Port: " + port);
        }

        SSLSocket sslSocket = null;
        if (host == null) {
            sslSocket = (SSLSocket)sslFactory.createSocket();
        } else {
            sslSocket = (SSLSocket)sslFactory.createSocket(host, port);
        }

        //tcp no delay flag
        boolean tcpNoDelay = true;
        String prop = System.getProperty("imqTcpNoDelay", "true");
        if ( prop.equals("false") ) {
            tcpNoDelay = false;
        } else {
            sslSocket.setTcpNoDelay(tcpNoDelay);
        }

        return sslSocket;
    }

    private static SSLContext getTrustSSLContext()
    throws Exception {

        SSLContext ctx = SSLContext.getInstance("TLS");
        TrustManager[] tm = new TrustManager [1];
        tm[0] = new DefaultTrustManager();
        ctx.init(null, tm, null);
        return ctx;
    }


    private static SSLContext getDefaultSSLContext(
        String keystoreloc, String keystorepwd, boolean isHostTrusted,
        Logger logger, ClientResources cr)
        throws Exception {

        if (keystorepwd == null) {
            if (cr != null) {
                throw new IOException(cr.getKString(cr.X_NO_KEYSTORE_PASSWORD));
            } else {
                throw new IOException("No key store password provided");
            }
        }
        String kpwd = keystorepwd;
        if (kpwd.equals("")) {
            kpwd = System.getProperty("javax.net.ssl.keyStorePassword");
        }
        if (kpwd == null) {
            if (cr != null) {
                throw new IOException(cr.getKString(cr.X_NO_KEYSTORE_PASSWORD));
            } else {
                throw new IOException("No key store password provided");
            }
        }

        String kloc = keystoreloc;
        if (kloc == null) {
            kloc = System.getProperty("javax.net.ssl.keyStore");
        }
        File f = new File(kloc);
        if (!f.exists()) {
            if (cr != null) {
                throw new IOException(cr.getKString(cr.X_FILE_NOT_FOUND, kloc));
            } else {
                throw new IOException("File not found: "+kloc);
            }
        }
        char[] kpwdc = kpwd.toCharArray();
        String ktype = System.getProperty("javax.net.ssl.keyStoreType");
        if (ktype == null) {
            ktype = "JKS";        
        }
        KeyStore kstore = KeyStore.getInstance(ktype);
        kstore.load(new FileInputStream(kloc), kpwdc);

        String kalg = "SunX509";
        KeyManagerFactory kmf = null;
        try {
             kmf = KeyManagerFactory.getInstance(kalg);
        } catch (NoSuchAlgorithmException e) {
            kalg = KeyManagerFactory.getDefaultAlgorithm();
            kmf = KeyManagerFactory.getInstance(kalg);
        }
        kmf.init(kstore, kpwdc);

        String talg = "SunX509";
        TrustManager[] tm = null;
        if (!isHostTrusted) {
            TrustManagerFactory tmf = null;
            try {
                tmf = TrustManagerFactory.getInstance(talg);
            } catch (NoSuchAlgorithmException e) {
                talg = TrustManagerFactory.getDefaultAlgorithm();
                tmf = TrustManagerFactory.getInstance(talg);
            }
            tmf.init(kstore);
            tm = tmf.getTrustManagers();
        } else {
            tm = new TrustManager[1];
            tm[0] = new DefaultTrustManager();
        }
        if (cr != null) {
            String[] args = {ktype, kalg, talg, kloc};
            String logmsg = cr.getKString(cr.I_USE_KEYSTORE, args);
            logger.info(logmsg);
        } else {
            System.out.println("Use "+ktype+" key store, "+kalg+
            " key manager factory and "+talg+" trust manager factory for "+kloc);
        }

        SSLContext ctx = SSLContext.getInstance("TLS");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ctx.init(kmf.getKeyManagers(), tm, random);
        return ctx;
    }

    public static String[] getKnownSSLEnabledProtocols() { 
        try {
            SSLContext sc = getTrustSSLContext();
            SSLEngine se = sc.createSSLEngine();
            return se.getEnabledProtocols();
        } catch (Exception e) {}

        return new String[]{"TLSv1"};
    }
}

