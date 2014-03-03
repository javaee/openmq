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
 * @(#)StringUtil.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Properties;


/**
 * StringUtil
 */
public class StringUtil {

    /**
     * Expand property variables a string with the
     * corresponding values in a Properties instance. A property
     * variable has the form ${some.property}. The variable
     * ${/} is shorthand for ${file.separator}. So if the string contains
     * ${jmq.varhome}${/}${jmq.instancename}${/}store then it would be
     * expanded to something like /var/opt/SUNWjmq/jmqbroker/store
     * ${NL} is shorthand for ${line.separator}
     *
     * If there are no variables in 'str' then you will get back your
     * original string.
     *
     * @param str   The string to expand variables in
     * @param props The Properties object to extract variables from
     *
     * @returns A string with all variables expanded
     */
    public static String expandVariables(String str, Properties props) {

	if (str == null) return str;

	String vname, value = null;

	int len = str.length();
	StringBuffer sbuf = null;

	int current, vstart, vend;
	current = vstart = vend = 0;

	while (current < len) {
	    // Locate the start of a variable
	    if ((vstart = str.indexOf('$', current)) == -1) {

                if (sbuf != null) {
	            // No more variables. Copy remainder of string and stop
	            sbuf.append(str.substring(current, len));
                }
		break;
	    }

	    if (str.charAt(vstart + 1) == '{') {
		// We have a variable start. Find the end
	        if ((vend = str.indexOf('}', vstart + 1)) == -1) {

                  if (sbuf != null) {
		    //No end. No more vars. Copy remainder of string and stop
		    sbuf.append(str.substring(current, len));
                  }
		  break;
	        }
	    }

            // Looks like we are expanding variables. Allocate buffer
            // if we haven't already.
            if (sbuf == null) {
	        sbuf = new StringBuffer(2 * len);
            }

	    // ${jmq.home}
	    // ^         ^-vend
	    // +- vstart
            if (vend > vstart) {
	        vname = str.substring(vstart + 2, vend);
            } else {
                // Variable is malformed.
                vname = null;
                vend = vstart;
            }

	    // Get variable value
            if (vname == null) {
                value = null;
	    } else if (vname.equals("/")) {
		value = System.getProperty("file.separator");
            } else if (vname.equals("NL")) {
		value = System.getProperty("line.separator");
            } else if (props != null) {
	        value = props.getProperty(vname);
            }

	    // Copy over stuff before variable
	    sbuf.append(str.substring(current, vstart));

	    // Copy variable
	    if (value == null) {
		// No value. Just duplicate variable name and move on
		sbuf.append(str.substring(vstart, vend + 1));
            } else {
		// Good variable. Copy value
		sbuf.append(value);
            }

	    // Advance current pointer past variable and continue
	    current = vend + 1;
	}

        if (sbuf != null) {
	    return sbuf.toString();
        } else {
            // If we never expanded variables we can return the original string
            return str;
        }
    }


    /**
     * Convert a string of "key1=val1, key2=val2, .." to Properties
     */
    public static Properties toProperties(String keyvalPairs) {
        return toProperties(keyvalPairs, null); 
    }

    public static Properties toProperties(String keyvalPairs, Properties props) {
        return toProperties(keyvalPairs, ",", props);
    }

    public static Properties toProperties(String keyvalPairs, String separator, Properties p) {
        Properties props =  p;
        if (props == null) props = new Properties();
        if (keyvalPairs == null) return props;

        List<String> pairs = breakToList(keyvalPairs, separator);
        for (String pair : pairs) {
            List<String> l = breakToList(pair, "=");
            if (l.size() != 2 ) {
                throw new IllegalArgumentException("Invalid property element: "+pair);
            }
            props.setProperty(l.get(0), l.get(1));
        }
        return props;
    }

    public static List<String> breakToList(String value, String separator) {
        StringTokenizer token = new StringTokenizer(value, separator, false);
        List<String> retv = new ArrayList<String>();
        while (token.hasMoreElements()) {
            String newtoken = token.nextToken();
            newtoken = newtoken.trim();
            int start = 0;
            while (start < newtoken.length()) {
                if (!Character.isSpaceChar(newtoken.charAt(start)))
                    break;
                start ++;
            }
            if (start > 0)
                newtoken = newtoken.substring(start+1);
            if (newtoken.trim().length() > 0)
                retv.add(newtoken.trim());
        }
        return retv;
    }

}

