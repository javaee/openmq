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
 * @(#)PrefixMessages.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util.test;

import java.text.*;
import java.util.*;

/**
 * This class has a method that will take the Object[][] from a ListResourceBundle
 * and appends the given string to all the strings in the messages (not the keys).
 *
 * Notes:
 *   - All of the objects need to be strings<p>
 */

public class PrefixMessages {
    public static Object[][] createPrefixedMessages(Object[][] old, String prefix) {
        Object[][] newContents = new Object[ old.length ][2];

        // loop through and reverse the contents
        for( int i = 0; i < old.length; i++ ) {
            try {
	        newContents[i][0] = old[i][0];
		String tmp = (String)old[i][1];

		newContents[i][1] = prefix + tmp;
            } catch (Exception e)  {
	        System.err.println("Problem encountered when generating new messages. Index="
				+ i
				+ ".\n"
				+ e.toString());

	        // just copy over the originals
	        newContents[i][0] = old[i][0];
	        newContents[i][1] = old[i][1];
	    }
        }

	return (newContents);
    }
}
