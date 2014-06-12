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
 * @(#)Compressor.java	1.3 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.zip;

import java.util.zip.*;
import java.io.*;

import com.sun.messaging.jmq.jmsclient.Debug;

public class Compressor {

    private Deflater deflater = new Deflater();

    //private Deflater noWrapDeflater = new Deflater (Deflater.DEFAULT_COMPRESSION, true);

    private static Compressor compressor = new Compressor();

    private boolean debug = Boolean.getBoolean("imq.zip.debug");

    private Compressor () {
    }

    public static Compressor getInstance() {
        return compressor;
    }

    public synchronized int
    compress (byte[] body, int offset, int length, OutputStream os) throws IOException {

        int compressedLength = -1;

        Deflater def = getDefaultDeflater();

        DeflaterOutputStream defos = new DeflaterOutputStream(os, def);

        defos.write(body, offset, length);

        defos.finish();

        int totalIn = def.getTotalIn();
        compressedLength = def.getTotalOut();

        if (debug) {
            Debug.getPrintStream().println
                ("**** compressor total in: " + totalIn);

            Debug.getPrintStream().println
                ("**** compressor total out: " + compressedLength);
        }

        def.reset();

        return compressedLength;
    }

    private Deflater getDeflater(boolean noWrap, int strategy, int level) {

        Deflater def = null;

        if ( noWrap ) {
            //def = noWrapDeflater;
            throw new RuntimeException ("No wrap deflater is not Unsupported.");
        } else {
            def = deflater;
        }

        def.setStrategy( strategy );

        def.setLevel( level );

        return def;
    }

    private Deflater getDefaultDeflater() {
        return getDeflater (false, Deflater.DEFAULT_STRATEGY, Deflater.DEFAULT_COMPRESSION);
    }
}
