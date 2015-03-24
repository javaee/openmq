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
 * @(#)HelpPrinter.java	1.28 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.broker;

import java.util.Enumeration;
import java.util.Properties;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.bkrutil.BrokerConstants;
import com.sun.messaging.jmq.admin.resources.AdminResources;

/** 
 * This class prints the usage/help statements for the jmqobjmgr.
 *
 */
public class BrokerCmdHelpPrinter implements CommonHelpPrinter, BrokerCmdOptions, BrokerConstants {

    private AdminResources ar = Globals.getAdminResources();

    /**
     * Constructor
     */
    public BrokerCmdHelpPrinter() {
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
	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_USAGE));
    }

    private void printSubcommands() {
	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_SUBCOMMANDS));
    }

    private void printOptions() {
	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_OPTIONS));
    }

    private void printAttributes() {
	/*
	Object qAttrs[] = {PROP_NAME_QUEUE_FLAVOUR,
			PROP_NAME_OPTION_MAX_MESG_BYTE,
			PROP_NAME_OPTION_MAX_PER_MESG_SIZE,
			PROP_NAME_OPTION_MAX_MESG};
	*/

	String tAttrs;

	tAttrs = PROP_NAME_OPTION_MAX_PER_MESG_SIZE;

	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_ATTRIBUTES1));

	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_ATTRIBUTES2));
	printQueueAttrs();
	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_ATTRIBUTES3, tAttrs));
	printTopicAttrs();

        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_DEST_UNLIMITED));

	for (int i = 0; i < DEST_ATTRS_UNLIMITED.length; ++i)  {
            Globals.stdOutPrintln("    " + DEST_ATTRS_UNLIMITED[i]);
	}
        Globals.stdOutPrintln("");

        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_VALID_VALUES, 
			PROP_NAME_LIMIT_BEHAVIOUR));

        Globals.stdOutPrint("\t");
	for (int i = 0; i < BKR_LIMIT_BEHAV_VALID_VALUES.length; ++i)  {
            Globals.stdOutPrint(BKR_LIMIT_BEHAV_VALID_VALUES[i]);
	    
	    if ((i+1) < BKR_LIMIT_BEHAV_VALID_VALUES.length)  {
                Globals.stdOutPrint(" ");
	    }
	}
        Globals.stdOutPrintln("\n");

	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_ATTRIBUTES4));
	printBrokerAttrs();
	Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_ATTRIBUTES5));
	printServiceAttrs();
    }

    private void printExamples() {
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES1));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES2));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES3));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES4));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES5));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES6));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES7));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES8));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES9));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES10));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES11));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES12));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES13));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES14));
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_EXAMPLES15));
    }

    private void printBrokerAttrs()  {
	BrokerCmdPrinter bcp = new BrokerCmdPrinter(2, 4);
	String[] row = new String[2];
	String indent = "    ";

	row[0] = indent + PROP_NAME_BKR_PRIMARY_PORT;
	row[1] = ar.getString(ar.I_JMQCMD_PRIMARY_PORT);
	bcp.add(row);
		
	row[0] = indent + PROP_NAME_BKR_AUTOCREATE_TOPIC;
	row[1] = ar.getString(ar.I_AUTO_CREATE_TOPICS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_AUTOCREATE_QUEUE;
	row[1] = ar.getString(ar.I_AUTO_CREATE_QUEUES);
	bcp.add(row);
		
	row[0] = indent + PROP_NAME_BKR_LOG_LEVEL;
	row[1] = ar.getString(ar.I_LOG_LEVEL);
	bcp.add(row);
		
	row[0] = indent + PROP_NAME_BKR_LOG_ROLL_SIZE;
	row[1] = ar.getString(ar.I_LOG_ROLLOVER_SIZE);
	bcp.add(row);
		
	row[0] = indent + PROP_NAME_BKR_LOG_ROLL_INTERVAL;
	row[1] = ar.getString(ar.I_LOG_ROLLOVER_INTERVAL);
	bcp.add(row);

	/*
	row[0] = indent + PROP_NAME_BKR_METRIC_INTERVAL;
	row[1] = ar.getString(ar.I_METRIC_INTERVAL);
	bcp.add(row);
	*/
		
	row[0] = indent + PROP_NAME_BKR_MAX_MSG;
	row[1] = ar.getString(ar.I_MAX_MSGS_IN_BROKER);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_MAX_TTL_MSG_BYTES;
	row[1] = ar.getString(ar.I_MAX_BYTES_IN_BROKER);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_MAX_MSG_BYTES;
	row[1] = ar.getString(ar.I_MAX_MSG_SIZE);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_CLS_URL;
	row[1] = ar.getString(ar.I_CLS_URL);
	bcp.add(row);

	/*
	row[0] = indent + PROP_NAME_BKR_QUEUE_DELIVERY_POLICY;
	row[1] = ar.getString(ar.I_AUTOCREATED_QUEUE_DELIVERY_POLICY);
	bcp.add(row);
	*/

	row[0] = indent + PROP_NAME_BKR_AUTOCREATE_QUEUE_MAX_ACTIVE_CONS;
	row[1] = ar.getString(ar.I_AUTOCREATED_QUEUE_MAX_ACTIVE_CONS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_AUTOCREATE_QUEUE_MAX_BACKUP_CONS;
	row[1] = ar.getString(ar.I_AUTOCREATED_QUEUE_MAX_FAILOVER_CONS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_LOG_DEAD_MSGS;
	row[1] = ar.getString(ar.I_BKR_LOG_DEAD_MSGS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_BKR_DMQ_TRUNCATE_MSG_BODY;
	row[1] = ar.getString(ar.I_BKR_DMQ_TRUNCATE_MSG_BODY);
	bcp.add(row);
        
	row[0] = indent + PROP_NAME_BKR_AUTOCREATE_DESTINATION_USE_DMQ;
	row[1] = ar.getString(ar.I_BKR_AUTOCREATE_DESTINATION_USE_DMQ);
	bcp.add(row);
		
	bcp.print();		

        Globals.stdOutPrintln("");
        Globals.stdOutPrint(indent);
        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_VALID_VALUES, 
			PROP_NAME_BKR_LOG_LEVEL));

        Globals.stdOutPrint("\t");
	for (int i = 0; i < BKR_LOG_LEVEL_VALID_VALUES.length; ++i)  {
            Globals.stdOutPrint(BKR_LOG_LEVEL_VALID_VALUES[i]);
	    
	    if ((i+1) < BKR_LOG_LEVEL_VALID_VALUES.length)  {
                Globals.stdOutPrint(" ");
	    }
	}
        Globals.stdOutPrintln("\n");

        Globals.stdOutPrintln(ar.getString(ar.I_BROKERCMD_HELP_BKR_UNLIMITED));

	for (int i = 0; i < BKR_ATTRS_UNLIMITED.length; ++i)  {
            Globals.stdOutPrintln("    " + BKR_ATTRS_UNLIMITED[i]);
	}
        Globals.stdOutPrintln("");

    }

    private void printQueueAttrs()  {
	BrokerCmdPrinter bcp = new BrokerCmdPrinter(2, 4);
	String[] row = new String[2];
	String indent = "    ";

	row[0] = indent + PROP_NAME_OPTION_MAX_MESG;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_MSG_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_OPTION_MAX_MESG_BYTE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_MSG_BYTES_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_OPTION_MAX_PER_MESG_SIZE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_BYTES_PER_MSG_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_MAX_FAILOVER_CONSUMER_COUNT;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_FAILOVER_CONSUMER_COUNT);
	bcp.add(row);

	row[0] = indent + PROP_NAME_MAX_ACTIVE_CONSUMER_COUNT;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_ACTIVE_CONSUMER_COUNT);
	bcp.add(row);

	row[0] = indent + PROP_NAME_IS_LOCAL_DEST
			+ " " 
			+ ar.getString(ar.I_BROKERCMD_HELP_ATTR_CREATE_ONLY);
	row[1] = ar.getString(ar.I_JMQCMD_DST_IS_LOCAL_DEST);
	bcp.add(row);

	row[0] = indent + PROP_NAME_LIMIT_BEHAVIOUR;
	row[1] = ar.getString(ar.I_JMQCMD_DST_LIMIT_BEHAVIOUR);
	bcp.add(row);

	row[0] = indent + PROP_NAME_LOCAL_DELIVERY_PREF;
	row[1] = ar.getString(ar.I_JMQCMD_DST_LOCAL_DELIVERY_PREF);
	bcp.add(row);

	row[0] = indent + PROP_NAME_CONSUMER_FLOW_LIMIT;
	row[1] = ar.getString(ar.I_JMQCMD_DST_CONS_FLOW_LIMIT);
	bcp.add(row);

	row[0] = indent + PROP_NAME_MAX_PRODUCERS;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_PRODUCERS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_USE_DMQ;
	row[1] = ar.getString(ar.I_JMQCMD_DST_USE_DMQ);
	bcp.add(row);

	row[0] = indent + PROP_NAME_VALIDATE_XML_SCHEMA_ENABLED;
	row[1] = ar.getString(ar.I_JMQCMD_DST_VALIDATE_XML_SCHEMA_ENABLED);
	bcp.add(row);

	row[0] = indent + PROP_NAME_XML_SCHEMA_URI_LIST;
	row[1] = ar.getString(ar.I_JMQCMD_DST_XML_SCHEMA_URI_LIST);
	bcp.add(row);

	row[0] = indent + PROP_NAME_RELOAD_XML_SCHEMA_ON_FAILURE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_RELOAD_XML_SCHEMA_ON_FAILURE);
	bcp.add(row);

	bcp.print();		

        Globals.stdOutPrintln("");
    }

    private void printTopicAttrs()  {
	BrokerCmdPrinter bcp = new BrokerCmdPrinter(2, 4);
	String[] row = new String[2];
	String indent = "    ";

	row[0] = indent + PROP_NAME_OPTION_MAX_MESG;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_MSG_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_OPTION_MAX_MESG_BYTE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_MSG_BYTES_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_OPTION_MAX_PER_MESG_SIZE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_BYTES_PER_MSG_ALLOW);
	bcp.add(row);

	row[0] = indent + PROP_NAME_IS_LOCAL_DEST
			+ " " 
			+ ar.getString(ar.I_BROKERCMD_HELP_ATTR_CREATE_ONLY);
	row[1] = ar.getString(ar.I_JMQCMD_DST_IS_LOCAL_DEST);
	bcp.add(row);

	row[0] = indent + PROP_NAME_LIMIT_BEHAVIOUR;
	row[1] = ar.getString(ar.I_JMQCMD_DST_LIMIT_BEHAVIOUR);
	bcp.add(row);

	row[0] = indent + PROP_NAME_CONSUMER_FLOW_LIMIT;
	row[1] = ar.getString(ar.I_JMQCMD_DST_CONS_FLOW_LIMIT);
	bcp.add(row);

	row[0] = indent + PROP_NAME_MAX_PRODUCERS;
	row[1] = ar.getString(ar.I_JMQCMD_DST_MAX_PRODUCERS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_USE_DMQ;
	row[1] = ar.getString(ar.I_JMQCMD_DST_USE_DMQ);
	bcp.add(row);

	row[0] = indent + PROP_NAME_VALIDATE_XML_SCHEMA_ENABLED;
	row[1] = ar.getString(ar.I_JMQCMD_DST_VALIDATE_XML_SCHEMA_ENABLED);
	bcp.add(row);

	row[0] = indent + PROP_NAME_XML_SCHEMA_URI_LIST;
	row[1] = ar.getString(ar.I_JMQCMD_DST_XML_SCHEMA_URI_LIST);
	bcp.add(row);

	row[0] = indent + PROP_NAME_RELOAD_XML_SCHEMA_ON_FAILURE;
	row[1] = ar.getString(ar.I_JMQCMD_DST_RELOAD_XML_SCHEMA_ON_FAILURE);
	bcp.add(row);

	bcp.print();		

        Globals.stdOutPrintln("");
    }

    private void printServiceAttrs()  {
	BrokerCmdPrinter bcp = new BrokerCmdPrinter(2, 4);
	String[] row = new String[2];
	String indent = "    ";

	row[0] = indent + PROP_NAME_SVC_PORT;
	row[1] = ar.getString(ar.I_JMQCMD_SVC_PORT);
	bcp.add(row);
		
	row[0] = indent + PROP_NAME_SVC_MIN_THREADS;
	row[1] = ar.getString(ar.I_JMQCMD_SVC_MIN_THREADS);
	bcp.add(row);

	row[0] = indent + PROP_NAME_SVC_MAX_THREADS;
	row[1] = ar.getString(ar.I_JMQCMD_SVC_MAX_THREADS);
	bcp.add(row);
		
	bcp.println();		
    }


}
