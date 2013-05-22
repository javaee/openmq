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

package com.sun.messaging.bridge.service.stomp;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.net.URL;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset; 
import javax.jms.Message;
import javax.jms.ConnectionFactory;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.portunif.PUProtocol;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
import org.glassfish.grizzly.ssl.SSLFilter; 
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import com.sun.messaging.portunif.PUService;
import com.sun.messaging.portunif.StompProtocolFinder;
import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.bridge.api.MessageTransformer;
import com.sun.messaging.bridge.api.LogSimpleFormatter;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;

/**
 * @author amyk 
 */
public class StompServer {

    private final static String PROP_HOSTNAME_SUFFIX = ".hostname";
    private final static String PROP_TCPENABLED_SUFFIX = ".tcp.enabled";
    private final static String PROP_SSLENABLED_SUFFIX = ".tls.enabled";
    private final static String PROP_TCPPORT_SUFFIX = ".tcp.port";
    private final static String PROP_SSLPORT_SUFFIX = ".tls.port";
    private final static String PROP_SSL_REQUIRE_CLIENTAUTH_SUFFIX = ".tls.requireClientAuth";
    private final static String PROP_FLOWLIMIT_SUFFIX = ".consumerFlowLimit";
    private final static String PROP_MSGTRANSFORM_SUFFIX = ".messageTransformer";

    private final static String PROP_LOGFILE_LIMIT_SUFFIX = ".logfile.limit";
    private final static String PROP_LOGFILE_COUNT_SUFFIX = ".logfile.count";

    public final static int DEFAULT_TCPPORT = 7672;
    public final static int DEFAULT_SSLPORT = 7673;

    private static StompBridgeResources _sbr = getStompBridgeResources();

    private static Logger _logger = null;

    private  int TCPPORT = DEFAULT_TCPPORT;
    private  int SSLPORT = DEFAULT_SSLPORT;
    private InetAddress HOST = null;
    private String TCPHOSTNAMEPORT = null;
    private String SSLHOSTNAMEPORT = null;

    private static MessageTransformer<Message, Message> _msgTransformer = null; 

    protected static BridgeContext _bc = null;
    protected static Properties jmsprop = null;
    private boolean _tcpEnabled = false;
    private boolean _sslEnabled = false;
    private boolean _inited = false;
    private TCPNIOTransport _tcpTransport = null;
    private TCPNIOTransport _sslTransport = null;
    private PUProtocol _tcppup = null;
    private PUProtocol _sslpup = null;

