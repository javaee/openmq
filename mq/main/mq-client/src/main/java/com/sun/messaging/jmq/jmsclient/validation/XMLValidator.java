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

import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jms.JMSException;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.sun.messaging.jmq.jmsclient.Debug;
import com.sun.messaging.jmq.jmsclient.ExceptionHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is used to validate if an XML document is valid.  
 * 
 * 
 * @author chiaming
 */
public class XMLValidator {
	/**
	 * System property to turn on/off xml schema validation.
	 * If set to true, XML content is validated against the
	 * schema provided or from the content declaration.
	 * 
	 */
	static public final String IS_Validate = "imq.xml.schema.validate";

	/**
	 * System property to set the XSD URI list for this validator 
	 */
	static public final String XSD_URI_LIST = "imq.xml.schema.uri";
	
	//urilist
	private Vector urilist = null;
	
	private String xsdURIList = null;
	
	private Schema schema = null;
	
	private Validator validator = null;
	
	private StreamSource ssarray[] = null;
	
	private boolean reloadXSDOnFailure = false;
	
	//default supported schema language
	private String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	
    private SAXParser saxParser = null;
    
    private EventHandler eventHandler = null;
    
    private boolean debug = Debug.debug;
    
    /**
     * This constructor is used to construct an instance to validate 
     * XML DTD.  When no external schema uri is configured, this
     * is used.
     * 
     * @throws javax.jms.JMSException
     */
	protected XMLValidator () throws JMSException {
        
        try {
            
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            
            saxFactory.setValidating(true);
            
            //parser to be used to validate xml DTD
            saxParser = saxFactory.newSAXParser();
            
            //default handler for the above sax parser
            eventHandler = new EventHandler();
            
        } catch (Exception ex) {
            JMSException jmse = 
				new com.sun.messaging.jms.JMSException(ex.getMessage());
			
			jmse.setLinkedException(ex);

			ExceptionHandler.throwJMSException(jmse);
        }
    }
    
    protected XMLValidator(String xsdURIList) throws JMSException {
        //by default, use XML schema language
        this (XMLConstants.W3C_XML_SCHEMA_NS_URI,  xsdURIList);
	}
    
	/**
	 * 
	 * @param xsdURIList
	 */
	protected XMLValidator(String schemaLang, String xsdURIList)
			throws JMSException {

		try {
			
			//save the xsd uri list string
			this.xsdURIList = xsdURIList;
			
			//put uri list string into urilist vector
			if (xsdURIList == null) {
				throw new NullPointerException ("XSD URI List can not be null"); 
			} else {
	            this.urilist = new Vector();
	            //this.xsdURIList = xsdURIList;
	        }
			
			StringTokenizer tokens = new StringTokenizer (xsdURIList, " ");
			
			while (tokens.hasMoreElements()) {
				String uri = tokens.nextToken();
				
				this.urilist.add(uri);
			}
			
			if (schemaLang != null) {
				this.setSchemaLanguage(schemaLang);
			}

			//this.setXSDURI(xsdURIList);

			this.initSchema();

		} catch (Exception ex) {

			JMSException jmse = 
				new com.sun.messaging.jms.JMSException(ex.getMessage());
			
			jmse.setLinkedException(ex);

			ExceptionHandler.throwJMSException(jmse);
		}
	}
	
	/**
	 * @param xsdURIList URI list separated by " "
	 */
	private synchronized void initStreamSource () {
		
		int size = urilist.size();
		ssarray = new StreamSource[size];
		for (int i=0; i<size; i++) {
			String uri = (String) urilist.elementAt(i);
			ssarray[i] = new StreamSource (uri);
		}	
	}
	
	private synchronized void setSchemaLanguage (String schemaLanguage) {
		this.schemaLanguage = schemaLanguage;
	}
	
