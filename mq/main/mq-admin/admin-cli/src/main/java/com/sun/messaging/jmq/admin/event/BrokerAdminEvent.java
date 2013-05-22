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
 * @(#)BrokerAdminEvent.java	1.23 06/27/07
 */ 

package com.sun.messaging.jmq.admin.event;

import java.util.Properties;
import com.sun.messaging.jmq.util.admin.DestinationInfo;

/**
 * Event class indicating some actions related to
 * Broker Management.
 *<P>
 * The fields of this event include the various pieces of information
 * needed for broker management tasks.
 */
public class BrokerAdminEvent extends AdminEvent {
    /*
     * BrokerAdminEvent event types
     */
    public final static int	ADD_BROKER		= 1;
    public final static int   	ADD_DEST                = 2;
    public final static int   	UPDATE_LOGIN            = 3;
    public final static int   	UPDATE_BROKER		= 4;
    public final static int   	DELETE_DUR		= 5;
    public final static int   	UPDATE_SVC		= 6;
    public final static int   	UPDATE_DEST		= 7;
    public final static int   	QUERY_BROKER		= 8;
    public final static int   	UPDATE_BROKER_ENTRY	= 9;
    public final static int   	PURGE_DUR		= 10;

    private Properties		bkrProps;

    private boolean             connect = true;
    private String		brokerName = null;
    private String		host = null;
    private int			port = -1;
    private String		username = null;
    private String		passwd = null;

    private String		destName = null;
    private int			destMask = -1;
    private int 		activeConsumers = 0;
    private int 		failoverConsumers = 0;
    private int 		maxProducers = 0;
    private long		maxMesgBytes = 0;
    private int			maxMesg = 0;
    private long		maxPerMesgSize = 0;

    private String		durableName = null;
    private String		clientID = null;

    private int			minThreads = -1;
    private int			maxThreads = -1;

    private int			limitBehavior = -1;
    private boolean             useDMQ = true;

    private DestinationInfo     destInfo = null;

    private boolean             okAction = true;

    /**
     * Creates an instance of BrokerAdminEvent
     * @param source the object where the event originated
     */
    public BrokerAdminEvent(Object source) {
	super(source);
    }

    /**
     * Creates an instance of BrokerAdminEvent
     * @param source the object where the event originated
     * @type the event type
     */
    public BrokerAdminEvent(Object source, int type) {
	super(source, type);
    }

    /*
     * Sets whether to attempt to connect to the broker
     * when adding/updating it.
     *
     * @param connect True if try to connect, false
     *                otherwise.
     */
    public void setConnectAttempt(boolean connect)  {
        this.connect = connect;
    }
    /*
     * Returns whether or not to attempt to connect to the
     * broker when adding/updating it.
     *
     * @return True if attempt to connect, false
     *         otherwise.
     */
    public boolean isConnectAttempt()  {
        return (connect);
    }

    public void setBrokerName(String brokerName) {
	this.brokerName = brokerName;
    }
    public String getBrokerName() {
	return (brokerName);
    }

    public void setHost(String host) {
	this.host = host;
    }

    public String getHost() {
	return host;
    }

    public void setPort(int port) {
	this.port = port;
    }

    public int getPort() {
	return port;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUsername() {
	return username;
    }

    public void setPassword(String passwd) {
	this.passwd = passwd;
    }

    public String getPassword() {
	return passwd;
    }

    public void setDestinationName(String name) {
	this.destName = name;
    }

    public String getDestinationName() {
	return destName;
    }

    public void setDestinationTypeMask(int mask) {
	this.destMask = mask;
    }

    public int getDestinationTypeMask() {
	return destMask;
    }

    public void setMaxMesgBytes(long bytes) {
	this.maxMesgBytes = bytes;
    }

    public long getMaxMesgBytes() {
	return maxMesgBytes;
    }

    public void setActiveConsumers(int number) {
	this.activeConsumers = number;
    }

    public int getActiveConsumers() {
	return activeConsumers;
    }

    public void setFailoverConsumers(int number) {
	this.failoverConsumers = number;
    }

    public int getFailoverConsumers() {
	return failoverConsumers;
    }

    public void setMaxProducers(int number) {
	this.maxProducers = number;
    }

    public int getMaxProducers() {
	return maxProducers;
    }

    public void setMaxMesg(int number) {
	this.maxMesg = number;
    }

    public int getMaxMesg() {
	return maxMesg;
    }

    public void setMaxPerMesgSize(long bytes) {
	this.maxPerMesgSize = bytes;
    }

    public long getMaxPerMesgSize() {
	return maxPerMesgSize;
    }

    public void setDurableName(String name) {
	this.durableName = name;
    }

    public String getDurableName() {
	return durableName;
    }

    public void setClientID(String id) {
	this.clientID = id;
    }

    public String getClientID() {
	return clientID;
    }

    public void setMinThreads(int min) {
	this.minThreads = min;
    }

    public int getMinThreads() {
	return minThreads;
    }

    public void setMaxThreads(int max) {
	this.maxThreads = max;
    }

    public int getMaxThreads() {
	return maxThreads;
    }

    /*
     * Set whether this event is trigerred by an 'OK' action.
     * This information is used to determine whether the originating
     * dialog (if one was involved) needs to be hidden.
     *
     * @param b True if this is an 'OK' action, false
     *		otherwise.
     */
    public void setOKAction(boolean b)  {
	this.okAction = b;
    }

    /*
     * Returns whether this event is trigerred by an 'OK' action.
     * @return True if this is an 'OK' action, false
     *		otherwise.
     */
    public boolean isOKAction()  {
	return (okAction);
    }

    public void setBrokerProps(Properties bkrProps)  {
	this.bkrProps = bkrProps;
    }
    public Properties getBrokerProps()  {
	return(bkrProps);
    }

    public void setLimitBehavior(int limitBehavior)  {
	this.limitBehavior = limitBehavior;
    }
    public int getLimitBehavior()  {
	return(limitBehavior);
    }

    public void setUseDMQ(boolean useDMQ)  {
	this.useDMQ = useDMQ;
    }
    public boolean useDMQ()  {
	return(useDMQ);
    }

    public void setDestinationInfo(DestinationInfo destInfo)  {
	this.destInfo = destInfo;
    }
    public DestinationInfo getDestinationInfo()  {
	return(destInfo);
    }
}
