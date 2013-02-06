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

/** SendMsgsToTopic.java
  * This program publishes messages to JMQ Broker.
  * Using commandline args the user can speciify
  * 1. destination name
  * 2. no of messages
  *
  */

public class SendMsgsToTopic {


	private String destName          = null;
        private int noOfMsgs;
        final String MSG_TEXT    = new String("Here is a message");

	public static void main(String args[]) {

		int exitcode=0;

	       if ( (args.length < 1) || (args.length > 2) ) {
                  System.out.println("Usage: java SendMsgsToTopic <topic_name> [<number_of_messages>]");
                  System.exit(1);
        	}

		SendMsgsToTopic sendMsg = new SendMsgsToTopic();
		sendMsg.parseArgs(args);
		try {
			// Send messages to topic
			sendMsg.sendmsgs();
		}catch(JMSException ex) {
		 	ex.printStackTrace();
			exitcode = 1;
		}		
		System.exit(exitcode);
	}


        public void parseArgs(String[] args){

		destName = new String(args[0]);
        	System.out.println("Topic name is " + destName);
        	if (args.length == 2){
            	   noOfMsgs = (new Integer(args[1])).intValue();
        	} else {
            	   noOfMsgs = 1;
        	}

	}

	 /**
         * Send messages to the topic destination
         *
         * @param  none
         * @throws JMSException
         */
	
	public void sendmsgs() throws JMSException {

		ConnectionFactory cf = new com.sun.messaging.ConnectionFactory();

		// JMSContext will be closed automatically with the try-with-resources block
		try (JMSContext jmsContext = cf.createContext(JMSContext.AUTO_ACKNOWLEDGE);) {
		    System.out.println("Created jms context successfully");

		    // Create topic
		    Topic topic = jmsContext.createTopic(destName);
		    System.out.println("Created topic successfully");

		    /*
         	    * Create producer.
         	    * Create text message.
         	    * Send messages, varying text slightly.
         	    */
		    for (int i = 0; i < noOfMsgs; i++) {
                        String data = MSG_TEXT + " " + (i + 1);
                        System.out.println("Sending message: " + data);
                        jmsContext.createProducer().send(topic,data);
            	    }
		}
	}
}
