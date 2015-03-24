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
 * @(#)FileLogHandler.java	1.13 06/29/07
 */ 

package com.sun.messaging.jmq.util.log;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.sun.messaging.jmq.util.log.RollingFileOutputStream;
import com.sun.messaging.jmq.resources.SharedResources;
import com.sun.messaging.jmq.util.StringUtil;

/**
 * A LogHandler that is implemented as a set of rolling files.
 */
public class FileLogHandler extends LogHandler {


    /* RoolingFileOutputStream implements set of rolling files */
    private RollingFileOutputStream rfos = null;

    /* Path we are logging to */
    private String  logFile = null;

    private long roll_bytes = 0L;
    private long roll_secs  = 0L;

    public FileLogHandler() {
    }

    /**
     * Configure the FileLogHandler with the values contained in
     * the passed Properties object. This handler's properties are
     * prefixed with the specified prefix.
     * <P>
     * An example of valid properties are:
     * <PRE>
     * imq.log.file.rolloverbytes=10240
     * imq.log.file.rolloversecs=0
     * imq.log.file.dirpath=log
     * imq.log.file.filename=brokerlog
     * imq.log.file.output=ALL
     * </PRE>
     * In this case prefix would be "imq.log.file".
     * 
     * <P>
     * As of MQ 3.5, the value that is used to mean 'unlimited'
     * is -1 (although 0 is still supported). To support -1 as
     * an additional way of specifying unlimited, this method will
     * detect if -1 was set for rolloverbytes or rolloversecs
     * (these are the properties that are relevant here), and
     * convert it to '0'.
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

	property = prefix + "rolloverbytes";
	if ((value = props.getProperty(property)) != null) {
	    try {
		bytes = Long.parseLong(value);
            } catch (NumberFormatException e) {
	        error_msg = rb.getString(rb.W_BAD_NFORMAT, property, value);
            }
	}

	property = prefix + "rolloversecs";
	if ((value = props.getProperty(property)) != null) {
	    try {
		secs = Long.parseLong(value);
            } catch (NumberFormatException e) {
	        error_msg = (error_msg != null ? error_msg + "\n" : "") +
			rb.getString(rb.W_BAD_NFORMAT, property, value);
            }
	}

	/*
         * As of MQ 3.5, the value that is used to mean 'unlimited'
         * is -1 (although 0 is still supported). To support -1 as
         * an additional way of specifying unlimited, this method will
         * detect if -1 was set for rolloverbytes or rolloversecs
         * (these are the properties that are relevant here), and
         * pass on the value '0' to the method setRolloverLimits().
         *
	 */
	if (bytes == -1)  {
	    bytes = 0L;
	}
	if (secs == -1)  {
	    secs = 0L;
	}

        setRolloverLimits(bytes, secs);

	// Get the location of the directory we will be logging in
	String dir, file;
	property = prefix + "dirpath"; 
	if ((value = props.getProperty(property)) != null) {
	    // Expand possible variables like ${jmq.varhome}
	    value = StringUtil.expandVariables(value, props);
	    dir = value;
        } else {
	    dir = "log";
	}


	if (!((new File(dir)).isAbsolute())) {
	    // If relative path, prefix with logHome
	    dir = logger.logHome + File.separator + dir;
        }

	property = prefix + "filename"; 
	if ((value = props.getProperty(property)) != null) {
	    file = value;
        } else {
	    file = "logfile";
	}

	try {
	    setLogFile(dir, file);
        } catch (IOException e) {
	    error_msg = (error_msg != null ? error_msg + "\n" : "") +
               rb.getString(rb.E_BAD_LOGFILE,
			    dir + File.separator + file,  e);
	}

	property = prefix + "output"; 
	if ((value = props.getProperty(property)) != null) {
	    try {
	        setLevels(value);
	    } catch (IllegalArgumentException e) {
	        error_msg = (error_msg != null ? error_msg + "\n" : "") +
			property + ": " + e.getMessage();
	    }
        } 

	if (error_msg != null) {
	    throw new IllegalArgumentException(error_msg);
	}
    }

    public void setLogFile(String logDir, String logFile)
        throws IOException {

	close();

	this.logFile = logDir + File.separator + logFile;

	File d = new File(logDir);
	if (!d.exists()) {
	    boolean rcode = d.mkdirs();

	    if (rcode == false) {
	        throw new IOException(rb.getString(rb.X_DIR_CREATE, logDir));
            }
        }

	File f = new File(this.logFile);
	f.createNewFile();

	if (!f.canWrite()) {
	    throw new IOException(rb.getString(rb.X_FILE_WRITE, this.logFile));
	}
    }

    /**
     * Set rollover limits for rolling log files. Log files can
     * roll over based on bytes, time or both
     *
     * @param roll_bytes Rollover the log file when it exceeds this size.
     *                   0 to never rollover based on size.
     *			 -1 to not change roll_bytes
     *
     * @param roll_secs  Rollover the log when it exceeds this age (in ms).
     *                   0 to never rollover based on time.
     *			 -1 to not change roll_ms
     */
    public void setRolloverLimits(long roll_bytes, long roll_secs) {
	if (roll_bytes >= 0) {
            this.roll_bytes = roll_bytes;
            if (rfos != null) rfos.setRolloverBytes(this.roll_bytes);
        }

	if (roll_secs >= 0) {
            this.roll_secs   = roll_secs;
            if (rfos != null) rfos.setRolloverSecs(this.roll_secs);
        }
    }

    /**
     * Publish string to log
     *
     * @param level	Log level message is being logged at
     * @param message	Message to write to log file
     *
     */
	public void publish(int level, String message) throws IOException {

		// ignore FORCE messages if we have explicitly been asked to ignore them
		if (level == Logger.FORCE && !isAllowForceMessage()) {
			return;
		}

		if (rfos != null) {
			rfos.write(message.getBytes());
		}
	}

    /**
     * Open handler
     */
    public void open() throws IOException {
       
	if (rfos != null) return;

	rfos = new RollingFileOutputStream(new File(logFile), roll_bytes,
		roll_secs);
    }

    /**
     * Close handler
     */
    public void close() {
	if (rfos != null) {
	    try {
	        rfos.flush();
	        rfos.close();
	    } catch (IOException e) {
	    }
        }

	rfos = null;
    }

    /**
     * Flush handler. This just flushes the output stream.
     */
    public void flush() {
        if (rfos != null) {
            try {
                rfos.flush();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Return a string description of this FileHandler. The descirption
     * is the class name followed by the path of the file we are logging to.
     */
    public String toString() {
	return this.getClass().getName() + ":" + logFile;
    }
}
