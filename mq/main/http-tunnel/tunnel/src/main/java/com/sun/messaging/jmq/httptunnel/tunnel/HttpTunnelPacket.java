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

/*
 * @(#)HttpTunnelPacket.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelDefaults;


/**
 * This class encapsulates the HTTP tunnel packet.
 */
public class HttpTunnelPacket implements HttpTunnelDefaults {
    protected static final short VERSION = 100;
    protected static final int HEADER_SIZE = 24;
    protected short version = VERSION;
    protected short packetType = 0;
    protected int packetSize = 0;
    protected int connId = 0;
    protected int sequence = 0;
    protected short winsize = 0;
    protected short reserved = 0;
    protected int checksum = 0;
    protected byte[] headerBuffer = new byte[HEADER_SIZE];
    protected byte[] packetBuffer = null;
    protected boolean dirty = false;

    private void parseHeader(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);

        version = dis.readShort();

        if (version > VERSION) {
            throw new IllegalStateException("Bad response format. " +
                "Check the tunnel servlet URL.");
        }

        packetType = dis.readShort();
        packetSize = dis.readInt();
        connId = dis.readInt();
        sequence = dis.readInt();
        winsize = dis.readShort();
        reserved = dis.readShort();
        checksum = dis.readInt();
    }

    private void updateBuffers() throws IOException {
        if (!dirty) {
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeShort(version);
        dos.writeShort(packetType);
        dos.writeInt(packetSize);
        dos.writeInt(connId);
        dos.writeInt(sequence);
        dos.writeShort(winsize);
        dos.writeShort(reserved);
        dos.writeInt(checksum);

        dos.flush();
        bos.flush();

        headerBuffer = bos.toByteArray();

        dirty = false;
    }

    /**
     * Read a packet from the given InputStream.
     */
    public void readPacket(InputStream is) throws IOException, EOFException {
        DataInputStream dis = new DataInputStream(is);
        dis.readFully(headerBuffer);

        parseHeader(new ByteArrayInputStream(headerBuffer));

        packetBuffer = new byte[packetSize - HEADER_SIZE];
        dis.readFully(packetBuffer);
    }

    /**
     * Write a packet to the given OutputStream.
     */
    public void writePacket(OutputStream os) throws IOException {
        updateBuffers();

        os.write(headerBuffer, 0, HEADER_SIZE);

        if (packetBuffer != null) {
            os.write(packetBuffer, 0, packetSize - HEADER_SIZE);
        }

        os.flush();
    }

    public int getPacketType() {
        return packetType;
    }

    public byte[] getPacketBody() {
        return packetBuffer;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getPacketDataSize() {
        return packetSize - HEADER_SIZE;
    }

    public int getConnId() {
        return connId;
    }

    public int getSequence() {
        return sequence;
    }

    public int getWinsize() {
        return winsize;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setPacketType(int packetType) {
        this.packetType = (short) packetType;
        dirty = true;
    }

    public void setPacketBody(byte[] data) {
        packetBuffer = data;
        packetSize = HEADER_SIZE;

        if (packetBuffer != null) {
            packetSize += packetBuffer.length;
        }

        dirty = true;
    }

    public void setConnId(int connId) {
        this.connId = connId;
        dirty = true;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
        dirty = true;
    }

    public void setWinsize(int winsize) {
        this.winsize = (short) winsize;
        dirty = true;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
        dirty = true;
    }

    public String toString() {
        String ret = " HttpTunnelPacket [ Version = " + version + "," +
            " packetType = " + getPacketTypeStr() + "," + " packetSize = " +
            packetSize + "," + " connId = " + connId + "," + " sequence = " +
            sequence + "," + " winsize = " + winsize + "," + " reserved = " +
            reserved + "," + " checksum = " + checksum + "]";

        if (packetBuffer != null) {
            ret = ret + " [ DATA = \"" + new String(packetBuffer) + "\"]";
        }

        return ret;
    }

    private String getPacketTypeStr() {
        switch (packetType) {
        case CONN_ABORT_PACKET:
            return "CONN_ABORT_PACKET";

        case CONN_CLOSE_PACKET:
            return "CONN_CLOSE_PACKET";

        case CONN_INIT_ACK:
            return "CONN_INIT_ACK";

        case CONN_INIT_PACKET:
            return "CONN_INIT_PACKET";

        case CONN_OPTION_PACKET:
            return "CONN_OPTION_PACKET";

        case CONN_REJECTED:
            return "CONN_REJECTED";

        case CONN_SHUTDOWN:
            return "CONN_SHUTDOWN";

        case DATA_PACKET:
            return "DATA_PACKET";

        case ACK:
            return "ACK";

        case LINK_INIT_PACKET:
            return "LINK_INIT_PACKET";

        case LISTEN_STATE_PACKET:
            return "LISTEN_STATE_PACKET";

        default:
            return String.valueOf(packetType);
        }
    }
}

/*
 * EOF
 */
