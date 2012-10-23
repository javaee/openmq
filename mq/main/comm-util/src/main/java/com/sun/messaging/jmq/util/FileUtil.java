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

    public static void main(String args[]) {
try {
        System.out.println("obfuscating file");
        obfuscateFile("myfile", "myfile2");
        System.out.println("deobfuscating file");
        deobfuscateFile("myfile2", "myfile3");
        System.out.println("Reading normal file");
        InputStream is = retrieveObfuscatedFile("myfile");
        FileWriter fw = new FileWriter("myfile4");
        java.util.Properties prop1 = new java.util.Properties();
        prop1.load(is);
        fw.close();
        is.close();
        System.out.println(prop1);

        System.out.println("Reading modified file");
        is = retrieveObfuscatedFile("myfile2");
        java.util.Properties prop2 = new java.util.Properties();
        prop2.load(is);
        System.out.println(prop2);
        System.out.println("DONE");
        fw.close();
        is.close();

} catch (IOException ex) {
ex.printStackTrace();
}
    }

    private static String magicNumberString="%947634%";
    private static String mqversion="4.2";
    private static String formatversion="1.0";

    //private static char fseq[] = {0,1,1,2,3,5,8,13,21,34,55,89,144,233};
    private static char fseq[] = {0,1,1,2,3,5,8};

    // OK, all obfuscated files will start with a single line with a magic number
    public static void obfuscateFile(String src, String dest) throws IOException{

         // read in file and calculate magic number
         // we use this because its really more set up to read chunks of data
         // and I can preallocate the buffer (because it has available());
         FileInputStream fis = new FileInputStream(src);

         byte[] currentBuffer = new byte[fis.available()];
         int bread= fis.read(currentBuffer);
         byte[] hashbytes = MD5.getHash(currentBuffer);
         String hash = MD5.convertToString(hashbytes);
         fis.close();

         // ok switch to using a reader which handles characters right
         FileReader fr = new FileReader(src);
         BufferedReader br = new BufferedReader(fr);
         FileWriter fw = new FileWriter(dest);
         BufferedWriter bw = new BufferedWriter(fw);

         //Ok, first write the magic number/version to the file
         bw.write(magicNumberString + ":" + hash + ":" + formatversion + ":" + mqversion);
         bw.newLine();

         //LKW - first go around just read and write

         int linecnt = 0;
         while (true) {
             String line = br.readLine();
             if (line == null) break;

             // OK, first get the character
             char c[] = line.toCharArray();

             //int pos = (linecnt + 3)%fseq.length;
             int pos = linecnt%fseq.length;

             String newstr = "";
             for (int i = 0; i < c.length ; i++) {
                 // work backwards
                 if (pos >= fseq.length) pos= 0;
                 newstr += (char)(c[i] ^ fseq[pos]);
                 pos ++;
             }

             bw.write(newstr);

             // now reverse bytes
             bw.newLine();
             linecnt ++;
         }


         // close everything
         bw.close();
         fw.close();
         br.close();
         fr.close();


    }

    public static void deobfuscateFile(String src, String dest) throws IOException {

         FileReader fr = new FileReader(src);
         BufferedReader br = new BufferedReader(fr);

         // verify line has the right magic number
         String top = br.readLine();
         int linecnt = 0;
         if (!top.startsWith(magicNumberString)) {
              fr.close();
              br.close();
              throw new IOException("unobfuscated file");
         }

         // OK, parse out the hashcode and version
         int start = top.indexOf(':');
         int end= top.indexOf(':',start+1);
         String hashcode = top.substring(start+1, end);

         FileWriter fw = new FileWriter(dest);
         BufferedWriter bw = new BufferedWriter(fw);

         StringBuffer contents = new StringBuffer();

         linecnt = 0;
         while (true) {
             String line = br.readLine();
             if (line == null) break;

             char[] c = line.toCharArray();

             int pos = linecnt%fseq.length;

             for (int i= 0; i < c.length; i ++) {
                 if (pos >= fseq.length) pos= 0;
                 char newc = (char)(c[i] ^ fseq[pos]);
                 contents.append(newc);
                 pos ++;
              }

                  // read ints
              contents.append('\n');
              linecnt ++;
         }

         String str = contents.toString();
         String newhash = MD5.getHashString(str);

         if (!newhash.equals(hashcode)) {
             throw new IOException("deobfuscateFile:  File corrupted, hashcodes do not match");
         }
         bw.write(str);


         // close everything
         bw.close();
         fw.close();
         br.close();
         fr.close();
    }
    public static boolean isobfuscated(String src) throws IOException {
         FileReader fr = new FileReader(src);
         BufferedReader br = new BufferedReader(fr);
         String top = br.readLine();
         br.close();
         fr.close();
         return top.startsWith(magicNumberString);
    }

    public static InputStream retrieveObfuscatedFile(String src) throws IOException {
          // not the fastest implementation
          // Ok. first load the file
          FileInputStream fis = new FileInputStream(src);

          // read in everything as an array of bytes
          int avail = fis.available();
          byte[] data = new byte[avail];
          fis.read(data);

          // now get a byte array input stream

          ByteArrayInputStream bis = new ByteArrayInputStream(data);
          DataInputStream dis = new DataInputStream(bis);

          String magicline = dis.readLine();
          if (! magicline.startsWith(magicNumberString)) {
              bis.reset();
              fis.close();
              return bis;
          }

          // retrieve hashcode
          int start = magicline.indexOf(':');
          int end= magicline.indexOf(':',start+1);
          String hashcode = magicline.substring(start+1, end);

          StringBuffer contents = new StringBuffer();

          // read in as bytes
           int linecnt = 0;
           while (true) {
               String line = dis.readLine();
               if (line == null) break;
  
               char[] c = line.toCharArray();
  
               int pos = linecnt%fseq.length;
  
               for (int i= 0; i < c.length; i ++) {
                   if (pos >= fseq.length) pos= 0;
                   char newc = (char)(c[i] ^ fseq[pos]);
                   contents.append(newc);
                   pos ++;
                }

                  // read ints
                contents.append('\n');
                linecnt ++;
          }

          String str = contents.toString();
          String newhash = MD5.getHashString(str);

          if (!newhash.equals(hashcode)) {
             throw new IOException("retrieveObfuscatedFile:  File corrupted, hashcodes do not match");
          }

          // ok now write to something
          byte[] bytes = str.getBytes("UTF8");
          ByteArrayInputStream pipeis = new ByteArrayInputStream(bytes);
          bis.close();
          dis.close();
          fis.close();
          return pipeis;
    }



}
