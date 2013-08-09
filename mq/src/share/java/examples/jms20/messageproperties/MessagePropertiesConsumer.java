
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
 * The MessagePropertiesConsumer.class receives message from a queue
 * and check the message properties.
 * <p>
 * Run this program in conjunction with MessagePropertiesProducer.
 * Specify a queue name on the command line when you run
 * the program.
 */

public class MessagePropertiesConsumer {

	private String destName          = null;
	static int exitcode = 0;

	/**
        * Main method.
        *
        * @param args      the queue used by the example
        */
        public static void main(String args[]) {

		if ( args.length < 1 ) {
                  System.out.println("Usage: java MessagePropertiesConsumer <queue_name> ");
                  System.exit(1);
                }

                MessagePropertiesConsumer msgPropertiesConsumer = new MessagePropertiesConsumer();
		msgPropertiesConsumer.parseArgs(args);

                try {
			//receive messages
                        msgPropertiesConsumer.runTest();
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
	 * Receive TextMessage and check the message properties that are set on message
	 * 
	 * @param  none
	 * @throws JMSException
	 */
	private void runTest() throws JMSException {

		String uniqueID = Long.toString(System.currentTimeMillis());
		String queueName = destName + uniqueID;

		// receive a message
		{
			ConnectionFactory connectionFactory = new com.sun.messaging.ConnectionFactory();
			JMSContext context = connectionFactory.createContext();
			JMSConsumer consumer = context.createConsumer(context.createQueue(destName));
			TextMessage textMessage = (TextMessage) consumer.receive(1000);
                        String payload = textMessage.getText();

			if ( textMessage != null) {
                                System.out.println("Message Received : "+textMessage.getText());
                        } else {
                                System.out.println("Message not Received..");
                                exitcode=1;
                                return;
                        }

                       	// check message properties
                       	//

			// boolean 
			System.out.println( "booleanProp on Message through getObjectProperty :" + textMessage.getObjectProperty("booleanProp"));
			System.out.println( "booleanProp on Message through getBooleanProperty :" + textMessage.getBooleanProperty("booleanProp"));

			// byte
			System.out.println( "byteProp on Message through getObjectProperty :" + textMessage.getObjectProperty("byteProp"));
			System.out.println( "byteProp on Message through getBytesProperty :" + textMessage.getByteProperty("byteProp"));

			// short
			System.out.println( "shortProp on Message through getObjectProperty :" + textMessage.getObjectProperty("shortProp"));
                       	System.out.println( "shortProp on Message through getShortProperty :" + textMessage.getShortProperty("shortProp"));

			// int
                       	System.out.println( "intProp on Message through getObjectProperty :" + textMessage.getObjectProperty("intProp"));
                       	System.out.println( "intProp on Message through getIntProperty :" + textMessage.getIntProperty("intProp"));

			// long
                       	System.out.println( "longProp on Message through getObjectProperty :" + textMessage.getObjectProperty("longProp"));
                       	System.out.println( "longProp on Message through getLongProperty :" + textMessage.getLongProperty("longProp"));

			// float
                       	System.out.println( "floatProp on Message through getObjectProperty :" + textMessage.getObjectProperty("floatProp"));
                       	System.out.println( "floatProp on Message through getFloatProperty :" + textMessage.getFloatProperty("floatProp"));

			// double
			System.out.println( "doubleProp on Message through getObjectProperty :" +textMessage.getObjectProperty("doubleProp"));
                       	System.out.println( "doubleProp on Message through getDoubleProperty :" +textMessage.getDoubleProperty("doubleProp"));

			// String
			System.out.println( "stringProp on Message through getObjectProperty :" +textMessage.getObjectProperty("stringProp"));
                       	System.out.println( "stringProp on Message through getStringProperty :" +textMessage.getStringProperty("stringProp"));

			context.close();
		}


	}
			

}
