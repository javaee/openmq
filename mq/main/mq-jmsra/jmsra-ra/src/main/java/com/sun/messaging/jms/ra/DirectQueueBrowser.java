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

package com.sun.messaging.jms.ra;

import javax.jms.*;

import java.util.Enumeration;
import java.util.logging.Logger;

import com.sun.messaging.jmq.io.JMSPacket;
import com.sun.messaging.jmq.jmsservice.JMSService;
import com.sun.messaging.jmq.jmsservice.JMSServiceReply;
import com.sun.messaging.jmq.jmsservice.JMSServiceException;

/**
 *
 */
public class DirectQueueBrowser
implements Enumeration<javax.jms.Message>, javax.jms.QueueBrowser {

    /**
     *  The JMSService for this DirectQueueBrowser
     */
    private JMSService jmsservice;

    /**
     *  The parent DirectSession that created this DirectQueueBrowser
     */
    private DirectSession ds;

    /**
     *  The connectionId of the parent DirectConnection
     */
    private long connectionId;

    /**
     *  The sessionId of the parent DirectSession
     */
    private long sessionId;

    /**
     *  The consumerId for this DirectQueueBrowser
     */
    private long consumerId = 0L;

    /**
     *  The JMSService Destination that is associated with this
     *  DirectQueueBrowser
     */
    private com.sun.messaging.jmq.jmsservice.Destination jmsservice_destination;

    /**
     *  The array of message returned from the JMSService that matches the
     *  QueueBrowser criteria.
     */
    private JMSPacket[] browserMessages;

    /**
     *  The size of the array of messages returned from the JMSService
     */
    private int size;

    /**
     *  The cursor for marching through the array of message when the 
     *  getNextElement() method is called on the Enumeration
     */
    private int cursor;

    /**
     *  The JMS Queue that is associated with this DirectQueueBrowser
     */
    private Queue destination = null;

    /**
     *  The JMS Message Selector that was used for this DirectQueueBrowser
     */
    private String msgSelector;

    /**
     *  Holds the closed state of this DirectQueueBrowser
     */
    private boolean isClosed = false;

    /** For optimized logging */
    protected int _logLevel;
    protected boolean _logFINE = false;

    /**
     *  Logging
     */
    private static transient final String _className =
            "com.sun.messaging.jms.ra.DirectQueueBrowser";
    private static transient final String _lgrNameOutboundConnection =
            "javax.resourceadapter.mqjmsra.outbound.connection";
    private static transient final String _lgrNameJMSQueueBrowser =
            "javax.jms.QueueBrowser.mqjmsra";
    private static transient final Logger _loggerOC =
            Logger.getLogger(_lgrNameOutboundConnection);
    private static transient final Logger _loggerJQB =
            Logger.getLogger(_lgrNameJMSQueueBrowser);
    private static transient final String _lgrMIDPrefix = "MQJMSRA_DQB";
    private static transient final String _lgrMID_EET = _lgrMIDPrefix + "1001: ";
    private static transient final String _lgrMID_INF = _lgrMIDPrefix + "1101: ";
    private static transient final String _lgrMID_WRN = _lgrMIDPrefix + "2001: ";
    private static transient final String _lgrMID_ERR = _lgrMIDPrefix + "3001: ";
    private static transient final String _lgrMID_EXC = _lgrMIDPrefix + "4001: ";

    /** Creates a new instance of DirectQueueBrowser */
    public DirectQueueBrowser()
    {
    }

    /** Creates a new instance of DirectProducer with a specified destination */
    public DirectQueueBrowser(DirectSession ds, JMSService jmsservice,
            long consumerId, Queue destination,
            com.sun.messaging.jmq.jmsservice.Destination jmsservice_dest,
            String selector) {
        Object params[] = new Object[6];
        params[0] = ds;
        params[1] = jmsservice;
        params[2] = consumerId;
        params[3] = destination;
        params[4] = jmsservice_dest;
        params[5] = selector;
        _loggerOC.entering(_className, "constructor()", params);        
        this.ds = ds;
        this.jmsservice = jmsservice;
        this.consumerId = consumerId;
        this.destination = destination;
        this.jmsservice_destination = jmsservice_dest;
        
        //Set the message selector to null if the empty string was used.
        this.msgSelector = "".equals(selector) ? null : selector;

        this.connectionId = ds.getConnectionId();
        this.sessionId = ds.getSessionId();
        java.util.logging.Level _level = _loggerJQB.getLevel();
        if (_level != null) {
            this._logLevel = _level.intValue();
            if (this._logLevel <= java.util.logging.Level.FINE.intValue()){
                this._logFINE = true;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //  methods that implement javax.jms.QueueBrowser
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Close the QueueBrowser.<p>
     *
     *  Since a provider may allocate some resources on behalf of a 
     *  QueueBrowser outside the Java virtual machine, clients should close them
     *  when they are not needed.
     *  Relying on garbage collection to eventually reclaim these resources may
     *  not be timely enough.
     *
     *  @throws JMSException if the JMS provider fails to close this
     *          browser due to some internal error.
     */
    public synchronized void close()
    throws JMSException {
        _loggerJQB.fine(_lgrMID_INF+"consumerId="+consumerId+":"+"close()");
        //harmless if already closed
        if (isClosed){
            return;
        } else {
            ds.removeBrowser(this);
            this._close();
        }
    }

    /**
     *  Get an Enumeration for browsing the current queue messages for the
     *  QueueBrowser in the order they would be received.
     *
     *  @return The Enumeration for browsing the messages.
     *
     *  @throws JMSException if the JMS provider fails to get the
     *          enumeration for this browser due to some internal error.
     *
     *  @see java.util.Enueration#hasMoreElements
     *  @see java.util.Enueration#nextElement
     */
    public Enumeration getEnumeration()
    throws JMSException {
        if (_logFINE){
            _loggerJQB.fine(_lgrMID_INF + "consumerId=" + this.consumerId +
                    ":getEnumeration()");
        }
        this._checkIfClosed("getEnumeration()");
        //Initialize to handle errors
        this.browserMessages = null;
        this.size = 0;
        this.cursor = 0;
        try {
            this.browserMessages = this.jmsservice.browseMessages(
                    this.connectionId, this.sessionId, this.consumerId);
            if (this.browserMessages != null) {
                this.size = this.browserMessages.length;
            }
            return this;
        } catch (JMSServiceException jse){
            JMSServiceReply.Status status = jse.getJMSServiceReply().getStatus();
            String failure_cause;
            JMSException jmse = null;
            String exerrmsg = 
                    "browseMessages on JMSService:" +
                    jmsservice.getJMSServiceID() +
                    " failed for connectionId:"+ connectionId +
                    " and sessionId:" + sessionId +
                    " due to " + jse.getMessage();
            jmse = new JMSException(exerrmsg);
            jmse.initCause(jse);
            throw jmse;
        }
    }

    /**
     *  Get the message selector expression for the QueueBrowser
     *  
     *  @return The QueueBrowser object's message selector, or null if no
     *          message selector exists for the message consumer (that is, if 
     *          the message selector was not set or was set to null or the 
     *          empty string)
     *
     *  @throws JMSException if the JMS provider fails to get the
     *          message selector for this browser due to some internal error.
     */
    public String getMessageSelector()
    throws JMSException {
        this._checkIfClosed("getMessageSelector()");
        return this.msgSelector;
    }

    /**
     *  Get the queue associated with this QueueBrowser.
     * 
     *  @return The queue
     *  
     *  @throws JMSException if the JMS provider fails to get the
     *          queue associated with this browser due to some internal error.
     */
    public Queue getQueue()
    throws JMSException {
        this._checkIfClosed("getQueue()");
        return this.destination;
    }
    /////////////////////////////////////////////////////////////////////////
    //  end javax.jms.QueueBrowser
    /////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////
    //  methods that implement java.util.Enumeration
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Test if this Enumeration contains more elements
     */
    public boolean hasMoreElements() {
        return (this.cursor < this.size);
    }

    /**
     *  Return the next element of this Enumeration if this Enumeration has
     *  at least one more element to provide.
     *
     */
    public javax.jms.Message nextElement() {
        javax.jms.Message msg = null;
        if (this.browserMessages !=null){
            try {
                msg = DirectPacket.constructMessage(
                    (this.browserMessages[cursor++]).getPacket(),
                        this.consumerId, this.ds, this.jmsservice, true);
            } catch (JMSException jmse){
            }
        }
        return msg;
    }
    /////////////////////////////////////////////////////////////////////////
    //  end java.util.Enumeration
    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    //  MQ methods
    /////////////////////////////////////////////////////////////////////////
    /**
     *  Return the consumerId for this DirectQueueBrowser
     *
     *  @return The consumerId
     */
    public long getConsumerId() {
        return this.consumerId;
    }

    /**
     *  Close browser for use when used by session.clos()
     */
    protected synchronized void _close()
    throws JMSException {
        //harmless if already closed
        if (this.isClosed){
            return;
        } else {
            this.browserMessages = null;
            this.size = 0;
            this.cursor = 0;
        }
        try {
            //System.out.println("DQB:Destroying browserId="+consumerId+":connectionId="+connectionId);
            jmsservice.deleteBrowser(connectionId, sessionId, consumerId);
        } catch (JMSServiceException jmsse){
            _loggerJQB.warning(_lgrMID_WRN+
                    "consumerId="+consumerId+":"+"close():"+
                    "JMSService.deleteBrowser():"+
                    "JMSServiceException="+
                    jmsse.getMessage());
        }
        this.isClosed = true;
    }

    /**
     *  Check if the DirectQueueBrowser is closed prior to performing an
     *  operation and throw a JMSException if it is closed.
     *
     *  @param methodname The name of the method from which this check is called
     *
     *  @throws JMSException if it is closed
     */
    private void _checkIfClosed(String methodname)
    throws JMSException {
        if (this.isClosed) {
            String closedmsg = _lgrMID_EXC + methodname +
                    "QueueBrowser is closed:Id=" + this.consumerId;
            _loggerJQB.warning(closedmsg);
            throw new javax.jms.IllegalStateException(closedmsg);
        }
    }
    /////////////////////////////////////////////////////////////////////////
    //  end MQ methods
    /////////////////////////////////////////////////////////////////////////
}
