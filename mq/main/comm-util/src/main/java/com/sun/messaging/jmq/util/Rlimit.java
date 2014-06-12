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
 * @(#)Rlimit.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.util;


/**
 * Native class that provides an interface to Unix getrlimit(2).
 */
public class Rlimit {

    private static final String IMQ_NATIVE_LIBRARY = "imqutil";

    public final static int RLIMIT_CPU      = 0;    // cpu time in milliseconds
    public final static int RLIMIT_FSIZE    = 1;    // maximum file size
    public final static int RLIMIT_DATA     = 2;    // data size
    public final static int RLIMIT_STACK    = 3;    // stack size
    public final static int RLIMIT_CORE     = 4;    // core file size
    public final static int RLIMIT_NOFILE   = 5;    // file descriptors
    public final static int RLIMIT_VMEM     = 6;    // maximum mapped memory
    public final static int RLIMIT_AS       = RLIMIT_VMEM;
    public final static int RLIMIT_NLIMITS  = 7;    // number of resource limits

    public final static long RLIM_INFINITY  = -3;

    private static boolean loadFailed = true;

    static {
        try {
            System.loadLibrary(IMQ_NATIVE_LIBRARY);
            loadFailed = false;
        } catch (Throwable ex) {
            loadFailed = true;
        }
    }

    private static native Limits nativeGetRlimit(int resource);

    /**
     * Get Unix system resource limits.
     *
     * @param resource Resource to get limits for. Must be one of RLIMIT_*
     * constants.
     *
     * @return The soft and hard limits for the resource
     */
    public static Limits get(int resource) throws
        UnsupportedOperationException, IllegalArgumentException {

        if (loadFailed) {
            throw new UnsupportedOperationException();
        }

        if (resource < RLIMIT_CPU || resource == RLIMIT_NLIMITS) {
            throw new IllegalArgumentException(String.valueOf(resource));
        }

        Limits l = null;

        try {
            l = nativeGetRlimit(resource);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e.toString());
        }

        return l;
    }

    /*
     * Limits for a resource.
     */
    public static class Limits {
        /* soft limit */
        public long current;

        /* hard limit */
        public long maximum;
    }
}

