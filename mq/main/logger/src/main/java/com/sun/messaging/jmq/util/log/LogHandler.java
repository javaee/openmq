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
 * @(#)LogHandler.java	1.12 06/29/07
 */ 

package com.sun.messaging.jmq.util.log;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Properties;
import com.sun.messaging.jmq.resources.SharedResources;
import org.jvnet.hk2.annotations.Contract;
import org.glassfish.hk2.api.PerLookup;


/**
 * Abstract class defining interface for a LogHandler. A LogHandler is
 * used by a Logger to publish log message to a logging device. A 
 * LogHandler could be implemented as a set of rolling files, or a 
 * simple output stream.
 */

@Contract
@PerLookup
public abstract class LogHandler {

    /**
     * The levels of messages this handler wants to accept.
     * Log levels are described in the Logger class.
     */
    public int levels = Logger.INFO | Logger.WARNING | Logger.ERROR |
                        Logger.FORCE;

    /* Our parent Logger */
    protected Logger logger = null;

    protected static final SharedResources rb = SharedResources.getResources();

    protected String name = null;
    
    /**
     * Whether messages of the level Level.FORCE should be sent to this handler
     */
    private boolean allowForceMessage=true;

    /**
     * Return whether messages of the level Level.FORCE may be sent to this handler
     * @return
     */
    public boolean isAllowForceMessage() {
		return allowForceMessage;
	}

    /**
     * Specify whether messages of the level Level.FORCE may be sent to this handler
     * @param allowForceMessage
     */
	protected void setAllowForceMessage(boolean allowForceMessage) {
		this.allowForceMessage = allowForceMessage;
	}

	/**
     * Convenience routine to have handler accept messages of all levels
     */
    public void acceptAllLevels() {
	levels = Logger.FORCE    |
                 Logger.ERROR	 |
    		 Logger.WARNING  |
    		 Logger.INFO     |
    		 Logger.DEBUG    |
    		 Logger.DEBUGMED |
    		 Logger.DEBUGHIGH;
    }

    /**
     * Set the log levels this handler will handle based on a String 
     * description. This is useful for setting the levels from a
     * property string.
     * 
     * @param levelList		A | seperated list of log levels this
     *				handler will accept. Valid values are
     *				"ALL", "NONE", "NOFORCE" or a list of one or more of
     *				ERROR, WARNING, INFO, DEBUG, DEBUGMED,
     *				and DEBUGHIGH
     *
     *	"NOFORCE" specifies that FORCE messages should not be sent to this handler;
     *   any that are sent to this handler will be ignored. You should typically
     *   use this in conjunction with a list of log levels that are accepted.
     */
	protected void setLevels(String levelList) throws IllegalArgumentException {

		String s;
		levels = 0;

		// All handlers will by default accept forced messages (override with NONE or NOFORCE)
		levels = Logger.FORCE;

		// Parse string and initialize levels bitmask.
		StringTokenizer token = new StringTokenizer(levelList, "|", false);
		while (token.hasMoreElements()) {
			s = token.nextToken();
			if (s.equals("ALL")) {
				acceptAllLevels();
				break;
			} else if (s.equals("NONE")) {
				levels = 0;
				break;
			} else if (s.equals("NOFORCE")) {
				setAllowForceMessage(false);
			} else {
				levels |= Logger.levelStrToInt(s);
			}
		}

	}

    /**
     * Perform basic initialization of the LogHandler
     *
     * @param parent	Logger parent of this LogHandler
     */
    public void init(Logger parent) {
	this.logger = parent;
        acceptAllLevels();
    }

    /**
     * Set the name of this handler
     *
     * @param name  Handler name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of this handler
     */
    public String getName() {
        return this.name;
    }

    /**
     * Flush handler
     */
    public void flush() {
        // No-op
    }

    abstract public void configure(Properties props, String prefix)
	throws IllegalArgumentException;

    /**
     * Publish string to log
     *
     * @param level	The log level
     * @param message	The message to publish to loggin device.
     */
    abstract public void publish(int level, String message) throws IOException;

    /**
     * Open handler
     */
    abstract public void open() throws IOException;

    /**
     * Close handler
     */
    abstract public void close();
    
}