    public synchronized void init(BridgeContext bc) throws Exception {
        _bc = bc;

        Properties props = bc.getConfig();

        String domain = props.getProperty(BridgeContext.BRIDGE_PROP_PREFIX);

        String cn = props.getProperty(domain+PROP_MSGTRANSFORM_SUFFIX);
        if (cn != null ) {
            _msgTransformer = (MessageTransformer<Message, Message>)
                                     Class.forName(cn).newInstance();
        }

        jmsprop =  new Properties();
        String flowlimit = props.getProperty(domain+PROP_FLOWLIMIT_SUFFIX);
        if (flowlimit != null) {
            jmsprop.setProperty(
                    com.sun.messaging.ConnectionConfiguration.imqConsumerFlowLimit,
                                String.valueOf(Integer.parseInt(flowlimit)));
        }

        _logger = Logger.getLogger(domain);
        if (bc.isSilentMode()) {
            _logger.setUseParentHandlers(false);
        }

        String var = bc.getRootDir();
        File dir =  new File(var);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String logfile = var+File.separator+"stomp%g.log";

        int limit = 0, count = 1;
        String limits = props.getProperty(domain+PROP_LOGFILE_LIMIT_SUFFIX);
        if (limits != null) {
            limit = Integer.parseInt(limits);
        }
        String counts = props.getProperty(domain+PROP_LOGFILE_COUNT_SUFFIX);
        if (counts != null) {
            count = Integer.parseInt(counts);
        }

        FileHandler h = new FileHandler(logfile, limit, count, true);
        h.setFormatter(new LogSimpleFormatter(_logger));  
        _logger.addHandler(h);

        _logger.log(Level.INFO, getStompBridgeResources().getString(
                    StompBridgeResources.I_LOG_DOMAIN, domain));
        _logger.log(Level.INFO, getStompBridgeResources().getString(
                    StompBridgeResources.I_LOG_FILE, logfile)+"["+limit+","+count+"]");

        String v = props.getProperty(domain+PROP_TCPENABLED_SUFFIX, "true");
        if (v != null && Boolean.valueOf(v).booleanValue()) {
            String p = props.getProperty(domain+PROP_TCPPORT_SUFFIX, String.valueOf(DEFAULT_TCPPORT));
            TCPPORT = Integer.parseInt(p);
            _tcpEnabled = true;
        }

        v = props.getProperty(domain+PROP_SSLENABLED_SUFFIX, "false");
        if (v != null && Boolean.valueOf(v).booleanValue()) {
            String p = props.getProperty(domain+PROP_SSLPORT_SUFFIX, String.valueOf(DEFAULT_SSLPORT));
            SSLPORT = Integer.parseInt(p);
            _sslEnabled = true;
        }

        if (!_tcpEnabled && !_sslEnabled) {
            throw new IllegalArgumentException(getStompBridgeResources().
                      getKString(StompBridgeResources.X_NO_PROTOCOL));
        }

        v = props.getProperty(domain+PROP_HOSTNAME_SUFFIX);
        if (v == null || v.length() == 0) {
            v = bc.getBrokerHostName();
        }
        String hn = null;
        if (v != null && v.length() > 0) {
            hn = v;
            HOST = InetAddress.getByName(v);
        } else {
            hn = InetAddress.getLocalHost().getCanonicalHostName();
        }
        URL u = new URL("http", hn, TCPPORT, "");
        TCPHOSTNAMEPORT = u.getHost()+":"+TCPPORT;
        u = new URL("http", hn, SSLPORT, "");
        SSLHOSTNAMEPORT = u.getHost()+":"+SSLPORT;

        int major = Grizzly.getMajorVersion();
        int minor = Grizzly.getMinorVersion();
        if (major < 2) {
            String[] params = { String.valueOf(major), Grizzly.getDotedVersion(), String.valueOf(1)};
            String emsg = getStompBridgeResources().getKString(
                          StompBridgeResources.X_INCOMPATIBLE_GRIZZLY_MAJOR_VERSION, params);
            _logger.log(Level.SEVERE, emsg);
            throw new UnsupportedOperationException(emsg);
        } 
        _logger.log(Level.INFO, getStompBridgeResources().getString(
                    StompBridgeResources.I_INIT_GRIZZLY, Grizzly.getDotedVersion()));

        PUService pu = null;
        if (_bc.doBind() && (_tcpEnabled || _sslEnabled))  {
            pu = (PUService)bc.getPUService();
            if (pu == null) { 
                if (_tcpEnabled) {
                    FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
                    filterChainBuilder.add(new TransportFilter());
                    filterChainBuilder.add(new StompMessageFilter(bc, jmsprop));
                    filterChainBuilder.add(new StompMessageDispatchFilter(bc, jmsprop));

                    _tcpTransport = TCPNIOTransportBuilder.newInstance().build();
                    _tcpTransport.setProcessor(filterChainBuilder.build());
                    InetSocketAddress saddr = (HOST == null ? 
                                               new InetSocketAddress(TCPPORT):
                                               new InetSocketAddress(HOST, TCPPORT));
                    _tcpTransport.bind(saddr);
                }
                if (_sslEnabled) {
                    final SSLEngineConfigurator serverConfig = initializeSSL(_bc, domain, props);
                    final SSLEngineConfigurator clientConfig = serverConfig.copy().setClientMode(true);
                    FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
                    filterChainBuilder.add(new TransportFilter());
                    filterChainBuilder.add(new SSLFilter(serverConfig, clientConfig));
                    filterChainBuilder.add(new StompMessageFilter(bc, jmsprop));
                    filterChainBuilder.add(new StompMessageDispatchFilter(bc, jmsprop));

                    _sslTransport = TCPNIOTransportBuilder.newInstance().build();
                    _sslTransport.setProcessor(filterChainBuilder.build());
                    InetSocketAddress saddr = (HOST == null ? 
                                               new InetSocketAddress(SSLPORT):
                                               new InetSocketAddress(HOST, SSLPORT));
                    _sslTransport.bind(saddr);
                }
            } else {
                if (_tcpEnabled) {
                    final FilterChain puProtocolFilterChain =
                                  pu.getPUFilterChainBuilder()
                                  .add(new StompMessageFilter(bc, jmsprop))
                                  .add(new StompMessageDispatchFilter(bc, jmsprop))
                                  .build();
                     StompProtocolFinder pf = new StompProtocolFinder((
                                  StompFrameMessage.Command.CONNECT).toString().
                                  getBytes(Charset.forName("UTF-8")));
                    _tcppup = new PUProtocol(pf, puProtocolFilterChain);
                }
                if (_sslEnabled) {
                    Properties sslprops = bc.getDefaultSSLContextConfig();
                    boolean reqcauth = false;         
                    v = props.getProperty(domain+PROP_SSL_REQUIRE_CLIENTAUTH_SUFFIX, "false");
                    if (v != null && Boolean.valueOf(v).booleanValue()) {
                        reqcauth = true;
                    }
                    if (!pu.initializeSSL(sslprops, reqcauth, null)) {
                        if (pu.getSSLClientAuthRequired() != reqcauth) {
                            _logger.log(Level.WARNING, getStompBridgeResources().getString(
                                StompBridgeResources.W_PROPERTY_SETTING_OVERRIDE_BY_BROKER,
                                domain+PROP_SSL_REQUIRE_CLIENTAUTH_SUFFIX+"="+reqcauth,
                                domain+PROP_SSL_REQUIRE_CLIENTAUTH_SUFFIX+"="+pu.getSSLClientAuthRequired()));
                        }
                    }
                    final FilterChain puProtocolFilterChain =
                                  pu.getSSLPUFilterChainBuilder()
                                  .add(new StompMessageFilter(bc, jmsprop))
                                  .add(new StompMessageDispatchFilter(bc, jmsprop))
                                  .build();
                    StompProtocolFinder pf = new StompProtocolFinder((
                                  StompFrameMessage.Command.CONNECT).toString().
                                  getBytes(Charset.forName("UTF-8")));
                    _sslpup = new PUProtocol(pf, puProtocolFilterChain);
                }
            }
        }

        if (_bc.doBind() && _tcpEnabled && pu == null) {
            _bc.registerService("stomp[TCP]", "stomp", TCPPORT, null);
        }
        if (_bc.doBind() && _sslEnabled && pu == null) {
            _bc.registerService("stomp[SSL/TLS]", "stomp", SSLPORT, null);
        }
        _inited = true;
     }

