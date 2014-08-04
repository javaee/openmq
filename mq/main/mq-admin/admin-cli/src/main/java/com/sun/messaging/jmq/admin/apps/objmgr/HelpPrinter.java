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
 * @(#)HelpPrinter.java	1.9 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.objmgr;

import java.util.Enumeration;
import java.util.Properties;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminResources;

/** 
 * This class prints the usage/help statements for the jmqobjmgr.
 *
 */
public class HelpPrinter {

    private AdminResources ar = Globals.getAdminResources();

    /**
     * Constructor
     */
    public HelpPrinter() {
    } 

    /**
     * Prints usage, subcommands, options then exits.
     */
    public void printShortHelp(int exitStatus) {
	printUsage();
	printSubcommands();
	printOptions();
	System.exit(exitStatus);
    }

    /**
     * Prints everything in short help plus
     * attributes, examples then exits.
     */
    public void printLongHelp() {
	printUsage();
	printSubcommands();
	printOptions();

	printAttributes();
	printExamples();
	System.exit(0);
    }

    private void printUsage() {
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_USAGE));
    }

    private void printSubcommands() {
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_SUBCOMMANDS));
    }

    private void printOptions() {
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_OPTIONS));
    }

    private void printAttributes() {

	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_ATTRIBUTES1));

	// Create a Destination administered object to get it's properties
	AdministeredObject obj = (AdministeredObject)new com.sun.messaging.Topic();
	Properties props = obj.getConfiguration();

	ObjMgrPrinter omp = new ObjMgrPrinter(2, 6);
        String[] row = new String[2];

	for (Enumeration e = obj.enumeratePropertyNames(); e.hasMoreElements();) {
	    String propName = (String)e.nextElement();
	    try {
		row[0] = "    " + propName;
		row[1] = obj.getPropertyLabel(propName);
		omp.add(row);
	    } catch (Exception ex) {
	    }
	}
	omp.print();

	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_ATTRIBUTES2));
	// Create a ConnFactory administered object to get it's properties
	obj = (AdministeredObject)new com.sun.messaging.TopicConnectionFactory();
	props = obj.getConfiguration();

	ObjMgrPrinter omp2 = new ObjMgrPrinter(2, 6);

	for (Enumeration e = obj.enumeratePropertyNames(); e.hasMoreElements();) {
	    String propName = (String)e.nextElement();
	    try {
		row[0] = "    " + propName;
		row[1] = obj.getPropertyLabel(propName);
		omp2.add(row);
	    } catch (Exception ex) {
	    }
	}
	omp2.print();

    }

    private void printExamples() {
        Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES1));
        Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES2));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES3));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES4));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES5));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES6));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES7));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES8));
	Globals.stdOutPrintln(ar.getString(ar.I_OBJMGR_HELP_EXAMPLES9));
    }

}
