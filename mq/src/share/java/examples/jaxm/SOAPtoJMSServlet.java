/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import javax.xml.messaging.JAXMServlet;
import javax.xml.messaging.ReqRespListener;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPPart;

import com.sun.messaging.xml.MessageTransformer;

import com.sun.messaging.TopicConnectionFactory;

import javax.jms.MessageListener;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.JMSException;
import javax.jms.TopicPublisher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * This example shows how to use the MessageTransformer utility to convert SOAP
 * message to JMS message.  When SOAP messages are received, they are
 * delivered to the ReqRespListener's onMessage() method.  The onMessage()
 * implementation uses the utility to convert SOAP to JMS message, then
 * publishes the message to the JMS Topic.
 * <p>
 * The onMessage() method adds <MessageStatus> element with value "published"
 * to the SOAPBody and returns the SOAP message to the caller.
 */
public class SOAPtoJMSServlet extends JAXMServlet implements ReqRespListener {

    TopicConnectionFactory tcf = null;
    TopicConnection tc = null;
    TopicSession session = null;
    Topic topic = null;

    TopicPublisher publisher = null;

    /**
     * The init method set up JMS Connection/Session/Publisher.
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {

            tcf = new com.sun.messaging.TopicConnectionFactory();

            tc = tcf.createTopicConnection();
            session = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            String topicName = config.getInitParameter("TopicName");
            if ( topicName == null ) {
                topicName = "TestTopic";
            }

            topic = session.createTopic(topicName);
            publisher = session.createPublisher(topic);

        } catch (Exception jmse) {
            throw new ServletException (jmse);
        }
    }

    /**
     * Clean up JMS connection.
     */
    public void destroy() {
        try {
            if ( tc != null ) {
                tc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SOAP Messages are delivered to this method and then published to the
     * JMS topic destination.
     */
    public SOAPMessage onMessage (SOAPMessage soapMessage) {

        try {
            Message message =
            MessageTransformer.SOAPMessageIntoJMSMessage(soapMessage, session);

            publisher.publish( message );

        } catch (Exception e) {
            e.printStackTrace();
        }

        SOAPMessage resp = generateResponseMessage(soapMessage);

        return resp;
    }

    /**
     * Add a MessageStatus element with the value of "published" to
     * the soapMessage.
     */
    public SOAPMessage generateResponseMessage(SOAPMessage soapMessage) {

        try {
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPBody soapBody = envelope.getBody();

            soapBody.addChildElement("MessageStatus").addTextNode("published");
            soapMessage.saveChanges();
        } catch (SOAPException soape) {
            soape.printStackTrace();
        }

        return soapMessage;
    }

}
