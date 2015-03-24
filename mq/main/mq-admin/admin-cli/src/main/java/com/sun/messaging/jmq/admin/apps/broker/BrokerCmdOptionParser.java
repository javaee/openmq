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
 * @(#)BrokerCmdOptionParser.java	1.43 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.broker;

import java.util.Properties;
import javax.naming.*;
import com.sun.messaging.jmq.util.options.OptionDesc;
import com.sun.messaging.jmq.util.options.OptionParser;
import com.sun.messaging.jmq.util.options.OptionException;

/**
 * This class is a command line option parser that is
 * specific to jmqcmd.
 *
 * The options that are valid for jmqcmd are defined in
 * the options table. This class also defines a
 * parseArgs() method, which is different from
 *	OptionParser.parseArgs()
 * because it:
 * <UL>
 * <LI>returns a ObjMgrProperties object
 * <LI>only takes the String args[] as parameter
 * </UL>
 *
 * @see		com.sun.messaging.jmq.admin.util.OptionType
 * @see		com.sun.messaging.jmq.admin.util.OptionDesc
 * @see		com.sun.messaging.jmq.admin.util.OptionParser
 */
public class BrokerCmdOptionParser extends OptionParser
			implements BrokerCmdOptions  {

    /**
     * Options for the jmqobjmgr utility
     */
    static OptionDesc brokerCmdOptions[] = {
	/* 
	 *
	 * OptionDesc(String option, int type, String baseProp, String value)
	 */
	new OptionDesc(CMD_LIST, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_LIST),
	new OptionDesc(CMD_PAUSE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_PAUSE),
	new OptionDesc(CMD_RESUME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_RESUME),
	new OptionDesc(CMD_SHUTDOWN, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_SHUTDOWN),
	new OptionDesc(CMD_RESTART, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_RESTART),
	new OptionDesc(CMD_CREATE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_CREATE),
	new OptionDesc(CMD_DESTROY, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_DESTROY),
	new OptionDesc(CMD_PURGE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_PURGE),
	new OptionDesc(CMD_UPDATE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_UPDATE),
	new OptionDesc(CMD_QUERY, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_QUERY),
	new OptionDesc(CMD_METRICS, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_METRICS),
	new OptionDesc(CMD_RELOAD, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_RELOAD),
	new OptionDesc(CMD_CHANGEMASTER, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_CHANGEMASTER),
	new OptionDesc(CMD_COMMIT, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_COMMIT),
	new OptionDesc(CMD_ROLLBACK, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_ROLLBACK),
	new OptionDesc(CMD_COMPACT, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_COMPACT),
	new OptionDesc(CMD_QUIESCE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_QUIESCE),
	new OptionDesc(CMD_TAKEOVER, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_TAKEOVER),
	new OptionDesc(CMD_MIGRATESTORE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_MIGRATESTORE),
	new OptionDesc(CMD_UNQUIESCE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_UNQUIESCE),
	new OptionDesc(CMD_EXISTS, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_EXISTS),
	new OptionDesc(CMD_GETATTR, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_GETATTR),
	new OptionDesc(CMD_UNGRACEFUL_KILL, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_UNGRACEFUL_KILL),
	new OptionDesc(CMD_PURGEALL, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_PURGEALL),
	new OptionDesc(CMD_DESTROYALL, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_DESTROYALL),
	new OptionDesc(CMD_RESET, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_RESET),			
	new OptionDesc(CMD_CHECKPOINT, OPTION_VALUE_NEXT_ARG, 
					PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_CHECKPOINT),
	new OptionDesc(OPTION_DEST_TYPE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_DEST_TYPE, null),
	new OptionDesc(OPTION_TARGET_NAME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_TARGET_NAME, null),
	new OptionDesc(OPTION_DEST_NAME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_DEST_NAME, null),
	new OptionDesc(OPTION_METRIC_INTERVAL, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_METRIC_INTERVAL, null),
	new OptionDesc(OPTION_METRIC_TYPE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_METRIC_TYPE, null),
	/*
	 * Not used
	new OptionDesc(OPTION_SVC_NAME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_SVC_NAME, null),
	*/
	new OptionDesc(OPTION_CLIENT_ID, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_CLIENT_ID, null),
	new OptionDesc(OPTION_BROKER_HOSTPORT, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_BROKER_HOSTPORT, null),
	new OptionDesc(OPTION_ADMIN_USERID, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_ADMIN_USERID, null),
	new OptionDesc(OPTION_ADMIN_PRIVATE_PASSWD, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_ADMIN_PASSWD, null),
	new OptionDesc(OPTION_ADMIN_PASSFILE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_ADMIN_PASSFILE, null),
	new OptionDesc(OPTION_TARGET_ATTRS, OPTION_VALUE_NEXT_ARG_RES, 
			PROP_NAME_OPTION_TARGET_ATTRS, null),
	new OptionDesc(OPTION_SYS_PROPS, OPTION_VALUE_SUFFIX_RES, 
			PROP_NAME_OPTION_SYS_PROPS, null),
	new OptionDesc(OPTION_SINGLE_TARGET_ATTR, OPTION_VALUE_NEXT_ARG,
			PROP_NAME_OPTION_SINGLE_TARGET_ATTR, null),

	/*
	 * Not used
	new OptionDesc(OPTION_INPUTFILE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_INPUTFILE, null),
	 */

	new OptionDesc(OPTION_FORCE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_FORCE, PROP_VALUE_OPTION_FORCE),
	new OptionDesc(OPTION_SILENTMODE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_SILENTMODE, PROP_VALUE_OPTION_SILENTMODE),
	new OptionDesc(OPTION_TEMP_DEST, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_TEMP_DEST, PROP_VALUE_OPTION_TEMP_DEST),

	new OptionDesc(OPTION_NOCHECK, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_NOCHECK, PROP_VALUE_OPTION_NOCHECK),
	new OptionDesc(OPTION_DETAIL, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_DETAIL, PROP_VALUE_OPTION_DETAIL),
	new OptionDesc(OPTION_DEBUG, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_DEBUG, PROP_VALUE_OPTION_DEBUG),
	new OptionDesc(OPTION_ADMIN_DEBUG, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_ADMIN_DEBUG, PROP_VALUE_OPTION_ADMIN_DEBUG),
	new OptionDesc(CMD_DUMP, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_DUMP),
	new OptionDesc(CMD_SEND, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_SEND),
	new OptionDesc(CMD_KILL, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_KILL),
	new OptionDesc(CMD_DEBUG, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_CMDARG, null, PROP_NAMEVALUE_CMD_DEBUG),

	new OptionDesc(OPTION_RECV_TIMEOUT, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_RECV_TIMEOUT, null),
	new OptionDesc(OPTION_NUM_RETRIES, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_NUM_RETRIES, null),
	new OptionDesc(OPTION_SSL, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_SSL, PROP_VALUE_OPTION_SSL),
	new OptionDesc(OPTION_METRIC_SAMPLES, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_METRIC_SAMPLES, null),
	new OptionDesc(OPTION_SERVICE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_SERVICE, null),
	new OptionDesc(OPTION_PAUSE_TYPE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_PAUSE_TYPE, null),
	new OptionDesc(OPTION_NO_FAILOVER, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_NO_FAILOVER, PROP_VALUE_OPTION_NO_FAILOVER),
	new OptionDesc(OPTION_TIME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_TIME, null),
	new OptionDesc(OPTION_RESET_TYPE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_RESET_TYPE, null),
	new OptionDesc(OPTION_START_MSG_INDEX, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_START_MSG_INDEX, null),
	new OptionDesc(OPTION_MAX_NUM_MSGS_RET, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_MAX_NUM_MSGS_RET, null),
	new OptionDesc(OPTION_MSG_ID, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_MSG_ID, null),

	/*
	 * These are options that are parsed by the startup script. They are
	 * parsed by the option parsing logic, but are not used to create
	 * the options property object.
	 */
	new OptionDesc("-javahome", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-jmqhome", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-jmqvarhome", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-varhome", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-jmqext", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-vmargs", OPTION_VALUE_NEXT_ARG,
		"", "", true),
	new OptionDesc("-verbose", OPTION_VALUE_HARDCODED,
		"", "", true),

        /*
         * This is to support the private "-adminkey" option
         * It is used to support authentication when shutting down 
	 * the broker from NT services.
         */
	new OptionDesc(OPTION_ADMINKEY, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_ADMINKEY, PROP_VALUE_OPTION_ADMINKEY),

	new OptionDesc(OPTION_SHOW_PARTITION, OPTION_VALUE_HARDCODED, 
                  PROP_NAME_OPTION_SHOW_PARTITION, PROP_VALUE_OPTION_SHOW_PARTITION),

	new OptionDesc(OPTION_LOAD_DESTINATION, OPTION_VALUE_HARDCODED, 
                  PROP_NAME_OPTION_LOAD_DESTINATION, PROP_VALUE_OPTION_LOAD_DESTINATION),

	new OptionDesc(OPTION_MSG, OPTION_VALUE_HARDCODED, 
                  PROP_NAME_OPTION_MSG, PROP_VALUE_OPTION_MSG),

    };
    
    
    /**
     * Parses arg list using the specified option description
     * table and returns a ObjMgrProperties object which corresponds
     * to it.
     */
    public static BrokerCmdProperties parseArgs(String args[]) 
		throws OptionException  {
	BrokerCmdProperties brokerCmdProps = new BrokerCmdProperties();

	/*
	 * Invoke main parsing code in superclass
	 */
        parseArgs(args, brokerCmdOptions, brokerCmdProps); 

        return (brokerCmdProps); 
    }
}
