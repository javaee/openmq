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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.sun.messaging.bridge.api.BridgeContext;
import com.sun.messaging.jms.MQRuntimeException;

/**
 * @author amyk
 */
public class ConnectionFactoryImpl implements ConnectionFactory, Refable {

    private ConnectionFactory _cf = null;
    private String _ref = null;
    private boolean _isEmbeded = false;
    private boolean _firstTime = true;

    private BridgeContext _bc = null;
    private Properties _jmsprop = null;
    private boolean _isadmin = false;

    public ConnectionFactoryImpl(ConnectionFactory cf, String ref) {
        _cf = cf;
        _ref = ref;
    }

    public ConnectionFactoryImpl(BridgeContext bc, Properties jmsprop,
                                 boolean isEmbeded, String ref)
                                 throws Exception {
        this(bc, jmsprop, false, isEmbeded, ref);
    }
    public ConnectionFactoryImpl(BridgeContext bc, Properties jmsprop,
                                 boolean isadmin, boolean isEmbeded, String ref)
                                 throws Exception {
        _bc = bc;
        _jmsprop = jmsprop;
        _isadmin = isadmin;
        if (!isadmin) {
            _cf =_bc.getConnectionFactory(_jmsprop);
        } else {
            _cf =_bc.getAdminConnectionFactory(_jmsprop);
        }
        _ref = ref;
        _isEmbeded = isEmbeded;
    }

    public Connection createConnection() throws JMSException {
    	return getConnectionFactory().createConnection();
    }

    public Connection createConnection(String userName, String password) throws JMSException {  	
    	return getConnectionFactory().createConnection(userName, password);
    }
    
	@Override
	public JMSContext createContext() {
		try {
			return getConnectionFactory().createContext();
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
	}

	@Override
	public JMSContext createContext(String userName, String password) {
		try {
			return getConnectionFactory().createContext(userName,password);
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
	}

	@Override
	public JMSContext createContext(String userName, String password, int sessionMode) {
		try {
			return getConnectionFactory().createContext(userName,password,sessionMode);
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
	}

	@Override
	public JMSContext createContext(int sessionMode) {
		try {
			return getConnectionFactory().createContext(sessionMode);
		} catch (JMSException e) {
			throw new MQRuntimeException(e);
		}
	}

	private ConnectionFactory getConnectionFactory() throws JMSException {
		if (_bc != null) {
	        ConnectionFactory cf = null;
	        try {
	            if (!_isadmin) {
	                cf = _bc.getConnectionFactory(_jmsprop);
	            } else {
	                cf = _bc.getAdminConnectionFactory(_jmsprop);
	            }
	        } catch (Exception e) {
	            JMSException jmse = new JMSException(e.getMessage(),
	                JMSBridge.getJMSBridgeResources().E_EXCEPTION_CREATE_CF);
	            jmse.setLinkedException(e);
	            throw jmse;
	        }
	        return cf;
		}
		return _cf;
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
        return false;
    }

    public String toString() {
        String refs = _ref+(_isEmbeded ? ", embeded":"");
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
