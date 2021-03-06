/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * @(#)PacketFlag.hpp	1.4 06/26/07
 */ 

#ifndef PACKETFLAG_HPP
#define PACKETFLAG_HPP

#include <nspr.h>

static const PRUint16 PACKET_FLAG_IS_QUEUE            =  1 << 0;  // Q_FLAG
static const PRUint16 PACKET_FLAG_REDELIVERED         =  1 << 1;  // R_FLAG
static const PRUint16 PACKET_FLAG_PERSISTENT          =  1 << 2;  // P_FLAG
static const PRUint16 PACKET_FLAG_SELECTORS_PROCESSED =  1 << 3;  // S_FLAG
static const PRUint16 PACKET_FLAG_SEND_ACK            =  1 << 4;  // A_FLAG
static const PRUint16 PACKET_FLAG_LAST_MESSAGE        =  1 << 5;  // L_FLAG
static const PRUint16 PACKET_FLAG_FLOW_PAUSED         =  1 << 6;  // F_FLAG
static const PRUint16 PACKET_FLAG_PART_OF_TRANSACTION =  1 << 7;  // T_FLAG
static const PRUint16 PACKET_FLAG_CONSUMER_FLOW_PAUSED = 1 << 8;  // C_FLAG
static const PRUint16 PACKET_FLAG_SERVER_PACKET        = 1 << 9;  // B_FLAG


/**
 * This class defines bit masks for the iMQ packet header flags.
 */
class PacketFlag {
public:
  static const char * isQueueStr(const PRUint16 bitFlags);
  static const char * isRedeliveredStr(const PRUint16 bitFlags);
  static const char * isPersistentStr(const PRUint16 bitFlags);
  static const char * isSelectorsProcessedStr(const PRUint16 bitFlags);
  static const char * isSendAckStr(const PRUint16 bitFlags);
  static const char * isLastMessageStr(const PRUint16 bitFlags);
  static const char * isFlowPausedStr(const PRUint16 bitFlags);
  static const char * isPartOfTransactionStr(const PRUint16 bitFlags);
  static const char * isConsumerFlowPausedStr(const PRUint16 bitFlags);
  static const char * isServerPacketStr(const PRUint16 bitFlags);
};


#endif //  PACKETFLAG_HPP
