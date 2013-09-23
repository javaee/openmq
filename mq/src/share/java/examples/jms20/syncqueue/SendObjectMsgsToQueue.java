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
import java.io.Serializable;

/**
 * The SendObjectMsgsToQueue.class sends several object messages to a queue.
 * <p>
 * Run this program in conjunction with SyncQueueConsumer.
 * Specify a queue name on the command line when you run
 * the program.  By default, the program sends one message.  Specify a number
 * after the queue name to send that number of messages.
 */

public class SendObjectMsgsToQueue{


	private String destName          = null;
        private int noOfMsgs;
        final String MSG_TEXT    = new String("Here is a message");
	static int exitcode      = 0;

	/**
        * Main method.
        *
        * @param args      the queue used by the example and, optionally, the
        *                   number of messages to send
        */
	public static void main(String args[]) {

	       if ( (args.length < 1) || (args.length > 2) ) {
                  System.out.println("Usage: java SendObjectMsgsToQueue <queue_name> [<number_of_messages>]");
                  System.exit(1);
        	}

		SendObjectMsgsToQueue sendMsg = new SendObjectMsgsToQueue();
		sendMsg.parseArgs(args);
		try {
			// Send messages to queue
			sendMsg.sendmsgs();
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
         * Send object messages to the queue destination
         *
         * @param  none
         * @throws JMSException
         */
	
	public void sendmsgs() throws JMSException {

		ConnectionFactory cf = new com.sun.messaging.ConnectionFactory();

		// JMSContext will be closed automatically with the try-with-resources block
		try (JMSContext jmsContext = cf.createContext(JMSContext.AUTO_ACKNOWLEDGE);) {
		    System.out.println("Created jms context successfully");

		    // Create queue 
		    Queue queue = jmsContext.createQueue(destName);
		    System.out.println("Created queue successfully");

		    /*
         	    * Create producer.
         	    * Create object message.
         	    * Send Object messages.
         	    */
		    for (int i = 0; i < noOfMsgs; i++) {
			
                        MyObject data = new MyObject(i + 1);
                        jmsContext.createProducer().send(queue,data);
		        System.out.println("Message sent : " + data.getValue());
            	    }
		}
	}

}
