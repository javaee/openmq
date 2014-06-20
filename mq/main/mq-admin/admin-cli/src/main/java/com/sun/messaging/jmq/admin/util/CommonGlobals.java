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
 */ 

package com.sun.messaging.jmq.admin.util;

import java.io.*;
import com.sun.messaging.jmq.Version;

/**
 * abstract class to be extended, contains commonly used globals
 *
 */
public abstract class CommonGlobals
{
    private static Version version = null;

    private static boolean silentMode = false;

  
    public static Version getVersion()  {
	if (version == null)  {
	    version = new Version(false);
	}

	return (version);
    }


    /*---------------------------------------------
     * Global error printing methods.
     *---------------------------------------------*/

    public static void setSilentMode(boolean mode) {
	silentMode = mode;
    }

    public static void stdErrPrintln(String msg) {
        doPrintln(System.err, msg, false);
    }

    public static void stdErrPrintln(String msg, boolean exit) {
        doPrintln(System.err, msg, exit);
    }

    public static void stdErrPrintln(String type, String msg) {
        doPrintln(System.err, type + " " + msg, false);
    }

    public static void stdErrPrintln(String type, String msg, boolean exit) {
        doPrintln(System.err, type + " " + msg, exit);
    }

    public static void stdOutPrintln(String msg) {
        doPrintln(System.out, msg, false);
    }

    public static void stdOutPrintln(String msg, boolean exit) {
        doPrintln(System.out, msg, exit);
    }

    public static void stdOutPrintln(String type, String msg) {
        doPrintln(System.out, type + " " + msg, false);
    }

    public static void stdOutPrintln(String type, String msg, boolean exit) {
        doPrintln(System.out, type + " " + msg, exit);
    }

    public static void stdErrPrint(String msg) {
        doPrint(System.err, msg, false);
    }

    public static void stdErrPrint(String msg, boolean exit) {
        doPrint(System.err, msg, exit);
    }

    public static void stdErrPrint(String type, String msg) {
        doPrint(System.err, type + " " + msg, false);
    }

    public static void stdErrPrint(String type, String msg, boolean exit) {
        doPrint(System.err, type + " " + msg, exit);
    }

    public static void stdOutPrint(String msg) {
        doPrint(System.out, msg, false);
    }

    public static void stdOutPrint(String msg, boolean exit) {
        doPrint(System.out, msg, exit);
    }

    public static void stdOutPrint(String type, String msg) {
        doPrint(System.out, type + " " + msg, false);
    }

    public static void stdOutPrint(String type, String msg, boolean exit) {
        doPrint(System.out, type + " " + msg, exit);
    }

    private static void doPrintln(PrintStream out, String msg, boolean exit) {
	if (silentMode)  {
	    return;
	}

        out.println(msg);
	if (exit)
	    System.exit(1);
    }

    private static void doPrint(PrintStream out, String msg, boolean exit) {
	if (silentMode)  {
	    return;
	}

        out.print(msg);
	if (exit)
	    System.exit(1);
    }
}

