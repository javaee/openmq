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

package com.sun.messaging.bridge.admin.bridgemgr;

import java.util.Properties;
import com.sun.messaging.jmq.util.options.OptionDesc;
import com.sun.messaging.jmq.util.options.OptionParser;
import com.sun.messaging.jmq.util.options.OptionException;

/**
 * This class is a command line option parser for imqbridgemgr.  
 *
 */
public class BridgeMgrOptionParser extends OptionParser implements BridgeMgrOptions
{

    private static OptionDesc[] bridgeMgrOptions = {

	/******************************************************************************************* 
	 * OptionDesc(String option, int type, String baseProp, String value, String nameValuePair)
	 ******************************************************************************************/

	new OptionDesc(Cmd.LIST, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.LIST),
	new OptionDesc(Cmd.PAUSE, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.PAUSE),
	new OptionDesc(Cmd.RESUME, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.RESUME),
	new OptionDesc(Cmd.START, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.START),
	new OptionDesc(Cmd.STOP, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.STOP),
	new OptionDesc(Cmd.DEBUG, OPTION_VALUE_NEXT_ARG, 
			PropName.CMDARG, null, PropNVForCmd.DEBUG),

	new OptionDesc(Option.BRIDGE_TYPE, OPTION_VALUE_NEXT_ARG, 
            PropName.OPTION_BRIDGE_TYPE, null),
	new OptionDesc(Option.BRIDGE_NAME, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_BRIDGE_NAME, null),
	new OptionDesc(Option.LINK_NAME, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_LINK_NAME, null),

	new OptionDesc(Option.BROKER_HOSTPORT, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_BROKER_HOSTPORT, null),
	new OptionDesc(Option.ADMIN_USERID, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_ADMIN_USERID, null),
	new OptionDesc(Option.ADMIN_PRIVATE_PASSWD, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_ADMIN_PRIVATE_PASSWD, null),
	new OptionDesc(Option.ADMIN_PASSFILE, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_ADMIN_PASSFILE, null),
    new OptionDesc(Option.SYS_PROPS, OPTION_VALUE_SUFFIX_RES,
            PropName.OPTION_SYS_PROPS, null),


	new OptionDesc(Option.FORCE, OPTION_VALUE_HARDCODED, 
			PropName.OPTION_FORCE, PropValue.OPTION_FORCE),
	new OptionDesc(Option.SILENTMODE, OPTION_VALUE_HARDCODED, 
			PropName.OPTION_SILENTMODE, PropValue.OPTION_SILENTMODE),

	new OptionDesc(Option.NOCHECK, OPTION_VALUE_HARDCODED, 
			PropName.OPTION_NOCHECK, PropValue.OPTION_NOCHECK),
	new OptionDesc(Option.DEBUG, OPTION_VALUE_HARDCODED, 
			PropName.OPTION_DEBUG, PropValue.OPTION_DEBUG),
    new OptionDesc(Option.ADMIN_DEBUG, OPTION_VALUE_HARDCODED,
            PropName.OPTION_ADMIN_DEBUG, PropValue.OPTION_ADMIN_DEBUG),
	new OptionDesc(Option.TARGET_NAME, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_TARGET_NAME, null),
    new OptionDesc(Option.TARGET_ATTRS, OPTION_VALUE_NEXT_ARG_RES,
			PropName.OPTION_TARGET_ATTRS, null),

	new OptionDesc(Option.RECV_TIMEOUT, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_RECV_TIMEOUT, null),
	new OptionDesc(Option.NUM_RETRIES, OPTION_VALUE_NEXT_ARG, 
			PropName.OPTION_NUM_RETRIES, null),
	new OptionDesc(Option.SSL, OPTION_VALUE_HARDCODED, 
			PropName.OPTION_SSL, PropValue.OPTION_SSL),

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

    };
    
    
    /**
     * Parses arg list using the specified option description
     * table and returns a BridgeMgrProperties object which corresponds
     * to it.
     */
    public static BridgeMgrProperties parseArgs(String args[]) 
                                     throws OptionException  {

        BridgeMgrProperties props = new BridgeMgrProperties();

	    /*
	     * Invoke main parsing code in superclass
	     */
        parseArgs(args, bridgeMgrOptions, props);

        return (props); 
    }
}
