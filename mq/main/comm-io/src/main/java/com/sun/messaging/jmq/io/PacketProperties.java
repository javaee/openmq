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
 * @(#)PacketProperties.java	1.7 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;

/*
 * Only the follow property types are supported:
 *    Boolean, Byte, Short, Integer, Long, Float, Double, and String
 *
 * Format:
 *     [Name length][Name (UTF-8)][Value type][Value Length][Value]
 *
 *    Pad out to 32 bit boundry
 * 
 */   
public class PacketProperties 
{
    public static final short BOOLEAN = 1; 
    public static final short BYTE = 2;
    public static final short SHORT = 3;
    public static final short INTEGER = 4;
    public static final short LONG = 5;
    public static final short FLOAT = 6;
    public static final short DOUBLE = 7;
    public static final short STRING = 8;
    public static final short OBJECT = 9;

    public static final int VERSION1=1;

    // add OBJECT

    // add version comment

    public static void write(Map map, OutputStream os) 
            throws IOException
    {
        if (map == null) {
            return;
        }
        DataOutputStream dos = new DataOutputStream(os);

        dos.writeInt(VERSION1);
        dos.writeInt(map.size());
        Iterator<Map.Entry> itr = map.entrySet().iterator();
        Map.Entry pair = null;
        String key = null;
        Object value = null;
        while (itr.hasNext()) {
            pair = itr.next();
            key = (String)pair.getKey();
            value = pair.getValue();
            dos.writeUTF(key);
            if (value instanceof Boolean) {
                dos.writeShort(BOOLEAN);
                dos.writeBoolean(((Boolean)value).booleanValue());
            } else if (value instanceof Byte) {
                dos.writeShort(BYTE);
                dos.writeByte(((Byte)value).byteValue());
            } else if (value instanceof Short) {
                dos.writeShort(SHORT);
                dos.writeShort(((Short)value).shortValue());
            } else if (value instanceof Integer) {
                dos.writeShort(INTEGER);
                dos.writeInt(((Integer)value).intValue());
            } else if (value instanceof Long) {
                dos.writeShort(LONG);
                dos.writeLong(((Long)value).longValue());
            } else if (value instanceof Float) {
                dos.writeShort(FLOAT);
                dos.writeFloat(((Float)value).floatValue());
            } else if (value instanceof Double) {
                dos.writeShort(DOUBLE);
                dos.writeDouble(((Double)value).doubleValue());
            } else if (value instanceof String) {
                dos.writeShort(STRING);
                dos.writeUTF((String)value);
            } else {
                dos.writeShort(OBJECT);
                JMQByteArrayOutputStream bos = 
                    new JMQByteArrayOutputStream(new byte[256]);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(value);
                oos.close();
                byte[] data = bos.getBuf();
                dos.writeInt(data.length);
                dos.write(data, 0, data.length);
            }
        }      
    }

    public static Hashtable parseProperties(InputStream is) 
        throws IOException, ClassNotFoundException
    {
        DataInputStream dis = new DataInputStream(is);

        int version = dis.readInt();
        if (version != VERSION1) {
            throw new IOException("Unsupported version of properties serialization ["
                    + version + "]");
        }
        int propcnt = dis.readInt();
        Hashtable ht = new Hashtable(propcnt);

        int cnt = 0;
        while (cnt < propcnt) {
            String key = dis.readUTF();
            if (key.length() <= 0) break;

            short type = dis.readShort();

            Object value = null;
            switch( type) {
                case BOOLEAN:
                    //value = new Boolean(dis.readBoolean());
                    value = Boolean.valueOf (dis.readBoolean());
                    break;
                case BYTE:
                    value = Byte.valueOf(dis.readByte());
                    break;
                case SHORT:
                    value = Short.valueOf(dis.readShort());
                    break;
                case INTEGER:
                    value = Integer.valueOf(dis.readInt());
                    break;
                case LONG:
                    value = Long.valueOf(dis.readLong());
                    break;
                case FLOAT:
                    value = Float.valueOf(dis.readFloat());
                    break;
                case DOUBLE:
                    value = Double.valueOf(dis.readDouble());
                    break;
                case STRING:
                    value = dis.readUTF();
                    break;
                case OBJECT:
                    int bytes = dis.readInt();
                    byte[] buf = new byte[bytes];
                    dis.read(buf,0,bytes);
                    JMQByteArrayInputStream bis = 
                        new JMQByteArrayInputStream(buf);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    value = ois.readObject();
                    ois.close();
                    bis.close();
                default:
                      // ignore (dont throw exception)
            }
            ht.put(key, value);
            cnt ++;
        }      
        
        return ht;
    }



    
}
