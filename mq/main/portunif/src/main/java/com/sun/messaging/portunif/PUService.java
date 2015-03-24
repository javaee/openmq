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

package com.sun.messaging.portunif;

import java.util.Set;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.io.IOException;
import java.util.Properties;
import java.net.SocketAddress;
import org.glassfish.grizzly.portunif.PUFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection; 
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;
import org.glassfish.grizzly.portunif.finders.SSLProtocolFinder;
import org.glassfish.grizzly.portunif.PUProtocol;
import org.glassfish.grizzly.filterchain.FilterChain; 

public class PUService {

    private PUFilter rootpuf = null;
    private PUFilter sslpuf = null;
    private TCPNIOTransport puTransport = null; 
    private SocketAddress bindAddr = null;
    private boolean sslClientAuthRequired = false;
    private PUProtocol endPUProtocol = null;
    private PUProtocol endPUProtocolSSL = null;
    private TCPNIOServerConnection serverConn = null;

    public PUService() {
        rootpuf = new PUFilter();
        final FilterChainBuilder puFilterChainBuilder =
                                   FilterChainBuilder.stateless()
                                   .add(new TransportFilter())
                                   .add(rootpuf);
        puTransport = TCPNIOTransportBuilder.newInstance().build();
        puTransport.setProcessor(puFilterChainBuilder.build());

    }

    public synchronized void bind(SocketAddress saddr, int backlog)
    throws IOException {

        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (bindAddr == null) {
            serverConn = puTransport.bind(saddr, backlog);
            bindAddr = saddr;
        } else if (!bindAddr.equals(saddr)) {
            puTransport.stop();
            serverConn = puTransport.bind(saddr, backlog);
            bindAddr = saddr;
        }
    }

    public synchronized void rebind(SocketAddress saddr, int backlog)
    throws IOException {
        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (!saddr.equals(bindAddr)) {
            puTransport.stop();
            serverConn = puTransport.bind(saddr, backlog);
            bindAddr = saddr;
        }
    }

    public synchronized SocketAddress start() throws IOException {
        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        puTransport.start();
        return bindAddr;
    }

    public synchronized SocketAddress getBindSocketAddress() 
    throws IOException {
        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (bindAddr == null) {
            throw new IOException("PUService not bound yet");
        }
        return bindAddr;
    }

    public synchronized void setBacklog(int backlog) throws IOException {
        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        puTransport.setServerConnectionBackLog(backlog);
    }

    private synchronized void preRegister(PUServiceCallback cb)
    throws IOException {
        if (endPUProtocol == null) {
            endPUProtocol = new PUProtocol(new EndProtocolFinder(cb),
                            rootpuf.getPUFilterChainBuilder().build());
        }
        rootpuf.deregister(endPUProtocol);
    }

    private synchronized void postRegister()
    throws IOException {
        rootpuf.register(endPUProtocol);
    }

    private synchronized void preRegisterSSL(PUServiceCallback cb)
    throws IOException {
        if (endPUProtocolSSL == null) {
            endPUProtocolSSL = new PUProtocol(new EndProtocolFinder(cb),
                               sslpuf.getPUFilterChainBuilder().build());
        }
        sslpuf.deregister(endPUProtocolSSL);
    }

    private synchronized void postRegisterSSL()
    throws IOException {
        sslpuf.register(endPUProtocolSSL);
    }

    public synchronized void register(PUProtocol pp, PUServiceCallback cb)
    throws IOException {
        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        preRegister(cb);
        try {
            rootpuf.register(pp);
        } finally {
            postRegister();
        }
    }

    public synchronized void deregister(PUProtocol pp) throws IOException {
        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        rootpuf.deregister(pp);
    }

    public synchronized void registerSSL(PUProtocol pp, PUServiceCallback cb)
    throws IOException {
        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (sslpuf == null) {
            throw new IOException("Illegal call: PUService SSL not initialized");
        }
        preRegisterSSL(cb);
        try {
            sslpuf.register(pp);
        } finally { 
            postRegisterSSL();
        }
    }

    public synchronized void deregisterSSL(PUProtocol pp) throws IOException {
       if (rootpuf == null) {
           throw new IOException("Illegal call: PUService not initialized");
       }
       if (sslpuf == null) {
           throw new IOException("Illegal call: PUService SSL not initialized");
       }
       sslpuf.deregister(pp);
    }

