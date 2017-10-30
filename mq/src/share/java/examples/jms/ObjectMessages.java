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

import javax.jms.*;

/**
 * The ObjectMessages class consists only of a main method, which demonstrates
 * that mutable objects are copied, not passed by reference, when you use them 
 * to create message objects.
 * <p>
 * The example uses only an ObjectMessage and a BytesMessage, but the same is
 * true for all message formats.
 */
public class ObjectMessages {

    /**
     * Main method.  Takes no arguments.
     */
    public static void main(String[] args) {
        ConnectionFactory    connectionFactory = null;
        Connection           connection = null;
        Session              session = null;
        ObjectMessage        objectMessage = null;
        String               object = "A String is an object.";
        BytesMessage         bytesMessage = null;
        byte[]               byteArray = {3, 5, 7, 9, 11};
        final int            ARRLEN = 5;
        int                  length = 0;
        byte[]               inByteData = new byte[ARRLEN];
        int                  exitResult = 0;

    	try {
            connectionFactory = 
                SampleUtilities.getConnectionFactory();
    	    connection = 
    	        connectionFactory.createConnection();
    	    session = connection.createSession(false, 
    	        Session.AUTO_ACKNOWLEDGE);
    	} catch (Exception e) {
            System.out.println("Connection problem: " + e.toString());
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ee) {}
            }
    	    System.exit(1);
    	} 

        try {
    	    /* 
    	     * Create an ObjectMessage from a String.
    	     * Modify the original object.
    	     * Read the message, proving that the object in the message
             * has not changed.
             */
    	    objectMessage = session.createObjectMessage();
    	    System.out.println("Writing ObjectMessage with string:  " + object);
    	    objectMessage.setObject(object);
    	    object = "I'm a different String now.";
    	    System.out.println("Changed string; object is now:  " + object);
            System.out.println("ObjectMessage contains:  " + 
                (String) objectMessage.getObject()); 

    	    /* 
    	     * Create a BytesMessage from an array.
    	     * Modify an element of the original array.
    	     * Reset and read the message, proving that contents of the message
             * have not changed.
    	     */
    	    bytesMessage = session.createBytesMessage();
    	    System.out.print("Writing BytesMessage with array: ");
            for (int i = 0; i < ARRLEN; i++) {
                System.out.print(" " + byteArray[i]);
    	    }
    	    System.out.println();
    	    bytesMessage.writeBytes(byteArray);
    	    byteArray[1] = 13;
    	    System.out.print("Changed array element; array is now: ");
            for (int i = 0; i < ARRLEN; i++) {
                System.out.print(" " + byteArray[i]);
    	    }
    	    System.out.println();
    	    bytesMessage.reset();
            length = bytesMessage.readBytes(inByteData);
            System.out.print("BytesMessage contains: ");
            for (int i = 0; i < length; i++) {
                System.out.print(" " + inByteData[i]);
            }
    	    System.out.println();
    	} catch (JMSException e) {
    	    System.out.println("Exception occurred: " + e.toString());
    	    exitResult = 1;
    	} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
    	            exitResult = 1;
                }
    	    }
    	}
    	SampleUtilities.exit(exitResult);
    }
}
