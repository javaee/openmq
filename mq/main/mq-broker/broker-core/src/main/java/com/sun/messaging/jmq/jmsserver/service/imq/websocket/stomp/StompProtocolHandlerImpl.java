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

package com.sun.messaging.jmq.jmsserver.service.imq.websocket.stomp;

import java.net.InetAddress;
import java.util.StringTokenizer;
import com.sun.messaging.jmq.Version;
import com.sun.messaging.jmq.ClientConstants;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.util.LoggerWrapper;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.bridge.api.StompFrameMessage;
import com.sun.messaging.bridge.api.StompProtocolHandler;
import com.sun.messaging.bridge.api.StompProtocolException;
import com.sun.messaging.bridge.api.StompFrameMessageFactory;


/**
 * @author amyk 
 */
public class StompProtocolHandlerImpl extends StompProtocolHandler {

    private static final Logger logger = Globals.getLogger();
    private static final BrokerResources br = Globals.getBrokerResources();

    private static final Version mqversion = new Version();

    private STOMPWebSocket wsocket = null;
    private JMSService jmsservice = null;

    public StompProtocolHandlerImpl(STOMPWebSocket wsocket, JMSService jmss) {
        super((LoggerWrapper)logger);
        this.wsocket = wsocket;
        this.jmsservice = jmss;
        stompConnection =  new StompConnectionImpl(this);
    }

    @Override
    public boolean getDEBUG() {
        return wsocket.getDEBUG();
    }

    public JMSService getJMSService() {
        return jmsservice;
    }

    public InetAddress getRemoteAddress() {
        return wsocket.getRemoteAddress();
    }

    public int getRemotePort() {
        return wsocket.getRemotePort();
    }

    @Override
    public String getSupportedVersions() {
        return StompFrameMessage.STOMP_PROTOCOL_VERSION_12; 
    }

    @Override
    public String getServerName() {
        return mqversion.getProductName();
    }

    @Override
    public String negotiateVersion(String acceptVersions) 
    throws StompProtocolException {
        if (acceptVersions == null) {
            throw new StompProtocolException(
                br.getKString(br.X_STOMP_PROTOCOL_VERSION_NO_SUPPORT, 
                StompFrameMessage.STOMP_PROTOCOL_VERSION_10));
        }
        StringTokenizer st = new StringTokenizer(acceptVersions, ",");
        String ver = null;
        while (st.hasMoreElements()) {
            ver = st.nextToken();
            if (Version.compareVersions(ver,
                StompFrameMessage.STOMP_PROTOCOL_VERSION_12) == 0) {
                return StompFrameMessage.STOMP_PROTOCOL_VERSION_12;
            }
        }
        throw new StompProtocolException(br.getKString(
            br.X_STOMP_PROTOCOL_VERSION_NO_SUPPORT, acceptVersions));
    }


    @Override
    public StompFrameMessageFactory getStompFrameMessageFactory() {
        return StompFrameMessageImpl.getFactory();
    }

    @Override
    public String getTemporaryQueuePrefix() {
        return ClientConstants.TEMPORARY_DESTINATION_URI_PREFIX+
            ClientConstants.TEMPORARY_QUEUE_URI_NAME;
    }

    @Override
    public String getTemporaryTopicPrefix() {
     	return ClientConstants.TEMPORARY_DESTINATION_URI_PREFIX+
            ClientConstants.TEMPORARY_TOPIC_URI_NAME;
    }

    protected String getKStringI_CLOSE_STOMP_CONN(String stompconn) {
        return br.getKString(br.I_STOMP_CLOSE_CONN, stompconn);
    }
    protected String getKStringW_CLOSE_STOMP_CONN_FAILED(String stompconn, String emsg) {
        return br.getKString(br.W_STOMP_CLOSE_CONN_FAILED, stompconn, emsg);
    }
    protected String getKStringE_COMMAND_FAILED(String cmd, String emsg, String stompconn) {
        String[] args = { cmd, emsg, stompconn };
        return br.getKString(br.E_STOMP_COMMAND_FAILED, args);
    }
    protected String getKStringE_UNABLE_SEND_ERROR_MSG(String emsg, String eemsg) {
        return br.getKString(br.E_STOMP_UNABLE_SEND_ERROR_MSG, emsg, eemsg);
    }
    protected String getKStringX_SUBID_ALREADY_EXISTS(String subid) {
        return br.getKString(br.X_STOMP_SUBID_ALREADY_EXISTS, subid);
    }
    protected String getKStringX_UNSUBSCRIBE_WITHOUT_HEADER(
        String destHeader, String subidHeader) {
        return br.getKString(br.X_STOMP_UNSUBSCRIBE_WITHOUT_HEADER,
                             destHeader, subidHeader);
    }
    protected String getKStringX_HEADER_NOT_SPECIFIED_FOR(String header, String cmd) {
        return br.getKString(br.X_STOMP_HEADER_NOT_SPECIFIED_FOR, header, cmd);
    }
    protected String getKStringX_SUBSCRIBER_ID_NOT_FOUND(String subid) {
        return br.getKString(br.X_STOMP_SUBSCRIBER_ID_NOT_FOUND, subid);
    }
    protected String getKStringW_NO_SUBID_TXNACK(String subidHeader,
        String tid, String subidPrefix, String msgid) {
        //not supported
        return br.getKString(br.X_STOMP_HEADER_NOT_SPECIFIED_FOR,
               subidHeader, "ACK[tid="+tid+", "+msgid+"]");
    }
    protected String getKStringW_NO_SUBID_NONTXNACK(
        String subidHeader, String subidPrefix, String msgid) {
        //not supported
        return br.getKString(br.X_STOMP_HEADER_NOT_SPECIFIED_FOR, 
                             subidHeader, "ACK["+msgid+"]");
    }
    protected String getKStringX_INVALID_MESSAGE_PROP_NAME(String name) {
        return br.getKString(br.X_STOMP_INVALID_MESSAGE_PROP_NAME, name);
    }
    @Override
    protected String getKStringX_INVALID_HEADER_VALUE(String header, String value) {
        return br.getKString(br.X_STOMP_INVALID_HEADER_VALUE, value, header);
    }
    @Override
    protected String getKStringI_USE_HEADER_IGNORE_OBSOLETE_HEADER_FOR(
        String useHeader, String ignoreHeaders, String cmd) {
        String[] args = { useHeader, ignoreHeaders, cmd };
        return br.getKString(br.I_STOMP_USE_HEADER_IGNORE_OBSOLETE_HEADER_FOR, args);
    }

}
