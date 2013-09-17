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
 * @(#)DObjectFactory.java	1.6 07/02/07
 */ 

package com.sun.messaging.naming;

import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.RefAddr;
import javax.naming.Reference;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.Destination;
import com.sun.messaging.Queue;
import com.sun.messaging.Topic;
import com.sun.messaging.DestinationConfiguration;

/**
 * <code>DObjectFactory</code> handles instance creation for
 * <code>com.sun.messaging.Queue</code>
 * and <code>com.sun.messaging.Topic</code> objects from JMQ1.1 created Reference objects.
 * <p>
 * It specifically handles the format conversion from
 * Reference objects created by JMQ1.1 <code>Destination</code> objects
 * to iMQ3.0 <code>Destination</code> objects.
 * <p>
 * @see javax.naming.Reference javax.naming.Reference
 * @see com.sun.messaging.Destination com.sun.messaging.Destination
 * @see com.sun.messaging.AdministeredObject com.sun.messaging.AdministeredObject
 * @since JMQ2.0
 */

/*
 * IMPORTANT:
 * ==========
 * The size of the reference for JMQ1.1 Destination objects is always 2.
 * The format of the reference in JMQ1.1 is always
 * as follows --
 *
 *  [0] = reserved for version
 *  [1] = reserved for destination name
 */

public abstract class DObjectFactory extends AdministeredObjectFactory {

    /** used only by Destination reference objects */
    private static final String REF_DESTNAME = "destName";
 
    /**
     * Creates an instance of the object represented by a Reference object.
     *   
     * @param obj The Reference object.
     *   
     * @return an instance of the class named in the Reference object <code>obj</code>.
     * @return null if <code>obj</code> is not an instance of a Reference object.
     *   
     * @throws MissingVersionNumberException if either <code>obj</code> references an object
     *         that is not an instance of a <code>com.sun.messaging.Queue</code> object
     *         or the version number is missing from the Reference object.
     * @throws UnsupportedVersionNumberException if an unsupported version number is present
     *         in the Reference.
     * @throws CorruptedConfigurationPropertiesException if <code>obj</code> does not have the
     *         minimum information neccessary to recreate an instance of a
     *         a valid <code>com.sun.messaging.AdministeredObject</code>.
     */
    public
    Object getObjectInstance
        (Object obj, Name name, Context ctx, Hashtable env) throws Exception {

        if (obj instanceof Reference) {
            Reference ref = (Reference)obj;
            String refClassName;
            refClassName = ref.getClassName();
            Destination destObj = null;
            if (refClassName.equals(com.sun.messaging.Queue.class.getName())) {
                destObj = new Queue();
            } else {
                if (refClassName.equals(com.sun.messaging.Topic.class.getName())) {
                    destObj = new Topic();
                } else {
                    throw new MissingVersionNumberException();
                }
            }
            //version number MUST exist and it MUST be the same as AO_VERSION_STR_JMQ1
            RefAddr versionAddr = ref.get(REF_VERSION);
            if (versionAddr == null) {
                //version number does not exist
                throw new MissingVersionNumberException();
            } else {
                String version = null;
                if (!AO_VERSION_STR_JMQ1.equals(version = (String)versionAddr.getContent())) {
                    //version number does not match
                    throw new UnsupportedVersionNumberException(version);
                }
                ((AdministeredObject)destObj).storedVersion = version;
            }
            RefAddr destAddr = ref.get(REF_DESTNAME);
            if (destAddr != null) {
                    destObj.setProperty(DestinationConfiguration.imqDestinationName,
                                            (String)destAddr.getContent());
                    return destObj;
            } else {
                    throw new CorruptedConfigurationPropertiesException();
            }
        }
        return null;
    }
}

