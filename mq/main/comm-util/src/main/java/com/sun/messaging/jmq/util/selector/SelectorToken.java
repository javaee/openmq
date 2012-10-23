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
 * @(#)SelectorToken.java	1.7 07/06/07
 */ 

package com.sun.messaging.jmq.util.selector;

/**
 * Immutable class that represents a token. A token consists of two
 * parts. And integer that defines the token, and an optional value
 * that defines an associated value. For example a LONG token has
 * an associated value that is the value of the long it represents.
 */
class SelectorToken {

    // Pre-allocate TRUE, FALSE and UNKNOWN tokens since these are used
    // constantly during evaluation.
    static final SelectorToken trueToken  =
                            new SelectorToken(Selector.TRUE, "true");
    static final SelectorToken falseToken =
                            new SelectorToken(Selector.FALSE, "false");
    static final SelectorToken unknownToken =
                            new SelectorToken(Selector.UNKNOWN, "unknown");
    
    // Pre-allocate a couple other tokens that commonly appear in expressions.
    // Note that LTE and GTE are used to evaluate BETWEEN so it's important
    // to have them in here.
    static final SelectorToken equalsToken  =
                            new SelectorToken(Selector.EQUALS, "=");
    static final SelectorToken notEqualsToken =
                            new SelectorToken(Selector.NOT_EQUALS, "<>");
    static final SelectorToken gtToken =
                            new SelectorToken(Selector.GT, ">");
    static final SelectorToken gteToken =
                            new SelectorToken(Selector.GTE, ">=");
    static final SelectorToken ltToken =
                            new SelectorToken(Selector.LT, "<");
    static final SelectorToken lteToken =
                            new SelectorToken(Selector.LTE, "<=");

    // Pre-allocate marker tokens
    static final SelectorToken andMarker =
                            new SelectorToken(Selector.AND_MARKER, "&");
    static final SelectorToken orMarker =
                            new SelectorToken(Selector.OR_MARKER, "|");

    // What this token is.
    int token = Selector.UNKNOWN;

    // Some tokens have an associated value. For example:
    // ESCAPE has an escape character.
    // IDENTIFIER has the identifier String
    // STRING     has the String value
    // DOUBLE      has the Float value
    Object value = null;

    public static SelectorToken getInstance(int token, Object value) {

        switch (token) {

        case Selector.TRUE:
            return trueToken;
        case Selector.FALSE:
            return falseToken;
        case Selector.UNKNOWN:
            return unknownToken;
        case Selector.EQUALS:
            return equalsToken;
        case Selector.GTE:
            return gteToken;
        case Selector.LTE:
            return lteToken;
        case Selector.GT:
            return gtToken;
        case Selector.LT:
            return ltToken;
        case Selector.NOT_EQUALS:
            return notEqualsToken;
        case Selector.AND_MARKER:
            return andMarker;
        case Selector.OR_MARKER:
            return orMarker;
        default:
            return new SelectorToken(token, value);
        }
    }

    public static SelectorToken getInstance(int token) {
        return getInstance(token, null);
    }

    private SelectorToken(int token) {
        this.token = token;
    }

    private SelectorToken(int token, Object value) {
        this.token = token;
        this.value = value;
    }

    public int getToken() {
        return token;
    }

    public Object getValue() {
        return value;
    }

    public boolean equals(Object o) {

        if (this == o) return true;

        if (!(o instanceof SelectorToken)) {
            return false;
        }

        SelectorToken obj = (SelectorToken)o;

        if (obj.token != token) {
            return false;
        }

        return (value == null ? obj.value == null : value.equals(obj.value));
    }

    public int hashCode() {

        if (value == null) {
            return token;
        } else {
            return value.hashCode() * token;
        }
    }

    public String toString() {
        return ("[" + token + "," +
                (value == null ? "null" : value.toString()) + "]");
    }
}
