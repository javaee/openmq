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


/** A {@code Queue} object encapsulates a provider-specific queue name. 
  * It is the way a client specifies the identity of a queue to JMS API methods.
  * For those methods that use a {@code Destination} as a parameter, a 
  * {@code Queue} object used as an argument. For example, a queue can
  * be used  to create a {@code MessageConsumer} and a 
  * {@code MessageProducer}  by calling:
  *<UL>
  *<LI> {@code Session.CreateConsumer(Destination destination)}
  *<LI> {@code Session.CreateProducer(Destination destination)}
  *
  *</UL>
  *
  * <P>The actual length of time messages are held by a queue and the 
  * consequences of resource overflow are not defined by the JMS API.
  *
  * @see Session#createConsumer(Destination)
  * @see Session#createProducer(Destination)
  * @see Session#createQueue(String)
  * @see QueueSession#createQueue(String)
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  *
  */
 
public interface Queue extends Destination { 

    /** Gets the name of this queue.
      *  
      * <P>Clients that depend upon the name are not portable.
      *  
      * @return the queue name
      *  
      * @exception JMSException if the JMS provider implementation of 
      *                         {@code Queue} fails to return the queue
      *                         name due to some internal
      *                         error.
      */ 
 
    String
    getQueueName() throws JMSException;  


    /** Returns a string representation of this object.
      *
      * @return the provider-specific identity values for this queue
      */
 
    String
    toString();
}
