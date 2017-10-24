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

package com.sun.messaging.bridge.service.jms; 

import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAJMSContext;

import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.jmq.jmsclient.ContainerType;
import com.sun.messaging.jmq.jmsclient.XAJMSContextImpl;
import com.sun.messaging.jms.MQRuntimeException;

/**
 * @author amyk
 *
 */
public class XAConnectionFactoryImpl implements XAConnectionFactory, Refable  {
    
    private XAConnectionFactory _cf = null;
    private String _ref = null;
    private boolean _isEmbeded = false;
    private boolean _isMultiRM = false;
    private boolean _firstTime = true;

    private BridgeContext _bc = null;
    private Properties _jmsprop = null;

    public XAConnectionFactoryImpl(XAConnectionFactory cf, 
                                   String ref, 
                                   boolean isMultiRM) {
        _cf = cf;
        _ref = ref;
        _isMultiRM = isMultiRM;
    }

    public XAConnectionFactoryImpl(BridgeContext bc, Properties jmsprop, 
                                   boolean isEmbeded,
                                   String ref, 
                                   boolean isMultiRM) throws Exception {
        _bc = bc;
        _jmsprop = jmsprop;
        _cf = bc.getXAConnectionFactory(jmsprop);
        _ref = ref;
        _isEmbeded = isEmbeded;
        _isMultiRM = isMultiRM;
    }

    public XAConnection
    createXAConnection() throws JMSException {
    if (_bc != null) {
        XAConnectionFactory cf = null;
        try {
            cf = _bc.getXAConnectionFactory(_jmsprop);
        } catch (Exception e) {
            JMSException jmse = new JMSException(e.getMessage(),
                JMSBridge.getJMSBridgeResources().E_EXCEPTION_CREATE_CF);
            jmse.setLinkedException(e);
            throw jmse;
        }
        return cf.createXAConnection();
    }
    return _cf.createXAConnection();
    }


    public XAConnection
    createXAConnection(String userName, String password) 
                                    throws JMSException {
    if (_bc != null) {
        XAConnectionFactory cf = null;
        try {
            cf = _bc.getXAConnectionFactory(_jmsprop);
        } catch (Exception e) {
            JMSException jmse = new JMSException(e.getMessage(),
                JMSBridge.getJMSBridgeResources().E_EXCEPTION_CREATE_CF);
            jmse.setLinkedException(e);
            throw jmse;
        }
        return cf.createXAConnection(userName, password);
    }
    return _cf.createXAConnection(userName, password);
    }
    
	@Override
	public XAJMSContext createXAContext() {
	    if (_bc != null) {
	        XAConnectionFactory cf = null;
	        try {
	            cf = _bc.getXAConnectionFactory(_jmsprop);
	        } catch (Exception e) {
	        	JMSRuntimeException jmse = new MQRuntimeException(e.getMessage(),
	                JMSBridge.getJMSBridgeResources().E_EXCEPTION_CREATE_CF,e);
	            throw jmse;
	        }
	        return cf.createXAContext();
	    }
	    return _cf.createXAContext();
	}

	@Override
    public XAJMSContext createXAContext(String userName, String password) {
    if (_bc != null) {
        XAConnectionFactory cf = null;
        try {
            cf = _bc.getXAConnectionFactory(_jmsprop);
        } catch (Exception e) {
        	JMSRuntimeException jmse = new MQRuntimeException(e.getMessage(),
                JMSBridge.getJMSBridgeResources().E_EXCEPTION_CREATE_CF,e);
            throw jmse;
        }
        return cf.createXAContext(userName, password);
    }
    return _cf.createXAContext(userName, password);
    }

    public String getRef() {
        return _ref;
    }

    public Object getRefed() {
        return _cf;
    }

    public boolean isEmbeded() {
        return _isEmbeded; 
    }

    public boolean isMultiRM() {
        return _isMultiRM; 
    }

    public String toString() {
        String refs = _ref+(_isEmbeded ? ", embeded":"")+(_isMultiRM ? ", multirm":"");
        String s = null;
        if (_firstTime) {
            s = "["+refs+"]"+_cf.toString();
            _firstTime = false;
        } else {
            s = "["+refs+"]"+_cf.getClass().getName();
        }
        return s;
    }
    
}
