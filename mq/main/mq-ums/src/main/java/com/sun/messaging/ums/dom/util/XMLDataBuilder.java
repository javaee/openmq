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

package com.sun.messaging.ums.dom.util;

import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * This is a utility class to build XML document as a UMS response message.
 * 
 */
public class XMLDataBuilder {
    
    public static final String UMSNS = "https://mq.java.net/ums";
    
    private static final String UMS_ENVELOPE = "ums";
    
    /**
     * Get a new instance of UMS xml document.
     * 
     * @return a new instance of UMS document.
     */
    public static Document newUMSDocument () {
        Document doc = MyInstance.parser.newDocument();
        
        Element root = doc.createElementNS (UMSNS, UMS_ENVELOPE);
	doc.appendChild(root);
        
        return doc;
    }
    
    /**
     * Get the root element of the UMS XML document.
     * 
     * @param doc The document in which to get the root element.
     * @return The root element of the document.
     */
    public static Element getRootElement (Document doc) {
        Element element = doc.getDocumentElement();
        
        return element;
    }
    
    /**
     * Add the specified child element to the parent element.
     * 
     * @param parent The parent element.
     * @param child The child element.
     * @return The child element.
     */
    public static Node addChildElement (Element parent, Element child) {
        return parent.appendChild(child);
    }
    
    /**
     * Create a new ums xml element for the specified document.
     * 
     * @param doc the doc from which the element is created.
     * @param elementName the name of the xml element.
     * @return the created element.
     */
    public static Element createUMSElement (Document doc, String elementName) {
        
        Element element = doc.createElementNS (UMSNS, elementName);
        
        return element;
    }
    
    /**
     * Set the text value to the specified ums xml element.
     * 
     * @param doc the document associated with the element.
     * @param element the element in which the text value is set to.
     * @param value the value to set to the xml element.
     */
    public static void setElementValue (Document doc, Element element, String value) {
        
        Node node = doc.createTextNode(value);
        
        element.appendChild(node);
    }
    
    /**
     * Set the specified attribute name/value to the element.
     * 
     * @param element
     * @param attrName
     * @param attrValue
     */
    public static void setElementAttribute (Element element, String attrName, String attrValue) {
       
        element.setAttribute(attrName, attrValue);
    }
    
    /**
     * Transform the specified xml document to a string.
     * 
     * @param doc
     * @return
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     */
    public static String domToString (Document doc) throws TransformerConfigurationException, TransformerException {
        
        doc.normalizeDocument();
        DOMSource domSource = new DOMSource (doc);
		
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	StreamResult sr = new StreamResult (baos);

	Transformer transformer = MyInstance.transformerFactory.newTransformer();
        
	transformer.transform(domSource, sr);
		
	String xml = baos.toString();
		
	return xml;
    }
    
    
    /**
     * my private singleton objects.
     */
    private static class MyInstance {
        
        private final static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        private static DocumentBuilder parser = null;
        private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
        //private static Transformer transformer = null;
        
        static {
            
            try {
                parser = factory.newDocumentBuilder();
                //transformer = transformerFactory.newTransformer();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }  
    }
    
    /**
     * Example API usage for this class.
     * 
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {

        //create a new instance of ums xml document.
        Document doc = XMLDataBuilder.newUMSDocument();
        
        //get the root element
        Element root = XMLDataBuilder.getRootElement(doc);
        
        //create the first child element
        Element firstChild = XMLDataBuilder.createUMSElement(doc, "firstChild");
        
        //set text value to the first child
        XMLDataBuilder.setElementValue(doc, firstChild, String.valueOf(System.currentTimeMillis()));
        
        //set attribute to the first child
        XMLDataBuilder.setElementAttribute(firstChild, "attr1", "value1");
        
        //add the first child to the root element
        XMLDataBuilder.addChildElement(root, firstChild);
        
        //create second child element
        Element secondChild = XMLDataBuilder.createUMSElement(doc, "secondChild");
        
        //set element text value
        XMLDataBuilder.setElementValue(doc, secondChild, String.valueOf(System.currentTimeMillis()));
        
        //set attribute to the second child
        XMLDataBuilder.setElementAttribute(secondChild, "attr2", "value2");
        
        //add second child to the root element.
        XMLDataBuilder.addChildElement(root, secondChild);
        
        //transform xml document to a string
        String xml = XMLDataBuilder.domToString(doc);
        
        //print the string
        System.out.println("xml=" + xml);

    }
}
