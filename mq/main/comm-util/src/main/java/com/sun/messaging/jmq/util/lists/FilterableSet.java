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
 * @(#)FilterableSet.java	1.4 06/29/07
 */ 

package com.sun.messaging.jmq.util.lists;

import java.util.*;

/**
 * An interface for sets which allow subsets of objects
 * to be returned that match a certain critera specified
 * by a filter or returns a set in the order specified by
 * the comparator
 *
 * @see Filter
 * @see java.util.Comparator
 */
public interface FilterableSet extends Set
{
    /**
     * returns a new set that contains all
     * objects matching the filter.
     * This new set will not be updated to reflect
     * changes in the original set.
     * @param f filter to use when matching
     * @returns a new set of matching objects
     * @see #subSet(Filter)
     */
    public Set getAll(Filter f);

    /**
     * returns a subset that contains all
     * objects matching the filter.
     * Changes made to this  new set will be reflected
     * in the original set (and changes in the original
     * set will reflect in the subset).<P> For example,
     * if you remove an object from the original set it
     * will also be removed from the subset.
     * @param f filter to use when matching
     * @returns a subset of matching objects
     * @see #getAll(Filter)
     */
    public SubSet subSet(Filter f);

    /**
     * returns the objects in this set ordered
     * by the comparator.
     * Changes made to this  new set will be reflected
     * in the original set (and changes in the original
     * set will reflect in the subset).<P> For example,
     * if you remove an object from the original set it
     * will also be removed from the subset.
     * @param c comparator to use when sorting the objects
     * @returns a set ordered by the comparator
     */
    public SubSet subSet(Comparator c);

    /**
     * Method which allows an object to be added to the
     * class for a specific reason.
     * @see EventBroadcaster
     * @param o object to add
     * @param r reason the object was added
     * @returns if the item was added to the list
     */
    public boolean add(Object o, Reason r);

    /**
     * Method which allows an object to be removed to the
     * class for a specific reason.
     * @see EventBroadcaster
     * @param o object to remove
     * @param r reason the object was removed
     * @returns true if the item was removed, false if
     *          it didnt exist
     */
    public boolean remove(Object o, Reason r);
}
