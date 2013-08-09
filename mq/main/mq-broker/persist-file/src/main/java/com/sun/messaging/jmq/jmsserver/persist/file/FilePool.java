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
 * @(#)FilePool.java	1.10 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.file;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.resources.*;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;

import java.io.*;
import java.util.*;

/**
 * A file pool. The pool has three sub pools:
 *   L1     The fastest. When files are returned to the pool
 *          they are simply marked as free. This is fast, but
 *          the files must be cleaned up at shutdown.
 *   L2     Slower. When files are returned to this pool they
 *          are truncated. This is slower, but files do not need
 *          to be cleaned up at shutdown.
 *   L3     Slowest. When files are returned to the pool they are
 *          deleted. Keeps us from leaving an unbounded number of
 *          files around, but deletion is expensive and we get no
 *          reuse. We track these just to preserve file names.
 */
public class FilePool {

    public static final int POOL_UNASSIGNED = 0;
    public static final int POOL_L1 = 1;
    public static final int POOL_L2 = 2;
    public static final int POOL_L3 = 3;

    /* Number of files we have assigned to each pool */
    int L1allocated = 0;
    int L2allocated = 0;
    int L3allocated = 0;

    /* The three subpools. L3 has unbounded capacity */
    int L1capacity = 0;
    ArrayList L1pool = null;

    int L2capacity = 0;
    ArrayList L2pool = null;

    ArrayList L3pool = null;

    /**
     * Hashtable of files we have handed out. We needed this so that
     * when a file is returned we know which pool to return it to
     */
    Hashtable active = null;

    /* We get the file names (numbers) from here */
    RandomAccessStore store;

    Logger logger = Globals.getLogger();

    /**
     * Create a file pool with the specified L1 and L2 capacity 
     */
    FilePool(RandomAccessStore store, int L1capacity, int L2capacity) {

        this.L1capacity = L1capacity;
        this.L1pool = new ArrayList(this.L1capacity);

        this.L2capacity = L2capacity;
        this.L2pool = new ArrayList(this.L2capacity);

        this.L3pool = new ArrayList();

        this.store = store;

        this.active = new Hashtable(this.L1capacity);

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUG, this.getClass().getName() +
                ": Created new file pool: L1capacity=" + L1capacity +
                ", L2capacity=" + L2capacity);
        }
    }

    /**
     * Get a file from the pool. Try L1, L2 then L3 in that order.
     * If there are no free files in any pool then we allocate a new one.
     */
    synchronized File getFile() {

        File file = null;
        FilePoolEntry fpe = null;
        Enumeration e = null;

        // Try to grab an unused file from a pool.
        if (!L1pool.isEmpty()) {
            fpe = (FilePoolEntry)L1pool.remove(L1pool.size() - 1);
        } else if (!L2pool.isEmpty()) {
            fpe = (FilePoolEntry)L2pool.remove(L2pool.size() - 1);
        } else if (!L3pool.isEmpty()) {
            fpe = (FilePoolEntry)L3pool.remove(L3pool.size() - 1);
        } else {
            // All file pools are empty. Need to create a new file.
	    int num = ((store.low > store.LOWEST_FILE_NUM) ?
			(--store.low) : (store.high++));

	    if (Store.getDEBUG()) {
	        logger.log(logger.DEBUGHIGH,
				"new number used for file name: " + num);
	    }
	    file = (new File(store.directory, String.valueOf(num)));
            fpe = new FilePoolEntry(file);
            assignEntryToPool(fpe);
        }

        // Put it in active list, so when it is free'd later we can
        // return it to the proper bool.
        active.put(fpe.getFile(), fpe);
        return fpe.getFile();
    }

    /**
     * Assign a newly created entry to a pool. We attempt to assign
     * it to L1 first, then L2 and then final L3.
     * 
     */
    private void assignEntryToPool(FilePoolEntry fpe) {
        // Find a pool to put it in
        if (L1allocated < L1capacity) {
            L1allocated++;
            fpe.setPool(POOL_L1);
        } else if (L2allocated < L2capacity) {
            L2allocated++;
            fpe.setPool(POOL_L2);
        } else {
            L3allocated++;
            fpe.setPool(POOL_L3);
        }
    }

    /**
     * Return a file to the pool.
     */
    synchronized void putFile(File file, boolean sync) {

        // Remove it from the active hashtable 
        FilePoolEntry fpe = (FilePoolEntry)active.remove(file);
        RandomAccessFile rfile = null;

        if (fpe == null) {
            // This file was never handed out. It is probably
            // an empty file being loaded at startup.
            fpe = new FilePoolEntry(file);
            assignEntryToPool(fpe);
        }

        // Return it to the proper sub-pool
        switch (fpe.getPool()) {
        case POOL_L1:
            // Mark file as free
            L1pool.add(fpe);
            try {
                // Only need to mark non-zero length files
                if (file.length() > 0) {
                    rfile = new RandomAccessFile(file, "rw");
                    rfile.writeUTF(store.FREE_FILE);

		    if (sync) {
		    	if(Store.getDEBUG_SYNC())
				{
					String msg = "FilePool putFile sync() "+file;
					logger.log(Logger.DEBUG,msg);
				}
			// bug 5042763:
			// don't sync meta data for performance reason
			rfile.getChannel().force(false);
		    }

                    rfile.close();
                }
            } catch (IOException e) {
                // can't mark it FREE; delete it
                file.delete();
                if (Store.getDEBUG())
                    logger.log(logger.DEBUG,
                            "Failed to tag free file " + file, e);
            }
            break;
        case POOL_L2:
            L2pool.add(fpe);
            // Truncate file
            try {
                rfile = new RandomAccessFile(file, "rw");
                rfile.setLength(0);

		if (sync) {
			if(Store.getDEBUG_SYNC())
			{
				String msg = "FilePool putFile sync() "+file;
				logger.log(Logger.DEBUG,msg);
			}
		    // bug 5042763:
		    // don't sync meta data for performance reason
		    rfile.getChannel().force(false);
		}

                rfile.close();
            } catch (IOException e) {
                // can't mark it FREE; delete it
                file.delete();
                if (Store.getDEBUG())
                    logger.log(logger.DEBUG,
                            "Failed to truncate free file " + file, e);
            }
            break;
        case POOL_L3:
            L3pool.add(fpe);
            // Delete file
            if (!file.delete()) {
                if (Store.getDEBUG())
                    logger.log(logger.DEBUG, "Failed to delete file " +file);
                }
            break;
        default:
            break;
        }
    }

    void clear() {
        L1pool.clear();
        L2pool.clear();
        L3pool.clear();
        L1allocated = 0;
        L2allocated = 0;
        L3allocated = 0;
        active.clear();
    }

    /**
     * Close and cleanup all free files in the file pool.
     */
    public void close(boolean cleanup) {
        if (Store.getDEBUG()) {
		logger.log(logger.DEBUG,
			"FilePool.close() called; cleanup = " + cleanup);
        }

	if (!cleanup)
	    return;

        Iterator iter = L1pool.iterator();
        FilePoolEntry fpe = null;
        File file = null;
        RandomAccessFile rfile = null;

        // Truncate all files in L1 pool. L2 pool files are already truncated.
        // L3 pool files are already deleted!
        while (iter.hasNext()) {
            fpe = (FilePoolEntry)iter.next();
            file = fpe.getFile();
            if (file != null && file.length() != 0) {
                try {
                    rfile = new RandomAccessFile(file, "rw");
                    rfile.setLength(0);
                    rfile.close();
                } catch (IOException e) {
                    logger.log(logger.DEBUG, "Could not truncate file: " +
                            file, e);
                }
            }
        }
    }

    public String toString() {
        return (
        "L1 capacity=" + L1capacity +
            "  allocated=" + L1allocated + "  free=" + L1pool.size() + "\n" +
        "L2 capacity=" + L2capacity +
            "  allocated=" + L2allocated + "  free=" + L2pool.size() + "\n" +
        "L3 capacity=unlimited"  +
            "  allocated=" + L3allocated + "  free=" + L3pool.size());
    }

    void printFileInfo(PrintStream out) {
        out.println(toString());
    }

    void printStatistics() {
	// print statistics
	if (Store.getDEBUG()) {
		logger.log(logger.DEBUG, toString());
	}
    }

    public int getNumFreeFiles() {
        return L1pool.size() + L2pool.size();
    }

    public void dumpPool(PrintStream out) {

        out.println(this.getClass().getName() + ": ");
        out.println(this.toString());
        
        out.print(">>>L1: ");
        Iterator iter = L1pool.iterator();
        while (iter.hasNext()) {
            FilePoolEntry fpe = (FilePoolEntry)iter.next();
            File file = fpe.getFile();
            out.print(file.getName() + ",");
        }

        out.print("\n>>>L2: ");
        iter = L2pool.iterator();
        while (iter.hasNext()) {
            FilePoolEntry fpe = (FilePoolEntry)iter.next();
            File file = fpe.getFile();
            out.print(file.getName() + ",");
        }

        out.print("\n>>>L3: ");
        iter = L3pool.iterator();
        while (iter.hasNext()) {
            FilePoolEntry fpe = (FilePoolEntry)iter.next();
            File file = fpe.getFile();
            out.print(file.getName() + ",");
        }
        out.print("\n");
    }

/**
 * An entry in a file pool. Consists of the file, and the pool it
 * has been assigned to.
 */
public static class FilePoolEntry {

    private File file = null;
    private int pool = POOL_UNASSIGNED;

    FilePoolEntry(File file) {
        this.file = file;
        this.pool = POOL_UNASSIGNED;
    }

    public void setPool(int pool) {
        this.pool = pool;
    }

    public int getPool() {
        return this.pool;
    }

    public File getFile() {
        return this.file;
    }
}

}
