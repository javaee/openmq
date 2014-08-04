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
 * @(#)CacheHashMap.java	1.6 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LinkedHashMap that is bounded by size. Once the HashMap is 
 * full, the oldest entry is discarded as new entries are added.
 */
public class CacheHashMap extends LinkedHashMap {

    private static int DEFAULT_CAPACITY = 16;
    private int capacity = DEFAULT_CAPACITY;

    /**
     * Create a CacheHashMap with a the specified capacity 
     *
     * @param   capacity    Capacity of the CacheHashMap.
     */
    public CacheHashMap(int capacity) {
        super(capacity);
        this.capacity = capacity;
    }

    /**
     * Create a CacheHashMap with a default capacity (16)
     */
    public CacheHashMap() {
        this(DEFAULT_CAPACITY);
    }

    public int capacity() {
	// BugId 6360052
	// Tom Ross
	// 10 Oct 2006

	// old line
	// return capacity();
	//new line
        return capacity;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > capacity;
    }

    public static void main(String args[]) {

        CacheHashMap c = new CacheHashMap(5);

        for (int i = 0; i < 10; i++) {
            c.put(Long.valueOf(i), "a" + i);
        }

        System.out.println(c);
    }
}
