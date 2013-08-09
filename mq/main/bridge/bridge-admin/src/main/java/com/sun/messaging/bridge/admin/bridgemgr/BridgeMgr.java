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

package com.sun.messaging.bridge.admin.bridgemgr;

import java.io.*;
import java.util.Properties;
import java.util.Enumeration;
import javax.jms.*;

import com.sun.messaging.jmq.util.options.OptionException;
import com.sun.messaging.jmq.admin.apps.broker.CommonCmdRunnerUtil;
import com.sun.messaging.bridge.admin.bridgemgr.resources.BridgeAdminResources;

/** 
 * This is an administration utility for the JMS bridge.
 *
 */
public class BridgeMgr implements BridgeMgrOptions  {

    private static BridgeAdminResources ar = Globals.getBridgeAdminResources();

    /**
     * Constructor
     */
    public BridgeMgr() {
    } 

    public static void main(String[] args)  {
	int exitcode = 0;

	if (silentModeOptionSpecified(args))  {
            Globals.setSilentMode(true);
	}

	/*
	 * Check for -h or -H, or that the first argument
	 * is the command type (-a, -d, -l or -q)
	 * if we still want to have that restriction.
	 */
	if (shortHelpOptionSpecified(args))  {
            BridgeMgrHelpPrinter hp = new BridgeMgrHelpPrinter();
	    hp.printShortHelp(0);
	} else if (longHelpOptionSpecified(args))  {
            BridgeMgrHelpPrinter hp = new BridgeMgrHelpPrinter();
	    hp.printLongHelp();
	}
	/*
	 * Check for -version.
	 */
	if (versionOptionSpecified(args)) {
	    CommonCmdRunnerUtil.printBanner();
        CommonCmdRunnerUtil.printVersion();
	    System.exit(0);
	}

	BridgeMgrProperties bridgeMgrProps = null;

	/*
	 * Convert String args[] into a BridgeMgrProperties object.
	 * The BridgeMgrProperties class is just a Properties
	 * subclass with some convenience methods in it.
	 */
	try  {
	    bridgeMgrProps = BridgeMgrOptionParser.parseArgs(args);
	} catch (OptionException e)  {
        CommonCmdRunnerUtil.handleArgsParsingExceptions(e, Option.ADMIN_PASSWD, "imqbridgemgr");
            System.exit(1);
	}

	/*
	 * For each command type used, check that the
	 * information passed in is sufficient.
	 */
	try  {
	    checkOptions(bridgeMgrProps);
	} catch (BridgeMgrException ome)  {
	    handleCheckOptionsExceptions(ome);
            System.exit(1);
	}

	/*
	 * Check if any properties were specified via -Dprop=val
	 * and set them via System.setProperty()
	 */
	Properties sysProps = bridgeMgrProps.getSysProps();
	if ((sysProps != null) && (sysProps.size() > 0))  {
	    for (Enumeration e = sysProps.propertyNames() ; e.hasMoreElements() ;)  {
	        String name = (String)e.nextElement(),
			value = sysProps.getProperty(name);

		if (bridgeMgrProps.adminDebugModeSet())  {
	            Globals.stdOutPrintln("Setting system property: "
				+ name
				+ "="
				+ value);
		}

		try  {
		    System.setProperty(name, value);
		} catch(Exception ex)  {
	            Globals.stdErrPrintln("Failed to set system property: "
				+ name
				+ "="
				+ value);
	            Globals.stdErrPrintln(ex.toString());
		}
            }
	}

	/*
	 * Execute the commands specified by the user
	 */

	CmdRunner cmdRunner = new CmdRunner(bridgeMgrProps);
	exitcode = cmdRunner.runCommand();

	System.exit(exitcode);
    }

