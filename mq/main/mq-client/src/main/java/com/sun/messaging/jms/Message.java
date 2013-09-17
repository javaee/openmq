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

/*
 * @(#)Message.java	1.5 07/02/07
 */ 

package com.sun.messaging.jms;

/** The <CODE>com.sun.messaging.jms.Message</CODE> interface defines
  * enhanced capabilities of a JMS Message in Oracle GlassFish(tm) Server Message Queue.
  * <P>
  * It defines
  * <UL>
  *   <LI>Additional methods available for custom message acknowledgement
  * behavior.
  * </UL>
  *
  * @see         javax.jms.Message
  */

public interface Message {

    /** Acknowledges this consumed message only.
      *  
      * <P>All consumed JMS messages in Oracle GlassFish(tm) Server Message Queue support the
      * <CODE>acknowledgeThisMessage</CODE> 
      * method for use when a client has specified that its JMS session's 
      * consumed messages are to be explicitly acknowledged.  By invoking 
      * <CODE>acknowledgeThisMessage</CODE> on a consumed message, a client
      * acknowledges only the specific message that the method is invoked on.
      * 
      * <P>Calls to <CODE>acknowledgeThisMessage</CODE> are ignored for both transacted 
      * sessions and sessions specified to use implicit acknowledgement modes.
      *
      * @exception javax.jms.JMSException if the messages fail to get
      *            acknowledged due to an internal error.
      * @exception javax.jms.IllegalStateException if this method is called
      *            on a closed session.
      *
      * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
      * @see javax.jms.Message#acknowledge() javax.jms.Message.acknowledge()
      * @see com.sun.messaging.jms.Message#acknowledgeUpThroughThisMessage()
      */ 
    void
    acknowledgeThisMessage() throws javax.jms.JMSException;

    /** Acknowledges consumed messages of the session up through
      * and including this consumed message.
      *  
      * <P>All consumed JMS messages in Oracle GlassFish(tm) Server Message Queue support the
      * <CODE>acknowledgeUpThroughThisMessage</CODE> 
      * method for use when a client has specified that its JMS session's 
      * consumed messages are to be explicitly acknowledged.  By invoking 
      * <CODE>acknowledgeUpThroughThisMessage</CODE> on a consumed message,
      * a client acknowledges messages starting with the first
      * unacknowledged message and ending with this message that
      * were consumed by the session that this message was delivered to.
      * 
      * <P>Calls to <CODE>acknowledgeUpThroughThisMessage</CODE> are
      * ignored for both transacted sessions and sessions specified
      * to use implicit acknowledgement modes.
      *
      * @exception javax.jms.JMSException if the messages fail to get
      *            acknowledged due to an internal error.
      * @exception javax.jms.IllegalStateException if this method is called
      *            on a closed session.
      *
      * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
      * @see javax.jms.Message#acknowledge() javax.jms.Message.acknowledge()
      * @see com.sun.messaging.jms.Message#acknowledgeThisMessage()
      */ 
    void
    acknowledgeUpThroughThisMessage() throws javax.jms.JMSException;

}
