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
 * @(#)RegularExpression.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util.selector;

/**
 * A simple RegularExpression handler to handle the JMS Selector "LIKE"
 * operation.
 * 
 */
public class RegularExpression {

    String  expression = null;
    Character    escape     = null;

    public RegularExpression(String expression, String escape) {
        this.expression = expression;

        if (escape != null) {
            this.escape = Character.valueOf(escape.charAt(0));
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof RegularExpression)) {
            return false;
        }
        RegularExpression obj = (RegularExpression)o;
        return (expression.equals(obj.expression) && 
                escape.equals(obj.escape));
    }

    public String toString() {
        return ("{re=" + expression + ", esc=" + escape + "}");
    }

    public String getExpression() {
        return expression;
    }

    public Character getEscape() {
        return escape;
    }

    public int hashCode() {
        return expression.hashCode();
    }

    public boolean match(String string) {
        return match(expression, 0, string, 0);
    }

    private boolean match(String re, int reStart, String value, int valStart) {

        int reLen = re.length();
        int vLen  = value.length();

        int i = reStart;
        int j = valStart;

        char esc = 0;
        if (escape != null) {
            esc = escape.charValue();
        }
        char c;

        boolean escaped = false;

        do {

            c = re.charAt(i);

            // Detect escape character
            if (escape != null && c == esc) {
                escaped = true;
                i++;
                continue;
            }

            switch (c) {

            // Match any single character
            case '_':
                if (escaped) {
                    escaped = false;
                    // Just a normal character
                    if (c == value.charAt(j)) {
                        // Two characters match. Move past them
                        i++;
                        j++;
                    } else {
                        // No match
                        return false;
                    }
                } else {
                    // Anything matches. Move on
                    i++;
                    j++;
                }
                break;

            case '%':
                if (escaped) {
                    escaped = false;
                    // Just a normal character
                    if (c == value.charAt(j)) {
                        // Two characters match. Move past them
                        i++;
                        j++;
                    } else {
                        // No match
                        return false;
                    }
                } else {
                    // Wildcard
                    // Skip %
                    i++; 
                    if (i == reLen) {
                        // % was at end of re. By definition we mach the rest
                        // of the string.
                        return true;
                    }
                    do {
                        // Match substring against re starting after %
                        if (match(re, i, value, j)) {
                            return true;
                        }
                        // No match starting here. Skip character in string
                        // and try again.
                        j++;
                    } while (j < vLen);
                    // Ran out of string with no match.
                    return false;
                }
                break;

            default:
                if (c == value.charAt(j)) {
                    // Two characters match. Move past them
                    i++;
                    j++;
                    escaped = false;
                } else {
                    // No match
                    return false;
                }
                break;
            }
        } while (j < vLen && i < reLen);

        // Skip any trailing % since they match 0 or more
        while (i < reLen && re.charAt(i) == '%') {
            i++;
        }

        if (j == vLen && i == reLen) {
            return true;
        } else {
            return false;
        }

    }


public static void main(String args[]) {


    String[][] tests = {
        /* RE        String     Escape   Result */
        {"abcd",    "abcd",     null,   "true"},
        {"a_cd",    "abcd",     null,   "true"},
        {"_bcd",    "abcd",     null,   "true"},
        {"_bc_",    "abcd",     null,   "true"},
        {"____",    "abcd",     null,   "true"},
        {"_X__",    "abcd",     null,   "false"},
        {"a%d",     "abcd",     null,   "true"},
        {"a%d",     "ad",       null,   "true"},
        {"%cd",     "abcd",     null,   "true"},
        {"%cd",     "cd",       null,   "true"},
        {"ab%",     "abcd",     null,   "true"},
        {"ab%",     "ab",       null,   "true"},
        {"ab%%",    "ab",       null,   "true"},
        {"%%ab",    "ab",       null,   "true"},
        {"a%c%",    "abcd",     null,   "true"},
        {"a%%d",    "abcd",     null,   "true"},
        {"%%%%",    "abcd",     null,   "true"},
        {"a%cd",    "acdcdbcd", null,   "true"},
        {"X%cd",    "acdcdbcd", null,   "false"},

        {"a%_cd",   "abcd",     null,   "true"},
        {"a%_cd",   "abbbbcd",  null,   "true"},

        {"a%_%d",   "abbbbcd",  null,   "true"},
        {"a%_%d",   "ad",       null,   "false"},

        {"a~_c",    "abc",      "~",   "false"},
        {"a~_c",    "a_c",      "~",   "true"},
        {"a~%c",    "abc",      "~",   "false"},
        {"a~%c",    "a%c",      "~",   "true"},
        {"a%c",     "abc",      "~",   "true"},
        {"~a%c",    "abc",      "~",   "true"},
    };

    int failCnt = 0;

    for (int n = 0; n < tests.length; n++) {
        RegularExpression re = new RegularExpression(tests[n][0], tests[n][2]);
        boolean expected = Boolean.valueOf(tests[n][3]).booleanValue();
        boolean actual   = re.match(tests[n][1]);
        String result;
        if (actual == expected) {
            result = "      PASS";
        } else {
            result = "***** FAIL";
            failCnt++;
        }
        System.out.println(result + " " + tests[n][0] + "=" + tests[n][1] +
            (tests[n][2] != null ? " (esc=" + tests[n][2] + ")" : "") +
            " : expected=" + expected + " actual=" + actual + " ");
    }

    System.out.println (tests.length + " tests: " + (tests.length - failCnt) +
        " passed " + failCnt + " failed ");

    if (failCnt > 0) {
        System.exit(1);
    } else {
        System.exit(0);
    }

}

}
