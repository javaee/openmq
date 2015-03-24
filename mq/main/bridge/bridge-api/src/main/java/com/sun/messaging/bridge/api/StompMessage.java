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

package com.sun.messaging.bridge.api;

import java.util.Enumeration;

/**
 * This interface encapsulates all information needed to convert  
 * between a StompFrameMessage and a provider message object 
 *
 * @author amyk 
 */
public interface StompMessage  {

    /**************************
     * to StompFrameMessage
     ************************************/
    public String getSubscriptionID() throws Exception; 
    public String getDestination() throws Exception; 
    public String getReplyTo() throws Exception;
    public String getJMSMessageID() throws Exception;
    public String getJMSCorrelationID() throws Exception;
    public String getJMSExpiration() throws Exception;
    public String getJMSRedelivered() throws Exception;
    public String getJMSPriority() throws Exception;
    public String getJMSTimestamp() throws Exception; 
    public String getJMSType() throws Exception;
    public Enumeration getPropertyNames() throws Exception;
    public String getProperty(String name) throws Exception;
    public boolean isTextMessage() throws Exception; 
    public boolean isBytesMessage() throws Exception; 
    //to be called only if isTextMessage() return true
    public String getText() throws Exception;
    //to be called only if isBytesMessage() return true
    public byte[] getBytes() throws Exception;

    /**************************
     * from StompFrameMessage
     ************************************/
    //either setText or setBytes (not both) to be called first
    public void setText(StompFrameMessage message) throws Exception;
    public void setBytes(StompFrameMessage message) throws Exception;
    public void setDestination(String stompdest) throws Exception;
    public void setPersistent(String v) throws Exception;
    public void setReplyTo(String replyto) throws Exception;
    public void setJMSCorrelationID(String v) throws Exception;
    public void setJMSExpiration(String v) throws Exception;
    public void setJMSPriority(String v) throws Exception;
    public void setJMSType(String v) throws Exception;
    public void setProperty(String name, String value) throws Exception;

}
