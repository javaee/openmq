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
 * @(#)SQLParserConstants.java	1.4 06/28/07
 */ 

package com.sun.messaging.jmq.jmsselector;

public interface SQLParserConstants {

  int EOF = 0;
  int AND = 6;
  int BETWEEN = 7;
  int IN = 8;
  int IS = 9;
  int LIKE = 10;
  int NOT = 11;
  int NULL = 12;
  int OR = 13;
  int ESCAPE = 14;
  int INTEGER_LITERAL = 15;
  int FLOATING_POINT_LITERAL = 16;
  int EXPONENT = 17;
  int STRING_LITERAL = 18;
  int BOOLEAN_LITERAL = 19;
  int ID = 20;
  int LETTER = 21;
  int DIGIT = 22;
  int LESS = 23;
  int LESSEQUAL = 24;
  int GREATER = 25;
  int GREATEREQUAL = 26;
  int EQUAL = 27;
  int NOTEQUAL = 28;
  int OPENPAREN = 29;
  int CLOSEPAREN = 30;
  int ASTERISK = 31;
  int SLASH = 32;
  int PLUS = 33;
  int MINUS = 34;
  int QUESTIONMARK = 35;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\t\"",
    "\"\\f\"",
    "\"and\"",
    "\"between\"",
    "\"in\"",
    "\"is\"",
    "\"like\"",
    "\"not\"",
    "\"null\"",
    "\"or\"",
    "\"escape\"",
    "<INTEGER_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<BOOLEAN_LITERAL>",
    "<ID>",
    "<LETTER>",
    "<DIGIT>",
    "\"<\"",
    "\"<=\"",
    "\">\"",
    "\">=\"",
    "\"=\"",
    "\"<>\"",
    "\"(\"",
    "\")\"",
    "\"*\"",
    "\"/\"",
    "\"+\"",
    "\"-\"",
    "\"?\"",
    "\",\"",
  };

}
