/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)MQAddress.java	1.7 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.*;
import java.net.*;
import java.io.Serializable;

/**
 * This class represents broker address URL.
 */
public class MQAddress implements Serializable 
{
    static final long serialVersionUID = -8430608988259524061L;

    public static final String isHostTrusted = "isHostTrusted";

    protected static final String DEFAULT_SCHEME_NAME = "mq";
    protected static final String DEFAULT_HOSTNAME = "localhost";
    protected static final int DEFAULT_PORTMAPPER_PORT = 7676;
    protected static final String DEFAULT_SERVICENAME = "jms";

    private String addr = null;

    protected String schemeName = null;
    protected String addrHost = null;
    protected int port = -1;
    protected String serviceName = null;
    protected boolean isHTTP = false;
    protected Properties props = new Properties();

    protected transient String tostring = null;

    //This flag is used to indicate if 'isHostTrusted' prop is set in
    //the imqAddressList.  If set, it over rides 'imqSSLIsHostTrusted'
    //property.  If not set, 'imqSSLIsHostTrusted' value is used.
    protected transient boolean isSSLHostTrustedSet = false;


    protected MQAddress() {}

    protected void initialize(String addr) 
        throws MalformedURLException
    {
        this.addr = addr;
        this.init();
        this.parseAndValidate();
    }

    protected void initialize(String host, int port) 
        throws MalformedURLException
    {
        if (port < 0) {
            throw new MalformedURLException("Illegal port :"+port);
        }
        if (host == null || host.trim().length() == 0) {
            this.addr = ":"+port;
        } else {
            URL u = new URL("http", host, port, "");
            this.addr = u.getHost()+":"+port;
        }
        this.init();
        this.parseAndValidate();
    }

    private void init() {
        // Set the default value for isHostTrusted attribute.
        props.setProperty(isHostTrusted, "false"); //TCR #3 default to false
    }

    protected void parseAndValidate() throws MalformedURLException {
        //String tmp = new String(addr);
        String tmp = addr;

        // Find scheme name.
        // Possible values : mq, mqtcp, mqssl, http, https
        schemeName = DEFAULT_SCHEME_NAME;
        int i = tmp.indexOf("://");
        if (i > 0) {
            schemeName = tmp.substring(0, i);
            tmp = tmp.substring(i + 3);
        }

        if (schemeName.equalsIgnoreCase("mq") ||
            schemeName.equalsIgnoreCase("mq+ssl")) {

            /*
             * Typical example -
             * mq://jpgserv:7676/ssljms?isHostTrusted=true
             */
            i = tmp.indexOf('?');
            if (i >= 0) {
                String qs = tmp.substring(i+1);
                parseQueryString(qs);
                tmp = tmp.substring(0, i);
            }
            i = tmp.indexOf('/');
            if (i >= 0) {
                serviceName = tmp.substring(i+1);
                tmp = tmp.substring(0, i);
            }

            parseHostPort(tmp);

            if (serviceName == null || serviceName.equals(""))
                serviceName = getDefaultServiceName();
        }
        else if (schemeName.equalsIgnoreCase("mqssl") ||
            schemeName.equalsIgnoreCase("mqtcp")) {
            /*
             * Typical example -
             * mqtcp://jpgserv:12345/jms
             * mqssl://jpgserv:23456/ssladmin
             */
            i = tmp.indexOf('?');
            if (i >= 0) {
                String qs = tmp.substring(i+1);
                parseQueryString(qs);
                tmp = tmp.substring(0, i);
            }

            i = tmp.indexOf('/');
            if (i >= 0) {
                serviceName = tmp.substring(i+1);
                tmp = tmp.substring(0, i);
            }
            parseHostPort(tmp);
        }
        else if (schemeName.equalsIgnoreCase("http") ||
            schemeName.equalsIgnoreCase("https")) {
            isHTTP = true;
            return;
        }
        else {
            throw new MalformedURLException(
                "Illegal address. Unknown address scheme : " + addr);
        }
    }

    protected void parseHostPort(String tmp) throws MalformedURLException {
         
        int i = tmp.indexOf(':');
        if (i != -1 && i == tmp.lastIndexOf(':')) {
            String half1 = tmp.substring(0, i).trim();
            String half2 = tmp.substring(i+1).trim();
            if (half1.length() == 0 || half2.length() == 0) {
                if (half1.length() == 0) {
                    addrHost = DEFAULT_HOSTNAME;
                } else {
                    addrHost = half1; 
                }
                if (half2.length() == 0) {
                    port = DEFAULT_PORTMAPPER_PORT;
                } else {
                    port = Integer.parseInt(half2);
                    if (port < 0) {
                        throw new MalformedURLException("Illegal port in :"+tmp);
                    }

                }
                return;
            }
        }

        URL hp = new URL("http://"+tmp);
        port = hp.getPort();
        if (port == -1) {
            port = DEFAULT_PORTMAPPER_PORT;
        }
        addrHost = hp.getHost();
        if (addrHost == null || addrHost.equals("")) {
            addrHost = DEFAULT_HOSTNAME;
        }
    }

    protected void parseQueryString(String qs) throws MalformedURLException {
        //String tmp = new String(qs);
        String tmp = qs;

        while (tmp.length() > 0) {
            String pair = tmp;

            int i = tmp.indexOf('&');
            if (i >= 0) {
                pair = tmp.substring(0, i);
                tmp = tmp.substring(i+1);
            }
            else {
                tmp = "";
            }

            int n = pair.indexOf('=');
            if (n <= 0)
                throw new MalformedURLException(
                    "Illegal address. Bad query string : " + addr);

            String name = pair.substring(0, n);
            String value = pair.substring(n+1);
            props.setProperty(name, value);

            if ( isHostTrusted.equals(name) ) {
                isSSLHostTrustedSet = true;
            }
        }
    }

    public boolean isServicePortFinal() {
        return (isHTTP || schemeName.equalsIgnoreCase("mqtcp") ||
            schemeName.equalsIgnoreCase("mqssl"));
    }

    public String getProperty(String pname) {
        return props.getProperty(pname);
    }

    public boolean getIsSSLHostTrustedSet() {
        return this.isSSLHostTrustedSet;
    }



    public String getSchemeName() {
        return schemeName;
    }

    public boolean isSSLPortMapperScheme() {
        return schemeName.equalsIgnoreCase("mq+ssl");
    }

    public String getHostName() {
        return addrHost;
    }

    public int getPort() {
        return port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean getIsHTTP() {
        return isHTTP;
    }

    public String getURL() {
        return addr;
    }

    public String toString() {

        if (tostring != null)
            return tostring;

        if (isHTTP) {
            tostring = addr;
            return addr;
        }

        tostring = schemeName + "://" + addrHost + ":" + port + "/" + serviceName;
        return tostring;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof MQAddress)) {
            return false;
        }
        return this.toString().equals(((MQAddress)obj).toString());
    }

    public String getDefaultServiceName()  {
	return (DEFAULT_SERVICENAME);
    }


     /**
     * Parses the given MQ Message Service Address and creates an
     * MQAddress object.
     */
    public static MQAddress getMQAddress(String addr) 
        throws MalformedURLException {
        MQAddress ret = new MQAddress();
        ret.initialize(addr);
        return ret;
    }

    public static MQAddress getMQAddress(String host, int port) 
        throws MalformedURLException {
        MQAddress ret = new MQAddress();
        ret.initialize(host, port);
        return ret;
    }
       

}
