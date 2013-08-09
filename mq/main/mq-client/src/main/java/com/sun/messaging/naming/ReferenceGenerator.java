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
 * @(#)ReferenceGenerator.java	1.7 07/02/07
 */ 

package com.sun.messaging.naming;

import com.sun.messaging.AdministeredObject;
import java.util.Properties;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.naming.StringRefAddr;
import javax.naming.Reference;

/**
 * A ReferenceGenerator generates a Reference object given an Administered object and
 * the Object Factory Class Name.
 */
public class ReferenceGenerator {

    /** The index in the Reference object of the Version Number */
    public final static int REF_INDEX_VERSION = 0;

    /** The index in the Reference object of the read only state */
    public final static int REF_INDEX_RO_STATE = 1;

    /** The index in the Reference object of the configuration properties */
    public final static int REF_INDEX_PROPERTIES = 2;

    /**
     * Returns the reference to this object.
     *
     * @param ao The AdministeredObject for which the Reference object is to be generated.
     * @param objectfactoryclassname The classname of the ObjectFactory class for ao.
     *
     * @return  The Reference object that can be used to reconstruct this object
     */
    public static Reference getReference(AdministeredObject ao, String objectfactoryclassname) {
    
        //Create a Reference without any addresses
        Reference ref = new Reference(ao.getClass().getName(),
               objectfactoryclassname, null);

        //Set the version number
        ref.add(REF_INDEX_VERSION, new StringRefAddr
            (AdministeredObjectFactory.REF_VERSION, ao.getVERSION()));

        //Set the readOnly state
        ref.add(REF_INDEX_RO_STATE, new StringRefAddr
            (AdministeredObjectFactory.REF_READONLY, String.valueOf(ao.isReadOnly())));

        //Set the configuration
        String sb;
        Properties aoprops = ao.getConfiguration();
        Enumeration ep = aoprops.propertyNames();
        for (int i = REF_INDEX_PROPERTIES; ep.hasMoreElements(); i++) {
            try {
                sb = (String)ep.nextElement();
                ref.add(i, new StringRefAddr(sb, (String)aoprops.get(sb)));
            } catch (NoSuchElementException e) {
                break;
            }
        }
        return ref;
    }
}
