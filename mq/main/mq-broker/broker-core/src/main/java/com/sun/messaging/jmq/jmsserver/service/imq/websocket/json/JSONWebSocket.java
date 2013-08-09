/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver.service.imq.websocket.json;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import java.io.StringReader;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonObjectBuilder;
import javax.json.JsonBuilderFactory;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.JMSPacket;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.util.BASE64Encoder;
import com.sun.messaging.jmq.jmsservice.JMSAck;
import com.sun.messaging.jmq.jmsservice.Consumer;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.Destination;
import com.sun.messaging.jmq.jmsservice.JMSServiceReply;
import com.sun.messaging.jmq.jmsservice.JMSService.MessageAckType;
import com.sun.messaging.jmq.jmsservice.JMSService.SessionAckMode;
import com.sun.messaging.jmq.jmsservice.JMSServiceException;
import com.sun.messaging.jmq.jmsservice.ConsumerClosedNoDeliveryException;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.service.imq.JMSServiceImpl;
import com.sun.messaging.jmq.jmsserver.service.imq.websocket.MQWebSocket;
import com.sun.messaging.jmq.jmsserver.service.imq.websocket.MQWebSocketServiceApp;


/**
 * prelimary prototype work  
 *
 * @author amyk
 */
public class JSONWebSocket extends MQWebSocket {

    private JMSService jmsservice = null;
    private long connectionId = 0L;

    private Map<String, Subscriber> subscribers = 
        Collections.synchronizedMap(new HashMap<String, Subscriber>());

    private class Subscriber implements Consumer {
        String id = null;
        long connid = 0L;
        long sessionid = 0L;
        long consumerid = 0L;
        String destination = null;
        SessionAckMode ackmode = SessionAckMode.AUTO_ACKNOWLEDGE;

        public String toString() {
            return "Consumer[id="+id+", consumerId="+consumerid+
            ", sessionId="+sessionid+", connectionId="+connid+"]";
        }

        public JMSAck deliver(JMSPacket msgpkt) 
            throws ConsumerClosedNoDeliveryException {
            if (closed || !isConnected()) {
                throw new ConsumerClosedNoDeliveryException(
                    "JSONWebSocket@"+JSONWebSocket.this.hashCode()+" closed");
            }
            if (connectionId != connid) {
                throw new ConsumerClosedNoDeliveryException(
                    "JSONWebSocket@"+JSONWebSocket.this.hashCode()+
                    " JSON CONNECTION "+connid+ " closed");
            }
            try {
                JsonObject message = buildMessage(msgpkt.getPacket(), this);
                if (DEBUG) {
                    logger.log(logger.INFO,  
                    "JSONWebSocket@"+JSONWebSocket.this.hashCode()+
                    " SEND message "+message+" for "+toString());
                }
                sendJsonObject(message);
                if (ackmode != SessionAckMode.CLIENT_ACKNOWLEDGE) { 
                    return new Ack(this, msgpkt.getPacket(),
                                   MessageAckType.ACKNOWLEDGE); 
                }
            } catch (Exception e) {
                logger.logStack(logger.WARNING, e.getMessage(), e);
            }
            return null;
        }
    }

    private static class Ack implements JMSAck {

        Subscriber sub = null; 
        Packet msg = null;
        MessageAckType acktype = MessageAckType.ACKNOWLEDGE;

        public Ack(Subscriber sub, Packet msg, MessageAckType acktype) {
            this.sub = sub;
            this.msg = msg;
            this.acktype = acktype;

        }
        public long getConnectionId() {
            return sub.connid;
        }

        public long getSessionId() {
            return sub.sessionid;
        }

        public long getConsumerId() {
            return sub.consumerid;
        }

        public SysMessageID getSysMessageID() {
            return  msg.getSysMessageID();
        }

        public long getTransactionId() {
            return 0L;
        }

        public MessageAckType getMessageAckType() {
            return acktype;
        }
    }

    public JSONWebSocket(MQWebSocketServiceApp app, 
                       ProtocolHandler protocolHandler,
                       HttpRequestPacket request,
                       WebSocketListener... listeners) {
        super(app, protocolHandler, request, listeners);
        jmsservice = new JMSServiceImpl(app.getMQService(), 
                         Globals.getProtocol(),  true /*XXimpl*/);
    }

