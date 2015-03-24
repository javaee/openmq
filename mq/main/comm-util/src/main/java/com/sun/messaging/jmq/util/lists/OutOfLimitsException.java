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
 * @(#)OutOfLimitsException.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

import java.util.*;
import java.io.*;

/**
 * this class is the exception which is thrown when a limit
 * on a limitable set/map is exceeded. It contains specifics
 * about the event that occurred (which can be used to localize
 * at higher levels)
 */


public class OutOfLimitsException extends IndexOutOfBoundsException
{
    public static final int CAPACITY_EXCEEDED = 0;
    public static final int BYTE_CAPACITY_EXCEEDED = 1;
    public static final int ITEM_SIZE_EXCEEDED = 2;
    public static final int PRIORITY_EXCEEDED = 3;

    Object limit = null;
    Object value = null;
    int type = -1;

    public OutOfLimitsException(int type, Object actual,
         Object limit) 
    {
        super(composeString(type,actual, limit));
        this.type = type;
        this.limit = limit;
        this.value = actual;
    }

    public Object getValue() {  
        return value;
    }

    public Object getLimit() {
        return limit;
    }

    public int getType() {
        return type;
    }

    public static final String toString(int type) {
        switch (type) {
              case CAPACITY_EXCEEDED:
                  return "Capacity Exceeded";
              case BYTE_CAPACITY_EXCEEDED:
                  return "Byte Capacity Exceeded";
              case ITEM_SIZE_EXCEEDED:
                  return "Item Size Exceeded";
              case PRIORITY_EXCEEDED:
                  return "Priority Exceeded";
              default:
                  return "Unknown Error";
       }
    }

    public static final String composeString(int type,
           Object actual, Object limit) {
         return toString(type) + " was " + actual
             + " expected " + limit;
    }

}
