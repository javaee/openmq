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
 * @(#)UserMgrOptionParser.java	1.11 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

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
public class UserMgrOptionParser extends OptionParser
			implements UserMgrOptions  {

    /**
     * Options for the jmqobjmgr utility
     */
    static OptionDesc userMgrOptions[] = {
	/* 
	 *
	 * OptionDesc(String option, int type, String baseProp, String value)
	 */
	new OptionDesc(CMD_ADD, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_ADD),
	new OptionDesc(CMD_DELETE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_DELETE),
	new OptionDesc(CMD_LIST, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_LIST),
	new OptionDesc(CMD_UPDATE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_UPDATE),

	new OptionDesc(CMD_EXISTS, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_EXISTS),
	new OptionDesc(CMD_GETGROUP, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_GETGROUP),
	new OptionDesc(CMD_GETGROUPSIZE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_GETGROUPSIZE),

	new OptionDesc(CMD_ENCODE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_ENCODE),
	new OptionDesc(CMD_DECODE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_CMD, PROP_VALUE_CMD_DECODE),

	new OptionDesc(OPTION_ACTIVE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_ACTIVE, null),
	new OptionDesc(OPTION_PASSWD, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_PASSWD, null),
	new OptionDesc(OPTION_ROLE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_ROLE, null),
	new OptionDesc(OPTION_USERNAME, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_USERNAME, null),
	new OptionDesc(OPTION_INSTANCE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_INSTANCE, null),
	new OptionDesc(OPTION_PASSFILE, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_PASSFILE, null),

	new OptionDesc(OPTION_FORCE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_FORCE, PROP_VALUE_OPTION_FORCE),
	new OptionDesc(OPTION_SILENTMODE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_SILENTMODE, PROP_VALUE_OPTION_SILENTMODE),
	new OptionDesc(OPTION_CREATEMODE, OPTION_VALUE_HARDCODED, 
			PROP_NAME_OPTION_CREATEMODE, PROP_VALUE_OPTION_CREATEMODE),

	new OptionDesc(OPTION_SRC, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_SRC, null),
	new OptionDesc(OPTION_TARGET, OPTION_VALUE_NEXT_ARG, 
			PROP_NAME_OPTION_TARGET, null),

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
	new OptionDesc("-verbose", OPTION_VALUE_HARDCODED,
		"", "", true),
	new OptionDesc("-jmqext", OPTION_VALUE_NEXT_ARG,
		"", "", true)
	};
    
    
    /**
     * Parses arg list using the specified option description
     * table and returns a ObjMgrProperties object which corresponds
     * to it.
     */
    public static UserMgrProperties parseArgs(String args[]) 
		throws OptionException  {
	UserMgrProperties userMgrProps = new UserMgrProperties();

	/*
	 * Invoke main parsing code in superclass
	 */
        parseArgs(args, userMgrOptions, userMgrProps); 

        return (userMgrProps); 
    }
}
