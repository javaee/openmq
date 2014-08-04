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
 * @(#)ObjMgrException.java	1.4 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.objmgr;

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
 * in a ObjMgrProperties object. This exception will
 * contain a ObjMgrProperties object to encapsulate
 * the erroneous information.
 **/

public class ObjMgrException extends Exception  {

    public static final int		NO_CMD_SPEC		= 0;
    public static final int		BAD_CMD_SPEC		= 1;
    public static final int		NO_OBJ_TYPE_SPEC	= 2;
    public static final int		INVALID_OBJ_TYPE	= 3;
    public static final int		NO_LOOKUP_NAME_SPEC	= 4;
    public static final int		NO_DEST_NAME_SPEC	= 5;
    public static final int		INVALID_READONLY_VALUE  = 6;

    /**
     * Props object encapsulating the user specified options/commands.
     **/
    private ObjMgrProperties objMgrProps;
    private int type;

    /**
     * Constructs an ObjMgrException
     */ 
    public ObjMgrException() {
        super();
        objMgrProps = null;
    }

    /** 
     * Constructs an ObjMgrException with type
     *
     * @param  type       type of exception 
     **/
    public ObjMgrException(int type) {
        super();
        objMgrProps = null;
	this.type = type;
    }

    /** 
     * Constructs an ObjMgrException with reason
     *
     * @param  reason        a description of the exception
     **/
    public ObjMgrException(String reason) {
        super(reason);
        objMgrProps = null;
    }

    /**
     * Gets the properties object that encapsulates the user specified
     * options/commands.
     *
     * @return the properties object that encapsulates the user 
     *		specified options/commands.
     **/
    public ObjMgrProperties getProperties() {
        return (objMgrProps);
    }

    /**
     * Sets the properties object that encapsulates the user specified
     * options/commands.
     *
     * @param p		the properties object that encapsulates the user 
     *			specified options/commands.
     **/
    public synchronized void setProperties(ObjMgrProperties p) {
        objMgrProps = p;
    }

    /**
     * Gets the type of exception.
     *
     * @return the exception type.
     **/
    public synchronized int getType() {
	return (type);
    }
}
