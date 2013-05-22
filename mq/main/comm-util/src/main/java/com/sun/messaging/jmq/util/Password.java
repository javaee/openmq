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
 * @(#)Password.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;
import java.util.Arrays;
import java.lang.reflect.Method;

public class Password  { 

    private static boolean DEBUG = Boolean.getBoolean(
            "imq.debug.com.sun.messaging.jmq.util.Password");
    private static boolean useNative = false;

    private static final String library ="imqutil";
    private native String getHiddenPassword();

    public Password() {
    }

    public boolean echoPassword() {
        return (!hasJavaConsole() && !useNative); 
    }

    private boolean hasJavaConsole() {
        try {
            Class consolec =  Class.forName("java.io.Console");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private String getPasswordFromJavaConsole() {
        if (DEBUG) {
            System.err.println("use java.io.Console");
        }
        try {
            Class consolec =  Class.forName("java.io.Console");
            Method sysm = System.class.getMethod("console", (Class[])null);
            Method consolem = consolec.getMethod("readPassword", (Class[])null);
            Object console = sysm.invoke(null, (Object[])null);
            if (console == null) {
                throw new Exception("Console not available");
            }
            char[] password = (char[])consolem.invoke(console, (Object[])null);
            if (password == null) {
                return null;
            } 
            String pw = new String(password);
            Arrays.fill(password, ' ');
            password = null;
            return pw;
        } catch (Throwable e) {
            if (DEBUG) e.printStackTrace();
            return null;
        }
    }

    private String getClearTextPassword()  {
	String s = null;

	try  {
	    BufferedReader in;

	    in = new BufferedReader(new InputStreamReader(System.in));
	    s = in.readLine();
	} catch (IOException exc)  {
	    System.err.println("Caught exception when reading passwd: " + exc);
	}

	return (s);
    }

    // We should call this guy, since no one else needs to know
    // that this call is system-dependent.
    public String getPassword() {
        if (hasJavaConsole()) {
            return getPasswordFromJavaConsole();
        } 
        if (useNative) {
            return getHiddenPassword();
        }
        return getClearTextPassword();
    }

    static  {
        try {
    	    System.loadLibrary(library);
            useNative = true;
	} catch (Throwable ex) {
            useNative = false;
	}
    }


    public static void main(String[] args)  {
	Password pw;
	boolean	 clearText = false;
	boolean	 normal = false;

	if (args.length > 0)  {
	    if (args[0].equalsIgnoreCase("-c"))  {
		clearText = true;
	    }
	    if (args[0].equalsIgnoreCase("-n"))  {
		normal = true;
	    }
	}

	pw = new Password();

	System.out.print("Enter password: ");
	String s;

	if (normal)
	    s = pw.getPassword();
	else if (clearText)
	    s = pw.getClearTextPassword();
	else
	    s = pw.getHiddenPassword();

	System.err.println("");
	System.out.println("Password enterd is: " + s);
    }

}
