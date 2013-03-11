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
 * @(#)PortMapperTable.java	1.14 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.util.StringTokenizer;

import com.sun.messaging.jmq.resources.*;

/**
 * A table of PortMapperEntries. Knows how to parse and generate
 * the output used by the portmapper service.
 */
public class PortMapperTable {

    private static boolean DEBUG = false;

    public final static int PORTMAPPER_VERSION = 101;
    public final static String DOT = ".";
    public final static String NEWLINE = "\n";
    public final static String SPACE = " ";
    public final static byte NEWLINE_BYTE = 10;
    public final static byte DOT_BYTE = 46;
    public final static byte SPACE_BYTE = 32;

    private String    brokerInstance = "???";
    private String    packetVersion = "???";
    private String    version = "???";

    private Map  table = null;

    /**
     * Construct an unititialized system message ID. It is assumed
     * the caller will set the fields either explicitly or via
     * readID()
     */
    public PortMapperTable() {
        try {
            Class c = Class.forName("java.util.LinkedHashMap");
            Map m = (Map)c.newInstance();
            table = Collections.synchronizedMap(m);
        } catch (Throwable ex) {
            table = Collections.synchronizedMap(new java.util.HashMap());
        }
        version = Integer.toString(PORTMAPPER_VERSION);
    }

    /**
     * Add a service
     */
    public void add(PortMapperEntry e) {
	table.remove(e.getName());
        table.put(e.getName(), e);
    }

    /**
     * get a service
     */

    public PortMapperEntry get(String name) {
        return (PortMapperEntry)table.get(name);
    }

    /**
     * Remove a service
     */
    public void remove(String name) {
        table.remove(name);
    }

    /**
     * Remove a service. 
     *
     * @param e PortMapperEntry to get service name from to delete.
     */
    public void remove(PortMapperEntry e) {
        table.remove(e.getName());
    }

    /**
     * Set the broker instance name
     */
    public void setBrokerInstanceName(String name) {
        brokerInstance = name;
    }

    /**
     * Set the broker version string
     */
    public void setPacketVersion(String s) {
        packetVersion = s;
    }

    /**
     * Get the broker instance name
     */
    public String getBrokerInstanceName() {
        return brokerInstance;
    }

    /**
     * Get the portmapper version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the broker version number
     */
    public String getPacketVersion() {
        return packetVersion;
    }

    /**
     * Get a hashtable containing the servicename/PortMapperEntry pairs
     */
    public Map getServices() {
        return table;
    }

    /**
     * Get the port number for a particular service.
     *
     * @param service   Name of service to get port number for
     * @returns         Port number, or -1 if port for service is not known.
     */
    public int getPortForService(String service) {

        PortMapperEntry pme = (PortMapperEntry)table.get(service);
        if (pme == null) {
            return -1;
        } else {
            return pme.getPort();
        }
    }

    public String toString() {
        return version + " " + brokerInstance + " " + packetVersion +
            table.toString();
    }


    /**
     * Write the portmapper data to the specified DataOutputStream.
     * The formate of the data is:
     *
     *  <PRE>
     *  <portmapper version><SP><broker instance name><SP>broker version><NL>
     *  <service name><SP><protocol><SP><type><SP><port><NL>
     *  <.><NL>
     *
     *  Where:
     *
     *  <portmapper version>Portmapper numeric version string (ie "100").
     *  <broker version>    Broker version string (ie "2.0").
     *  <NL>                Newline character (octal 012)
     *  <service name>      Alphanumeric string. No embedded whitespace.
     *  <space>             A single space character
     *  <protocol>          Transport protocol. Typically "tcp" or "ssl"
     *  <service>           Service type. Typically "NORMAL", "ADMIN" or
     *                      "PORTMAPPER"
     *  <port>              Numeric string. Service port number
     *  <.>                 The '.' (dot) character
     *
     *  An example would be:
     *
     *  101 jmqbroker 2.0
     *  portmapper tcp PORTMAPPER 7575
     *  jms tcp NORMAL 59510
     *  admin tcp ADMIN 59997
     *  ssljms ssl NORMAL 42322
     *  .
     *
     *   </PRE>
     *
     * @param    out    OutputStream to write ID to
     *
     */
    public void write(OutputStream out)
	throws IOException {

        StringBuffer data = new StringBuffer();
        String  name;
        PortMapperEntry pme;
        Integer port;

        data.append(PORTMAPPER_VERSION + SPACE + brokerInstance + SPACE +
		    packetVersion + NEWLINE);

        for (Iterator e = table.keySet().iterator(); e.hasNext() ;) {
            name = (String)e.next();
            pme = (PortMapperEntry)table.get(name);
            data.append(pme.toString() + NEWLINE);
        }

        data.append(DOT + NEWLINE);

        out.write(data.toString().getBytes("ASCII"));
        out.flush();
    }

    /**
     * Read the data from the specified DataInputStream. The format of
     * the data is assumed to match that generated by write.
     *
     * @param    in    InputStream to read from
     *
     */
    public void read(InputStream is)
	throws IOException {

        BufferedInputStream in = new BufferedInputStream(is);
	/*
	 * IH: Increased size of buffer from 128 to 2048. There
	 * shouldn't really be a hard limit here.
	 */
        byte[] buffer = new byte[2048];
        int nBytes = 0;

        if (DEBUG) {
            System.err.println(this.getClass().getName() +
                ".read():");
        }

        // Read first line
        nBytes = readLine(in, buffer);
        if (nBytes < 0 ) {
            throw new IOException(
                SharedResources.getResources().getString(
                SharedResources.X_PORTMAPPER_SOCKET_CLOSED_UNEXPECTEDLY));
        }

	StringTokenizer st = new StringTokenizer(new String(buffer, "ASCII"));

        int ver = -1;
        try {
            version = st.nextToken();
            ver = Integer.parseInt(version);
        } catch (Exception e) {
            throw new IOException(
                SharedResources.getResources().getString(
                    SharedResources.X_BAD_PORTMAPPER_VERSION,
                    String.valueOf(version),
                    String.valueOf(PORTMAPPER_VERSION)), e);
        }
        if (ver != PORTMAPPER_VERSION) {
            throw new IOException(
                SharedResources.getResources().getString(
                    SharedResources.X_BAD_PORTMAPPER_VERSION,
                    String.valueOf(version),
                    String.valueOf(PORTMAPPER_VERSION)));
        }

        brokerInstance = st.nextToken();

	packetVersion = st.nextToken();

        // Read service name/port number value pairs
        while (true) {
            nBytes = readLine(in, buffer);

            if (nBytes <= 0 || (nBytes == 1 && buffer[0] == DOT_BYTE)) {
                break;
            }

            PortMapperEntry pme = PortMapperEntry.parse(
                                    new String(buffer, 0, nBytes, "ASCII"));
            this.add(pme);
        }
    }

    /**
     * Read an ASCII line from an input stream into a buffer. If
     * the input line is longer than the buffer then the bytes at
     * the end of the line are lost
     *
     * @returns Number of bytes in buffer
     */
    private int readLine(InputStream in, byte[] buffer)
	throws IOException {

        int b = 0;
        int n = 0;

        b = in.read();
        while (b != -1 && b != NEWLINE_BYTE) {
            if (n < buffer.length) {
                buffer[n] = (byte)b;
                n++;
            }
            b = in.read();
        }

        if (DEBUG) {
            try {
                System.err.println(new String(buffer, 0, n, "ASCII"));
            } catch (UnsupportedEncodingException e) {
            }
        }

        if (n == 0 && b == -1) {
            return -1;
        }
        return n;

    }
}
