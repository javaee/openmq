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

package com.sun.messaging.bridge.api;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Properties;

/**
 * These utility methods are from jmsserver/config/
 */

public class BridgeUtil {

    /**
     *
     */
    public static List<String> getListProperty(String name, Properties props) {
        String value = props.getProperty(name);
        if (value == null) return new ArrayList<String>();
        return breakToList(value, ",");
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


    /**
     * Returns a list of property names that match specified name prefix.
     *
     * @param prefix The proerty name prefix
     * @return a list of all property names that match the name prefix
     */
    public static List<String> getPropertyNames(String prefix, Properties props) {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration e = props.keys();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            if (key.startsWith(prefix)) {
                list.add(key);
            }
        }
        return list;
    }

    public static String toString(String[] args) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
           buf.append(args[i]);
           buf.append(" ");
        }
        return buf.toString();
    }
}
