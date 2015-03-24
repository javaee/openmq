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
 * @(#)MQObjectInputStream.java	1.7 07/25/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.api.util;

import java.io.*;

/**
 * A special subclasses of ObjectInputStream that is used for store migration.
 * This class allow us to deserialize an old object format by allowing us to
 * locate class file containing the definitions for the old object format that
 * has been moved to a different package.
 *
 * As an example, we want to change the TransactionUID class, which would make
 * it incompatible with the serialized version in the old store. First, we
 * moved the original version of the TransactionUID to another package, e.g.
 * com.sun.messaging.jmq.jmsserver.data.migration.TransactionUID. Next, we
 * create a new version of com.sun.messaging.jmq.jmsserver.data.TransactionUID
 * class. When loading the old serialized TransactionUID object, we use this
 * class to locate the class definition of the old TransactionUID which has
 * been moved to com.sun.messaging.jmq.jmsserver.data.migration.
 */
public class MQObjectInputStream extends ObjectInputStream {

    public MQObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Overide the ObjectInputStream.resolveClass() to return the old class
     * definition for serialized object prior to 3.7 release.
     */
    protected Class resolveClass(ObjectStreamClass osc)
        throws IOException, ClassNotFoundException {

        Class clazz = null;
        String name = osc.getName();
        long serialVersion = osc.getSerialVersionUID();

        // For performance we check serial version ID before the class name
        if (serialVersion == 1518763750089861353L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.data.TransactionAcknowledgement")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.data.migration.TransactionAcknowledgement");
            }
        } else if (serialVersion == -6848527428749630176L) {
                if (name.equals("com.sun.messaging.jmq.jmsserver.data.TransactionState")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.data.migration.TransactionState");
            }
        } else if (serialVersion == 4438769866522991889L) {
                if (name.equals("com.sun.messaging.jmq.jmsserver.data.TransactionState")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.data.migration.thrasher2.TransactionState");
            }
        } else if (serialVersion == 4132677693277056907L) {
                if (name.equals("com.sun.messaging.jmq.jmsserver.data.TransactionState")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.data.migration.finch.TransactionState");
            }
        } else if (serialVersion == 3158474602500727000L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.data.TransactionUID")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.data.migration.TransactionUID");
            }
        } else if (serialVersion == 5231476734057401743L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.core.ConsumerUID")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.core.migration.ConsumerUID");
            }
        } else if (serialVersion == 8099322820906352261L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.core.ConsumerUID")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.core.migration.thrasher.ConsumerUID");
            }
        } else if (serialVersion == 5642215309770752611L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.persist.TransactionInfo")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.persist.api.TransactionInfo");
            }
        } else if (serialVersion == -6833553314062089908L) {
            if (name.equals("com.sun.messaging.jmq.jmsserver.persist.HABrokerInfo")) {
                clazz = Class.forName("com.sun.messaging.jmq.jmsserver.persist.api.HABrokerInfo");
            }
        }

        // Return the class definition of old serialized object
        if (clazz != null) {
            return clazz;
        }

        return super.resolveClass(osc);
    }
}
