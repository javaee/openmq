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

package com.sun.messaging.ums.service;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * This class is not used yet.  
 * 
 * @author chiaming
 */
public class ClientTable {
    
    private Hashtable<String, Client> clients = new Hashtable <String, Client>();
    
    public void put(String sid, Client client) {

        String seq = this.getSequence(sid);
        
        clients.put(sid, client);
    }
    
    private String getSequence (String sid) {
        //get sequence index
        int index = sid.indexOf('-');

        //get sequence
        String seq = sid.substring(0, index);
        
        return seq;
    }
    
    public Client get (String sid) {
        
        String seq = this.getSequence(sid);
        
        Client client = clients.get(seq);
        
        return client;
    }
    
    public int size() {
        return clients.size();
    }
    
    public Collection values() {
        return clients.values();
    }
    
    public void clear() {
        clients.clear();
    }
    
    public Client remove (String sid) {
        
        Client client = clients.remove(sid);
        
        return client;
    }
    
    public Enumeration keys() {
        return clients.keys();
    }

}
