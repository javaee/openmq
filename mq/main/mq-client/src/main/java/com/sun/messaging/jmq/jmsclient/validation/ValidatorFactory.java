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

package com.sun.messaging.jmq.jmsclient.validation;

import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import javax.jms.JMSException;

public class ValidatorFactory {
    
    static final String TOPIC_PROP_NAME_PREFIX = "imq.xml.validate.topic";
    static final String QUEUE_PROP__NAME_PREFIX = "imq.xml.validate.queue";
   
    /**
     * Default constructor is protected on purpose.
     */
    protected ValidatorFactory() {
        
    }
    
    /**
     * new instance of this class.
     * @return a new instance of the factory.
     */
    public static ValidatorFactory newInstance() {
        return new ValidatorFactory();
    }
    
    /**
     * Construct a new instance of the XMLValidator.
     * This is used to validate against DTD defined in the
     * XML document.
     * 
     * @return a new instance of XMLValidator.
     * 
     * @throws javax.jms.JMSException
     */
    public static XMLValidator newValidator() throws JMSException {
        return new XMLValidator();
    }
    
     /**
     * Construct a new instance of validator with the 
     * specified schema language and xsd URI list.
     * 
     * BY default, the xml schema language is used: 
     * "http://www.w3.org/2001/XMLSchema"
     * 
     * @param xsdURIList the xsd used by this validator to
     * validate XML document.
     * 
     * @return a new instance of the xml validator.
     * 
     * @throws javax.jms.JMSException
     */
    public static XMLValidator 
        newValidator(String xsdURIList) throws JMSException {
        
        return new XMLValidator(xsdURIList);
    }
    
    /**
     * Construct a new instance of validator with the 
     * specified schema language and xsd URI list.
     * 
     * @param schemaLang the schema language for this
     * validator.
     * 
     * @param xsdURIList the xsd used by this validator to
     * validate XML document.
     * 
     * @return a new instance of the xml validator.
     * 
     * @throws javax.jms.JMSException
     */
    public static XMLValidator 
        newValidator(String schemaLang, String xsdURIList) throws JMSException {
        
        return new XMLValidator(schemaLang, xsdURIList);
    }
    
    public static Hashtable getTopicValidateTable() {
        return getValidateTable (TOPIC_PROP_NAME_PREFIX);
    }
    
    public static Hashtable getQueueValidateTable() {
        return getValidateTable (QUEUE_PROP__NAME_PREFIX);
    }
    
    /**
     * return topic validation table defined with System properties for
     * the client runtime JVM.
     * 
     * 
     * @return Hashtable -- key=topic name, value=XMLValodator instance
     * for the topic.
     * 
     */
    private static Hashtable getValidateTable(String prefix) {

        Hashtable table = new Hashtable();

        try {

            Properties props = System.getProperties();

            Enumeration enum2 = props.keys();

            while (enum2.hasMoreElements()) {

                String name = (String) enum2.nextElement();
                if (name.startsWith(prefix)) {

                    int fromIndex = prefix.length()+1;
                    int endIndex = name.indexOf('.', fromIndex);

                    String topicName = name.substring(fromIndex, endIndex);
                    String uri = System.getProperty(name);

                    //System.out.println ("destName=" + topicName + ", fromIndex=" + fromIndex + ", endIndex="+endIndex);
                    //System.out.println ("uri=" + uri);
                        
                    XMLValidator validator = null;
                    //http: or file:
                    if (uri.length() > 4) {
                        validator = newValidator(uri);    
                    } else {
                        validator = newValidator();
                    }

                    //add to table
                    table.put(topicName, validator);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return table;

    }
}
 
