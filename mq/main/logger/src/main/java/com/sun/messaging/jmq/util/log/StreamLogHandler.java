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
 * @(#)StreamLogHandler.java	1.8 06/29/07
 */ 

package com.sun.messaging.jmq.util.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import com.sun.messaging.jmq.util.log.RollingFileOutputStream;

/**
 * A LogHandler that is implemented as a simple OutputStream
 * (For example System.err)
 */
public class StreamLogHandler extends LogHandler {

    private OutputStream os = null;

    public StreamLogHandler() {
    }

    /**
     * Configure the StreamLogHandler with the values contained in
     * the passed Properties object. This handler's properties are
     * prefixed with the specified prefix.
     * <P>
     * An example of valid properties are:
     * <PRE>
     * jmq.log.console.stream=ERR
     * jmq.log.console.output=ERROR|WARNING|INFO
     * </PRE>
     * In this case prefix would be "jmq.log.stream"
     *
     * @param props	Properties to get configuration information from
     * @param prefix	String that this handler's properties are prefixed with
     *
     * @throws IllegalArgumentException if one or more property values are
     *                                  invalid. All valid properties will
     *					still be set.
     */
    public void configure(Properties props, String prefix)
	throws IllegalArgumentException {

	String value = null;
	String property = null;
	String error_msg = null;
	long   bytes = 0L, secs = 0L;;

	prefix = prefix + ".";

	property = prefix + "stream";
	if ((value = props.getProperty(property)) != null) {
	    if (value.equals("ERR")) {
		setLogStream(System.err);
	    } else if (value.equals("OUT")) {
		setLogStream(System.out);
            } else {
	        error_msg = rb.getString(rb.W_BAD_LOGSTREAM, property, value);
            }
	}

	property = prefix + "output"; 
	if ((value = props.getProperty(property)) != null) {
	    try {
	        setLevels(value);
	    } catch (IllegalArgumentException e) {
	        error_msg = (error_msg != null ? error_msg + rb.NL : "") +
			property + ": " + e.getMessage();
	    }
        } 

	if (error_msg != null) {
	    throw new IllegalArgumentException(error_msg);

	}
    }

    public void setLogStream(OutputStream os) {
	close();
	this.os = os;
    }

    /**
     * Publish string to log
     *
     * @param level	Log level to use
     * @param message	Message to write to log file
     *
     */
	public void publish(int level, String message) throws IOException {

		// ignore FORCE messages if we have explicitly been asked to ignore them
		if (level == Logger.FORCE && !isAllowForceMessage()) {
			return;
		}

		if (os != null) {
			os.write(message.getBytes());
		}
	}

    /**
     * Open handler. This is a no-op. It is assumed the stream is already
     * opened.
     */
    public void open() throws IOException {
	return;
    }

    /**
     * Close handler. This just flushes the output stream.
     */
    public void close() {
	if (os != null) {
	    try {
	        os.flush();
	    } catch (IOException e) {
	    }
	}
    }

    /**
     * This just flushes the output stream.
     */
    public void flush() {
        if (os != null) {
            try {
                os.flush();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Return a string description of this FileHandler. The descirption
     * is the class name followed by the path of the file we are logging to.
     */
    public String toString() {
	return this.getClass().getName() + ":" + os.toString();
    }
}
