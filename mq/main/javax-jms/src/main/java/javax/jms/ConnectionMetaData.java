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

import java.util.Enumeration;

/** A {@code ConnectionMetaData} object provides information describing the 
  * {@code Connection} object.
  *
  * @version JMS 2.0
  * @since JMS 1.0
  */

public interface ConnectionMetaData {

    /** Gets the JMS API version.
      *
      * @return the JMS API version
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    String 
    getJMSVersion() throws JMSException;


    /** Gets the JMS major version number.
      *  
      * @return the JMS API major version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int 
    getJMSMajorVersion() throws JMSException; 
 

    /** Gets the JMS minor version number.
      *  
      * @return the JMS API minor version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int  
    getJMSMinorVersion() throws JMSException;


    /** Gets the JMS provider name.
      *
      * @return the JMS provider name
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */ 

    String 
    getJMSProviderName() throws JMSException;


    /** Gets the JMS provider version.
      *
      * @return the JMS provider version
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */ 

    String 
    getProviderVersion() throws JMSException;


    /** Gets the JMS provider major version number.
      *  
      * @return the JMS provider major version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int
    getProviderMajorVersion() throws JMSException; 

 
    /** Gets the JMS provider minor version number.
      *  
      * @return the JMS provider minor version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int  
    getProviderMinorVersion() throws JMSException;

 
    /** Gets an enumeration of the JMSX property names.
      *  
      * @return an Enumeration of JMSX property names
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    Enumeration
    getJMSXPropertyNames() throws JMSException;
}
