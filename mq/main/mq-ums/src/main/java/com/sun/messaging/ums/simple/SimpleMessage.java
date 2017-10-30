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

package com.sun.messaging.ums.simple;

import com.sun.messaging.ums.common.Constants;
import java.util.Map;

/**
 *
 * @author chiaming
 */
public class SimpleMessage {
    
    /**
     * Message type
     */
    public static final int LOGIN_SERVICE_TYPE = 100;
    
    public static final int SEND_SERVICE_TYPE = 101;
    
    public static final int RECEIVE_SERVICE_TYPE = 102;
    
    public static final int CLOSE_SERVICE_TYPE = 103;
    
    public static final int COMMIT_SERVICE_TYPE = 104;
    
    public static final int ADMIN_SERVICE_TYPE = 900;
    
    public static final int INVALID_SERVICE_TYPE = -1;
    
    //public static final String SID = "ums.sid";

    //public static final String DOMAIN = "ums.domain";
   
    //public static final String USER = "ums.user";
    
    //public static final String PASSWORD = "ums.password";
    
    //public static final String SERVICE = "ums.service";
   
  
    /**
     * plain text message body content type.
     */
    public static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
      
    private String text = null;
    
    //private String destinationName = null;
    
    private Map properties = null;
    
    private int myServiceType = this.INVALID_SERVICE_TYPE;
    
    public SimpleMessage(Map map, String text) {
        this.properties = map;
        this.text = text;
        
        init();
    }
    
    private void init() {
        
        if (this.isLoginService()) {
            this.myServiceType = LOGIN_SERVICE_TYPE;
        } else if (this.isSendService()) {
            this.myServiceType = SEND_SERVICE_TYPE;
        } else if (this.isReceiveService()) {
            this.myServiceType = RECEIVE_SERVICE_TYPE;
        } else if (this.isCloseService()) {
            this.myServiceType = CLOSE_SERVICE_TYPE;
        } else if (this.isCommitService()) {
            this.myServiceType = COMMIT_SERVICE_TYPE;
        }
    
    }
    
    public int getServiceType() {
        return this.myServiceType;
    }
     
    public Map getMessageProperties() {
        return this.properties;
    }
    
    public String getMessageProperty (String name) {
        
        String[] values =  (String[]) this.properties.get (name);
        
        if (values != null) {
            return values [0];
        } else {
            return null;
        }
    }
        
    public String getText() {
        return this.text;
    }
    
    //public void setText (String text) {
    //    this.text = text;
    //}
    
    public boolean isTopicDomain() {
        boolean istopic = false;
        
        String domain = this.getMessageProperty(Constants.DOMAIN);
        
        if ("topic".equals(domain)) {
            istopic = true;
        } 
        
        return istopic;
        
    }
    
    public boolean isLoginService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_LOGIN.equals(service));
    }
    
    public boolean isSendService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_SEND_MESSAGE.equals(service));
    }
    
    public boolean isReceiveService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_RECEIVE_MESSAGE.equals(service));
    }
    
    public boolean isCommitService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_COMMIT.equals(service));
    }
    
    public boolean isRollbackService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_ROLLBACK.equals(service));
    }
    
    public boolean isCloseService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return (Constants.SERVICE_VALUE_CLOSE.equals(service));
    }
    
    public boolean isAdminService () {
        String service = this.getMessageProperty(Constants.SERVICE_NAME);
        
        return ("admin".equals(service));
    }
    
}
