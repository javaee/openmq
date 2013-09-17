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
 * @(#)OptionException.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.util.options;

/**
 * This exception provides information about problems
 * encountered when processing command line options.
 *
 * <P>It provides following information:
 * <UL>
 *   <LI> A string describing the error - This string is 
 *        the standard Java exception message, and is available via 
 *        getMessage().
 *   <LI>The command line option that is relevant.
 * </UL>
 **/

public class OptionException extends Exception {

    /**
     * Stored command line option
     **/
    private String option;

    /**
     * Constructs an OptionException
     */ 
    public OptionException() {
        super();
        option = null;
    }

    /** 
     * Constructs an OptionException with reason
     *
     * @param  reason        a description of the exception
     **/
    public OptionException(String reason) {
        super(reason);
        option = null;
    }

    /**
     * Gets the command line option that is relevant to the exception.
     *
     * @return the command line option
     **/
    public String getOption() {
        return (option);
    }

    /**
     * Sets the command line option that is relevant to the exception.
     *
     * @param o       the command line option
     **/
    public synchronized void setOption(String o) {
        option = o;
    }
}
