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
 * @(#)OptionDesc.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.util.options;

/**
 * This class describes a command line option:
 *
 * <UL>
 * <LI>it's type
 * <LI>the actual option string
 * <LI>it's base property
 * <LI>it's value (if type is OptionType.OPTION_VALUE_HARDCODED)
 * </UL>
 *
 * @see		OptionType
 */
public class OptionDesc {
    /**
     * The type of the option, as defined by the interface OptionType.
     * The valid values are:
     * <UL>
     * <LI>OPTION_VALUE_HARDCODED
     * <LI>OPTION_VALUE_NEXT_ARG
     * <LI>OPTION_VALUE_NEXT_ARG_RES
     * </UL>
     *
     * @see	OptionType
     */
    public int		type;

    /**
     * The actual option, for example <EM>-a</EM>
     */
    public String	option;

    /**
     * The property name that will be associated with this
     * option. This property name may be a basename for
     * the actual property used.
     *
     * @see  
     * com.sun.messaging.jmq.admin.util.OptionType#OPTION_VALUE_NEXT_ARG_RES
     */
    public String	baseProperty;

    public String	nameValuePair = null;

    /**
     * Flag indicating whether this option should be parsed, but
     * not processed (i.e. stored in properties database).
     */
    public boolean	ignore = false;

    /**
     * Value of the property for this option. See
     * OptionType.OPTION_VALUE_HARDCODED.
     *
     * @see	OptionType
     */
    public String	value;
    
    public OptionDesc(String option, int type, String baseProp, String value)  {
	this(option, type, baseProp, value, null, false);
    }

    public OptionDesc(String option, int type, String baseProp, String value,
				String nameValuePair)  {
	this(option, type, baseProp, value, nameValuePair, false);
    }


    public OptionDesc(String option, int type, String baseProp,
				String value, boolean ignore)  {
	this(option, type, baseProp, value, null, ignore);
    }

    public OptionDesc(String option, int type, String baseProp,
				String value, String nameValuePair,
				boolean ignore)  {
	this.type = type;
	this.option = option;
	this.baseProperty = baseProp;
	this.value = value;
	this.nameValuePair = nameValuePair;
	this.ignore = ignore;
    }
}
