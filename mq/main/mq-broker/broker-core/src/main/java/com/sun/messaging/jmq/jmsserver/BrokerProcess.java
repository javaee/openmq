/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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

/*
 * @(#)BrokerProcess.java	1.16 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver;

import java.util.*;
import com.sun.messaging.jmq.jmsserver.service.Service;
import com.sun.messaging.jmq.jmsserver.service.ServiceManager;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQService;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQDirectService;
import com.sun.messaging.jmq.jmsservice.JMSBroker;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.BrokerEventListener;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;

/**
 * Wrapper used to start the broker. It wraps a singleton class
 * (only one broker can be running in any process).<P>
 *
 * <u>Example</u><P>
 * <code><PRE>
 *      BrokerProcess bp = BrokerProcess.getBrokerProcess();
 *      try {
 *      
 *          Properties ht = bp.convertArgs(args);
 *          int exitcode = bp.start(true, ht, null);
 *          System.out.println("Broker exited with " + exitcode);
 *
 *      } catch (IllegalArgumentException ex) {
 *          System.err.println("Bad Argument " + ex.getMessage());
 *          System.out.println(BrokerProcess.usage());
 *      }
 * </PRE></code>
 */
public class BrokerProcess implements JMSBroker
{
    private Broker broker = null;

    /**
     * Constructor
     */
    public BrokerProcess() {
        broker = Broker.getBroker();
    }

    /**
     * Change command line args into a hashtable format
     *<P>
     * Additional arguments are:
     *    <UL>
     *       <LI> -varhome</LI>
     *       <LI> -imqhome</LI>
     *    </UL>
     *
     * @param args arguments in broker format
     */
    public Properties convertArgs(String[] args)
        throws IllegalArgumentException
    {
        Properties props = new Properties();

        // first look for var home and the like
        for (int i =0; i < args.length; i ++) {
            String arg = args[i];
            if (arg.equals("-varhome")) {
                props.setProperty("imq.varhome",
                        args[i+1]);
                i ++;
            } else if (arg.equals("-imqhome")) {
                props.setProperty("imq.home",
                        args[i+1]);
                i ++;
            } else if (arg.equals("-libhome")) {
                props.setProperty("imq.libhome",
                        args[i+1]);
                i ++;
            }
        }
        Globals.pathinit(props);
        return broker.convertArgs(args);
    }

    public Properties parseArgs(String[] args)
        throws IllegalArgumentException
    {
        return (convertArgs(args));
    }

    /**
     * Checks the state of the Broker
     *
     * @return the state of the broker
     */
    public boolean isRunning() {
        return true;
    }

    /**
     * Start the broker (only one broker can be running in a given
     * vm).<p>This call returns as soon as the broker sucessfully starts.
     * @param inProcess - indicates that the broker is running inprocess
     *                    and the shutdown hook and memory management
     *                    code should not be used.
     * @param properties - configuration setttings for the broker
     *
     * @param bn - optional class to notify when a broker has completed
     *             starting or has been shutdown.
     *
     * @return the exit code what would be returned by the broker if it
     *       was running as a standalone process. (or 0 if it sucessfully
     *       started).
     *
     * @throws OutOfMemoryError - if the broker can not allocate enough 
     *          memory to continue running
     * @throws IllegalStateException - the broker is already running.  
     * @throws IllegalArgumentException - an invalid value for a property
     *                was passed on the command line
     */
    public int start(boolean inProcess, Properties properties, BrokerEventListener bel, boolean initOnly)
        throws OutOfMemoryError, IllegalStateException, IllegalArgumentException
    {

        return broker.start(inProcess, properties, bel, initOnly);
    }

    /**
     * Stop the broker (only one broker can be running in a given
     * vm).<p>
     * @param cleanup - if false, the code does not need to worry about freeing
     *                  unused resources. (broker is about to exit)
     * @throws IllegalStateException - the broker is already stopped.  
     */
    public void stop(boolean cleanup)
        throws IllegalStateException
    {
        broker.destroyBroker(cleanup);
        broker = null;
    }

    public boolean isShutdown() {
        return (broker == null || broker.broker == null);
    }
    
	/**
	 * Specify a message that will be written to the broker logfile  
	 * when the broker starts as an INFO message. This is typically used to log the
	 * broker properties configured on an embedded broker, and so is logged immediately
	 * after its arguments are logged. 
	 * 
	 * This can be called multiple times to specify
	 * multiple messages, each of which will be logged on a separate line.
	 * 
	 * @param embeddedBrokerStartupMessage
	 */    
	public void addEmbeddedBrokerStartupMessage(String message) {
		broker.addEmbeddedBrokerStartupMessage(message);
	}

}


