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
 * @(#)BrokerException.java	1.13 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.util;


import com.sun.messaging.jmq.io.Status;

/**
 * this is the sub-class for exceptions thrown by the broker
 */

public class BrokerException extends Exception
{
    /**
     * the "error-code" associated with the problem (if any)
     */
    private String errorID = null;

    /**
     * the status code associated with the problem (if any)
     */
    private int status = Status.ERROR;

    /**
     * Whether this exception's stacktrace has been logged 
     */
    private boolean stackLogged = false;

    private boolean remote = false;
    private Object remoteBroker = null;
    private String remoteConsumers = "";
    private boolean sqlRecoverable = false;
    private boolean sqlReplayCheck = false;
    private boolean sqlReconnect = false;

    /**
     * create an exception with no message or root cause
     */
    public BrokerException() {
        super();
    }

    /**
     * create an exception with a message but no root cause
     *
     * @param msg the detail message
     */
    public BrokerException(String msg) {
        this(msg, null, null);
    }

    public BrokerException(String msg, int status) {
        this(msg, null, null, status);
    }
    public BrokerException(String msg, Throwable thr, int status) {
        this(msg, null, thr, status);
    }

    /**
     * create an exception with a message and a root cause
     *
     * @param msg the detail message
     * @param thr the root cause
     */
    public BrokerException(String msg, Throwable thr) {
        this(msg,  null, thr);
    }

    /**
     * create an exception with a message but no root cause
     *
     * @param msg the detail message
     */
    public BrokerException(String msg, String errcode) {
        this(msg, errcode, (Throwable) null);
    }

    /**
     * create an exception with a message and a root cause
     *
     * @param msg the detail message
     * @param thr the root cause
     */
    public BrokerException(String msg, String errcode, Throwable thr) {
        this(msg, errcode, thr, Status.ERROR);
    }

    /**
     * create an exception with a message and a root cause
     *
     * @param msg the detail message
     * @param thr the root cause
     */
    public BrokerException(String msg, String errcode, Throwable thr, int status) {
        super(msg, thr);
        this.errorID = errcode;
        this.status = status;
    }


    /**
     * retrieves the error code associated with the exception
     *
     * @returns the error code (if any)
     */
    public String getErrorCode() {
	return errorID;
    }

    /**
     * retrieves the status code associated with the exception
     *
     * @returns the status code
     */
    public int getStatusCode() {
	return status;
    }

    public void overrideStatusCode(int s) {
        this.status = s;
    }

    public void setRemote(boolean v) {
        remote = v;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemoteBrokerAddress(Object b) {
        remoteBroker = b;
    }

    public Object getRemoteBrokerAddress() {
        return remoteBroker;
    }

    public void setStackLogged() {
        stackLogged = true;
    }

    /**
     * To use this method, you are responsible to call setStackLogged() 
     */
    public boolean isStackLogged() {
        return stackLogged;
    }

    /**
     * space separated ConsumerUID.longValue()s  
     */
    public void setRemoteConsumerUIDs(String cuids) {
        remoteConsumers = cuids;
    }

    public String getRemoteConsumerUIDs() {
        return remoteConsumers;
    }

    public void setSQLRecoverable(boolean b) {
        sqlRecoverable = b;
    }

    public boolean getSQLRecoverable() {
        return sqlRecoverable;
    }

    public void setSQLReplayCheck(boolean b) {
        sqlReplayCheck = b;
    }

    public boolean getSQLReplayCheck() {
        return sqlReplayCheck;
    }

    public void setSQLReconnect(boolean v) {
        sqlReconnect = v;
    }

    public boolean getSQLReconnect() {
        return sqlReconnect;
    }

/*
    public String toString() {
        String str = "";
        if (errorID != null)
            str += errorID + ": ";

        str += super.toString();

        if (getCause() != null)
            str += "[" + getCause().toString() +"]";

        return str;
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(java.io.PrintStream s) {
        if (thr != null)
            thr.printStackTrace(s);
        else
            super.printStackTrace(s);
    }

    public void printStackTrace(java.io.PrintWriter w) {
        if (thr != null)
            thr.printStackTrace(w);
        else
            super.printStackTrace(w);
    }
*/
}
