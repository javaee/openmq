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
 * @(#)Debug.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.util.Properties;
import java.util.Enumeration;
import java.lang.reflect.Field;

/**
 * Debug
 */
public class Debug {

    public final static String debugFieldName = "DEBUG";

    /**
     * Set the DEBUG flag on the specified class. The DEBUG field
     * is assumed to be declared:
     * <pre>
     * public static boolean DEBUG;
     * </pre>
     * @param className	Fully qualified classname to set DEBUG on
     * @param debug	Value to set DEBUG to
     *
     *
     * @throws ClassNotFoundException if class is not found
     * @throws NoSuchFieldExcetpion if DEBUG field is not found in class
     */
    public static void setDebug (String className, boolean debug) 
	throws ClassNotFoundException, NoSuchFieldException,
	       IllegalArgumentException, IllegalAccessException {

	Class cl = Class.forName(className);
    Field[] fields = cl.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
        if (fields[i].getName().equals(debugFieldName)) {
            fields[i].setAccessible(true);
            fields[i].setBoolean(null, debug);
            return;
        }
    }
    throw new NoSuchFieldException(debugFieldName);

    }

    /**
     * Set the DEBUG flag on the classes specified by values in a
     * a Properties object. The Properties object should contain
     * a series of properties of the format:
     * <pre>
     * <prefix>.<classname>=true|false
     * </pre>
     * This method will set the DEBUG flag on <classname> to the specified
     * value. For example if "jmq.debug." is the prefix then
     * <pre>
     * jmq.debug.com.sun.messaging.jmq.jmsserver.data.AcknowledgeList=true
     * </pre>
     * Will set
     * com.sun.messaging.jmq.jmsserver.data.AcknowledgeList.DEBUG 
     * to true.
     * <p>
     * If an error occurs when processing the properties further processing
     * stops and the appropriate exception is thrown.
     *
     * @param props	Properties object containing entries to set DEBUG on
     * @param prefix	String that the prefixes each classname. If a property
     *			does not begin with this string then it is ignored.
     *
     * @throws ClassNotFoundException if class is not found
     * @throws NoSuchFieldExcetpion if DEBUG field is not found in class
     */
    public static void setDebug (Properties props, String prefix) 
	throws ClassNotFoundException, NoSuchFieldException,
	       IllegalAccessException {

	// Scan through properties
	for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
	    String key = (String)e.nextElement();

	    // Find properties that match prefix
	    if (key.startsWith(prefix)) {

		// Get className and value and set debug
		String className = key.substring(prefix.length());
		if (className != null && className.length() != 0) {
		    String value = (String)props.getProperty(key);
		    if (value != null && value.length() != 0) {
			try {
		            setDebug(className,
				 (Boolean.valueOf(value)).booleanValue() );
			} catch (NoSuchFieldException ex) {
			    throw new NoSuchFieldException(className +
				"." + debugFieldName);
			} catch (IllegalAccessException ex) {
			    throw new IllegalAccessException(className +
				"." + debugFieldName);
			}
		    }
		}
	    }
	}
    }
}