     private static SSLEngineConfigurator initializeSSL(
         BridgeContext bc, String domain, Properties props)
         throws Exception {

         _logger.log(Level.INFO, getStompBridgeResources().
                     getString(StompBridgeResources.I_INIT_SSL));

         Properties sslprops = bc.getDefaultSSLContextConfig();
         SSLContextConfigurator sslcf = new SSLContextConfigurator();
         sslcf.setKeyManagerFactoryAlgorithm(sslprops.getProperty(bc.KEYSTORE_ALGORITHM));
         sslcf.setKeyStoreFile(sslprops.getProperty(bc.KEYSTORE_FILE));
         sslcf.setKeyStorePass(sslprops.getProperty(bc.TRUSTSTORE_PASSWORD));
         sslcf.setKeyStoreType(sslprops.getProperty(bc.KEYSTORE_TYPE));

         sslcf.setTrustManagerFactoryAlgorithm(sslprops.getProperty(bc.TRUSTSTORE_ALGORITHM));
         sslcf.setTrustStoreFile(sslprops.getProperty(bc.TRUSTSTORE_FILE));
         sslcf.setTrustStorePass(sslprops.getProperty(bc.TRUSTSTORE_PASSWORD));
         sslcf.setTrustStoreType(sslprops.getProperty(bc.TRUSTSTORE_TYPE));

         sslcf.setSecurityProtocol(sslprops.getProperty(bc.SECURESOCKET_PROTOCOL));

         boolean reqcauth = false;         
         String v = props.getProperty(domain+PROP_SSL_REQUIRE_CLIENTAUTH_SUFFIX, "false");
         if (v != null && Boolean.valueOf(v).booleanValue()) {
             reqcauth = true;
         }

         return new SSLEngineConfigurator(sslcf.createSSLContext(),
                                          false, reqcauth, reqcauth);
     }

     protected static Logger logger() {
        return _logger;
     }

