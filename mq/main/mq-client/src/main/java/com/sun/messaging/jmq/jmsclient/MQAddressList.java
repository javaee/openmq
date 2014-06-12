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
 * @(#)MQAddressList.java	1.3 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Random;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.net.MalformedURLException;

/**
 * This class represents broker address URL.
 */
public class MQAddressList extends com.sun.messaging.jmq.io.MQAddressList {
    public static final int PRIORITY = 1;
    public static final int RANDOM = 2;

	private int behavior;


    protected  com.sun.messaging.jmq.io.MQAddress createMQAddress(String s) 
         throws java.net.MalformedURLException
   {
            return com.sun.messaging.jmq.jmsclient.MQAddress.createMQAddress(s);
    }

    public static MQAddressList createMQAddressList(String addrs)
        throws MalformedURLException {
        MQAddressList alist = new MQAddressList();
        StringTokenizer st = new StringTokenizer(addrs, " ,");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            alist.add(alist.createMQAddress(s));
        }

        return alist;
    }

	public int getBehavior() {
		return behavior;
	}

	public void setBehavior(int behavior) {
		this.behavior = behavior;

        if (behavior == RANDOM) {
            // Randomize the sequence.
            Random r = new Random();
            int max = size();

            for (int i = 0; i < max; i++) {
                int pos = i + r.nextInt(max - i);

                Object o = get(i);
                set(i, get(pos));
                set(pos, o);
            }
        }
	}

    public String toString() {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            ret.append("addr[" + i + "] :\t" + get(i) + "\n");
        }

        return ret.toString();
    }

    public static void main(String[] args) throws Exception {
        MQAddressList list = createMQAddressList(args[0]); 
        if (System.getProperty("test.random") != null)
            list.setBehavior(RANDOM);
        System.out.println(list);
    }
}

/*
 * EOF
 */
