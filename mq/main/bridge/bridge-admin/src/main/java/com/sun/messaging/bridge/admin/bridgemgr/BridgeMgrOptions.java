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

/**
 * Interface containing constants for command line options,
 * property names and values for the JMS Bridge Manager utility.
 *
 */
public interface BridgeMgrOptions  {

    /*************************************************
     * imqbridgrmgr <command> <commandarg> [options]
     *************************************************/

    public static enum Cmd {
        ;
        public static final String LIST   = "list";
        public static final String PAUSE  = "pause";
        public static final String RESUME = "resume";
        public static final String START  = "start";
        public static final String STOP   = "stop";
        public static final String DEBUG  = "debug";
    }

    public static enum CmdArg {
        ;
        public static final String BRIDGE = "bridge";
        public static final String LINK   = "link";
    }

    public static final String[] CMD_LIST_VALID_CMDARGS = {
                                    CmdArg.BRIDGE,
                                    CmdArg.LINK
                           };

    public static final String[] CMD_PAUSE_VALID_CMDARGS = {
                                     CmdArg.BRIDGE,
                                     CmdArg.LINK
                           };

    public static final String[] CMD_RESUME_VALID_CMDARGS = {
                                      CmdArg.BRIDGE,
                                      CmdArg.LINK
                           };

    public static final String[] CMD_START_VALID_CMDARGS  = {
                                     CmdArg.BRIDGE,
                                     CmdArg.LINK
                           };

    public static final String[] CMD_STOP_VALID_CMDARGS   = {
                                    CmdArg.BRIDGE,
                                    CmdArg.LINK
                           };


    public static enum Option {
        ;

        //imqbridgemgr specific 

        public static final String BRIDGE_NAME = "-bn";
        public static final String LINK_NAME   = "-ln";
        public static final String BRIDGE_TYPE = "-t";

        //same as imqcmd 

        public static final String BROKER_HOSTPORT      = "-b";
        public static final String ADMIN_USERID         = "-u";
        public static final String ADMIN_PASSWD         = "-p"; //not supported
        public static final String ADMIN_PRIVATE_PASSWD = "-pw"; // not used
        public static final String ADMIN_PASSFILE       = "-passfile";
        public static final String SSL                  = "-secure";
        public static final String RECV_TIMEOUT         = "-rtm";
        public static final String NUM_RETRIES          = "-rtr";
        public static final String SYS_PROPS            = "-D";
        public static final String DEBUG                = "-debug"; 
        public static final String TARGET_NAME          = "-n"; 
        public static final String TARGET_ATTRS         = "-o"; 
        public static final String ADMIN_DEBUG          = "-adebug"; 
        public static final String NOCHECK              = "-nocheck"; //TBD

        //standard
        public static final String FORCE        = "-f";
        public static final String SILENTMODE   = "-s";
        public static final String INPUTFILE    = "-i"; // not used
        public static final String SHORT_HELP1  = "-h";
        public static final String SHORT_HELP2  = "-help";
        public static final String LONG_HELP1   = "-H";
        public static final String LONG_HELP2   = "-Help";
        public static final String VERSION1     = "-v";
        public static final String VERSION2     = "-version";
    }


    public enum PropName {
        ;
        public static final String CMD                  = "cmdtype";
        public static final String CMDARG               = "cmdarg";

        //imqbridgemgr specific
        public static final String OPTION_BRIDGE_TYPE           = "bridgeType";
        public static final String OPTION_BRIDGE_NAME           = "bridgeName";
        public static final String OPTION_LINK_NAME             = "linkName";

        //same as imqcmd
        public static final String OPTION_BROKER_HOSTPORT = "brokerHostPort";
        public static final String OPTION_ADMIN_USERID    = "adminUser";
        public static final String OPTION_ADMIN_PRIVATE_PASSWD    = "adminPasswd";
        public static final String OPTION_ADMIN_PASSFILE  = "adminPassfile";
        public static final String OPTION_SSL             = "secure";
        public static final String OPTION_RECV_TIMEOUT    = "receiveTimeout";
        public static final String OPTION_NUM_RETRIES	    = "numRetries";
		public static final String OPTION_SYS_PROPS       = "sys.props";
        public static final String OPTION_DEBUG	          = "debug";
        public static final String OPTION_TARGET_NAME     = "targetName";
        public static final String OPTION_TARGET_ATTRS    = "target.attrs";
        public static final String OPTION_ADMIN_DEBUG	  = "adebug";
        public static final String OPTION_NOCHECK	      = "nocheck";

        //standard
        public static final String OPTION_FORCE           = "force";
        public static final String OPTION_SILENTMODE      = "silent";
        /*
         * property name for the admin password that is stored in a passfile.
         */
        public static final String PASSFILE_PASSWD       = "imq.imqbridgemgr.password";
    }

    public enum PropValue {
        ;
        public static final String OPTION_SSL             = "true";
        public static final String OPTION_NOCHECK         = "true";
        public static final String OPTION_DEBUG           = "true";
        public static final String OPTION_ADMIN_DEBUG     = "true";

        public static final String OPTION_FORCE           = "true";
        public static final String OPTION_SILENTMODE      = "true";

    }

    /*
     * These strings are of the form name=value.
     * They are needed because the command for imqbridgemgr require
     * the following actions:
     *	1. signal error if no command args are specified e.g. 'imqbridgemgr stop' 
     *	   without specifying 'bridge' or 'link'
     *	2. add property name/value pair for the arg specified e.g.
     *     imqbridgemgr pause bridge 
     *	   should add the property pair: cmdarg=bridge
     *	3. add property name/value pair for the command specified e.g.
     *     imqbridgemgr pause bridge 
     *	   should add the property pair: cmdtype=pause
     *
     * 1 and 2 are taken care of by the OPTION_VALUE_NEXT_ARG option type.
     * For 3, we needed to define a field in the OptionDesc class that
     * is basically a name/value pair that you want set whenever the option
     * is used. The strings that follow define the name/value pairs for
     * those options. They all of the form:
     *		cmdtype=<subcommand>
     */
    public enum PropNVForCmd {
        ;
        public static final String LIST   = PropName.CMD+"="+Cmd.LIST;
        public static final String PAUSE  = PropName.CMD+"="+Cmd.PAUSE;
        public static final String RESUME = PropName.CMD+"="+Cmd.RESUME;
        public static final String START  = PropName.CMD+"="+Cmd.START;
        public static final String STOP  =  PropName.CMD+"="+Cmd.STOP;
        public static final String DEBUG  = PropName.CMD+"="+Cmd.DEBUG;
    }

}

