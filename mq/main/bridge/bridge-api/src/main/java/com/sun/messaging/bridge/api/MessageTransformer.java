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

package com.sun.messaging.bridge.api;

import java.util.Properties;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 *
 * The message transformer class to be extended by user. 
 * Its implementation must provide a public zero-argument constructor.
 *
 * The following is an example usage of this class for MQ STOMP bridge
 * <pre>
 * import java.util.*;
 * import javax.jms.*;
 * import com.sun.messaging.bridge.api.MessageTransformer;
 *
 * public class MessageTran extends MessageTransformer &lt;Message, Message&gt; {
 *
 * public Message transform(Message message, 
 *                          boolean readOnly,
 *                          String charsetName,
 *                          String source, 
 *                          String target,
 *                          Properties properties)
 *                          throws Exception {
 *
 *    Message m = message;
 *    if (source.equals(STOMP)) { //from STOMP client to Java Message Queue
 *        //convert any invalid headers from STOMP SEND frame
 *        if (properties != null) {
 *            ......
 *            //convert key to valid JMS message property name, then call m.setStringProperty()
 *            ......
 *        }
 *     
 *    } else if (source.equals(SUN_MQ)) { //from Java Message Queue to STOMP client
 *
 *        if (message instanceof ObjectMessage) {
 *
 *            //create a new BytesMessage for <i>message</i> to be transformed to 
 *            BytesMessage bm = (BytesMessage)createJMSMessage(JMSMessageType.BYTESMESSAGE);
 *               
 *            //convert <i>message</i> to the BytesMessage
 *            ......
 *            m = bm;
 *        } else {
 *            ....
 *        }
 *    }
 *    return m;
 * }
 *</pre>
 *
 * @author amyk
 */
public abstract class MessageTransformer <T, S>
{
    /**
     * The predefined provider name for JMS message to/from Sun Java Message Queue
     */
    public static final String SUN_MQ = "SUN_MQ";

    /**
     * The predefined provider name for JMS message to/from STOMP client
     */
    public static final String STOMP = "STOMP";

    private Object _obj = null;
    private Object _branchTo = null;
    private String _bridgeType = null;
    private boolean _notransfer = false;

    public enum JMSMessageType {
        MESSAGE, TEXTMESSAGE, BYTESMESSAGE, MAPMESSAGE, STREAMMESSAGE, OBJECTMESSAGE 
    }

    /**
     * This method is called by the bridge service before transform() is called.
     *
     * A message transformer object is initialized by init() each time 
     * before transform() is called. After transform() returns, it's back  
     * to uninitialized state.
     *
     */
    public final void init(Object obj, String bridgeType) {
        _obj = obj;
        _branchTo = null;
        _bridgeType = bridgeType;
        _notransfer = false;
    }

    /**
     * This method is called by the bridge service after transform()
     * is returned for bridge types that support branchTo()
     */
    public final Object getBranchTo() {
        return _branchTo;
    }

    /**
     * This method is called by the bridge service after transform()
     * is returned for bridge types that support noTransfer()
     */
    public final boolean isNoTransfer() {
        return _notransfer;
    }

   

    /**
     * Create a JMS message object.
     *
     * This method is to be used in tranform() method implemenation
     * when it needs to create a new JMS message 
     *
     * @param type the type of the JMS message to be created
     *
     * @return a newly created uninitialized JMS message object 
     *
     * @exception IllegalStateException if this MessageTransfomer object is not initialized
     *
     * @exception Exception if fails to create the JMS Message
     */
    protected final Message createJMSMessage(JMSMessageType type) throws Exception {
        javax.jms.Session ss = (javax.jms.Session)_obj; 
        if (ss == null) {
            throw new IllegalStateException("The MessageTransformer is not initialized !");
        }
        switch (type) {
            case MESSAGE:
                 return ss.createMessage();
            case TEXTMESSAGE:
                 return ss.createTextMessage();
            case BYTESMESSAGE:
                 return ss.createBytesMessage();
            case MAPMESSAGE:
                 return ss.createMapMessage();
            case STREAMMESSAGE:
                 return ss.createStreamMessage();
            case OBJECTMESSAGE:
                 return ss.createObjectMessage();
            default: throw new IllegalArgumentException("Unexpected message type "+type);
        }
    }

    /**
     * To be called from the transform() method when needs to create a JMS Queue 
     * object to the target provider	
     *
     * @param queueName the name of the Queue 
     *
     * @return a javax.jms.Queue object 
     *
     * @exception IllegalStateException if this MessageTransfomer object is not initialized
     * @exception Exception if fails to create the Queue object
     */
    protected final Queue createQueue(String queueName) throws Exception {
        javax.jms.Session ss = (javax.jms.Session)_obj; 
        if (ss == null) {
            throw new IllegalStateException("The MessageTransformer is not initialized !");
        }
        return ss.createQueue(queueName);
    }

    /**
     * To be called from the transform() method when needs to create a JMS Topic 
     * object to the target provider	
     *
     * @param topicName the name of the Topic 
     *
     * @return a javax.jms.Topic object 
     *
     * @exception IllegalStateException if this MessageTransfomer object is not initialized
     * @exception Exception if fails to create the Topic object
     */
    protected final Topic createTopic(String topicName) throws Exception {
        javax.jms.Session ss = (javax.jms.Session)_obj; 
        if (ss == null) {
            throw new IllegalStateException("The MessageTransformer is not initialized !");
        }
        return ss.createTopic(topicName);
    }

    /**
     * To be called from the transform() method when needs to tell the bridge to
     * branch the message that is to be returned by the transform() call to a 
     * different destination in the target provider
     *
     * @param d a java.lang.String or javax.jms.Destination object that specifies
     *          the destination in target provider to branch the message to
     *
     * @exception IllegalStateException if this MessageTransfomer object is not initialized
     * @exception IllegalArgumentException if null or unexpected object type passed in
     * @exception UnsupportedOperationException if the operation is not supported for the bridge type
     *
     * @exception Exception if fails to create the JMS Message
     */
    protected final void branchTo(Object d) throws Exception {
        if (_obj == null) {
            throw new IllegalStateException(
            "The MessageTransformer is not initialized !");
        }
        if (!_bridgeType.equals(Bridge.JMS_TYPE)) {
            throw new UnsupportedOperationException(
            "MessageTransformer.branchTo() is not supported for bridge type "+_bridgeType);
        }
        if (d == null) {
            throw new IllegalArgumentException("null passed to MessageTransformer.branchTo()");
        }
        if (!(d instanceof String) && !(d instanceof javax.jms.Destination)) {
            throw new IllegalArgumentException(
            "Unexpected branchTo object type: "+d.getClass().getName());
        }
        _branchTo = d;
    }


    /**
     * To be called from the transform() method when needs to tell the bridge
     * to consume from source and not transfer to target the message that is 
     * to be returned by the transform() call
     *
     * @exception IllegalStateException if this MessageTransfomer object is not initialized
     * @exception UnsupportedOperationException if the operation is not supported for the bridge type
     *
     */
    protected final void noTransfer() throws Exception { 
        if (_obj == null) {
            throw new IllegalStateException(
            "The MessageTransformer is not initialized !");
        }
        if (!_bridgeType.equals(Bridge.JMS_TYPE)) {
            throw new UnsupportedOperationException(
            "MessageTransformer.noTransfer() is not supported for bridge type "+_bridgeType);
        }
        _notransfer = true; 
    }

    /**
     * To be implemented by user 
     *
     * @param message the message object to be tranformed. 
     * @param readOnly if the <i>message</i> is in read-only mode 
     * @param charsetName the charset name for <i>message</i> if applicable, null if not available 
     * @param source the source provider name 
     * @param target the target provider name 
     * @param properties any properties for the transform() call, null if none
     *
     * @return a message object that is transformed from the passed in <i>message</i>
     *
     * @throws Exception if unable to transform <i>message</i>
     */
    public abstract T transform(S message, 
                                boolean readOnly,
                                String charsetName, 
                                String source, 
                                String target,
                                Properties properties) 
                                throws Exception;

}
