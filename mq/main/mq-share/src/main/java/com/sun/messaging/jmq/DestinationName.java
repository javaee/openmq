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
 * %W% %G%
 */ 

package com.sun.messaging.jmq;

/**
 * <code>DestinationName</code> encapsulates the validation
 * of the JMQ provider specific syntax for Destination Names.
 */
public class DestinationName {

    /* No public constructor needed */
    private DestinationName(){}

    /**
     * Internal destination name prefix
     * @since 3.5
     */
    public static final String INTERNAL_DEST_PREFIX = "mq.";


    /**
     * Validates whether a name conforms to the
     * JMQ provider specific syntax for Destination
     * Names.
     * 
     * @param name The name to be validated.
     *
     * @return <code>true</code> if the name is valid;
     *         <code>false</code> if the name is invalid.
     */
    public static final boolean isSyntaxValid(String name) {
        //Invalid if name is null or empty.
        if (name == null || "".equals(name)) {
            return false;
        }
        if (isInternal(name)) {
	    /*
            // remove .'s for validation
            StringBuffer tmp = new StringBuffer(name);
            for (int i=0; i < tmp.length(); i ++) {
                if (tmp.charAt(i) == '.') {
                    tmp.setCharAt(i, '_');
                }
            }
            name = tmp.toString();
	    */
	    
	    /*
	     * Relax syntax checking if name starts with "mq."
	     * This is to allow temporary destinations to be
	     * monitored. The previous syntax checking
	     * was preventing destination names such as
	     * the following from being created:
	     *  mq.metrics.destination.queue.temporary_destination://queue/192.18.116.222/48422/1
	     */
	    return (true);
        }
        //Verify identifier start character and part
        char[] namechars = name.toCharArray();
        if (Character.isJavaIdentifierStart(namechars[0]) ||
            (namechars[0]=='*' || namechars[0]=='>')) {
            for (int i = 1; i<namechars.length; i++) {
                if (namechars[i] == '.') { // valid for wildcards
                } else if (namechars[i] == '*') { // valid for wildcards
                } else if (namechars[i] == '>') { // valid for whildcards
                } else if (!Character.isJavaIdentifierPart(namechars[i])) {
                    //Invalid if body characters are not valid using isJavaIdentifierPart().
                    return false;
                }
            }   
        } else {
            //Invalid if first character is not valid using isJavaIdentifierStart().
            return false;
        }
        return true;
    }

    /**
     * @since 3.5
     */
    public static boolean isInternal(String destName) {
	if ((destName != null) &&
	    destName.startsWith(INTERNAL_DEST_PREFIX))  {
	    return (true);
	}

	return (false);
    }

}

