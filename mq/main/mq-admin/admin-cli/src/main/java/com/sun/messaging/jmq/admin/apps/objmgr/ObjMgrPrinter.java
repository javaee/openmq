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
 * @(#)ObjMgrPrinter.java	1.9 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.objmgr;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.util.MultiColumnPrinter;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminResources;

public class ObjMgrPrinter extends MultiColumnPrinter {

    private static AdminResources ar = Globals.getAdminResources();

    public ObjMgrPrinter(int numCol, int gap, String border, int align, boolean sort) {
	super(numCol, gap, border, align, sort);
    }

    public ObjMgrPrinter(int numCol, int gap, String border, int align) {
	super(numCol, gap, border, align);
    }

    public ObjMgrPrinter(int numCol, int gap, String border) {
	super(numCol, gap, border);
    }

    public ObjMgrPrinter(int numCol, int gap) {
	super(numCol, gap);
    }

    public ObjMgrPrinter(Hashtable h, int numCol, int gap) {
	super(numCol, gap);

	String[] row = new String[2];

        for (Enumeration e = h.keys();  e.hasMoreElements();) {
            String propName = (String)e.nextElement(),
                        propValue = (String)h.get(propName);

	    row[0] = propName;
	    row[1] = propValue;
            add(row);
        }
    }

    public void printJMSObject(Object obj) {

        if (obj instanceof com.sun.messaging.Topic) {
            Globals.stdOutPrintln(ar.getString(ar.I_TOPIC_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.Queue) {
            Globals.stdOutPrintln(ar.getString(ar.I_QUEUE_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.XATopicConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_XATOPIC_CF_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.XAQueueConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_XAQUEUE_CF_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.XAConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_XA_CF_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.TopicConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_TOPIC_CF_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.QueueConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_QUEUE_CF_ATTRS_HDR));
        }
        else if (obj instanceof com.sun.messaging.ConnectionFactory) {
            Globals.stdOutPrintln(ar.getString(ar.I_CF_ATTRS_HDR));
	}

        if (obj instanceof AdministeredObject)
            printObjPropertiesFromObj((AdministeredObject)obj);
    }

    /**
     * Prints the properties of the administered object in a nice
     * formatted 2 collumn table. The property names/values, as well
     * as property name labels are obtained from the passed object.
     */
    public void printObjPropertiesFromObj(AdministeredObject obj) {
        /*
         * Set the specified properties on the new object.
         */
        Properties props = obj.getConfiguration();
        for (Enumeration e = obj.enumeratePropertyNames(); e.hasMoreElements();) {

            String propName = (String)e.nextElement();

	    /*
	     * If an exception is caught while checking if a property is hidden
	     * the property will be displayed.
	     */
	    try  {
	        if (obj.isPropertyHidden(propName))  {
		    continue;
	        }
	    } catch(Exception ex)  {
	    }

            String value = props.getProperty(propName);
            String propLabel = "";

	    /*
	     * If an exception is caught while getting the property label,
	     * "" will be used instead as the label.
	     */
            try  {
                propLabel = obj.getPropertyLabel(propName);
            } catch (Exception ex)  {
            }

            String printLabel = propName + " [" + propLabel + "]";
            String printValue = value;

	    String[] row = new String[2];
            row[0] = printLabel;
            row[1] = printValue;
            add(row);
        }
	print();
    }

    /**
     * Prints the property names and values in a nice formatted 2
     * collumn table. The property names will also contain a
     * description/label if found by querying the passed administered
     * object.
     *
     * In this method, the administered object serves only as a way
     * to get the property name label. The property values will come
     * from the properties object. 
     *
     * This is used mostly for printing the printing the properties
     * modified by the 'update' operation.
     */
    public void printObjPropertiesFromProp(Properties p,
                                        AdministeredObject obj)  {

        for (Enumeration e = p.propertyNames();  e.hasMoreElements();)  {
            String propName = (String)e.nextElement(),
                value = p.getProperty(propName),
                propLabel, printLabel;

            try  {
                propLabel = obj.getPropertyLabel(propName);
            } catch (Exception ex)  {
                propLabel = "";
            }

            printLabel = propName + " [" + propLabel + "]";

            String[] row = new String[2];
            row[0] = printLabel;
            row[1] = value;
            add(row);
        }
	print();
    }

    public static void printReadOnly(String value)  {
        if (value != null)
            Globals.stdOutPrintln(ar.getString(ar.I_READONLY, value));
        else
            Globals.stdOutPrintln(ar.getString(ar.I_READONLY,
                                  Boolean.FALSE.toString()));
    }

    public static void printReadOnly(boolean value)  {
        if (value)
            Globals.stdOutPrintln(ar.getString(ar.I_READONLY,
                                  Boolean.TRUE.toString()));
        else
            Globals.stdOutPrintln(ar.getString(ar.I_READONLY,
                                  Boolean.FALSE.toString()));
    }

    public void doPrint(String str) {
        Globals.stdOutPrint(str);
    }

    public void doPrintln(String str) {
        Globals.stdOutPrintln(str);
    }
}
