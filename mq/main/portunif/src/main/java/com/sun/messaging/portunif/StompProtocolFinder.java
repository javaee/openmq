/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.portunif;

import java.nio.charset.Charset;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.portunif.PUContext;
import org.glassfish.grizzly.portunif.ProtocolFinder;
import org.glassfish.grizzly.filterchain.FilterChainContext;


public class StompProtocolFinder implements ProtocolFinder {

    private static boolean DEBUG = false;

    private byte[] cmd = null;

    public StompProtocolFinder() {
        this.cmd = "CONNECT".getBytes(Charset.forName("UTF-8"));
    }

    public StompProtocolFinder(byte[] cmd) {
        this.cmd = cmd;
    }

    /**
     */
    @Override
    public Result find(final PUContext puContext, final FilterChainContext ctx) {

        final Buffer input = ctx.getMessage();
        if (DEBUG) {
            System.out.println(this+": input="+input.toStringContent());
        }

        int pos = input.position();
        int len = input.remaining();
        if (len <= cmd.length) {
            return Result.NEED_MORE_DATA;
        }
        for (int i = 0; i < cmd.length; i++) {
            if (cmd[i] != input.get(i)) {
                return Result.NOT_FOUND;
            }
        }
        byte b = input.get(cmd.length);
        if (b != '\n' && b != '\r') {
            return Result.NEED_MORE_DATA;
        }
        input.position(pos);
        if (DEBUG) {
            System.out.println(this+": FOUND input="+input.toStringContent());
        }
        return Result.FOUND;
    }

}

