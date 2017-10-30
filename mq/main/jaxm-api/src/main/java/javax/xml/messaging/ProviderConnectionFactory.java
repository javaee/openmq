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

import javax.xml.soap.*;

/**
 * A factory for creating connections to a particular messaging provider.
 * A <code>ProviderConnectionFactory</code> object can be obtained in two
 * different ways.
 * <ul>
 * <li>Call the <code>ProviderConnectionFactory.newInstance</code>
 * method to get an instance of the default <code>ProviderConnectionFactory</code>
 * object.<br>
 *  This instance can be used to create a <code>ProviderConnection</code>
 * object that connects to the default provider implementation.
 * <PRE>
 *      ProviderConnectionFactory pcf = ProviderConnectionFactory.newInstance();
 *      ProviderConnection con = pcf.createConnection();
 * </PRE>
 * <P>
 * <li>Retrieve a <code>ProviderConnectionFactory</code> object
 * that has been registered with a naming service based on Java Naming and 
 * Directory Interface<sup><font size=-2>TM</font></sup> (JNDI) technology.<br>
 * In this case, the <code>ProviderConnectionFactory</code> object is an 
 * administered object that was created by a container (a servlet or Enterprise
 * JavaBeans<sup><font size=-2>TM</font></sup> container). The
 * <code>ProviderConnectionFactory</code> object was configured in an implementation-
 * specific way, and the connections it creates will be to the specified
 * messaging provider. <br>
 * <P>
 * Registering a <code>ProviderConnectionFactory</code> object with a JNDI naming service
 * associates it with a logical name. When an application wants to establish a
 * connection with the provider associated with that
 * <code>ProviderConnectionFactory</code> object, it does a lookup, providing the
 * logical name.  The application can then use the 
 * <code>ProviderConnectionFactory</code>
 * object that is returned to create a connection to the messaging provider.
 * The first two lines of the  following code fragment use JNDI methods to 
 * retrieve a <code>ProviderConnectionFactory</code> object. The third line uses the
 * returned object to create a connection to the JAXM provider that was 
 * registered with "ProviderXYZ" as its logical name.
 * <PRE>
 *      Context ctx = new InitialContext();
 *      ProviderConnectionFactory pcf = (ProviderConnectionFactory)ctx.lookup(
 *                                                                 "ProviderXYZ");
 *      ProviderConnection con = pcf.createConnection();
 * </PRE>
 * </ul>
 */
public abstract class ProviderConnectionFactory {
    /**
     * Creates a <code>ProviderConnection</code> object to the messaging provider that
     * is associated with this <code>ProviderConnectionFactory</code>
     * object. 
     *
     * @return a <code>ProviderConnection</code> object that represents 
     *         a connection to the provider associated with this 
     *         <code>ProviderConnectionFactory</code> object
     * @exception JAXMException if there is an error in creating the
     *            connection
     */
    public abstract ProviderConnection createConnection() 
        throws JAXMException;

    static private final String PCF_PROPERTY
        = "javax.xml.messaging.ProviderConnectionFactory";

    static private final String DEFAULT_PCF 
        = "com.sun.xml.messaging.jaxm.client.remote.ProviderConnectionFactoryImpl";

    /**
     * Creates a default <code>ProviderConnectionFactory</code> object. 
     *
     * @return a new instance of a <code>ProviderConnectionFactory</code>
     *
     * @exception JAXMException if there was an error creating the
     *            default <code>ProviderConnectionFactory</code>
     */
    public static ProviderConnectionFactory newInstance() 
        throws JAXMException
    {
        //try {
	    return (ProviderConnectionFactory)
                FactoryFinder.find(PCF_PROPERTY,
                                   DEFAULT_PCF);
        //} catch (Exception ex) {
            //throw new JAXMException("Unable to create "+
                                    //"ProviderConnectionFactory: "
                                    //+ex.getMessage());
        //}
    }
}
