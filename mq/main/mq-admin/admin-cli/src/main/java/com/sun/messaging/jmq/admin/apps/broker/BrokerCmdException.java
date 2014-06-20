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
 */ 

package com.sun.messaging.jmq.admin.apps.broker;

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
 * in a BrokerCmdProperties object. This exception will
 * contain a BrokerCmdProperties object to encapsulate
 * the erroneous information.
 **/

public class BrokerCmdException extends CommonCmdException  {

    /****************************************************************************
     * use integer 0 -1000 to avoid overlap with super class and other subclasses
     ****************************************************************************/

    public static final int     TARGET_NAME_NOT_SPEC    = 2;
    public static final int     DEST_NAME_NOT_SPEC  = 5;
    public static final int     TARGET_ATTRS_NOT_SPEC   = 6;
    public static final int     DEST_TYPE_NOT_SPEC  = 7;
    public static final int     FLAVOUR_TYPE_INVALID    = 8;
    public static final int     INVALID_DEST_TYPE   = 10;
    public static final int     CLIENT_ID_NOT_SPEC  = 11;
    public static final int     BAD_ATTR_SPEC_CREATE_DST_QUEUE  = 12;
    public static final int     BAD_ATTR_SPEC_CREATE_DST_TOPIC  = 13;
    public static final int     BAD_ATTR_SPEC_UPDATE_BKR    = 14;
    public static final int     BAD_ATTR_SPEC_UPDATE_DST_QUEUE  = 15;
    public static final int     BAD_ATTR_SPEC_UPDATE_DST_TOPIC  = 16;
    public static final int     BAD_ATTR_SPEC_UPDATE_SVC    = 17;
    public static final int     INVALID_BOOLEAN_VALUE      = 18;
    public static final int     INVALID_LOG_LEVEL_VALUE = 19;
    public static final int     INVALID_METRIC_INTERVAL = 20;
    public static final int     INVALID_METRIC_TYPE = 21;
    public static final int     INVALID_BYTE_VALUE         = 22;
    public static final int     BAD_ATTR_SPEC_GETATTR = 24;
    public static final int     SINGLE_TARGET_ATTR_NOT_SPEC = 25;
    public static final int     BAD_ATTR_SPEC_PAUSE_DST     = 26;
    public static final int     INVALID_PAUSE_TYPE      = 27;
    public static final int     INVALID_METRIC_DST_TYPE     = 28;
    public static final int     INVALID_METRIC_SAMPLES      = 29;
    public static final int     INVALID_LIMIT_BEHAV_VALUE   = 31;
    public static final int     DST_QDP_VALUE_INVALID       = 32;
    public static final int     BKR_QDP_VALUE_INVALID       = 33;
    public static final int     INVALID_RESET_TYPE      = 36;
    public static final int     MSG_ID_NOT_SPEC         = 37;
    public static final int     UPDATE_DST_ATTR_SPEC_CREATE_ONLY_QUEUE  = 38;
    public static final int     UPDATE_DST_ATTR_SPEC_CREATE_ONLY_TOPIC  = 39;
    public static final int     BAD_ATTR_SPEC_CHANGEMASTER  = 40;



    public BrokerCmdException() {
        super();
    }

    /** 
     * Constructs an BrokerCmdException with type
     *
     * @param  type       type of exception 
     **/
    public BrokerCmdException(int type) {
        super(type);
    }

    /** 
     * Constructs an BrokerCmdException with reason
     *
     * @param  reason        a description of the exception
     **/
    public BrokerCmdException(String reason) {
        super(reason);
    }

}