    /**
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute user commands. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkOptions(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {

	if (bridgeMgrProps.debugModeSet() || 
	    bridgeMgrProps.noCheckModeSet())  {
        if (bridgeMgrProps.adminDebugModeSet()) {
	    Globals.stdOutPrintln("Option checking turned off.");
        }
	    return;
	}

    if (bridgeMgrProps.adminDebugModeSet()) {
    Globals.stdErrPrintln("BridgeMgrProperties dump:");
	bridgeMgrProps.list(System.err);
	Globals.stdErrPrintln("-------------\n");
	Globals.stdErrPrintln("Command: " + bridgeMgrProps.getCommand());
	Globals.stdErrPrintln("Command Argument: " + bridgeMgrProps.getCommandArg());
	Globals.stdErrPrintln("Broker Host/Port: " + bridgeMgrProps.getBrokerHostPort());
	Globals.stdErrPrintln("Admin User ID: " + bridgeMgrProps.getAdminUserId());
	Globals.stdErrPrintln("Admin User Password: " + bridgeMgrProps.getAdminPasswd());
    }

	String cmd = bridgeMgrProps.getCommand();

	if (cmd == null)  {
	    BridgeMgrException objMgrEx;
	    objMgrEx = new BridgeMgrException(BridgeMgrException.NO_CMD_SPEC);
	    objMgrEx.setProperties(bridgeMgrProps);

	    throw(objMgrEx);
	}

	/*
	 * Determine type of command and invoke the relevant check method
	 * to verify the contents of the BridgeMgrProperties object.
	 *
	 */
	if (cmd.equals(Cmd.LIST))  {
	    checkList(bridgeMgrProps);
	} else if (cmd.equals(Cmd.PAUSE))  {
	    checkPause(bridgeMgrProps);
	} else if (cmd.equals(Cmd.RESUME))  {
	    checkResume(bridgeMgrProps);
	} else if (cmd.equals(Cmd.START))  {
	    checkStart(bridgeMgrProps);
	} else if (cmd.equals(Cmd.STOP))  {
	    checkStop(bridgeMgrProps);
	} else if (bridgeMgrProps.debugModeSet() && cmd.equals(Cmd.DEBUG))  {
	} else  {
	    BridgeMgrException objMgrEx;
	    objMgrEx = new BridgeMgrException(BridgeMgrException.BAD_CMD_SPEC);
	    objMgrEx.setProperties(bridgeMgrProps);

	    throw(objMgrEx);
	}

	/*
	 * Check if receiveTimeout value is valid, if it is specified.
	 */
	String recvTimeoutStr = bridgeMgrProps.getProperty(PropName.OPTION_RECV_TIMEOUT);
	if (recvTimeoutStr != null)  {
	    try  {
	        checkIntegerValue
	            (bridgeMgrProps, PropName.OPTION_RECV_TIMEOUT, recvTimeoutStr);
	    } catch (Exception e)  {
	        BridgeMgrException bce;
	        bce = new BridgeMgrException(BridgeMgrException.INVALID_RECV_TIMEOUT_VALUE);
	        bce.setProperties(bridgeMgrProps);
	  	bce.setErrorString(recvTimeoutStr);

	        throw(bce);
	    }
	}

