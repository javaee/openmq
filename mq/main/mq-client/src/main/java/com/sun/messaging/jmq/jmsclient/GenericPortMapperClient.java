/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)GenericPortMapperClient.java	1.8 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

import com.sun.messaging.jmq.io.PortMapperTable;
import com.sun.messaging.jmq.io.PortMapperEntry;

/**
 *
 * This is a generic class (ie does not use any JMX/JMX specific
 * classes to read the portmapper.
 *
 * It is assumed that the user of this class will perform the JMS
 * or JMX specific checks e.g. version checking and act accordingly.
 *
 */


public class GenericPortMapperClient {

    protected PortMapperTable portMapperTable = null;
    protected String host;
    protected int port;
    private boolean debug = Debug.debug;

    public GenericPortMapperClient (String host, int port)  {
        this.host = host;
        this.port = port;
        init();
    }

    public int getPortForProtocol(String protocol, String type){
        return getPort(protocol, type, null);
    }

    public int getPortForService(String protocol, String service, String type){
        return getPort(protocol, type, service);
    }

    /*
     * Get key property value of portmapper entry with the given protocol, 
     * type, and serviename. If any of protocol/type/servicename is null,
     * that means it is a "don't care" e.g. if protocol is null, any
     * protocol value is OK - the protocol won't be used to find a 
     * matching entry.
     *
     * The portmapper entry can look like the following:
     *
     *   jmxrmi rmi JMX 0 [url=theURL]
     *
     * From the above entry, the only property or key is "url" and it's value
     * is "theURL".
     */
    public String getProperty(String key, String protocol, String type, 
			String servicename) {
        int port = 25374;
	String propVal = null;
        Map table = portMapperTable.getServices();
        PortMapperEntry pme = null;

        Iterator pmeIterator = table.values().iterator();
        while (pmeIterator.hasNext()){
            pme = (PortMapperEntry) pmeIterator.next();

            if ((protocol != null) && !pme.getProtocol().equals(protocol)) {
		continue;
	    }

            if ((type != null) && !pme.getType().equals(type)) {
		continue;
	    }

            if ((servicename != null) && !pme.getName().equals(servicename)) {
		continue;
	    }

	    propVal = pme.getProperty(key);
	    if (propVal != null)  {
		break;
	    }


	    /*
	     * OLD logic - did not handle nulls for protocol
            if (pme.getProtocol().equals(protocol)){
                if (pme.getType().equals(type)){
                    if (servicename == null){
			propVal = pme.getProperty(key);
                        break;
                    } else {
                        if (pme.getName().equals(servicename)){
			    propVal = pme.getProperty(key);
                            break;
                        }
                    }
                }
            }
	    */
        }
        return propVal;
    }

    private int getPort(String protocol, String type, String servicename) {
        int port = 25374;
        Map table = portMapperTable.getServices();
        PortMapperEntry pme = null;

        Iterator pmeIterator = table.values().iterator();
        while (pmeIterator.hasNext()){
            pme = (PortMapperEntry) pmeIterator.next();
            if (pme.getProtocol().equals(protocol)){
                if (pme.getType().equals(type)){
                    if (servicename == null){
                        port = pme.getPort();
                        break;
                    } else {
                        if (pme.getName().equals(servicename)){
                            port = pme.getPort();
                            break;
                        }
                    }
                }
            }
        }
        return port;
    }

    protected void init()  {
        readBrokerPorts();
    }

    public String getPaketVersion()  {
        return portMapperTable.getPacketVersion();
    }

    protected void readBrokerPorts()  {
        if ( debug ) {
            Debug.println("PortMapper connecting to host: " + host + "  port: " + port);
        }

        try {
            String version =
                String.valueOf(PortMapperTable.PORTMAPPER_VERSION) + "\n";

            Socket socket = new Socket (host, port);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // Write version of portmapper we support to broker
            try {
                os.write(version.getBytes());
                os.flush();
            } catch (IOException e) {
                // This can sometimes fail if the server already wrote
                // the port table and closed the connection
            }

            portMapperTable = new PortMapperTable();
            portMapperTable.read(is);

            is.close();
            socket.close();

        } catch ( Exception e ) {
	    throw new RuntimeException("Exception caught when reading portmapper.", e);
        }
    }

    public static void main (String args[]) {
        try {
            GenericPortMapperClient pmc = new GenericPortMapperClient("localhost", 7676);

            String url = pmc.getProperty("url", "rmi", "JMX", null);

	    System.out.println("url = " + url );

        } catch (Exception e) {
            Debug.printStackTrace(e);
        }
    }
}

