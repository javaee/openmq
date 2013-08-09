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
 * @(#)OptionType.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.util.options;

/**
 * This interface defines constants for command line option types.
 *
 * <P>
 * Every command line option has a property name and value
 * associated with it.
 *
 * <P>
 * The different option types vary depending on where their
 * property values come from and the format of the value.
 *
 * @see		OptionDesc
 */
public interface OptionType {

    /**
     * Options that have a hardcoded value. The value will not be
     * specified on the command line. Their value will come from 
     * the OptionDesc class. Examples:
     * <UL>
     * <LI>jmqobjmgr -a
     * <LI>jmqobjmgr -d
     * <LI>jmqobjmgr -f
     * </UL>
     */
    public static int	OPTION_VALUE_HARDCODED		= 1;

    /**
     * Options that have a value specified on the command
     * line. The value is the very next argument on the
     * command line. Examples:
     * <UL>
     * <LI>jmqobjmgr -t qf
     * <LI>jmqobjmgr -i "com.sun.jndi.ldap.LdapCtxFactory"
     * </UL>
     *
     */
    public static int	OPTION_VALUE_NEXT_ARG		= 2;

    /**
     * Options that have a value specified on the command
     * line. The value has a name/value pair format:
     *		<EM>name=value</EM>
     * and is the very next argument on the command line.
     * The property that will represent this option will
     * be the concatenation of the base property for this
     * option (see OptionDesc class) and the <EM>name</EM>
     * portion of the name/value pair.
     * Examples:
     * <UL>
     * <LI>jmqobjmgr -o "foo=bar"
     * </UL>
     * In this example, if the base property for <EM>-o</EM> is
     * <EM>obj.attrs</EM>, the relevant property and value here
     * will be: <EM>obj.attrs.foo=bar</EM>
     *
     */
    public static int	OPTION_VALUE_NEXT_ARG_RES	= 3;

    /**
     * Options that have a value specified on the command
     * line. The value has a name/value pair format:
     *		<EM>name=value</EM>
     * and is appended(ie is a suffix to) the option.
     *
     * The property that will represent this option will
     * be the concatenation of the base property for this
     * option (see OptionDesc class) and the <EM>name</EM>
     * portion of the name/value pair.
     * Examples:
     * <UL>
     * <LI>imqcmd -Dfoo=bar
     * </UL>
     * In this example, if the base property for <EM>-D</EM> is
     * <EM>sys.props</EM>, the relevant property and value here
     * will be: <EM>sys.props.foo=bar</EM>
     */
    public static int	OPTION_VALUE_SUFFIX_RES	= 4;
}
