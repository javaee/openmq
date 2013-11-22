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
 * @(#)ULFformatter.java	1.4 07/02/07
 */ 

package com.sun.messaging.jms.logging;

import java.util.*;
import java.util.logging.*;

import java.io.*;

import java.text.SimpleDateFormat;

import com.sun.messaging.jmq.jmsclient.ConnectionMetaDataImpl;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

/**
 * ULF Message formatter.
 */
public class ULFformatter extends SimpleFormatter {

    public static final String FR_BEGIN = "[#|";
    public static final String FR_END = "|#]\n";
    public static final String FR_DELIMITER = "|";

    public static final String
        PRODUCT_NAME = ConnectionMetaDataImpl.JMSProviderName + " " +
                       ConnectionMetaDataImpl.providerVersion;

    public static final ClientResources resources = ClientResources.getResources();

    //XXX HAWK: replace time zone with offset time.
    public static final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS z";

    //XXX HAWK: use static instance?
    private SimpleDateFormat formatter =
        new SimpleDateFormat(pattern, Locale.getDefault());

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
     * Format message to ULF format.
     *
     * [#|Date&Time&Zone|LogLevel|ProductName|ModuleID|OptionalKey1=Value1
     * ;OptionalKey2=Value2;OptionalKeyN=ValueN|MessageID:MessageText|#]\n
     *
     * http://jpgserv.us.oracle.com/not/MQHawk/engineering/funcspecs/javaClientLogging/UniformLogging0.7.pdf
     *
     *
     * @param record LogRecord
     * @return String
     */
    private String doFormat (LogRecord record) {

        StringBuffer sb = new StringBuffer (FR_BEGIN);

        String datestr = formatter.format ( new Date(record.getMillis()) );

        sb.append(datestr).append(FR_DELIMITER);

        sb.append( record.getLevel().getName() ).append(FR_DELIMITER);

        sb.append(PRODUCT_NAME).append(FR_DELIMITER);

        sb.append(record.getSourceClassName()).append(FR_DELIMITER);

        Object params[] = record.getParameters();

        int length = 0;

        if ( params != null ) {
            length = params.length;
        }

        String key = record.getMessage();

        String msg = null;

        try {
            switch (length) {
            case 0:
                msg = resources.getKString(key);
                break;
            case 1:
                msg = resources.getKString(key, params[0]);
                break;
            case 2:
                msg = resources.getKString(key, params[0], params[1]);
                break;
            default:
                msg = resources.getKString(key, params);
            }
        } catch (Exception e) {
           msg = key;
        }

        Throwable throwable = record.getThrown();
        if ( throwable != null ) {
            msg = msg + "\n" + getThrowableMessage (throwable);
        }

        sb.append(msg);

        sb.append(FR_END);

        return sb.toString();
    }

    private static String getThrowableMessage (Throwable throwable) {
        String msg = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintWriter pw = new PrintWriter(baos);

            throwable.printStackTrace(pw);

            pw.flush();
            baos.flush();

            baos.close();

            msg = baos.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

}
