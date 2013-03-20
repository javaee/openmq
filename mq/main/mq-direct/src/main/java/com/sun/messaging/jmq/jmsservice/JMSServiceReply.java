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

/*
 * @(#)JMSServiceReply.java	1.7 06/29/07
 */ 

package com.sun.messaging.jmq.jmsservice;

import java.util.Hashtable;
import java.util.Map;

/**
 *  The JMSServiceReply class encapsulates the JMS server's reply to requests
 *  made to the server using the JMSService interface methods.<p>
 *
 *  Almost every interface method of JMSService returns an instance of
 *  JMSServiceReply. The Status of the request can be obtained using the
 *  {@code getStatus()} method.<p>
 *
 *  All relevant returned properties can be obtained using methods that follow
 *  the pattern {@code get<PropertyName>}, where {@code <PropertyName>} is
 *  replaced with the name of the property in the SJSMQ wire protocol.<br>
 *  When the {@code <PropertyName>} is a required property, its absence will
 *  cause a {@code NoSuchFieldException} to be thrown.<br>
 *  When the {@code <PropertyName>} is an optional property, its absence will
 *  cause a {@code NoSuchFieldException} <b>unless</b> it makes sense to
 *  return a default value.
 */
public class JMSServiceReply {

    private JMSPacketProperties _replyProps = null;
    private JMSPacketBody _replyBody = null;
    private Status _status;
    
    /**
     * Creates a new instance of JMSServiceReply using the specified Hashtable
     *
     * @param replyProps The Hashtable containing the JMSServiceReply properties
     */
    public JMSServiceReply(
            Map <? extends String, ? extends Object> replyProps,
            JMSPacketBody replyBody) {
        _replyProps = ((replyProps != null)
            ? new JMSPacketProperties(replyProps)
            : new JMSPacketProperties());
        _replyBody = replyBody;
        setStatus();
    }

    /////////////////////////////////////////////////////////////////////////
    // public / generic property access methods
    /////////////////////////////////////////////////////////////////////////
    /**
     * returns the status associated with a JMSService request
     * 
     * @return The Status of the JMSService request
     */
    public Status getStatus(){
        return _status;
    }

