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
 * @(#)InterestTable.java	1.12 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Hashtable;
import java.util.Enumeration;
import javax.jms.*;

/** The Interest Table is used to hold the consumer's object that has
 *  registered interest to the Broker.
 */


class InterestTable {

    //next available interest id.
    //XXX PROTOCOL2.1
    //private long nextInterestId = 0;
    //max number of consumers per client/connection.
    //XXX PROTOCOL2.1
    //public static final long MAX_INTEREST_ID = 1000000;

    //table to hold consumer objects that has registered interests to the
    //broker.
    private Hashtable table = new Hashtable();

    //when interestId reached its maximum, this flag is set.
    //XXX PROTOCOL2.1
    //private boolean interestIdReset = false;


    /**
     * Add message consumer to the interest table.
     *
     * @param intId    the key associated with the message consumer.
     * @param consumer the message consumer to be added to the interest table.
     */
    protected void
    put (Object intId, Object consumer) {
        table.put(intId, consumer);
    }

    /**
     * Remove message consumer form the interest table.
     *
     * @param intId the key to be used for removing the message consumer
     *              from the interest table.
     */
    protected void
    remove (Object intId) {
        table.remove (intId);
    }

    /**
     * Add message consumer to the interest table.
     *
     * @param consumer the message consumer to be added to the interest table.
     */
    protected void
    addInterest (Consumer consumer) {
        put (consumer.interestId, consumer);
    }

    /**
     * Remove message consumer form the interest table.
     *
     * @param consumer the message consumer to be removed from the interest
     * table.
     */
    protected void
    removeInterest (Consumer consumer) {
        if (consumer.interestId != null) {
            remove (consumer.interestId);
        }
    }

    /**
     * Get the message consumer from the interest table based on the
     * interest id.
     *
     * @param interestId the key that the consumer is used to store in the
     *                   interest table.
     * @return Consumer the consumer in the interest table that
     *                             matches the interest id.
     */
    protected Consumer
    getConsumer (Object interestId) {
        return  (Consumer)table.get(interestId);
    }

    /**
     * Get all consumers in this connection
     */
     protected Enumeration getAllConsumers() {
        return table.elements();
     }

     /**
      * return an array of all consumers in this connection.
      */
     protected Object[] toArray() {
         return table.values().toArray();
     }

    /**
     * Get the next available interest id.
     *
     * @return the next available interest id.
     */
    //XXX PROTOCOL2.1 -- to be removed.
    /*protected synchronized
    Long getNextInterestId() {
        nextInterestId ++;

        //check if it has reached max value
        if (nextInterestId == MAX_INTEREST_ID) {
            nextInterestId = 1;
            interestIdReset = true;
        }

        //if it has reached to the limit at least once.
        if ( interestIdReset == true ) {
            boolean found = false;
            while ( !found ) {
                //check if still in use
                Object key = table.get ( new Long (nextInterestId) );
                if ( key == null ) {
                    //not in use
                    found = true;
                } else {
                    //increase one and keep trying
                    nextInterestId ++;
                    //still need to check the limit
                    if (nextInterestId == MAX_INTEREST_ID) {
                        nextInterestId = 1;
                    }
                }
            }
        }
        //XXX PROTOCOL2.1
        return new Long (nextInterestId);
    }*/

}

