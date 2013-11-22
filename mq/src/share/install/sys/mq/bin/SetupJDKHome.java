/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)SetupJDKHome.java	1.4 07/11/07
 */ 

import java.io.*;
import java.util.*;

/*
 * java SetupJDKHome -i <input file> -j <string containing JDK location>
 * Defaults:
 *	<input file>		/etc/imq/imqenv.conf
 *	<string>		IMQ_DEFAULT_JAVAHOME=/usr/jdk/latest
 *
 */
public class SetupJDKHome  {
    String			jdkHomeString	= null;
    String			inputFileName	= null;
    StringBuffer		buf = null;
    private static final String	IMQ_DEFAULT_JAVAHOME	= "IMQ_DEFAULT_JAVAHOME=/usr/jdk/latest";

    public SetupJDKHome(String inputFileName, String jdkHomeString)  {
        this.inputFileName = inputFileName;
        this.jdkHomeString = jdkHomeString;

	setDefaults();

	try  {
	    openFile();
	} catch(Exception e)  {
	    System.out.println("Problems opening file: " + e);
	    System.exit(1);
	}

	appendBuffer();

	try  {
	    writeNewFile();
	} catch(Exception e)  {
	    System.out.println("Problems writing file: " + e);
	    System.exit(1);
	}

    }

    private void openFile() throws FileNotFoundException, IOException {
        FileInputStream file = new FileInputStream (inputFileName);
        byte[] b = new byte[file.available ()];
        file.read( b );
        file.close();

        buf = new StringBuffer(new String(b));
	System.out.println("Done reading in file: " + inputFileName);
    }

    private void appendBuffer() {
	buf.append(jdkHomeString + "\n");
	System.out.println("Appending: " + jdkHomeString);
    }

    private void writeNewFile() throws FileNotFoundException, IOException  {
        FileOutputStream file = new FileOutputStream (inputFileName);
        byte[] b = buf.toString().getBytes();
        file.write( b );
        file.close();
	System.out.println("Done writing out file: " + inputFileName);
    }

    private void setDefaults()  {
	if (jdkHomeString == null)  {
	    jdkHomeString = IMQ_DEFAULT_JAVAHOME;
	}

	if (inputFileName == null)  {
	    inputFileName = "/etc/imq/imqenv.conf";
	}
    }

    public static void usage()  {
        usage(null, 0);
    }

    public static void usage(String msg)  {
        usage(msg, 0);
    }

    public static void usage(String msg, int exitCode)  {
	if (msg != null)  {
            System.out.println(msg);
	}
        System.out.println("Usage:");

        System.exit(exitCode);
    }

    public static void main(String[] args) {
	String jdkHomeString = null;
	String inputF = null;

	for (int i = 0; i < args.length; ++i)  {

	    if (args[i].equals("-i"))  {
		if (i+1 >= args.length)  {
		    usage("Path to input file not specified with -i", 1);
		}
		inputF = args[++i];
	    } else if (args[i].equals("-j"))  {
		if (i+1 >= args.length)  {
		    usage("String containing JDK location not specified with -j", 1);
		}
		jdkHomeString = args[++i];
	    } else  {
		usage();
	    }
	}

	SetupJDKHome rft = new SetupJDKHome(inputF, jdkHomeString);
    }
}
