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

import java.util.Enumeration;
import java.util.Properties;
import javax.jms.*;

/**
 * The VMMetrics example is a JMS application that monitors the
 * Java VM used by the Oracle GlassFish(tm) Server Message Queue broker. It does so by 
 * subscribing to a topic named 'mq.metrics.jvm'. The messages that arrive 
 * contain Java VM information such as:
 *  - amount of free memory
 *  - amount of maximum memory
 *  - total amount of memory
 *
 * By default VMMetrics will connect to the broker running on localhost:7676.
 * You can use -DimqAddressList attribute to change the host, port and 
 * transport:
 *
 *	java -DimqAddressList=mq://<host>:<port>/jms VMMetrics
 */
public class VMMetrics implements MessageListener  {
    ConnectionFactory        metricConnectionFactory;
    Connection               metricConnection;
    Session                  metricSession;
    MessageConsumer          metricConsumer;
    Topic                    metricTopic;
    MetricsPrinter           mp;
    int                      rowsPrinted = 0;
  
    public static void main(String args[])  {
	VMMetrics bm = new VMMetrics();
        bm.initPrinter();
        bm.initJMS();
        bm.subscribeToMetric();
    }

    /*
     * Initializes the class that does the printing, MetricsPrinter.
     * See the MetricsPrinter class for details.
     */
    private void initPrinter() {
	String oneRow[] = new String[ 3 ];
	int i = 0;

	mp = new MetricsPrinter(3, 2, "-");

	i = 0;
	oneRow[i++] = "Free Memory";
	oneRow[i++] = "Max Memory";
	oneRow[i++] = "Total Memory";
	mp.addTitle(oneRow);
    }

    /** 
     * Create the Connection and Session etc.
     */
    public void initJMS() {
        try {
            metricConnectionFactory = new com.sun.messaging.ConnectionFactory();
            metricConnection = metricConnectionFactory.createConnection();
            metricConnection.start();

            //  creating Session
            //	Transaction Mode: None
            //	Acknowledge Mode: Automatic
            metricSession = metricConnection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
        } catch(Exception e) {
            System.err.println("Cannot create metric connection or session: "
			+ e.getMessage());
            e.printStackTrace();
	    System.exit(1);
        }
    }
  
    public void subscribeToMetric() {
        try {
            metricTopic = metricSession.createTopic("mq.metrics.jvm");

            metricConsumer = metricSession.createConsumer(metricTopic);
            metricConsumer.setMessageListener(this);
        } catch(JMSException e) {
            System.err.println("Cannot subscribe to metric topic: "
			+ e.getMessage());
            e.printStackTrace();
	    System.exit(1);
        }
    }

    /*
     * When a metric message arrives
     *	- verify it's type
     *	- extract it's fields
     *  - print one row of output
     */
    public void onMessage(Message m)  {
	try  {
	    MapMessage mapMsg = (MapMessage)m;
	    String type = mapMsg.getStringProperty("type");

	    if (type.equals("mq.metrics.jvm"))  {
	        String oneRow[] = new String[ 3 ];
		int i = 0;

	        /*
	         * Extract broke metrics
	         */
		oneRow[i++] = Long.toString(mapMsg.getLong("freeMemory"));
		oneRow[i++] = Long.toString(mapMsg.getLong("maxMemory"));
		oneRow[i++] = Long.toString(mapMsg.getLong("totalMemory"));

		mp.add(oneRow);

		if ((rowsPrinted % 20) == 0)  {
		    mp.print();
		} else  {
		    mp.print(false);
		}

		rowsPrinted++;

		mp.clear();
	    } else  {
	        System.err.println("Msg received: not vm metric type");
	    }
	} catch (Exception e)  {
	    System.err.println("onMessage: Exception caught: " + e);
	}
    }
}