    public synchronized void stop() throws IOException {
        if (puTransport == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        puTransport.stop();
    }

    public synchronized void destroy() throws IOException {
        if (puTransport != null) {
            puTransport.stop();
            puTransport = null;
            rootpuf = null;
        }
    }

    public synchronized FilterChainBuilder getPUFilterChainBuilder()
    throws IOException {
        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        return rootpuf.getPUFilterChainBuilder();
    }

    public synchronized FilterChainBuilder getSSLPUFilterChainBuilder()
    throws IOException {
        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (sslpuf == null) {
            throw new IOException("Illegal call: PUService SSL not initialized");
        }
        return sslpuf.getPUFilterChainBuilder();
    }

    /**
     */
    public synchronized boolean getSSLClientAuthRequired() {
        return sslClientAuthRequired;
    }

    /**
     */
    public synchronized boolean initializeSSL(
        Properties props, boolean clientAuthRequired, 
        PUServiceCallback cb, boolean poodleFixEnabled, 
        String[] knownSSLEnabledProtocols)
        throws IOException { 

        if (rootpuf == null) {
            throw new IOException("Illegal call: PUService not initialized");
        }
        if (sslpuf != null) { 
            return false;
        }

        SSLContextConfigurator sslcf = createSSLContextConfigrattor(props);
        if (!sslcf.validateConfiguration(true)) {
            throw new IOException("Invalid SSL context configuration:"+sslcf);
        }
        SSLEngineConfigurator clientc = new SSLEngineConfigurator(sslcf.createSSLContext());
        SSLEngineConfigurator serverc = new SSLEngineConfigurator(sslcf.createSSLContext(),
                                            false, clientAuthRequired, clientAuthRequired);
        if (poodleFixEnabled) {
            applyPoodleFix(clientc, knownSSLEnabledProtocols, "PUService");
            applyPoodleFix(serverc, knownSSLEnabledProtocols, "PUService");
        }

        sslpuf = new PUFilter();
        FilterChain sslProtocolFilterChain = rootpuf.getPUFilterChainBuilder()
                               .add(new SSLFilter(serverc, clientc))
                               .add(sslpuf).build();
        PUProtocol pu = new PUProtocol(new SSLProtocolFinder(serverc),
                                       sslProtocolFilterChain);
        try {
            register(pu, cb);
            this.sslClientAuthRequired = clientAuthRequired;
            return true;
        } catch (Exception e) { 
            sslpuf = null;
            this.sslClientAuthRequired = false;
            if (e instanceof IOException) {
                throw (IOException)e;
            }
            throw new IOException(e.toString(), e);
        }
    }

    public static SSLContextConfigurator 
    createSSLContextConfigrattor(Properties props) {

        SSLContextConfigurator sslcf = new SSLContextConfigurator();
        sslcf.setKeyManagerFactoryAlgorithm(props.getProperty(KEYSTORE_ALGORITHM));
        sslcf.setKeyStoreType(props.getProperty(KEYSTORE_TYPE));
        sslcf.setKeyStoreFile(props.getProperty(KEYSTORE_FILE));
        sslcf.setKeyStorePass(props.getProperty(TRUSTSTORE_PASSWORD));

        sslcf.setTrustManagerFactoryAlgorithm(props.getProperty(TRUSTSTORE_ALGORITHM));
        sslcf.setTrustStoreType(props.getProperty(TRUSTSTORE_TYPE));
        sslcf.setTrustStoreFile(props.getProperty(TRUSTSTORE_FILE));
        sslcf.setTrustStorePass(props.getProperty(TRUSTSTORE_PASSWORD));

        sslcf.setSecurityProtocol(props.getProperty(SECURESOCKET_PROTOCOL));
        return sslcf;
    }

    public static void applyPoodleFix(SSLEngineConfigurator ec, 
        String[] knownSSLEnabledProtocols, String caller) {

        String[] protocols = ec.getEnabledProtocols();
        if (protocols == null) {
            protocols = knownSSLEnabledProtocols;
        }
        String orig = Arrays.toString(protocols);

        Set<String> set = new LinkedHashSet<String>();
        for (String s : protocols) {
            if (s.equals("SSLv3")) {
                continue;
            }
            set.add(s);
        }
        protocols = set.toArray(new String[set.size()]);
        System.out.println("["+caller+"]: ["+orig+
            "], setEnabledProtocols:["+Arrays.toString(protocols)+"]");
        ec.setEnabledProtocols(protocols);
        return;
    }

    public static final String KEYSTORE_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    public static final String KEYSTORE_TYPE = "javax.net.ssl.keyStoreType";
    public static final String KEYSTORE_FILE = "javax.net.ssl.keyStore";
    public static final String KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";


    public static final String TRUSTSTORE_ALGORITHM = "ssl.TrustManagerFactory.algorithm";
    public static final String TRUSTSTORE_TYPE = "javax.net.ssl.trustStoreType";
    public static final String TRUSTSTORE_FILE = "javax.net.ssl.trustStore";
    public static final String TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    public static final String SECURESOCKET_PROTOCOL = "securesocket.protocol";
}

