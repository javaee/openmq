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
 * @(#)PacketFormatter.java	1.3 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.logging;

import java.util.*;
import java.util.logging.*;
import java.io.*;

import com.sun.messaging.jmq.io.ReadOnlyPacket;

/**
 * MQ packet formatter.
 *
 * This is also a utility class that may be used to format MQ packets.
 */
public class PacketFormatter extends SimpleFormatter {

    /**
     * Format the log record.  If this is a MQ packet record, it is formatted
     * to the packet format.  Otherwise, the simple formatter format is used.
     */
    public synchronized String format(LogRecord record) {

        String str = doFormat (record);

        if ( str == null ) {
            str = super.format(record);
        }

        return str;
    }

    /**
     * Check if this is a MQ packet.  If yes, calls formatPacket method to
     * format the packet.
     */
    public static String doFormat (LogRecord record) {

        String lstring = null;

        ReadOnlyPacket pkt = getPacket(record);

        if ( pkt != null ) {

            long time = record.getMillis();
            Date date = new Date();
            date.setTime(time);

            lstring = date.toString() + "  " + record.getMessage() + "\n";

            lstring = lstring + formatPkt(pkt);
        }

        return lstring;
    }

    /**
     * Get MQ packet from the log record.
     */
    public static ReadOnlyPacket getPacket(LogRecord record) {

        ReadOnlyPacket pkt = null;

        Object obj[] = record.getParameters();

        if (obj != null) {

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof ReadOnlyPacket ) {
                    pkt = (ReadOnlyPacket) obj[i];
                    break;
                }
            }
        }

        return pkt;
    }

    /**
     * Format MQ packet.
     */
    public static String formatPkt(ReadOnlyPacket pkt) {

        String out = null;

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            pkt.dump(ps);

            ps.flush();
            ps.close();
            baos.close();

            out = baos.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return out;
    }

}