	/*
	 * Check if numRetries value is valid, if it is specified.
	 */
	String numRetriesStr = bridgeMgrProps.getProperty(PropName.OPTION_NUM_RETRIES);
	if (numRetriesStr != null)  {
	    try  {
	         checkIntegerValue
	             (bridgeMgrProps, PropName.OPTION_NUM_RETRIES, numRetriesStr);
	    } catch (Exception e)  {
	        BridgeMgrException bce;
	        bce = new BridgeMgrException(BridgeMgrException.INVALID_NUM_RETRIES_VALUE);
	        bce.setProperties(bridgeMgrProps);
	  	bce.setErrorString(numRetriesStr);

	        throw(bce);
	    }
	}
    }

    /*
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute the 'list' command. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkList(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	checkCmdArg(bridgeMgrProps, CMD_LIST_VALID_CMDARGS);

	String	cmdArg = bridgeMgrProps.getCommandArg();

	/*
	 * If link name specified, make sure a bridge name was also specified
	 */
	if (cmdArg.equals(CmdArg.LINK))  {
            checkBridgeName(bridgeMgrProps);
            checkLinkName(bridgeMgrProps);
	}
    }

    /*
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute the 'pause' command. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkPause(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {

	checkCmdArg(bridgeMgrProps, CMD_PAUSE_VALID_CMDARGS);

	String	cmdArg = bridgeMgrProps.getCommandArg();

	/*
	 * If link name specified, make sure a bridge name was also specified
	 */
	if (cmdArg.equals(CmdArg.LINK))  {
            checkBridgeName(bridgeMgrProps);
            checkLinkName(bridgeMgrProps);
	}
    }

    /*
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute the 'resume' command. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkResume(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	checkCmdArg(bridgeMgrProps, CMD_RESUME_VALID_CMDARGS);

	String	cmdArg = bridgeMgrProps.getCommandArg();

	/*
	 * If link name specified, make sure a bridge name was also specified
	 */
	if (cmdArg.equals(CmdArg.LINK))  {
            checkBridgeName(bridgeMgrProps);
            checkLinkName(bridgeMgrProps);
	}
    }

    /*
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute the 'start' command. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkStart(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	checkCmdArg(bridgeMgrProps, CMD_START_VALID_CMDARGS);

	String	cmdArg = bridgeMgrProps.getCommandArg();

	/*
	 * If link name specified, make sure a bridge name was also specified
	 */
	if (cmdArg.equals(CmdArg.LINK))  {
            checkBridgeName(bridgeMgrProps);
            checkLinkName(bridgeMgrProps);
	}
    }

    /*
     * Check BridgeMgrProperties object to make sure it contains
     * all the correct info to execute the 'stop' command. This
     * method may print out errors/warnings and exit with an error
     * code.
     */
    private static void checkStop(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	checkCmdArg(bridgeMgrProps, CMD_STOP_VALID_CMDARGS);

	String	cmdArg = bridgeMgrProps.getCommandArg();

	/*
	 * If link name specified, make sure a bridge name was also specified
	 */
	if (cmdArg.equals(CmdArg.LINK))  {
            checkBridgeName(bridgeMgrProps);
            checkLinkName(bridgeMgrProps);
	}
    }

    private static int checkIntegerValue
	(BridgeMgrProperties bridgeMgrProps, String type, String maxValue)
	throws BridgeMgrException {
        BridgeMgrException ex = null;
        int iValue;

        try {
            iValue = Integer.parseInt(maxValue);
            if ( iValue < -1) {
                ex = new BridgeMgrException(BridgeMgrException.INVALID_INTEGER_VALUE);
                ex.setProperties(bridgeMgrProps);
	  	ex.setErrorString(type);

	   	throw(ex);
	    }

        } catch (Exception e) {
            ex = new BridgeMgrException(BridgeMgrException.INVALID_INTEGER_VALUE);
            ex.setProperties(bridgeMgrProps);
	    ex.setErrorString(type);

	    throw(ex);
	}

	return (iValue);
    }


    private static void checkCmdArg(BridgeMgrProperties bridgeMgrProps,
				String validCmdArgs[])
			throws BridgeMgrException  {
	BridgeMgrException ex;
	String cmdArg = bridgeMgrProps.getCommandArg();

	if (cmdArg == null)  {
	    ex = new BridgeMgrException(BridgeMgrException.CMDARG_NOT_SPEC);
	    ex.setProperties(bridgeMgrProps);

	    throw(ex);
	}

	if (validCmdArgs == null)
	    return;

	for (int i = 0; i < validCmdArgs.length; ++i)  {
	    if (cmdArg.equals(validCmdArgs[i]))  {
            if (cmdArg.equals(CmdArg.BRIDGE)) {
                if (bridgeMgrProps.getLinkName() != null) {
	                ex = new BridgeMgrException(BridgeMgrException.LINK_OPTION_NOT_ALLOWED_FOR_CMDARG);
                    ex.setProperties(bridgeMgrProps);
                    throw (ex);
                }
            } 
		    return;
	    }
	}

	ex = new BridgeMgrException(BridgeMgrException.BAD_CMDARG_SPEC);
	ex.setProperties(bridgeMgrProps);
	ex.setValidCmdArgs(validCmdArgs);
	throw(ex);
    }

    private static void checkBridgeName(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	String bn = bridgeMgrProps.getBridgeName();

	if (bn == null || bn.trim().length() == 0)  {
	    BridgeMgrException ex = 
		new BridgeMgrException(BridgeMgrException.BRIDGE_NAME_NOT_SPEC);
	    ex.setProperties(bridgeMgrProps);

	    throw(ex);
	}
    }

    private static void checkLinkName(BridgeMgrProperties bridgeMgrProps) 
			throws BridgeMgrException  {
	String ln = bridgeMgrProps.getLinkName();

	if (ln == null || ln.trim().length()== 0)  {
	    BridgeMgrException ex = 
		new BridgeMgrException(BridgeMgrException.LINK_NAME_NOT_SPEC);
	    ex.setProperties(bridgeMgrProps);

	    throw(ex);
	}
    }


    /*
     * REVISIT: Is it possible for any option value to be '-h' ?
     */
    private static boolean shortHelpOptionSpecified(String args[]) {
	for (int i = 0; i < args.length; ++i)  {
	    if (args[i].equals(Option.SHORT_HELP1) || 
		args[i].equals(Option.SHORT_HELP2)) {
		return (true);
	    }
	}

	return (false);
    }

    private static boolean longHelpOptionSpecified(String args[]) {
	for (int i = 0; i < args.length; ++i)  {
	    if (args[i].equals(Option.LONG_HELP1) ||
	   	args[i].equals(Option.LONG_HELP2)) {
		return (true);
	    }
	}

	return (false);
    }

    private static boolean versionOptionSpecified(String args[]) {
	for (int i = 0; i < args.length; ++i)  {
	    if (args[i].equals(Option.VERSION1) ||
		args[i].equals(Option.VERSION2)) {
		return (true);
	    }
	}

	return (false);
    }

    private static boolean silentModeOptionSpecified(String args[]) {
	for (int i = 0; i < args.length; ++i)  {
	    if (args[i].equals(Option.SILENTMODE)) {
		return (true);
	    }
	}

	return (false);
    }

    private static void handleCheckOptionsExceptions(BridgeMgrException e) {
	BridgeMgrProperties	bridgeMgrProps = (BridgeMgrProperties)e.getProperties();
	String			cmd = bridgeMgrProps.getCommand(),
    				cmdArg = bridgeMgrProps.getCommandArg();
	int			type = e.getType();

	/*
	 * REVISIT: should check bridgeMgrProps != null
	 */

	switch (type)  {
	case BridgeMgrException.BRIDGE_NAME_NOT_SPEC:
	    Globals.stdErrPrintln(
                ar.getString(ar.I_ERROR_MESG), 
		ar.getKString(ar.E_BRIDGE_NAME_NOT_SPEC, Option.BRIDGE_NAME));
	break;

	case BridgeMgrException.LINK_NAME_NOT_SPEC:
	    Globals.stdErrPrintln(
                ar.getString(ar.I_ERROR_MESG), 
		ar.getKString(ar.E_LINK_NAME_NOT_SPEC, Option.LINK_NAME));
	break;

	case BridgeMgrException.LINK_OPTION_NOT_ALLOWED_FOR_CMDARG:
	    Globals.stdErrPrintln(
                ar.getString(ar.I_ERROR_MESG), 
		ar.getKString(ar.E_OPTION_NOT_ALLOWED_FOR_CMDARG, Option.LINK_NAME, cmd+" "+cmdArg));
	break;

	default:
	    CommonCmdRunnerUtil.handleCommonCheckOptionsExceptions(e, cmd, cmdArg, new BridgeMgrHelpPrinter());
	}
    }

}
