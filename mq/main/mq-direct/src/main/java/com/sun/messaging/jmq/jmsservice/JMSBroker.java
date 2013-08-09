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
 * @(#)JMSBroker.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.jmsservice;

import java.util.Properties;

/**
 *
 */
public interface JMSBroker {
    
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
    public Properties parseArgs(String[] args)
    throws IllegalArgumentException;

    /**
     *  Start the broker. Only one broker can be running in a single JVM.
     *  The call returns as soon as the broker successfully starts.
     *
     *  @param  inProcess   indicates that the broker is running inprocess
     *                      and the shutdown hook and memory management
     *                      code should not be used.
     *
     *  @param  properties  the configuration properties for the broker.
     *
     *  @param  el          An optional class to notify when a broker has
     *                      completed starting or has been shutdown.
     *  @param initOnly      Only initiaize the broker var directory, do
     *                      not actually start the broker
     *  @return The exit code returned by the broker. This is the same value
     *          returned as would be if the broker was running as a standalone
     *          process.<br>
     *          {@code 0} - if it was started successfully<br>.
     *          {@code non-zero} - otherwise
     *
     *  @throws OutOfMemoryError    If the broker can not allocate enough
     *                              memory to continue running.
     *  @throws IllegalStateException   If the broker is already running.  
     *  @throws IllegalArgumentException    If an invalid value for a property
     *                                      was passed in {@code properties}.
     */
    public int start(boolean inProcess, 
            Properties properties, BrokerEventListener el, boolean initOnly)
    throws OutOfMemoryError, IllegalStateException, IllegalArgumentException;
    
    /**
     *  Stop the broker. Only one broker can be running in a single JVM
     *
     *  @param  cleanup {@code false} indicates that the broker does not have
     *                  to clean up; free resources etc. since the it is about
     *                  to exit.<br>
     *                  {@code true} indicates that the broker does have to
     *                  clean up/free resources etc.
     *
     *  @throws IllegalStateException if the broker is already stopped.  
     */
    public void stop(boolean cleanup)
    throws IllegalStateException;

    /**
     * @return true if the broker is shutdown
     */
    public boolean isShutdown();
    
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
