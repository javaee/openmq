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

package com.sun.messaging.jms.ra;


import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;

import com.sun.messaging.jmq.io.JMSPacket;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.jmsservice.JMSService;

/**
 *
 */
public class DirectTextPacket
        extends DirectPacket
        implements javax.jms.TextMessage {

    /**
     *  The String hat holds the JMS TextMessage body
     */
    private String text = null;

    /**
     *  Logging
     */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.DirectTextPacket";

    /** 
     *  Create a new instance of DirectTextPacket.<p>
     *  
     *  Used by createTextMessage API
     *  
     */
    public DirectTextPacket(DirectSession ds, String txt)
    throws JMSException {
        super(ds);
        if (_logFINE){
            Object params[] = new Object[3];
            params[0] = ds;
            params[2] = txt;
            _loggerOC.entering(_className, "constructor()", params);
        }
        this.text = txt;
    }

    /**
     *  Create a new instance of DirectTextPacket.
     *  Used by Consumer.deliver.
     */
    public DirectTextPacket(JMSPacket jmsPacket, long consumerId,
            DirectSession ds, JMSService jmsservice)
    throws JMSException {
        super(jmsPacket, consumerId, ds, jmsservice);
        this._getMessageBodyFromPacket();
    }
    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.TextMessage
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Clear out the message body .
     */
    public void clearBody()
    throws JMSException {
        super.clearBody();
        this.text = null;
    }

    /**
     *  Get the string containing this message's data.  The default
     *  value is null.
     *  
     *  @return The <CODE>String</CODE> containing the message's data
     *  
     *  @throws JMSException if the JMS provider fails to get the text due to
     *          some internal error.
     */ 
    public String getText()
    throws JMSException {
        if (_logFINE){
            String methodName = "getText()";
            _loggerJM.fine(_lgrMID_INF+/*"messageId="+messageId+":"+*/
                    methodName+":"/*+this.text*/);
        }
        return text;
    }

    /**
     *  Set the string containing this message's data.
     *  
     *  @param string the <CODE>String</CODE> containing the message's data
     *  
     *  @throws JMSException if the JMS provider fails to set the text due to
     *          some internal error.
     *  @throws MessageNotWriteableException if the message is in read-only 
     *          mode.
     */ 
    public void setText(String string)
    throws JMSException {
        String methodName = "setText()";
        if (_logFINE){
            _loggerJM.fine(_lgrMID_INF+/*"messageId="+messageId+":"+*/
                    methodName+":"+string);
        }
        this.checkForReadOnlyMessageBody(methodName);
        this.text = string;
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.TextMessage
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  MQ methods DirectTextPacket / javax.jms.TextMessage
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Set the JMS default values on this JMS TextMessage
     */
    protected void _setDefaultValues()
    throws JMSException {
        super._setDefaultValues();
        this.pkt.setPacketType(PacketType.TEXT_MESSAGE);
    }

    /**
     *  Set the JMS Message body into the packet
     */
    protected void _setBodyToPacket()
    throws JMSException {
        if (this.text != null) {
            try {
                super._setMessageBodyOfPacket(text.getBytes(UTF8));
            } catch (Exception ex) {
                String errMsg = _lgrMID_EXC +
                        ":ERROR setting TextMessage body="+ this.text +
                        ":Exception="+ ex.getMessage();
                _loggerJM.severe(errMsg);
                JMSException jmse = new javax.jms.JMSException(errMsg);
                jmse.initCause(ex);
                throw jmse;
            }
        }
    }

    /**
     *  Get the JMS Message body from the packet on a receeived message
     */
    protected void _getMessageBodyFromPacket()
    throws JMSException {
        try {
            byte[] btext = this._getMessageBodyByteArray();
            if (btext != null) {
                this.text = new String(btext, UTF8);
            }
        } catch (Exception e) {
            String errMsg = _lgrMID_EXC +
                    ":Exception getting body for receieved TextMessage"+
                    e.getMessage();
            _loggerJM.severe(errMsg);
            JMSException jmse = new javax.jms.JMSException(errMsg);
            jmse.initCause(e);
            throw jmse;
        }
    }
}
