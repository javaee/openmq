/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsclient.runtime;

import java.util.Properties;
import com.sun.messaging.jmq.jmsservice.BrokerEventListener;
import com.sun.messaging.jmq.jmsservice.JMSService;

public interface BrokerInstance {
	
	/**
     *  Parse broker command line and convert the args into a hashtable format.<p>
     *
     *  Additional arguments are:
     *  <UL>
     *      <LI> -varhome: The location of the VAR directory to use</LI>
     *      <LI> -imqhome: The location of the base IMQ directory</LI>
     *  </UL>
     *
     *  @param args The broker arguments in broker command line format.
     *
     *  @return The resulting Properties that represent the command line
     *          parameters passed in.
     *
     *  @throws IllegalArguementException   If args contain any invalid option.
     */
    public Properties parseArgs(String[] args) throws IllegalArgumentException;

	
	/**
	 * Initialize broker with properties specified in the properties.
	 * 
	 * The props parameter is usually obtained from parseArgs() method.
	 * 
	 * This must be called before start/stop/shutdown
	 * 
	 * @param props  the properties required to init broker.  Obtain required info from broker/Linda.
	 * 
	 * @param evlistener used to listen to broker life cycle events.
	 */
	public void init (Properties props, BrokerEventListener evlistener);
	
	/**
	 * start the broker instance.  
	 */
	public void start();
	
	/**
	 * stop the broker instance
	 */
	public void stop();
	
	/**
	 * shutdown the broker instance.
	 */
	public void shutdown();
	
	/**
	 * Get broker init properties
	 * 
	 * @return
	 */
	public Properties getProperties();
	
	/**
	 * Get the broker event listener.
	 * @return
	 */
	public BrokerEventListener getBrokerEventListener();
	
	/**
	 * check if broker is running
	 * @return
	 */
	public boolean isBrokerRunning();
	
	/**
	 * check if broker instance implements direct mode connection.
	 * 
	 * @return
	 */
	public boolean isDirectMode();

	/**
	 * Return a JMSService that can be used to create legacy RADirect
	 * connections to this broker
	 * 
	 * @return
	 */
	public JMSService getJMSService();


	/**
	 * Specify a message that will be written to the broker logfile  
	 * when the broker starts as an INFO message. This is typically used to log the
	 * broker properties configured on an embedded broker, and so is logged immediately
	 * after its arguments are logged. However this method can be used for other
	 * messages which need to be logged by an embedded broker when it starts.
	 * 
	 * This can be called multiple times to specify
	 * multiple messages, each of which will be logged on a separate line.
	 * 
	 * @param embeddedBrokerStartupMessage
	 */
	public void addEmbeddedBrokerStartupMessage(String embeddedBrokerStartupMessage);
}
