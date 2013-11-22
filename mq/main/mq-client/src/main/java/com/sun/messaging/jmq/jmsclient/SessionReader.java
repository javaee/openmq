/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)SessionReader.java	1.34 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

import java.io.*;
import java.util.Enumeration;

import com.sun.messaging.AdministeredObject;

//XXX REVISIT
public class SessionReader extends ConsumerReader {

    private boolean debug = Debug.debug;

    protected SessionImpl session = null;
    
    //bug 6360068 - Class defines field that obscures a superclass field.
    //protected long timeout = 0;

    //the message that is delivering/delivered to the message consumer
    protected volatile MessageImpl currentMessage = null;

    public SessionReader (SessionImpl session) {
        super(session.getConnection(), session.getSessionQueue());
        this.session = session;

        //set timeout value
        if ( (session.acknowledgeMode==Session.DUPS_OK_ACKNOWLEDGE) &&
             (session.dupsOkAckOnTimeout == true) ) {

            if ( debug ) {
                Debug.println("**** setting dupsOkAckTimeout: " + session.dupsOkAckTimeout);
            }

            //set dups ok ack timeout.
            setTimeout(session.dupsOkAckTimeout);
        }

        init();
    }
    
    protected void setCurrentMessage (MessageImpl cm) {
    	this.currentMessage = cm;
    }

    /**
     * The session thread is waken up with a packet from sessionQueue
     *
     * @param packet the message packet to be delivered to consumer
     *
     * @exception IOException
     * @exception JMSException
     */
    protected void deliver(ReadOnlyPacket packet)
                      throws IOException, JMSException {
    	
        //XXX PROTOCOL2.1
        long interestId = 0;
        Consumer consumer = null;

        currentMessage = getJMSMessage ( packet );
        //get intID
        interestId = currentMessage.getInterestID();

        //delegate to message consumer
        consumer = session.getMessageConsumer(Long.valueOf(interestId));

        if (consumer == null) {
            consumer = session.getBrowserConsumer(Long.valueOf(interestId));
        }

        if (consumer != null) {
            consumer.onMessage(currentMessage);
        } else {
            if ( debug ) {
                String errorString = AdministeredObject.cr.getKString(
                                     ClientResources.X_CONSUMER_NOTFOUND);

                Debug.getPrintStream().println(errorString);
                packet.dump(Debug.getPrintStream());
            }
        }
    }

    /**
     * The session thread is waken up without a packet from sessionQueue
     *
     * @exception IOException
     * @exception JMSException
     */
    protected void deliver() throws IOException, JMSException {

        if ( sessionQueue.getIsClosed() == false ) {

            if ( session.dupsOkAckOnTimeout ) {
                //do dups ok ack.
                if ( debug ) {
                    Debug.println("*** Calling dups ok commit from timeout thread");
                }

                session.syncedDupsOkCommitAcknowledge();
            }

            if ( sessionQueue.isListenerSetLate() ) {
                //someone set message listener
                //after messages were delivered
                //to the receiveQueue of the
                //consumer.
                onMessageToLateListeners();

                //reset flag
                sessionQueue.setListenerLate(false);
            }
        }

    }

    /**
     * Check each message consumer and deliver messages
     * from receive queue to the message listener.
     * Loop through consumers table and call
     * consumer.onMessageToListenerFromReceiveQueue() if
     * the consumer has a message listener set.
     */
    protected void onMessageToLateListeners() throws JMSException {
        MessageConsumerImpl consumer = null;
        Enumeration enum2 = session.consumers.elements();
        while ( enum2.hasMoreElements() ) {
            consumer = (MessageConsumerImpl) enum2.nextElement();
            if ( consumer.getSyncReadFlag() == false ) {
                consumer.onMessageToListenerFromReceiveQueue();
            }
        }
    }

    /**
     * Convert JMQ packet to JMQ message type.
     *
     * @param pkt the packet to be converted.
     */
    protected MessageImpl
    getJMSMessage (ReadOnlyPacket pkt) throws JMSException {
        MessageImpl msg = protocolHandler.getJMSMessage( pkt );
        msg.setSession ( session );

        return msg;
    }

    public void dump (PrintStream ps) {
        ps.println ("------ SessionReader dump ------");
        ps.println ("Session ID: " + session.getSessionId() );
        super.dump(ps);
    }
}
