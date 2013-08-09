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
 * @(#)ObjMgrOptionParser.java	1.9 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.objmgr;

import java.util.Properties;
import javax.naming.*;
import com.sun.messaging.jmq.util.options.OptionDesc;
import com.sun.messaging.jmq.util.options.OptionParser;
import com.sun.messaging.jmq.util.options.OptionException;

/**
 * This class is a command line option parser that is
 * specific to jmqobjmgr.
 *
 * The options that are valid for jmqobjmgr are defined in
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
public class ObjMgrOptionParser extends OptionParser
			implements ObjMgrOptions  {

    /**
     * Options for the jmqobjmgr utility
     */
    static OptionDesc objMgrOptions[] = {
	/* 
	 *
	 * OptionDesc(String option, int type, String baseProp, String value)
	 */
	new OptionDesc(OBJMGR_ADD, OPTION_VALUE_HARDCODED, 
			OBJMGR_CMD_PROP_NAME, OBJMGR_ADD_PROP_VALUE),
	new OptionDesc(OBJMGR_DELETE, OPTION_VALUE_HARDCODED,
			OBJMGR_CMD_PROP_NAME, OBJMGR_DELETE_PROP_VALUE),
	new OptionDesc(OBJMGR_QUERY, OPTION_VALUE_HARDCODED,
			OBJMGR_CMD_PROP_NAME, OBJMGR_QUERY_PROP_VALUE),
	new OptionDesc(OBJMGR_LIST, OPTION_VALUE_HARDCODED,
			OBJMGR_CMD_PROP_NAME, OBJMGR_LIST_PROP_VALUE),
	new OptionDesc(OBJMGR_UPDATE, OPTION_VALUE_HARDCODED,
			OBJMGR_CMD_PROP_NAME, OBJMGR_UPDATE_PROP_VALUE),

	new OptionDesc(OBJMGR_TYPE, OPTION_VALUE_NEXT_ARG,
			OBJMGR_TYPE_PROP_NAME, OBJMGR_TYPE_PROP_VALUE),
	new OptionDesc(OBJMGR_NAME, OPTION_VALUE_NEXT_ARG,
			OBJMGR_NAME_PROP_NAME, OBJMGR_NAME_PROP_VALUE),
	new OptionDesc(OBJMGR_OBJ_ATTRS, OPTION_VALUE_NEXT_ARG_RES,
			OBJMGR_OBJ_ATTRS_PROP_NAME, OBJMGR_OBJ_ATTRS_PROP_VALUE),
	new OptionDesc(OBJMGR_OBJSTORE_ATTRS, OPTION_VALUE_NEXT_ARG_RES,
			OBJMGR_OBJSTORE_ATTRS_PROP_NAME, OBJMGR_OBJSTORE_ATTRS_PROP_VALUE),
	new OptionDesc(OBJMGR_READONLY, OPTION_VALUE_NEXT_ARG,
			OBJMGR_READONLY_PROP_NAME, OBJMGR_READONLY_PROP_VALUE),
	new OptionDesc(OBJMGR_FORCE, OPTION_VALUE_HARDCODED,
			OBJMGR_FORCE_PROP_NAME, OBJMGR_FORCE_PROP_VALUE),
	new OptionDesc(OBJMGR_PREVIEW, OPTION_VALUE_HARDCODED,
		OBJMGR_PREVIEW_PROP_NAME, OBJMGR_PREVIEW_PROP_VALUE),
	new OptionDesc(OBJMGR_INPUTFILE, OPTION_VALUE_NEXT_ARG,
		OBJMGR_INPUTFILE_PROP_NAME, OBJMGR_INPUTFILE_PROP_VALUE),
	new OptionDesc(OBJMGR_SILENTMODE, OPTION_VALUE_HARDCODED,
		OBJMGR_SILENTMODE_PROP_NAME, OBJMGR_SILENTMODE_PROP_VALUE),
        new OptionDesc(OBJMGR_OBJSTORE_BIND_ATTRS, OPTION_VALUE_NEXT_ARG_RES,
                        OBJMGR_OBJSTORE_BIND_ATTRS_PROP_NAME, OBJMGR_OBJSTORE_BIND_ATTRS_PROP_VALUE),

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
    public static ObjMgrProperties parseArgs(String args[]) 
		throws OptionException  {
	ObjMgrProperties objMgrProps = new ObjMgrProperties();

	/*
	 * Invoke main parsing code in superclass
	 */
        parseArgs(args, objMgrOptions, objMgrProps); 

        return (objMgrProps); 
    }
}


