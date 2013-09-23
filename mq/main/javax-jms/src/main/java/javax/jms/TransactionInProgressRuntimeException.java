/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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
package javax.jms;

/**
 * <P> This unchecked exception is thrown when an 
 *     operation is invalid because a transaction is in progress. 
 *     For instance, an attempt to call {@code JMSContext.commit} when the 
 *     context is part of a distributed transaction should throw a 
 *     {@code TransactionInProgressRuntimeException}.
 * 
 * @version JMS 2.0
 * @since JMS 2.0
 * 
 */
public class TransactionInProgressRuntimeException extends JMSRuntimeException {

	/**
	 * Constructs a {@code TransactionInProgressRuntimeException} with the
	 * specified detail message
	 * 
	 * @param detailMessage
	 *            a description of the exception
	 **/
	public TransactionInProgressRuntimeException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * Constructs a {@code TransactionInProgressRuntimeException} with the
	 * specified detail message and error code.
	 * 
	 * @param detailMessage
	 *            a description of the exception
	 * @param errorCode
	 *            a provider-specific error code
	 **/
	public TransactionInProgressRuntimeException(String detailMessage, String errorCode) {
		super(detailMessage, errorCode);
	}
	
	/**
	 * Constructs a {@code TransactionInProgressRuntimeException} with the
	 * specified detail message, error code and cause
	 * 
	 * @param detailMessage
	 *            a description of the exception
	 * @param errorCode
	 *            a provider-specific error code
	 * @param cause
	 *            the underlying cause of this exception
	 */
	public TransactionInProgressRuntimeException(String detailMessage, String errorCode, Throwable cause) {
		super(detailMessage, errorCode, cause);
	}

}
