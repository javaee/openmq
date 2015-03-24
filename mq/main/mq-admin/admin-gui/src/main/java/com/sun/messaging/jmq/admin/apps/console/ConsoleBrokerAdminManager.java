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
 * @(#)ConsoleBrokerAdminManager.java	1.4 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.util.Enumeration;
import java.util.Vector;
import com.sun.messaging.jmq.admin.util.UserPropertiesException;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdmin;
import com.sun.messaging.jmq.admin.bkrutil.BrokerAdminException;


public class ConsoleBrokerAdminManager {
    private String fileName = "brokerlist.properties";

    /**
     * The collection of BrokerAdmin objects.
     */
    private Vector admins = new Vector();

    public ConsoleBrokerAdminManager()  {
    }

    /**
     * Adds an instance of BrokerAdmin to the list.
     * This will simply overwrite the existing one if there is any.
     * Should make sure dups are not added by calling exist() before doing
     * this.
     */
    public void addBrokerAdmin(BrokerAdmin ba) {
	admins.addElement(ba);	
    }

    /**
     *
     */
    public void deleteBrokerAdmin(BrokerAdmin ba) {

	String baKey = ba.getKey();

	for (int i = 0; i < admins.size(); i++) {
	    BrokerAdmin ba2 = (BrokerAdmin)admins.get(i);
	    String ba2Key = ba2.getKey();
	    if (baKey.equals(ba2Key)) {
		admins.remove(i);
	        return;
	    }
	}
    }

    /**
     * Reads the files and populates the manager with
     * BrokerAdmin objects.
     *
     */
    public void readBrokerAdminsFromFile() throws UserPropertiesException, 
					BrokerAdminException {

	BrokerListProperties	blProps = readFromFile();

	int count = blProps.getBrokerCount();

	for (int i = 0; i < count; ++i)  {
	    BrokerAdmin ba = blProps.getBrokerAdmin(i);

	    addBrokerAdmin(ba);
	}
    }


    /**
     * Writes broker list to files.
     */
    public void writeBrokerAdminsToFile() throws UserPropertiesException  {

	BrokerListProperties  blProps = new BrokerListProperties();

	for (int i = 0; i < admins.size(); i++) {
	    BrokerAdmin ba = (BrokerAdmin)admins.get(i);
	    blProps.addBrokerAdmin(ba);
	}


	writeToFile(blProps);
    }

    /**
     * Returns the list of admin instances.
     */
    public Vector getBrokerAdmins() {
	return admins;
    }

    /**
     * Returns true if the key of BrokerAdmin exists in the list.
     * Returns false otherwise.
     */
    public boolean exist(String key) {
	for (int i = 0; i < admins.size(); i++) {
	    BrokerAdmin ba = (BrokerAdmin)admins.get(i);
	    String baKey = ba.getKey();
	    if (key.equals(baKey)) {
		return true;
	    }
	}

	return false;
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


    /**
     * Reads the files containing the list of Brokers.
     *
     * @return    Properties object containing list of Brokers
     *
     */
    private BrokerListProperties readFromFile() throws UserPropertiesException {

	BrokerListProperties  blProps = new BrokerListProperties();

	blProps.setFileName(fileName);
	blProps.load();

        return (blProps);
    }

    /**
     * Writes ObjStoreAttrs to files.
     *
     */
    private void writeToFile(BrokerListProperties blProps) 
					throws UserPropertiesException {
        blProps.setFileName(fileName);
        blProps.save();
    }


}
