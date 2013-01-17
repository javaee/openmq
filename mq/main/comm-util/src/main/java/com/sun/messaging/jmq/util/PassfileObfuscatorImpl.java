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

package com.sun.messaging.jmq.util;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import com.sun.messaging.jmq.util.BASE64Encoder;
import com.sun.messaging.jmq.util.BASE64Decoder;

public class PassfileObfuscatorImpl implements PassfileObfuscator
{
    private static final String FORMAT_VERSION = "500";
    private static final String MAGIC_NUMBER = "50fec"+FORMAT_VERSION; //passfile encoding
    private static String OBSUFFIX ="{"+Integer.parseInt(MAGIC_NUMBER, 16)+"}"; //1358873856

    /**
     */
    @Override
    public void 
    obfuscateFile(String source, String target, String prefix)
    throws IOException {

         try {

         FileReader fr = new FileReader(source);
         BufferedReader br = new BufferedReader(fr);
         FileWriter fw = new FileWriter(target);
         BufferedWriter bw = new BufferedWriter(fw);

         MessageDigest md = MessageDigest.getInstance("SHA-256");
         BASE64Encoder encoder = new BASE64Encoder();

         String line = null;
         while ((line = br.readLine()) != null) {
             if (line.startsWith(prefix)) {
                 int ind = line.indexOf("=");
                 if (ind > 0 && line.length() > (ind+1)) {
                     String key = line.substring(0, ind);
                     if (!key.trim().endsWith(OBSUFFIX)) {
                         String pw = line.substring(ind+1);
                         String epw = encoder.encode(pw.getBytes("UTF8"));
                         byte[] hashbytes = md.digest(pw.getBytes("UTF8"));
                         String hashstr = new BigInteger(hashbytes).toString(16);
                         bw.write(key+OBSUFFIX+"="+hashstr+":"+epw);
                         bw.newLine();
                         md.reset();
                         continue;
                     }
                 }
             }
             bw.write(line);
             bw.newLine();
         }

         bw.close();
         fw.close();
         br.close();
         fr.close();

         } catch (IOException e) {
         throw e; 
         } catch (Exception ee) {
         throw new IOException(ee.toString(), ee);
         }
    }


    @Override
    public void 
    deobfuscateFile(String source, String target, String prefix)
    throws IOException {
        deobfuscateFile(source, target, prefix, false);
    }

    private StringBuffer 
    deobfuscateFile(String source, String target, String prefix, boolean returnContentOnly)
    throws IOException {

         try {

         FileReader fr = new FileReader(source);
         BufferedReader br = new BufferedReader(fr);

         MessageDigest md = MessageDigest.getInstance("SHA-256");
         BASE64Decoder decoder = new BASE64Decoder();

         StringBuffer contents = new StringBuffer();

         String line = null;
         while ((line = br.readLine()) != null) {
             if (line.startsWith(prefix)) {
                 int ind = line.indexOf("=");
                 if (ind > 0 && line.length() > (ind+1)) {
                     String key = line.substring(0, ind);
                     if (key.trim().endsWith(OBSUFFIX)) {
                         String hashepw = line.substring(ind+1);
                         int ind2 = hashepw.indexOf(":");
                         if (ind2 > 0 && hashepw.length() > (ind2+1)) {
                             String epw = hashepw.substring(ind2+1);
                             String hashstr = hashepw.substring(0, ind2);
                             byte[] hashbytes = decoder.decodeBuffer(epw);
                             String pw = new String(hashbytes, "UTF8");
                             byte[] hb = md.digest(hashbytes);
                             String hs = new BigInteger(hb).toString(16);
                             md.reset();
                             if (!hashstr.equals(hs)) {
                                 throw new IOException("Password corrupted: hash code not match for "+line);
                             }
                             contents.append(key.substring(0, key.lastIndexOf(OBSUFFIX))+"="+pw);
                             contents.append(System.getProperty("line.separator"));
                             continue;
                         } else {
                             throw new IOException("Corrupted line: "+line);
                         }
                     }
                 }
                   
             }
             contents.append(line);
             contents.append(System.getProperty("line.separator"));
         }


         if (returnContentOnly) {
             br.close();
             fr.close();
             return contents;
         }

         FileWriter fw = new FileWriter(target);
         BufferedWriter bw = new BufferedWriter(fw);
         String str = contents.toString();
         bw.write(str);

         bw.close();
         fw.close();
         br.close();
         fr.close();

         return null;

         } catch (IOException e) {
         throw e;
         } catch (Exception ee) {
         throw new IOException(ee.toString(), ee);
         }
    }

    @Override
    public InputStream 
    retrieveObfuscatedFile(String source, String prefix)
    throws IOException {

          StringBuffer contents = deobfuscateFile(source, null, prefix, true);

          byte[] bytes = contents.toString().getBytes("UTF8");
          ByteArrayInputStream pipeis = new ByteArrayInputStream(bytes);
          return pipeis;
    }

    public static void main(String args[]) {
        try {
        PassfileObfuscator po = new PassfileObfuscatorImpl();
        System.out.println("obfuscating file");
        po.obfuscateFile("myfile", "myfile2", "imq");
        System.out.println("deobfuscating file");
        po.deobfuscateFile("myfile2", "myfile3", "imq");
        System.out.println("Reading normal file");
        InputStream is = po.retrieveObfuscatedFile("myfile", "imq");
        FileWriter fw = new FileWriter("myfile4");
        java.util.Properties prop1 = new java.util.Properties();
        prop1.load(is);
        fw.close();
        is.close();
        System.out.println(prop1);

        System.out.println("Reading modified file");
        is = po.retrieveObfuscatedFile("myfile2", "imq");
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
}

