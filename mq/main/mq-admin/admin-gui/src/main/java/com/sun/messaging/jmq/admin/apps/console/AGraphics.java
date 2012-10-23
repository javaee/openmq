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
 * @(#)AGraphics.java	1.19 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.io.File;
import javax.swing.ImageIcon;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.Version;


/** 
 * This class initializes all the images used by the iMQ admin
 * console.
 *
 * This class is also used to access the images. Here is an
 * example:
 * <PRE>
 *     JLabel l = new JLabel(AGraphics.adminImages[AGraphics.SPLASH_SCREEN]);
 * </PRE>
 */
public class AGraphics  {

    /*
     * Image file names.
     *
     * File names are relative to
     * IMQ_HOME/lib/images/admin
     * Defaults to openmq splash.
     */
    private static String imageFileNames[] = {
	    "AppIcon48x.gif",		/* DESKTOP_ICON */

	    "splash_openmq.gif",	/* SPLASH_SCREEN */

	    "folder.gif",		/* DEFAULT_FOLDER */
	    "dot.gif",			/* DEFAULT_LEAF */
	    "CollectionOfObjectStores16x.gif",		/* OBJSTORE_LIST */
	    "ObjectStore16x.gif",	/* OBJSTORE */
	    "ObjectStoreCFDestination16xList.gif",		/* OBJSTORE_DEST_LIST */
	    "ObjectStoreCFDestination16xList.gif",		/* OBJSTORE_CONN_FAC_LIST */
	    "CollectionOfBrokers16x.gif",		/* BROKER_LIST */
	    "Broker16X.gif",	/* BROKER */
	    "ServiceList16x.gif",	/* BROKER_SERVICE_LIST */
	    "dot.gif",			/* BROKER_SERVICE */
	    "BrokerDestinationList16x.gif",		/* BROKER_DEST_LIST */
	    "dot.gif",			/* BROKER_TOPIC */
	    "folder.gif",		/* BROKER_QUEUE_LIST */
	    "dot.gif",			/* BROKER_QUEUE */
	    "folder.gif",		/* BROKER_LOG_LIST */
	    "dot.gif",			/* BROKER_LOG */

	    "ObjectStoreX16X.gif",	/* OBJSTORE_DISCONNECTED */
	    "BrokerX16X.gif",	/* BROKER_DISCONNECTED */

	    "Add24.gif",		/* ADD */
	    "Delete24.gif",		/* DELETE */
	    "Preferences24.gif",	/* PREFERENCES */
	    "Pause24.gif",		/* PAUSE */
	    "Play24.gif",		/* RESUME */
	    "Properties24.gif",		/* PROPERTIES */
	    "Refresh24.gif",		/* REFRESH */
	    "Restart24x.gif",	/* RESTART */
	    "Shutdown24x.gif",	/* SHUTDOWN */
	    "ExpandAll24x.gif",	/* EXPAND_ALL */
	    "CollapseAll24x.gif",	/* COLLAPSE_ALL */
	    "AdminConnectToObjectStore24x.gif",	/* CONNECT_TO_OBJSTORE */
	    "AdminConnectBroker24x.gif",	/* CONNECT_TO_BROKER */
	    "AdminDisConnectToObjectStore24x.gif",	/* DISCONNECT_FROM_OBJSTORE */
	    "AdminDisConnectBroker24x.gif",	/* DISCONNECT_FROM_BROKER */
	    "Purge24x.gif",				/* PURGE */
	    "BrokerQuery24X.gif",			/* QUERY_BROKER */
	    "AboutBox48x.gif"				/* ABOUT_BOX */
    };

    /*
     * Indices for images
     */

    /*
     * Desktop icon
     */
    public final static int DESKTOP_ICON		= 0;

    /*
     * Splash screen
     */
    public final static int SPLASH_SCREEN		= 1;

    /*
     * Explorer pane tree icons
     */
    public final static int DEFAULT_FOLDER		= 2;
    public final static int DEFAULT_LEAF		= 3;
    public final static int OBJSTORE_LIST		= 4;
    public final static int OBJSTORE			= 5;
    public final static int OBJSTORE_DEST_LIST		= 6;
    public final static int OBJSTORE_DEST		= DEFAULT_LEAF;
    public final static int OBJSTORE_CONN_FAC_LIST	= 7;
    public final static int OBJSTORE_CONN_FAC		= DEFAULT_LEAF;
    public final static int BROKER_LIST			= 8;
    public final static int BROKER			= 9;
    public final static int BROKER_SERVICE_LIST		= 10;
    public final static int BROKER_SERVICE		= 11;
    public final static int BROKER_DEST_LIST		= 12;
    public final static int BROKER_DEST			= 13;
    public final static int BROKER_LOG_LIST		= 14;
    public final static int BROKER_LOG			= DEFAULT_LEAF;

    /*
     * Disconnected server icons
     */
    public final static int OBJSTORE_DISCONNECTED	= 18;
    public final static int BROKER_DISCONNECTED		= 19;

    /*
     * Toolbar/menu icons
     */
    public final static int ADD				= 20;
    public final static int DELETE			= 21;
    public final static int PREFERENCES			= 22;
    public final static int PAUSE			= 23;
    public final static int RESUME			= 24;
    public final static int PROPERTIES			= 25;
    public final static int REFRESH			= 26;
    public final static int RESTART			= 27;
    public final static int SHUTDOWN			= 28;
    public final static int EXPAND_ALL			= 29;
    public final static int COLLAPSE_ALL		= 30;
    public final static int CONNECT_TO_OBJSTORE		= 31;
    public final static int CONNECT_TO_BROKER		= 32;
    public final static int DISCONNECT_FROM_OBJSTORE	= 33;
    public final static int DISCONNECT_FROM_BROKER	= 34;
    public final static int PURGE			= 35;
    public final static int QUERY_BROKER		= 36;
    public final static int ABOUT_BOX			= 37;




    public static ImageIcon	adminImages[];

    private static boolean	imagesLoaded = false;

    public static void loadImages() {
	int		imgTotal;
	String		imgRoot;
	Version		version;

	if (imagesLoaded)
	    return;

	imgTotal = imageFileNames.length;
	adminImages = new ImageIcon [ imgTotal ];
	version = new Version(false);

	/*
	System.out.println("Loading Images...");
	*/

	/*
         * File names are relative to
         * IMQ_HOME/lib/images/admin
	 */
	imgRoot = Globals.JMQ_LIB_HOME
			+ File.separator 
			+ "images"
			+ File.separator 
			+ "admin";

	/*
	 * When loading splash screen, check if commercial product
	 * and load splash_comm instead of default splash_openmq.
    	 */
	for (int i = 0; i < imgTotal; ++i)  {
	    String fileName;

	    if (i == SPLASH_SCREEN && version.isCommercialProduct()) {
	      fileName = imgRoot
			  + File.separator
			  + "splash_comm.gif";
	    } else {
	      fileName = imgRoot
			  + File.separator
			  + imageFileNames[i];
 	    }

	    /*
            System.err.println("loading: " + fileName);
	     */
	    
	    adminImages[i] = new ImageIcon(fileName);
	}

	/*
	System.out.println("  - Images loaded");
	*/
	imagesLoaded = true;
    }
}
