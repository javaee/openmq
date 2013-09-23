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
 * @(#)SizeString.java	1.7 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;


/**
 * This is an object which represents a String which represents
 * bytes in the format of:
 *
 *    #[bkm] where:
 *     128 -> 128 Kbytes
 *     128b -> 128 bytes
 *     128k -> 128 kbytes
 *     128m -> 128 Mbytes
 */ 
public class SizeString implements Serializable
{
    private static final long K = 1024;
    private static final long M = 1024*1024;
    private static final long B = 1;

    String str = null;
    long bytes = 0;
    public SizeString(String str)
        throws NumberFormatException
    {
        setString(str);
    }

    public SizeString()
        throws NumberFormatException
    {
        setString("0b");
    }

    public SizeString(long newKbytes)
    {
        setKBytes(newKbytes);
    }


    public void setString(String setstr)
        throws NumberFormatException
    {
        this.str = setstr;
        long multiplier = B;
        if (str == null) {
            this.str = null;
            bytes = 0;
            return;
        }
        if (Character.isLetter(setstr.charAt(str.length() -1))) {
            char multchar = setstr.charAt(str.length() -1);
            setstr = str.substring(0,str.length() -1);
            switch (multchar) {
                case 'm':
                case 'M':
                    multiplier = M;
                    break;

                case 'k':
                case 'K':
                    multiplier = K;
                    break;

                case 'b':
                case 'B':
                    multiplier = B;
                    break;

                default:
                    throw new NumberFormatException("Unknown size " + multchar);
             }
        }
        int val = (new Integer(setstr)).intValue();
        bytes = val * multiplier;
 
    }

    public String getString()
    {
        return str;
    }

    public String getByteString()
    {
        return bytes + "b";
    }

    public String getKByteString()
    {
        return getKBytes() + "K";
    }

    public String getMByteString()
    {
        return getMBytes() + "M";
    }

    public void setKBytes(long newKbytes) {
        this.str = String.valueOf(newKbytes) + "K";
        bytes = newKbytes*K;
    }

    public void setMBytes(long newMbytes) {
        this.str = String.valueOf(newMbytes) + "M";
        bytes = newMbytes*M;
    }

    public void setBytes(long newbytes) {
        this.str = String.valueOf(newbytes) + "b";
        bytes = newbytes*B;
    }

    public long getBytes() {
        return bytes;
    }

    public long getKBytes() {
        return (bytes == 0) ? 0 : bytes/K;
    }

    public long getMBytes() {
        return (bytes == 0) ? 0 : bytes/M;
    }

    public String toString() {
        return getString();
    }
    
    public static void main(String args[])
    {
        try {
	    System.err.println("## 100b");
            System.err.println((new SizeString("100b")).toString());
            System.err.println((new SizeString("100b")).getByteString());
            System.err.println((new SizeString("100b")).getKByteString());
            System.err.println((new SizeString("100b")).getMByteString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         try {
	    System.err.println("## 100k");
            System.err.println((new SizeString("100k")).toString());
            System.err.println((new SizeString("100k")).getByteString());
            System.err.println((new SizeString("100k")).getKByteString());
            System.err.println((new SizeString("100k")).getMByteString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
	    System.err.println("## 100m");
            System.err.println((new SizeString("100m")).toString());
            System.err.println((new SizeString("100m")).getByteString());
            System.err.println((new SizeString("100m")).getKByteString());
            System.err.println((new SizeString("100m")).getMByteString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            (new SizeString("100B")).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         try {
            (new SizeString("100K")).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            (new SizeString("100M")).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
          try {
            (new SizeString("100")).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
           try {
            (new SizeString("100L")).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
       
    }
}
