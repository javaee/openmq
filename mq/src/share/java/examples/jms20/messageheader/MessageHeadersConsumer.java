
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

import javax.jms.*;

/**
 * The MessageHeadersConsumer.class receives message from a queue
 * and check the message headers.
 * <p>
 * Run this program in conjunction with MessageHeadersProducer.
 * Specify a queue name on the command line when you run
 * the program.
 */

public class MessageHeadersConsumer {

	private String destName          = null;
	static int exitcode = 0;

	/**
        * Main method.
        *
        * @param args      the queue used by the example
        */
        public static void main(String args[]) {

		if ( args.length < 1 ) {
                  System.out.println("Usage: java MessageHeadersConsumer <queue_name> ");
                  System.exit(1);
                }

		// receive messages
                MessageHeadersConsumer msgHeadersConsumer = new MessageHeadersConsumer();
		msgHeadersConsumer.parseArgs(args);

                try {
                        msgHeadersConsumer.runTest();
                }catch(JMSException ex) {
                        ex.printStackTrace();
                        exitcode = 1;
                }
                System.exit(exitcode);
        }
	
	/**
        * parseArgs method.
        *
        * @param args  the arguments that are passed through main method
        */
	public void parseArgs(String[] args){

                destName = new String(args[0]);
                System.out.println("Queue name is " + destName);
        }


	/**
	 * Receive TextMessage and check the message headers that are set on message
	 * 
	 * @param  none
	 * @throws JMSException
	 */
	private void runTest() throws JMSException {

		// receive a message
		{
			ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			JMSContext context = connectionFactory.createContext();
			JMSConsumer consumer = context.createConsumer(context.createQueue(destName));
			TextMessage textMessage = (TextMessage) consumer.receive(1000);
		
			if ( textMessage != null) {
				System.out.println("Message Received : "+textMessage.getText());
			} else {
				System.out.println("Message not Received..");
                        	exitcode=1;
                        	return;
                	}
                       	// check message header
                       	//

			// check JMSType message header
			System.out.println("getJMSType : "+textMessage.getJMSType());
			// check JMSReplyTo mesage header
			System.out.println("getJMSReplyTo : "+textMessage.getJMSReplyTo());
			// check getJMSCorrelationIDAsBytes message header
			byte[] jmsCorrelationIdAsBytesRead = textMessage.getJMSCorrelationIDAsBytes();
			System.out.println("getJMSCorrelationIdAsBytes length : "+ jmsCorrelationIdAsBytesRead.length);
                       	for (int i = 0; i < jmsCorrelationIdAsBytesRead.length; i++) {
                        	System.out.println("getJMSCorrelationIdAsBytes[" + i + "] :" + jmsCorrelationIdAsBytesRead[i]);
                       	}
		
			// check getCorrelationID
			String jmsCorrelationIdAsStringRead = textMessage.getJMSCorrelationID();
                        System.out.println("getJMSCorrelationID : "+jmsCorrelationIdAsStringRead);

			context.close();
		}


	}
			

}
