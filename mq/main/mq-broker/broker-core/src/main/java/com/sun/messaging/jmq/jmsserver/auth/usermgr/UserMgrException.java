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
 * @(#)UserMgrException.java	1.14 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

/**
 * This exception is thrown when problems are
 * encountered when validating the information
 * that is provided to execute commands. Examples
 * of errors include:
 * <UL>
 * <LI>bad command type
 * <LI>missing mandatory values
 * </UL>
 *
 * <P>
 * The information that is provided by the user is encapsulated
 * in a UserMgrProperties object. This exception will
 * contain a UserMgrProperties object to encapsulate
 * the erroneous information.
 **/

public class UserMgrException extends Exception  {

    public static final int	NO_CMD_SPEC		= 0;
    public static final int	BAD_CMD_SPEC		= 1;
    public static final int	PASSWD_NOT_SPEC		= 2;
    public static final int	USERNAME_NOT_SPEC	= 4;
    public static final int	ROLE_NOT_SPEC		= 5;
    public static final int	INVALID_ROLE_SPEC	= 6;
    public static final int	PW_FILE_NOT_FOUND	= 7;
    public static final int	PW_FILE_FORMAT_ERROR	= 8;
    public static final int	USER_NOT_EXIST		= 9;
    public static final int	USER_ALREADY_EXIST	= 10;
    public static final int	PASSWD_INCORRECT	= 11;
    public static final int	PW_FILE_WRITE_ERROR	= 12;
    public static final int	PW_FILE_READ_ERROR	= 13;
    public static final int	ONLY_ONE_ANON_USER	= 14;
    public static final int	PASSWD_OR_ACTIVE_NOT_SPEC	= 15;
    public static final int	ILLEGAL_USERNAME	= 16;
    public static final int	BAD_ACTIVE_VALUE_SPEC	= 17;
    public static final int	PROBLEM_GETTING_INPUT	= 18;
    public static final int	ACTIVE_NOT_VALID_WITH_ADD= 19;
    public static final int	PASSWD_ENCRYPT_FAIL	= 20;
    public static final int	INSTANCE_NOT_EXISTS	= 21;
    public static final int	READ_PASSFILE_FAIL	= 22;
    public static final int	USERNAME_IS_EMPTY	= 23;
    public static final int	SRC_FILE_NOT_SPEC	= 24;
    public static final int	CANT_CREATE_INSTANCE	= 25;
    public static final int	CANT_CREATE_PWFILE      = 26;

    /**
     * Props object encapsulating the user specified options/commands.
     **/
    private UserMgrProperties	userMgrProps;
    private String		pwFile,
				userName;
    private Exception		linkedEx;
    private int			type;

    /**
     * Constructs an UserMgrException
     */ 
    public UserMgrException() {
        super();
        userMgrProps = null;
    }

    /** 
     * Constructs an UserMgrException with type
     *
     * @param  type       type of exception 
     **/
    public UserMgrException(int type) {
        super();
        userMgrProps = null;
	this.type = type;
    }

    public UserMgrException(int type, Throwable thr) {
        super(thr);
        userMgrProps = null;
        this.type = type;
    }

    /** 
     * Constructs an UserMgrException with reason
     *
     * @param  reason        a description of the exception
     **/
    public UserMgrException(String reason) {
        super(reason);
        userMgrProps = null;
    }

    public UserMgrException(int type, String reason) {
        super(reason);
        userMgrProps = null;
        this.type = type;
    }

    /**
     * Gets the properties object that encapsulates the user specified
     * options/commands.
     *
     * @return the properties object that encapsulates the user 
     *		specified options/commands.
     **/
    public synchronized UserMgrProperties getProperties() {
        return (userMgrProps);
    }

    /**
     * Sets the properties object that encapsulates the user specified
     * options/commands.
     *
     * @param p		the properties object that encapsulates the user 
     *			specified options/commands.
     **/
    public synchronized void setProperties(UserMgrProperties p) {
        userMgrProps = p;
    }

    /**
     * Gets the type of exception.
     *
     * @return the exception type.
     **/
    public synchronized int getType() {
	return (type);
    }

    public void setLinkedException(Exception ex)  {
	linkedEx = ex;
    }
    public Exception getLinkedException()  {
	return (linkedEx);
    }

    public void setUserName(String name)  {
	userName = name;
    }
    public String getUserName()  {
	return (userName);
    }

    public void setPasswordFile(String fileName)  {
	pwFile = fileName;
    }
    public String getPasswordFile()  {
	return (pwFile);
    }
}
