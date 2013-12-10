/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;

import com.sun.messaging.xml.MessageTransformer;

import com.sun.messaging.ConnectionFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jms.MessageListener;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.MessageConsumer;

import java.util.Iterator;

/**
 * This example shows a JMS message listener can use the MessageTransformer
 * utility to convert JMS messages back to SOAP messages.
 */
public class ReceiveSOAPMessageWithJMS implements MessageListener {

    ConnectionFactory        connectionFactory = null;
    Connection               connection = null;
    Session                  session = null;
    Topic                    topic = null;
    MessageConsumer          msgConsumer = null;

    MessageFactory           messageFactory = null;

    /**
     * Default constructor.
     */
    public ReceiveSOAPMessageWithJMS(String topicName) {
        init(topicName);
    }

    /**
     * JMS Connection/Session/Destination/MessageListener set ups.
     */
    public void init(String topicName) {
        try {

            /**
             * construct a default SOAP message factory.
             */
            messageFactory = MessageFactory.newInstance();

            /**
             * JMS set up.
             */
            connectionFactory = new com.sun.messaging.ConnectionFactory();
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            topic = session.createTopic(topicName);
            msgConsumer = session.createConsumer(topic);
            msgConsumer.setMessageListener( this );
            connection.start();

            System.out.println ("ready to receive SOAP messages ...");
        } catch (Exception jmse) {
            jmse.printStackTrace();
        }
    }

    /**
     * JMS Messages are delivered to this method. The body of the message
     * contains SOAP streams.
     *
     * 1.  The message conversion utility converts JMS message to SOAP
     * message type.
     * 2.  Get the attachment parts and print content information to the
     * standard output stream.
     */
    public void onMessage (Message message) {

        try {

            /**
             * convert JMS to SOAP message.
             */
            SOAPMessage soapMessage =
            MessageTransformer.SOAPMessageFromJMSMessage( message, messageFactory );

            /**
             * Print attachment counts.
             */
            System.out.println("message received!  Attachment counts: " + soapMessage.countAttachments());

            /**
             * Get attachment parts of the SOAP message.
             */
            Iterator iterator = soapMessage.getAttachments();
            while ( iterator.hasNext() ) {
                /**
                 * Get next attachment.
                 */
                AttachmentPart ap = (AttachmentPart) iterator.next();
                /**
                 * Get content type.
                 */
                String contentType = ap.getContentType();
                System.out.println("content type: " + contentType);
                /**
                 * Get content Id.
                 */
                String contentId = ap.getContentId();
                System.out.println("content Id: " + contentId);

                /**
                 * Check if this is a Text attachment.
                 */
                if ( contentType.indexOf("text") >=0 ) {
                    /**
                     * Get and print the content if it is a text
                     * attachment.
                     */
                    
                    Object content =  ap.getContent();
                    
                    /**
                     * content could be returned as an Input Stream.
                     */
                    if ( content instanceof InputStream ) {
                        
                        InputStreamReader isr = new InputStreamReader ((InputStream)content);
                        BufferedReader reader = new BufferedReader (isr);
                        
                        System.out.println("*** attachment content: ");
                        
                        String line = null;
                        while ( (line = reader.readLine ()) != null ) {
                            System.out.println (line);
                        }
                        
                    } else {
                        System.out.println("*** attachment content: " + content);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method to start the example receiver.
     */
    public static void main (String[] args) {

        String topicName = "TestTopic";

        if (args.length > 0) {
            topicName = args[0];
        }
        try {
            ReceiveSOAPMessageWithJMS rsm = new ReceiveSOAPMessageWithJMS(topicName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
