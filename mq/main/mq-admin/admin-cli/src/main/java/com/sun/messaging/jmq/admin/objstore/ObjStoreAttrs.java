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
 * @(#)ObjStoreAttrs.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.admin.objstore;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;

/**
 * This class contains attributes needed to open the ObjStore
 * managed by the ObjStoreManager.
 * 
 * For the ObjStore using JNDI, this object will be directly passed
 * on to create the initialContext.
 */
public class ObjStoreAttrs extends Hashtable {

    // Debug messages on / off.
    private boolean DEBUG = false;

    /**
     * Unique ID for this class.  This will also be used as the unique ID for
     * the corresponding ObjStore.
     */
    private String id = null;

    /**
     * Description for this class.
     */
    private String description = null;

    /**
     * Type of this object store.
     * By default, it is JNDI.
     */
    private int type = ObjStoreManager.JNDI;

    /**
     * The default id and description values given to this class.
     */
    private static final String DEFAULT = "default";

    /**
     * Stores keys of non-stored attributes
     */
    private Vector transientAttrs = new Vector();

    /**
     * Constructor.
     */
    public ObjStoreAttrs() {
	this(DEFAULT, DEFAULT);
    }

    /**
     * Constructor.
     * @param id	unique id for this class.
     */
    public ObjStoreAttrs(String id) {
	this(id, id);
    }

    /**
     * Constructor.
     * @param type	type of this class.
     */
    public ObjStoreAttrs(int type) {
	this(DEFAULT, DEFAULT, type);
    }

    /**
     * Constructor.
     * @param id	unique id for this class.
     * @param type	type of this class.
     */
    public ObjStoreAttrs(String id, int type) {
	this(id, id, type);
    }

    /**
     * Constructor.
     * @param id	unique id for this class.
     * @param desc	description for this class.
     * @param type	type of this class.
     */
    public ObjStoreAttrs(String id, String desc, int type) {
	this(id, desc);
	this.type = type;
    }

    /**
     * Constructor.
     * @param id	unique id for this class.
     * @param desc	description for this class.
     */
    public ObjStoreAttrs(String id, String desc) {
	this.id = id;
	this.description = desc;
    }

    public String getID() {
	return id;
    }

/**
 * Disabling the method so that I don't have to worry about cloing the object
 * when this class is used by multiple ObjStores.
 * 
    public void setID(String id) {
	this.id = id;
    }
*/

    public String getDescription() {
	return description;
    }

/**
 * Disabling the method so that I don't have to worry about cloing the object
 * when this class is used by multiple ObjStores.
 * 
    public void setDescription(String desc) {
	this.description = desc;
    }
*/

    public int getType() {
	return type;
    }

/**
 * Disabling the method so that I don't have to worry about cloing the object
 * when this class is used by multiple ObjStores.
 * 
    public void setType(int type) {
	this.type = type;
    }
*/

    /**
     * Sets the attribute transient.  When the attribute is transient,
     * it will not be saved (serialized) when writeObject() is called.
     */
    public void setAttrTransient(String key) {
	if (!transientAttrs.contains(key))
	    transientAttrs.addElement(key);
    }

    /**
     * Sets the attribute permanent.  When the attribute is permanent,
     * it will be saved (serialized) when writeObject() is called.
     */
    public void setAttrPermanent(String key) {
	if (transientAttrs.contains(key))
	    transientAttrs.removeElement(key);
    }

    /**
     * Prepares this class by removing all the transient attributes.
     * This must be called prior to terminating the objStoreManager.
     */
    public void prepareToTerminate() {
      
	// REVISIT:
	// The best thing to do is to put this code inside of writeObject()
	// so that this cannot be executed independent of writeObject().
	// However, in order to make these two operations an atomic unit
	// one needs to implement Externalizable instead of Serializable, which
	// is prone to more errors.
	// Since the API user will not be able to access this method, it is
	// safe to make them separate and keep the logic and code simple.
	// The only thing is that the ObjStoreManager must call this method
	// prior to terminating so that transient fields will not be stored.
	// If I come up with a better approach, this may be enhanced, but this
	// will do its work just fine as is for now.

	if (DEBUG) 
	System.out.println("DEBUG: transientAttrs -> " + transientAttrs);

	Enumeration e = transientAttrs.elements();

        while (e != null && e.hasMoreElements()) {
            String key = (String)e.nextElement();
	    this.remove(key);	    
	}

	transientAttrs.removeAllElements();
    }
}
