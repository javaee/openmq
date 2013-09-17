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
 * @(#)BrokerAdminException.java	1.14 06/27/07
 */ 

package com.sun.messaging.jmq.admin.bkrutil;

import javax.jms.Message;

/**
 *  This class used by imqcmd, imqadmin ,imqbridgemgr
 *  Please create subcass if need individual admin tool specific references 
 */
public class BrokerAdminException extends Exception {

    public static final int	CONNECT_ERROR		= 0;
    public static final int	MSG_SEND_ERROR		= 1;
    public static final int	MSG_REPLY_ERROR		= 2;
    public static final int	CLOSE_ERROR		= 3;
    public static final int	PROB_GETTING_MSG_TYPE	= 4;
    public static final int	PROB_GETTING_STATUS	= 5;
    public static final int	REPLY_NOT_RECEIVED	= 6;
    public static final int	INVALID_OPERATION	= 7;
    public static final int	INVALID_PORT_VALUE	= 8;
    public static final int	BAD_HOSTNAME_SPECIFIED	= 9;
    public static final int	BAD_PORT_SPECIFIED	= 10;
    public static final int	INVALID_LOGIN		= 11;
    public static final int	SECURITY_PROB		= 12;
    public static final int	BUSY_WAIT_FOR_REPLY	= 13;
    public static final int	IGNORE_REPLY_IF_RCVD	= 14;
    public static final int	PROB_SETTING_SSL	= 15;
    public static final int	BAD_ADDR_SPECIFIED	= 16;

    private BrokerAdminConn ba;
    private Exception	linkedException;
    private String	brokerErrorStr,
			badValue,
			brokerHost,
			brokerPort,
			brokerAddr;
    private int		type,
			replyStatus = -1,
    			replyMsgType;

    private Message replyMsg = null;

    public BrokerAdminException(int type) {
	super();
	this.type = type;
    }

    public int getType()  {
	return (type);
    }

    public void setBrokerErrorStr(String errorStr)  {
	brokerErrorStr = errorStr;
    }
    public String getBrokerErrorStr()  {
	return (brokerErrorStr);
    }

    public void setReplyStatus(int replyStatus)  {
	this.replyStatus = replyStatus;
    }
    public int getReplyStatus()  {
	return (replyStatus);
    }

    public void setReplyMsgType(int replyMsgType)  {
	this.replyMsgType = replyMsgType;
    }
    public int getReplyMsgType()  {
	return (replyMsgType);
    }

    public void setReplyMsg(Message msg)  {
	this.replyMsg = msg;
    }

    public Message getReplyMsg()  {
	return replyMsg;
    }

    public void setBadValue(String badValue)  {
	this.badValue = badValue;
    }
    public String getBadValue()  {
	return (badValue);
    }

    public void setBrokerAdminConn(BrokerAdminConn ba)  {
	this.ba = ba;
    }
    public BrokerAdminConn getBrokerAdminConn()  {
	return (ba);
    }

    public void setLinkedException(Exception e)  {
	linkedException = e;
    }
    public Exception getLinkedException()  {
	return (linkedException);
    }

    public void setBrokerHost(String s)  {
	brokerHost = s;
    }
    public String getBrokerHost()  {
	return (brokerHost);
    }

    public void setBrokerPort(String s)  {
	brokerPort = s;
    }
    public String getBrokerPort()  {
	return (brokerPort);
    }

    public void setBrokerAddress(String s)  {
	brokerAddr = s;
    }
    public String getBrokerAddress()  {
	return (brokerAddr);
    }

}