    public synchronized void start() throws Exception {

        if (!_inited || 
            (_bc.doBind() && _bc.getPUService() == null && 
              _tcpTransport == null && _sslTransport == null) ||
            (_bc.doBind() && _bc.getPUService() != null && _tcppup == null && _sslpup == null)) {
            String emsg = getStompBridgeResources().getKString(
                          StompBridgeResources.X_STOMP_SERVER_NO_INIT);
            _logger.log(Level.SEVERE, emsg);
            throw new IllegalStateException(emsg);
        }
        if (!_bc.doBind()) { //to be implemented
            return;
        }
        PUService pu = (PUService)_bc.getPUService();
        if (pu != null) {
            try {
                if (_tcpEnabled) {
                    pu.register(_tcppup, null);
                    _logger.log(Level.INFO, getStompBridgeResources().getString(
                        StompBridgeResources.I_START_TRANSPORT, "TCP" , pu.getBindSocketAddress()));
                }
                if (_sslEnabled) {
                    pu.registerSSL(_sslpup, null);
                    _logger.log(Level.INFO, getStompBridgeResources().getString(
                        StompBridgeResources.I_START_TRANSPORT, "SSL/TLS" , pu.getBindSocketAddress()));
                }

            } catch (Exception e) {
                _logger.log(Level.SEVERE, e.getMessage(), e);
                try {
                stop();
                } catch (Exception ee) {}
                throw e;
            }
        } else {
            try {
                if (_tcpEnabled) {
                    _tcpTransport.start(); 
                    _logger.log(Level.INFO, getStompBridgeResources().getString(
                        StompBridgeResources.I_START_TRANSPORT, "TCP" , TCPHOSTNAMEPORT));
                }
                if (_sslEnabled) {
                    _sslTransport.start(); 
                    _logger.log(Level.INFO, getStompBridgeResources().getString(
                        StompBridgeResources.I_START_TRANSPORT, "SSL/TLS" , SSLHOSTNAMEPORT));
                }
            } catch (Exception e) {
                _logger.log(Level.SEVERE, e.getMessage(), e);
                try {
                stop();
                } catch (Exception ee) {}
                throw e;
            }
        }
    }

    public synchronized void stop() throws Exception {

        if (!_inited || 
            (_bc.doBind() && _bc.getPUService() == null && 
              _tcpTransport == null && _sslTransport == null) ||
            (_bc.doBind() && _bc.getPUService() != null && _tcppup == null && _sslpup == null)) {
            String emsg = getStompBridgeResources().getKString(StompBridgeResources.X_STOMP_SERVER_NO_INIT);
            _logger.log(Level.SEVERE, emsg);
            throw new IllegalStateException(emsg);
        }
        if (!_bc.doBind()) { //to be implemented
            return;
        }

        _logger.log(Level.INFO, getStompBridgeResources().getString(
                                StompBridgeResources.I_STOP_STOMP_SERVER));

        PUService pu =  (PUService)_bc.getPUService();
        if (pu != null) {
            Exception e = null;  
            if (_tcpEnabled) {
                try {
                pu.deregister(_tcppup);
                } catch (Exception ee) {
                e = ee;
                }
            }
            if (_sslEnabled) {
                try {
                pu.deregisterSSL(_sslpup);
                } catch (Exception ee) {
                if (e == null) {
                e = ee;
                }
                }
            }
            if (e != null) {
                _logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        } else {
            Exception e = null;  
            if (_tcpEnabled) {
                try {
                _tcpTransport.stop(); 
                } catch (Exception ee) {
                e = ee;
                }
            }
            if (_sslEnabled) {
                try {
                _sslTransport.stop();
                } catch (Exception ee) {
                if (e == null) {
                e = ee;
                }
                }
            }
            if (e != null) {
               _logger.log(Level.SEVERE, e.getMessage(), e);
               throw e;
            }
        }

        _logger.log(Level.INFO, getStompBridgeResources().getString(
                                StompBridgeResources.I_STOMP_SERVER_STOPPED));
    }

    protected static Logger getLogger() {
        return _logger;
    }

    protected static MessageTransformer<Message, Message> getMessageTransformer() {
        return _msgTransformer;
    }

    public static StompBridgeResources getStompBridgeResources() {
        if (_sbr == null) {
            synchronized(StompServer.class) {
                if (_sbr == null) {
                    _sbr = StompBridgeResources.getResources(Locale.getDefault());
                }
            }
        }
        return _sbr;
    }
}
