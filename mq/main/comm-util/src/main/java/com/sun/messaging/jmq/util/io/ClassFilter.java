/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.messaging.jmq.util.io;

import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * a Class filter utility which is utilized as a mechanism
 * which hooks into resolveClass, to prevent any black listed classes or packages from loading
 * By Default the following packages are black listed:
 *
 *  @code org.apache.commons.collections.functors
 *  @code com.sun.org.apache.xalan.internal.xsltc.trax
 *  @code javassist
 *
 *  The black list mechanism may be globally disabled by setting @code com.sun.messaging.io.disableblacklist to true
 *  The above defaults may be disabled by setting @code com.sun.messaging.io.disabledefaultblacklist to true
 *
 *  Alternatively, the blacklist mechanism maybe modified by setting the black list property as follows :
 *
 *  To add elements to the list :
 *
 *  com.sun.messaging.io.blacklist=+org.apache.commons.collections.functors,+com.sun.org.apache.xalan.internal.xsltc.trax,+javassist
 *
 *  To remove elements from the blacklist :
 *
 *  com.sun.messaging.io.blacklist=-org.apache.commons.collections.functors,-javassist
 *
 *  It's important to note that blacklisting a package overrides any class exclusion.
 *
 *
 */
abstract public class ClassFilter {
  static final String BLACK_LIST_PROPERTY = "com.sun.messaging.io.blacklist";
  static final String DISABLE_DEFAULT_BLACKLIST_PROPERTY = "com.sun.messaging.io.disabledefaultblacklist";
  static final String DISABLE_BLACK_LIST_PROPERTY = "com.sun.messaging.io.disableblacklist";

  private static final String DEFAULT_BLACK_LIST = "+org.apache.commons.collections.functors," +
    "+com.sun.org.apache.xalan.internal.xsltc.trax," +
    "+javassist," +
    "+org.codehaus.groovy.runtime.ConvertedClosure," +
    "+org.codehaus.groovy.runtime.ConversionHandler," +
    "+org.codehaus.groovy.runtime.MethodClosure";

  private static final HashSet<String> BLACK_LIST = new HashSet<>(32);


  static {
    if (!isBlackListDisabled()) {
      if (!isDefaultBlacklistEntriesDisabled()) updateBlackList(DEFAULT_BLACK_LIST);
      updateBlackList(System.getProperty(BLACK_LIST_PROPERTY, null));
    }
  }

  private static boolean isBlackListDisabled() {
    return Boolean.getBoolean(DISABLE_BLACK_LIST_PROPERTY);
  }

  private static boolean isDefaultBlacklistEntriesDisabled() {
    return Boolean.getBoolean(DISABLE_DEFAULT_BLACKLIST_PROPERTY);
  }

  private static void updateBlackList(String blackList) {
    if (blackList != null) {
      StringTokenizer st = new StringTokenizer(blackList, ",");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        processToken(token);
      }
    }
  }

  private static void processToken(String token) {
    if (token.startsWith("+"))
      BLACK_LIST.add(token.substring(1));
    else if (token.startsWith("-"))
      BLACK_LIST.remove(token.substring(1));
    else // no operand specified: first character must be part of class name
      BLACK_LIST.add(token);
  }

  /**
   * Returns true if the named class, or its package is black listed
   * @param className the class name to check
   * @return true if black listed
   */
  public static boolean isBlackListed(String className) {
    if (!className.isEmpty() && BLACK_LIST.contains(className)) {
      return true;
    }
    String pkgName;
    try {
      pkgName = className.substring(0, className.lastIndexOf('.'));
    } catch (Exception ignored) {
      return false;
    }
    return !pkgName.isEmpty() && BLACK_LIST.contains(pkgName);
  }

}
