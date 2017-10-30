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

import javax.jms.*;

/**
 * The MessageHeadersProducer.class sends messages to a queue with
 * message header properties set on producer
 * <p>
 * Run this program in conjunction with MessageHeadersConsumer.
 * Specify a queue name on the command line when you run
 * the program.
 */

public class MessageHeadersProducer {

	private String destName          = null;
	static int exitcode = 0;

	/**
        * Main method.
        *
        * @param args      the queue used by the example
        */
        public static void main(String args[]) {

		if ( args.length < 1 ) {
                  System.out.println("Usage: java MessageHeadersProducer <queue_name> ");
                  System.exit(1);
                }

		// Send messages to queue with message header properties set.
                MessageHeadersProducer msgHeadersProducer = new MessageHeadersProducer();
		msgHeadersProducer.parseArgs(args);

                try {
                        msgHeadersProducer.runTest();
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
	 * Test JMSProducer method send(Destination destination, Message message),
         * with a TextMessage ensuring that you can set the four supported message
         * headers
	 * 
	 * @param  none
	 * @throws JMSException
	 */
	private void runTest() throws JMSException {

		String uniqueID = Long.toString(System.currentTimeMillis());

		byte[] jmsCorrelationIDAsBytes = { 77, 121, 67, 111, 114, 114, 101, 108, 97, 116, 105, 111, 110, 73, 68 };
                String jmsCorrelationIDAsString = "MyCorrelationID";
                String jmsType = "MyJMSType";
                String jmsReplyTo = "SomeOtherQueue";

		// send a message
		{
			ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			JMSContext context = connectionFactory.createContext();
			JMSProducer producer = context.createProducer();
			System.out.println("Set message headers on producer");
			// setJMSReplyTo
			System.out.println("setJMSReplyTo on producer");
			producer.setJMSReplyTo(context.createQueue(jmsReplyTo));
			// setJMSType
			System.out.println("setJMSType on producer");
                        producer.setJMSType(jmsType);
			// setJMSCorrelationID 
			System.out.println("setJMSCorrelationID on producer");
			producer.setJMSCorrelationID(jmsCorrelationIDAsString);
			// setJMSCorrelationIDAsBytes
			System.out.println("setJMSCorrelationIDAsBytes on producer");
                        producer.setJMSCorrelationIDAsBytes(jmsCorrelationIDAsBytes);

			// send message
			producer.send(context.createQueue(destName),context.createTextMessage(uniqueID));
			System.out.println("Message "+uniqueID+" sent successfully");
			context.close();
		}


	}
			

}
