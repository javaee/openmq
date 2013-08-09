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

import java.util.*;
import javax.jms.*;

/**
 * The SharedDuraConsumerExample class demonstrates 
 * the use of multiple dura subscribers
 * sharing messages that are published.
 * <p>
 * The program contains a Subscriber class,
 * a main method, and a method that runs the subscriber
 * threads.
 * <p>
 * The program creates two instances of the Subscriber class 
 * that displays the messages that
 * the shared durable subscribers receive.  Because all the 
 * objects run in threads, the displays are interspersed when the program runs.
 * <p>
 * Specify a topic name & no of msgs on the command line when you run the program.
 */
public class SharedDuraConsumerExample {

    static int                  exitcode     = 0;
    private String destName                  = null;
    private int noOfMsgs;
    static int msgsReceived                  = 0;
    static boolean doneSignal		     = false;


     /**
     * Reads the topic name from the command line, then calls the
     * run_threads method to execute the program threads.
     *
     * @param args      the topic used by the example
     */
    public static void main(String[] args) {

	if ( (args.length < 1) || (args.length > 2) ) {
                  System.out.println("Usage: java SharedDuraConsumerExample <topic_name> [<number_of_messages>]");
                  System.exit(1);
        }

        SharedDuraConsumerExample receiveMsg = new SharedDuraConsumerExample();
        receiveMsg.parseArgs(args);
        try {
                // Receive messages from topic
                receiveMsg.run_threads();
        }catch(Exception ex) {
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
         System.out.println("Topic name is " + destName);
         if (args.length == 2){
         	noOfMsgs = (new Integer(args[1])).intValue();
         } else {
                noOfMsgs = 1;
         }

    }

    /**
     * Each instance of the Subscriber class creates a subscriber.
     * It receives messages using receive(timeout).
     * It does not exit till the both subscribers get messages.
    */
    public class Subscriber extends Thread {

        int     subscriberNumber;

        /**
         * Constructor.
         * subscriberNumber based on Subscriber array index.
         *
         * @param num	the index of the Subscriber array
         */
        public Subscriber(int num) {
            subscriberNumber = num + 1;
        }
 
        /**
         * Runs the thread.
         */
        public void run() {
            ConnectionFactory    connectionFactory = null;
	    JMSContext  	 context = null;          
            Topic                topic = null;
            String               selector = null;
            JMSConsumer          msgConsumer = null;
            connectionFactory = 
                    new com.sun.messaging.ConnectionFactory();
            try  {
	    	context = 
                    connectionFactory.createContext();
             	topic = context.createTopic(destName);
		
            	/*
             	* Create durable subscriber with shared subscription name.
             	* Start message delivery.
             	* Wait till all messages have arrived.
             	*/
             	msgConsumer = 
                    context.createSharedDurableConsumer(topic,"durasubid");
                
		context.start();

             	/*
		* Start receiving messages
		* Block until all subscribers receive msgs.
              	*/
		
		while(!doneSignal) {

			TextMessage txtMsg = (TextMessage) msgConsumer.receive(15000);
		
			if((txtMsg == null) && (doneSignal = true)) {
				break;
			}
			
			if(txtMsg != null) {
				System.out.println("SUBSCRIBER " + subscriberNumber
                                       + " : Message received: "
                                       + txtMsg.getText());
                    		msgsReceived++;
			}  else {
				System.out.println("SUBSCRIBER " + subscriberNumber
                                       + " : No message received");
				break;
			}
			if ( msgsReceived == noOfMsgs)
                        {
                        	System.out.println("Received all messages");
                        	doneSignal = true;
                        }
		}
		
           } catch (Exception e) {
          	System.out.println("Exception occurred: " + e.toString());
               	exitcode = 1;
	   } finally {
		if( context != null) {
			context.close();
		}
	   }
        }

     }
    
    /**
     * Creates an array of Subscriber objects and starts their threads.
     * Calls the join method to wait for the threads to die.
     */
    public void run_threads() {
        final       int NUM_SUBSCRIBERS = 2;
        Subscriber  subscriberArray[] = new Subscriber[NUM_SUBSCRIBERS];

        subscriberArray[0] = new Subscriber(0);
        subscriberArray[0].start();        
        subscriberArray[1] = new Subscriber(1);
        subscriberArray[1].start();

        for (int i = 0; i < subscriberArray.length; i++) {
            try {
                subscriberArray[i].join();
            } catch (InterruptedException e) {}
        }
        
    }
}
