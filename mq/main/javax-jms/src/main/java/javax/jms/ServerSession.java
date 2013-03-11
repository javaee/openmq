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

/** A {@code ServerSession} object is an application server object that 
  * is used by a server to associate a thread with a JMS session (optional).
  *
  * <P>A {@code ServerSession} implements two methods:
  *
  * <UL>
  *   <LI>{@code getSession} - returns the {@code ServerSession}'s 
  *       JMS session.
  *   <LI>{@code start} - starts the execution of the 
  *       {@code ServerSession} 
  *       thread and results in the execution of the JMS session's 
  *       {@code run} method.
  * </UL>
  *
  * <P>A {@code ConnectionConsumer} implemented by a JMS provider uses a 
  * {@code ServerSession} to process one or more messages that have 
  * arrived. It does this by getting a {@code ServerSession} from the 
  * {@code ConnectionConsumer}'s {@code ServerSessionPool}; getting 
  * the {@code ServerSession}'s JMS session; loading it with the messages; 
  * and then starting the {@code ServerSession}.
  *
  * <P>In most cases the {@code ServerSession} will register some object 
  * it provides as the {@code ServerSession}'s thread run object. The 
  * {@code ServerSession}'s {@code start} method will call the 
  * thread's {@code start} method, which will start the new thread, and 
  * from it, call the {@code run} method of the 
  * {@code ServerSession}'s run object. This object will do some 
  * housekeeping and then call the {@code Session}'s {@code run} 
  * method. When {@code run} returns, the {@code ServerSession}'s run 
  * object can return the {@code ServerSession} to the 
  * {@code ServerSessionPool}, and the cycle starts again.
  *
  * <P>Note that the JMS API does not architect how the 
  * {@code ConnectionConsumer} loads the {@code Session} with 
  * messages. Since both the {@code ConnectionConsumer} and 
  * {@code Session} are implemented by the same JMS provider, they can 
  * accomplish the load using a private mechanism.
  *
  * @see         javax.jms.ServerSessionPool
  * @see         javax.jms.ConnectionConsumer
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  * 
  */

public interface ServerSession {

    /** Return the {@code ServerSession}'s {@code Session}. This must 
      * be a {@code Session} created by the same {@code Connection} 
      * that will be dispatching messages to it. The provider will assign one or
      * more messages to the {@code Session} 
      * and then call {@code start} on the {@code ServerSession}.
      *
      * @return the server session's session
      *  
      * @exception JMSException if the JMS provider fails to get the associated
      *                         session for this {@code ServerSession} due
      *                         to some internal error.
      **/

    Session
    getSession() throws JMSException;


    /** Cause the {@code Session}'s {@code run} method to be called 
      * to process messages that were just assigned to it.
      *  
      * @exception JMSException if the JMS provider fails to start the server
      *                         session to process messages due to some internal
      *                         error.
      */

    void 
    start() throws JMSException; 
}
