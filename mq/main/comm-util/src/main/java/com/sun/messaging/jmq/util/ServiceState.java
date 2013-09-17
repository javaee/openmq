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
 * @(#)ServiceState.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.util;

public class ServiceState
{
    public static final int UNKNOWN = -1;
    public static final int UNINITIALIZED = 0;
    public static final int INITIALIZED = 1;
    public static final int STARTED = 2;
    public static final int RUNNING = 3;
    public static final int PAUSED = 4;
    public static final int SHUTTINGDOWN = 5;
    public static final int STOPPED = 6;
    public static final int DESTROYED = 7;
    public static final int QUIESCED = 8;

    public static int getStateFromString(String str) {
        if (str.equals("UNINITIALIZED"))
            return UNINITIALIZED;
        if (str.equals("INITIALIZED")) 
            return INITIALIZED;
        if (str.equals("STARTED")) 
            return STARTED;
        if (str.equals("RUNNING") )
            return RUNNING;
        if (str.equals("PAUSED")) 
            return PAUSED;
        if (str.equals("SHUTTINGDOWN") )
            return SHUTTINGDOWN;
        if (str.equals("STOPPED") )
            return STOPPED;
        if (str.equals("DESTROYED") )
            return DESTROYED;
        if (str.equals("QUIESCED") )
            return QUIESCED;
        return UNKNOWN;
    }


    public static String getString(int state)
    {
        switch (state) {
            case UNINITIALIZED:
                return "UNINITIALIZED";

            case INITIALIZED:
                return "INITIALIZED";

            case STARTED:
                return "STARTED";

            case RUNNING:
                return "RUNNING";

            case PAUSED:
                return "PAUSED";

            case SHUTTINGDOWN:
                return "SHUTTINGDOWN";

            case STOPPED:
                return "STOPPED";

            case DESTROYED:
                return "DESTROYED";

            case QUIESCED:
                return "QUIESCED";

        }
        return "UNKNOWN";

    }
}
