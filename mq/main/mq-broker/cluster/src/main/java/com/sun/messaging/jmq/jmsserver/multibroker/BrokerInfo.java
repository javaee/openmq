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
 * @(#)BrokerInfo.java	1.15 07/02/07
 */ 

package com.sun.messaging.jmq.jmsserver.multibroker;

import java.io.*;
import java.net.InetAddress;

import com.sun.messaging.jmq.jmsserver.core.BrokerAddress;
import com.sun.messaging.jmq.util.UID;

/**
 * This class encapsulates general information about a broker.
 * Each broker maintains a list of <code> BrokerInfo </code> objects
 * representing the brokers known to be in the same cluster.
 */
public class BrokerInfo implements Serializable {
    static final long serialVersionUID = 6384851141864345643L;

    private static boolean DEBUG = false;

    private BrokerAddress brokerAddr = null;
    private String description = null;
    private long startTime = 0;
    private boolean storeDirtyFlag = false;

    private String heartbeatHostAddress = null ;
    private int heartbeatPort = -1;
    private int heartbeatInterval = 0;

	private Integer clusterProtocolVersion = null;

    private transient String realRemote =  null;

    public BrokerInfo() {
    }

	public Integer getClusterProtocolVersion() {
		return clusterProtocolVersion;
	}

	public void setClusterProtocolVersion(Integer v) {
		this.clusterProtocolVersion = v;
	}

    public void setBrokerAddr(BrokerAddress brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public BrokerAddress getBrokerAddr() {
        return brokerAddr;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStoreDirtyFlag(boolean storeDirtyFlag) {
        this.storeDirtyFlag = storeDirtyFlag;
    }

    public boolean getStoreDirtyFlag() {
        return storeDirtyFlag;
    }

    public void setHeartbeatHostAddress(String ip) {
        heartbeatHostAddress =  ip;
    }

    public String getHeartbeatHostAddress() {
        return heartbeatHostAddress;
          
    }

    public void setHeartbeatPort(int p) {
        heartbeatPort = p;
    }

    public int getHeartbeatPort() {
        return heartbeatPort;
    }

    public void setHeartbeatInterval(int s) {
        heartbeatInterval = s;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(
                              "\n\tAddress = " + brokerAddr +
                              "\n\tStartTime = " + startTime +
          ((DEBUG == true) ? ("\n\tDescription = " + description +
                              "\n\tStoreDirty = " + storeDirtyFlag): "")+
                              "\n\tProtocolVersion = " + clusterProtocolVersion);
        if (heartbeatHostAddress != null) {
            sb.append("\n\tHeartbeatHost = " + heartbeatHostAddress +
                      "\n\tHeartbeatPort = " + heartbeatPort);
        }
        return sb.toString();
    }

    public void setRealRemoteString(String str) {
        this.realRemote = str; 
    }

    public String getRealRemoteString() {
        return realRemote;
    }
}

/*
 * EOF
 */