	private synchronized void
	initSchema () throws SAXException {
		
		//get the stream source array
		this.initStreamSource();
		
		// create a SchemaFactory capable of understanding 
		//the specified schemas
	    SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
	    schema = factory.newSchema(ssarray);
	    
	    validator = schema.newValidator();
        
        ErrorHandler errorHandler = new ErrorHandler();
        validator.setErrorHandler(errorHandler);
	}
	
    /**
     * validate an XML document.
     * @param xml
     * @throws javax.jms.JMSException
     */
	public synchronized void validate(String xml) throws JMSException {

		try {
			
			if (validator != null) {
				
				if ( this.reloadXSDOnFailure ) {
					this.reloadXMLSchemaOnFailure (xml);
				} else {
					this.doValidateXSD(xml);
				}
			
            } else if (this.saxParser != null) {
                
                StringReader reader = new StringReader(xml);
				//use SAX input source
				org.xml.sax.InputSource isource = new org.xml.sax.InputSource (reader);
                
                this.saxParser.parse(isource, this.eventHandler);
                
                this.saxParser.reset();
                //System.out.println ("DTD validated by internal SAXParser.");
            }
            
		} catch (Exception ex) {
			JMSException jmse = new com.sun.messaging.jms.JMSException(ex
					.getMessage());

			jmse.setLinkedException(ex);

			ExceptionHandler.throwJMSException(jmse);
		} 

	}
	
	private synchronized void reloadXMLSchemaOnFailure (String xml) throws SAXException, IOException {
		
		if (debug) {
			Debug.println("*** In reloadXMLSchemaOnFailure, validating xml ... ");
		}
		
		try {
			this.doValidateXSD(xml);
		} catch (Exception e) {
			
			if (debug) {
				Debug.println("*** reloading XSD from " + this.xsdURIList );
			}
			
			//reload xsd
			this.initSchema();
			
			if (debug) {
				Debug.println("*** re-validating XML ..." + xml);
			}
			
			//re-validate
			this.doValidateXSD(xml);
		}
	}
	
	/**
	 * validate xml with validator.
	 * @param xml
	 * @throws Exception
	 */
	private synchronized void doValidateXSD (String xml) throws SAXException, IOException {
		
		StringReader reader = new StringReader(xml);
		//use SAX input source
		org.xml.sax.InputSource isource = new org.xml.sax.InputSource (reader);
		SAXSource saxSource = new SAXSource (isource);
		
		validator.validate(saxSource);
	}
    
    public synchronized void 
        validateURI (String xmluri) throws JMSException {
        
        try {
			
			if (validator != null) {
                
                StreamResult result = new StreamResult (System.out);
                StreamSource source = new StreamSource (xmluri);
        
                //StreamSource works for JDK 1.6 only.
				validator.validate(source, result);
			} else if (this.saxParser != null) {
                
                System.out.println ("*** use saxParser .... isValidating: " + this.saxParser.isValidating());
                this.saxParser.parse (xmluri, this.eventHandler);
            }
            
		} catch (Exception ex) {
			JMSException jmse = new com.sun.messaging.jms.JMSException(ex
					.getMessage());

			jmse.setLinkedException(ex);

			ExceptionHandler.throwJMSException(jmse);
		}
        
    }
    
    public String getURIList() {
    	return this.xsdURIList;
    }
    
    public synchronized void setReloadOnFailure (boolean doReload) {
    	this.reloadXSDOnFailure = doReload;
    }
    
    public boolean getReloadOnFailure() {
    	return this.reloadXSDOnFailure;
    }
    
    public static void main (String[] args) throws Exception {
        
        if (args.length == 2) {
            //System.out.println("Usage: XMLValidator schemaURIList xmlInstanceURI");
            //System.exit(1);
            
            XMLValidator xmlValidator = new XMLValidator(args[0]);
            xmlValidator.validateURI (args[1]);
            
            
             System.out.println("xml is validated: " + args[1]);
        } else {
        
            XMLValidator xmlValidator = new XMLValidator();
            xmlValidator.validateURI (args[0]);
            System.out.println("xml is validated without schema: " + args[0]);
        }
   
    }
	
}
