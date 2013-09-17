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
 * @(#)ObjMgrOptions.java	1.11 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.objmgr;

import javax.naming.Context;

/**
 * Interface containing constants for command line options,
 * property names and values for the JMS Object Administration
 * utility.
 */
public interface ObjMgrOptions  {
    /**
     * Property name representing what command
     * needs to be executed.
     */
    public static String OBJMGR_CMD_PROP_NAME	= "cmdtype";

    /**
     * The command line option indicating the add command.
     */
    public static String OBJMGR_ADD		= "add";

    /**
     * The property value for the OBJMGR_CMD_PROP_NAME property
     * indicating the add command.
     */
    public static String OBJMGR_ADD_PROP_VALUE	= "add";

    /**
     * The command line option indicating the delete command.
     */
    public static String OBJMGR_DELETE			= "delete";

    /**
     * The property value for the OBJMGR_CMD_PROP_NAME property
     * indicating the delete command.
     */
    public static String OBJMGR_DELETE_PROP_VALUE	= "delete";

    /**
     * The command line option indicating the query command.
     */
    public static String OBJMGR_QUERY			= "query";

    /**
     * The property value for the OBJMGR_CMD_PROP_NAME property
     * indicating the query command.
     */
    public static String OBJMGR_QUERY_PROP_VALUE	= "query";

    /**
     * The command line option indicating the list command.
     */
    public static String OBJMGR_LIST			= "list";

    /**
     * The property value for the OBJMGR_CMD_PROP_NAME property
     * indicating the list command.
     */
    public static String OBJMGR_LIST_PROP_VALUE		= "list";

    /**
     * The command line option indicating the update command.
     */
    public static String OBJMGR_UPDATE			= "update";

    /**
     * The property value for the OBJMGR_CMD_PROP_NAME property
     * indicating the update command.
     */
    public static String OBJMGR_UPDATE_PROP_VALUE	= "update";

    /**
     * The command line option used to specify the type of object.
     */
    public static String OBJMGR_TYPE			= "-t";

    /**
     * Property name representing the object type
     */
    public static String OBJMGR_TYPE_PROP_NAME		= "obj.type";

    /**
     * 
     */
    public static String OBJMGR_TYPE_PROP_VALUE		= null;

    /**
     * The command line option used to specify the 
     * lookup name of the object.
     */
    public static String OBJMGR_NAME			= "-l";

    /**
     * Property name representing the object lookup
     * name.
     */
    public static String OBJMGR_NAME_PROP_NAME		= "obj.lookupName";
    /**
     *
     */
    public static String OBJMGR_NAME_PROP_VALUE		= null;

    /**
     * The command line option used to specify if
     * the object is stored read-only.
     */
    public static String OBJMGR_READONLY		= "-r";
    /**
     * Property name to create,update the object
     * read-only.
     */
    public static String OBJMGR_READONLY_PROP_NAME	= "obj.readOnly";
    /**
     *
     */
    public static String OBJMGR_READONLY_PROP_VALUE	= null;

    /**
     * The command line option used to specify the 
     * attributes of the object.
     */
    public static String OBJMGR_OBJ_ATTRS		= "-o";

    /**
     * Base property name representing the object attributes.
     */
    public static String OBJMGR_OBJ_ATTRS_PROP_NAME	= "obj.attrs";

    /**
     *
     */
    public static String OBJMGR_OBJ_ATTRS_PROP_VALUE	= null;

    /**
     * The command line option used to specify 'force' or
     * 'no user interaction needed' mode.
     */
    public static String OBJMGR_FORCE			= "-f";

    /**
     * Property name representing 'force' or 'no user interaction 
     * needed' mode.
     */
    public static String OBJMGR_FORCE_PROP_NAME		= "force";

    /**
     * Property value representing 'force' or 'no user interaction 
     * needed' mode.
     */
    public static String OBJMGR_FORCE_PROP_VALUE	= "true";

    /**
     * The command line option used to specify the 
     * attributes of the object store.
     */
    public static String OBJMGR_OBJSTORE_ATTRS		= "-j";

    /**
     * Base property name representing the object store
     * attributes.
     */
    public static String OBJMGR_OBJSTORE_ATTRS_PROP_NAME	= "objstore.attrs";

    /**
     *
     */
    public static String OBJMGR_OBJSTORE_ATTRS_PROP_VALUE	= null;

    /**
     * The command line option used to specify the 
     * bind attributes.
     */
    public static String OBJMGR_OBJSTORE_BIND_ATTRS          = "-b";

    /**
     * Base property name representing the bind attributes.
     */
    public static String OBJMGR_OBJSTORE_BIND_ATTRS_PROP_NAME = "objstore.bind.attrs";

    /**
     *
     */
    public static String OBJMGR_OBJSTORE_BIND_ATTRS_PROP_VALUE       = null;

    /**
     *
     */
    public static String OBJMGR_PREVIEW			= "-pre";
    public static String OBJMGR_PREVIEW_PROP_NAME	= "preview";
    public static String OBJMGR_PREVIEW_PROP_VALUE	= "true";

    public static String OBJMGR_INPUTFILE		= "-i";
    public static String OBJMGR_INPUTFILE_PROP_NAME	= "inputFile";
    public static String OBJMGR_INPUTFILE_PROP_VALUE	= null;

    public static String OBJMGR_SILENTMODE		= "-s";
    public static String OBJMGR_SILENTMODE_PROP_NAME	= "silent";
    public static String OBJMGR_SILENTMODE_PROP_VALUE	= "true";

    /**
     * Types of JMQ objects we can create.
     */
    public static final String OBJMGR_TYPE_TOPIC	= "t";
    public static final String OBJMGR_TYPE_QUEUE	= "q";
    public static final String OBJMGR_TYPE_TCF		= "tf";
    public static final String OBJMGR_TYPE_QCF		= "qf";
    public static final String OBJMGR_TYPE_CF		= "cf";
    public static final String OBJMGR_TYPE_XTCF		= "xtf";
    public static final String OBJMGR_TYPE_XQCF		= "xqf";
    public static final String OBJMGR_TYPE_XCF		= "xcf";

    public static final String OBJMGR_SHORT_HELP1	= "-h";
    public static final String OBJMGR_SHORT_HELP2	= "-help";
    public static final String OBJMGR_LONG_HELP1	= "-H";
    public static final String OBJMGR_LONG_HELP2	= "-Help";

    public static final String OBJMGR_VERSION1		= "-v";
    public static final String OBJMGR_VERSION2		= "-version";
}

