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

/*
 * @(#)FileUtil.java	1.5 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * A class which encapsulates some common File and Directory operations.
 */
public class FileUtil  { 

    // not to be instantiated
    FileUtil() {
    }

    /**
     * Recursively remove all files and directories under and
     * including path depending on the value of removeTopDir.
     * If path is a directory and removeTopDir is true, the top directory
     * will be removed as well, otherwise the top directory will not
     * be removed.
     * If path is a file, it will be removed regardless of the value of
     * removeTopDir.
     *
     * @param path      File or directory to be removed.
     * @param removeTopDir      If true, the top directory will be removed.
     */
    public static void removeFiles(File path, boolean removeTopDir)
	throws IOException {

	if (!path.exists()) return;

	if (!path.isDirectory()) {
	    // delete the file
	    if (!path.delete()) {
		throw new IOException("failed to delete "+path);
	    }
	} else {
	    String[] files = path.list();
	    if (files != null) {
		for (int i = 0; i < files.length; i++) {
		    removeFiles(new File(path, files[i]), true);
		}
	    }

	    // remove the directory
	    if (removeTopDir && !path.delete()) {
		throw new IOException("failed to delete "+path);
	    }
	}
    }

    /**
     * Returns the absolute canonical path to the file/dir of the path passed in.
     * If for whatever reason, this cannot be determined, the path that is passed
     * in is returned.
     *
     * @param path      Path of file/directory
     * @return 		Canonical version of path param or the path param
     *			itself if the canonical path cannot be determined.
     */
    public static String getCanonicalPath(String path)  {
	File f = new File(path);

	try  {
	    return (f.getCanonicalPath());
	} catch (Exception e)  {
	    return (path);
	}
    }

    /**
     * Copies all files under srcDir to dstDir.
     * If dstDir does not exist, it will be created.
     */
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                              new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    /**
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     */
    public static void copyFile(File src, File dst) throws IOException {
        // Create channel on the source & destination
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();

        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        // Close the channels
        srcChannel.close();
        dstChannel.close();
    }

/* LKS
    public static void main(String[] args) throws Exception {

	String filename = null;
	if (args.length > 1)  {
	    if (args[0].equalsIgnoreCase("-rmdir"))  {
		filename = args[1];
	    }
	}

	if (filename != null) {
	    FileUtil.removeFiles(new File(filename), true);
	}
    }
*/

}
