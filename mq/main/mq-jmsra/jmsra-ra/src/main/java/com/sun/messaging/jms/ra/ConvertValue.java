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

package com.sun.messaging.jms.ra;

import com.sun.messaging.AdministeredObject;
import javax.jms.MessageFormatException;

/**
 *
 */
public class ConvertValue {
    
    /**
     * Creates a new instance of ConvertValue
     */
    public ConvertValue() {
    }

    /**
     * If possible, converts the given object to boolean value
     * @param obj the object to convert
     * @return the converted value
     */
    static boolean
    toBoolean(Object obj) throws MessageFormatException {
        if (obj == null) {
            return Boolean.valueOf((String)null).booleanValue();
        }
        else if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        }
        else if (obj instanceof String) {
            return Boolean.valueOf((String)obj).booleanValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }

    /**
     * If possible, converts the given object to the byte value
     * This method is part of the implementation of the 
     * JMS 1.1 API method getByteProperty on the javax.jms.Message 
     * interface. Attempting to read a null value as a primitive type 
     * must be treated as calling the primitive's corresponding 
     * valueOf(String) conversion method with a null value.
     * @param obj the object to convert
     * @return the coverted value
     **/
     static byte toByte(Object obj) throws MessageFormatException {
        if (obj == null) {
            return Byte.valueOf((String)null).byteValue();
        }
        else if (obj instanceof Byte) {
            return ((Byte)obj).byteValue();
        }
        else if (obj instanceof String) {
            return Byte.valueOf((String)obj).byteValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }

    /**
     * If possible, converts the given object to the short value
     * This method is part of the implementation of the 
     * JMS 1.1 API method getShortProperty on the javax.jms.Message 
     * interface. Attempting to read a null value as a primitive type 
     * must be treated as calling the primitive's corresponding 
     * valueOf(String) conversion method with a null value.
     * @param obj the object to convert
     * @return the coverted value
     **/
    static short
    toShort(Object obj) throws MessageFormatException {
        if (obj == null) {
            return Short.valueOf((String)null).shortValue();
        }
        else if (obj instanceof Short) {
            return ((Short)obj).shortValue();
        }
        else if (obj instanceof String) {
            return Short.valueOf((String)obj).shortValue();
        }
        else if (obj instanceof Byte) {
            return ((Byte)obj).shortValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }


    /**
     * If possible, converts the given object to the int value
     * This method is part of the implementation of the 
     * JMS 1.1 API method getIntProperty on the javax.jms.Message 
     * interface. Attempting to read a null value as a primitive type 
     * must be treated as calling the primitive's corresponding 
     * valueOf(String) conversion method with a null value.
     * @param obj the object to convert
     * @return the coverted value
     **/
    static int
    toInt(Object obj) throws MessageFormatException {
        if (obj == null) {
            return Integer.valueOf((String)null).intValue();
        }
        else if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        }
        else if (obj instanceof String) {
            return Integer.valueOf((String)obj).intValue();
        }
        else if (obj instanceof Byte) {
            return ((Byte)obj).intValue();
        }
        else if (obj instanceof Short) {
            return ((Short)obj).intValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }

    /**
     * If possible, converts the given object to the long value
     * @param obj the object to convert
     * @return the coverted value
     */
    static long
    toLong(Object obj) throws MessageFormatException {
        if (obj == null) {
            return Long.valueOf((String)null).longValue();
        }
        else if (obj instanceof Long) {
            return ((Long)obj).longValue();
        }
        else if (obj instanceof String) {
            return Long.valueOf((String)obj).longValue();
        }
        else if (obj instanceof Byte) {
            return ((Byte)obj).longValue();
        }
        else if (obj instanceof Short) {
            return ((Short)obj).longValue();
        }
        else if (obj instanceof Integer) {
            return ((Integer)obj).longValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }


    /**
     * If possible, converts the given object to the float value
     * @param obj the non null object to convert
     * @return the coverted value
     **/
    static float
    toFloat(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// CTS expects this exception type so will conform to it
        	throw new NullPointerException();
        }
        else if (obj instanceof Float) {
            return ((Float)obj).floatValue();
        }
        else if (obj instanceof String) {
            return Float.valueOf((String)obj).floatValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }

    /**
     * If possible, converts the given object to the double value
     * @param obj the non null object to convert
     * @return the coverted value
     */
    static double
    toDouble(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// CTS expects this exception type so will conform to it
        	throw new NullPointerException();
        }
        else if (obj instanceof Float) {
            return ((Float)obj).doubleValue();
        }
        else if (obj instanceof Double) {
            return ((Double)obj).doubleValue();
        }
        else if (obj instanceof String) {
            return Double.valueOf((String)obj).doubleValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }

    /**
     * If possible, converts the given object to the String value
     * @param obj the object to convert
     * @return the coverted value
     */
    static String
    toString(Object obj) throws MessageFormatException {

        /**
         * Can not convert byte[] to string object.
         */
        if ( obj instanceof byte[] ) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }

        if ( obj == null ) {
            return null;
        }
        else if (obj instanceof String) {
            return (String)obj;
        }
        else {
            return obj.toString();
        }
    }

    /**
     * If possible, converts the given object to the char value
     * @param obj the object to convert
     * @return the coverted value
     */
    static char
    toChar(Object obj) throws MessageFormatException  {
        if (obj == null) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new NullPointerException(errorString);
        }
        else if (obj instanceof Character) {
            return ((Character)obj).charValue();
        }
        else {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_MESSAGE_FORMAT);
            throw new MessageFormatException(errorString, AdministeredObject.cr.X_MESSAGE_FORMAT);
        }
    }
}
