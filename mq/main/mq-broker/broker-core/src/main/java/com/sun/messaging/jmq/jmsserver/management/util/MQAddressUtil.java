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
 * @(#)MQAddressUtil.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.util;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.io.MQAddress;

public class MQAddressUtil {
    private static boolean DEBUG = false;

    /**
     * Return portmapper MQAddress. This MQAddress does not include the service name
     * and is of the form mq://host:port
     *
     * @param port	Portmapper port
     * @return		Portmapper address. This MQAddress does not include the 
     *			service name. It is of the form mq://host:port.
     */
    public static MQAddress getPortMapperMQAddress(Integer port)  {
	MQAddress	addr = null;

	if (port == null)  {
	    if (DEBUG)  {
	        Logger logger = Globals.getLogger();
                logger.log(Logger.DEBUG, "Null port passed in to getPortMapperMQAddress()");
	    }
	    return (null);
	}

	try  {
	    String url = Globals.getMQAddress().getHostName() + ":"  + port.toString();
	    addr = PortMapperMQAddress.createAddress(url);
	} catch (Exception e)  {
	    if (DEBUG)  {
	        Logger logger = Globals.getLogger();
                logger.log(Logger.DEBUG, "Failed to create portmapper address", e);
	    }
	}

	return (addr);
    }

    /**
     * Return connection service MQAddress. Connection service addresses
     * can have 2 forms and the bypassPortmapper parameter allows the caller
     * to select which one is desired. The 2 forms depend on whether the client
     * will contact the portmapper (mq://host:port) or the connection service 
     * directly ({mqtcp,mqssl}://host:port/svcname).
     *
     * @param svcName	Connection service name.
     * @param port	Portmapper port or connection service port.
     * @param bypassPortmapper	Boolean to indicate which type of address is desired.
     *				If the value for bypassPortmapper is false, the address
     *				will be of the form mq://host:port/svcName. If the value
     *				is true, the scheme will be one of mqtcp or mqssl. The 
     *				address will be of the form scheme://host:svc_port/svc_name.
     *				e.g. mqtcp://myhost:87635/jms
     * @return		Connection service address.
     */
    public static MQAddress getServiceMQAddress(String svcName, Integer port, 
				boolean bypassPortmapper)  {
	MQAddress addr = null;
	String scheme = "mq";
	Logger logger = Globals.getLogger();

	if ((svcName == null) || (svcName.equals("")) || (port == null))  {
	    if (DEBUG)  {
                logger.log(Logger.DEBUG, "Null service name and/or port passed in to getServiceMQAddress()");
	    }
	    return (null);
	}

	if (bypassPortmapper)  {
	    scheme = getScheme(svcName);
	}

	if (scheme == null)  {
	    return (null);
	}

	if (bypassPortmapper)  {
	    try  {
	        String url = scheme 
			+ "://" 
			+ Globals.getMQAddress().getHostName() 
			+ ":"  
			+ port.toString() 
			+ "/" + svcName;
	        addr = MQAddress.getMQAddress(url);
	    } catch (Exception e)  {
		if (DEBUG)  {
                    logger.log(Logger.DEBUG, "Failed to create service address", e);
		}
	    }
	} else  {
	    try  {
	        String url = Globals.getMQAddress().getHostName()
				+ ":"  
				+ port.toString()
				+ "/"
				+ svcName;
	        addr = PortMapperMQAddress.createAddress(url);
	    } catch (Exception e)  {
		if (DEBUG)  {
                    logger.log(Logger.DEBUG, "Failed to create service address", e);
		}
	    }
	}

	return (addr);
    }

    private static String getScheme(String svcName)  {
        String proto = Globals.getConfig().getProperty(Globals.IMQ + "." + svcName + ".protocoltype");
        String scheme = null;

	if (proto.equals("tcp"))  {
	    scheme = "mqtcp";
	} else if (proto.equals("tls"))  {
	    scheme = "mqssl";
	}

	return (scheme);
    }

}
