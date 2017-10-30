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
 * @(#)Reader.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel.test;

import java.io.InputStream;
import java.io.ObjectInputStream;

import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelSocket;
import com.sun.messaging.jmq.util.io.FilteringObjectInputStream;

class Reader extends Thread {
    private HttpTunnelSocket s = null;
    private InputStream is = null;
    private static int VERBOSITY =
        Integer.getInteger("test.verbosity", 0).intValue();
    private static int MAX =
        Integer.getInteger("test.max", -1).intValue();
    private static int PULLPERIOD =
        Integer.getInteger("test.pullperiod", -1).intValue();

    public Reader(HttpTunnelSocket s) {
        this.s = s;
        try {
            s.setPullPeriod(PULLPERIOD);
            this.is = s.getInputStream();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            ObjectInputStream dis = new FilteringObjectInputStream(is); 
            int n = 0;

            while (MAX < 0 || n < MAX) {
                RandomBytes rb = (RandomBytes) dis.readObject();
                boolean valid = rb.isValid();

                int seq = rb.getSequence();

                if (seq != n) {
                    System.out.println(
                        "#### PACKET OUT OF SEQUENCE ####");
                    return;
                }

                if (VERBOSITY > 0) {
                    System.out.println("#### Received packet #" + seq);
                }

                if (VERBOSITY > 1) {
                    byte[] tmp = rb.getData();
                    int len = tmp.length > 64 ? 64 : tmp.length;

                    System.out.println("Bytes = " +
                        new String(tmp, 1, len - 1));
                    System.out.println("Length = " + (tmp.length - 1));
                    System.out.println("Checksum = " + rb.getChecksum());
                    System.out.println("Computed checksum = " +
                        RandomBytes.computeChecksum(tmp));
                    System.out.println("rb.isValid() = " + valid);
                    System.out.println();
                }

                if (! valid) {
                    System.out.println(
                        "#### CHECKSUM ERROR DETECTED ####");
                    return;
                }

                n++;
                if (n % 100 == 0) {
                    System.out.println("#### Free memory = " +
                        Runtime.getRuntime().freeMemory());
                    System.out.println("#### Total memory = " +
                        Runtime.getRuntime().totalMemory());
                    System.out.println();
                }
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
        System.out.println("#### Reader exiting...");
    }
}

/*
 * EOF
 */
