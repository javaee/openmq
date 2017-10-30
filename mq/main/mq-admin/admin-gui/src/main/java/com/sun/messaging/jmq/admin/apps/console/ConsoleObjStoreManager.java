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

/*
 * @(#)ConsoleObjStoreManager.java	1.7 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.util.Enumeration;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.objstore.ObjStoreAttrs;
import com.sun.messaging.jmq.admin.objstore.ObjStore;
import com.sun.messaging.jmq.admin.objstore.ObjStoreManager;
import com.sun.messaging.jmq.admin.objstore.ObjStoreException;
import com.sun.messaging.jmq.admin.util.UserPropertiesException;

/**
 * This class manages all the instances of the ObjStores that it 
 * creates.  It also provides the default implementations for the 
 * miscellaneous useful operations for the manager.  The user should 
 * extend this class and overwrite methods if he wishes to provide 
 * different implementations or add more operations.
 */
public class ConsoleObjStoreManager extends ObjStoreManager {


    /**
     * The reference to this class itself.
     */
    private static ConsoleObjStoreManager mgr = null;

    private String fileName = "objstorelist.properties";

    /**
     * Private constructor for this class.
     * This is called only once.
     */
    protected ConsoleObjStoreManager() {
	super();
    }

    /**
     * If the ObjStoreManager was requested more than once, this
     * will simply return the same instance.
     *
     * @return  ConsoleObjStoreManager  the only one instance of this class
     *
     * REVISIT:
     * This allows two instances (ObjStoreManager and ConsoleObjStoreManager)
     * to coexist, which is NO GOOD.  We should only have one instance of
     * it - either ObjStoreManager OR ConsoleObjStoreManager OR a custom 
     * manager.  This needs to be fixed.
     */
    public static synchronized 
	ConsoleObjStoreManager getConsoleObjStoreManager() {

        if (mgr == null)
            mgr = new ConsoleObjStoreManager();
        return mgr;
    }

    /**
     * Reads the files and populates objStores.
     *
     */
    public void readObjStoresFromFile() throws UserPropertiesException, 
					ObjStoreException {

	ObjStoreListProperties	oslProps = readFromFile();

	int count = oslProps.getObjStoreCount();

	for (int i = 0; i < count; ++i)  {
	    ObjStoreAttrs osa = oslProps.getObjStoreAttrs(i);

	    createStore(osa);
	}
    }

    /**
     * Reads the files containing ObjStoreAttrs.
     *
     * @return    Properties object containing list of ObjectStoreAttrs
     *
     */
    private ObjStoreListProperties readFromFile() throws UserPropertiesException {

	ObjStoreListProperties  oslProps = new ObjStoreListProperties();

	oslProps.setFileName(fileName);
	oslProps.load();

        return (oslProps);
    }

    /**
     * Writes ObjStores to files.
     */
    public void writeObjStoresToFile() throws UserPropertiesException  {

        // Must call ObjStoreAttrs.prepareToTerminate() prior to
        // storing

	Enumeration e = objStores.elements();
	ObjStoreListProperties  oslProps = new ObjStoreListProperties();

	while (e.hasMoreElements()) {

	    ObjStore os = (ObjStore)e.nextElement();
	    ObjStoreAttrs osa = os.getObjStoreAttrs();
	    osa.prepareToTerminate();

	    oslProps.addObjStoreAttrs(osa);
	}

	writeToFile(oslProps);
    }

    /**
     * Writes ObjStoreAttrs to files.
     *
     */
    private void writeToFile(ObjStoreListProperties oslProps) 
					throws UserPropertiesException {
        oslProps.setFileName(fileName);
        oslProps.save();
    }

    /**
     * Sets the name of the file where the objstore list is saved
     * when writeObjStoresToFile() is called. This is also the file
     * that is read from when readObjStoresFromFile() is called.
     *
     * @param     fileName	The fileName where the object stores
     *				are read from and written to.
     */
    public void setFileName(String fileName)  {
        this.fileName = fileName;
    }
}
