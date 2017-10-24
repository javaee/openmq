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

package javax.xml.messaging;

/**
 * An opaque representation of an application endpoint. Typically, an
 * <code>Endpoint</code> object represents a business entity, but it
 * may represent a party of any sort. Conceptually, an 
 * <code>Endpoint</code> object is the mapping of a logical name
 * (example, a URI) to a physical location, such as a URL.
 * <P>
 * For messaging using a provider that supports profiles, an application
 * does not need to specify an endpoint when it sends a message because 
 * destination information will be contained in the profile-specific header.
 * However, for point-to-point plain SOAP messaging, an application must supply
 * an <code>Endpoint</code> object to
 * the <code>SOAPConnection</code> method <code>call</code>
 * to indicate the intended destination for the message. 
 * The subclass {@link URLEndpoint} can be used when an application
 * wants to send a message directly to a remote party without using a
 * messaging provider.
 * <P>
 * The default identification for an <code>Endpoint</code> object
 * is a URI. This defines what JAXM messaging
 * providers need to support at minimum for identification of
 * destinations. A messaging provider
 * needs to be configured using a deployment-specific mechanism with
 * mappings from an endpoint to the physical details of that endpoint. 
 * <P>
 * <code>Endpoint</code> objects can be created using the constructor, or
 * they can be looked up in a naming
 * service. The latter is more flexible because logical identifiers
 * or even other naming schemes (such as DUNS numbers)
 * can be bound and rebound to specific URIs. 
 */
public class Endpoint {
   /**
    * A string that identifies the party that this <code>Endpoint</code>
    * object represents; a URI is the default.
    */
    protected String id;
    
    /**
     * Constructs an <code>Endpoint</code> object using the given
     * string identifier.
     * 
     * @param uri a string that identifies the party that this
     *        <code>Endpoint</code> object represents; the default
     *        is a URI
     */
    public Endpoint(String uri) {
	this.id = uri;
    }
    
    /**
     * Retrieves a string representation of this <code>Endpoint</code>
     * object.  This string is likely to be provider-specific, and
     * programmers are discouraged from parsing and programmatically
     * interpreting the contents of this string.
     *
     * @return a <code>String</code> with a provider-specific representation
     *         of this <code>Endpoint</code> object
     */
    public String toString() {
        return id;
    }
}
