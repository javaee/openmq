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
 * @(#)RemoveReason.java	1.9 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.util.lists;

import com.sun.messaging.jmq.util.lists.*;

/**
 * Reason a packet may be removed from a list.
 */

public class RemoveReason extends Reason
{
    public static final RemoveReason EXPIRED = new RemoveReason(1,"EXPIRED");
    public static final RemoveReason ACKNOWLEDGED = new RemoveReason(2,"ACKNOWLEDGED");
    public static final RemoveReason DELIVERED = new RemoveReason(3,"DELIVERED");
    public static final RemoveReason REMOVED_LOW_PRIORITY = new RemoveReason(4,"LOW_PRIORITY");
    public static final RemoveReason REMOVED_OLDEST = new RemoveReason(5,"OLDEST");
    public static final RemoveReason REMOVED_REJECTED = new RemoveReason(6,"REJECTED");
    public static final RemoveReason REMOVED_OTHER = new RemoveReason(7,"REMOVED");
    public static final RemoveReason PURGED = new RemoveReason(8,"PURGED");
    public static final RemoveReason UNLOADED = new RemoveReason(9,"UNLOADED");
    public static final RemoveReason ROLLBACK = new RemoveReason(10,"ROLLBACK");
    public static final RemoveReason OVERFLOW = new RemoveReason(11,"OVERFLOW");
    public static final RemoveReason ERROR = new RemoveReason(12,"ERROR");
    public static final RemoveReason UNDELIVERABLE = new RemoveReason(13,"UNDELIVERABLE");
    public static final RemoveReason EXPIRED_ON_DELIVERY = new RemoveReason(14,"EXPIRED_ON_DELIVERY");
    public static final RemoveReason EXPIRED_BY_CLIENT = new RemoveReason(15,"EXPIRED_BY_CLIENT");
    public static final RemoveReason REMOVE_ADMIN = new RemoveReason(16,"REMOVE_ADMIN");

    private static final RemoveReason[] reasons =
        { EXPIRED, 
          ACKNOWLEDGED,
          DELIVERED,
          REMOVED_LOW_PRIORITY,
          REMOVED_OLDEST,
          REMOVED_REJECTED,
          REMOVED_OTHER,
          PURGED,
          UNLOADED,
          ROLLBACK,
          OVERFLOW,
          ERROR,
          UNDELIVERABLE,
          EXPIRED_ON_DELIVERY,
          EXPIRED_BY_CLIENT,
          REMOVE_ADMIN };

    private RemoveReason(int id, String str) {
        super(id, str);
    }

    public static RemoveReason findReason(int id) {
        return reasons[id -1];
    }
}
