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
 * @(#)Packet.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker.fullyconnected;

import java.io.*;

/**
 * This class encapsulates the packet format for standard
 * fully connected broker topology.
 */
class Packet {
    private static final short VERSION = 100;
    private static final int HEADER_SIZE = 16;

    /* packetType */
    public static final int UNICAST = 1;
    public static final int BROADCAST = 2;
    public static final int BROKER_INFO = 3;
    public static final int LINK_INIT = 4;
    public static final int STOP_FLOW = 5;
    public static final int RESUME_FLOW = 6;
    public static final int PING = 7;
    public static final int BROKER_INFO_REPLY = 9;

    /* bitFlags */
    public static final int USE_FLOW_CONTROL = 0x0001;

    private short version = VERSION;
    private short packetType = 0;
    private int packetSize = 0;
    private int destId = 0;
    private int bitFlags = 0;

    private byte[] packetBuffer = null;

    public void readPacket(InputStream is)
        throws IOException, EOFException {

        DataInputStream dis = new DataInputStream(is);

        version = dis.readShort();
        packetType = dis.readShort();
        packetSize = dis.readInt();
        destId = dis.readInt();
        bitFlags = dis.readInt();

        try {
            packetBuffer = new byte[packetSize - HEADER_SIZE];
        }
        catch (OutOfMemoryError oom) {
            dis.skip(packetSize - HEADER_SIZE);
            throw oom;
        }
        dis.readFully(packetBuffer);
    }

    public void writePacket(OutputStream os)
        throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(version);
        dos.writeShort(packetType);
        dos.writeInt(packetSize);
        dos.writeInt(destId);
        dos.writeInt(bitFlags);
        dos.flush();
        bos.flush();

        byte[] headerBuffer = bos.toByteArray();

        os.write(headerBuffer, 0, HEADER_SIZE);
        if (packetBuffer != null)
            os.write(packetBuffer, 0, packetSize - HEADER_SIZE);
        os.flush();
    }

    public int getPacketType() {
        return packetType;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getDestId() {
        return destId;
    }

    public byte[] getPacketBody() {
        return packetBuffer;
    }

    public boolean getFlag(int flag) {
        return ((bitFlags & flag) == flag);
    }

    public void setPacketType(int packetType) {
        this.packetType = (short) packetType;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public void setPacketBody(byte[] data) {
        packetBuffer = data;
        packetSize = HEADER_SIZE;
        if (packetBuffer != null)
            packetSize += packetBuffer.length;
    }

    public void setFlag(int flag, boolean on) {
        if (on)
            bitFlags = bitFlags | flag;
        else
            bitFlags = bitFlags & ~flag;
    }

    public String toString() {
        return "PacketType = " + packetType +
            ", DestId = " + destId + ", DATA :\n" +
            hexdump(packetBuffer, 128);
    }

    public static String hexdump(byte[] buffer, int maxlen) {
        if (buffer == null)
            return "";

        int addr = 0;
        int buflen = buffer.length;
        if (buflen > maxlen)
            buflen = maxlen;

        StringBuffer ret = new StringBuffer(buflen);

        while (buflen > 0) {
            int count = buflen < 16 ? buflen : 16;
            ret.append("\n" + i2hex(addr, 6, "0"));

            String tmp = "";

            int i;
            for (i = 0; i < count; i++) {
                int b = (int) buffer[addr + i];

                if (i == 8)
                    ret.append("-");
                else
                    ret.append(" ");
                ret.append(i2hex(b, 2, "0"));
                if (b >= 32 && b < 128)
                    tmp = tmp + ((char) b);
                else
                    tmp = tmp + ".";
            }
            for (; i < 16; i++)
                ret.append("   ");

            ret.append("   " + tmp);

            addr += count;
            buflen -= count;
        }
        return ret.append("\n").toString();
    }

    public static String i2hex(int i, int len, String filler) {
        String str = Integer.toHexString(i);
        if (str.length() == len)
            return str;
        if (str.length() > len)
            return str.substring(str.length() - len);
        while (str.length() < len)
            str = filler + str;
        return str;
    }

    public static String getPacketTypeString(int type) { 
        switch(type) {
            case BROKER_INFO: return "BROKER_INFO";
            case LINK_INIT:   return "LINK_INIT";
            case STOP_FLOW:   return "STOP_FLOW";
            case RESUME_FLOW: return "RESUME_FLOW";
            case PING:        return "PING";
            case BROKER_INFO_REPLY: return "BROKER_INFO_REPLY";
            default: return "UNKNOWN";
        }
    }

}

/*
 * EOF
 */
