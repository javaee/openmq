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
 * @(#)UserMgrOptions.java	1.11 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

/**
 * Interface containing constants for command line options,
 * property names and values for the JMS Object Administration
 * utility.
 */
public interface UserMgrOptions  {

    /*
     * BEGIN OPTION NAMES
     */

    /**
     * Strings defining what the sub command names are
     */
    public static final String CMD_ADD			= "add";
    public static final String CMD_DELETE		= "delete";
    public static final String CMD_LIST			= "list";
    public static final String CMD_UPDATE		= "update";

    /*
     * Private sub commands - for testing purposes
     */
    public static final String CMD_EXISTS		= ".exists";
    public static final String CMD_GETGROUP		= ".getgroup";
    public static final String CMD_GETGROUPSIZE		= ".getgroupsize";

    /*
     * More private sub commands
     */
    public static final String CMD_ENCODE		= "encode";
    public static final String CMD_DECODE		= "decode";

    /*
     * Options - jmqusermgr specific
     */
    public static final String OPTION_ACTIVE		= "-a";
    public static final String OPTION_PASSWD		= "-p";
    public static final String OPTION_ROLE		= "-g";
    public static final String OPTION_USERNAME		= "-u";
    public static final String OPTION_INSTANCE		= "-i";
    public static final String OPTION_PASSFILE		= "-passfile";
    public static final String OPTION_SRC		= "-src";
    public static final String OPTION_TARGET		= "-target";

    /*
     * Options - 'Standard'
     */
    public static final String OPTION_FORCE		= "-f";
    public static final String OPTION_SILENTMODE	= "-s";
    public static final String OPTION_CREATEMODE	= "-c";
    public static final String OPTION_SHORT_HELP1	= "-h";
    public static final String OPTION_SHORT_HELP2	= "-help";
    public static final String OPTION_LONG_HELP1	= "-H";
    public static final String OPTION_LONG_HELP2	= "-Help";
    public static final String OPTION_VERSION1		= "-v";
    public static final String OPTION_VERSION2		= "-version";
    public static final String OPTION_SYSTEM_PROPERTY_PREFIX = "-D";

    /*
     * END OPTION NAMES
     */

    /*
     * BEGIN PROPERTY NAMES/VALUES
     */

    /**
     * Property name representing what command
     * needs to be executed.
     */
    public static String PROP_NAME_CMD			= "cmdtype";

    /*
     * Property values for command types.
     */
    public static String PROP_VALUE_CMD_ADD		= CMD_ADD;
    public static String PROP_VALUE_CMD_DELETE		= CMD_DELETE;
    public static String PROP_VALUE_CMD_LIST		= CMD_LIST;
    public static String PROP_VALUE_CMD_UPDATE		= CMD_UPDATE;

    public static String PROP_VALUE_CMD_EXISTS		= CMD_EXISTS;
    public static String PROP_VALUE_CMD_GETGROUP	= CMD_GETGROUP;
    public static String PROP_VALUE_CMD_GETGROUPSIZE	= CMD_GETGROUPSIZE;

    public static String PROP_VALUE_CMD_ENCODE		= CMD_ENCODE;
    public static String PROP_VALUE_CMD_DECODE		= CMD_DECODE;

    public static String PROP_NAME_OPTION_ACTIVE	= "active";
    public static String PROP_NAME_OPTION_PASSWD	= "passwd";

    public static String PROP_NAME_OPTION_ROLE		= "role";
    public static String PROP_VALUE_ROLE_ADMIN		= "admin";
    public static String PROP_VALUE_ROLE_USER		= "user";
    public static String PROP_VALUE_ROLE_ANON		= "anonymous";

    public static String PROP_NAME_OPTION_USERNAME	= "username";

    public static String PROP_NAME_OPTION_INSTANCE	= "instance";

    public static String PROP_NAME_OPTION_FORCE		= "force";
    public static String PROP_VALUE_OPTION_FORCE	= "true";

    public static String PROP_NAME_OPTION_SILENTMODE	= "silent";
    public static String PROP_VALUE_OPTION_SILENTMODE	= "true";

    public static String PROP_NAME_OPTION_CREATEMODE	= "create";
    public static String PROP_VALUE_OPTION_CREATEMODE	= "false";

    public static String PROP_NAME_OPTION_SRC		= "src";
    public static String PROP_NAME_OPTION_TARGET	= "target";

    /*
     * Location of user repository
     * e.g. /var/imq/instances/imqbroker/etc/passwd
     */
    public static String PROP_NAME_PASSWORD_FILE	= "pwfile";

    /*
     * Location/name of passfile - file containing
     * user's password that is specified to imqusermgr.
     */
    public static String PROP_NAME_OPTION_PASSFILE	= "passfile";

    /*
     * END PROPERTY NAMES/VALUES
     */
    
    public static final String[] OPTION_ROLE_VALID_VALUES	= {
							    PROP_VALUE_ROLE_ADMIN,
							    PROP_VALUE_ROLE_USER,
							    PROP_VALUE_ROLE_ANON
							};

    /*
     * List of characters that cannot be used in usernames
     */
    public static final char[] OPTION_USERNAME_INVALID_CHARS	= {
							    ':', 
							    '*', 
							    ',', 
							    '\n',
							    '\r'
							};

    public static final String DEFAULT_ENCODE_PREFIX	= ".encode";
    public static final String DEFAULT_DECODE_PREFIX	= ".decode";
}

