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

package javax.xml.messaging;

import javax.xml.soap.*;

/**
 * An exception that signals that a JAXM exception has occurred. A
 * <code>JAXMException</code> object may contain a <code>String</code>
 * that gives the reason for the exception, an embedded
 * <code>Throwable</code> object, or both. This class provides methods
 * for retrieving reason messages and for retrieving the embedded
 * <code>Throwable</code> object.
 *
 * <P> Typical reasons for throwing a <code>JAXMException</code>
 * object are problems such as not being able to send a message and
 * not being able to get a connection with the provider.  Reasons for
 * embedding a <code>Throwable</code> object include problems such as
 * an input/output errors or a parsing problem, such as an error
 * parsing a header.
 */
public class JAXMException extends SOAPException {

    private Throwable cause;

    /**
     * Constructs a <code>JAXMException</code> object with no
     * reason or embedded <code>Throwable</code> object.
     */
    public JAXMException() {
        super();
    }

    /**
     * Constructs a <code>JAXMException</code> object with the given
     * <code>String</code> as the reason for the exception being thrown.
     *
     * @param reason a <code>String</code> giving a description of what 
     *        caused this exception
     */
    public JAXMException(String reason) {
        super(reason);
    }

    /**
     * Constructs a <code>JAXMException</code> object with the given
     * <code>String</code> as the reason for the exception being thrown
     * and the given <code>Throwable</code> object as an embedded
     * exception.
     *
     * @param reason a <code>String</code> giving a description of what 
     *        caused this exception
     * @param cause a <code>Throwable</code> object that is to
     *        be embedded in this <code>JAXMException</code> object
     */
    public JAXMException(String reason, Throwable cause) {
       super (reason, cause);
    }

    /**
     * Constructs a <code>JAXMException</code> object initialized
     * with the given <code>Throwable</code> object.
     *
     * @param cause a <code>Throwable</code> object that is to
     *        be embedded in this <code>JAXMException</code> object
     */
    public JAXMException(Throwable cause) {
	super(cause);
    }
}
