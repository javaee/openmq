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
 *
 * @author amyk
 */

public class DestinationElement 
{
    private Properties _attrs = null;
    private Properties _props = null;
    private String _type = null;

    public DestinationElement() {}

    public void setAttributes(Properties a) throws IllegalArgumentException {
        if (a != null) {
            String refname = a.getProperty(JMSBridgeXMLConstant.Common.REFNAME);
            if (refname.equals(DMQElement.BUILTIN_DMQ_DESTNAME) ||
                refname.equals(DMQElement.BUILTIN_DMQ_NAME) ||
                refname.equals(JMSBridgeXMLConstant.Target.DESTINATIONREF_AS_SOURCE)) {
                throw new IllegalArgumentException(
                    JMSBridge.getJMSBridgeResources().getKString(
                    JMSBridgeResources.X_XML_IS_RESERVED, JMSBridgeXMLConstant.Destination.REFNAME+"="+refname));
            }
            if (a.getProperty(JMSBridgeXMLConstant.Destination.LOOKUPNAME) == null) {
                String name =  a.getProperty(JMSBridgeXMLConstant.Destination.NAME);
                if (name == null) {
                    String[] params = { JMSBridgeXMLConstant.Destination.LOOKUPNAME,
                                        JMSBridgeXMLConstant.Destination.NAME,
                                        JMSBridgeXMLConstant.Element.DESTINATION };
                    throw new IllegalArgumentException(JMSBridge.getJMSBridgeResources().getKString(
                                             JMSBridgeResources.X_XML_NO_LOOKUP_NO_NAME_ELEMENT, params));
                }
                if (name.equals(DMQElement.BUILTIN_DMQ_DESTNAME)) {
                    throw new IllegalArgumentException(
                        JMSBridge.getJMSBridgeResources().getKString(
                        JMSBridgeResources.X_XML_IS_RESERVED,JMSBridgeXMLConstant.Destination.NAME+"="+name));
                }
            }
        } 
        _attrs = a;
    }

    public void setProperties(Properties a) {
        _props = a;
    }

    public Properties getAttributes() {
        return _attrs;
    }

    public Properties getProperties() {
        return _props;
    }

    public String getName() throws Exception {
        String name =  _attrs.getProperty(JMSBridgeXMLConstant.Destination.NAME);
        String lookup = _attrs.getProperty(JMSBridgeXMLConstant.Destination.LOOKUPNAME);
        if (lookup != null) {
            throw new UnsupportedOperationException(
            "Called when "+JMSBridgeXMLConstant.Destination.LOOKUPNAME+ " is specified");
        }
        if (name == null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
                JMSBridgeResources.X_XML_ATTR_NOT_SPECIFIED,
                JMSBridgeXMLConstant.Destination.NAME,
                JMSBridgeXMLConstant.Element.DESTINATION));
        }
        return name;
    }

    public String getLookupName() {
        return _attrs.getProperty(JMSBridgeXMLConstant.Destination.LOOKUPNAME);
    }

    public String getRefName() {
        return _attrs.getProperty(JMSBridgeXMLConstant.Destination.REFNAME);
    }

    public boolean isQueue() throws Exception {
        if (_type == null) {
            _type = _attrs.getProperty(JMSBridgeXMLConstant.Destination.TYPE);
        }
        if (getLookupName() != null) {
            throw new UnsupportedOperationException(
            "Called when "+JMSBridgeXMLConstant.Destination.LOOKUPNAME+ " is specified");
        }
        if (_type == null) {
            throw new IllegalArgumentException(
            JMSBridge.getJMSBridgeResources().getKString(
                JMSBridgeResources.X_XML_ATTR_NOT_SPECIFIED,
                JMSBridgeXMLConstant.Destination.TYPE,
                JMSBridgeXMLConstant.Element.DESTINATION));
        }
        _type = _type.trim().toLowerCase();
        return !_type.equals(JMSBridgeXMLConstant.Destination.TOPIC);
    }

    public String toString() {
        return JMSBridgeXMLConstant.Element.DESTINATION+"["+getRefName()+"]";
    }
}
