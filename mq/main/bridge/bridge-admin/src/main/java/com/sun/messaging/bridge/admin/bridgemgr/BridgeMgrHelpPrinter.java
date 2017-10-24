/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.messaging.bridge.admin.bridgemgr;


import com.sun.messaging.jmq.admin.apps.broker.CommonHelpPrinter;
import com.sun.messaging.bridge.admin.bridgemgr.resources.BridgeAdminResources;

/** 
 * This class prints the usage/help statements for the imqbridgemgr
 *
 */
public class BridgeMgrHelpPrinter implements CommonHelpPrinter, BridgeMgrOptions  {

    private BridgeAdminResources bar = Globals.getBridgeAdminResources();

    /**
     * Constructor
     */
    public BridgeMgrHelpPrinter() {
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

	printExamples();
	System.exit(0);
    }

    private void printUsage() {
	Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_USAGE));
    }

    private void printSubcommands() {
	Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_SUBCOMMANDS));
    }

    private void printOptions() {
	Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_OPTIONS));
    }

    private void printExamples() {
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES1));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES2));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES3));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES4));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES5));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES6));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES7));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES8));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES9));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES10));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES11));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES12));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES13));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES14));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES15));
        Globals.stdOutPrintln(bar.getString(bar.I_BGMGR_HELP_EXAMPLES16));
    }
}
