/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)MQMBeanReadWrite.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.AttributeChangeNotification;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.InvalidAttributeValueException;

import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;

public abstract class MQMBeanReadWrite extends MQMBeanReadOnly {
    public MQMBeanReadWrite()  {
        super();
    }

    /**
     * Sets the value of the specified attribute of the Dynamic MBean.
     */
    public void setAttribute(Attribute attribute) throws
			AttributeNotFoundException,
			InvalidAttributeValueException,
			MBeanException,
			ReflectionException  {

	if (attribute == null)  {
	    throw new RuntimeOperationsException(
		new IllegalArgumentException(
			"MBean "
			+ getMBeanName()
			+ ": Null attribute passed to setAttribute()"));
        }

	String name = attribute.getName();
	String methodName = "set" + name;
	Object value = attribute.getValue();
	Method m = null;

	checkSettableAttribute(name, value);

	try  {
	    /*
	     * What if value is null ?
	     */
	    Class methodParams[] = { value.getClass() };

	    m = this.getClass().getMethod(methodName, methodParams);
	} catch(NoSuchMethodException noSuchE)  {
	    String tmp = "MBean "
			+ getMBeanName()
			+ ": Cannot find method "
			+ methodName;
            throw (new ReflectionException(noSuchE, tmp));
	} catch(SecurityException se)  {
	    throw (new ReflectionException(se));
	}

        try {
            Object params[] = { value };
            m.invoke(this, params);
        } catch(Exception e)  {
            throw (new MBeanException(e, e.toString()));
        }
    }

    public void logProblemGettingOldVal(String attr, Exception e)  {
        logger.log(Logger.ERROR,
            getMBeanName()
            + " notification "
            + AttributeChangeNotification.ATTRIBUTE_CHANGE
            + ": encountered problem while getting old value of attribute "
            + attr
            + ": " 
            + e);
    }


    private void checkSettableAttribute(String name, Object value) throws
		AttributeNotFoundException,
		InvalidAttributeValueException  {
        MBeanAttributeInfo attrInfo = getAttributeInfo(name);

	if (attrInfo == null)  {
	    throw new AttributeNotFoundException("The attribute "
			+ name
			+ " is not a valid attribute for MBean"
			+ getMBeanName());
	}

	if (!attrInfo.isWritable())  {
	    throw new AttributeNotFoundException("The attribute "
			+ name
			+ " is not a settable attribute for MBean"
			+ getMBeanName());
	}

	if (!attrInfo.getType().equals(value.getClass().getName()))  {
	    throw new InvalidAttributeValueException(
	"The type of the value used to set the attribute "
			+ name
			+ " is incorrect ("
			+ value.getClass().getName()
			+ ").\n"
			+ "The expected value type is "
			+ attrInfo.getType()
			+ ".");
	}
    }
}
