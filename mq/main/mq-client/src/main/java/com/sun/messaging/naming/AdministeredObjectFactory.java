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
 * @(#)AdministeredObjectFactory.java	1.12 07/02/07
 */ 

package com.sun.messaging.naming;

import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.RefAddr;
import javax.naming.Reference;
import com.sun.messaging.AdministeredObject;

/**
 * The <code>AdministeredObjectFactory</code> class is the factory class for
 * iMQ Administered Objects that are stored using the Java Naming and Directory
 * Interface (JNDI) API for regeneration using the getInstance() method of this class.
 */ 

public class AdministeredObjectFactory implements javax.naming.spi.ObjectFactory {

    /** Key for version in Reference objects */
    protected static final String REF_VERSION = "version";

    /** Key for read only flag in Reference objects */
    protected static final String REF_READONLY = "readOnly";
 
    /** Current Administered Object version supported */
    protected static final String AO_VERSION_STR = "3.0";

    /** iMQ 3.0 Beta Administered Object version supported */
    protected static final String AO_VERSION_STR_JMQ3B = "2.1";

    /** JMQ 2 Administered Object version supported */
    protected static final String AO_VERSION_STR_JMQ2 = "2.0";

    /** JMQ 1 Administered Object version supported */
    protected static final String AO_VERSION_STR_JMQ1 = "1.1";

    /**
     * Creates an instance of the object represented by a Reference object.
     *
     * @param obj The Reference object.
     *
     * @return an instance of the class named in the Reference object <code>obj</code>.
     * @return null if <code>obj</code> is not an instance of a Reference object.
     *
     * @throws MissingVersionNumberException if either <code>obj</code> references an object
     *         that is not an instance of a <code>com.sun.messaging.AdministeredObject</code> object
     *         or the version number is missing from the Reference object.
     * @throws UnsupportedVersionNumberException if an unsupported version number is present
     *         in the Reference.
     * @throws CorruptedConfigurationPropertiesException if <code>obj</code> does not have the
     *         minimum information neccessary to recreate an instance of a
     *         a valid <code>com.sun.messaging.AdministeredObject</code>.
     */
    public
    Object getObjectInstance (Object obj, Name name, Context ctx, Hashtable env) throws Exception {

        if (obj instanceof Reference) {
            Reference ref = (Reference)obj;
            String version = null;
            boolean readOnly = false;
            
            //Construct the desired AdministeredObject
            Object newobj = Class.forName(ref.getClassName()).newInstance();

            //version number MUST exist and it MUST be this version or a supported version
            RefAddr versionAddr = ref.get(REF_VERSION);

            //Support reading previous object versions here (2.0, 2.1 etc.). Floor is 2.0
            if (versionAddr == null || !(newobj instanceof com.sun.messaging.AdministeredObject)) {
                //if version number does not exist or it is not an AdministeredObject
                throw new MissingVersionNumberException();
            } else {
                version = (String)versionAddr.getContent();
                //Support reading previous object versions here (2.0, 2.1 etc.). Floor is 2.0
                if ( ! (AO_VERSION_STR.equals(version) ||
                        AO_VERSION_STR_JMQ3B.equals(version) ||
                        AO_VERSION_STR_JMQ2.equals(version)) ){
                    //Reference contains a bad version number
                    throw new UnsupportedVersionNumberException(version);
                }
                if (ref.size() < 2) {
                    //Reference is corrupted
                    throw new CorruptedConfigurationPropertiesException();
                }
                RefAddr readOnlyAddr = ref.get(REF_READONLY);
                if ("true".equals((String)readOnlyAddr.getContent())) {
                    //Reference has readOnly set
                    readOnly = true;
                }
                ((AdministeredObject)newobj).storedVersion = version;
            }

            RefAddr refaddr;                                                                               
            String refContent;
            //Skip the version # and r/o flag (start at 2)
            //System.out.println("AOtoString="+ newobj.toString());
            for (int i = 2; i < ref.size(); i++) {
                refaddr = ref.get(i);
                refContent = (String)refaddr.getContent();
                //System.out.println("gOI:ref#="+i+"; refCntnt="+refContent);
                //Guard against null values coming back from JNDI
                //Some service-providers will return `null'; others will return "" (empty string)
                if (refContent == null) {
                    refContent = "";
                }
                //If property fails to set then ignore since we may have looked up a newer object
                try {
                    //XXX RFE:tharakan
                    //Need to add support migrating 2.x properties to 3.x
                    //System.out.println("gOI:settingProp");
                    //System.out.println("gOI:propName="+refaddr.getType());
                    ((AdministeredObject)newobj).setProperty(refaddr.getType(), refContent);
                    //System.out.println("gOI:propName="+refaddr.getType()+" set successfully");
                } catch (Exception bpe) {
                    //Ignore exception
                    //System.out.println("gOI:propName="+refaddr.getType()+" exception; "+bpe.getMessage());
                    //bpe.printStackTrace();
                }
            }
            //Set the readOnly flag
            if (readOnly) {
                ((AdministeredObject)newobj).setReadOnly();
            }
            return newobj;
        }
        return null;
    }
}

