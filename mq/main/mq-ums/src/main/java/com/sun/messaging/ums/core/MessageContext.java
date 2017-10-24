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

import javax.xml.soap.SOAPMessage;

/**
 * Message context is an object that associates with a specific message in
 * the life time of a SOAP Service message processing cycle.
 * <p>
 * A SOAP service message processing cycle is defined as follows:
 * A message that *flows* through A SOAP Service's request handler chain,
 * get processed (by a service provider), and *flows* through the service's
 * response handler chain.
 * <p>
 * A message context instance is obtained from the ServiceContext object in a
 * SOAPService.
 * <p>
 * A message context instance is removed after a SOAPService finished
 * processing the last Response MessageHandler.
 * <p>
 * Message context may be used by Message Handlers to communicate with each
 * other for the life time of a message processing cycle in a SOAP service.
 * <p>
 * Information that needs to be kept longer than a message processing cycle
 * should be set as attributes in the ServiceContext.
 *
 * @see ServiceContext
 * @see MessageHandler
 */
public interface MessageContext {

    /**
     * Binds an object to a given attribute name in this message context.
     * If the name specified is already used for an attribute, this method
     * will replace the attribute with the new to the new attribute.
     *
     * <p>If a null value is passed, the effect is the same as calling
     * removeAttribute().
     *
     * <p>Attribute names should follow the same convention as package names.
     *
     * @param key   an Objetc specifying the key of the attribute
     * @param value  an Object representing the attribute to be bound
     */
    public void setAttribute (Object key, Object value);

    /**
     * Returns the attribute with the given name, or null if
     * there is no attribute by that name.  SOAPService and its
     * MessageHandlers may use this API to share information.
     *
     * <p>The attribute is returned as a java.lang.Object or some subclass.
     *
     * @param name  a String specifying the name of the attribute.
     *
     * @return  an Object containing the value of the attribute, or null
     * if no attribute exists matching the given name
     *
     * @see getAttributeKeys
     */
    public Object getAttribute (Object key);

    /**
     * Removes the attribute with the given name from the message context.
     * After removal, subsequent calls to getAttribute(java.lang.String)
     * to retrieve the attribute's value will return null.
     *
     * @param name a String specifying the name of the attribute to be removed
     */
    public Object removeAttribute(Object key);

    /**
     * Returns an Iterator containing the attribute names available
     * within this message context. Use the getAttribute(java.lang.String)
     * method with an attribute name to get the value of an attribute.
     *
     * @return an Iterator of attribute keys
     */
    public java.util.Iterator getAttributeKeys();

    /**
     * Get request message for this message context.
     *
     * @return request message associated with this message context.
     */
    public SOAPMessage getRequestMessage();

    /**
     * Set request message for this message context.
     *
     * @param message the request message associated with this context.
     */
    public void setRequestMessage (SOAPMessage message);

    /**
     * Get response message for this message context.
     *
     * @return response message associated with this message context.
     */
    public SOAPMessage getResponseMessage();

    /**
     * Set response message for this message context.
     *
     * @param message the response message associated with this context.
     */
    public void setResponseMessage(SOAPMessage message);

}
