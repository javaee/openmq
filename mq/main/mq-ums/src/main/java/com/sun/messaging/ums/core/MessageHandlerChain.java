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

package com.sun.messaging.ums.core;

import java.util.Vector;


/**
 * The MessageHandlerChain is part of the MQ SOAP Messaging Service framework.
 *
 * <p>A MessageHandlerChain is a list of MessageHandlers that process each
 * SOAPMessage in sequence.  A SOAP message *flow* through each MessageHandler
 * registered in the MessageHandlerChain.
 *
 * <p>MessageHandlerChian is used in the SOAPService.  A SOAPService defines
 * two message handler chains - ReqHandlerChain and RespHandlerChain.
 *
 * @author  chiaming yang
 * @see MessageHandler
 * @see SOAPService
 */
public class MessageHandlerChain {

    private Vector handlerChain = new Vector();

    /**
     * Add a MessageHandler to the message handler chain.
     */
    public void addMessageHandler (MessageHandler handler) {
            handlerChain.add(handler);
    }

    /**
     * Add a MessageHandler to the message handler chain at the specified
     * index.
     */
    public void addMessageHandlerAt (int index, MessageHandler handler) {
            handlerChain.add(index, handler);
    }

    /**
     * Get MessageHandler from the handler chain at the specified index.
     */
    public MessageHandler getMessageHandlerAt (int index) {
        return (MessageHandler) handlerChain.get( index );
    }

    /**
     * Get all message handlers from this handler chain.
     *
     * @return an array of MessageHandlers in the handler chain.
     */
    public Object[] getMessageHandlers () {
        return handlerChain.toArray();
    }

    /**
     * Remove the message handler from the hendler chain.
     */
    public boolean removeMessageHandler (MessageHandler handler) {
        return handlerChain.remove( handler );
    }

    /**
     * Deletes the message handler at the specified index.
     *
     * @throws ArrayIndexOutOfBoundsException - if the index was invalid
     */
    public void removeMessageHandlerAt (int index) {
        handlerChain.remove( index );
    }

    /**
     * Remove all message handlers in this chain.
     */
    public void removeAllMessageHandlers() {
        handlerChain.clear();
    }

    /**
     * Get the number of Message Handlers in this chain.
     *
     * @return count of message handlers in this chain.
     */
    public int size() {
        return handlerChain.size();
    }
}
