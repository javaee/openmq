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
 */ 

package com.sun.messaging.jmq.jmsserver.service.imq.grizzly;

import java.io.IOException;
import java.io.EOFException;
import java.io.StreamCorruptedException;
import org.glassfish.grizzly.Buffer; 
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.io.BigPacketException;
import com.sun.messaging.jmq.io.PacketPayload;
import com.sun.messaging.jmq.io.PacketVariableHeader;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.glassfish.grizzly.memory.Buffers;

public class GrizzlyMQPacket extends Packet {

    public GrizzlyMQPacket(boolean useDirect) {
        super(useDirect);
    }

    protected static int parsePacketSize(Buffer buf) throws IOException { 
        int magic   = buf.getInt();

        if (magic != MAGIC) {
            throw new StreamCorruptedException(
            "Bad packet magic number: " +magic+". Expecting: "+MAGIC);
        }
        
//        buf.getShort();
//        buf.getShort();
        buf.position(buf.position() + 4);
        return buf.getInt();
    }

    /**
    public void readPacket(Buffer buf) 
    throws IOException {

        if (writeInProgress) {
            // Should never happen
            throw new IOException("Can't read packet. Write in progress.");
        }
        if (destroyed) {
            throw new IOException("Packet has been destroyed");
        }

        reset();
  
//        buf.get(fixedBuf);
//        fixedBuf.rewind();
        final ByteBuffer bb = buf.toByteBuffer();
        final int pos = bb.position();
        final int lim = bb.limit();
        
        try {
            bb.limit(pos + HEADER_SIZE);
            parseFixedBuffer(bb);
        } finally {
            Buffers.setPositionLimit(bb, pos, lim);
            Buffers.setPositionLimit(buf, pos + HEADER_SIZE, lim);
        }

        if (packetSize > maxPacketSize) {
            //This packet is too large. Skip it.
            buf.position(packetSize-1);
            throw new BigPacketException("Packet size (" + packetSize +
                ") is greater than the maximum allowed packet size ("
                + maxPacketSize + "). Disgarding packet." );
        }

        initializeReadBufs(buf);
//        for (int i = 0; i < nBufs; i++) {
//            buf.get(readBufs[i]);
//        }

        packetVariableHeader.setBytes(varBuf);
        packetPayload.setPropertiesBytes(propBuf, version);
        packetPayload.setBody(bodyBuf);

        if (versionMismatch) {
            throw new IllegalArgumentException("Bad packet version number: " +
                version + ". Expecting: " + VERSION1 + " or " + VERSION2
                 + " or " + VERSION3);
        }
    }
    **/
    
    /**
     * Initialize the readBufs to be the proper size. This must be
     * called after the fixed header has been read and parsed.
     * Returns the number of buffers allocated
     */
    protected void initializeReadBufs(final Buffer buffer) {

        if (version != VERSION1 && version != VERSION2 && version != VERSION3) {
            // This is a packet version we don't understand. Set values
            // so we swallow rest of packet as the body
            propertyOffset = HEADER_SIZE;
            propertySize = 0;
            versionMismatch = true;
        }

        final ByteBuffer byteBuffer = buffer.toByteBuffer();
        final int position = byteBuffer.position();
        final int limit = byteBuffer.limit();
        
        try {

            // Now that we know the sizes we can allocate buffers to read
            // the rest of the packet.
            int size = 0;
            nBufs = 0;

            // Variable header buffer
            size = propertyOffset - HEADER_SIZE;
            if (size > 0) {
//            if (varBuf == null || varBuf.capacity() < size) {
//                varBuf = allocateBuffer(size);
//            } else {
//                varBuf.clear();
//                varBuf.limit(size);
//            }
//            readBufs[nBufs++] = varBuf;
                final int newLimit = byteBuffer.position() + size;
                byteBuffer.limit(newLimit);
                varBuf = byteBuffer.slice();

                Buffers.setPositionLimit(byteBuffer, newLimit, limit);
            }

            // Properties buffer
            size = propertySize;
            if (size > 0) {
//            if (propBuf == null || propBuf.capacity() < size) {
//                propBuf = allocateBuffer(size);
//            } else {
//                propBuf.clear();
//                propBuf.limit(size);
//            }
//            readBufs[nBufs++] = propBuf;

                final int newLimit = byteBuffer.position() + size;
                byteBuffer.limit(newLimit);
                propBuf = byteBuffer.slice();

                Buffers.setPositionLimit(byteBuffer, newLimit, limit);
            }

            // Body Buffer
            size = packetSize - propertyOffset - propertySize;
            if (size > 0) {
//            if (bodyBuf == null || bodyBuf.capacity() < size) {
//                bodyBuf = allocateBuffer(size);
//            } else {
//                bodyBuf.clear();
//                bodyBuf.limit(size);
//            }
//            readBufs[nBufs++] = bodyBuf;

                final int newLimit = byteBuffer.position() + size;
                byteBuffer.limit(newLimit);
                bodyBuf = byteBuffer.slice();

                Buffers.setPositionLimit(byteBuffer, newLimit, limit);
            }
        } finally {
            final int delta = byteBuffer.position() - position;
            Buffers.setPositionLimit(byteBuffer, position, limit);
            buffer.position(buffer.position() + delta);
        }
        // XXX 1/24/2002 dipol: Needed to work around nio bug (imq 4627557)
//        for (int i = 0; i < readBufs.length; i++) {
//            if (readBufs[i] != null) {
//                readBufsLimits[i] = readBufs[i].limit();
//            }
//        }
    }    

    @Override
    public void reset() {
	version        = VERSION3;
	magic          = MAGIC;
        packetType     = 0;
        packetSize     = 0;
        expiration     = 0;
        propertyOffset = 0;
        propertySize   = 0;
        encryption     = 0;
        priority       = 5;
        bitFlags       = 0;
        consumerID     = 0;
        transactionID  = 0;

        readInProgress = false;
        headerBytesRead = 0;
        ropBytesRead = 0;

        bufferDirty = false;

	sysMessageID.clear();

        if (fixedBuf != null) {
            fixedBuf.clear();
        } else {
            fixedBuf = allocateBuffer(HEADER_SIZE);
        }

        varBuf = null;
//        if (varBuf != null) {
//            varBuf.clear();
//        }

        propBuf = null;
//        if (propBuf != null) {
//            propBuf.clear();
//        }

        bodyBuf = null;
//        if (bodyBuf != null) {
//            bodyBuf.clear();
//        }

        if (packetVariableHeader != null) {
            ((GrizzlyMQPacketVariableHeader) packetVariableHeader).reset();
        } else {
            packetVariableHeader = new GrizzlyMQPacketVariableHeader();
        }

        if (packetPayload != null) {
            packetPayload.reset();
        } else {
            packetPayload = new PacketPayload();
        }
    }    
}
