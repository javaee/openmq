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
 * @(#)ValueConvert.java	1.13 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.MessageFormatException;

import com.sun.messaging.AdministeredObject;

/** This class is used by
  *
  * MessageImpl       for property value conversion
  * MapMessageImpl    for mapped value read conversion (see MapMessageImpl.java)
  * StreamMessageImpl for data read conversion (see StreamMessageImpl.java)
  *
  * <P>Message properties support the following conversion table. The marked
  * cases must be supported. The unmarked cases must throw a JMSException. The
  * String to primitive conversions may throw a runtime exception if the
  * primitives <CODE>valueOf()</CODE> method does not accept it as a valid
  * String representation of the primitive.
  *
  * <P>A value written as the row type can be read as the column type.
  *
  * <PRE>
  * |        | boolean byte short int long float double String
  * |----------------------------------------------------------
  * |boolean |    X                                       X
  * |byte    |          X     X    X   X                  X
  * |short   |                X    X   X                  X
  * |int     |                     X   X                  X
  * |long    |                         X                  X
  * |float   |                               X     X      X
  * |double  |                                     X      X
  * |String  |    X     X     X    X   X     X     X      X
  * |----------------------------------------------------------
  * </PRE>
  *
  * <P>In addition to the type-specific set/get methods for properties, JMS
  * provides the <CODE>setObjectProperty</CODE> and
  * <CODE>getObjectProperty</CODE> methods. These support the same set of
  * property types using the objectified primitive values. Their purpose is
  * to allow the decision of property type to made at execution time rather
  * than at compile time. They support the same property value conversions.
  *
  * <P>The <CODE>setObjectProperty</CODE> method accepts values of class
  * Boolean, Byte, Short, Integer, Long, Float, Double and String. An attempt
  * to use any other class must throw a JMSException.
  *
  * <P>The <CODE>getObjectProperty</CODE> method only returns values of class
  * Boolean, Byte, Short, Integer, Long, Float, Double and String.
  */

class ValueConvert {

    /**
     * If possible, converts the given object to boolean value
     * @param obj the object to convert
     * @return the converted value
     */
    static boolean
    toBoolean(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// deliberately delegate the handling of this null value to the primitive's valueOf method
        	// in this case it will return false
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
     * @param obj the object to convert
     * @return the coverted value
     **/
     static byte toByte(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// deliberately delegate the handling of this null value to the primitive's valueOf method
        	// in this case it will throw a java.lang.NumberFormatException 
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
     * @param obj the object to convert
     * @return the coverted value
     **/
    static short
    toShort(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// deliberately delegate the handling of this null value to the primitive's valueOf method
        	// in this case it will throw a java.lang.NumberFormatException 
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
     * @param obj the object to convert
     * @return the coverted value
     **/
    static int
    toInt(Object obj) throws MessageFormatException {
        if (obj == null) {
        	// deliberately delegate the handling of this null value to the primitive's valueOf method
        	// in this case it will throw a java.lang.NumberFormatException 
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
        	// deliberately delegate the handling of this null value to the primitive's valueOf method
        	// in this case it will throw a java.lang.NumberFormatException 
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
        	// explicitly throw a java.lang.NullPointerException: [C4017]: Invalid message format.
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
