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
 * @(#)AdminConnectionConfiguration.java	1.4 06/28/07
 */ 

package com.sun.messaging;

/**
 * The <code>AdminConnectionConfiguration</code> class contains property names 
 * and special values for configuring the <CODE>AdminConnectionFactory</CODE> object.
 * <P>
 * Other property names and information related to security is TBD.
 *
 * @see         com.sun.messaging.AdminConnectionFactory com.sun.messaging.AdminConnectionFactory
 */
public class AdminConnectionConfiguration {

    /* No public constructor needed */
    private AdminConnectionConfiguration(){}

    /**
     * This property holds the address that will be used by management clients to
     * connect to the MQ Message Service.
     * <p>
     * <b>Message Server Address Syntax</b>
     * <p>The syntax for specifying a message server address is as follows:<BR>
     *  </p>  
     * <p><code><i>scheme</i>://<i>address_syntax</i></code><br>
     *   </p> 
     *  
     * <P>
     * This syntax is similar to the one used by JMS clients to configure JMS
     * ConnectionFactory objects. However, the address syntax includes an MQ broker
     * JMX connector name (instead of a connection service name).
     *
     * <p><code><i>scheme</i></code> and <code><i>address_syntax</i></code> are described in the folowing 
     *   table.</p>
     * <TABLE columns="4" border="1">
     * 	<TR>
     * 		<TH>Scheme</TH>
     * 		<TH>JMX Connector Name</TH>
     * 		<TH>Description</TH>
     * 		<TH>Syntax</TH>
     * 	</TR>
     * 
     * 	<TR>
     * 		<TD valign="top"><code>mq</code></TD>
     * 		
     *       <TD valign="top">
     *       <code>jmxrmi<br>
     *         and <br>
     *         jmxsslrmi</code>
     *       </TD>
     * 		
     * 	<TD valign="top">The MQ Port Mapper at the specified host and port 
     *      will handle the connection request, and determine the JMXServiceURL
     *      for the connector that is specified. Once this is known, MQ makes the 
     *	    connection.</TD>
     * 		
     *     <TD valign="top"><code>[<i>hostName</i>][:<i>port</i>]/<i>connectorName</i></code> <br>
     *       Defaults: <br>
     *       <code><i>hostName</i> = localhost <br>
     *       <i>port</i> = 7676</code><br>
     *       A connector name must be specified.
     *     </TD>
     * 	</TR>
     * 
     * </TABLE>
     * <p>&nbsp;</p>
     * <p>The following table shows how the message server address syntax applies in 
     *   some typical cases.</p>
     * <TABLE columns="4" border="1">
     * 	<TR>
     * 		<TH>Connector Name</TH>
     * 		<TH>Broker Host</TH>
     * 		<TH>Port</TH>
     * 		<TH>Example Address</TH>
     * 	</TR>
     * 
     * 	<TR>
     * 		
     *     <TD valign="top">jmxrmi</TD>
     * 		
     *     <TD valign="top">Unspecified</TD>
     * 	    
     *     <TD valign="top">Unspecified</TD>
     * 		
     *     <TD valign="top"><code>mq:///jmxrmi<br>
     *       (mq://localhost:7676/jmxrmi)</code></TD>	
     * 	</TR>
     * 	
     * 	<TR>
     * 		
     *     <TD valign="top">jmxrmi</TD>
     * 		
     *     <TD valign="top">Specified Host</TD>
     * 	    
     *     <TD valign="top">Unspecified</TD>
     * 		
     *     <TD valign="top"><code>mq://myBkrHost/jmxrmi<br>
     *       (mq://myBkrHost:7676/jmxrmi)</code></TD>	
     * 	</TR>
     * 	
     * 	<TR>
     * 		
     *     <TD valign="top">jmxrmi</TD>
     * 		
     *     <TD valign="top">Unspecified</TD>
     * 	    
     *     <TD valign="top">Specified Portmapper Port</TD>
     * 		
     *     <TD valign="top"><code>mq://:1012/jmxrmi<br>
     *       (mq://localHost:1012/jmxrmi)</code></TD>	
     * 	</TR>
     * 	
     * 	<TR>
     * 		
     *     <TD valign="top"><code>jmxsslrmi</code></TD>
     * 		
     *     <TD valign="top">Local Host</TD>
     * 	    
     *     <TD valign="top">Default Portmapper Port</TD>
     * 		
     *     <TD valign="top"><code>mq://localHost:7676/jmxsslrmi</code></TD>	
     * 	</TR>
     * 	
     * 	<TR>
     * 	 <TD valign="top"><code>jmxsslrmi</code></TD>
     * 		
     *     <TD valign="top">Specified Host</TD>
     * 	    
     *     <TD valign="top">Default Portmapper Port</TD>
     * 		
     *     <TD valign="top"><code>mq://myBkrHost:7676/jmxsslrmi</code></TD>	
     * 	</TR>
     * 	<TR>
     * 	 <TD valign="top"><code>jmxsslrmi</code></TD>
     * 		
     *     <TD valign="top">Specified Host</TD>
     * 	    
     *     <TD valign="top">Specified Portmapper Port</TD>
     * 		
     *     <TD valign="top"><code>mq://myBkrHost:1012/jmxsslrmi</code></TD>	
     * 	</TR>
     * 	
     * </TABLE>
     * <P>&nbsp;</P>
     * <p>
     * The default value of this property is <code><b>mq://localhost:7676/jmxrmi</b></code>
     * <p>
     */
    public static final String imqAddress = "imqAddress";

    /**
     * This property holds the default administrator username that will be used
     * to authenticate with the MQ Administration Service.
     * <p>
     * The default value of this property is <code><b>admin</b></code>
     */
    public static final String imqDefaultAdminUsername = "imqDefaultAdminUsername";

    /**
     * This property holds the default administrator password that will be used
     * to authenticate with the MQ Administration Service.
     * <p>
     * The default value of this property is <code><b>admin</b></code>
     */
    public static final String imqDefaultAdminPassword = "imqDefaultAdminPassword";

}
