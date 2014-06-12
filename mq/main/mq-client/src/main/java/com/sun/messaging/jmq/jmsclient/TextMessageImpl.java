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
 * @(#)TextMessageImpl.java	1.14 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.io.*;
import javax.jms.*;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.io.PacketType;

/** A TextMessage is used to send a message containing a
 * <CODE>java.lang.String</CODE>.
 * It inherits from <CODE>Message</CODE> and adds a text message body.
 *
 * <P>The inclusion of this message type is based on our presumption
 * that XML will likely become a popular mechanism for representing
 * content of all kinds including the content of JMS messages.
 *
 * <P>When a client receives a TextMessage, it is in read-only mode. If a
 * client attempts to write to the message at this point, a
 * MessageNotWriteableException is thrown. If <CODE>clearBody</CODE> is
 * called, the message can now be both read from and written to.
 *
 * @see         javax.jms.Session#createTextMessage()
 * @see         javax.jms.Session#createTextMessage(String)
 * @see         javax.jms.BytesMessage
 * @see         javax.jms.MapMessage
 * @see         javax.jms.Message
 * @see         javax.jms.ObjectMessage
 * @see         javax.jms.StreamMessage
 * @see         java.lang.String
 */

public class TextMessageImpl
    extends MessageImpl
    implements TextMessage {

  private String text = null;

  //serialize message body
  //This is called when producing messages.
  protected void
      setMessageBodyToPacket() throws JMSException {

    //ObjectOutputStream oos;
    if (text == null) {
      return;
    }
    try {
      setMessageBody(text.getBytes(UTF8));
    }
    catch (Exception e) {
      ExceptionHandler.handleException(e,
                                       AdministeredObject.cr.X_MESSAGE_SERIALIZE);
    }

  }

  //deserialize message body
  //This is called after message is received
  protected void
      getMessageBodyFromPacket() throws JMSException {

    //InputStream is = getMessageBodyStream();
    //ObjectInputStream ois = new ObjectInputStream (is);
    //text = (String) ois.readObject();

    try {
      byte[] body = getMessageBody();
      if (body != null) {
        text = new String(body, UTF8);
      }
    }
    catch (Exception e) {
      ExceptionHandler.handleException(e,
                                       AdministeredObject.cr.X_MESSAGE_DESERIALIZE);
    }
  }

  /**
   * Constructor.
   */
  protected TextMessageImpl() throws JMSException {
    super();
    setPacketType(PacketType.TEXT_MESSAGE);
  }

  /**
   * clear body
   */
  public void clearBody() throws JMSException {
    text = null;
    setMessageReadMode(false);
  }

  /** Set the string containing this message's data.
   *
   * @param string the String containing the message's data
   *
   * @exception JMSException if JMS fails to set text due to
   *                         some internal JMS error.
   * @exception MessageNotWriteableException if message in read-only mode.
   */

  public void
      setText(String string) throws JMSException {
    checkMessageAccess();
    text = string;
  }

  /** Get the string containing this message's data.  The default
   * value is null.
   *
   * @return the String containing the message's data
   *
   * @exception JMSException if JMS fails to get text due to
   *                         some internal JMS error.
   */

  public String
      getText() throws JMSException {
    return text;
  }

  public String toString() {
    return new StringBuffer().append("\nText:\t").append(text).append(super.
        toString()).toString();
  }

  public void dump(PrintStream ps) {
    ps.println("------ TextMessageImpl dump ------");
    super.dump(ps);
  }
}
