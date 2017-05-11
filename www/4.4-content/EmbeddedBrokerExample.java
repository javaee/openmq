/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2009 Sun Microsystems, Inc. All rights reserved. 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License ("CDDL") (collectively, the "License").  You may
 * not use this file except in compliance with the License.  You can obtain
 * a copy of the License at https://glassfish.java.net/public/CDDL+GPL.html
 * or mq/legal/LICENSE.txt.  See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at mq/legal/LICENSE.txt.  Sun designates
 * this particular file as subject to the "Classpath" exception as provided by
 * Sun in the GPL Version 2 section of the License file that accompanied this
 * code.  If applicable, add the following below the License Header, with the
 * fields enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or  to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright holder. 
 */

package test.direct;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.runtime.BrokerInstance;
import com.sun.messaging.jmq.jmsclient.runtime.ClientRuntime;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;
import com.sun.messaging.jmq.jmsservice.BrokerEventListener;

public class EmbeddedBrokerExample {
				
	public void run(String[] args) throws Exception{
		
		// obtain the ClientRuntime singleton object
		ClientRuntime clientRuntime = ClientRuntime.getRuntime();
		
		// create the embedded broker instance
		BrokerInstance brokerInstance = clientRuntime.createBrokerInstance();
		
		// convert the specified broker arguments into Properties
		// this is a utility function: it doesn't change the broker
		Properties props = brokerInstance.parseArgs(args);
		
		// initialise the broker instance 
		// using the specified properties
		// and a BrokerEventListener
		BrokerEventListener listener = new ExampleBrokerEventListener();
		brokerInstance.init(props, listener);
		
		// now start the embedded broker		
		brokerInstance.start();
				
		System.out.println ("Embedded broker started");
		
		// now create a direct connection to the embedded broker 
		// this is identical to a normal TCP connection except that a special URL is used
		com.sun.messaging.ConnectionFactory qcf = new com.sun.messaging.ConnectionFactory();
		qcf.setProperty(ConnectionConfiguration.imqAddressList, "mq://localhost/direct");
		
		Connection connection = qcf.createConnection();
		System.out.println ("Created direct connection to embedded broker");

		// now create a session and a producer and consumer in the normal way 
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("exampleQueue");
		MessageConsumer consumer = session.createConsumer(queue);
		MessageProducer producer = session.createProducer(queue);

		// send a message to the queue in the normal way
		TextMessage textMessage = session.createTextMessage("This is a message");
		producer.send(textMessage);
		
		// receive a message from the queue in the normal way
		connection.start();
		Message receivedMessage = consumer.receive(1000);
		System.out.println ("Received message "+((TextMessage)receivedMessage).getText());
		
		// close the client connection
		connection.close();
		
		// stop the embedded broker		
		brokerInstance.stop();
		
		// shutdown the embedded broker			
		brokerInstance.shutdown();

	}
	
	public static void main(String[] args) throws Exception {
		
		EmbeddedBrokerExample ebe = new EmbeddedBrokerExample();
		ebe.run(args);
 
	}
	
	class ExampleBrokerEventListener implements BrokerEventListener {

		public void brokerEvent(BrokerEvent brokerEvent) {
	    	System.out.println ("Received broker event:"+brokerEvent);
		}

		public boolean exitRequested(BrokerEvent event, Throwable thr) {
	    	System.out.println ("Broker is requesting a shutdown because of:"+event+" with "+thr);
	    	
	    	// return true to allow the broker to shutdown
	    	return true;
		}
		
	}

}
