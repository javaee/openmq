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
 * @(#)BrokerInfo.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.util.admin;

/**
 * Broker encapsulates information about a JMQ Broker service. It
 * is used to pass this information between the Broker and an
 * administration client.
 */
public class BrokerInfo extends AdminInfo implements java.io.Serializable {

    /**
     * Broker's name. Not updateable
     */
    public String name;


    /*--------------------- Properties that can be updated -----------------*/

    /**
     * True for the broker to automatically create topics when messages
     * are published.
     */
    public boolean autoCreateTopic;

    /**
     * True for the broker to automatically create queues when messages
     * are published.
     */
    public boolean autoCreateQueue;

    /**
     * primary broker port (portmapper port)
     */
    public int		port;

    /**
     * Broker log level: "ERROR", "WARNING", "INFO", "DEBUG", "DEBUGMED"
     * "DEBUGHIGH"
     */
    public String	logLevel;

    /**
     * Bit mask contiaing log destinations. Some combination of
     * LOG_FILE, LOG_STREAM and LOG_TOPIC
     */
    public int	        logDestination;


    /**
     * Rollover criteria for file logging
     */
    public long		logRolloverBytes;
    public int		logRolloverSeconds;

    /**
     * Interval in seconds to generate metric report. 0 for no metric
     * reporting
     */
    public int      metricInterval;

    /**
     * True to log metric reports
     */
    public boolean  metricLog;

    /**
     * True to send metric data to the metric topic
     */
    public boolean  metricTopic;

    /**
     * The maximum number of messages the broker will hold in memory
     * before it starts paging them to disk
     */
    public int maxMsgsInMemory;

    /**
     * The maximum number of message bytes the broker will hold in memory
     * before it starts paging them to disk
     */
    public long maxBytesInMemory;

    /**
     * The maximum number of messages the broker can hold (in disk and
     * in memory.
     */
    public int maxMsgsInBroker;

    /**
     * The maximum number of message bytes the broker can hold (in disk and
     * in memory.
     */
    public long maxBytesInBroker;

    /**
     * The maximum message size the broker will accept
     */
    public long maxMessageSize;

    /**
     * Logging destination bitmasks
     */
    public static final int LOG_FILE   = 0x00000001;
    public static final int LOG_STREAM = 0x00000002;
    public static final int LOG_TOPIC  = 0x00000004;

    /**
     * Bit masks for updateable properties
     */
    public static final int AUTO_CREATE_TOPIC   = 0x00000001;
    public static final int AUTO_CREATE_QUEUE   = 0x00000002;
    public static final int PORT                = 0x00000004;
    public static final int LOG_DESTINATION     = 0x00000008;
    public static final int LOG_LEVEL           = 0x00000010;
    public static final int LOG_ROLLOVER_BYTES  = 0x00000080;
    public static final int LOG_ROLLOVER_SECONDS= 0x00000100;
    public static final int METRIC_INTERVAL     = 0x00000200;
    public static final int METRIC_LOG          = 0x00000400;
    public static final int METRIC_TOPIC        = 0x00000800;
    public static final int MAX_MSGS_IN_MEMORY  = 0x00001000;
    public static final int MAX_BYTES_IN_MEMORY = 0x00002000;
    public static final int MAX_MSGS_IN_BROKER  = 0x00004000;
    public static final int MAX_BYTES_IN_BROKER = 0x00008000;
    public static final int MAX_MESSAGE_SIZE    = 0x00010000;

    /**
     * Constructor for BrokerInfo.
     *
     */
    public BrokerInfo() {
	reset();
    }

    public void reset() {
        name = null;
        autoCreateTopic = true;
        autoCreateQueue = false;
        port = 0;
        logLevel = "WARNING";
        logDestination = 0;
        logRolloverBytes = 0;
        logRolloverSeconds = 60 * 60 * 24 * 7;
        metricInterval = 0;
        metricLog = true;
        metricTopic = false;
        maxMsgsInMemory = 0;
        maxBytesInMemory = 0;
        maxMsgsInBroker = 0;
        maxBytesInBroker = 0;
        maxMessageSize = 0;
        resetMask();
    }

    /**
     * Return a string representation of the service. 
     *
     * @return String representation of the service.
     */
    public String toString() {
	return  "{" + name + "}";
    }

    /**
     * Set the primary (portmapper) port for the Broker
     *
     * @param port	Broker's port number
     */
    public void setPort(int port) {
	this.port = port;
        setModified(PORT);
    }

    /**
     * Set if topic autocreation should be enabled on the broker
     *
     * @param b	true to turn topic autocreation on, else false
     */
    public void setAutoCreateTopic(boolean b) {
	this.autoCreateTopic = b;
        setModified(AUTO_CREATE_TOPIC);
    }

    /**
     * Set if queue autocreation should be enabled on the broker
     *
     * @param b	true to turn queue autocreation on, else false
     */
    public void setAutoCreateQueue(boolean b) {
	this.autoCreateQueue = b;
        setModified(AUTO_CREATE_QUEUE);
    }

    /**
     * Set broker's log level.
     *
     * @param level Should be one of: "ERROR", "WARNING", 
     * "INFO", "DEBUG", "DEBUGMED", "DEBUGHIGH"
     *
     */
    public void setLogLevel(String level) {
	this.logLevel = level;
        setModified(LOG_LEVEL);
    }

    /**
     * Set logging destination(s). This is a bit mask of some combination of
     * LOG_FILE, LOG_STREAM and LOG_TOPIC
     *
     */
    public void setLogDestination(int d) {
	this.logDestination = d;
        setModified(LOG_DESTINATION);
    }

    /**
     * Sets the rollover file size in bytes for the log file. 0 to not
     * rollover based on size.
     */
    public void setLogRolloverBytes(long n) {
	this.logRolloverBytes = n;
        setModified(LOG_ROLLOVER_BYTES);
    }

    /**
     * Sets the rollover time in seconds for the log file.  0 to not
     * rollover based on time.
     */
    public void setLogRolloverSeconds(int n) {
	this.logRolloverSeconds = n;
        setModified(LOG_ROLLOVER_SECONDS);
    }

    /**
     * Sets the interval, in seconds that metric reports are generated.
     * 0 to not generate reports.
     */
    public void setMetricInterval(int n) {
	this.metricInterval = n;
        setModified(METRIC_INTERVAL);
    }

    /**
     * True to send metric reports to the log file. Else false.
     */
    public void setMetricInterval(boolean b) {
	this.metricLog = b;
        setModified(METRIC_LOG);
    }

    /**
     * True to send metric reports to the log topic. Else false.
     */
    public void setMetricTopic(boolean b) {
	this.metricTopic = b;
        setModified(METRIC_TOPIC);
    }

    /**
     * Set's the max number of messages that the broker will hold in
     * memory before paging them to disk.
     */
    public void setMaxMsgsInMemory(int n) {
	this.maxMsgsInMemory = n;
        setModified(MAX_MSGS_IN_MEMORY);
    }

    /**
     * Set's the max number of message bytes that the broker will hold in
     * memory before paging messages to disk.
     */
    public void setMaxBytesInMemory(long n) {
	this.maxBytesInMemory = n;
        setModified(MAX_BYTES_IN_MEMORY);
    }

    /**
     * Set's the max number of messages that the broker will hold before
     * it starts rejecting messages
     */
    public void setMaxMsgsInBroker(int n) {
	this.maxMsgsInBroker = n;
        setModified(MAX_MSGS_IN_BROKER);
    }

    /**
     * Set's the max number of message bytes that the broker will hold before
     * it starts rejecting messages
     */
    public void setMaxBytesInBroker(long n) {
	this.maxBytesInBroker = n;
        setModified(MAX_BYTES_IN_BROKER);
    }

    /**
     * Set's the max message size the broker will accept
     */
    public void setMaxMessageSize(long n) {
	this.maxMessageSize = n;
        setModified(MAX_MESSAGE_SIZE);
    }

}
