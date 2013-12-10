/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.messaging.jmq.jmsserver.audit.api;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.comm.CommGlobals;
import com.sun.messaging.jmq.jmsserver.license.LicenseBase;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsservice.BrokerEvent;

/**
 * This class contains a static method to obtain a singleton MQAuditSession
 * instance which takes care of writing audit logs for the rest of
 * the broker.
 * It is also a service factory for obtaining instances of a MQAuditSession
 * for various types of implementations.
 * <p>
 * Audit logging is an EE feature.
 *
 */
public class MQAuditService {

    static Logger logger = null;
    static BrokerResources br = null;

    // whether auditing is allowed (auditing is an EE feature)
    private static boolean AUDIT_LOGGING_LICENSED = false;

    // -------------------------------------------------------------------
    //
    // Private define constants
    //
    // -------------------------------------------------------------------

    // Property names for configuration properties and values
    // public properties:
    // imq.audit.enabled
    // 
    // private properties:
    // imq.audit.bsm.disabled
    // imq.audit.<type>.class
    public  static final String AUDIT_PROP_PREFIX = CommGlobals.IMQ + ".audit.";
    public  static final String AUDIT_ENABLED_PROP
					= AUDIT_PROP_PREFIX + "enabled";
    private static final String CLASS_PROP_SUBFIX = ".class";

    // types of MQAuditSession implementation
    public static final String BSM_TYPE = "bsm";
    public static final String LOG_TYPE = "log";

    // default values
    private static final String DEFAULT_LOG_CLASS =
		"com.sun.messaging.jmq.jmsserver.audit.impl.LogAuditSession";
    private static final String DEFAULT_BSM_CLASS =
		"com.sun.messaging.jmq.jmsserver.audit.impl.BSMAuditSession";
    private static final String MQ_AUDIT_CLASS =
		"com.sun.messaging.jmq.jmsserver.audit.impl.MQAuditManager";

    // Solaris Java based BSM audit interface
    private static final String BSM_CLASS = "com.sun.audit.AuditSession";

    // default value for whether BSM is disabled or not
    static boolean DEFAULT_BSM_DISABLED = true;
    static boolean bsmDisabled = DEFAULT_BSM_DISABLED;

    // indicate whether BSM audit logging is to be initiated
    // true iff
    //  - auditing is licensed
    //  - BSM is not disabled (by private property imq.audit.bsm.disabled)
    //  - BSM library is available
    static boolean bsmAudit = false;

    // indicate whether audit records should be written in broker's log file
    static boolean logAuditEnabled = false;

    public static final boolean isAUDIT_LOGGING_LICENSED() {
        return AUDIT_LOGGING_LICENSED;
    }

    public static boolean isBSMAudit() {
        return bsmAudit;
    } 

    public static boolean logAuditEnabled() {
        return logAuditEnabled;
    }

    public static void clear() {
        logger =  null;
        br =  null;
        auditSession = null;
    }

    public static void init() {
        logger =  CommGlobals.getLogger();
        br =  CommGlobals.getBrokerResources();
	try {
	    // Audit logging is an EE feature
	    LicenseBase license = CommGlobals.getCurrentLicense(null);
	    AUDIT_LOGGING_LICENSED = license.getBooleanProperty(
					license.PROP_ENABLE_AUDIT_CCC, false);
	} catch (BrokerException ex) {
	    AUDIT_LOGGING_LICENSED = false;
	}

	// check private property
	bsmDisabled = CommGlobals.getConfig().getBooleanProperty(
			"imq.audit.bsm.disabled", DEFAULT_BSM_DISABLED);

	if (AUDIT_LOGGING_LICENSED && !bsmDisabled) {
	    // check whether BSM auditing is avaliable
	    try {
		Class.forName(BSM_CLASS);
		bsmAudit = true;
	    } catch (Throwable t) {
		logger.log(logger.WARNING, 
                "BSM audit will not be available: " + t+"["+BSM_CLASS+"]");
	    }
	}
    }

    // Singleton MQAuditSession
    static private MQAuditSession auditSession = null;

    // -------------------------------------------------------------------
    //
    // Public static methods
    //
    // -------------------------------------------------------------------

    /**
     * The getAuditSession method returns a singleton instance of the
     * MQAuditManager class which is responsible for writing audit records
     * according to what is configured and what platform specific auditing
     * implementation is available.
     *
     * @return a reference to a MQAuditSession instance.
     */

