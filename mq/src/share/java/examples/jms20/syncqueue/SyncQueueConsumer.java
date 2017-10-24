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
import java.io.Serializable;

/**
 * The SyncQueueConsumer.class receives messages from
 * a queue using synchronous message delivery.
 * <p>
 * Run this program in conjunction with SendObjectMsgsToQueue.
 * Specify a queue name on the command line when you run
 * the program. By default, the program receives one message. Specify a number
 * after the queue name to receive that number of messages.
 */

public class SyncQueueConsumer {

	private String destName          = null;
        private int noOfMsgs;
	static int exitcode		 = 0;

	/**
        * Main method.
        *
        * @param args      the queue used by the example and, optionally, the
        *                   number of messages to receive
        */
	public static void main(String args[]) {

		 if ( (args.length < 1) || (args.length > 2) ) {
                  System.out.println("Usage: java SyncQueueConsumer <queue_name> [<number_of_messages>]");
                  System.exit(1);
                }

		SyncQueueConsumer recvMsg = new SyncQueueConsumer();
		recvMsg.parseArgs(args);
		try {
			// Receive messages from queue
			recvMsg.receivemsgs();
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
                if (args.length == 2){
                   noOfMsgs = (new Integer(args[1])).intValue();
                } else {
                   noOfMsgs = 1;
                }

        }

	 /**
         * Receive object messages synchronously from the queue destination
         *
         * @param  none
         * @throws JMSException
         */
	
	public void receivemsgs() throws JMSException {

		// Create connection factory for the MQ client
		ConnectionFactory cf = new com.sun.messaging.ConnectionFactory();

		// JMSContext will be closed automatically with the try-with-resources block
		try (JMSContext jmsContext = cf.createContext(JMSContext.AUTO_ACKNOWLEDGE);) {
                    System.out.println("Created jms context successfully");
	
		    // Create queue 
		    Queue queue = jmsContext.createQueue(destName);
		    System.out.println("Created queue successfully");

		    // Create consumer
		    JMSConsumer consumer = jmsContext.createConsumer(queue);
		    jmsContext.start();

		    // Receive msgs
		    for (int i = 0; i < noOfMsgs; i++) {
			
		       //Receive messages using receiveBody(Class<T> c, long timeout)
	
		       MyObject payload = consumer.receiveBody(MyObject.class, 1000);

		       if( payload != null ) {
			  System.out.println("Message Received : "+payload.getValue());
		       } else {
			  System.out.println("Message not received");
			  exitcode=1;
			  break;
		       }

		   }	
		
	      }

	}

}
