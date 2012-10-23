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
 * @(#)debugutil.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.test;

import java.io.*;

class debugutil {
    public static byte[] readFile(String fname) throws Exception {
        File f = new File(fname);
        long len = f.length();
        byte[] content = new byte[(int) len];

        FileInputStream fis = new FileInputStream(f);
        fis.read(content);
        return content;
    }

    public static String i2hex(int i) {
        return i2hex(i, -1, " ");
    }

    public static String i2hex(int i, int len) {
        return i2hex(i, len, " ");
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

    public static String hexdump(byte[] buffer) {
        String ret = "";
        int addr = 0;
        int buflen = buffer.length;

        while (buflen > 0) {
            int count = buflen < 16 ? buflen : 16;
            ret = ret + "\n" + i2hex(addr, 6, "0");

            String tmp = "";

            int i;
            for (i = 0; i < count; i++) {
                byte b = buffer[addr + i];

                if (i == 8)
                    ret = ret + "-";
                else
                    ret = ret + " ";
                ret = ret + i2hex(b, 2, "0");
                if (b >= 32 && b < 128)
                    tmp = tmp + ((char) b);
                else
                    tmp = tmp + ".";
            }
            for (; i < 16; i++)
                ret = ret + "   ";

            ret = ret + "   " + tmp;

            addr += count;
            buflen -= count;
        }
        return ret + "\n";
    }

    public static void main(String args[]) throws Exception {
        byte[] hex = readFile(args[0]);
        System.out.println(hexdump(hex));
    }
}

/*
 * EOF
 */
