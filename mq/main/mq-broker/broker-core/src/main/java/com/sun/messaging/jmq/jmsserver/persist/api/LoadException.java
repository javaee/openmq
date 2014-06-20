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
 * @(#)LoadException.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.api;

import com.sun.messaging.jmq.jmsserver.util.BrokerException;

/**
 * This class provides information about problems and/or data corruptions
 * encountered when loading data from persistent store.
 * If the key and/or the value of the hash map entry is loaded successfully,
 * it can be retrieved by calling <code>getKey()</code> and/or
 * <code>getValue</code> respectively. The throwable caught while
 * deserializing the key can be retrieved by calling
 * <code>getKeyCause()</code>. Similarly, the throwable caught while
 * deserializing the value can be retrieved by calling
 * <code>getValueCause()</code>. Other exception caught while parsing
 * the record, if any, can be retrieved by <code>getCause</code>.
 * <code>getNextException()</code> returns
 * the next chained exception for other loading problems or
 * <code>null</code> if there's no more chained exception.
 */

public class LoadException extends BrokerException {

    private Object key = null;
    private Object value = null;
    private LoadException next = null;
    private Throwable keyCause = null;
    private Throwable valueCause = null;

    /**
     * Constructs a LoadException
     */ 
    public LoadException(String msg, Throwable t) {
        super(msg, t);
    }

    public void setKey(Object k) {
	this.key = k;
    }

    /**
     * The key of the HashMap entry loaded from file.
     */
    public Object getKey() {
	return key;
    }

    public void setValue(Object v) {
	this.value = v;
    }

    /**
     * The value of the HashMap entry loaded from file.
     */
    public Object getValue() {
	return value;
    }

    public void setKeyCause(Throwable t) {
	this.keyCause = t;
    }

    /**
     * Return the Throwable caught while loading the key.
     */
    public Throwable getKeyCause() {
	return this.keyCause;
    }

    public void setValueCause(Throwable t) {
	this.valueCause = t;
    }

    /**
     * Return the Throwable caught while loading the key.
     */
    public Throwable getValueCause() {
	return this.valueCause;
    }

    public void setNextException(LoadException e) {
	this.next = e;
    }

    /**
     * Return the exception chained to this object.
     */
    public LoadException getNextException() {
	return next;
    }

    public String toString() {
	return getMessage() + "\nkey="+key+";cause="+keyCause+";value="+value
			+";cause="+valueCause;
    }
}
