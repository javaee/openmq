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
 * @(#)JMSObjFactory.java	1.25 06/28/07
 */ 

package com.sun.messaging.jmq.admin.util;

import java.util.Enumeration;
import java.util.Properties;
import javax.jms.JMSException;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.InvalidPropertyException;
import com.sun.messaging.ReadOnlyPropertyException;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminResources;

/**
 * This class creates a JMS object from a Properties object
 */

public class JMSObjFactory {

    /**
     * Create a JMS Topic.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS Topic
     * is created.
     * @return the com.sun.messaging.Topic
     */ 
    public static Object createTopic(Properties objProps) 
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.Topic();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS Queue.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS Queue
     * is created.
     * @return the com.sun.messaging.Queue
     */ 
    public static Object createQueue(Properties objProps)  
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.Queue();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS Topic Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * Topic Connection Factory is created.
     * @return the com.sun.messaging.TopicConnectionFactory
     */ 
    public static Object createTopicConnectionFactory(Properties objProps)
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.TopicConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * Topic Connection Factory is created.
     * @return the com.sun.messaging.ConnectionFactory
     */ 
    public static Object createConnectionFactory(Properties objProps)
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.ConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS XA Topic Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * XA Topic Connection Factory is created.
     * @return the com.sun.messaging.XATopicConnectionFactory
     */ 
    public static Object createXATopicConnectionFactory(Properties objProps)
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.XATopicConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS Queue Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * Queue Connection Factory is created.
     * @return the com.sun.messaging.QueueConnectionFactory
     */ 
    public static Object createQueueConnectionFactory(Properties objProps) 
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.QueueConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS XA Queue Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * XA Queue Connection Factory is created.
     * @return the com.sun.messaging.XAQueueConnectionFactory
     */ 
    public static Object createXAQueueConnectionFactory(Properties objProps) 
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.XAQueueConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    /**
     * Create a JMS XA Connection Factory.
     *
     * <P>No verification of valid param values are needed at this point
     * because the assumption is that valid values were checked before
     * this was called.
     *
     * @param props  the set of Properties to be set when the JMS 
     * XA Queue Connection Factory is created.
     * @return the com.sun.messaging.XAConnectionFactory
     */ 
    public static Object createXAConnectionFactory(Properties objProps) 
				throws JMSException {

	AdministeredObject obj = null;

	obj = (AdministeredObject)new com.sun.messaging.XAConnectionFactory();

	setProperties(obj, objProps);

	return (obj);
    }

    public static Object updateTopic(Object oldObj, Properties objProps,
				     String readOnlyValue)  
						throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.Topic();
	/*
	 * Copy the properties from old object to new object.
	 * Then set the new specified props into the new object.
	 * XXX REVISIT - What if oldObj is not instance of AdministeredObject??
	 */
	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
					readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateQueue(Object oldObj, Properties objProps,
				     String readOnlyValue)  
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.Queue();
	/*
	 * Copy the properties from old object to new object.
	 * Then set the new specified props into the new object.
	 * XXX REVISIT - What if oldObj is not instance of AdministeredObject??
	 */
	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateTopicConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.TopicConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateXATopicConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.XATopicConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateQueueConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.QueueConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.ConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateXAQueueConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.XAQueueConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    public static Object updateXAConnectionFactory(Object oldObj, 
			Properties objProps, String readOnlyValue) 
				throws JMSException {

	AdministeredObject newObj = null;
	String value;

	newObj = (AdministeredObject)new com.sun.messaging.XAConnectionFactory();

	if (oldObj instanceof AdministeredObject) {
	    updateAdministeredObject((AdministeredObject)oldObj, newObj, objProps,
				     readOnlyValue);
	}

	return (newObj);
    }

    /*
     * Set the properties on this object.
     */
    private static void setProperties(AdministeredObject obj, 
				Properties objProps) 
				throws JMSException {
	/*
	 * Set the specified properties on the new object.
	 */
	for (Enumeration e = objProps.propertyNames(); e.hasMoreElements(); ) {

	    String propName = (String)e.nextElement();
	    String value  = objProps.getProperty(propName);
	    if (value != null) {
	        try {
		    obj.setProperty(propName, value);

	        } catch (JMSException je) {
		    throw je;
 	        }
	    }
	}
    }

    /*
     *  Update a Read-Only Object:  -r true  => readOnly
     *				    -r false => RW				
     *				    no -r    => readOnly
     *
     *  Update a Read-Write Object:  -r true  => readOnly
     *				     -r false => RW				
     *				     no -r    => readWrite
     */
    private static void updateAdministeredObject(AdministeredObject oldObj,
		AdministeredObject newObj, Properties objProps,
		String readOnlyValue) throws JMSException {
 	/*
	 * Get the properties from the old object and 
	 * set them into the new object.
	 */
	 Properties oldProps = ((AdministeredObject)oldObj).getConfiguration();
	 setProperties((AdministeredObject)newObj, oldProps);

        /*
	 * Now set the new, specified props into the new object.
	 */
        setProperties(newObj, objProps);

	/*
	 * Set the read-only flag on new Object, if necessary.
	 */ 
	if (oldObj.isReadOnly() && readOnlyValue == null) {
	   newObj.setReadOnly();
	} else if (readOnlyValue != null &&
		   readOnlyValue.equalsIgnoreCase(Boolean.TRUE.toString())) {
	   newObj.setReadOnly();
	} else {
	}

    }

    /*
     * Create object with -r true  => ReadOnly
     * 		          -r false => ReadWrite
     * 		          no -r    => ReadWrite
     */
    public static void doReadOnlyForAdd(Object obj, String value)  {
	
	if (value != null && 
	    value.equalsIgnoreCase(Boolean.TRUE.toString())) {
	    ((AdministeredObject)obj).setReadOnly();
        } else {
	}
    }
}
