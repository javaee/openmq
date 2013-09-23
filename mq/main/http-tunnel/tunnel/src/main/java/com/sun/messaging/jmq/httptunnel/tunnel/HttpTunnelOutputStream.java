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
 * @(#)HttpTunnelOutputStream.java	1.3 06/28/07
 */ 

package com.sun.messaging.jmq.httptunnel.tunnel;

import java.io.*;
import com.sun.messaging.jmq.httptunnel.api.share.HttpTunnelDefaults;

/**
 * Provides an output stream for writing data to an HTTP tunnel
 * connection.
 */
public class HttpTunnelOutputStream extends OutputStream
    implements HttpTunnelDefaults {
    private HttpTunnelConnection conn = null;

    /**
     * Creates an OutputStream for a HttpTunnelSocket.
     */
    protected HttpTunnelOutputStream(HttpTunnelConnection conn) {
        this.conn = conn;
    }

    /**
     * Writes the specified byte to this output stream. The general 
     * contract for <code>write</code> is that one byte is written 
     * to the output stream. The byte to be written is the eight 
     * low-order bits of the argument <code>b</code>. The 24 
     * high-order bits of <code>b</code> are ignored.
     * <p>
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular, 
     *             an <code>IOException</code> may be thrown if the 
     *             output stream has been closed.
     */
    public synchronized void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) b;
        conn.writeData(buf);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this output stream. 
     * The general contract for <code>write(b, off, len)</code> is that 
     * some of the bytes in the array <code>b</code> are written to the 
     * output stream in order; element <code>b[off]</code> is the first 
     * byte written and <code>b[off+len-1]</code> is the last byte written 
     * by this operation.
     * <p>
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular, 
     *             an <code>IOException</code> is thrown if the output 
     *             stream is closed.
     */
    public synchronized void write(byte b[], int off, int len)
        throws IOException {
        byte[] buf;

        while (len > 0) {
            if (len > MAX_PACKETSIZE)
                buf = new byte[MAX_PACKETSIZE];
            else
                buf = new byte[len];

            System.arraycopy(b, off, buf, 0, buf.length);
            conn.writeData(buf);

            off += buf.length;
            len -= buf.length;
        }
    }

    /**
     * Closes this output stream and releases any system resources associated
     * with the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void close() throws IOException {
    }
}

/*
 * EOF
 */
