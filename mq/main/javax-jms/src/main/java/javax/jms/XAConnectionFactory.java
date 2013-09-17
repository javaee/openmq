/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package javax.jms;

/** The {@code XAConnectionFactory} interface is a base interface for the
  * {@code XAQueueConnectionFactory} and 
  * {@code XATopicConnectionFactory} interfaces.
  *
  * <P>Some application servers provide support for grouping JTA capable 
  * resource use into a distributed transaction (optional). To include JMS API transactions 
  * in a JTA transaction, an application server requires a JTA aware JMS
  * provider. A JMS provider exposes its JTA support using an
  * {@code XAConnectionFactory} object, which an application server uses 
  * to create {@code XAConnection} objects.
  *
  * <P>{@code XAConnectionFactory} objects are JMS administered objects, 
  * just like {@code ConnectionFactory} objects. It is expected that 
  * application servers will find them using the Java Naming and Directory
  * Interface (JNDI) API.
  *
  *<P>The {@code XAConnectionFactory} interface is optional. JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than use these XA
  * interfaces directly. 
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  * 
  */
public interface XAConnectionFactory {
    
     /** Creates an {@code XAConnection} with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the {@code Connection.start} method
      * is explicitly called.
      *
      * @return a newly created {@code XAConnection}
      *
      * @exception JMSException if the JMS provider fails to create an XA  
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      * 
      * @since JMS 1.1 
      * 
      */ 

    XAConnection
    createXAConnection() throws JMSException;


    /** Creates an {@code XAConnection} with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the {@code Connection.start} method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created {@code XAConnection}
      *
      * @exception JMSException if the JMS provider fails to create an XA  
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      *
      * @since JMS 1.1 
      * 
      */ 

    XAConnection
    createXAConnection(String userName, String password) 
					     throws JMSException;
    
	/**
	 * Creates a {@code XAJMSContext} with the default user identity
	 * <p>
     * A connection and session are created for use by the new {@code XAJMSContext}. 
     * The connection is created in stopped mode but will be automatically started
     * when a {@code JMSConsumer} is created.
	 * 
	 * @return a newly created {@code XAJMSContext}
	 * 
	 * @exception JMSRuntimeException
	 *                if the JMS provider fails to create the {@code XAJMSContext} due
	 *                to some internal error.
	 * @exception JMSSecurityRuntimeException
	 *                if client authentication fails due to an invalid user name
	 *                or password.
	 * @since JMS 2.0
	 * 
	 */
	XAJMSContext createXAContext();
   
    /** 
     * Creates a JMSContext with the specified user identity
	 * <p>
     * A connection and session are created for use by the new {@code XAJMSContext}. 
     * The connection is created in stopped mode but will be automatically started
     * when a {@code JMSConsumer} is created.
     * 
     * @param userName the caller's user name
     * @param password the caller's password
     *  
     * @return a newly created JMSContext
     *
     * @exception JMSRuntimeException if the JMS provider fails to create the 
     *                         JMSContext due to some internal error.
     * @exception JMSSecurityRuntimeException  if client authentication fails due to 
     *                         an invalid user name or password.
     * @since JMS 2.0 
     * 
     */
    XAJMSContext createXAContext(String userName, String password);    
   
}
