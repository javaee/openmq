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

import java.io.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/*
 * java GenerateXMLFile -i <input file> -o <output file> -t <token1=value1> -t <token2=value2>
 * Defaults:
 *	<input file>		/etc/imq/xml/com.sun.cmm.mq.xml
 *	<output file>		$cwd/com.sun.cmm.mq.xml
 *	<token>			_INSTALL_DATE_
 *	<value>			<current date in milliseconds>
 *
 */
public class GenerateXMLFile  {
    String 			token		= null;
    Hashtable<String, String>	tokens		= null;
    String			inputFileName	= null;
    String			outputFileName	= null;
    String buf = null;
    private static final String	INSTALL_DATE	= "_INSTALL_DATE_";

    public GenerateXMLFile(Hashtable<String, String> tokens, String inputFileName, 
						String outputFileName)  {
        this.tokens = tokens;
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;

	setDefaults();

	try  {
	    openFile();
	} catch(Exception e)  {
	    System.out.println("Problems opening file: " + e);
	    System.exit(1);
	}

	try  {
	    replaceToken();
	} catch(Exception e)  {
	    System.out.println("Problems replacing token: " + e);
	    System.exit(1);
	}

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

        buf = new String(b);
    }

    private void replaceToken() throws PatternSyntaxException {
        for (Enumeration e = tokens.keys(); e.hasMoreElements() ;) {
            String curToken = (String)e.nextElement(),
                   curValue = (String)tokens.get(curToken);
            buf = buf.replaceAll(curToken, curValue);
        }
    }

    private void writeNewFile() throws FileNotFoundException, IOException  {
        FileOutputStream file = new FileOutputStream (outputFileName);
        byte[] b = buf.getBytes();
        file.write( b );
        file.close();
	System.out.println("Done writing out file: ");
    }

    private void setDefaults()  {
	if (tokens == null)  {
	    tokens = new Hashtable<String, String>();
	}

	if (!tokens.containsKey(INSTALL_DATE))  {
	    Date d = new Date();
            System.out.println("Date used: " + d);
	    String replacementString = "" + (d.getTime());
            System.out.println("Replacement string : " + replacementString);

	    tokens.put(INSTALL_DATE, replacementString);
	}

	if (inputFileName == null)  {
	    inputFileName = "/etc/imq/xml/template/com.sun.cmm.mq.xml";
	}

	if (outputFileName == null)  {
	    outputFileName = "com.sun.cmm.mq.xml";
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
	Hashtable<String, String> cmdlineTokens = null;
	String inputF = null, outputF = null;

	for (int i = 0; i < args.length; ++i)  {

	    if (args[i].equals("-i"))  {
		if (i+1 >= args.length)  {
		    usage("Path to input file not specified with -i", 1);
		}
		inputF = args[++i];
	    } else if (args[i].equals("-o"))  {
		if (i+1 >= args.length)  {
		    usage("Path to output file not specified with -o", 1);
		}
		outputF = args[++i];
	    } else if (args[i].equals("-t"))  {
		if (i+1 >= args.length)  {
		    usage("token=value pair not specified with -t", 1);
		}
		String tokenValuePair = args[++i],
		    token = getToken(tokenValuePair),
		    value = getValue(tokenValuePair);
		
		if (cmdlineTokens == null)  {
		    cmdlineTokens = new Hashtable<String, String>();
		}
		cmdlineTokens.put(token, value);
	    } else  {
		usage();
	    }
	}

	GenerateXMLFile rft = new GenerateXMLFile(cmdlineTokens, inputF, outputF);
    }

    private static String getToken(String tokenValuePair)  {
	if (tokenValuePair == null)  {
	    return (null);
	}

	int index = tokenValuePair.indexOf("=");

	if (index > 0) {
	    if (index == 0)  {
		return ("");
	    } else  {
		return (tokenValuePair.substring(0, index));
	    }
	}

	return (null);
    }

    private static String getValue(String tokenValuePair)  {
	if (tokenValuePair == null)  {
	    return (null);
	}

	int index = tokenValuePair.indexOf("=");

	if (index > 0) {
	    if (tokenValuePair.length() == 1)  {
		return ("");
	    } else  {
		return (tokenValuePair.substring(index+1));
	    }
	}

	return (null);
    }

}