    @Override
    public void onClose(final DataFrame frame) {
        try {

        synchronized(closeLock) {
            if (connectionId != 0L) {
                try {
                    jmsservice.destroyConnection(connectionId);
                } catch (Exception e) {
                    logger.logStack(logger.WARNING, e.getMessage(), e);
                } finally {
                    connectionId = 0L;
                }
            }
        }

        }finally {
	super.onClose(frame);
        }
    }

    @Override
    protected void writePacket(Packet pkt) throws IOException {
        if (!isConnected()) {
            throw new IOException("JSONWebSocket@"+hashCode()+" is not connected"); 
        }
        if (DEBUG) {
            logger.log(logger.INFO, 
            Thread.currentThread()+"JSONWebSocket@"+hashCode()+": WRITE PACKET="+pkt);
        }

        if (DEBUG) {
            logger.log(logger.INFO, 
            Thread.currentThread()+"JSONWebSocket@"+hashCode()+": SENT PACKET="+pkt);
        }
    }

    @Override
    protected void processData(String text) throws Exception {
        if (DEBUG) {
            logger.log(logger.INFO, 
            Thread.currentThread()+"JSONWebSocket@"+hashCode()+": process data="+text);
        }

        JsonObject joreply = null;
        try {

        JsonReader jsonReader = Json.createReader(new StringReader(text)); 
        JsonObject jo = jsonReader.readObject();
        String type = jo.getString("type"); 
        JsonObject headers = jo.getJsonObject("headers"); 
        JsonObject body = jo.getJsonObject("body"); 

        if ("CONNECT".equals(type)) {
            if (headers == null) {
                throw new IOException("No header not provided");
            }
            String username = headers.getString("login");
            String password = headers.getString("passcode");
            if (username == null || password == null) {
                throw new IOException("username or password not provided");
            }
            JMSServiceReply reply = jmsservice.createConnection(username, password, null);
            connectionId = reply.getJMQConnectionID();
            logger.log(logger.INFO, "CREATED connection: "+connectionId);
            JsonBuilderFactory jsonfactory = Json.createBuilderFactory(null);
            joreply = jsonfactory.createObjectBuilder()
                      .add("type", "CONNECTED")
                      .add("headers", jsonfactory.createObjectBuilder()
                          .add("version", "1.2")
                          .add("session", String.valueOf(connectionId))
                          .add("server", Globals.getVersion().getProductName())).build();
        } else if ("SEND".equals(type)) {
            if (headers == null) {
                throw new IOException("No header provided");
            }
            String jsdest = headers.getString("destination");
            if (jsdest == null) {
                throw new IOException("destination header not provided");
            }
            Packet pkt = new Packet();
            destinationToPacket(jsdest, pkt);
            if (headers.getBoolean("persistent", true)) {
                pkt.setPersistent(true);
            }
            if (body != null) {
                JsonString bodytype = body.getJsonString("type");
                if (bodytype == null || 
                    (bodytype != null && bodytype.getString().equals("text"))) {
                    pkt.setPacketType(PacketType.TEXT_MESSAGE);
                    if (body.getJsonString("encoder") == null) {
                        JsonString msg = body.getJsonString("text");
                        if (msg != null) {
                            logger.log(logger.INFO, "GOT message body:"+msg.getString());
                            pkt.setMessageBody(msg.getString().getBytes("UTF-8"));
                        }
                    } else {
                        throw new IOException("encoder not supported");
                    }
                } else {
                    throw new IOException("body type:"+bodytype+" not supported");
                }
            }
            Destination dest = destinationToDestination(jsdest);
            long connid = connectionId;
            JMSServiceReply reply = null;
            try {
                reply = jmsservice.createDestination(connid, dest);
            } catch (JMSServiceException jmsse) {
                JMSServiceReply.Status status = jmsse.getJMSServiceReply().getStatus();
                if (status == JMSServiceReply.Status.CONFLICT) {
                   logger.log(logger.DEBUG, "destination "+jsdest+" already exist");
                } else {
                   throw jmsse;
                }
            }
            //XXopt
            reply = jmsservice.createSession(connectionId, 
                                        SessionAckMode.AUTO_ACKNOWLEDGE);
            long sessionid = reply.getJMQSessionID();
            reply = jmsservice.addProducer(connid, sessionid, dest);
            long producerid = reply.getJMQProducerID();
            pkt.setProducerID(producerid);
            pkt.setIP(getRemoteAddress().getAddress());
            pkt.setPort(getRemotePort());

            pkt.prepareToSend();
            final Packet p = pkt;
            reply = jmsservice.sendMessage(
                                connectionId, new JMSPacket() {
                                public Packet getPacket() {
                                    return p;
                                }
                            });
            try {
                jmsservice.deleteProducer(connid, sessionid, producerid);
            } catch (Exception e) {
                logger.logStack(logger.WARNING, e.getMessage(), e);
            }
            joreply = buildReceipt(headers);

        } else if ("SUBSCRIBE".equals(type)) {
            if (headers == null) {
                throw new IOException("No header not provided");
            }
            String jsdest = headers.getString("destination");
            if (jsdest == null) {
                throw new IOException("destination header not provided");
            }
            String subid = headers.getString("id");
            if (subid == null) {
                throw new IOException("id header not provided");
            }
            if (subscribers.get(subid) != null) {
                throw new IOException("An existing subscriber with id "+subid+" already exist");
            } 
            JsonString acktype = headers.getJsonString("ack");
            SessionAckMode ackmode = SessionAckMode.AUTO_ACKNOWLEDGE;
            if (acktype != null && acktype.getString().equals("client")) {
                ackmode = SessionAckMode.CLIENT_ACKNOWLEDGE;
            }

            long connid = connectionId;

            JMSServiceReply reply = jmsservice.createSession(connid, ackmode);
            long sessionId = reply.getJMQSessionID(); 
            Destination dest = destinationToDestination(jsdest);
            try {
                reply = jmsservice.createDestination(connid, dest);
            } catch (JMSServiceException jmsse) {
                JMSServiceReply.Status status = jmsse.getJMSServiceReply().getStatus();
                if (status == JMSServiceReply.Status.CONFLICT) {
                   logger.log(logger.DEBUG, "Destination "+jsdest+" already exist");
                } else {
                   throw jmsse;
                }
            }
            reply = jmsservice.startConnection(connid);
            reply = jmsservice.addConsumer(connid, sessionId, 
                        dest, null, null, false, false, false, null, false);
            long consumerId = reply.getJMQConsumerID();
            Subscriber sub = new Subscriber();
            sub.id = subid;
            sub.connid = connid;
            sub.sessionid = sessionId;
            sub.consumerid = consumerId;
            sub.destination = jsdest;
            sub.ackmode = ackmode;
            subscribers.put(subid, sub);
            reply = jmsservice.setConsumerAsync(connid, sessionId, consumerId, sub);
            joreply = buildReceipt(headers);

        } else if ("UNSUBSCRIBE".equals(type)) {
            if (headers == null) {
                throw new IOException("No header not provided");
            }
            String subid = headers.getString("id");
            if (subid == null) {
                throw new IOException("id header not provided");
            }
            Subscriber sub = subscribers.get(subid);
            if (sub == null) {
                throw new IOException("A subscriber with id "+subid+" not exist");
            }
            JMSServiceReply reply = null;
            try {
                reply = jmsservice.deleteConsumer(sub.connid, 
                            sub.sessionid, sub.consumerid, null, false, null, null);
                subscribers.remove(subid);
            } finally {
                reply = jmsservice.destroySession(sub.connid, sub.sessionid);
                subscribers.remove(subid);
            }
            joreply = buildReceipt(headers);

        } else if ("DISCONNECT".equals(type)) {
            synchronized(closeLock) {
                jmsservice.destroyConnection(connectionId);
                connectionId = 0L;
            }
            joreply = buildReceipt(headers);
        } else {
            throw new IOException("Unsupported: "+type);
        }

        } catch (Exception e) {
            logger.logStack(logger.ERROR, e.getMessage(), e);
            joreply = buildErrorReply(e);
        }

        sendJsonObject(joreply);
    }

