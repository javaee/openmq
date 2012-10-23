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
 * @(#)BrokerCmdStatusEvent.java	1.23 06/27/07
 */ 

package com.sun.messaging.jmq.admin.event;


/**
 * Event class indicating some common actions related to broker Management.
 * This class is subclassed by imqcmd (BrokerCmdStatusEvent), 
 *                             imqadmin (BrokerCmdStatusEvent)
 * and                         imqbridgemgr (BridgeCmdStatusEvent)
 *<P>
 */
public class CommonCmdStatusEvent extends AdminEvent {

    /********************************************************
     * CommonCmdStatusEvent event types
     * use integers >= 10000 to avoid overlap with subclasses
     ********************************************************/
    public final static int	BROKER_BUSY 		= 10000;

    private int			replyType;
    private String		replyTypeString;

    private boolean		success = true;

    private Exception		linkedException;

    private int			numRetriesAttempted = 0,
				maxNumRetries = 0;
    private long		retryTimeount = 0;

	private Object      returnedObject = null;

    /**
     * Creates an instance of CommonCmdStatusEvent
     * @param source the object where the event originated
     * @type the event type
     */
    public CommonCmdStatusEvent(Object source, int type) {
	super(source, type);
    }

    public void setSuccess(boolean b)  {
	success = b;
    }

    public boolean getSuccess()  {
	return (success);
    }

    public void setReturnedObject(Object obj) {
    this.returnedObject = obj;
    }
    public Object getReturnedObject() {
    return returnedObject;
    }

    public void setReplyType(int type)  {
	replyType = type;
    }
    public int getReplyType()  {
	return (replyType);
    }

    public void setReplyTypeString(String typeString)  {
	replyTypeString = typeString;
    }
    public String getReplyTypeString()  {
	return (replyTypeString);
    }

    public void setLinkedException(Exception e)  {
	linkedException = e;
    }
    public Exception getLinkedException()  {
	return (linkedException);
    }

    public void setNumRetriesAttempted(int numRetriesAttempted)  {
	this.numRetriesAttempted = numRetriesAttempted;
    }
    public int getNumRetriesAttempted()  {
	return (numRetriesAttempted);
    }

    public void setMaxNumRetries(int maxNumRetries)  {
	this.maxNumRetries = maxNumRetries;
    }
    public int getMaxNumRetries()  {
	return (maxNumRetries);
    }

    public void setRetryTimeount(long retryTimeount)  {
	this.retryTimeount = retryTimeount;
    }
    public long getRetryTimeount()  {
	return (retryTimeount);
    }
}
