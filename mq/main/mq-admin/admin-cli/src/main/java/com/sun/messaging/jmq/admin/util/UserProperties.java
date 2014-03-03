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
 * @(#)UserProperties.java	1.7 06/27/07
 */ 

package com.sun.messaging.jmq.admin.util;

import java.io.*;
import java.io.File;
import java.util.Properties;
import com.sun.messaging.jmq.admin.resources.AdminResources;

/**
 * This class can be used to save user property files.
 * This class can be subclassed for use in any application requiring
 * loading/saving of user preferences.
 *
 * <P>
 * It has a default directory location of:
 * <PRE>
 *  &lt;value of user.home property&gt;/.imq/admin
 * </PRE>
 * and a default file name of "admin.properties".
 *
 * <P>
 * The name of the property file and directory can be configured.
 *
 * <P>
 * If the property file does not exist when a save is attempted,
 * it will be created (including the directory hierarchy).
 */

public class UserProperties extends Properties  {
    private static AdminResources ar = Globals.getAdminResources();

    private final static String JMQPREFSROOT	= ".imq";
    private final static String JMQADMINROOT	= "admin";

    private String	fileName,
			dirName;

    /**
     * Instantiate a UserProperties object.
     */
    public UserProperties()  {
	super();

	String	userHome = System.getProperty("user.home");

	fileName = "admin.properties";
	dirName = userHome 
			+ File.separator 
			+ JMQPREFSROOT 
			+ File.separator 
			+ JMQADMINROOT;
    }

    /**
     * Set save file name. The default for this is "admin.properties"
     *
     * @param fileName The filename to use when saving this property object.
     */
    public void setFileName(String fileName)  {
	this.fileName = fileName;
    }

    /**
     * Get absolute save file name.
     *
     * @return fileName The absolute path to the file used when saving this property object.
     */
    public String getAbsoluteFileName()  {
	String	absFileName = dirName + File.separator + fileName;

	return (absFileName);
    }

    /**
     * Set save directory name. The default for this is:
     * <PRE>
     *  &lt;value of user.home property&gt;/.imq/admin
     * </PRE>
     *
     * @param dirName The directory name to use when saving this property object.
     */
    public void setDirName(String dirName)  {
	this.dirName = dirName;
    }

    /**
     * Loads properties from file.
     * <P>
     * This method will load the properties from the file specified via setFileName()
     * or the default if none was specified. The directory used here is the one
     * specified by setDirName() or a default if none was specified.
     */
    public void load() throws UserPropertiesException, SecurityException  {
	String	absFileName = dirName + File.separator + fileName,
		errStr;
	File	propFile;
	
	propFile = new File(absFileName);

	if (!propFile.exists())  {
	    /*
	    UserPropertiesException upe;
	    errStr = "Property file:\n"
	            + propFile.getAbsolutePath()
	            + "\n"
	            + "does not exist.";

	    upe = new UserPropertiesException(errStr);
            throw (upe);
	    */
	    return;
        }

        if (!propFile.canRead())  {
            UserPropertiesException upe;
            upe = new UserPropertiesException(
		    ar.getString(ar.E_PROPFILE_NOT_READABLE, propFile.getAbsolutePath()));
            throw (upe);
        }

	loadFile(propFile);
    }

    private void loadFile(File propFile) throws UserPropertiesException  {
	FileInputStream	fis;

	try  {
	    fis = new FileInputStream(propFile);

	    load(fis);
	} catch (Exception ex)  {
	    UserPropertiesException upe;
	    upe = new UserPropertiesException(
		    ar.getString(ar.E_FAILED_TO_OPEN_PROPFILE, 
			propFile.getAbsolutePath(), ex.toString()));
	    throw (upe);
	}
    }

    /**
     * Saves properties to file.
     * <P>
     * This method will save the properties to the file specified via setFileName()
     * or the default if none was specified. The directory used here is the one
     * specified by setDirName() or a default if none was specified.
     */
    public void save() throws UserPropertiesException, SecurityException  {
	String absFileName = dirName + File.separator + fileName;
	File	propDir, propFile;
	
	propDir = new File(dirName);
	propFile = new File(absFileName);

	if (propFile.exists())  {
	    if (!propFile.canWrite())  {
	        UserPropertiesException upe;
	        upe = new UserPropertiesException(
			    ar.getString(ar.E_PROPFILE_NOT_WRITEABLE, 
				    propFile.getAbsolutePath()));

	        throw (upe);
	    }
	} else  {
	    if (propDir.exists())  {
	        if (!propDir.canWrite())  {
	            UserPropertiesException upe;
	            upe = new UserPropertiesException(
				ar.getString(ar.E_CANNOT_CREATE_PROPFILE,
					propFile.getAbsolutePath()));

	            throw (upe);
                }
            } else  {
                propDir.mkdirs();
            }
        }

	try  {
	    FileOutputStream fos = new FileOutputStream(propFile);

	    store(fos, null);
	} catch (IOException ioe)  {
	    UserPropertiesException upe;
	    upe = new UserPropertiesException(
		    ar.getString(ar.E_FAILED_TO_WRITE_PROPFILE, 
			propFile.getAbsolutePath(), ioe.toString()));
	    throw (upe);
	}
    }
}
