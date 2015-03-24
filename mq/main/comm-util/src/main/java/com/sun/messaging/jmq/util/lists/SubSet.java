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
 * @(#)SubSet.java	1.8 08/28/07
 */

package com.sun.messaging.jmq.util.lists;


import java.util.Set;

/**
 * this class represents a view into an existing set.
 * This subset is different from a copy of a set, because
 * changing one affects the other. e.g.<BR>
 * <UL>
 *   <LI>adding an item to the original set is reflected
 *       in the subset <i>if </i>the new object should be
 *       part of the subset </li>
 *   <LI>adding an item to the subset is reflected
 *       in the original set </li>
 * </UL>
 */
public interface SubSet extends Set, EventBroadcaster
{

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

    /**
     * optional method which tells the
     * system it can free up resources.
     * If destroy is not called, the
     * subset will no longer be maintained
     * once the garbage collector frees
     * the reference.
     */
    public void destroy();

    /**
     * Used instead of iterator.next(), 
     * iterator.remove() to remove the first 
     * item from the list.
     * Subsets do not allow iterator.remove() to
     * be called because of the risk on 
     * incorrect state or deadlocks.
     */
    public Object removeNext();

    public String toDebugString();

    public Object peekNext();


}