    private void sendJsonObject(JsonObject jo) throws Exception {
        if (jo == null) {
            return;
        }
        /*
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonWriter writer = Json.createWriter(os); 
        writer.writeObject(jo);
        os.flush();
        byte[] bytes = os.toByteArray();
            logger.log(logger.INFO, "write json object["+jo+"]len="+bytes.length);
        String data= new String(bytes, "UTF-8");
        */
        synchronized(closeLock) {
            if (isClosed()) {
                logger.log(logger.INFO, Thread.currentThread()+"JSONWebSocketSocket@"+this.hashCode()+" is closed. not sending ["+jo+"]");
                return;
            }
            send(jo.toString());
        }
        if (DEBUG) {
            logger.log(logger.INFO, Thread.currentThread()+"JSONWebSocketSocket@"+this.hashCode()+" SENT json object["+jo+"]");
        }
        /*
        try {
            writer.close();
            os.close();
        } catch (Exception e) {
            logger.logStack(logger.WARNING, e.getMessage(), e);
        }
        */
    }

    private JsonObject buildReceipt(JsonObject headers) {
        if (headers == null) {
            return null;
        }
        JsonString receipt =  headers.getJsonString("receipt-id");
        if (receipt == null) {
            return null;
        }
        JsonBuilderFactory jsonfactory = Json.createBuilderFactory(null);
        return jsonfactory.createObjectBuilder()
                   .add("type", "RECEIPT")
                   .add("headers", jsonfactory.createObjectBuilder()
                       .add("receipt-id", receipt.getString())).build();
    }

