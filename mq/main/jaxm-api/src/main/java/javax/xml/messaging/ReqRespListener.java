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
 * A marker interface for components that are 
 * intended to be consumers of request-response messages.
 * In the request-response style of messaging, sending a request and receiving
 * the response are both done in a single operation. This means that the 
 * client sending the request cannot do anything else until after it has
 * received the response.
 * <P>
 * From the standpoint of the
 * sender, a message is sent via the <code>SOAPConnection</code> method
 * <code>call</code> in a point-to-point fashion.  The method <code>call</code>
 * blocks, waiting until it gets a  response message that it can return.  
 * The sender may be a standalone client, or it may be deployed in a container.
 * <P>
 * The receiver, typically a service operating in a servlet, implements the 
 * <code>ReqRespListener</code> method <code>onMessage</code> to specify
 * how to respond to the requests it receives.
 *<P>
 * It is possible that a standalone client might use the method <code>call</code>
 * to send a message that does not require a response.  For such cases,
 * the receiver must implement the method <code>onMessage</code> such that 
 * it returns a message whose only purpose is to unblock the 
 * <code>call</code> method.
 *
 * @see JAXMServlet
 * @see OnewayListener
 * @see javax.xml.soap.SOAPConnection#call
 */
public interface ReqRespListener {

    /**
     * Passes the given <code>SOAPMessage</code> object to this
     * <code>ReqRespListener</code> object and returns the response.  
     * This method is invoked behind the scenes, typically by the
     * container (servlet or EJB container) after the messaging provider
     * delivers the message to the container. 
     *
     * It is expected that EJB Containers will deliver JAXM messages
     * to EJB components using message driven Beans that implement the
     * <code>javax.xml.messaging.ReqRespListener</code> interface.
     *
     * @param message the <code>SOAPMessage</code> object to be passed to this
     *                <code>ReqRespListener</code> object
     *
     * @return the response. If this is <code>null</code>, then the
     *         original message is treated as a "oneway" message.
     */
    public SOAPMessage onMessage(SOAPMessage message);
}
