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
 * %W% %G%
 */ 

package com.sun.messaging.jmq.io;

/**
 * This class defines the JMQ packet bit flags and provides some
 * convenience routines.
 */
public class PacketFlag {

    public static final int Q_FLAG =  0x0001;
    public static final int R_FLAG =  0x0002;
    public static final int P_FLAG =  0x0004;
    public static final int S_FLAG =  0x0008;
    public static final int A_FLAG =  0x0010;
    public static final int L_FLAG =  0x0020;
    public static final int F_FLAG =  0x0040;
    public static final int T_FLAG =  0x0080;
    public static final int C_FLAG =  0x0100;
    public static final int B_FLAG =  0x0200;
    public static final int Z_FLAG =  0x0400;
    public static final int I_FLAG =  0x0800;
    public static final int W_FLAG =  0x1000;

    /**
     * Return a human readable string describing the bits set in "flags"
     */
    public static String getString(int flags) {
	String s =
	    ((flags & A_FLAG) == A_FLAG ? "A" : "") +
	    ((flags & S_FLAG) == S_FLAG ? "S" : "") +
	    ((flags & P_FLAG) == P_FLAG ? "P" : "") +
	    ((flags & R_FLAG) == R_FLAG ? "R" : "") +
	    ((flags & Q_FLAG) == Q_FLAG ? "Q" : "") +
	    ((flags & L_FLAG) == L_FLAG ? "L" : "") +
	    ((flags & F_FLAG) == F_FLAG ? "F" : "") +
	    ((flags & T_FLAG) == T_FLAG ? "T" : "") +
	    ((flags & B_FLAG) == B_FLAG ? "B" : "") +
	    ((flags & Z_FLAG) == Z_FLAG ? "Z" : "") +
	    ((flags & C_FLAG) == C_FLAG ? "C" : "") +
	    ((flags & I_FLAG) == I_FLAG ? "I" : "") +
	    ((flags & W_FLAG) == W_FLAG ? "W" : "");

        return s;
    }

}