    private JsonObject buildErrorReply(Exception e) {
        String estack = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os, true, "UTF-8");
            ps.flush();
            os.flush();
            e.printStackTrace(ps);
            estack = os.toString("UTF-8");
        } catch (Exception ee) {
            if (DEBUG) {
                logger.logStack(logger.INFO, ee.toString(), ee);
            }
        }
        JsonBuilderFactory jsonfactory = Json.createBuilderFactory(null);
        JsonObjectBuilder jb = jsonfactory.createObjectBuilder()
                            .add("type", "ERROR")
                            .add("headers", jsonfactory.createObjectBuilder()
                                .add("message", e.toString()));
        if (estack != null) {
            jb.add("body", jsonfactory.createObjectBuilder()
                  .add("text", estack));
        }
        return jb.build();
    }

    private JsonObject buildMessage(Packet pkt, Subscriber sub) throws Exception {
        JsonBuilderFactory jsonfactory = Json.createBuilderFactory(null);
        JsonObjectBuilder jb = jsonfactory.createObjectBuilder()
                   .add("type", "MESSAGE")
                   .add("headers", jsonfactory.createObjectBuilder()
                       .add("subscription", sub.id)
                       .add("message-id", pkt.getSysMessageID().toString()) 
                       .add("destination", sub.destination)
                       .add("content-type", "text/plain"));
        byte[] body = pkt.getMessageBodyByteArray();
        int ptype = pkt.getPacketType();
        if (ptype == PacketType.TEXT_MESSAGE) {
            jb.add("body", jsonfactory.createObjectBuilder()
                  .add("type", "text")
                  .add("text", new String(body, "UTF-8")));
        } else if (ptype == PacketType.BYTES_MESSAGE) {
            BASE64Encoder encoder = new BASE64Encoder();
            String data = encoder.encode(body);
            jb.add("body", jsonfactory.createObjectBuilder()
                  .add("type", "bytes")
                  .add("encoder", "base64")
                  .add("text", data));
        }
        return jb.build();
    }

    private void destinationToPacket(String jsdest, Packet pkt) {
       	if (jsdest.startsWith("/queue/")) {
            String dest = jsdest.substring("/queue/".length(), jsdest.length()).trim();
            pkt.setDestination(dest);
            pkt.setIsQueue(true);
            pkt.setDestinationClass("com.sun.messaging.Queue");
	}  
    }

    private Destination destinationToDestination(String jsdest)
        throws Exception {

       	if (jsdest.startsWith("/queue/")) {
            String dest = jsdest.substring("/queue/".length(), jsdest.length()).trim();
            return new Destination(dest, 
               com.sun.messaging.jmq.jmsservice.Destination.Type.QUEUE,
               com.sun.messaging.jmq.jmsservice.Destination.Life.STANDARD); 
	}  
        throw new IOException("Invalid destination header "+jsdest); 
    }


    @Override
    protected void processData(byte[] data) throws Exception {
        throw new IOException("JSONWebSocket.processData(byte[]): unexpected call");
    }
}
