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

package com.sun.messaging.jms.ra;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.jmsservice.JMSAck;
import com.sun.messaging.jmq.jmsservice.JMSService.MessageAckType;

/**
 *
 */
public class DirectAck implements JMSAck {

    /** The connectionId of the JMSAck */
    private long connectionId;

    /** The sessionId of the JMSAck */
    private long sessionId;

    /** The consumerId of the JMSAck */
    private long consumerId;

    /** The Sun MQ SysMessageID of the JMSAck */
    private SysMessageID sysMessageID;

    /** The transactionId of the JMSAck */
    private long transactionId;

    /** The messageAckType of the JMSAck */
    private MessageAckType messageAckType;

    /** Creates a new instance of DirectAck */
    public DirectAck(long connectionId, long sessionId, long consumerId,
            SysMessageID sysMessageID, long transactionId,
            MessageAckType messageAckType) {
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.consumerId = consumerId;
        this.sysMessageID = sysMessageID;
        this.messageAckType = messageAckType;
    }

    /**
     *  Return the connectionId of this JMSAck
     *
     *  @return The connectionId
     */
    public long getConnectionId(){
        return this.connectionId;
    }

    /**
     *  Return the consumerId of this JMSAck
     *
     *  @return The consumerId
     */
    public long getConsumerId(){
        return this.consumerId;
    }

    /**
     *  Return the messageAckType of this JMSAck
     *
     *  @return The messageAckType
     */
    public MessageAckType getMessageAckType(){
        return this.messageAckType;
    }

    /**
     *  Return the sessionId of this JMSAck
     *
     *  @return The sessionId
     */
    public long getSessionId(){
        return this.sessionId;
    }

    /**
     *  Return the sysMessageID of this JMSAck
     *
     *  @return The sysMessageID
     */
    public SysMessageID getSysMessageID(){
        return this.sysMessageID;
    }

    /**
     *  Return the transactionId of this JMSAck
     *
     *  @return The transactionId
     */
    public long getTransactionId(){
        return this.transactionId;
    }
}