    public static synchronized MQAuditSession getAuditSession()
	throws BrokerException {

	if (auditSession == null) {
	    // license check
	    // if auditing is not licensed and the imq.audit.enabled
	    // property is set to true; log error and exit
	    logAuditEnabled = CommGlobals.getConfig().getBooleanProperty(
				AUDIT_ENABLED_PROP, false);

	    if (!AUDIT_LOGGING_LICENSED && logAuditEnabled) {
		// check if we need to throw exception
		CommGlobals.getLogger().log(Logger.ERROR,
			BrokerResources.E_FATAL_FEATURE_UNAVAILABLE,
			CommGlobals.getBrokerResources().getString(
				BrokerResources.M_AUDIT_FEATURE));
                        CommGlobals.getCommBroker().exit(
			com.sun.messaging.jmq.util.BrokerExitCode.ERROR,
                    CommGlobals.getBrokerResources().getString(
                                BrokerResources.M_AUDIT_FEATURE),
                    BrokerEvent.Type.FATAL_ERROR);
	    }

	    // Create a new audit session. May throw an audit exception.
	    auditSession = createAuditSession();
	}

	// Return the audit session instance.
	return auditSession;
    }

    // Create a new audit session according to the specified type.
    public static MQAuditSession getAuditSession(String type)
	throws BrokerException {

	String classname = null;

	if (LOG_TYPE.equals(type)) {
	    classname = DEFAULT_LOG_CLASS;
	} else if (BSM_TYPE.equals(type)) {
	    classname = DEFAULT_BSM_CLASS;
	} else {
	    throw new BrokerException(
			"UNEXPECTED AUDIT TYPE SPECIFIED: " + type);
	}

	String prop = AUDIT_PROP_PREFIX + type + CLASS_PROP_SUBFIX;
	classname = CommGlobals.getConfig().getProperty(prop, classname);

	MQAuditSession asession = null;
	Exception exception = null;
	try {
            if (CommGlobals.isNucleusManagedBroker()) {
                asession = CommGlobals.getHabitat().
                               getService(MQAuditSession.class, type);
                if (asession == null) {
                    throw new ClassNotFoundException(classname);
                }
            } else {
	        asession = (MQAuditSession)Class.forName(classname).newInstance();
            }
        } catch (ClassNotFoundException e) {
            if (!logAuditEnabled && !bsmAudit) {
                asession = new NoAuditSession(); 
            } else {
	        exception = e;
            }
        } catch (InstantiationException e) {
	    exception = e;
        } catch (IllegalAccessException e) {
	    exception = e;
	} finally {
	    if (exception != null) {
		if (exception instanceof BrokerException) {
		    throw (BrokerException)exception;
		} else {
		    throw new BrokerException(
			br.getString(br.X_FAILED_TO_GET_AUDIT_SESSION,
				classname), exception);
		}
	    }
	}
	return asession;
    }

    // -------------------------------------------------------------------
    //
    // Private static methods
    //
    // -------------------------------------------------------------------

    /**
     * Returns a singleton instance of the MQAuditManager class which
     * is responsible for writing audit records according to what is
     * configured and what platform specific auditing implementation
     * is available.
     */
    private static MQAuditSession createAuditSession()
	throws BrokerException {

	MQAuditSession asession = null;
	Exception exception = null;
	try {
            if (CommGlobals.isNucleusManagedBroker()) {
                asession = CommGlobals.getHabitat().
                               getService(MQAuditSession.class, MQ_AUDIT_CLASS);
                if (asession == null) {
                    throw new ClassNotFoundException(MQ_AUDIT_CLASS);
                }
            } else {
	        asession = (MQAuditSession)Class.forName(MQ_AUDIT_CLASS).newInstance();
            }
        } catch (ClassNotFoundException e) {
            if (!logAuditEnabled && !bsmAudit) {
                asession = new NoAuditSession(); 
            } else {
	        exception = e;
            }
        } catch (InstantiationException e) {
	    exception = e;
        } catch (IllegalAccessException e) {
	    exception = e;
	} finally {
	    if (exception != null) {
		if (exception instanceof BrokerException) {
		    throw (BrokerException)exception;
		} else {
		    throw new BrokerException(
			br.getString(br.X_FAILED_TO_GET_AUDIT_SESSION,
				MQ_AUDIT_CLASS), exception);
		}
	    }
	}
	return asession;
    }
}
