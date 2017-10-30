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

package com.sun.messaging.ums.core;


/**
 * The MessageHandler is part of the MQ SOAP Messaging Service framework.
 *
 * <p>A MessageHandler is a component to process SOAP messages.  For example,
 * a handler could be designed to process a specific set of SOAP headers.
 *
 * <p>More than one MessageHandler can be formed in a MessageHandler chain.
 *
 * <p>The difference between this interface to JAXM OnewayListener is that
 * the method processMessage() throws SOAPException.  This is required
 * because the implementation is part of the MQ SOAP message processing
 * model, and SOAPException is likely to be generated during the header
 * processing phase.
 *
 * <p>After MQ framework finished processing one MessageHandler, the message is
 * forwarded to the next MessageHandler.
 *
 * @author  chiaming yang
 * @see     MessageHandlerChain
 * @see     SOAPService
 * @see     ServiceContext
 * @see     MessageHandlerException
 */
public interface MessageHandler {

    /**
     * initialize the message handler with the current ServiceContext.
     * This method is called by SOAPService provider after the
     * MessageHandler is loaded to JVM.
     *
     * @throw MessageHandlerException if unable to initialize this
     *        handler.
     */
    public void
    init (ServiceContext context) throws MessageHandlerException;

    /**
     * Process the message context passed in the parameter.
     * @param context the message context to be processed.
     *
     * @throw SOAPException if any internal error when processing the message.
     */
    public void
    processMessage (MessageContext context) throws MessageHandlerException;

    /**
     * Close the message handler.  This method is called when SOAPService
     * is closed. MessageHandler SHOULD free all the resources it allocates.
     */
    public void close();

}