    public String getErrorCode(){
        if (_replyProps == null) {
            return null;
        }
        Object o = _replyProps.get(JMSPacketProperties.JMQErrorCode); 
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String)o;
        }
        return o.toString();
    }

    /**
     *  Returns the properties associated with the JMSServiceReply
     *
     *  @return The JMSPacketProperties associated with this JMSServiceReply
     */
    public JMSPacketProperties getProperties(){
        return _replyProps;
    }

    /**
     * gets the boolean value of a property from the replyProps
     *
     * @param prop The property whose value is to be returned as a boolean
     */
    public boolean getBooleanProp(String prop) throws NoSuchFieldException {
        String errMsg = "JMSServiceReply has no return property values";
        if (_replyProps != null){
            try {
                return ((Boolean)_replyProps.get(prop)).booleanValue();
            } catch (Exception e){
            }
            errMsg = "JMSServiceReply is missing boolean property -" + prop;
        }
        throw new NoSuchFieldException(errMsg);
    }

    /**
     * gets the int value of a property from the replyProps
     *
     * @param prop The property whose value is to be returned as an int
     */
    public int getIntProp(String prop) throws NoSuchFieldException {
        String errMsg = "JMSServiceReply has no return property values";
        if (_replyProps != null){
            try {
                return ((Integer)_replyProps.get(prop)).intValue();
            } catch (Exception e){
            }
            errMsg = "JMSServiceReply is missing int property -" + prop;
        }
        throw new NoSuchFieldException(errMsg);
    }

    /**
     *  gets the long value of a property from the replyProps
     *
     *  @param prop The property whose value is to be returned as a long
     */
    public long getLongProp(String prop) throws NoSuchFieldException {
        String errMsg = "JMSServiceReply has no return property values";
        if (_replyProps != null){
            try {
                Long _tmp = (Long)_replyProps.get(prop);
                return _tmp.longValue();
            } catch (Exception e){
            }
            errMsg = "JMSServiceReply is missing long property -" + prop;
        }
        throw new NoSuchFieldException(errMsg);
    }

    /**
     *  gets the string value of a property from the replyProps
     *
     *  @param prop The property whose value is to be returned as a string
     */
    public String getStringProp(String prop) throws NoSuchFieldException {
        String errMsg = "JMSServiceReply has no return property values";
        if (_replyProps != null){
            try {
                return (String)_replyProps.get(prop);
            } catch (Exception e){
            }
            errMsg = "JMSServiceReply is missing string property -" + prop;
        }
        throw new NoSuchFieldException(errMsg);
    }

    /**
     *  returns the product version of the JMSService from the createConnection
     *  JMSServiceReply. Note that JMQVerison <b>must</b> be returned for a
     *  createConnection method.
     *
     *  @return The product version String
     */
    public String getJMQVersion() throws NoSuchFieldException {
        return getStringProp("JMQVersion");            
    }

    /**
     *  returns the ID of the Connection from the createConnection
     *  JMSServiceReply. Note that JMQConnectionID <b>must</b> be returned for a
     *  createConnection method.
     *
     *  @return The connectionID
     */
    public long getJMQConnectionID() throws NoSuchFieldException {
        return getLongProp("JMQConnectionID");            
    }

    /**
     *  returns whether the Connection is HA from the createConnection 
     *  JMSServiceReply. Note that JMQHA is an optional return property for a
     *  creatConnection method.
     *
     *  @return {@code true} if HA is enabled, {@code false} otherwise
     */
    public boolean getJMQHA() {
        boolean _JMQHA = false;
        try {
            _JMQHA = getBooleanProp("JMQHA");
        } catch (NoSuchFieldException nsfe){
        }
        return _JMQHA;
    }

    /**
     *  returns the ID of the cluster from the createConnection JMSServiceReply.
     *  Note that JMQClusterID is an optional return property for a
     *  createConnection method.
     *
     *  @return The cluster ID
     */
    public String getJMQClusterID() {
        String _JMQClusterID = null;
        try {
            _JMQClusterID = getStringProp("JMQClusterID");
        } catch (NoSuchFieldException nsfe) {
        }
        return _JMQClusterID;
    }

    /**
     *  returns the maximum message size that the JMSService can handle.
     *
     *  @return The maximum message size in bytes that the JMSService can handle
     */
    public long getJMQMaxMsgBytes() throws NoSuchFieldException {
       return getLongProp("JMQMaxMsgBytes");
    }

    /**
     *  returns the brokerlist of the cluster from the createConnection
     *  JMSServiceReply.
     *  Note that JMQBrokerList is an optional return property for a
     *  createConnection method.
     *
     *  @return The BrokerList
     */
    public String getJMQBrokerList() {
        String _JMQBrokerList = null;
        try {
            _JMQBrokerList = getStringProp("JMQBrokerList");
        } catch (NoSuchFieldException nsfe) {
        }
        return _JMQBrokerList;
    }

    /**
     *  returns the ID of the Session from the createSession
     *  JMSServiceReply. Note that JMQSessionID <b>must</b> be returned for a
     *  createSession method.
     *
     *  @return The sessionID
     */
    public long getJMQSessionID() throws NoSuchFieldException {
        return getLongProp("JMQSessionID");            
    }

    /**
     *  returns the name of the destination if present. Note that this is an
     *  optional property
     *
     *  @return The destination name String
     */
    public String getJMQDestination() {
        String _JMQDestination = null;
        try {
            _JMQDestination = getStringProp("JMQDestination");
        } catch (NoSuchFieldException nsfe) {
        }
        return _JMQDestination;
    }

    /**
     *  returns the type of the Destination that was created or verified.
     *  
     *
     *  @return The type of the Destination
     */
    public Destination.Type getJMQDestType() throws NoSuchFieldException {
        int _DestinationType = getIntProp("JMQDestType");
        if (_DestinationType == 1){
            return Destination.Type.QUEUE;
        } else {
            return Destination.Type.TOPIC;
        }
    }

    /**
     *  returns whether the Destination can be created with the 
     *  {@code createDestination()} call when the JMSServiceReply Status
     *  returned from a {@code verifyDestination()}method is NOT_FOUND.
     *  Note that JMQHA is an optional return property for a
     *  {@code verifyDestination()} method.
     *
     *  @return {@code true} if the Destination can be auto-created;
     *  {@code false} otherwise
     */
    public boolean getJMQCanCreate() {
        boolean _JMQCanCreate = false;
        try {
            _JMQCanCreate = getBooleanProp("JMQCanCreate");
        } catch (NoSuchFieldException nsfe){
        }
        return _JMQCanCreate;
    }

    /**
     *  returns the ID of the Producer from the addProducer
     *  JMSServiceReply. Note that JMQProducerID <b>must</b> be returned for an
     *  addProducer method.
     *
     *  @return The producerID
     */
    public long getJMQProducerID() throws NoSuchFieldException {
        return getLongProp("JMQProducerID");            
    }

    /**
     *  returns the ID of the Consumer from the addConsumer
     *  JMSServiceReply. Note that JMQConsumerID <b>must</b> be returned for an
     *  addConsumer method.
     *
     *  @return The consumerID
     */
    public long getJMQConsumerID() throws NoSuchFieldException {
        return getLongProp("JMQConsumerID");            
    }

    /**
     *  returns the ID of the Transaction from the startTransaction
     *  JMSServiceReply. Note that JMQTransactionID <b>must</b> be returned for an
     *  startTransaction method.
     *
     *  @return The transactionID
     */
    public long getJMQTransactionID() throws NoSuchFieldException {
        return getLongProp("JMQTransactionID");            
    }

    /////////////////////////////////////////////////////////////////////////
    // private methods
    /////////////////////////////////////////////////////////////////////////

    /**
     * sets the status of this JMSServiceReply
     */
    private void setStatus(){
        _status = Status.UNKNOWN;
        try {
            if(_replyProps !=null){
                try {
                    _status = (Status)_replyProps.get("JMQStatus");
                } catch (ClassCastException cce) {
                    Integer _tmp = (Integer)_replyProps.get("JMQStatus");
                    int _replyCode = _tmp.intValue();
                    _status = Status.UNKNOWN.convert(_replyCode);
                }
            }
        } catch (Exception e){
            System.err.println("JMSServiceReply:setStatus:Exception:"+e.getMessage());
        }
    }
    /////////////////////////////////////////////////////////////////////////
    // end private methods
    /////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////
    // public enums
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Enumerated Status responses for JMSService requests encapsulated in
     *  the methods defined in the JMSService interface.
     */
    public static enum Status implements EnumConverter<Status> {
        //XXX:TBD Update w/ remaining error codes as needed
        //jmq.io.Status
        /**
         *  200 JMSService request succeeded without error, warning,
         *      notification or exception.
         */
        OK (200),

        /**
         *  201
         */
        CREATED (201),

        /**
         *  300 
         */
        MULTIPLE_CHOICES (300),

        /**
         *  301  
         */
        MOVED_PERMENANTLY (301),

        /**
         *
         */
        NOT_MODIFIED (304),             //304

        /**
         *  400 JMSService request failed due to a request error.<p>
         *      An {@code addConsumer()} call that results in this Status
         *      indicates that the selector description is invalid.<p>
         *      
         */
        BAD_REQUEST (400),

        /**
         *  401 JMSService request failed due to an authorization failure
         */
        UNATUHORIZED (401),

        PAYMENT_REQUIRED (402),

        /**
         *  403 JMSService request was forbidden, unauthorized or otherwise not
         *      allowed.<p>
         *      A {@code creatConnection()} call that results in this Status
         *      indicates that there was an authorization failure or some other
         *      server failure in the autorization process.
         */
        FORBIDDEN (403),

        /**
         *  404 JMSService request was not completed due to a request component
         *      being non-existent.<p>
         *      A {@code sendmessage()} call that results in this Status
         *      indicates that the target Destination does not exist.
         */
        NOT_FOUND (404),

        /**
         *  405 JMSService request was not completed due to the request being
         *      not allowed.<p>
         *      An {@code addConsumer()} call that results in this Status
         *      indicates that the request is for a consumer to be created on
         *      a TemporaryDestination that was not created on the same
         *      connection.
         */
        NOT_ALLOWED (405),              //405

        /**
         *  408 JMSService request failed due to a server timeout
         */
        TIMEOUT (408),

        /**
         *  409 JMSService request failed due to a conflict between the request
         *      and the current state.
         *      <p>
         *      A {@code setClientId()} call that results in this Status
         *      indicates that an attempt to set the clientId for a connection
         *      was denied due to a conflict between the requested clientId
         *      and the clientId on an existing connection.
         */
        CONFLICT (409),

        /**
         *  412 JMSService request failed due to a pre-requisite condition not
         *      being met.<p>
         *      An {@code addConsumer()} call that results in this Status
         *      indicates that a missing clientID that was required to be set
         *      on the connection in order for a Durable Subscriber to be
         *      created.
         *
         */
        PRECONDITION_FAILED (412),

        /**
         *  413 JMSService request to login failed due to an authentication
         *      failure.<p>
         *      A createConnection() call that results in this Status indicates
         *      that a login authentication attempt for the given username and
         *      password was denied.
         */
        INVALID_LOGIN (413),

        /**
         *  414 JMSService request was not completed due to the target resource
         *      exceeding it's capacity.<p>
         *      A sendMessage() call that results in this Status indicates that
         *      the target Destination would exceed its configured allowable
         *      capacity.
         */
        RESOURCE_FULL (414),

        /**
         *  423 JMSService request was not completed due to a request component
         *      entity exceeding the allowable limit.<p>
         *      A sendmessage() call that results in this Status indicates that
         *      the message size exceeded the allowable, configured limits.
         */
        ENTITY_TOO_LARGE (423),

        /**
         *  500 JMSService request failed due to a server error.
         */
        ERROR (500),

        /**
         *  501 JMSService request failed due to a capability in the request not
         *      being implemented by the server.<p>
         *      A {@code starteTransaction()} call that results in this Status
         *      indicates that the JMQAutoRollback support requested is not
         *      implemented.
         */
        NOT_IMPLEMENTED (501),

        /**
         *  503 JMSService request failed due to the server being temporarily
         *      unavailable.
         */
        UNAVAILABLE (503),

        /**
         *  505 JMSService does not support the version of the protocol
         *      specified.
         */
        BAD_VERSION (505),

        /**
         *  999 JMSService Status is unknown.
         */
        UNKNOWN (999);

        
        private static ReverseEnumMap<Status> map =
        new ReverseEnumMap<Status>(Status.class);

        private final int _statusCode;
        
        Status(int statusCode){
            this._statusCode = statusCode;
        }

        public int getStatusCode(){
            return _statusCode;
        }

        /* Methods implementing EnumConverter interface */
        public int convert() {
            return _statusCode;
        }

        public Status convert(int val) {
            return map.get(val);
        } 
    }
}
