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

import java.util.Hashtable;
import java.util.Properties;


/**
 * MQServiceContext is part of the MQ SOAP Service framework
 * implementation.
 *
 * Service context defines a set of methods that a SOAPService uses to
 * communicate with its MessageHandlers.
 *
 * <p>There is a context for each SOAPService instance. SOAPService
 * provider constructs this object and makes it available through
 * SOAPService interface.
 *
 * <p>This object is passed to the MessageHandler.init (ServiceContext ct)
 * method after MessageHandler is loaded by the Service provider.
 *
 * <p>The concept of this class is adopted from Java Servlet.
 *
 * @see SOAPService
 * @see MessageHandler
 */
public class UMSServiceContext implements ServiceContext {

    /**
     * hashtable to hold attributes for this context.
     */
    protected Hashtable attributes = null;

    /**
     * init properties.
     */
    protected Properties props = null;

    /**
     * default constructor.
     */
    public UMSServiceContext (Properties props) {
        this.props = props;

        attributes = new Hashtable();
    }

    /**
     * Binds an object to a given attribute name in this Service context.
     * If the name specified is already used for an attribute, this method
     * will replace the attribute with the new to the new attribute.
     *
     * <p>If a null value is passed, the effect is the same as calling
     * removeAttribute().
     *
     * <p>Attribute names should follow the same convention as package names.
     *
     * @param name   a String specifying the name of the attribute
     * @param value  an Object representing the attribute to be bound
     */
    public void setAttribute (Object key, Object value) {
        this.attributes.put(key, value);
    }

    /**
     * Returns the SOAP Service attribute with the given name, or null if
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
    public Object getAttribute (Object name) {
        return this.attributes.get( name );
    }

    /**
     * Removes the attribute with the given name from the service context.
     * After removal, subsequent calls to getAttribute(java.lang.String)
     * to retrieve the attribute's value will return null.
     *
     * @param key an Object specifying the key of the attribute to be removed
     */
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    /**
     * Returns an Iterator containing the attribute names available
     * within this service context. Use the getAttribute(java.lang.Object)
     * method with an attribute key to get the value of an attribute.
     *
     * @return an Iterator of attribute keys
     */
    public java.util.Iterator getAttributeKeys() {
        return this.attributes.keySet().iterator();
    }

    /**
     * Get the initialized properties from the Service context.  This is the
     * same object as the properties passed to SOAPService.init().
     *
     * @return the init properties of the SOAPService instance.
     */
    public java.util.Properties getInitProperties() {
        return this.props;
    }

}
