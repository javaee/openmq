/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * A marker interface for components (for example, servlets) that are 
 * intended to be consumers of one-way (asynchronous) JAXM messages.  
 * The receiver of a one-way message is sent the message in one operation,
 * and it sends the response in another separate operation. The time
 * interval between the receipt of a one-way message and the sending
 * of the response may be measured in fractions of seconds or days.
 * <P>
 * The implementation of the <code>onMessage</code> method defines
 * how the receiver responds to the <code>SOAPMessage</code> object
 * that was passed to the <code>onMessage</code> method.
 *
 * @see JAXMServlet
 * @see ReqRespListener
 */
public interface OnewayListener {

    /**
     * Passes the given <code>SOAPMessage</code> object to this
     * <code>OnewayListener</code> object.  
     * This method is invoked behind the scenes, typically by the
     * container (servlet or EJB container) after the messaging provider
     * delivers the message to the container. 
     *
     * It is expected that EJB Containers will deliver JAXM messages
     * to EJB components using message driven Beans that implement the
     * <code>javax.xml.messaging.OnewayListener</code> interface.
     *
     * @param message the <code>SOAPMessage</code> object to be passed to this
     *                <code>OnewayListener</code> object
     */
    public void onMessage(SOAPMessage message);
}
