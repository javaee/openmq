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
 * @(#)BrowserConsumer.java	1.18 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import java.util.*;
import java.io.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;

 /**
  * A BrowserConsumer consumes a browser's messages for enumeration
  *
  * Some of potential optimizations:
  * 1. recycle interestIds for BrowserConsumers in a QueueBrowser
  *    (require recycle interestIds support in general globaly)
  * 2. receiveQueue max size limit
  * 3. a common cache in a QueueBrowser for its BrowserConsumers
  */

class BrowserConsumer extends Consumer implements Enumeration, Traceable {

    protected SessionImpl session = null;
    protected QueueBrowserImpl browser = null;

    protected ReceiveQueue receiveQueue = null;

    private long browseTimeout = 60000;
    private int browseChunkLimit = 1000;

    private SysMessageID[] messageIDs = null;
    private int cursor = 0;
    private int cursorEnd = 0;
    private int waitCounter = 0;

    public BrowserConsumer(QueueBrowserImpl browser,
                           Destination dest) throws JMSException {

        this(browser, dest, null);
    }

    public BrowserConsumer(QueueBrowserImpl browser,
                           Destination dest,
                           String messageSelector) throws JMSException {

        super(browser.getSession().getConnection(), dest, messageSelector, false);
        this.browser = browser;
        this.session = browser.getSession();
        try {
            browseTimeout = Long.parseLong(session.getConnection().getProperty(
                                             ConnectionConfiguration.imqQueueBrowserRetrieveTimeout));
            browseChunkLimit = Integer.parseInt(session.getConnection().getProperty(
                                             ConnectionConfiguration.imqQueueBrowserMaxMessagesPerRetrieve));
        } catch (NumberFormatException nfe) {
            //Use hardcoded defaults
        }
        init();
    }

    private void init() throws JMSException {
        receiveQueue= new ReceiveQueue();
        //XXX PROTOCOL2.1
        //messageIDs = session.getMessageIdSet(getDestination(), getMessageSelector());
        messageIDs = session.getMessageIdSet( this );
        cursorEnd = messageIDs.length - 1;
        cursor = 0;

        addInterest();

        waitCounter = 0;

        moreMessage();
    }

    private void addInterest() {
        //XXX PROTOCOL2.1
        //session.addBrowserConsumer(this);
        connection.addLocalInterest(this);
    }

    private void removeInterest() {
        connection.removeLocalInterest(this);
        session.removeBrowserConsumer(this);

        FlowControl fc = connection.getReadChannel().flowControl;
        fc.removeConsumerFlowControl(this);
    }

    protected Long getReadQueueId() {
        return session.getSessionId();
    }

    /* This method is called by SessionReader.
     * Messages are delivered to the receiveQueue
     */
    protected void
    onMessage(MessageImpl message) throws JMSException {
        if (receiveQueue.getIsClosed()) {
            return;
        }
        receiveQueue.enqueueNotify(message);
    }

    private Message receive(long timeout) throws JMSException {
        MessageImpl message = null;

        try {
            message = (MessageImpl) receiveQueue.dequeueWait(timeout);

            if ( message != null ) {
                message.setIsBrowserMsg(true);
            }

        } finally {
            receiveQueue.setReceiveInProcess(false);
        }

        return message;
    }

    private void moreMessage() throws JMSException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(
                                    ProtocolHandler.ACK_MESSAGE_BODY_SIZE);
        DataOutputStream dos = new DataOutputStream(bos);

        boolean moreComming = false;
        SysMessageID messageID = null;
        while(!moreComming && cursor <= cursorEnd) {
            int count = 0;
            while (cursor <= cursorEnd && count < browseChunkLimit) {
                messageID = (SysMessageID)messageIDs[cursor];
                try {
                    messageID.writeID(dos);
                } catch (IOException e) {
                    ExceptionHandler.handleException(e, AdministeredObject.cr.X_CAUGHT_EXCEPTION);
                }
                cursor++;
                count++;
            }
            if (count > 0) {
                try {
                dos.flush();
                bos.flush();
                }
                catch  (IOException e) {
                ExceptionHandler.handleException(e, AdministeredObject.cr.X_CAUGHT_EXCEPTION);
                }
                moreComming = session.requestMessages(bos, this);
                bos.reset();
            }
        }
        if (moreComming) {
            waitCounter++;
        }
    }

    public boolean hasMoreElements() {
        if (receiveQueue.getIsClosed()) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_BROWSER_CLOSED);
            throw new NoSuchElementException(errorString);
        }
        synchronized(this) {
            return (!(waitCounter == 0));
        }
    }

    public Object nextElement() {

    Message message = null;

    synchronized (this) {
        if (!hasMoreElements()) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_BROWSER_END);
            throw new NoSuchElementException(errorString);
        }
        try { // waitCounter must be > 0
             message = receive(browseTimeout);
             if (message != null && isLast((MessageImpl)message)) {
                 waitCounter--;
                 moreMessage();
             }
        }
        catch (JMSException e) {
            close();
            throw new NoSuchElementException(e.getMessage());
        }
    }

    //message is null when either timeout or closed
    if (message == null) {
        if (receiveQueue.getIsClosed()) {
            String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_BROWSER_CLOSED);
            throw new NoSuchElementException(errorString);
        }
        close();
        String errorString = AdministeredObject.cr.getKString(AdministeredObject.cr.X_BROWSER_TIMEOUT);
        throw new NoSuchElementException(errorString);
    }

    return message;
    }

    private boolean isLast(MessageImpl message) {
        return message.getPacket().getIsLast();
    }

    protected QueueBrowserImpl getBrowser() {
        return browser;
    }

    protected SessionImpl getSession() {
        return session;
    }

    protected void close() {
        if (receiveQueue.getIsClosed()) {
            return;
        }
        receiveQueue.close();

        removeInterest();

        if ( debug ) {
            Debug.println( "browser consumer closed ...");
            Debug.println( this );
        }
    }

    public void dump (PrintStream ps) {

        ps.println ("------ BrowserConsumer dump ------");

        ps.println ("Interest ID: " + getInterestId());
        ps.println ("destination: " + getDestination());
        ps.println ("selector: " + messageSelector);
        ps.println ("waitCounter: " + waitCounter);

        if ( receiveQueue != null ) {
            receiveQueue.dump(ps);
        } else {
            ps.println ("receiveQueue is null.");
        }
    }
}
