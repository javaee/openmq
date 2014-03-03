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

package javax.xml.messaging;

/** 
 * Information about the messaging provider to which a client has
 * a connection.
 * <P>
 * After obtaining a connection to its messaging provider, a client
 * can get information about that provider.  The following code fragment
 * demonstrates how the <code>ProviderConnection</code> object <code>con</code>
 * can be used to retrieve its <code>ProviderMetaData</code> object 
 * and then to get the name and version number of the messaging provider.
 * <PRE>
 *   ProviderMetaData pmd = con.getMetaData();
 *   String name = pmd.getName();
 *   int majorVersion = pmd.getMajorVersion();
 *   int minorVersion = pmd.getMinorVersion();
 * </PRE>
 *
 * The <code>ProviderMetaData</code> interface also makes it possible
 * to find out which profiles a JAXM provider supports.
 * The following line of code uses the method 
 * <code>getSupportedProfiles</code> to 
 * retrieve an array of <code>String</code> objects naming the profile(s)
 * that the JAXM provider supports.
 * <PRE>
 *   String [] profiles = pmd.getSupportedProfiles();
 * </PRE>
 * 
 * When a JAXM implementation supports a profile, it supports the functionality 
 * supplied by a particular messaging specification. A profile is built on top 
 * of the SOAP 1.1 and SOAP with Attachments specifications and adds more
 * capabilities.  For example, a JAXM provider may support
 * an ebXML profile, which means that it supports headers that specify
 * functionality defined in the ebXML specification "Message Service Specification:
 * ebXML Routing, Transport, & Packaging, Version 1.0".
 * <P>
 * Support for  profiles, which typically add enhanced security
 * and quality of service features, is required for the implementation of
 * end-to-end asynchronous messaging.
 */
public interface ProviderMetaData {

    /**
     * Retrieves a <code>String</code> containing the name of the
     * messaging provider to which the <code>ProviderConnection</code> object 
     * described by this <code>ProviderMetaData</code> object is
     * connected. This string is provider implementation-dependent. It
     * can either describe a particular instance of the provider or
     * just give the name of the provider.
     *
     * @return the messaging provider's name as a <code>String</code>
     */
    public String getName();

    /**
     * Retrieves an <code>int</code> indicating the major version number 
     * of the messaging provider to which the <code>ProviderConnection</code> object 
     * described by this <code>ProviderMetaData</code> object is
     * connected.
     *
     * @return the messaging provider's major version number as an 
     *         <code>int</code>
     */
    public int getMajorVersion();
 
    /**
     * Retrieves an <code>int</code> indicating the minor version number 
     * of the messaging provider to which the <code>ProviderConnection</code> object 
     * described by this <code>ProviderMetaData</code> object is
     * connected.
     *
     * @return the messaging provider's minor version number as an 
     *         <code>int</code>
     */
    public int getMinorVersion();

    /**
     * Retrieves a list of the messaging profiles that are supported
     * by the messaging provider to which the <code>ProviderConnection</code> object
     * described by this <code>ProviderMetaData</code> object is
     * connected.
     *
     * @return a <code>String</code> array in which each element is a
     *         messaging profile supported by the messaging provider
     */
    public String[] getSupportedProfiles();
}
