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
 * @(#)RandomBytes.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.test;

import java.io.*;
import java.util.Random;

class RandomBytes implements Serializable {
    private byte[] data = null;
    private int sequence = 0;

    public static byte computeChecksum(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += (data[i] & 0xff);
            if (sum > 255)
                sum = (sum & 0xff) + 1;
        }
        return (byte) ~((sum & 0xff));
    }

    public RandomBytes(int maxlen) {
        Random r = new Random();

        //int len = r.nextInt(maxlen) + 1;
        int len = (int)(r.nextFloat()*maxlen) + 1;
        data = new byte[len];

        data[0] = 0;
        for (int i = 1; i < len; i++) {
            //data[i] = (byte) (32 + r.nextInt(96));
            data[i] = (byte) (32 + (int)(r.nextFloat()*96));
        }
        data[0] = computeChecksum(data);
    }

    public RandomBytes(String str) {
        byte[] in = str.getBytes();
        data = new byte[in.length + 1];
        data[0] = 0;
        System.arraycopy(in, 0, data, 1, in.length);
        data[0] = computeChecksum(data);
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public byte getChecksum() {
        return data[0];
    }

    public byte[] getData() {
        return data;
    }

    public boolean isValid() {
        return (computeChecksum(data) == 0);
    }

    public static void main(String args[]) {
        int maxlen = 64;
        if (args.length > 0) {
            try {
                maxlen = Integer.parseInt(args[0]);
            }
            catch (Exception e) {
                maxlen = -1;
            }
        }
        RandomBytes rb;
        if (maxlen < 0)
            rb = new RandomBytes(args[0]);
        else
            rb = new RandomBytes(maxlen);

        byte[] tmp = rb.getData();
        int len = tmp.length;
        System.out.println("Bytes = " + new String(tmp, 1, len - 1));
        System.out.println("Length = " + (len - 1));
        System.out.println("Checksum = " + rb.getChecksum());
        System.out.println("Computed checksum = " +
            computeChecksum(tmp));
        System.out.println("rb.isValid() = " + rb.isValid());
    }
}

/*
 * EOF
 */
