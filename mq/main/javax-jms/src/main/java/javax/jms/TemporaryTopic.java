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

/** A {@code TemporaryTopic} object is a unique {@code Topic} object 
  * created for the duration of a {@code Connection}. It is a 
  * system-defined topic that can be consumed only by the 
  * {@code Connection} that created it.
  *
  *<P>A {@code TemporaryTopic} object can be created either at the 
  * {@code Session} or {@code TopicSession} level. Creating it at the
  * {@code Session} level allows the {@code TemporaryTopic} to participate
  * in the same transaction with objects from the PTP domain. 
  * If a {@code TemporaryTopic} is  created at the 
  * {@code TopicSession}, it will only
  * be able participate in transactions with objects from the Pub/Sub domain.
  *
  * @see Session#createTemporaryTopic()
  * @see TopicSession#createTemporaryTopic()
  * 
  * @version JMS 2.0
  * @since JMS 1.0
  * 
  */

public interface TemporaryTopic extends Topic {

    /** Deletes this temporary topic. If there are existing subscribers
      * still using it, a {@code JMSException} will be thrown.
      *  
      * @exception JMSException if the JMS provider fails to delete the
      *                         temporary topic due to some internal error.
      */

    void 
    delete() throws JMSException; 
}
