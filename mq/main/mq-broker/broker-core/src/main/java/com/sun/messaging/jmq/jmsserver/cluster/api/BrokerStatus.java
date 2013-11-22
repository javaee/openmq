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
 * @(#)BrokerStatus.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.cluster.api;

/**
 * This class is used to set the status flags that represent
 * the running status of the broker.
 * <p>
 * The status uses bit flags. Supported values are as follows:
 * <TABLE border=1>
 * <TR><TH>Bit</TH><TH>Description</TH><TH>Use</TH></TR>
 * <TR>
 *     <TD>LINK_UP</TD>
 *     <TD>If set, the link (socket connection) between brokers is 
 *           running. </TD>
 *     <TD><UL>
 *          <LI>set (with UP) when activateBroker is called by the
 *             multibroker code.</LI>
 *          <LI>unset when deactivateBroker is called when the
 *             socket between the brokers gets an IOException</LI>
 *          </UL></TD>
 * </TR>
 * <TR>
 *    <TD>IN_DOUBT</TD>
 *     <TD>The broker may be down.</TD>
 *     <TD><UL>
 *        <LI>Set by the heartbeat service when a broker may be down
 *         for some reason </LI>
 *        <LI>Unset by the monitor service when it determines a broker
 *            may be up or down</LI></TD>
 * </TR>
 * <TR>
 *    <TD>UP</TD>
 *    <TD>The broker is operating (it may or may not have
 *         a connection and may or may not be indoubt).</TD>
 *     <TD> <LI>Set by the monitor service when it determines a broker
 *              has shutdown. </LI>
 *        <LI>Unset when a broker is down (or a goodbye is received)</LI></TD>
 * </TR>
 *</TABLE>
 */


public class BrokerStatus
{
    public static final int BROKER_LINK_UP=0x00000001;
    public static final int BROKER_INDOUBT=0x00000010;
    public static final int BROKER_UP=0x00000100;
    public static final int BROKER_UNKNOWN=0x00000000;

    public static final int ACTIVATE_BROKER= BROKER_LINK_UP | BROKER_UP;

    public static int setBrokerIsUp(int status) 
    {
        int retval = status | BROKER_UP;
        return retval;
    }

    public static boolean getBrokerIsUp(int status)
    {
        return (status & BROKER_UP) == BROKER_UP;
    }

    public static int setBrokerIsDown(int status)
    {
        int retval = status & ~BROKER_UP;
        return retval;
    }

   
    public static boolean getBrokerIsDown(int status)
    {
        return (status & BROKER_UP) == 0;
    }


    public static int setBrokerLinkIsUp(int status) 
    {
        int retval = status | BROKER_LINK_UP;
        return retval;
    }

    public static boolean getBrokerLinkIsUp(int status)
    {
        return (status & BROKER_LINK_UP) == BROKER_LINK_UP;
    }


    public static int setBrokerLinkIsDown(int status)
    {
        int retval = status & ~BROKER_LINK_UP;
        return retval;
    }

    public static boolean getBrokerLinkIsDown(int status)
    {
        return (status & BROKER_LINK_UP) == 0;
    }

    public static int setBrokerInDoubt(int status) 
    {
        int retval = status | BROKER_INDOUBT;
        return retval;
    }

    public static boolean getBrokerInDoubt(int status)
    {
        return (status & BROKER_INDOUBT) == BROKER_INDOUBT;
    }

    public static int setBrokerNotInDoubt(int status)
    {
        int retval = status & ~BROKER_INDOUBT;
        return retval;
    }


    public static boolean getBrokerNotInDoubt(int status)
    {
        return (status & BROKER_INDOUBT) == 0;
    }

   
    /**
     * String representation of the status
     */
    public static String toString(int status) {
        return "BrokerStatus["+getStatusString(status)+"]";
    }

    private static String getStatusString(int status) {
        String str = "";
        if (getBrokerLinkIsUp(status))
            str +="LINK_UP";
        else
            str +="LINK_DOWN";

        if (getBrokerInDoubt(status))
            str +=":IN_DOUBT";
        else
            str +=":NOT_INDOUBT";

        if (getBrokerIsUp(status))
            str +=":UP";
        else
            str +=":DOWN";
        return str;
    }



}
