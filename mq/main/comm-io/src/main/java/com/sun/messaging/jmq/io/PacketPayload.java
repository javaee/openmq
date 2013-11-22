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
 * @(#)PacketPayload.java	1.6 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.Hashtable;
import java.io.*;
import java.nio.ByteBuffer;

import com.sun.messaging.jmq.util.net.IPAddress;

/**
 * The payload of an iMQ packet. The payload consists of the message
 * properties (which are stored as a serialized java.util.Hashtable)
 * and the message body (simply a sequence of bytes).
 */
public class PacketPayload {


    // Property buffer
    protected ByteBuffer propBuf_v1 = null;
    protected ByteBuffer propBuf_v2 = null;
    // Property Hashtable
    protected Hashtable	properties = null;

    // Body buffer
    protected ByteBuffer bodyBuf = null;

    /**
     * Create a new empty packet payload
     */
    public PacketPayload() {
        reset();
    }

    /**
     * Clear the packet payload
     */
    public synchronized void reset() {
        propBuf_v1 = null;
        propBuf_v2 = null;
        properties = null;
        bodyBuf = null;
    }


    /**
     * Get the payload body.
     * WARNING! The returned ByteBuffer is NOT a copy or duplicate!
     */
    public synchronized ByteBuffer getBodyBytes() {
        if (bodyBuf == null) {
            return null;
        } else {
            bodyBuf.rewind();
            return bodyBuf;
        }
    }

    /**
     * Return the size of the message body in bytes
     *
     * @return    Size of message body in bytes
     */
    public int getBodySize() {
        if (bodyBuf == null) {
            return 0;
        } else {
	    return bodyBuf.limit();
        }
    }

    /**
     * Get the payload body as an InputStream. The returned
     * InputStream contains the contents of the message body.
     *
     * @return    An InputStream from which the message body can
     *            be read from. Or null if no message body.
     */
    public InputStream getBodyStream() {
	if (bodyBuf == null) {
	    return null;
        } else {
	    return new JMQByteBufferInputStream(getBodyBytes());
	}
    }

    /**
     * Get the payload properties as a java.util.Hashtable.
     * The hashtable is NOT a copy
     */
    public synchronized Hashtable getProperties() throws
        IOException, ClassNotFoundException  {

        if (properties != null) {
            return properties;
        }
        ByteBuffer propBuf = propBuf_v2;
        short version = Packet.VERSION3;
        if (propBuf == null && propBuf_v1 != null) {
            propBuf = propBuf_v1;
            version = Packet.VERSION2;
        }

        if (propBuf != null) {
            // Need to deserialize properties
            propBuf.rewind();
            try {
                // retrieve the properties stream from the "right"
                // type of byte buffer
                InputStream is = getPropertiesStream(version);

                if (version >= Packet.VERSION3) {
                     properties = PacketProperties.parseProperties(is);
               } else {
                    properties = parseProperties(is);
                }
            } catch (IOException e) {
                // Should never happen
                System.err.println("Could not parse properties " + e);
                e.printStackTrace();
                throw e;
            } catch (ClassNotFoundException e) {
                // Should never happen
                System.err.println("Could not parse properties " + e);
                e.printStackTrace();
                throw e;
            }
            return properties;
        } else {
            return null;
        }
    }

    /**
     * Get the payload property bytes.
     * WARNING! The returned ByteBuffer is NOT a copy or duplicate!
     */
    public synchronized ByteBuffer getPropertiesBytes(short version) {


        if (propBuf_v1 == null && propBuf_v2 == null && properties == null) {
            return null;
        }

        ByteBuffer propBuf = (version >= Packet.VERSION3) ? propBuf_v2 : propBuf_v1;

        // see if we have the correct version of the buffer

        if (propBuf == null &&  properties == null) {
                 // convert other format to a properties object
                try {
                    getProperties();
                } catch (Exception ex) {
                      ex.printStackTrace(); //XXX
                }
        }


        if (propBuf == null) {
            // Backing byte array will grow if needed
            JMQByteArrayOutputStream bos = 
                new JMQByteArrayOutputStream(new byte[256]);

            try {
                if (version >= Packet.VERSION3) {
                    PacketProperties.write(properties, bos);
                } else {
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(properties);
                    oos.close();
                }
            } catch (Exception e) {
                // Should never happen
                System.err.println("Could not marshal properties " + e);
                e.printStackTrace();
            }
            propBuf = ByteBuffer.wrap(bos.getBuf(), 0, bos.getCount());
        }
        propBuf.rewind();
        return propBuf;
    }


    /**
     * Get the payload properties as an input stream.
     * bytes.
     *
     * @return    An InputStream from which the message property bytes  can
     *            be read from. Or null if no message properties.
     */
    public InputStream getPropertiesStream(short version) {

        ByteBuffer buf = getPropertiesBytes(version);

        if (buf == null) {
            return null;
        } else {
	    return new JMQByteBufferInputStream(buf);
        }
    }

    /**
     * Set the payloads body.
     * WARNING! The passed buffer is NOT copied or duplicated
     *
     * @param   buf Buffer containing body content.
     */
    public synchronized void setBody(ByteBuffer body) {
        if (body == null) {
            bodyBuf = null;
        } else {
            bodyBuf = body;
            bodyBuf.rewind();
        }
    }

    /**
     * Set the payload properties as an instance of java.util.Hashtable.
     * The hashtable is NOT copied
     */
    public synchronized void setProperties(Hashtable props) {
        properties = props;
        propBuf_v1 = null;
        propBuf_v2 = null;
        return;
    }

    /**
     * Set the payload properties as raw bytes (i.e. a serialized 
     * java.util.Hashtable bytes).
     * WARNING! The passed buffer is NOT copied or duplicated
     */
    public synchronized void setPropertiesBytes(ByteBuffer buf, short version) {

        if (buf == null) {
            propBuf_v1 = null;
            propBuf_v2 = null;
        } else {
            ByteBuffer propBuf = null;
            if (version >= Packet.VERSION3) {
                propBuf = propBuf_v2=buf;
            } else {
                propBuf = propBuf_v1=buf;
            }
             propBuf.rewind();
        }
        properties = null;
    }

    /**
     * Read the property bytes (VERSION2) from an input stream and convert to
     * a java.util.Hashtable.
     */
    private Hashtable parseProperties(InputStream is)
        throws IOException, ClassNotFoundException {

        ObjectInputStream p = new ObjectInputStream(is);
        return (Hashtable)p.readObject();
    }
}
