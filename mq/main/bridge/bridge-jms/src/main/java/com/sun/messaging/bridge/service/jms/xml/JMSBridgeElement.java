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

package com.sun.messaging.bridge.service.jms.xml;

import java.util.*;
import com.sun.messaging.bridge.service.jms.JMSBridge;
import com.sun.messaging.bridge.service.jms.resources.JMSBridgeResources;

/**
 * @author amyk
 */
public class JMSBridgeElement 
{
    private Properties _attrs = null;

    private LinkedHashMap<String, LinkElement> _links = 
                new LinkedHashMap<String, LinkElement>();

    private LinkedHashMap<String, DMQElement> _dmqs = 
                new LinkedHashMap<String, DMQElement>();

    private LinkedHashMap<String, DestinationElement> _dests =
                new LinkedHashMap<String, DestinationElement>();

    private LinkedHashMap<String, ConnectionFactoryElement> _cfs =
                new LinkedHashMap<String, ConnectionFactoryElement>();

    public JMSBridgeElement() {}

    public void setAttributes(Properties a) {
        _attrs = a;
        if (a != null) {
            if (getName().equals(DMQElement.BUILTIN_DMQ_DESTNAME) ||
                getName().equals(DMQElement.BUILTIN_DMQ_NAME)) {
                throw new IllegalArgumentException(
                JMSBridgeXMLConstant.Element.JMSBRIDGE+" "+
                JMSBridgeXMLConstant.Common.NAME+" "+getName()+" is reserved.");
            }
        }
    }

    public String getName() {
        return _attrs.getProperty(JMSBridgeXMLConstant.Common.NAME);
    }

    public boolean tagBridgeName() {
        return Boolean.valueOf(_attrs.getProperty(
                   JMSBridgeXMLConstant.JMSBRIDGE.TAG_BRIDGENAME,
                   JMSBridgeXMLConstant.JMSBRIDGE.TAG_BRIDGENAME_DEFAULT)).booleanValue();
    }

    public boolean logMessageTransfer() {
        return Boolean.valueOf(_attrs.getProperty(
                   JMSBridgeXMLConstant.JMSBRIDGE.LOG_MESSAGE_TRANSFER,
                   JMSBridgeXMLConstant.JMSBRIDGE.LOG_MESSAGE_TRANSFER)).booleanValue();
    }

    public void addLink(LinkElement l) throws Exception {
        String name = l.getName();
        if (name.equals(DMQElement.BUILTIN_DMQ_DESTNAME) ||
            name.equals(DMQElement.BUILTIN_DMQ_NAME) ||
            name.equals(JMSBridge.BRIDGE_NAME_PROPERTY)) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_IS_RESERVED, JMSBridgeXMLConstant.Element.LINK+" "+JMSBridgeXMLConstant.Common.NAME+"="+name));
        }
        LinkElement pre = _links.get(name);
        if (pre != null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_ALREADY_EXIST,
            JMSBridgeXMLConstant.Element.LINK, JMSBridgeXMLConstant.Common.NAME+"="+name));
        }
        
        _links.put(name, l);
    }

    public Map getLinks() {
        return _links;
    }

    public void addDMQ(DMQElement d) throws Exception {
        String name = d.getName();
        if (name.equals(DMQElement.BUILTIN_DMQ_DESTNAME)) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_IS_RESERVED, JMSBridgeXMLConstant.Element.DMQ+" "+JMSBridgeXMLConstant.Common.NAME+"="+name));
        }
        DMQElement pre = _dmqs.get(name);
        if (pre != null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_ALREADY_EXIST,
            JMSBridgeXMLConstant.Element.DMQ, JMSBridgeXMLConstant.Common.NAME+"="+name));
        }
        
        _dmqs.put(name, d);
    }

    public DMQElement getBuiltInDMQ() throws Exception {
        DMQElement dmq =  _dmqs.get(DMQElement.BUILTIN_DMQ_NAME);
        if (dmq == null) {
            dmq = new DMQElement();
            Properties prop = new Properties();
            prop.setProperty(JMSBridgeXMLConstant.Common.NAME, DMQElement.BUILTIN_DMQ_NAME);
            dmq.setAttributes(prop);
        }
        return dmq;
    }

    public Map getDMQs() {
        return _dmqs;
    } 

    public void addCF(String ref, ConnectionFactoryElement ecf) throws Exception {
        if (ref == null) {
            throw new IllegalArgumentException(
            JMSBridgeXMLConstant.Common.REFNAME+" null for "+ecf);
        }
        if (ref.equals(DMQElement.BUILTIN_DMQ_NAME) ||
            ref.equals(DMQElement.BUILTIN_DMQ_DESTNAME)) {
            throw new IllegalArgumentException(
            JMSBridgeXMLConstant.Element.CF+" "+
            JMSBridgeXMLConstant.Common.REFNAME+" "+ref+" is reserved.");
        }
        ConnectionFactoryElement pre = _cfs.get(ref);
        if (pre != null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_ALREADY_EXIST,
            JMSBridgeXMLConstant.Element.CF, JMSBridgeXMLConstant.Common.REFNAME+"="+ref));
        }
        
        _cfs.put(ref, ecf); 
    }

    public ConnectionFactoryElement getCF(String ref) throws Exception {
        ConnectionFactoryElement ecf = _cfs.get(ref);
        if (ecf == null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_DONOT_EXIST,
            JMSBridgeXMLConstant.Element.CF, JMSBridgeXMLConstant.Common.REFNAME+"="+ref));
        }
        return ecf;
    }

    public Map getAllCF() {
        return _cfs;
    }

    public void addDestination(String ref, DestinationElement ed) throws Exception {
        if (ref == null) {
            throw new IllegalArgumentException(
            JMSBridgeXMLConstant.Common.REFNAME+" null for "+ed);
        }
        if (ref.equals(DMQElement.BUILTIN_DMQ_NAME) ||
            ref.equals(DMQElement.BUILTIN_DMQ_DESTNAME) ||
            ref.equals(JMSBridgeXMLConstant.Target.DESTINATIONREF_AS_SOURCE)) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_IS_RESERVED, JMSBridgeXMLConstant.Element.DESTINATION+" "+
            JMSBridgeXMLConstant.Common.REFNAME+"="+ref));
        }
        DestinationElement pre = _dests.get(ref);
        if (pre != null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_ALREADY_EXIST,
            JMSBridgeXMLConstant.Element.DESTINATION, JMSBridgeXMLConstant.Common.REFNAME+"="+ref));
        }
        
        _dests.put(ref, ed); 
    }

    public DestinationElement getDestination(String ref) throws Exception {
        DestinationElement ed = _dests.get(ref);
        if (ed == null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
            JMSBridgeResources.X_XML_ELEMENT_DONOT_EXIST,
            JMSBridgeXMLConstant.Element.DESTINATION, JMSBridgeXMLConstant.Common.REFNAME+"="+ref));
        }
        return ed;
    }

}
