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
 * @(#)ServiceEntry.java	1.4 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.StringTokenizer;

/**
 * Encapsulates information about a service. For use with the cluster
 * discovery protocol.
 */
public class ServiceEntry {
    private String address = null;
    private String protocol = null;;
    private String type = null;;
    private String name = null;

    public final static String SPACE = " ";

    public ServiceEntry() {
    }

    /**
     * Set the transport address for this service.
     *
     * Service address syntax examples :
     * <pre>
     *     jms@host:port
     *     ssljms@host:port
     *     httpjms@http://www.foo.com/jmqservlet?ServerName=jpgserv
     * </pre>
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the service address.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Set the protocol.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Get the protocol.
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Set the service type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the service type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the service name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the service name.
     */
    public String getName() {
        return this.name;
    }

    public String toString() {
        return name + SPACE + protocol + SPACE +
            type + SPACE + address;
    }
}

/*
 * EOF
 */
