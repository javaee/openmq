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
 * @(#)PortMapperEntry.java	1.10 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * Encapsulates information about a service. For use with the PortMapper
 */
public class PortMapperEntry {

    private int    port = 0;
    private String protocol = null;;
    private String type = null;;
    private String name = null;
    private HashMap properties = null;

    private static boolean DEBUG = false;

    public final static String NEWLINE = "\n";
    public final static String SPACE = " ";

    /**
     */
    public PortMapperEntry() {
    }


    public void addProperty(String name, String value)
    {
        synchronized (this) {
        if (properties == null)
             properties = new HashMap();
        }
        synchronized (properties) {
            properties.put(name, value);
        }
    }

    public void addProperties(HashMap props)
    {
        synchronized (this) {
        if (properties == null)
             properties = new HashMap();
        }
        synchronized (properties) {
            properties.putAll(props);
        }
    }

    /*
     * This method returns the value of a property specified in one
     * portmapper row/entry. For example, a portmapper entry can 
     * look like:
     *   rmi rmi JMX 0 [foo=bar, url=service:jmx:rmi://myhost/jndi/rmi://myhost:9999/server]
     *
     * This method allows  one to get the value of the 'url' property above.
     */
    public String getProperty(String name)
    {
        synchronized (this) {
            if (properties == null)
                return (null);
        }
        synchronized (properties) {
            return ((String)properties.get(name));
        }
    }

    /**
     * Set the service's port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    /**
     * Set the service's protocol (i.e. "tcp" or "ssl")
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Set the service's type (ie "NORMAL", "ADMIN", etc)
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    /**
     * Set the service's name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /** 
     * Convert a PortMapperEntry into a string of the form that matches
     * that expected by parse(). For example
     *  jms tcp NORMAL 5951
     */
    public String toString() {
        String base =  name + SPACE + protocol + SPACE + type + SPACE + port;
        if (properties != null) {
            base += " [" ;
            synchronized(properties) {
                Set keyset = properties.keySet();
                Iterator itr = keyset.iterator();
                while (itr.hasNext()) {
                    String key = (String)itr.next();
                    String value = (String)properties.get(key);
                    base += (key + "=" + value);
                    if (itr.hasNext())
                        base += ",";
                }
            }
            base += "]";
        }
        return base;
    }

    /**
     * Parse a string into a PortMapperEntry. The format of the
     * string should be:
     *  <service name><SP><port><SP><protocol><SP><type><SP>[a=b, c=d]
     *
     * For example:
     *  jms tcp NORMAL 5951 [foo=bar, url=service:jmx:rmi://myhost/jndi/rmi://myhost:9999/server]
     *
     */
    static public PortMapperEntry parse(String s)
        throws IllegalArgumentException {

        PortMapperEntry pme = new PortMapperEntry();
        StringTokenizer st = new StringTokenizer(s);

        pme.name = st.nextToken();
        pme.protocol = st.nextToken();
        pme.type = st.nextToken();
        pme.port = Integer.parseInt(st.nextToken());

        //OK we want to read in properties
        int propIndx = s.indexOf("[");
        if (propIndx != -1) {
            int endPropIndx = s.indexOf("]");
            String sub = s.substring(propIndx+1, endPropIndx);
            StringTokenizer sst = new StringTokenizer(sub,",");
            while (sst.hasMoreTokens()) {
                String pair = sst.nextToken();
                int indx=pair.indexOf("=");
                if (indx == -1) {
                    continue;
                }
                String name= pair.substring(0,indx);
                String value= pair.substring(indx+1);
                pme.addProperty(name,value);
            }
        }   

        return pme;
    }
}
