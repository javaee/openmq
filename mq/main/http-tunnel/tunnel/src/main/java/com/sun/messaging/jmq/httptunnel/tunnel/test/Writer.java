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
 * @(#)Writer.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.test;

import java.io.*;
import java.util.Random;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;

class Writer extends Thread {
    private HttpTunnelSocket s = null;
    private OutputStream os = null;

    private static int SLEEP =
        Integer.getInteger("test.sleep", 0).intValue();
    private static int DATASIZE =
        Integer.getInteger("test.datasize", 32768).intValue();
    private static int VERBOSITY =
        Integer.getInteger("test.verbosity", 0).intValue();
    private static int MAX =
        Integer.getInteger("test.max", -1).intValue();
    private static int PULLPERIOD =
        Integer.getInteger("test.pullperiod", -1).intValue();

    public Writer(HttpTunnelSocket s) {
        this.s = s;
        try {
            s.setPullPeriod(PULLPERIOD);
            this.os = s.getOutputStream();;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(os, 8192);
            ObjectOutputStream dos = new ObjectOutputStream(bos);

            int n = 0;
            Random r = new Random();
            while (MAX < 0 || n < MAX) {
                RandomBytes rb = new RandomBytes(DATASIZE);
                rb.setSequence(n);

                dos.writeObject(rb);
                dos.flush();
                dos.reset();
                if (SLEEP > 0)
    //                Thread.sleep(r.nextInt(SLEEP) * 1000);
                    Thread.sleep((int)(r.nextFloat() * 1000));

                n++;
                if (VERBOSITY > 0)
                    System.out.println("#### Sent packet #" + n);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("#### Writer exiting...");
    }
}

/*
 * EOF
 */
