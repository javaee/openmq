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

/*
 * @(#)BytesField.java	1.6 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console.util;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JComboBox;

import com.sun.messaging.jmq.util.SizeString;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/**
 * This class implements a field used to enter some amount of
 * bytes. It supports the selection of the byte unit:
 *
 * <UL>
 * <LI>MegaBytes
 * <LI>KiloBytes
 * <LI>Bytes
 * </UL>
 * The default unit displayed is KILOBYTES.
 * <P>
 *
 * The getValue() method returns the value entered in <STRONG>BYTES</STRONG>.
 */
public class BytesField extends JPanel {
    public final static int		BYTES		= 0;
    public final static int		KILOBYTES	= 1;
    public final static int		MEGABYTES	= 2;

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    private LongField	lf;
    private JComboBox	unitCB;

    public BytesField(long min, long max, String text) {
	this(min, max, text, 0);
    }

    public BytesField(long min, long max, int columns) {
	this(min, max, null, columns);
    }

    public BytesField(long min, long max, 
			String text, int columns) {
	initGui(min, max, text, columns);
	setUnit(KILOBYTES);
    }

    public void addActionListener(ActionListener l)  {
	lf.addActionListener(l);
    }

    public void setText(String s)  {
	lf.setText(s);
    }

    public String getText()  {
	return (lf.getText());
    }

    public void setEnabled(boolean b)  {
	lf.setEnabled(b);
	unitCB.setEnabled(b);
    }

    /**
     * Set the byte unit.
     *
     * @param unit	byte unit. Can be one of MEGABYTES,
     *			KILOBYTES, or BYTES
     */
    public void setUnit(int unit)  {
	if ((unit < 0) || (unit > MEGABYTES))  {
	    return;
	}

	unitCB.setSelectedIndex(unit);
    }

    /*
     * Returns the unit of the value i.e. BYTES, KILOBYTES, or
     * MEGABYTES.
     *
     * @return	The unit of the value.
     */
    public int getUnit()  {
	int selIndex = unitCB.getSelectedIndex();

	return (selIndex);
    }


    /**
     * Sets the bytes string using the format
     * recognized by the SizeString class.
     */
    public void setSizeString(String strVal)  {
	SizeString	ss;
	String		tmp = strVal.trim();
	long		val;
	int		unit;
	char		c;

	try  {
	    ss = new SizeString(tmp);
	} catch (Exception e)  {
	    /*
	     * Should not get here
	     */
	    return;
	}

	c = tmp.charAt(tmp.length() -1);

	if (Character.isLetter(c)) {
	    switch (c)  {
	    case 'm':
	    case 'M':
		val = ss.getMBytes();
	        unit = BytesField.MEGABYTES;
	    break;

	    case 'k':
	    case 'K':
		val = ss.getKBytes();
	        unit = BytesField.KILOBYTES;
	    break;

	    case 'b':
	    case 'B':
		val = ss.getBytes();
	        unit = BytesField.BYTES;
	    break;

	    default:
		val = 0;
	        unit = BytesField.BYTES;
	    break;
	    }
	} else  {
	    val = ss.getBytes();
	    unit = BytesField.BYTES;
	}

	setText(Long.toString(val));
	setUnit(unit);
    }

    /**
     * Returns the bytes string in the format recognized by
     * the SizeString class.
     */
    public String getSizeString()  {
	String	strValue = getText();
	int	unit = getUnit();
	String	unitStr;

	switch (unit)  {
	case BytesField.BYTES:
	    unitStr = "b";
	break;

	case BytesField.KILOBYTES:
	    unitStr = "k";
	break;

	case BytesField.MEGABYTES:
	    unitStr = "m";
	break;

	default:
	    unitStr = "b";
	break;
	}

	return (strValue + unitStr);
    }


    /**
     * Return the value in bytes.
     *
     * @return	The value entered in bytes.
     */
    public long getValue()  {
	String  s;
	int	selIndex;
	long	tmpLong;

	s = lf.getText();

	try  {
	    tmpLong = Long.parseLong(s);
	} catch (Exception e)  {
	    return (-1);
	}

	selIndex = unitCB.getSelectedIndex();

	switch (selIndex)  {
	case MEGABYTES:
	    return (tmpLong * 1048576);

	case KILOBYTES:
	    return (tmpLong * 1024);

	case BYTES:
	    return (tmpLong);

	default:
	    return (-1);
	}
    }

    private void initGui(long min, long max, String text, int collumns)  {
        String[] units;

	lf = new LongField(min, max, text, collumns);

	units = new String[ 3 ];
	units[MEGABYTES] = acr.getString(acr.I_MEGABYTES);
	units[KILOBYTES] = acr.getString(acr.I_KILOBYTES);
	units[BYTES] = acr.getString(acr.I_BYTES);

	unitCB = new JComboBox(units);
	setLayout(new BorderLayout());

	add(lf, "Center");
	add(unitCB, "East");
    }

}

