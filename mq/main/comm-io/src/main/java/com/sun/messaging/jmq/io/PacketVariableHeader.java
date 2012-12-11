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
 * @(#)PacketVariableHeader.java	1.11 07/10/07
 */ 

package com.sun.messaging.jmq.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;

public class PacketVariableHeader {

    // Needed to convert between UTF-8 and String
    protected static final CharsetDecoder decoder;
    protected static final CharsetEncoder encoder;
    protected static final Charset        charset;

    // Buffer to hold variable portion of header
    protected ByteBuffer buffer = null;

    protected boolean bufferDirty = false;
    protected boolean bufferParsed = false;

    // The variable portion of the packet contains primarily strings
    // Currently transactionID, producerID, deliveryTime 
    // and deliverycount are the only exception.
    protected long      transactionID      = 0L;
    protected long      producerID         = 0L;
    protected long      deliveryTime       = 0L;
    protected int       deliveryCount      = 0;
    protected String[]  stringItems = new String[PacketString.LAST];

    static {
        Charset charsetBuf = null;
        CharsetDecoder decoderBuf = null;
        CharsetEncoder encoderBuf = null;
        try {
            charsetBuf = Charset.forName("UTF-8");
            decoderBuf = charsetBuf.newDecoder();
            encoderBuf = charsetBuf.newEncoder();
        } catch (Exception e) {
            // Should never happen
            System.err.println("Could not get charset: " + e);
        }
        charset = charsetBuf;
        decoder = decoderBuf;
        encoder = encoderBuf;
        
    }

    public PacketVariableHeader() {
	this.reset();
    }

    /**
     * Set the variable header portion as bytes
     * WARNING! The buffer is NOT copied or duplicated!
     */
    public synchronized void setBytes(ByteBuffer buf) {
        // Clear all data members and set buffer
        reset();

        if (buf == null) {
            buffer = null;
        } else {
            buffer = buf;
            buffer.rewind();
        }

        bufferParsed = false;
        bufferDirty = false;
    }

    /**
     * Return the variable header portion as bytes
     * WARNING! The buffer is NOT a copy or duplicate.
     */
    public synchronized ByteBuffer getBytes()
        throws IOException {

        if (bufferDirty) {
            updateBuffer();
        }

        if (buffer == null) {
            return null;
        }

        buffer.rewind();
        return buffer;
    }

    /**
     * Return the variable header portion as bytes in a way
     * that is compatible with 2.0 clients.
     *
     * This routine will not return null. If there is no data in
     * the variable header a buffer will be allocated that just
     * contains the terminating NULL entry. This is for backwards
     * compatibility with 2.0 clients that had a bug and always
     * expected something in this part of the packet.
     * 
     * WARNING! The buffer is NOT a copy or duplicate.
     */
    public synchronized ByteBuffer getBytes2()
        throws IOException {

        if (bufferDirty || buffer == null) {
            updateBuffer();
        }

        return getBytes();
    }

    /**
     * Get the string value for 'field' from the buffer
     */
    protected synchronized String getStringField(int field) {

        if (!bufferParsed) {
            parseBuffer();
        }

        if (field < PacketString.LAST) {
            return stringItems[field];
        } else {
            return null;
        }
    }


    /**
     * Get the long value for 'field' from the variable header portion of
     * the packet.
     */
    protected synchronized long getLongField(int field) {

        if (!bufferParsed) {
            parseBuffer();
        }

        switch (field) {

        case PacketString.TRANSACTIONID:
            return transactionID;
        case PacketString.PRODUCERID:
            return producerID;
        case PacketString.DELIVERY_TIME:
            return deliveryTime;
        default:
            return 0;
        }
    }

    protected synchronized void setStringField(int field, String value) {

        // We must do this so we don't loose other field values if
        // updateBuffer is called.
        if (!bufferParsed) {
            parseBuffer();
        }

        if (field < PacketString.LAST) {
            stringItems[field] = value;
            bufferDirty = true;
        }
    }

    protected synchronized void setLongField(int field, long value) {

        // We must do this so we don't loose other field values if
        // updateBuffer is called.
        if (!bufferParsed) {
            parseBuffer();
        }

        switch (field) {

        case PacketString.TRANSACTIONID:
            transactionID = value;
            bufferDirty = true;
            break;
        case PacketString.PRODUCERID:
            producerID = value;
            bufferDirty = true;
            break;
        case PacketString.DELIVERY_TIME:
            deliveryTime = value;
            bufferDirty = true;
            break;
        default:
            break;
        }
    }

    /**
     * Get the int value for 'field' from the variable header portion of
     * the packet.
     */
    protected synchronized int getIntField(int field) {

        if (!bufferParsed) {
            parseBuffer();
        }

        switch (field) {

        case PacketString.DELIVERY_COUNT:
            return deliveryCount;
        default:
            return 0;
        }
    }

    protected synchronized void setIntField(int field, int value) {

        // We must do this so we don't loose other field values if
        // updateBuffer is called.
        if (!bufferParsed) {
            parseBuffer();
        }

        switch (field) {

	case PacketString.DELIVERY_COUNT:
            deliveryCount = value;
            bufferDirty = true;
            break;
	default:
            break;
	}
    }


    /**
     * Reset packet to initial values
     */
    protected void reset() {
        for (int n = 0; n < PacketString.LAST; n++) {
            stringItems[n] = null;
        }
        transactionID = 0L;
        producerID    = 0L;
        deliveryTime = 0L;
        deliveryCount = 0;

        //buffer = null;
        if (buffer != null) {
            buffer.clear();
        }
        bufferDirty = false;
        bufferParsed = true;
    }

    /**
     * Parse buffer and populate class with values
     */
    void parseBuffer() {

        int type, len = 0;

        if (buffer == null) {
            bufferParsed = true;
            return;
        }

        buffer.rewind();

        type = buffer.getShort();
	while (type != PacketString.NULL) {
            switch(type) {

            case PacketString.TRANSACTIONID:
                // Skip length. TransactinID is a long
                len = buffer.getShort();
                transactionID = buffer.getLong();
                break;

            case PacketString.PRODUCERID:
                // Skip length. ProducerID is a long
                len = buffer.getShort();
                producerID = buffer.getLong();
                break;

            case PacketString.DELIVERY_TIME:
                // Skip length. deliveryTime is a long
                len = buffer.getShort();
                deliveryTime = buffer.getLong();
                break;

            case PacketString.DELIVERY_COUNT:
                // Skip length. deliveryCount is a int 
                len = buffer.getShort();
                deliveryCount = buffer.getInt();
                break;

            case PacketString.DESTINATION:
            case PacketString.MESSAGEID:
            case PacketString.CORRELATIONID:
            case PacketString.REPLYTO:
            case PacketString.TYPE:
            case PacketString.DESTINATION_CLASS:
            case PacketString.REPLYTO_CLASS:
                len = buffer.getShort();

                int currentLimit = buffer.limit();
                int currentPosition = buffer.position();

                // Set limit so we can decode
                buffer.limit(currentPosition + len);
                try {
                    synchronized(decoder) {
                        stringItems[type] = decoder.decode(buffer).toString();
                    }
                } catch (CharacterCodingException e) {
                    // Should never get
                    System.out.println("Could not decode string " + e);
                }

                //reset limit
                buffer.limit(currentLimit);
                break;

             default:
                // Skip unknown field
                len = buffer.getShort();
                buffer.position(buffer.position() + len);
                break;
            }
            type = buffer.getShort();
        }

        bufferParsed = true;
        return;
    }

    /**
     * Update buffer to contain data held in class fields
     */
    private void updateBuffer()
        throws IOException {

	byte[] pad = new byte[4];	// Four nulls

        // ByteArrayOutputStream will grow buf if necessary.
        byte[] buf = new byte[512];
	JMQByteArrayOutputStream bos =
			new JMQByteArrayOutputStream(buf);

	DataOutputStream dos = new DataOutputStream(bos);

        // Make sure transactionID is first in buffer
	if (transactionID != 0) {
            writeLong(dos, PacketString.TRANSACTIONID, transactionID);
	}

	if (producerID != 0) {
            writeLong(dos, PacketString.PRODUCERID, producerID);
	}

	if (deliveryTime != 0L) {
            writeLong(dos, PacketString.DELIVERY_TIME, deliveryTime);
	}

	if (deliveryCount > 0) {
            writeInt(dos, PacketString.DELIVERY_COUNT, deliveryCount);
	}

        // Write string values to buffer. DESTINATION should be first
        for (int n = 0; n < PacketString.LAST; n++) {
            if (stringItems[n] != null) {
	        writeString(dos, n, stringItems[n]);
            }
        }

	//Teminate list
	dos.writeShort(PacketString.NULL);
	dos.flush();

	// Pad to nearest 32 bit boundary
        int padding = 4 - (bos.getCount() % 4);
        bos.write(pad, 0, padding);
	bos.flush();

        // Wrap a ByteBuffer around the streams backing buffer.
        buffer = ByteBuffer.wrap(bos.getBuf(), 0, bos.getCount());
        bufferDirty = false;

        // Since buffer matches fields we can set this to true
        bufferParsed = true;

	bos.close();
        dos.close();

	return;
    }

    /**
     * Write a header string item to the specified output stream
     */
    private void writeString(DataOutputStream dos, int type, String value)
	throws IOException {
	if (value != null) {
	    dos.writeShort(type);
	    dos.writeUTF(value);
	}
    }

    /**
     * Write a long field to the variable portion of the packet
     */
    private void writeLong(DataOutputStream dos, int type, long value)
	throws IOException {
	dos.writeShort(type);
        dos.writeShort(8);
	dos.writeLong(value);
    }

    /**
     * Write a int field to the variable portion of the packet
     */
    private void writeInt(DataOutputStream dos, int type, int value)
	throws IOException {
	dos.writeShort(type);
        dos.writeShort(4);
	dos.writeInt(value);
    }

}
