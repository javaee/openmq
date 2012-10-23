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

package com.sun.messaging.bridge.api;

import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
import org.jvnet.hk2.annotations.Contract;
import org.glassfish.hk2.api.PerLookup;


/**
 * The <CODE>Bridge</CODE> interface is to be implemented by  
 * an external (to the bridge service manager) bridge service
 *
 * @author amyk
 */
@Contract
@PerLookup
public interface Bridge 
{

   final public static String JMS_TYPE = "JMS";
   final public static String STOMP_TYPE = "STOMP";

    public enum State {
        STOPPING { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_STOPPING); }},
        STOPPED  { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_STOPPED); }},
        STARTING { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_STARTING); }},
        STARTED  { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_STARTED); }},
        PAUSING  { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_PAUSING); }},
        PAUSED   { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_PAUSED); }},
        RESUMING { public String toString(ResourceBundle rb) { return rb.getString(BridgeCmdSharedResources.I_STATE_RESUMING); }};

        public abstract String toString(ResourceBundle rb);
    };

    /**
     * Start the bridge
     *
     * @param bc the bridge context
     * @param args start parameters
     *
     * @return true if successfully started; false if started asynchronously
     *
     * @throws Exception if start failed
     */
    public boolean start(BridgeContext bc, String[] args) throws Exception;

    /**
     *  Pause the bridge
     *
     * @param bc the bridge context
     * @param args pause parameters  
     *
     * @throws Exception if unable to pause the bridge
     */
    public void pause(BridgeContext bc, String[] args) throws Exception;

    /**
     *  Resume the bridge
     *
     * @param bc the bridge context
     * @param args resume parameters  
     *
     * @throws Exception if unable to resume the bridge
     */
    public void resume(BridgeContext bc, String[] args) throws Exception;


    /**
     * Stop the bridge
     *
     * @param bc the bridge context
     * @param args stop parameters  
     *
     * @throws Exception if unable to stop the bridge 
     */
    public void stop(BridgeContext bc, String[] args) throws Exception;

    /**
     * List the bridge
     *
     * @param bc the bridge context
     * @param args list parameters  
     * @param rb ResourceBundle to be get String resources for data 
     *
     * @throws Exception if unable to list the bridge 
     */
    public ArrayList<BridgeCmdSharedReplyData> list(BridgeContext bc,
                                                    String[] args, 
                                                    ResourceBundle rb)
                                                    throws Exception;


    /**
     *
     * @return the type of the bridge
     */
    public String getType();

    /**
     *
     * @return true if multiple of this type of bridge can coexist
     */
    public boolean isMultipliable();

    /**
     * Guarantee to be called before start() method is called
     */
    public void setName(String name);

    /**
     *
     * @return get the bridge's name
     */
    public String getName();

    /**
     *
     * @return the current state of the bridge 
     */
    public State getState();

    /**
     * 
     * @return an object of exported service corresponding to the className
     */
    public Object getExportedService(String className, Properties props) throws Exception;
}
