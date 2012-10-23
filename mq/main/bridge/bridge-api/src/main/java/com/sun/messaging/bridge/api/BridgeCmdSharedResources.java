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

package com.sun.messaging.bridge.api;

/**
 * This interface is shared by imqbridgemgr, BridgeServiceManager
 * and individual services. It contains imqbridgemgr string resource
 * keys that are referenced by all. 
 *
 * The properties for the keys in this file are defined in imqbridgemgr
 * resource properties file
 *
 * @author amyk
 *
 */

public interface BridgeCmdSharedResources {

    // 1000-1999 Informational Messages
    final public static String I_BGMGR_TITLE_BRIDGE_NAME    = "BS1000";
    final public static String I_BGMGR_TITLE_BRIDGE_TYPE    = "BS1001";
    final public static String I_BGMGR_TITLE_BRIDGE_STATE   = "BS1002";

    final public static String I_BGMGR_TITLE_NUM_LINKS      = "BS1003";

    final public static String I_BGMGR_TITLE_LINK_NAME        = "BS1004";
    final public static String I_BGMGR_TITLE_LINK_STATE       = "BS1005";
    final public static String I_BGMGR_TITLE_SOURCE      = "BS1006";
    final public static String I_BGMGR_TITLE_TARGET      = "BS1007";
    final public static String I_BGMGR_TITLE_TRANSACTED  = "BS1008";

    final public static String I_BGMGR_TITLE_TRANSACTIONS  = "BS1009";

    final public static String I_BGMGR_TITLE_POOLED      = "BS1010";
    final public static String I_BGMGR_TITLE_NUM_INUSE   = "BS1011";
    final public static String I_BGMGR_TITLE_NUM_IDLE    = "BS1012";
    final public static String I_BGMGR_TITLE_IDLE        = "BS1013";
    final public static String I_BGMGR_TITLE_TIMEOUT     = "BS1014";
    final public static String I_BGMGR_TITLE_MAX         = "BS1015";
    final public static String I_BGMGR_TITLE_RETRIES     = "BS1016";
    final public static String I_BGMGR_TITLE_RETRY       = "BS1017";
    final public static String I_BGMGR_TITLE_INTERVAL    = "BS1018";

    final public static String I_BGMGR_TITLE_SHARED = "BS1019";
    final public static String I_BGMGR_TITLE_REF    = "BS1020";
    final public static String I_BGMGR_TITLE_COUNT  = "BS1021";


    final public static String I_STATE_UNINITIALIZED  = "BS1500";
    final public static String I_STATE_STARTING  = "BS1501";
    final public static String I_STATE_STARTED  = "BS1502";
    final public static String I_STATE_STOPPING  = "BS1503";
    final public static String I_STATE_STOPPED  = "BS1504";
    final public static String I_STATE_PAUSING  = "BS1505";
    final public static String I_STATE_PAUSED  = "BS1506";
    final public static String I_STATE_RESUMING  = "BS1507";

}
